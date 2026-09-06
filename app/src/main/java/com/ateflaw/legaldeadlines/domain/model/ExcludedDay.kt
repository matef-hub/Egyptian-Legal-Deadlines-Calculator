package com.ateflaw.legaldeadlines.domain.model

import java.time.LocalDate

/**
 * Domain model representing a single day skipped during deadline adjustment.
 */
data class ExcludedDay(
    val date: LocalDate,
    val reason: String
)
