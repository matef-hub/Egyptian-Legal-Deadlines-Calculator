package com.ateflaw.legaldeadlines.data.seed

import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule

internal object ReleaseRuleCatalog {
    const val SETTLEMENT_CURRENT_ID = -156L
    const val SETTLEMENT_PRE_2008_ID = -180156L
    const val REGISTRATION_CURRENT_ID = -147L
    const val REGISTRATION_CERTIFICATE_ID = -350147L
    const val WORK_NOTICE_CURRENT_ID = -159L
    const val RESIGNATION_CURRENT_ID = -161L
    const val PROSECUTOR_APPEAL_ID = -125L

    private const val LAW_2003 = "https://www.labour.gov.eg/media/225nyfw5/%D9%82%D8%A7%D9%86%D9%88%D9%86-%D8%A7%D9%84%D8%B9%D9%85%D9%84-12-%D9%84%D8%B3%D9%86%D9%87-2003.pdf"
    private const val LAW_2008 = "https://www.labour.gov.eg/media/1d3psheo/%D8%AA%D8%B9%D8%AF%D9%8A%D9%84-%D9%82%D8%A7%D9%86%D9%88%D9%86-%D8%A7%D9%84%D8%B9%D9%85%D9%84-%D8%A8%D8%A7%D9%84%D9%82%D8%A7%D9%86%D9%88%D9%86-%D8%B1%D9%82%D9%85-180-%D9%84%D8%B3%D9%86%D9%87-2008.pdf"
    private const val LAW_2025 = "https://portal.eta.gov.eg/sites/default/files/2026-03/law.no_.14.of_.2025.pdf"
    private const val LAW_2024 = "https://mediadr.sis.gov.eg/handle/123456789/51750"
    private const val LABOR_OLD_END = "2025-08-31"
    private const val LABOR_CURRENT_START = "2025-09-01"
    private const val LABOR_OLD_START = "2003-07-07"
    private const val CPC_OLD_END = "2027-09-30"
    private const val LABOR_OLD_NAME = "قانون العمل رقم ١٢ لسنة ٢٠٠٣ (سابق)"
    private const val LABOR_CURRENT_NAME = "قانون العمل رقم ١٤ لسنة ٢٠٢٥"
    private const val DISPUTE_DATE = "تاريخ نشوء النزاع العمالي الفردي"
    private const val RESIGNATION_DATE = "تاريخ إخطار العامل بقبول الاستقالة"
    private const val NO_EXTENSION = "NO_AUTOMATIC_EXTENSION"
    private const val CRIMINAL_APPEAL_ARTICLE = "المادة ٤١٩ مكررًا (٤) من قانون الإجراءات الجنائية، المضافة بالقانون ١ لسنة ٢٠٢٤"

