package com.ateflaw.legaldeadlines.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ateflaw.legaldeadlines.domain.model.Holiday
import com.ateflaw.legaldeadlines.domain.usecase.GetHolidaysUseCase
import com.ateflaw.legaldeadlines.domain.usecase.SyncHolidaysUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HolidaysUiState(
    val holidays: List<Holiday> = emptyList(),
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null
)

class HolidaysViewModel(
    private val getHolidaysUseCase: GetHolidaysUseCase,
    private val syncHolidaysUseCase: SyncHolidaysUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HolidaysUiState())
    val uiState: StateFlow<HolidaysUiState> = _uiState.asStateFlow()

    init {
        observeHolidays()
    }

    private fun observeHolidays() {
        viewModelScope.launch {
            getHolidaysUseCase().collect { holidayList ->
                _uiState.update { currentState ->
                    currentState.copy(
                        holidays = holidayList,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun syncHolidays() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncMessage = null) }
            val currentYear = LocalDate.now().year
            val result = syncHolidaysUseCase(currentYear)
            if (result.isSuccess) {
                val count = result.getOrDefault(0)
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncMessage = "تم تحديث جدول العطلات الرسمية بنجاح ($count عطلة)"
                    )
                }
            } else {
                val msg = result.exceptionOrNull()?.message ?: "تم الاعتماد على قاعدة البيانات المحلية المعتمدة"
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncMessage = msg
                    )
                }
            }

        }
    }

    fun dismissSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }

    companion object {
        fun provideFactory(
            getHolidaysUseCase: GetHolidaysUseCase,
            syncHolidaysUseCase: SyncHolidaysUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HolidaysViewModel(
                    getHolidaysUseCase,
                    syncHolidaysUseCase
                ) as T
            }
        }
    }
}
