package com.ateflaw.legaldeadlines.domain.calculator

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * One day-status policy for the calculator and the visible "today" indicator.
 *
 * The ordinary Egyptian procedural calendar in this application treats Friday
 * as weekly rest and Saturday as a working day. A separately recorded official
 * holiday is observed on any weekday, including Saturday.
 *
 * This status is informational: the applicable legal rule still determines
 * whether and how the deadline rolls forward.
 */
enum class CourtDayType {
    WEEKLY_REST,
    OFFICIAL_HOLIDAY,
    WORKING_DAY
}

object CourtDayClassifier {
    fun classify(date: LocalDate, isOfficialHoliday: Boolean): CourtDayType = when {
        date.dayOfWeek == DayOfWeek.FRIDAY -> CourtDayType.WEEKLY_REST
        isOfficialHoliday -> CourtDayType.OFFICIAL_HOLIDAY
        else -> CourtDayType.WORKING_DAY
    }
}