    // Negative built-in IDs avoid the positive IDs Room already assigned to user rules.
    fun editions(seedRules: List<LegalRule>): List<LegalRule> {
        val seeds = seedRules.associateBy(LegalRule::id)
        val historical = seedRules.filter { it.id in 147L..167L }.map { old ->
            val base = old.copy(
                lawName = LABOR_OLD_NAME,
                effectiveFrom = LABOR_OLD_START,
                effectiveTo = LABOR_OLD_END,
                officialSourceUrl = LAW_2003,
                reviewStatus = "NEEDS_REVIEW",
                distancePolicy = "NOT_APPLICABLE",
                extensionPolicy = NO_EXTENSION,
                category = "العمل",
                courtType = "الجهة العمالية المختصة"
            )
            when (old.id) {
                147L -> base.copy(triggerEventLabel = "تاريخ إلحاق العامل بالعمل")
                156L -> base.copy(
                    actionName = "طلب التسوية الودية للنزاع العمالي الفردي (قانون ١٢/٢٠٠٣ بعد تعديل ٢٠٠٨)",
                    duration = 10,
                    lawArticle = "المادة ٧٠ من قانون العمل رقم ١٢ لسنة ٢٠٠٣ المعدلة بالقانون ١٨٠ لسنة ٢٠٠٨",
                    officialSourceUrl = LAW_2008,
                    effectiveFrom = "2008-06-23",
                    triggerEventLabel = DISPUTE_DATE,
                    reviewStatus = "VERIFIED_ACCURATE",
                    notes = "يُحسب طلب التسوية الودية خلال عشرة أيام من نشوء النزاع في هذه الفترة. للإحالة واللجوء إلى المحكمة شروط ومدد مستقلة."
                )
                161L -> base.copy(
                    actionName = "عدول العامل كتابة عن استقالته (قانون ١٢/٢٠٠٣)",
                    lawArticle = "المادة ١١٩ من قانون العمل رقم ١٢ لسنة ٢٠٠٣",
                    triggerEventLabel = RESIGNATION_DATE,
                    notes = "مدة العدول الكتابي أسبوع من إخطار صاحب العمل للعامل بقبول استقالته.",
                    reviewStatus = "VERIFIED_ACCURATE"
                )
                else -> base
            }
        }
        val beforeAmendment = historical.first { it.id == 156L }.copy(
            id = SETTLEMENT_PRE_2008_ID,
            actionName = "طلب التسوية الودية للنزاع العمالي الفردي (قانون ١٢/٢٠٠٣ قبل تعديل ٢٠٠٨)",
            lawArticle = "المادة ٧٠ من قانون العمل رقم ١٢ لسنة ٢٠٠٣ قبل تعديلها بالقانون ١٨٠ لسنة ٢٠٠٨",
            duration = 7,
            effectiveFrom = LABOR_OLD_START,
            effectiveTo = "2008-06-22",
            officialSourceUrl = LAW_2003,
            notes = "مدة طلب التسوية الودية في النص الأصلي سبعة أيام من نشوء النزاع."
        )
        val laborCurrent = listOf(
            currentLabor(SETTLEMENT_CURRENT_ID, "طلب التسوية الودية للنزاع العمالي الفردي (قانون ١٤/٢٠٢٥)",
                10, 149, DISPUTE_DATE, "لكل طرف طلب التسوية خلال عشرة أيام من نشوء النزاع. المادة ١٥٠ تنظم الإحالة عند تعذر التسوية."),
            currentLabor(REGISTRATION_CURRENT_ID, "قيد العامل غير الحاصل على شهادة قيد (قانون ١٤/٢٠٢٥)",
                30, 33, "تاريخ تسلّم العامل العمل", "عند تشغيل عامل بغير شهادة قيد، يقيد صاحب العمل اسمه بالجهة الإدارية المختصة خلال ثلاثين يومًا."),
            currentLabor(REGISTRATION_CERTIFICATE_ID, "إعادة شهادة قيد العامل إلى الجهة الإدارية (قانون ١٤/٢٠٢٥)",
                45, 35, "تاريخ تسلّم العامل العمل", "يعيد صاحب العمل شهادة القيد بعد استيفاء بياناتها في الجهة الإدارية المختصة."),
            currentLabor(RESIGNATION_CURRENT_ID, "عدول العامل كتابة عن استقالته (قانون ١٤/٢٠٢٥)",
                10, 167, RESIGNATION_DATE, "العدول كتابة خلال عشرة أيام من الإخطار بالقبول مع اعتماد الجهة الإدارية المختصة."),
            currentLabor(WORK_NOTICE_CURRENT_ID, "إخطار الطرف الآخر قبل إنهاء عقد العمل غير محدد المدة (قانون ١٤/٢٠٢٥)",
                3, 156, "تاريخ الإنهاء المقصود", "يُخطر الطرف الآخر كتابة قبل الإنهاء بثلاثة أشهر. تعرض القاعدة في الدليل بصفة ميعاد سابق على الإنهاء.")
                .copy(unit = DurationUnit.MONTHS, calculationKind = "BACKWARD_FROM_EVENT")
        )
        val criminalAppeal = seeds.getValue(125L).copy(
            actionName = "استئناف أحكام الجنايات (ميعاد الأربعين يومًا)",
            lawArticle = CRIMINAL_APPEAL_ARTICLE,
            officialSourceUrl = LAW_2024,
            effectiveFrom = "2024-01-17",
            effectiveTo = CPC_OLD_END,
            triggerEventLabel = "تاريخ صدور الحكم",
            distancePolicy = "NOT_APPLICABLE",
            notes = "مدة الاستئناف العامة أربعون يومًا من صدور الحكم. للنائب العام قاعدة مستقلة مدتها ستون يومًا.",
            reviewStatus = "VERIFIED_ACCURATE",
            courtType = "جنايات",
            category = "استئناف"
        )
        val prosecutor = criminalAppeal.copy(
            id = PROSECUTOR_APPEAL_ID,
            actionName = "استئناف النائب العام لأحكام الجنايات (ستون يومًا)",
            summary = "استئناف النائب العام لأحكام الجنايات",
            duration = 60,
            notes = "ميعاد النائب العام ستون يومًا من صدور الحكم وفق المادة ٤١٩ مكررًا (٤)."
        )
        return historical + beforeAmendment + laborCurrent + listOf(criminalAppeal, prosecutor)
    }

    private fun currentLabor(
        id: Long,
        name: String,
        days: Long,
        article: Int,
        trigger: String,
        notes: String
    ) = LegalRule(
        id = id,
        actionName = name,
        duration = days,
        unit = DurationUnit.DAYS,
        lawArticle = "المادة $article من قانون العمل رقم ١٤ لسنة ٢٠٢٥",
        lawName = LABOR_CURRENT_NAME,
        notes = notes,
        triggerEventLabel = trigger,
        reviewStatus = "VERIFIED_ACCURATE",
        effectiveFrom = LABOR_CURRENT_START,
        officialSourceUrl = LAW_2025,
        distancePolicy = "NOT_APPLICABLE",
        extensionPolicy = NO_EXTENSION,
        category = "العمل",
        courtType = "الجهة العمالية المختصة",
        calculationKind = "FORWARD"
    )
}
