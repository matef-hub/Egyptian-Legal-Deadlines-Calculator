package com.ateflaw.legaldeadlines.domain.model

/**
 * Type of procedural distance deadline according to the Egyptian Procedures Law.
 * - [NONE]: No distance applicable (same jurisdictional circuit / domicile).
 * - [LOCAL]: Domestic distance within the Arab Republic of Egypt under Article 16 (1 day per 50 km, max 4 days).
 * - [ABROAD]: Domiciled abroad under Article 17 (statutory 60 days for Mediterranean/Red Sea or 90 days).
 */
enum class DistanceType(
    val arabicDisplayName: String,
    val statutoryMaxDays: Int,
    val statutoryBasis: String
) {
    NONE(
        arabicDisplayName = "بدون ميعاد مسافة",
        statutoryMaxDays = 0,
        statutoryBasis = ""
    ),
    LOCAL(
        arabicDisplayName = "مسافة محلية داخل الجمهورية (المادة 16 مرافعات)",
        statutoryMaxDays = 4,
        statutoryBasis = "المادة 16 من قانون المرافعات المدنية والتجارية رقم 13 لسنة 1968 (يوم لكل 50 كم بحد أقصى 4 أيام)"
    ),
    ABROAD(
        arabicDisplayName = "موطن بالخارج (المادة 17 مرافعات)",
        statutoryMaxDays = 90,
        statutoryBasis = "المادة 17 من قانون المرافعات المدنية والتجارية رقم 13 لسنة 1968 (ميعاد المسافة لمن موطنه بالخارج)"
    )
}
