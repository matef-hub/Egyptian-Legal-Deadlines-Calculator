package com.ateflaw.legaldeadlines.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ateflaw.legaldeadlines.data.seed.LegalRuleSeedData
import com.ateflaw.legaldeadlines.domain.model.DistanceType
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.SavedDeadline
import com.ateflaw.legaldeadlines.domain.usecase.CalculateDeadlineUseCase
import com.ateflaw.legaldeadlines.domain.usecase.GetHolidaysUseCase
import com.ateflaw.legaldeadlines.domain.usecase.GetLegalRulesUseCase
import com.ateflaw.legaldeadlines.domain.usecase.SaveDeadlineUseCase
import com.ateflaw.legaldeadlines.domain.usecase.SyncHolidaysUseCase
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils.toArabicDigits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalculatorViewModel(
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
                        // Re-resolve the selected rule after a legal-data update.
                        // Do not keep a stale rule object in an existing calculation.
                        val selectedRule = currentState.selectedRule?.let { selected ->
                            rulesList.firstOrNull { it.id == selected.id }
                        }
                        val nextState = currentState.copy(rules = rulesList, selectedRule = selectedRule)
                        if (selectedRule != currentState.selectedRule) nextState.invalidateCalculation() else nextState
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

            // A changed holiday calendar invalidates any previously calculated final date.
            var previousHolidays: List<com.ateflaw.legaldeadlines.domain.model.Holiday>? = null
            getHolidaysUseCase().collect { holidays ->
                val holidaysChanged = previousHolidays != null && previousHolidays != holidays
                previousHolidays = holidays
                val updatedCount = holidays.size
                val updatedIsHoliday = holidays.any { it.holidayDate == today }
                val updatedName = holidays.firstOrNull { it.holidayDate == today }?.name
                _uiState.update {
                    val nextState = it.copy(
                        totalHolidaysCount = updatedCount,
                        isTodayHoliday = updatedIsHoliday,
                        todayHolidayName = updatedName
                    )
                    if (holidaysChanged) nextState.invalidateCalculation() else nextState
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
        _uiState.update {
            val forbidDistance = rule?.distancePolicy?.equals("NOT_APPLICABLE", ignoreCase = true) == true
            it.copy(
                selectedRule = rule,
                distanceType = if (forbidDistance) DistanceType.NONE else it.distanceType,
                additionalDistanceDaysInput = if (forbidDistance) "0" else it.additionalDistanceDaysInput
            ).invalidateCalculation()
        }
    }

    fun onAnnouncementDateChanged(date: LocalDate) {
        _uiState.update { it.copy(announcementDate = date).invalidateCalculation() }
    }

    fun onDistanceTypeChanged(type: DistanceType) {
        _uiState.update {
            val defaultDays = when (type) {
                DistanceType.NONE -> "0"
                DistanceType.LOCAL -> {
                    val current = it.additionalDistanceDaysInput.toIntOrNull()
                    if (current == null || current !in 1..4) "1" else it.additionalDistanceDaysInput
                }
                DistanceType.BORDER_AREA -> "15"
                DistanceType.ABROAD -> "60"
                DistanceType.ABROAD_JUDICIAL_REDUCTION -> {
                    val current = it.additionalDistanceDaysInput.toIntOrNull()
                    if (current == null || current !in 1..59) "30" else it.additionalDistanceDaysInput
                }
                DistanceType.ABROAD_SERVED_IN_EGYPT -> "0"
            }
            it.copy(distanceType = type, additionalDistanceDaysInput = defaultDays).invalidateCalculation()
        }
    }

    fun onAdditionalDistanceDaysChanged(input: String) {
        val filtered = input.filter { it.isDigit() }
        _uiState.update { it.copy(additionalDistanceDaysInput = filtered).invalidateCalculation() }
    }

    fun onLocalDistanceKmChanged(kmStr: String) {
        val km = kmStr.toDoubleOrNull() ?: 0.0
        val calculatedDays = DistanceType.calculateLocalDistanceDays(km)
        _uiState.update {
            it.copy(
                localDistanceKmInput = kmStr,
                additionalDistanceDaysInput = calculatedDays.toString()
            ).invalidateCalculation()
        }
    }

    fun onJudicialOrderInputChanged(orderText: String) {
        _uiState.update { it.copy(judicialOrderInput = orderText).invalidateCalculation() }
    }

    fun onCaseNumberChanged(caseNum: String) {
        _uiState.update { it.copy(caseNumber = caseNum, isSavedSuccessfully = false, successMessage = null) }
    }

    fun onClientNameChanged(name: String) {
        _uiState.update { it.copy(clientName = name, isSavedSuccessfully = false, successMessage = null) }
    }

    fun calculateDeadline() {
        val currentState = _uiState.value
        if (currentState.isCalculating) return
        val rule = currentState.selectedRule

        if (rule == null) {
            _uiState.update { it.copy(errorMessage = "يرجى اختيار الإجراء القانوني أولاً") }
            return
        }

        val distanceType = currentState.distanceType
        val distanceDays: Int = when (distanceType) {
            DistanceType.NONE -> 0
            DistanceType.LOCAL -> {
                val days = currentState.additionalDistanceDaysInput.toIntOrNull()
                if (days == null || days !in 0..4) {
                    _uiState.update {
                        it.copy(errorMessage = "ميعاد المسافة المحلي لا يجوز أن يجاوز ٤ أيام إعمالاً للمادة ١٦ من قانون المرافعات (يوم لكل ٥٠ كم، وما زاد على ٣٠ كم يعتبر ٥٠ كم، بحد أقصى ٤ أيام)")
                    }
                    return
                }
                days
            }
            DistanceType.BORDER_AREA -> 15
            DistanceType.ABROAD -> 60
            DistanceType.ABROAD_JUDICIAL_REDUCTION -> {
                val days = currentState.additionalDistanceDaysInput.toIntOrNull()
                if (days == null || days !in 1..59) {
                    _uiState.update {
                        it.copy(errorMessage = "ميعاد المسافة المنقوص بأمر قضائي لمن موطنه بالخارج يجب أن يكون بين ١ و ٥٩ يوماً وفقاً للمادة ١٧ فقرة ٢ مرافعات")
                    }
                    return
                }
                if (currentState.judicialOrderInput.isBlank()) {
                    _uiState.update {
                        it.copy(errorMessage = "يلزم إثبات بيانات ورقم الأمر القضائي الصادر بإنقاص ميعاد المسافة")
                    }
                    return
                }
                days
            }
            DistanceType.ABROAD_SERVED_IN_EGYPT -> 0
        }

        if (rule.distancePolicy.equals("NOT_APPLICABLE", ignoreCase = true) &&
            (distanceType != DistanceType.NONE && distanceType != DistanceType.ABROAD_SERVED_IN_EGYPT)
        ) {
            _uiState.update {
                it.copy(errorMessage = "القاعدة القانونية المختارة (${rule.actionName}) لا يقبل تطبيق مواعيد المسافة عليها وفقاً للقواعد الإجرائية المقررة")
            }
            return
        }

        viewModelScope.launch {
            // The user may have changed a field before this coroutine was scheduled.
            if (_uiState.value.inputRevision != currentState.inputRevision) return@launch
            _uiState.update { it.copy(isCalculating = true, errorMessage = null, result = null, isSavedSuccessfully = false) }
            try {
                val calculationResult = calculateDeadlineUseCase(
                    announcementDate = currentState.announcementDate,
                    rule = rule,
                    distanceType = distanceType,
                    additionalDistanceDays = distanceDays,
                    judicialOrderDetails = if (distanceType == DistanceType.ABROAD_JUDICIAL_REDUCTION) currentState.judicialOrderInput else null
                )
                _uiState.update {
                    if (it.inputRevision != currentState.inputRevision) it else it.copy(
                        isCalculating = false,
                        result = calculationResult,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    if (it.inputRevision != currentState.inputRevision) it else it.copy(
                        isCalculating = false,
                        result = null,
                        errorMessage = "حدث خطأ أثناء احتساب الميعاد: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun resetCalculation() {
        _uiState.update { it.invalidateCalculation() }
    }

    fun onProactiveReminderDaysChanged(days: Int?) {
        _uiState.update { it.copy(proactiveReminderDays = days, isSavedSuccessfully = false, successMessage = null) }
    }

    fun saveCurrentDeadline(proactiveDays: Int? = _uiState.value.proactiveReminderDays) {
        val currentState = _uiState.value
        if (currentState.isCalculating) return
        val result = currentState.result ?: return
        val rule = currentState.selectedRule ?: return

        viewModelScope.launch {
            try {
                val ruleSnapshotText = buildString {
                    append("معرف القاعدة: ").append(rule.id)
                    append(" | الاسم: ").append(rule.actionName)
                    append(" | حالة المراجعة: ").append(rule.reviewStatus)
                    append(" | آخر مراجعة: ").append(rule.lastReviewedAt)
                    append(" | المدة: ").append(rule.duration.toString().toArabicDigits()).append(" ").append(rule.unit.arabicDisplayName)
                    append(" | ").append(rule.lawArticle.toArabicDigits())
                    if (rule.triggerEventLabel.isNotBlank()) {
                        append(" | الواقعة: ").append(rule.triggerEventLabel)
                    }
                }

                val reminderDaysToSave = proactiveDays ?: currentState.proactiveReminderDays
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
                    createdAt = System.currentTimeMillis(),
                    proactiveReminderDays = reminderDaysToSave,
                    ruleSnapshot = ruleSnapshotText,
                    distanceType = currentState.distanceType.name
                )

                saveDeadlineUseCase(deadlineToSave)
                _uiState.update {
                    if (it.inputRevision != currentState.inputRevision) it else it.copy(
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
                return CalculatorViewModel(
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
