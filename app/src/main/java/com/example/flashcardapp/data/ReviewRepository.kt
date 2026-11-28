package com.example.flashcardapp.data

import com.example.flashcardapp.data.local.Flashcard
import com.example.flashcardapp.data.local.ReviewCounts
import com.example.flashcardapp.data.local.ReviewDao
import com.example.flashcardapp.data.local.SessionCounts
import com.example.flashcardapp.domain.ReviewGrade
import com.example.flashcardapp.domain.isEligibleForReview
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.Flow

class ReviewRepository(private val reviewDao: ReviewDao) {
    fun observeReviewCounts(profileId: Long, setId: Long, today: LocalDate): Flow<ReviewCounts> =
        reviewDao.observeReviewCounts(profileId, setId, today.toEpochDay())

    // Read once at review start. An empty list keeps the learner at the entry screen.
    suspend fun getEligibleCardIds(profileId: Long, setId: Long, today: LocalDate): List<Long> =
        reviewDao.getEligibleCardIds(profileId, setId, today.toEpochDay())

    suspend fun getEligibleCard(
        profileId: Long,
        setId: Long,
        cardId: Long,
        today: LocalDate
    ): Flashcard? {
        val card = reviewDao.getCard(profileId, setId, cardId) ?: return null
        val dueDate = card.dueDay?.let(LocalDate::ofEpochDay)
        return if (isEligibleForReview(dueDate, today)) card else null
    }

    // True means the session has a committed grade for this card, including after a retry.
    // False means the card is unavailable. Storage failures propagate to the caller.
    suspend fun submitGrade(
        profileId: Long,
        setId: Long,
        sessionId: String,
        cardId: Long,
        grade: ReviewGrade,
        reviewedAt: ZonedDateTime = ZonedDateTime.now()
    ): Boolean = reviewDao.submitGrade(
        profileId = profileId,
        setId = setId,
        sessionId = sessionId,
        cardId = cardId,
        grade = grade,
        reviewedAt = reviewedAt.toInstant().toEpochMilli(),
        reviewDate = reviewedAt.toLocalDate()
    )

    suspend fun getSessionCounts(profileId: Long, setId: Long, sessionId: String): SessionCounts =
        reviewDao.getSessionCounts(profileId, setId, sessionId)
}
