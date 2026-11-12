package com.ateflaw.legaldeadlines.data.review

import android.content.Context
import androidx.room.withTransaction
import com.ateflaw.legaldeadlines.data.database.AppDatabase
import com.ateflaw.legaldeadlines.data.seed.LegalRuleSeedData

/** Idempotent and revision-safe adoption of the user's signed-off checklist. */
object ReviewedWorkbookImporter {
    suspend fun apply(context: Context, database: AppDatabase) {
        val records = ReviewedWorkbook.load(context)
        val builtin = LegalRuleSeedData.getSeedRules().associateBy { it.id }
        database.withTransaction {
            val dao = database.legalRuleDao()
            records.forEach { item ->
                val seed = builtin[item.id] ?: error("Unknown original rule ${item.id}")
                val expiry = when (item.id) {
                    in 147L..167L -> null // reviewed against effective 2025 Labor Law
                    in 92L..127L -> "2027-09-30" // transition to new Criminal Procedure Law
                    else -> seed.effectiveTo
                }
                val applied = dao.adoptReviewedWorkbookRule(
                    id = item.id,
                    actionName = seed.actionName,
                    decision = item.decision,
                    method = item.method,
                    reviewerNote = item.reviewerNote,
                    source = item.sourceUrl,
                    effectiveFrom = item.effectiveFrom,
                    effectiveTo = expiry,
                    kind = item.normalizedKind,
                    duration = item.correctedDuration,
                    unit = if (item.method == "ساعات") "DAYS" else item.correctedUnit,
                    correctedActionName = item.correctedActionName,
                    correctedLawArticle = item.correctedLawArticle
                )
                if (applied == 0) {
                    // Existing user edits or missing rows: never clobber custom
                    // text, duration, review status or article references.
                    dao.attachWorkbookMemo(item.id, item.decision, item.method, item.reviewerNote)
                }
            }
        }
    }
}
