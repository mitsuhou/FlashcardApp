package com.example.flashcardapp.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.flashcardapp.data.local.FlashcardDatabase
import com.example.flashcardapp.data.local.Profile
import com.example.flashcardapp.data.local.SessionCounts
import com.example.flashcardapp.domain.ReviewGrade
import java.time.ZonedDateTime
import java.util.UUID
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GradeCommitTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "grade-test-${UUID.randomUUID()}"
    private val reviewedAt = ZonedDateTime.parse("2026-11-24T12:00:00+07:00")
    private val today = reviewedAt.toLocalDate()
    private lateinit var db: FlashcardDatabase
    private lateinit var review: ReviewRepository
    private var profileId = 0L
    private var setId = 0L
    private var cardId = 0L

    private fun openDatabase() {
        db = Room.databaseBuilder(context, FlashcardDatabase::class.java, databaseName).build()
        review = ReviewRepository(db.reviewDao())
    }

    @Before
    fun setUp() = runBlocking {
        openDatabase()
        profileId = db.profileDao().insertProfile(Profile(displayName = "A", createdAt = 0))
        val library = LibraryRepository(db.libraryDao())
        setId = library.createSet(profileId, "Greetings", "", null)
        cardId = library.createCard(profileId, setId, "Hello", "Xin chào")
    }

    @After
    fun tearDown() {
        db.close()
        context.deleteDatabase(databaseName)
    }

    private suspend fun grade(card: Long, rating: ReviewGrade): Boolean =
        review.submitGrade(profileId, "session", card, rating, setId, reviewedAt)

    @Test
    fun failedScheduleUpdateRollsBackAndRetrySurvivesReopen() = runBlocking {
        val library = LibraryRepository(db.libraryDao())
        val forgotten = library.createCard(profileId, setId, "Thanks", "Cảm ơn")
        val outstanding = library.createCard(profileId, setId, "Goodbye", "Tạm biệt")
        assertTrue(grade(forgotten, ReviewGrade.FORGOT))
        val original = requireNotNull(db.reviewDao().getCard(profileId, cardId))
        val initialQueue = review.getEligibleCardIds(profileId, today)
        val sqlite = db.openHelper.writableDatabase
        // Fail the second write only when the first write is visible inside the transaction.
        sqlite.execSQL("""
            CREATE TRIGGER fail_schedule BEFORE UPDATE OF stage, dueDay ON flashcards
            WHEN EXISTS (SELECT 1 FROM review_logs WHERE sessionId = 'session' AND cardId = NEW.id)
            BEGIN SELECT RAISE(ABORT, 'test schedule failure'); END
        """)
        try {
            grade(cardId, ReviewGrade.REMEMBERED)
            fail("The schedule update should fail")
        } catch (error: SQLiteConstraintException) {
            assertTrue(error.message.orEmpty().contains("test schedule failure"))
        }
        assertEquals(original, db.reviewDao().getCard(profileId, cardId))
        assertEquals(SessionCounts(0, 1), review.getSessionCounts(profileId, "session"))
        assertEquals(initialQueue, review.getEligibleCardIds(profileId, today))

        sqlite.execSQL("DROP TRIGGER fail_schedule")
        assertTrue(grade(cardId, ReviewGrade.REMEMBERED))
        assertTrue(grade(cardId, ReviewGrade.FORGOT))
        db.close()
        openDatabase()

        assertEquals(SessionCounts(1, 1), review.getSessionCounts(profileId, "session"))
        assertEquals(original.copy(stage = 1, dueDay = today.plusDays(1).toEpochDay()),
            db.reviewDao().getCard(profileId, cardId))
        assertEquals(listOf(outstanding), review.getEligibleCardIds(profileId, today))
        assertEquals(listOf(cardId, forgotten, outstanding), review.getEligibleCardIds(profileId, today.plusDays(1)))
    }

    @Test
    fun competingGradesCommitOneLogAndOneScheduleUpdate() = runBlocking {
        // A second update of this new card would fail even if it wrote the same schedule.
        db.openHelper.writableDatabase.execSQL("""
            CREATE TRIGGER reject_second_schedule BEFORE UPDATE OF stage, dueDay ON flashcards
            WHEN OLD.dueDay IS NOT NULL
            BEGIN SELECT RAISE(ABORT, 'second schedule update'); END
        """)
        val remembered = async { grade(cardId, ReviewGrade.REMEMBERED) }
        val forgot = async { grade(cardId, ReviewGrade.FORGOT) }
        assertTrue(remembered.await())
        assertTrue(forgot.await())

        val counts = review.getSessionCounts(profileId, "session")
        assertEquals(1, counts.remembered + counts.forgot)
        val card = requireNotNull(db.reviewDao().getCard(profileId, cardId))
        val expectedStage = if (counts.remembered == 1) 1 else 0
        assertEquals(expectedStage, card.stage)
        assertEquals(today.plusDays(1).toEpochDay(), card.dueDay)
    }
}
