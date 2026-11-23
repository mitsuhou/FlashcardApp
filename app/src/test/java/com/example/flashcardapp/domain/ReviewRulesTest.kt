package com.example.flashcardapp.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewRulesTest {
    @Test
    fun eligibility_includesNewDueAndOverdueCards() {
        val today = LocalDate.of(2026, 11, 24)

        assertTrue(isEligibleForReview(null, today))
        assertTrue(isEligibleForReview(today.minusDays(1), today))
        assertTrue(isEligibleForReview(today, today))
        assertFalse(isEligibleForReview(today.plusDays(1), today))
    }

    @Test
    fun remembered_advancesThroughSixIntervalsAndStaysAtStageSix() {
        val reviewDay = LocalDate.of(2026, 1, 1)
        val expectedStages = listOf(1, 2, 3, 4, 5, 6, 6)
        val expectedDays = listOf(1L, 3L, 7L, 14L, 30L, 60L, 60L)

        for (stage in 0..6) {
            val schedule = nextReviewSchedule(stage, ReviewGrade.REMEMBERED, reviewDay)
            assertEquals("From stage $stage", expectedStages[stage], schedule.stage)
            assertEquals("From stage $stage", reviewDay.plusDays(expectedDays[stage]), schedule.dueDate)
        }
    }

    @Test
    fun forgot_fromEveryStageResetsToZeroAndBecomesEligibleTomorrow() {
        val reviewDay = LocalDate.of(2026, 12, 31)
        val tomorrow = LocalDate.of(2027, 1, 1)

        for (stage in 0..6) {
            val schedule = nextReviewSchedule(stage, ReviewGrade.FORGOT, reviewDay)
            assertEquals(0, schedule.stage)
            assertEquals(tomorrow, schedule.dueDate)
            assertFalse(isEligibleForReview(schedule.dueDate, reviewDay))
            assertTrue(isEligibleForReview(schedule.dueDate, tomorrow))
        }
    }

    @Test
    fun lateReview_schedulesFromActualReviewDate() {
        val oldDueDate = LocalDate.of(2026, 11, 22)
        val reviewDay = LocalDate.of(2026, 11, 24)

        assertTrue(isEligibleForReview(oldDueDate, reviewDay))
        val schedule = nextReviewSchedule(1, ReviewGrade.REMEMBERED, reviewDay)
        assertEquals(2, schedule.stage)
        assertEquals(LocalDate.of(2026, 11, 27), schedule.dueDate)
    }

    @Test
    fun schedule_countsLeapDayAndCrossesMonthEnd() {
        val february28 = LocalDate.of(2024, 2, 28)
        val leapDay = nextReviewSchedule(0, ReviewGrade.REMEMBERED, february28)
        assertEquals(LocalDate.of(2024, 2, 29), leapDay.dueDate)

        val march = nextReviewSchedule(leapDay.stage, ReviewGrade.REMEMBERED, leapDay.dueDate)
        assertEquals(LocalDate.of(2024, 3, 3), march.dueDate)
    }

    @Test
    fun eligibility_usesSuppliedLocalDayAcrossDaylightSavingChange() {
        val newYork = ZoneId.of("America/New_York")
        val reviewDay = LocalDate.of(2024, 3, 10)
        val schedule = nextReviewSchedule(0, ReviewGrade.REMEMBERED, reviewDay)
        val instant = Instant.parse("2024-03-11T03:30:00Z")

        assertEquals(LocalDate.of(2024, 3, 11), schedule.dueDate)
        assertFalse(isEligibleForReview(schedule.dueDate, instant.atZone(newYork).toLocalDate()))
        assertTrue(isEligibleForReview(schedule.dueDate, instant.atZone(ZoneId.of("UTC")).toLocalDate()))
    }

    @Test
    fun recallRate_omitsEmptySessionsAndRoundsCommittedGrades() {
        assertNull(recallRatePercent(remembered = 0, forgot = 0))
        assertEquals(0, recallRatePercent(remembered = 0, forgot = 1))
        assertEquals(33, recallRatePercent(remembered = 1, forgot = 2))
        assertEquals(50, recallRatePercent(remembered = 1, forgot = 1))
        assertEquals(67, recallRatePercent(remembered = 2, forgot = 1))
        assertEquals(100, recallRatePercent(remembered = 1, forgot = 0))
    }
}
