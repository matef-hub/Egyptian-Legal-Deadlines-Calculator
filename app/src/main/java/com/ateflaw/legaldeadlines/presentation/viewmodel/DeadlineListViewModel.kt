package com.ateflaw.legaldeadlines.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ateflaw.legaldeadlines.domain.model.SavedDeadline
import com.ateflaw.legaldeadlines.domain.usecase.DeleteSavedDeadlineUseCase
import com.ateflaw.legaldeadlines.domain.usecase.GetSavedDeadlinesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeadlineListViewModel(
    private val getSavedDeadlinesUseCase: GetSavedDeadlinesUseCase,
    private val deleteSavedDeadlineUseCase: DeleteSavedDeadlineUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeadlineListUiState(isLoading = true))
    val uiState: StateFlow<DeadlineListUiState> = _uiState.asStateFlow()

    init {
        loadSavedDeadlines()
    }

    private fun loadSavedDeadlines() {
        viewModelScope.launch {
            try {
                getSavedDeadlinesUseCase().collect { list ->
                    _uiState.update { it.copy(deadlines = list, isLoading = false, errorMessage = null) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "تعذر تحميل المواعيد المحفوظة: ${e.localizedMessage}") }
            }
        }
    }

    fun onSelectDeadline(deadline: SavedDeadline?) {
        _uiState.update { it.copy(selectedDeadline = deadline) }
    }

    fun requestDeleteConfirmation(deadline: SavedDeadline) {
        _uiState.update { it.copy(deleteConfirmationDeadline = deadline) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(deleteConfirmationDeadline = null) }
    }

    fun confirmDelete() {
        val deadlineToDelete = _uiState.value.deleteConfirmationDeadline ?: return
        viewModelScope.launch {
            try {
                deleteSavedDeadlineUseCase(deadlineToDelete)
                _uiState.update {
                    it.copy(
                        deleteConfirmationDeadline = null,
                        selectedDeadline = if (it.selectedDeadline?.id == deadlineToDelete.id) null else it.selectedDeadline
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        deleteConfirmationDeadline = null,
                        errorMessage = "تعذر حذف الميعاد: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun dismissErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        fun provideFactory(
            getSavedDeadlinesUseCase: GetSavedDeadlinesUseCase,
            deleteSavedDeadlineUseCase: DeleteSavedDeadlineUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DeadlineListViewModel(
                    getSavedDeadlinesUseCase,
                    deleteSavedDeadlineUseCase
                ) as T
            }
        }
    }
}
