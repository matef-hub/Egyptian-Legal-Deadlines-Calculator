package com.ateflaw.legaldeadlines.domain.model

/**
 * Domain model representing an Egyptian procedural legal rule with full auditability.
 */
data class LegalRule(
    val id: Long = 0,
    val actionName: String,
    val duration: Long,
    val unit: DurationUnit,
    val distanceDays: Int = 0,
    val startRule: StartRule = StartRule.NEXT_DAY,
    val lawArticle: String = "",
    val notes: String = "",
    val summary: String = "",
    val triggerEventType: String = "ANNOUNCEMENT", // نوع الواقعة المنشئة
    val triggerEventLabel: String = "تاريخ الإعلان أو صدور الحكم", // تسمية حقل الإدخال الصريح
    val courtType: String = "مدني وتجاري", // نوع المحكمة
    val category: String = "طرق الطعن", // فئة الإجراء
    val distancePolicy: String = "APPLICABLE", // سياسة ميعاد المسافة
    val extensionPolicy: String = "ARTICLE_18_COURTS", // سياسة امتداد العطلات (السبت يوم عمل للمحاكم)
    val lawName: String = "قانون المرافعات المدنية والتجارية", // اسم القانون
    val article: String = "", // رقم المادة
    val officialSourceUrl: String = "", // مصدر تشريعي موثوق
    val effectiveFrom: String = "1968-11-01", // تاريخ بدء النفاذ
    val effectiveTo: String? = null, // تاريخ انتهاء النفاذ إن وجد
    val lastReviewedAt: String = "2026-10-13", // تاريخ آخر مراجعة قانونية
    val reviewStatus: String = "VERIFIED_ACCURATE" // حالة المراجعة القانونية
)
