package com.ateflaw.legaldeadlines.domain.model

import java.time.LocalDate

/**
 * Result output of the Egyptian legal deadline calculation engine.
 */
data class DeadlineResult(
    val startDate: LocalDate,
    val provisionalDate: LocalDate,
    val duration: Long,
    val unit: DurationUnit,
    val ruleDistanceDays: Int,
    val additionalDistanceDays: Int,
    val totalDistanceDays: Int,
    val excludedDays: List<ExcludedDay>,
    val finalDeadline: LocalDate,
    val explanation: String,
    val lawArticle: String,
    val notes: String
)
