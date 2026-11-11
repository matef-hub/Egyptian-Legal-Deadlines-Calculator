package com.ateflaw.legaldeadlines.data.review

import android.content.Context
import com.ateflaw.legaldeadlines.R
import org.json.JSONArray
import java.io.ByteArrayInputStream
import java.util.zip.InflaterInputStream

/**
 * Exact user-supplied review sheet metadata (169 rules), 11 November 2026.
 * The Cassation worksheet is deliberately NOT imported.
 *
 * Asset fields: rule id, reviewer decision, counting method, cited reference
 * URL, effective date, reviewer's notes (column X). The original source
 * workbook stays separate, and its decisions are not independently certified.
 */
data class ReviewedWorkbookRecord(
    val id: Long,
    val decision: String,
    val method: String,
    val sourceUrl: String,
    val effectiveFrom: String,
    val reviewerNote: String
) {
    val correctedDuration: Long?
        get() = when (id) {
            11L -> 15L // Article 99 of CPC
            16L -> 2L  // Article 140 of CPC
            74L -> 5L  // amended article 377 Civil Code
            131L -> 1L // one year of actual imprisonment
            132L -> 0L // WRONG legal basis: no general one-year denial-of-paternity claim
            159L -> 3L // Labor Law 14/2025, article 156
            161L -> 10L // Labor Law 14/2025, article 167
            else -> null
        }
    val correctedUnit: String?
        get() = when (id) {
            11L, 132L, 161L -> "DAYS"
            16L, 74L, 131L -> "YEARS"
            159L -> "MONTHS"
            else -> null
        }
    val normalizedKind: String
        get() = when (method) {
            "عكسي قبل الجلسة" -> "BACKWARD_FROM_EVENT"
            "تقادم" -> "LIMITATION_OR_CONDITION"
            "حد سن" -> "AGE_THRESHOLD"
            "أخرى" -> "SPECIAL_UNSUPPORTED"
            "ساعات" -> "FORWARD" // all five are recorded as 24h=1 day in the review
            else -> if (id == 132L || id == 17L) "SPECIAL_UNSUPPORTED" else "FORWARD"
        }
}

object ReviewedWorkbook {
    fun parse(json: String): List<ReviewedWorkbookRecord> {
        val matrix = JSONArray(json)
        val result = (0 until matrix.length()).map { i ->
            val a = matrix.getJSONArray(i)
            require(a.length() == 6) { "Workbook columns do not match." }
            ReviewedWorkbookRecord(
                id = a.getLong(0),
                decision = a.getString(1),
                method = a.getString(2),
                sourceUrl = a.getString(3),
                effectiveFrom = a.getString(4),
                reviewerNote = a.getString(5)
            )
        }
        require(result.size == 169 && result.map { it.id }.toSet() == (1L..169L).toSet()) {
            "Expected 169 unique rule IDs in the reviewed workbook."
        }
        require(result.count { it.decision == "VERIFIED_ACCURATE" } == 161)
        require(result.count { it.decision == "NEEDS_CORRECTION" } == 7)
        require(result.count { it.decision == "KEEP_BLOCKED" } == 1)
        require(result.all { it.reviewerNote.isNotBlank() })
        return result
    }

    fun load(context: Context): List<ReviewedWorkbookRecord> {
        val original = context.resources.openRawResource(R.raw.legal_review).use { it.readBytes() }
        val json = InflaterInputStream(ByteArrayInputStream(original))
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        return parse(json)
    }
}
