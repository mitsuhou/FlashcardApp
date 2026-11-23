package com.example.flashcardapp.domain

import java.time.LocalDate
import kotlin.math.roundToInt

enum class ReviewGrade {
    REMEMBERED,
    FORGOT
}

data class ReviewSchedule(val stage: Int, val dueDate: LocalDate)

private val rememberedIntervals = listOf(1L, 3L, 7L, 14L, 30L, 60L)

fun isEligibleForReview(dueDate: LocalDate?, today: LocalDate): Boolean =
    dueDate == null || dueDate <= today

fun nextReviewSchedule(currentStage: Int, grade: ReviewGrade, reviewDate: LocalDate): ReviewSchedule {
    require(currentStage in 0..rememberedIntervals.size)
    if (grade == ReviewGrade.FORGOT) {
        return ReviewSchedule(stage = 0, dueDate = reviewDate.plusDays(1))
    }

    val nextStage = (currentStage + 1).coerceAtMost(rememberedIntervals.size)
    val daysUntilReview = rememberedIntervals[nextStage - 1]
    return ReviewSchedule(nextStage, reviewDate.plusDays(daysUntilReview))
}

fun recallRatePercent(remembered: Int, forgot: Int): Int? {
    require(remembered >= 0 && forgot >= 0)
    val reviewed = remembered.toLong() + forgot
    if (reviewed == 0L) return null
    return (100.0 * remembered / reviewed).roundToInt()
}
