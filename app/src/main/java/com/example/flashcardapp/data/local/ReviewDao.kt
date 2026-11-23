package com.example.flashcardapp.data.local

import androidx.room.Dao
import androidx.room.Query
import com.example.flashcardapp.domain.ReviewGrade
import kotlinx.coroutines.flow.Flow

data class ReviewCounts(val newCount: Int, val dueCount: Int)

data class SessionCounts(val remembered: Int, val forgot: Int)

@Dao
interface ReviewDao {
    // A null setId selects the whole profile for a review started from Home.
    @Query("""
        SELECT COUNT(CASE WHEN c.dueDay IS NULL THEN 1 END) AS newCount,
               COUNT(CASE WHEN c.dueDay <= :today THEN 1 END) AS dueCount
        FROM flashcards AS c
        JOIN study_sets AS s ON s.id = c.setId
        WHERE s.ownerId = :profileId AND (:setId IS NULL OR s.id = :setId)
    """)
    fun observeReviewCounts(profileId: Long, today: Long, setId: Long? = null): Flow<ReviewCounts>

    @Query("""
        SELECT c.id FROM flashcards AS c
        JOIN study_sets AS s ON s.id = c.setId
        WHERE s.ownerId = :profileId AND (:setId IS NULL OR s.id = :setId)
          AND (c.dueDay IS NULL OR c.dueDay <= :today)
        ORDER BY c.dueDay IS NULL, c.dueDay,
                 CASE WHEN c.dueDay IS NULL THEN c.createdAt END, c.id
    """)
    suspend fun getEligibleCardIds(profileId: Long, today: Long, setId: Long? = null): List<Long>

    @Query("""
        SELECT c.* FROM flashcards AS c
        JOIN study_sets AS s ON s.id = c.setId
        WHERE c.id = :cardId AND s.ownerId = :profileId AND (:setId IS NULL OR s.id = :setId)
    """)
    suspend fun getCard(profileId: Long, cardId: Long, setId: Long? = null): Flashcard?

    @Query("""
        SELECT COUNT(CASE WHEN r.grade = 'REMEMBERED' THEN 1 END) AS remembered,
               COUNT(CASE WHEN r.grade = 'FORGOT' THEN 1 END) AS forgot
        FROM review_logs AS r
        JOIN flashcards AS c ON c.id = r.cardId
        JOIN study_sets AS s ON s.id = c.setId
        WHERE s.ownerId = :profileId AND r.sessionId = :sessionId
    """)
    suspend fun getSessionCounts(profileId: Long, sessionId: String): SessionCounts

    @Query("""
        SELECT EXISTS (
            SELECT 1 FROM review_logs AS r
            JOIN flashcards AS c ON c.id = r.cardId
            JOIN study_sets AS s ON s.id = c.setId
            WHERE s.ownerId = :profileId AND r.sessionId = :sessionId AND r.cardId = :cardId
        )
    """)
    suspend fun hasGrade(profileId: Long, sessionId: String, cardId: Long): Boolean

    // cardId is required. A missing or foreign card makes this insert fail.
    @Query("""
        INSERT INTO review_logs (sessionId, cardId, grade, reviewedAt)
        VALUES (:sessionId, (
            SELECT c.id FROM flashcards AS c
            JOIN study_sets AS s ON s.id = c.setId
            WHERE c.id = :cardId AND s.ownerId = :profileId AND (:setId IS NULL OR s.id = :setId)
        ), :grade, :reviewedAt)
    """)
    suspend fun insertGrade(
        profileId: Long,
        setId: Long?,
        sessionId: String,
        cardId: Long,
        grade: ReviewGrade,
        reviewedAt: Long
    )

    @Query("""
        UPDATE flashcards SET stage = :stage, dueDay = :dueDay
        WHERE id = :cardId AND setId IN (
            SELECT id FROM study_sets WHERE ownerId = :profileId AND (:setId IS NULL OR id = :setId)
        )
    """)
    suspend fun updateSchedule(profileId: Long, setId: Long?, cardId: Long, stage: Int, dueDay: Long): Int
}
