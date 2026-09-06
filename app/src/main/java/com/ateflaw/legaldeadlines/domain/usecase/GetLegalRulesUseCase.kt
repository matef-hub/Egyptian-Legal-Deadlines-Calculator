package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.repository.LegalRuleRepository
import kotlinx.coroutines.flow.Flow

class GetLegalRulesUseCase(
    private val repository: LegalRuleRepository
) {
    operator fun invoke(): Flow<List<LegalRule>> = repository.getAllRules()
}
