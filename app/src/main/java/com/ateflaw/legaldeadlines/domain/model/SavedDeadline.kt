package com.ateflaw.legaldeadlines.domain.model

import java.time.LocalDate

/**
 * Domain model representing a saved deadline calculation record.
 */
data class SavedDeadline(
    val id: Long = 0,
    val caseNumber: String = "",
    val clientName: String = "",
    val actionName: String,
    val announcementDate: LocalDate,
    val duration: Long,
    val unit: DurationUnit,
    val distanceDays: Int,
    val finalDeadline: LocalDate,
    val lawArticle: String,
    val calculationExplanation: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val proactiveReminderDays: Int? = null
)
