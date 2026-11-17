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
    val correctedActionName: String?
        get() = when (id) {
            132L -> "ضوابط عدم سماع دعوى إثبات النسب (المادة 15)"
            159L, 160L -> "مهلة الإخطار لإنهاء عقد العمل غير محدد المدة"
            else -> null
        }

    val correctedLawArticle: String?
        get() = when (id) {
            132L -> "المادة 15 من المرسوم بقانون رقم 25 لسنة 1929"
            159L, 160L -> "المادة 156 من قانون العمل رقم 14 لسنة 2025"
            161L -> "المادة 167 من قانون العمل رقم 14 لسنة 2025"
            else -> null
        }

    /**
     * Version 2 completes the same reviewed corrections in the text users read.
     * The original seed stays unchanged so installed user annotations can be
     * distinguished from bundled text before applying this backfill.
     */
    val correctedNotes: String?
        get() = when (id) {
            11L -> "يجب على المدعي تعجيل الدعوى وإعلان خصمه خلال خمسة عشر يوماً التالية لانتهاء مدة الوقف الجزائي وإلا حُكم باعتبارها كأن لم تكن."
            16L -> "في جميع الأحوال تنقضي الخصومة بمضي سنتين على آخر إجراء صحيح اتخذ فيها أياً كان سبب عدم السير."
            74L -> "تتقادم الضرائب والرسوم المستحقة للدولة والطعن فيها والحق في استرداد ما دفع بغير حق بانقضاء خمس سنوات."
            131L -> "يجوز لزوجة المحبوس بحكم نهائي بعقوبة 3 سنوات فأكثر طلب التطليق بعد مضي سنة كاملة من حبسه؛ الثلاث سنوات شرط للعقوبة وليست مدة الانتظار."
            132L -> "تتعلق هذه القاعدة بضوابط عدم سماع دعوى إثبات النسب وفق المادة 15؛ لا تقرر مدة تقادم عامة لدعوى نفي النسب، ولا تُحسب آلياً."
            159L, 160L -> "مهلة الإخطار الكتابي لإنهاء عقد العمل غير محدد المدة ثلاثة أشهر وفق المادة 156 من قانون العمل رقم 14 لسنة 2025؛ تراجع شروط بدء المهلة ووقفها والاستثناءات."
            161L -> "للعامل العدول عن الاستقالة كتابة خلال عشرة أيام من تاريخ إخطاره بقبولها وفق المادة 167 من قانون العمل رقم 14 لسنة 2025؛ تراجع إجراءات الاعتماد وشروط العدول."
            else -> null
        }

    val correctedSummary: String?
        get() = when (id) {
            11L -> "تعجيل الوقف الجزائي (15 يوماً)"
            16L -> "انقضاء الخصومة بمضي سنتين"
            74L -> "تقادم الضرائب والرسوم (5 سنوات)"
            131L -> "التطليق بعد سنة من الحبس مع تحقق شرط العقوبة"
            132L -> "ضوابط عدم سماع دعوى إثبات النسب"
            159L, 160L -> "مهلة إخطار إنهاء العقد (3 أشهر)"
            161L -> "العدول عن الاستقالة (10 أيام)"
            else -> null
        }

    val normalizedKind: String
        get() = when (method) {
            "عكسي قبل الجلسة" -> "BACKWARD_FROM_EVENT"
            "تقادم" -> "LIMITATION_OR_CONDITION"
            "حد سن" -> "AGE_THRESHOLD"
            "أخرى" -> "SPECIAL_UNSUPPORTED"
            "ساعات" -> "EXACT_HOURS"
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
