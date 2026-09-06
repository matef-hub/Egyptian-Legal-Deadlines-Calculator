package com.ateflaw.legaldeadlines.presentation.viewmodel

import com.ateflaw.legaldeadlines.domain.model.DeadlineResult
import com.ateflaw.legaldeadlines.domain.model.Holiday
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import java.time.LocalDate

data class MainUiState(
    val rules: List<LegalRule> = emptyList(),
    val selectedRule: LegalRule? = null,
    val announcementDate: LocalDate = LocalDate.now(),
    val additionalDistanceDaysInput: String = "0",
    val caseNumber: String = "",
    val clientName: String = "",

    val todayDate: LocalDate = LocalDate.now(),
    val isTodayHoliday: Boolean = false,
    val todayHolidayName: String? = null,
    val totalHolidaysCount: Int = 0,

    val isCalculating: Boolean = false,
    val result: DeadlineResult? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSavedSuccessfully: Boolean = false
)
