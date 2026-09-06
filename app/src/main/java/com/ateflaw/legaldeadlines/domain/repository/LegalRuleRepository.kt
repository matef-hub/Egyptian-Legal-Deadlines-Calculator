package com.ateflaw.legaldeadlines.domain.repository

import com.ateflaw.legaldeadlines.domain.model.LegalRule
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository abstraction for Egyptian legal rules.
 */
interface LegalRuleRepository {
    fun getAllRules(): Flow<List<LegalRule>>
    suspend fun getRuleById(id: Long): LegalRule?
    suspend fun getRulesCount(): Int
    suspend fun insertRules(rules: List<LegalRule>)
    suspend fun clearRules()
}
