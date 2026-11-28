package com.example.flashcardapp.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.flashcardapp.domain.ReviewGrade
import com.example.flashcardapp.domain.isEligibleForReview
import com.example.flashcardapp.domain.nextReviewSchedule
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

data class ReviewCounts(val newCount: Int, val dueCount: Int)

data class SessionCounts(val remembered: Int, val forgot: Int)

@Dao
interface ReviewDao {
    @Query("""
        SELECT COUNT(CASE WHEN c.dueDay IS NULL THEN 1 END) AS newCount,
               COUNT(CASE WHEN c.dueDay <= :today THEN 1 END) AS dueCount
        FROM flashcards AS c
        JOIN study_sets AS s ON s.id = c.setId
        WHERE s.ownerId = :profileId AND s.id = :setId
    """)
    fun observeReviewCounts(profileId: Long, setId: Long, today: Long): Flow<ReviewCounts>

    @Query("""
        SELECT c.id FROM flashcards AS c
        JOIN study_sets AS s ON s.id = c.setId
        WHERE s.ownerId = :profileId AND s.id = :setId
          AND (c.dueDay IS NULL OR c.dueDay <= :today)
        ORDER BY c.dueDay IS NULL, c.dueDay,
                 CASE WHEN c.dueDay IS NULL THEN c.createdAt END, c.id
    """)
    suspend fun getEligibleCardIds(profileId: Long, setId: Long, today: Long): List<Long>

    @Query("""
        SELECT c.* FROM flashcards AS c
        JOIN study_sets AS s ON s.id = c.setId
        WHERE s.ownerId = :profileId AND s.id = :setId AND c.id = :cardId
    """)
    suspend fun getCard(profileId: Long, setId: Long, cardId: Long): Flashcard?

    @Query("""
        SELECT COUNT(CASE WHEN r.grade = 'REMEMBERED' THEN 1 END) AS remembered,
               COUNT(CASE WHEN r.grade = 'FORGOT' THEN 1 END) AS forgot
        FROM review_logs AS r
        JOIN flashcards AS c ON c.id = r.cardId
        JOIN study_sets AS s ON s.id = c.setId
        WHERE s.ownerId = :profileId AND s.id = :setId AND r.sessionId = :sessionId
    """)
    suspend fun getSessionCounts(profileId: Long, setId: Long, sessionId: String): SessionCounts

    @Query("""
        SELECT EXISTS (
            SELECT 1 FROM review_logs AS r
            JOIN flashcards AS c ON c.id = r.cardId
            JOIN study_sets AS s ON s.id = c.setId
            WHERE s.ownerId = :profileId AND s.id = :setId
              AND r.sessionId = :sessionId AND r.cardId = :cardId
        )
    """)
    suspend fun hasGrade(profileId: Long, setId: Long, sessionId: String, cardId: Long): Boolean

    // cardId is required. A missing or foreign card makes this insert fail.
    @Query("""
        INSERT INTO review_logs (sessionId, cardId, grade, reviewedAt)
        VALUES (:sessionId, (
            SELECT c.id FROM flashcards AS c
            JOIN study_sets AS s ON s.id = c.setId
            WHERE s.ownerId = :profileId AND s.id = :setId AND c.id = :cardId
        ), :grade, :reviewedAt)
    """)
    suspend fun insertGrade(
        profileId: Long,
        setId: Long,
        sessionId: String,
        cardId: Long,
        grade: ReviewGrade,
        reviewedAt: Long
    )

    @Query("""
        UPDATE flashcards SET stage = :stage, dueDay = :dueDay
        WHERE id = :cardId AND setId = (
            SELECT s.id FROM study_sets AS s WHERE s.ownerId = :profileId AND s.id = :setId
        )
    """)
    suspend fun updateSchedule(profileId: Long, setId: Long, cardId: Long, stage: Int, dueDay: Long): Int

    @Transaction
    suspend fun submitGrade(
        profileId: Long,
        setId: Long,
        sessionId: String,
        cardId: Long,
        grade: ReviewGrade,
        reviewedAt: Long,
        reviewDate: LocalDate
    ): Boolean {
        val card = getCard(profileId, setId, cardId) ?: return false
        if (hasGrade(profileId, setId, sessionId, cardId)) return true

        val dueDate = card.dueDay?.let(LocalDate::ofEpochDay)
        if (!isEligibleForReview(dueDate, reviewDate)) return false

        val schedule = nextReviewSchedule(card.stage, grade, reviewDate)
        insertGrade(profileId, setId, sessionId, cardId, grade, reviewedAt)
        val updated = updateSchedule(profileId, setId, cardId, schedule.stage, schedule.dueDate.toEpochDay())
        check(updated == 1) { "Could not update the graded card's schedule." }
        return true
    }
}
