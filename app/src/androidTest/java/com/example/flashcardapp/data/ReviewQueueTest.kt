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
        setId = library.createSet(owner, "Vocabulary", "", null)
    }

    @After
    fun tearDown() = db.close()

    // Explicit creation times make ties reproducible without waiting for the device clock.
    private suspend fun addCard(createdAt: Long, set: Long = setId): Long =
        db.libraryDao().insertCard(owner, set, "Front", "Back", createdAt)

    private suspend fun grade(card: Long, rating: ReviewGrade, on: LocalDate) {
        val reviewedAt = on.atStartOfDay(ZoneOffset.UTC)
        assertTrue(reviews.submitGrade(owner, "fixture", card, rating, reviewedAt = reviewedAt))
    }

    private suspend fun assertQueue(day: LocalDate, set: Long?, counts: ReviewCounts, expected: List<Long>) {
        assertEquals(counts, reviews.observeReviewCounts(owner, day, set).first())
        assertEquals(expected, reviews.getEligibleCardIds(owner, day, set))
    }

    @Test
    fun homeAndSetQueuesOrderDueAndNewCardsAndExcludeForeignAndFutureCards() = runBlocking {
        val secondSet = library.createSet(owner, "Grammar", "", null)
        val otherOwner = db.profileDao().insertProfile(Profile(displayName = "B", createdAt = 0))
        val foreignSet = library.createSet(otherOwner, "Vocabulary", "", null)
        val foreignCard = library.createCard(otherOwner, foreignSet, "Foreign", "Card")

        val newFirst = addCard(createdAt = 20)
        val dueFirst = addCard(createdAt = 90)
        val olderNew = addCard(createdAt = 10, set = secondSet)
        val dueSecond = addCard(createdAt = 30)
        val overdue = addCard(createdAt = 50, set = secondSet)
        val newSecond = addCard(createdAt = 20)
        val future = addCard(createdAt = 5)
        grade(dueFirst, ReviewGrade.REMEMBERED, today.minusDays(1))
        grade(dueSecond, ReviewGrade.FORGOT, today.minusDays(1))
        grade(overdue, ReviewGrade.REMEMBERED, today.minusDays(3))
        grade(future, ReviewGrade.REMEMBERED, today)

        assertQueue(today, null, ReviewCounts(3, 3), listOf(overdue, dueFirst, dueSecond, olderNew, newFirst, newSecond))
        assertQueue(today, setId, ReviewCounts(2, 2), listOf(dueFirst, dueSecond, newFirst, newSecond))
        assertQueue(today, secondSet, ReviewCounts(1, 1), listOf(overdue, olderNew))
        assertQueue(today, foreignSet, ReviewCounts(0, 0), emptyList())
        assertNull(reviews.getEligibleCard(owner, foreignCard, today))
        assertNull(reviews.getEligibleCard(owner, olderNew, today, setId))
        assertNull(reviews.getEligibleCard(owner, future, today))

        val rows = library.observeSets(owner, today.toEpochDay()).first().associateBy { it.id }
        assertEquals(SetWithCounts(setId, "Vocabulary", null, 5, 4), rows[setId])
        assertEquals(SetWithCounts(secondSet, "Grammar", null, 2, 2), rows[secondSet])
    }

    @Test
    fun forgottenCardBecomesDueTomorrowWithoutAWriteOrAChangeToItsSavedDate() = runBlocking {
        val card = addCard(createdAt = 10)
        grade(card, ReviewGrade.FORGOT, today)
        val tomorrow = today.plusDays(1)
        val saved = requireNotNull(db.reviewDao().getCard(owner, card))
        assertEquals(0, saved.stage)
        assertEquals(tomorrow.toEpochDay(), saved.dueDay)

        for (scope in listOf(null, setId)) {
            assertQueue(today, scope, ReviewCounts(0, 0), emptyList())
            assertQueue(tomorrow, scope, ReviewCounts(0, 1), listOf(card))
        }
        assertEquals(0, library.observeSets(owner, today.toEpochDay()).first().single().eligibleCount)
        assertEquals(1, library.observeSets(owner, tomorrow.toEpochDay()).first().single().eligibleCount)
        assertEquals(saved, db.reviewDao().getCard(owner, card))
    }

    @Test
    fun sameInstantInTwoZonesChangesEligibilityAndPreservesTheStoredDueDay() = runBlocking {
        val card = addCard(createdAt = 10)
        grade(card, ReviewGrade.REMEMBERED, today)
        val instant = Instant.parse("2026-11-24T18:00:00Z")
        val inLosAngeles = instant.atZone(ZoneId.of("America/Los_Angeles")).toLocalDate()
        val inHanoi = instant.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate()

        for (scope in listOf(null, setId)) {
            assertQueue(inLosAngeles, scope, ReviewCounts(0, 0), emptyList())
            assertQueue(inHanoi, scope, ReviewCounts(0, 1), listOf(card))
        }
        val storedCard = requireNotNull(library.observeCard(owner, setId, card).first())
        assertEquals(today.plusDays(1).toEpochDay(), storedCard.dueDay)
    }

    @Test
    fun snapshotKeepsOriginalSlotsWhenCardsDisappearOrBecomeIneligible() = runBlocking {
        val deleted = addCard(createdAt = 10)
        val forgotten = addCard(createdAt = 20)
        val remaining = addCard(createdAt = 30)
        val snapshot = reviews.getEligibleCardIds(owner, today, setId)

        assertTrue(library.deleteCard(owner, setId, deleted))
        grade(forgotten, ReviewGrade.FORGOT, today)
        val arrival = addCard(createdAt = 40)

        assertEquals(listOf(deleted, forgotten, remaining), snapshot)
        assertNull(reviews.getEligibleCard(owner, snapshot[0], today, setId))
        assertNull(reviews.getEligibleCard(owner, snapshot[1], today, setId))
        assertEquals(remaining, reviews.getEligibleCard(owner, snapshot[2], today, setId)?.id)
        assertQueue(today, setId, ReviewCounts(2, 0), listOf(remaining, arrival))
    }
}
