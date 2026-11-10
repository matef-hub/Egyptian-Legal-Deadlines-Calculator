package com.ateflaw.legaldeadlines.data.repository

import androidx.room.withTransaction
import com.ateflaw.legaldeadlines.data.database.AppDatabase
import com.ateflaw.legaldeadlines.data.dao.LegalRuleDao
import com.ateflaw.legaldeadlines.data.entity.RuleEditHistoryEntity
import com.ateflaw.legaldeadlines.domain.model.LegalRuleDraft
import com.ateflaw.legaldeadlines.domain.model.StartRule
import com.ateflaw.legaldeadlines.data.entity.LegalRuleEntity
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.repository.LegalRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LegalRuleRepositoryImpl(
    private val database: AppDatabase
) : LegalRuleRepository {
    private val legalRuleDao: LegalRuleDao get() = database.legalRuleDao()

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

    override suspend fun addDraft(draft: LegalRuleDraft): Long {
        val input = draft.validate(isNew = true)
        // New rules remain NON-CALCULABLE until their exact legal policy has been
        // verified separately. The editor cannot approve a rule.
        val record = LegalRule(
            actionName = input.actionName,
            lawName = input.lawName,
            lawArticle = input.lawArticle,
            articleText = input.articleText,
            duration = input.duration,
            unit = input.unit,
            notes = input.notes,
            startRule = StartRule.NEXT_DAY,
            triggerEventLabel = "تاريخ الواقعة المنشئة للميعاد",
            distancePolicy = "NOT_APPLICABLE",
            extensionPolicy = "UNREVIEWED",
            calculationKind = "SPECIAL_UNSUPPORTED",
            reviewStatus = "NEEDS_REVIEW",
            officialSourceUrl = "",
            effectiveFrom = "",
            lastReviewedAt = "",
            revision = 1
        )
        return legalRuleDao.insertDraft(LegalRuleEntity.fromDomain(record))
    }

    override suspend fun updateDraft(id: Long, expectedRevision: Int, draft: LegalRuleDraft) {
        val input = draft.validate(isNew = false)
        database.withTransaction {
            val previous = legalRuleDao.getById(id)
                ?: throw IllegalArgumentException("القاعدة القانونية لم تعد موجودة.")
            require(previous.revision == expectedRevision) {
                "تم تعديل هذه القاعدة من شاشة أخرى؛ أعد فتحها قبل الحفظ."
            }
            val affected = legalRuleDao.updateDraftIfCurrent(
                id = id,
                expectedRevision = expectedRevision,
                actionName = input.actionName,
                lawName = input.lawName,
                lawArticle = input.lawArticle,
                articleText = input.articleText,
                duration = input.duration,
                unit = input.unit.name,
                notes = input.notes
            )
            check(affected == 1) { "تغيّرت القاعدة أثناء الحفظ. أعد تحميلها." }
            legalRuleDao.insertEditHistory(
                RuleEditHistoryEntity(
                    ruleId = id,
                    previousRevision = previous.revision,
                    actionName = previous.actionName,
                    lawName = previous.lawName,
                    lawArticle = previous.lawArticle,
                    articleText = previous.articleText,
                    duration = previous.duration,
                    unit = previous.unit,
                    notes = previous.notes,
                    reviewStatus = previous.reviewStatus,
                    officialSourceUrl = previous.officialSourceUrl,
                    editedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun clearRules() {
        legalRuleDao.clear()
    }
}
