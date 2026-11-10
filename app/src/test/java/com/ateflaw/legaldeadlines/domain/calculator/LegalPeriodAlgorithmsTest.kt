package com.ateflaw.legaldeadlines.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class LegalPeriodAlgorithmsTest {
    @Test fun forwardDaysHandlesInclusionExplicitly() {
        val date = LocalDate.of(2026, 5, 10)
        assertEquals(LocalDate.of(2026, 5, 20), LegalPeriodAlgorithms.forwardDays(date, 10, true))
        assertEquals(LocalDate.of(2026, 5, 19), LegalPeriodAlgorithms.forwardDays(date, 10, false))
    }

    @Test fun backwardCalculationLeavesThreeCompleteDaysBeforeHearing() {
        val hearing = LocalDate.of(2026, 5, 10)
        assertEquals(LocalDate.of(2026, 5, 6), LegalPeriodAlgorithms.beforeEventWithClearDays(hearing, 3))
    }

    @Test fun hourlyCalculationUsesElapsedTimeNotRoundedDates() {
        val zone = ZoneId.of("Africa/Cairo")
        val event = ZonedDateTime.of(2026, 4, 23, 21, 30, 0, 0, zone)
        val deadline = LegalPeriodAlgorithms.addElapsedHours(event, 24)
        assertEquals(24L, ChronoUnit.HOURS.between(event.toInstant(), deadline.toInstant()))
    }

    @Test fun ageAnniversaryDoesNotApplyWeekendCourtExtension() {
        assertEquals(LocalDate.of(2025, 2, 28),
            LegalPeriodAlgorithms.anniversary(LocalDate.of(2024, 2, 29), 1))
    }

    @Test fun explicitCourtClosingCalendarCanIncludeSaturday() {
        val saturday = LocalDate.of(2026, 10, 3)
        assertEquals(DayOfWeek.SATURDAY, saturday.dayOfWeek)
        assertEquals(LocalDate.of(2026, 10, 5), LegalPeriodAlgorithms.nextAvailableDate(saturday) {
            it.dayOfWeek == DayOfWeek.FRIDAY || it.dayOfWeek == DayOfWeek.SATURDAY
        })
    }

    @Test fun backwardClosurePolicyMovesEarlierNeverTowardHearing() {
        val saturday = LocalDate.of(2026, 10, 3)
        assertEquals(LocalDate.of(2026, 10, 1), LegalPeriodAlgorithms.previousAvailableDate(saturday) {
            it.dayOfWeek == DayOfWeek.FRIDAY || it.dayOfWeek == DayOfWeek.SATURDAY
        })
    }

    @Test fun invalidArithmeticFailsClosed() {
        assertThrows(IllegalArgumentException::class.java) {
            LegalPeriodAlgorithms.forwardDays(LocalDate.of(2026, 1, 1), 0, true)
        }
        assertThrows(IllegalArgumentException::class.java) {
            LegalPeriodAlgorithms.beforeEventWithClearDays(LocalDate.of(2026, 1, 1), -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            LegalPeriodAlgorithms.nextAvailableDate(LocalDate.of(2026, 1, 1)) { true }
        }
    }
}
