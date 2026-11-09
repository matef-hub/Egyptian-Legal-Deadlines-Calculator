package com.ateflaw.legaldeadlines.domain.model

import java.time.LocalDate

/**
 * Domain model representing an official Egyptian holiday.
 */
data class Holiday(
    val id: Long = 0,
    val holidayDate: LocalDate,
    val name: String
)
