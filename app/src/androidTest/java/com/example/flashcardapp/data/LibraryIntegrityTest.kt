package com.example.flashcardapp.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.flashcardapp.data.local.FlashcardDatabase
import com.example.flashcardapp.data.local.Profile
import com.example.flashcardapp.data.local.ReviewLog
import com.example.flashcardapp.domain.ReviewGrade
import java.time.ZonedDateTime
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryIntegrityTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "library-test-${UUID.randomUUID()}"
    private val reviewedAt = ZonedDateTime.parse("2026-11-24T12:00:00+07:00")
    private val today = reviewedAt.toLocalDate().toEpochDay()
    private lateinit var db: FlashcardDatabase
    private lateinit var library: LibraryRepository

    private data class LibraryIds(val profile: Long, val group: Long, val set: Long, val card: Long)
    private lateinit var a: LibraryIds
    private lateinit var b: LibraryIds

    private fun openDatabase() {
        db = Room.databaseBuilder(context, FlashcardDatabase::class.java, databaseName).build()
        library = LibraryRepository(db.libraryDao())
    }

    private fun reopenDatabase() {
        db.close()
        openDatabase()
    }

    @Before
    fun setUp() = runBlocking {
        openDatabase()
        a = createLibrary("A")
        b = createLibrary("B")
    }

    @After
    fun tearDown() {
        db.close()
        context.deleteDatabase(databaseName)
    }

    private suspend fun createLibrary(name: String): LibraryIds {
        val profile = db.profileDao().insertProfile(Profile(displayName = name, createdAt = 0))
        val group = library.createGroup(profile, "Languages", "Vocabulary")
        val set = library.createSet(profile, "Greetings", "Common words", group)
        val card = createReviewedCard(profile, set)
        return LibraryIds(profile, group, set, card)
    }

    private suspend fun createReviewedCard(profile: Long, set: Long): Long {
        val card = library.createCard(profile, set, "Hello", "Xin chào")
        val review = ReviewRepository(db.reviewDao())
        assertTrue(review.submitGrade(profile, "session-$profile", card, ReviewGrade.REMEMBERED, set, reviewedAt))
        return card
    }

    // Inspect the tables directly so an orphaned card or log cannot be hidden by an ownership join.
    private fun storedCardIds(): List<Long> =
        db.openHelper.readableDatabase.query("SELECT id FROM flashcards ORDER BY id").use { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.getLong(0)) }
        }

    private fun storedLogs(): List<ReviewLog> {
        val sql = "SELECT sessionId, cardId, grade, reviewedAt FROM review_logs ORDER BY cardId, sessionId"
        return db.openHelper.readableDatabase.query(sql).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val grade = ReviewGrade.valueOf(cursor.getString(2))
                    add(ReviewLog(cursor.getString(0), cursor.getLong(1), grade, cursor.getLong(3)))
                }
            }
        }
    }

    @Test
    fun matchingNamesAcrossProfiles_doNotAllowForeignReadsOrWrites() = runBlocking {
        for (owner in listOf(a, b)) {
            assertEquals(listOf(owner.group), library.observeGroups(owner.profile).first().map { it.id })
            assertEquals(listOf(owner.set), library.observeSets(owner.profile, today).first().map { it.id })
            assertEquals(listOf(owner.card), library.observeCards(owner.profile, owner.set).first().map { it.id })
        }
        assertNull(library.observeGroup(b.profile, a.group).first())
        assertNull(library.observeSet(b.profile, a.set).first())
        assertNull(library.observeCard(b.profile, a.set, a.card).first())
        assertFalse(library.updateGroup(b.profile, a.group, "Changed", ""))
        assertFalse(library.updateSet(b.profile, a.set, "Changed", "", b.group))
        assertFalse(library.moveSet(b.profile, a.set, b.group))
        assertFalse(library.updateCardText(b.profile, a.set, a.card, "Changed", "Changed"))
        assertFalse(library.deleteCard(b.profile, a.set, a.card))
        assertFalse(library.deleteSet(b.profile, a.set))
        assertFalse(library.deleteGroup(b.profile, a.group))
        try {
            library.createCard(b.profile, a.set, "Wrong owner", "Back")
            fail("Creating a card in another profile's set should fail")
        } catch (_: SQLiteConstraintException) {
            // The insert requires an owned parent set.
        }
        assertEquals("Languages", library.observeGroup(a.profile, a.group).first()?.name)
        assertEquals(a.group, library.observeSet(a.profile, a.set).first()?.groupId)
        assertEquals("Hello", library.observeCard(a.profile, a.set, a.card).first()?.front)
        assertEquals(listOf(a.card, b.card), storedCardIds())
    }

    @Test
    fun foreignGroupIsRejectedOnInsertEditAndMove() = runBlocking {
        try {
            library.createSet(a.profile, "Invalid", "", b.group)
            fail("The composite foreign key should reject another profile's group")
        } catch (_: SQLiteConstraintException) {
            // The Room insert goes directly through the composite foreign key.
        }
        assertFalse(library.updateSet(a.profile, a.set, "Invalid", "", b.group))
        assertFalse(library.moveSet(a.profile, a.set, b.group))
        assertEquals(listOf(a.set), library.observeSets(a.profile, today).first().map { it.id })
        assertEquals(a.group, library.observeSet(a.profile, a.set).first()?.groupId)
    }

    @Test
    fun descriptionsAreTrimmedOnCreateAndEditAndClearedOnBlank() = runBlocking {
        val limit = "x".repeat(500)
        val group = library.createGroup(a.profile, "Group", "  $limit  ")
        val set = library.createSet(a.profile, "Set", "  $limit  ", group)
        reopenDatabase()
        assertEquals(limit, library.observeGroup(a.profile, group).first()?.description)
        assertEquals(limit, library.observeSet(a.profile, set).first()?.description)

        assertTrue(library.updateGroup(a.profile, group, "Group", "  edited group  "))
        assertTrue(library.updateSet(a.profile, set, "Set", "  edited set  ", group))
        val tooLong = limit + "x"
        val groupError = runCatching { library.updateGroup(a.profile, group, "Group", tooLong) }.exceptionOrNull()
        val setError = runCatching { library.updateSet(a.profile, set, "Set", tooLong, group) }.exceptionOrNull()
        assertTrue(groupError is IllegalArgumentException)
        assertTrue(setError is IllegalArgumentException)
        assertEquals("edited group", library.observeGroup(a.profile, group).first()?.description)
        assertEquals("edited set", library.observeSet(a.profile, set).first()?.description)

        assertTrue(library.updateGroup(a.profile, group, "Group", " \t "))
        assertTrue(library.updateSet(a.profile, set, "Set", " \n ", group))
        reopenDatabase()
        assertNull(requireNotNull(library.observeGroup(a.profile, group).first()).description)
        assertNull(requireNotNull(library.observeSet(a.profile, set).first()).description)
    }

    @Test
    fun editingMovingAndDeletingGroupPreservesCardsAndHistoryAfterReopen() = runBlocking {
        val target = library.createGroup(a.profile, "Target", "")
        val sibling = library.createSet(a.profile, "Sibling", "", target)
        val originalSet = requireNotNull(library.observeSet(a.profile, a.set).first())
        val originalCard = requireNotNull(db.reviewDao().getCard(a.profile, a.card))
        val otherCard = requireNotNull(db.reviewDao().getCard(b.profile, b.card))
        val logs = storedLogs()
        assertEquals(2, logs.size)
        assertTrue(library.updateCardText(a.profile, a.set, a.card, "Goodbye", "Tạm biệt"))
        assertTrue(library.updateSet(a.profile, a.set, "Edited", "Notes", a.group))
        assertTrue(library.moveSet(a.profile, a.set, target))
        assertEquals(target, library.observeSet(a.profile, a.set).first()?.groupId)
        assertTrue(library.deleteGroup(a.profile, target))

        reopenDatabase()
        assertNull(library.observeGroup(a.profile, target).first())
        val ungrouped = library.observeUngroupedSets(a.profile, today).first()
        assertEquals(setOf(a.set, sibling), ungrouped.map { it.id }.toSet())
        val expectedSet = originalSet.copy(name = "Edited", description = "Notes", groupId = null)
        val expectedCard = originalCard.copy(front = "Goodbye", back = "Tạm biệt")
        assertEquals(expectedSet, library.observeSet(a.profile, a.set).first())
        assertEquals(expectedCard, db.reviewDao().getCard(a.profile, a.card))
        assertEquals(otherCard, db.reviewDao().getCard(b.profile, b.card))
        assertEquals(logs, storedLogs())
    }

    @Test
    fun deleteCardRemovesOnlyItsHistory() = runBlocking {
        val sibling = createReviewedCard(a.profile, a.set)
        val survivingLogs = storedLogs().filter { it.cardId != a.card }
        assertTrue(library.deleteCard(a.profile, a.set, a.card))
        reopenDatabase()
        assertEquals(listOf(sibling), library.observeCards(a.profile, a.set).first().map { it.id })
        assertEquals(listOf(b.card, sibling), storedCardIds())
        assertEquals(survivingLogs, storedLogs())
    }

    @Test
    fun deleteSetRemovesOnlyItsCardsAndHistory() = runBlocking {
        val sibling = library.createSet(a.profile, "Other set", "", a.group)
        val siblingCard = createReviewedCard(a.profile, sibling)
        val survivingLogs = storedLogs().filter { it.cardId != a.card }
        assertTrue(library.deleteSet(a.profile, a.set))
        reopenDatabase()
        assertEquals(listOf(sibling), library.observeSets(a.profile, today, a.group).first().map { it.id })
        assertEquals(listOf(b.card, siblingCard), storedCardIds())
        assertEquals(survivingLogs, storedLogs())
    }

    @Test
    fun deleteProfileRemovesItsLibraryAndPreservesTheOtherProfile() = runBlocking {
        val survivor = requireNotNull(db.profileDao().getProfile(b.profile))
        val group = requireNotNull(library.observeGroup(b.profile, b.group).first())
        val set = requireNotNull(library.observeSet(b.profile, b.set).first())
        val card = requireNotNull(db.reviewDao().getCard(b.profile, b.card))
        val survivingLogs = storedLogs().filter { it.cardId == b.card }
        assertEquals(1, db.profileDao().deleteProfile(a.profile))
        reopenDatabase()
        assertEquals(listOf(survivor), db.profileDao().observeProfiles().first())
        assertTrue(library.observeGroups(a.profile).first().isEmpty())
        assertTrue(library.observeSets(a.profile, today).first().isEmpty())
        assertEquals(group, library.observeGroup(b.profile, b.group).first())
        assertEquals(set, library.observeSet(b.profile, b.set).first())
        assertEquals(card, db.reviewDao().getCard(b.profile, b.card))
        assertEquals(listOf(b.card), storedCardIds())
        assertEquals(survivingLogs, storedLogs())
    }
}
