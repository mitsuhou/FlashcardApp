package com.example.flashcardapp.data

import com.example.flashcardapp.data.local.Flashcard
import com.example.flashcardapp.data.local.ReviewCounts
import com.example.flashcardapp.data.local.ReviewDao
import com.example.flashcardapp.domain.isEligibleForReview
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class ReviewRepository(private val reviewDao: ReviewDao) {
    fun observeReviewCounts(profileId: Long, today: LocalDate, setId: Long? = null): Flow<ReviewCounts> =
        reviewDao.observeReviewCounts(profileId, today.toEpochDay(), setId)

    // Read once at review start. An empty list keeps the learner at the entry screen.
    suspend fun getEligibleCardIds(profileId: Long, today: LocalDate, setId: Long? = null): List<Long> =
        reviewDao.getEligibleCardIds(profileId, today.toEpochDay(), setId)

    suspend fun getEligibleCard(
        profileId: Long,
        cardId: Long,
        today: LocalDate,
        setId: Long? = null
    ): Flashcard? {
        val card = reviewDao.getCard(profileId, cardId, setId) ?: return null
        val dueDate = card.dueDay?.let(LocalDate::ofEpochDay)
        return if (isEligibleForReview(dueDate, today)) card else null
    }
}
