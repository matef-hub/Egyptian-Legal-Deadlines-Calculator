package com.ateflaw.legaldeadlines.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ateflaw.legaldeadlines.data.seed.LegalRuleSeedData
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.SavedDeadline
import com.ateflaw.legaldeadlines.domain.usecase.CalculateDeadlineUseCase
import com.ateflaw.legaldeadlines.domain.usecase.GetHolidaysUseCase
import com.ateflaw.legaldeadlines.domain.usecase.GetLegalRulesUseCase
import com.ateflaw.legaldeadlines.domain.usecase.SaveDeadlineUseCase
import com.ateflaw.legaldeadlines.domain.usecase.SyncHolidaysUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(
    private val getLegalRulesUseCase: GetLegalRulesUseCase,
    private val getHolidaysUseCase: GetHolidaysUseCase,
    private val calculateDeadlineUseCase: CalculateDeadlineUseCase,
    private val saveDeadlineUseCase: SaveDeadlineUseCase,
    private val syncHolidaysUseCase: SyncHolidaysUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadLegalRules()
        loadHolidayStatus()
        triggerBackgroundSync()
    }

    private fun loadLegalRules() {
        viewModelScope.launch {
            if (getLegalRulesUseCase.getRulesCount() == 0) {
                getLegalRulesUseCase.insertRules(LegalRuleSeedData.getSeedRules())
            }
            getLegalRulesUseCase().collect { rulesList ->
                if (rulesList.isEmpty()) {
                    getLegalRulesUseCase.insertRules(LegalRuleSeedData.getSeedRules())
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            rules = rulesList,
                            selectedRule = currentState.selectedRule ?: rulesList.firstOrNull()
                        )
                    }
                }
            }
        }
    }

    private fun loadHolidayStatus() {
        viewModelScope.launch {
            if (getHolidaysUseCase.getCount() == 0) {
                getHolidaysUseCase.insertHolidays(LegalRuleSeedData.getInitialOfficialHolidays())
            }
            val today = LocalDate.now()
            val isHoliday = getHolidaysUseCase.isHoliday(today)
            val holidayName = if (isHoliday) getHolidaysUseCase.getHolidayName(today) else null
            val count = getHolidaysUseCase.getCount()

            _uiState.update {
                it.copy(
                    todayDate = today,
                    isTodayHoliday = isHoliday,
                    todayHolidayName = holidayName,
                    totalHolidaysCount = count
                )
            }

            // Observe holidays changes to keep count updated
            getHolidaysUseCase().collect { holidays ->
                val updatedCount = holidays.size
                val updatedIsHoliday = holidays.any { it.holidayDate == today }
                val updatedName = holidays.firstOrNull { it.holidayDate == today }?.name
                _uiState.update {
                    it.copy(
                        totalHolidaysCount = updatedCount,
                        isTodayHoliday = updatedIsHoliday,
                        todayHolidayName = updatedName
                    )
                }
            }
        }
    }

    private fun triggerBackgroundSync() {
        viewModelScope.launch {
            val currentYear = LocalDate.now().year
            syncHolidaysUseCase(currentYear)
        }
    }

    fun onRuleSelected(rule: LegalRule?) {
        _uiState.update { it.copy(selectedRule = rule, errorMessage = null) }
    }

    fun onAnnouncementDateChanged(date: LocalDate) {
        _uiState.update { it.copy(announcementDate = date, errorMessage = null) }
    }

    fun onAdditionalDistanceDaysChanged(input: String) {
        // Allow digits only
        val filtered = input.filter { it.isDigit() }
        _uiState.update { it.copy(additionalDistanceDaysInput = filtered, errorMessage = null) }
    }

    fun onCaseNumberChanged(caseNum: String) {
        _uiState.update { it.copy(caseNumber = caseNum) }
    }

    fun onClientNameChanged(name: String) {
        _uiState.update { it.copy(clientName = name) }
    }

    fun calculateDeadline() {
        val currentState = _uiState.value
        val rule = currentState.selectedRule

        if (rule == null) {
            _uiState.update { it.copy(errorMessage = "يرجى اختيار الإجراء القانوني أولاً") }
            return
        }

        val distanceDays = currentState.additionalDistanceDaysInput.toIntOrNull()
        if (distanceDays == null || distanceDays < 0) {
            _uiState.update { it.copy(errorMessage = "يرجى إدخال ميعاد مسافة إضافي صحيح (رقم غير سالب)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCalculating = true, errorMessage = null, isSavedSuccessfully = false) }
            try {
                val calculationResult = calculateDeadlineUseCase(
                    announcementDate = currentState.announcementDate,
                    rule = rule,
                    additionalDistanceDays = distanceDays
                )
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        result = calculationResult,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        errorMessage = "حدث خطأ أثناء احتساب الميعاد: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun resetCalculation() {
        _uiState.update { it.copy(result = null, isSavedSuccessfully = false, errorMessage = null) }
    }

    fun saveCurrentDeadline() {
        val currentState = _uiState.value
        val result = currentState.result ?: return
        val rule = currentState.selectedRule ?: return

        viewModelScope.launch {
            try {
                val deadlineToSave = SavedDeadline(
                    caseNumber = currentState.caseNumber.trim(),
                    clientName = currentState.clientName.trim(),
                    actionName = rule.actionName,
                    announcementDate = currentState.announcementDate,
                    duration = result.duration,
                    unit = result.unit,
                    distanceDays = result.totalDistanceDays,
                    finalDeadline = result.finalDeadline,
                    lawArticle = result.lawArticle,
                    calculationExplanation = result.explanation,
                    notes = result.notes,
                    createdAt = System.currentTimeMillis()
                )

                saveDeadlineUseCase(deadlineToSave)
                _uiState.update {
                    it.copy(
                        isSavedSuccessfully = true,
                        successMessage = "تم حفظ الميعاد بنجاح في قائمة المواعيد المحفوظة"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "تعذر حفظ الميعاد: ${e.localizedMessage}")
                }
            }
        }
    }

    fun dismissMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    companion object {
        fun provideFactory(
            getLegalRulesUseCase: GetLegalRulesUseCase,
            getHolidaysUseCase: GetHolidaysUseCase,
            calculateDeadlineUseCase: CalculateDeadlineUseCase,
            saveDeadlineUseCase: SaveDeadlineUseCase,
            syncHolidaysUseCase: SyncHolidaysUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(
                    getLegalRulesUseCase,
                    getHolidaysUseCase,
                    calculateDeadlineUseCase,
                    saveDeadlineUseCase,
                    syncHolidaysUseCase
                ) as T
            }
        }
    }
}
