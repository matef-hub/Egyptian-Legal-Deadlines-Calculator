package com.ateflaw.legaldeadlines.domain.model

/**
 * Units of duration supported for legal deadlines.
 */
enum class DurationUnit(val arabicDisplayName: String) {
    DAYS("أيام"),
    MONTHS("أشهر"),
    YEARS("سنوات"),
    HOURS("ساعات")
}
