package com.ateflaw.legaldeadlines.domain.model

import java.time.LocalDate

/**
 * Type of milestone in the procedural calculation timeline.
 */
enum class MilestoneType {
    TRIGGER_EVENT,      // الواقعة المنشئة (إعلان / صدور حكم / حضور)
    COUNTING_START,     // بدء احتساب الميعاد (المادة 15 مرافعات)
    BASE_DEADLINE,      // نهاية المدة القانونية الأصلية
    DISTANCE_EXTENSION, // أيام مسافة الطريق (المادة 16 أو 17 مرافعات)
    HOLIDAY_EXTENSION,  // امتداد الميعاد لمصادفة عطلة رسمية أو جمعة (المادة 18 مرافعات)
    FINAL_DEADLINE      // الميعاد النهائي الملزم
}

/**
 * Data structure representing a structured step in the legal deadline calculation.
 */
data class CalculationMilestone(
    val type: MilestoneType,
    val date: LocalDate,
    val title: String,
    val description: String,
    val legalBasis: String? = null,
    val isExcludedDay: Boolean = false
)
