package com.ateflaw.legaldeadlines.data.repository

import com.ateflaw.legaldeadlines.data.dao.LegalRuleDao
import com.ateflaw.legaldeadlines.data.entity.LegalRuleEntity
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.repository.LegalRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LegalRuleRepositoryImpl(
    private val legalRuleDao: LegalRuleDao
) : LegalRuleRepository {

    override fun getAllRules(): Flow<List<LegalRule>> {
        return legalRuleDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getRuleById(id: Long): LegalRule? {
        return legalRuleDao.getById(id)?.toDomain()
    }

    override suspend fun getRulesCount(): Int {
        return legalRuleDao.count()
    }

    override suspend fun insertRules(rules: List<LegalRule>) {
        legalRuleDao.insertAll(rules.map { LegalRuleEntity.fromDomain(it) })
    }

    override suspend fun clearRules() {
        legalRuleDao.clear()
    }
}
