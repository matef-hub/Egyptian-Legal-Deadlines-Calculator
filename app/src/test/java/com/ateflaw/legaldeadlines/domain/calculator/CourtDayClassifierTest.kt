package com.ateflaw.legaldeadlines.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CourtDayClassifierTest {

    @Test
    fun fridayIsWeeklyRestEvenWithoutOfficialHolidayRecord() {
        assertEquals(
            CourtDayType.WEEKLY_REST,
            CourtDayClassifier.classify(LocalDate.of(2026, 11, 13), isOfficialHoliday = false)
        )
    }

    @Test
    fun ordinaryThursdayIsWorkingDay() {
        assertEquals(
            CourtDayType.WORKING_DAY,
            CourtDayClassifier.classify(LocalDate.of(2026, 11, 12), isOfficialHoliday = false)
        )
    }

    @Test
    fun ordinarySaturdayIsWorkingDayUnderCurrentProceduralCalendar() {
        assertEquals(
            CourtDayType.WORKING_DAY,
            CourtDayClassifier.classify(LocalDate.of(2026, 11, 14), isOfficialHoliday = false)
        )
    }

    @Test
    fun recordedOfficialHolidayOnSaturdayTakesPrecedenceOverWorkingDay() {
        assertEquals(
            CourtDayType.OFFICIAL_HOLIDAY,
            CourtDayClassifier.classify(LocalDate.of(2026, 11, 14), isOfficialHoliday = true)
        )
    }

    @Test
    fun fridayRemainsWeeklyRestEvenIfAlsoInHolidayTable() {
        assertEquals(
            CourtDayType.WEEKLY_REST,
            CourtDayClassifier.classify(LocalDate.of(2026, 11, 13), isOfficialHoliday = true)
        )
    }
}
