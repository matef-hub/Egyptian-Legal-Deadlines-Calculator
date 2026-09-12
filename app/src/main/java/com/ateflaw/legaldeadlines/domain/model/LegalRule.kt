package com.ateflaw.legaldeadlines.domain.model

/**
 * Domain model representing an Egyptian procedural legal rule.
 */
data class LegalRule(
    val id: Long = 0,
    val actionName: String,
    val duration: Long,
    val unit: DurationUnit,
    val distanceDays: Int = 0,
    val startRule: StartRule = StartRule.NEXT_DAY,
    val lawArticle: String = "",
    val notes: String = "",
    val summary: String = ""
)
