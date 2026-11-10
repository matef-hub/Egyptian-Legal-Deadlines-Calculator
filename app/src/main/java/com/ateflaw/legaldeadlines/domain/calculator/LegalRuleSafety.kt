package com.ateflaw.legaldeadlines.domain.calculator

import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import java.time.LocalDate
import java.net.URI
import java.util.Locale

/**
 * Fail-closed application gate. A legal source and a human review are prerequisites
 * for a result to be used by the Android app. A passing unit test is not legal review.
 *
 * The legacy seed is deliberately NOT whitelisted by its numeric duration: many
 * entries are ages, limitation periods, hours or deadlines measured BEFORE a hearing.
 * These must have their own verified algorithms and triggers.
 */
object LegalRuleSafety {
    enum class CalculationKind {
        FORWARD, BACKWARD_FROM_EVENT, EXACT_HOURS, AGE_THRESHOLD,
        LIMITATION_OR_CONDITION, SPECIAL_UNSUPPORTED
    }

    private val backwards = setOf(
        4L, 5L, 6L, 39L, 40L, 51L, 53L, 54L,
        115L, 116L, 117L, 118L, 119L, 126L, 137L, 139L,
        150L, 154L, 155L, 159L, 160L, 162L, 164L, 166L
    )
    private val exactHours = setOf(7L, 96L, 103L, 104L)
    private val ages = setOf(134L, 136L)
    private val limitations = setOf(
        13L, 15L, 16L, 17L, 48L, 59L, 60L, 61L, 62L, 63L,
        64L, 65L, 66L, 67L, 71L, 72L, 73L, 74L, 75L, 76L,
        77L, 78L, 79L, 80L, 81L, 85L, 86L, 87L, 88L, 89L,
        92L, 93L, 94L, 95L, 100L, 102L, 131L, 132L,
        133L, 135L, 138L
    )
    private val special = setOf(9L, 10L, 12L, 14L, 18L, 20L, 41L, 42L, 43L, 90L, 91L)

    private fun inherentKind(rule: LegalRule): CalculationKind = when {
        rule.id in exactHours || rule.unit == DurationUnit.HOURS ->
            CalculationKind.EXACT_HOURS
        rule.id in ages -> CalculationKind.AGE_THRESHOLD
        rule.id in backwards -> CalculationKind.BACKWARD_FROM_EVENT
        rule.id in limitations || rule.duration == 0L ->
            CalculationKind.LIMITATION_OR_CONDITION
        rule.id in special -> CalculationKind.SPECIAL_UNSUPPORTED
        else -> CalculationKind.FORWARD
    }

    fun kind(rule: LegalRule): CalculationKind {
        val inherited = inherentKind(rule)
        val declared = CalculationKind.entries.firstOrNull { it.name == rule.calculationKind }
        // An explicit database value cannot downgrade a known unsafe old seed
        // into a generic forward-calculation rule.
        return if (inherited != CalculationKind.FORWARD) inherited else declared ?: inherited
    }

    fun validate(rule: LegalRule, eventDate: LocalDate) {
        // Current law 14/2025 superseded law 12/2003 on 2025-09-01.
        // Do not quietly produce a 2026 deadline from the legacy labor seed.
        require(rule.id !in 147L..167L || eventDate.isBefore(LocalDate.of(2025, 9, 1))) {
            "قواعد قانون العمل القديمة لا تصلح للوقائع من 1 سبتمبر 2025. يلزم نص القانون 14 لسنة 2025 ومراجعته قبل الحساب."
        }
        // Law 174/2025, as amended by law 160/2026, starts 2027-10-01.
        require(rule.id !in 92L..128L || eventDate.isBefore(LocalDate.of(2027, 10, 1))) {
            "قواعد الإجراءات الجنائية القديمة لا تطبق آلياً من 1 أكتوبر 2027. يلزم اعتماد القواعد الجديدة والأحكام الانتقالية."
        }
        val type = kind(rule)
        require(type == CalculationKind.FORWARD) {
            when (type) {
                CalculationKind.BACKWARD_FROM_EVENT -> "هذا الميعاد يُحسب قبل واقعة أو جلسة؛ ولا يجوز استخدام الحساب الأمامي له."
                CalculationKind.EXACT_HOURS -> "هذا الإجراء يتطلب توقيتاً دقيقاً بالساعة والدقيقة وليس تاريخ يوم فقط."
                CalculationKind.AGE_THRESHOLD -> "هذا حد سن قانوني وليس ميعاد طعن ولا تطبق عليه قواعد ترحيل الإجازات."
                CalculationKind.LIMITATION_OR_CONDITION -> "هذه مدة تقادم أو شرط قانوني يتطلب فحص الوقف والانقطاع وشروط بدء السريان."
                CalculationKind.SPECIAL_UNSUPPORTED -> "هذا الإجراء يتطلب سياسة خاصة لحسابه ولم تتم مراجعتها بعد."
                else -> "هذه القاعدة غير مدعومة."
            }
        }
        require(rule.reviewStatus == "VERIFIED_ACCURATE") {
            "لا يمكن حساب ميعاد ملزم من قاعدة لم تعتمد قانونياً. راجع السند الرسمي وحالة اعتماد القاعدة."
        }
        val parsedSource = try { URI(rule.officialSourceUrl) } catch (_: Exception) { null }
        val sourceHost = parsedSource?.host?.lowercase(Locale.ROOT).orEmpty()
        val officialHost = sourceHost.endsWith(".gov.eg") ||
            sourceHost == "alamiria.com" ||
            sourceHost == "www.alamiria.com"
        require(parsedSource?.scheme == "https" && officialHost) {
            "لا يمكن اعتماد قاعدة دون مصدر تشريعي رسمي موثق، مثل المطابع الأميرية أو موقع حكومي مصري."
        }
        require(rule.effectiveFrom.isNotBlank()) {
            "لم يُحدد تاريخ بدء سريان هذه النسخة من القاعدة القانونية."
        }
        require(rule.extensionPolicy == "ARTICLE_18_COURTS") {
            "سياسة امتداد الميعاد غير مدعومة لهذا الإجراء."
        }
    }
}
