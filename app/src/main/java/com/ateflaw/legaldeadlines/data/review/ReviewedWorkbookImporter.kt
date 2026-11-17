package com.ateflaw.legaldeadlines.data.review

import android.content.Context
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.data.entity.LegalRuleEntity
import androidx.room.withTransaction
import com.ateflaw.legaldeadlines.data.database.AppDatabase
import com.ateflaw.legaldeadlines.data.seed.LegalRuleSeedData

/** Idempotent and revision-safe adoption of the user's signed-off checklist. */
object ReviewedWorkbookImporter {
    // Early versions could store notes without recording an edit-history row.
    // Apply an edition only when the installed text matches a known bundled version.
    fun hasBundledEditionText(row: LegalRuleEntity, seed: LegalRule?, edition: LegalRule): Boolean {
        val review = ReviewedWorkbookRecord(row.id, row.workbookDecision, row.reviewMethod,
            row.officialSourceUrl, row.effectiveFrom, row.reviewerNote)
        return row.notes in listOfNotNull(seed?.notes, edition.notes, review.correctedNotes) &&
            row.summary in listOfNotNull(seed?.summary, edition.summary, review.correctedSummary) &&
            row.articleText in listOfNotNull(seed?.articleText, edition.articleText) &&
            row.actionName in listOfNotNull(seed?.actionName, edition.actionName, review.correctedActionName) &&
            row.lawArticle in listOfNotNull(seed?.lawArticle, edition.lawArticle, review.correctedLawArticle) &&
            row.duration in listOfNotNull(seed?.duration, edition.duration, review.correctedDuration) &&
            row.unit in listOfNotNull(seed?.unit?.name, edition.unit.name, review.correctedUnit)
    }

    fun isBundledLaborBackfill(row: LegalRuleEntity): Boolean {
        if (row.id !in setOf(159L, 160L, 161L) || row.revision != 3 || row.reviewVersion != 2) return false
        val review = ReviewedWorkbookRecord(row.id, row.workbookDecision, row.reviewMethod,
            row.officialSourceUrl, row.effectiveFrom, row.reviewerNote)
        return row.reviewStatus == row.workbookDecision && row.notes == review.correctedNotes &&
            row.summary == review.correctedSummary && row.lawArticle == review.correctedLawArticle &&
            (review.correctedUnit == null || row.unit == review.correctedUnit) &&
            (review.correctedDuration == null || row.duration == review.correctedDuration)
    }

    suspend fun apply(context: Context, database: AppDatabase) {
        val records = ReviewedWorkbook.load(context)
        val builtin = LegalRuleSeedData.getSeedRules().associateBy { it.id }
        database.withTransaction {
            val dao = database.legalRuleDao()
            records.forEach { item ->
                val seed = builtin[item.id] ?: error("Unknown original rule ${item.id}")
                val expiry = when (item.id) {
                    in 147L..167L -> "2025-08-31"
                    in 92L..127L -> "2027-09-30" // transition to new Criminal Procedure Law
                    else -> seed.effectiveTo
                }
                val legacyLabor = item.id in 147L..167L
                val applied = dao.adoptReviewedWorkbookRule(
                    id = item.id,
                    actionName = seed.actionName,
                    decision = if (legacyLabor) "NEEDS_REVIEW" else item.decision,
                    method = item.method,
                    reviewerNote = item.reviewerNote,
                    source = if (legacyLabor) "" else item.sourceUrl,
                    effectiveFrom = if (legacyLabor) "" else item.effectiveFrom,
                    effectiveTo = expiry,
                    kind = item.normalizedKind,
                    duration = if (legacyLabor) null else item.correctedDuration,
                    unit = if (item.method == "ساعات") "DAYS" else item.correctedUnit,
                    correctedActionName = if (legacyLabor) null else item.correctedActionName,
                    correctedLawArticle = if (legacyLabor) null else item.correctedLawArticle
                )
                if (applied == 0) {
                    // Existing user edits or missing rows: never clobber custom
                    // text, duration, review status or article references.
                    dao.attachWorkbookMemo(item.id, item.decision, item.method, item.reviewerNote)
                }
                // Runs for both a fresh import and already-installed version 1
                // imports. The DAO compares the original text and imported
                // values before changing anything, preserving user overrides.
                val correctedNotes = item.correctedNotes
                val correctedSummary = item.correctedSummary
                if (!legacyLabor && correctedNotes != null && correctedSummary != null) {
                    dao.backfillReviewedWorkbookText(
                        id = item.id,
                        decision = item.decision,
                        actionName = item.correctedActionName ?: seed.actionName,
                        lawArticle = item.correctedLawArticle ?: seed.lawArticle,
                        duration = item.correctedDuration ?: seed.duration,
                        unit = item.correctedUnit ?: seed.unit.name,
                        seedNotes = seed.notes,
                        seedSummary = seed.summary,
                        correctedNotes = correctedNotes,
                        correctedSummary = correctedSummary
                    )
                }
            }
        }
    }
}
