package com.ateflaw.legaldeadlines.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ateflaw.legaldeadlines.data.entity.LegalRuleEntity
import com.ateflaw.legaldeadlines.data.entity.RuleEditHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LegalRuleDao {

    @Query("SELECT * FROM legal_rules ORDER BY action_name ASC")
    fun getAll(): Flow<List<LegalRuleEntity>>

    @Query("SELECT COUNT(*) FROM legal_rules")
    suspend fun count(): Int

    @Query("SELECT * FROM legal_rules WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): LegalRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<LegalRuleEntity>)

    /**
     * Metadata-only adoption of historical built-in rows. NEVER overwrite user
     * annotations, duration or other original record contents during upgrade.
     * Match action_name and unversioned revision to avoid touching custom IDs.
     */
    @Query("""
        UPDATE legal_rules
        SET review_status = 'NEEDS_REVIEW',
            effective_from = '',
            effective_to = :expiresAt,
            revision = :revision,
            calculation_kind = :kind
        WHERE id = :id AND action_name = :expectedName AND revision = 0
    """)
    suspend fun adoptSeedMetadata(
        id: Long,
        expectedName: String,
        expiresAt: String?,
        revision: Int,
        kind: String
    ): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDraft(rule: LegalRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEditHistory(revision: RuleEditHistoryEntity): Long

    /** Compare-and-swap: simultaneous edits cannot silently overwrite each other. */
    @Query("""
        UPDATE legal_rules SET
            action_name = :actionName,
            law_name = :lawName,
            law_article = :lawArticle,
            article_text = :articleText,
            duration = :duration,
            unit = :unit,
            notes = :notes,
            revision = revision + 1,
            review_status = 'VERIFIED_ACCURATE',
            workbook_decision = 'USER_APPROVED',
            review_version = CASE WHEN review_version < 1 THEN 1 ELSE review_version END,
            official_source_url = :officialSourceUrl,
            last_reviewed_at = :approvedAt
        WHERE id = :id AND revision = :expectedRevision
    """)
    suspend fun updateDraftIfCurrent(
        id: Long,
        expectedRevision: Int,
        actionName: String,
        lawName: String,
        lawArticle: String,
        articleText: String,
        duration: Long,
        unit: String,
        notes: String,
        officialSourceUrl: String,
        approvedAt: String
    ): Int

    /** Import only untouched built-in rows; never overwrite user edits. */
    @Query("""
        UPDATE legal_rules SET
            workbook_decision = :decision,
            review_method = :method,
            reviewer_note = :reviewerNote,
            review_version = 1,
            review_status = :decision,
            official_source_url = :source,
            effective_from = :effectiveFrom,
            effective_to = :effectiveTo,
            calculation_kind = :kind,
            duration = COALESCE(:duration, duration),
            unit = COALESCE(:unit, unit),
            action_name = COALESCE(:correctedActionName, action_name),
            law_article = COALESCE(:correctedLawArticle, law_article),
            last_reviewed_at = '2026-11-11',
            revision = 2
        WHERE id = :id AND revision <= 1 AND review_version = 0 AND action_name = :actionName
    """)
    suspend fun adoptReviewedWorkbookRule(
        id: Long,
        actionName: String,
        decision: String,
        method: String,
        reviewerNote: String,
        source: String,
        effectiveFrom: String,
        effectiveTo: String?,
        kind: String,
        duration: Long?,
        unit: String?,
        correctedActionName: String?,
        correctedLawArticle: String?
    ): Int

    /** Preserve overrides while retaining reviewer observations separately. */
    @Query("""
        UPDATE legal_rules SET
            workbook_decision = :decision,
            review_method = :method,
            reviewer_note = :reviewerNote,
            review_version = 1
        WHERE id = :id AND review_version = 0
    """)
    suspend fun attachWorkbookMemo(id: Long, decision: String, method: String, reviewerNote: String): Int

    @Query("DELETE FROM legal_rules")
    suspend fun clear()
}
