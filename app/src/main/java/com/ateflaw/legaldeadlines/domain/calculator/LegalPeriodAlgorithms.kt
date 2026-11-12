package com.ateflaw.legaldeadlines.domain.calculator

import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * Pure, policy-free date arithmetic for future reviewed legal-rule types.
 * A mathematical result is not a legal deadline until its trigger, inclusions,
 * court calendar, suspension rules and statutory source are independently approved.
 */
object LegalPeriodAlgorithms {
    /** For 10 days after an event, excluding its day, the result is event + 10. */
    fun forwardDays(event: LocalDate, days: Long, excludeEventDay: Boolean): LocalDate {
        require(days > 0) { "Duration must be positive." }
        return event.plusDays(days - if (excludeEventDay) 0 else 1)
    }

    /**
     * Last submission date leaving [clearDays] COMPLETE calendar dates strictly
     * between submission and hearing. This date must still be checked against
     * the expressly selected legal policy for court closing days.
     */
    fun beforeEventWithClearDays(event: LocalDate, clearDays: Long): LocalDate {
        require(clearDays >= 0) { "Clear-day count cannot be negative." }
        return event.minusDays(Math.addExact(clearDays, 1))
    }

    /** Exact elapsed hours across clock and daylight-saving transitions. */
    fun addElapsedHours(event: ZonedDateTime, hours: Long): ZonedDateTime {
        require(hours > 0) { "Hour count must be positive." }
        return event.plusHours(hours)
    }

    /**
     * Pure civil anniversary. Does not assert that this is the legally applicable
     * attainment-of-age rule, particularly for 29 February births.
     */
    fun anniversary(birthDate: LocalDate, years: Int): LocalDate {
        require(years >= 0) { "Age cannot be negative." }
        return birthDate.plusYears(years.toLong())
    }

    /**
     * Court closures are caller-supplied, not guessed from the day name.
     * Abort pathological calendars instead of infinite loops.
     */
    fun nextAvailableDate(start: LocalDate, isClosed: (LocalDate) -> Boolean): LocalDate {
        var day = start
        repeat(366) {
            if (!isClosed(day)) return day
            day = day.plusDays(1)
        }
        throw IllegalArgumentException("No available court date within 366 days.")
    }

    fun previousAvailableDate(start: LocalDate, isClosed: (LocalDate) -> Boolean): LocalDate {
        var day = start
        repeat(366) {
            if (!isClosed(day)) return day
            day = day.minusDays(1)
        }
        throw IllegalArgumentException("No available court date within 366 days.")
    }
}
