package com.ateflaw.legaldeadlines.presentation.viewmodel

import com.ateflaw.legaldeadlines.domain.model.SavedDeadline

data class DeadlineListUiState(
    val deadlines: List<SavedDeadline> = emptyList(),
    val selectedDeadline: SavedDeadline? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val deleteConfirmationDeadline: SavedDeadline? = null
)
