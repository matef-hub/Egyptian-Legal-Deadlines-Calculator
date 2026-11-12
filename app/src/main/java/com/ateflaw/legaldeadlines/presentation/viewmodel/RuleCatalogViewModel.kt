package com.ateflaw.legaldeadlines.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.LegalRuleDraft
import com.ateflaw.legaldeadlines.domain.repository.LegalRuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RuleCatalogUiState(
    val rules: List<LegalRule> = emptyList(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val notice: String? = null,
    val savedToken: Long = 0
)

/** Saves the user's final review decision atomically and archives the prior revision. */
class RuleCatalogViewModel(
    private val repository: LegalRuleRepository
) : ViewModel() {
    private val state = MutableStateFlow(RuleCatalogUiState())
    val uiState: StateFlow<RuleCatalogUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.getAllRules().collect { rules ->
                    state.update { it.copy(rules = rules, loading = false) }
                }
            } catch (error: Exception) {
                state.update { it.copy(
                    loading = false,
                    error = error.localizedMessage ?: "تعذر قراءة دليل المواعيد."
                ) }
            }
        }
    }

    fun dismissMessage() {
        state.update { it.copy(error = null, notice = null) }
    }

    fun save(rule: LegalRule?, draft: LegalRuleDraft) {
        if (state.value.saving) return
        state.update { it.copy(saving = true, error = null, notice = null) }
        viewModelScope.launch {
            try {
                if (rule == null) {
                    repository.addDraft(draft)
                } else {
                    repository.updateDraft(rule.id, rule.revision, draft)
                }
                state.update { it.copy(
                    saving = false,
                    savedToken = it.savedToken + 1,
                    notice = "تم حفظ القاعدة واعتماد تعديلك نهائيًا، مع الاحتفاظ بسجل النسخة السابقة."
                ) }
            } catch (error: Exception) {
                state.update { it.copy(
                    saving = false,
                    error = error.localizedMessage ?: "تعذر حفظ التعديل."
                ) }
            }
        }
    }

    companion object {
        fun factory(repository: LegalRuleRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    RuleCatalogViewModel(repository) as T
            }
    }
}
