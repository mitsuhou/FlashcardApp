package com.example.flashcardapp.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.flashcardapp.data.local.FlashcardDatabase
import com.example.flashcardapp.data.local.Profile
import com.example.flashcardapp.data.local.ReviewCounts
import com.example.flashcardapp.data.local.SetWithCounts
import com.example.flashcardapp.domain.ReviewGrade
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewQueueTest {
    private val today = LocalDate.of(2026, 11, 24)
    private lateinit var db: FlashcardDatabase
    private lateinit var reviews: ReviewRepository
    private lateinit var library: LibraryRepository
    private var owner = 0L
    private var setId = 0L

    @Before
    fun setUp() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, FlashcardDatabase::class.java).build()
        reviews = ReviewRepository(db.reviewDao())
        library = LibraryRepository(db.libraryDao())
        owner = db.profileDao().insertProfile(Profile(displayName = "A", createdAt = 0))
        setId = library.createSet(owner, "Vocabulary", "")
    }

    @After
    fun tearDown() = db.close()

    // Explicit creation times make ties reproducible without waiting for the device clock.
    private suspend fun addCard(createdAt: Long, set: Long = setId): Long =
        db.libraryDao().insertCard(owner, set, "Front", "Back", createdAt)

    private suspend fun grade(card: Long, rating: ReviewGrade, on: LocalDate, set: Long = setId) {
        val reviewedAt = on.atStartOfDay(ZoneOffset.UTC)
        assertTrue(reviews.submitGrade(owner, set, "fixture", card, rating, reviewedAt))
    }

    private suspend fun assertQueue(day: LocalDate, set: Long, counts: ReviewCounts, expected: List<Long>) {
        assertEquals(counts, reviews.observeReviewCounts(owner, set, day).first())
        assertEquals(expected, reviews.getEligibleCardIds(owner, set, day))
    }

    @Test
    fun setQueuesOrderDueAndNewCardsAndExcludeOtherSetsAndFutureCards() = runBlocking {
        val secondSet = library.createSet(owner, "Grammar", "")
        val emptySet = library.createSet(owner, "Empty", "")
        val otherOwner = db.profileDao().insertProfile(Profile(displayName = "B", createdAt = 0))
        val foreignSet = library.createSet(otherOwner, "Vocabulary", "")
        val foreignCard = library.createCard(otherOwner, foreignSet, "Foreign", "Card")

        val newFirst = addCard(createdAt = 20)
        val dueFirst = addCard(createdAt = 90)
        val olderNew = addCard(createdAt = 10, set = secondSet)
        val dueSecond = addCard(createdAt = 30)
        val overdue = addCard(createdAt = 50)
        val siblingDue = addCard(createdAt = 60, set = secondSet)
        val newSecond = addCard(createdAt = 20)
        val future = addCard(createdAt = 5)
        grade(dueFirst, ReviewGrade.REMEMBERED, today.minusDays(1))
        grade(dueSecond, ReviewGrade.FORGOT, today.minusDays(1))
        grade(overdue, ReviewGrade.REMEMBERED, today.minusDays(3))
        grade(siblingDue, ReviewGrade.REMEMBERED, today.minusDays(1), set = secondSet)
        grade(future, ReviewGrade.REMEMBERED, today)

        assertQueue(today, setId, ReviewCounts(2, 3), listOf(overdue, dueFirst, dueSecond, newFirst, newSecond))
        assertQueue(today, secondSet, ReviewCounts(1, 1), listOf(siblingDue, olderNew))
        assertQueue(today, emptySet, ReviewCounts(0, 0), emptyList())
        assertQueue(today, foreignSet, ReviewCounts(0, 0), emptyList())
        assertNull(reviews.getEligibleCard(owner, foreignSet, foreignCard, today))
        assertNull(reviews.getEligibleCard(owner, setId, olderNew, today))
        assertNull(reviews.getEligibleCard(owner, setId, future, today))

        val rows = library.observeSets(owner, today.toEpochDay()).first().associateBy { it.id }
        assertEquals(SetWithCounts(setId, "Vocabulary", 6, 5), rows[setId])
        assertEquals(SetWithCounts(secondSet, "Grammar", 2, 2), rows[secondSet])
        assertEquals(SetWithCounts(emptySet, "Empty", 0, 0), rows[emptySet])
        assertEquals(rows[setId], library.observeSetCounts(owner, setId, today.toEpochDay()).first())
        assertEquals(rows[emptySet], library.observeSetCounts(owner, emptySet, today.toEpochDay()).first())
    }

    @Test
    fun forgottenCardBecomesDueTomorrowWithoutAWriteOrAChangeToItsSavedDate() = runBlocking {
        val card = addCard(createdAt = 10)
        grade(card, ReviewGrade.FORGOT, today)
        val tomorrow = today.plusDays(1)
        val saved = requireNotNull(db.reviewDao().getCard(owner, setId, card))
        assertEquals(0, saved.stage)
        assertEquals(tomorrow.toEpochDay(), saved.dueDay)

        assertQueue(today, setId, ReviewCounts(0, 0), emptyList())
        assertQueue(tomorrow, setId, ReviewCounts(0, 1), listOf(card))
        assertEquals(0, library.observeSets(owner, today.toEpochDay()).first().single().eligibleCount)
        assertEquals(1, library.observeSets(owner, tomorrow.toEpochDay()).first().single().eligibleCount)
        assertEquals(0, library.observeSetCounts(owner, setId, today.toEpochDay()).first()?.eligibleCount)
        assertEquals(1, library.observeSetCounts(owner, setId, tomorrow.toEpochDay()).first()?.eligibleCount)
        assertEquals(saved, db.reviewDao().getCard(owner, setId, card))
    }

    @Test
    fun sameInstantInTwoZonesChangesEligibilityAndPreservesTheStoredDueDay() = runBlocking {
        val card = addCard(createdAt = 10)
        grade(card, ReviewGrade.REMEMBERED, today)
        val instant = Instant.parse("2026-11-24T18:00:00Z")
        val inLosAngeles = instant.atZone(ZoneId.of("America/Los_Angeles")).toLocalDate()
        val inHanoi = instant.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate()

        assertQueue(inLosAngeles, setId, ReviewCounts(0, 0), emptyList())
        assertQueue(inHanoi, setId, ReviewCounts(0, 1), listOf(card))
        val storedCard = requireNotNull(library.observeCard(owner, setId, card).first())
        assertEquals(today.plusDays(1).toEpochDay(), storedCard.dueDay)
    }

    @Test
    fun snapshotKeepsOriginalSlotsWhenCardsDisappearOrBecomeIneligible() = runBlocking {
        val deleted = addCard(createdAt = 10)
        val forgotten = addCard(createdAt = 20)
        val remaining = addCard(createdAt = 30)
        val snapshot = reviews.getEligibleCardIds(owner, setId, today)

        assertTrue(library.deleteCard(owner, setId, deleted))
        grade(forgotten, ReviewGrade.FORGOT, today)
        val arrival = addCard(createdAt = 40)

        assertEquals(listOf(deleted, forgotten, remaining), snapshot)
        assertNull(reviews.getEligibleCard(owner, setId, snapshot[0], today))
        assertNull(reviews.getEligibleCard(owner, setId, snapshot[1], today))
        assertEquals(remaining, reviews.getEligibleCard(owner, setId, snapshot[2], today)?.id)
        assertQueue(today, setId, ReviewCounts(2, 0), listOf(remaining, arrival))
    }
}
