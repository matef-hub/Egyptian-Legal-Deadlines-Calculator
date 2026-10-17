package com.ateflaw.legaldeadlines.domain.model

/**
 * Procedural distance deadline types according to Articles 16, 17, and 18
 * of the Egyptian Civil and Commercial Procedures Law No. 13 of 1968.
 *
 * - [NONE]: No distance applicable (same jurisdictional circuit / domicile).
 * - [LOCAL]: Domestic distance within the Arab Republic of Egypt under Article 16
 *            (1 day per 50 km; fraction exceeding 30 km counts as 50 km; capped at 4 days).
 * - [BORDER_AREA]: Domiciled in remote border areas under Article 16 Para 2 (statutory 15 days).
 * - [ABROAD]: Domiciled abroad under Article 17 (statutory 60 days).
 * - [ABROAD_JUDICIAL_REDUCTION]: Domiciled abroad with reduction by judicial order
 *                                under Article 17 Para 2 (1 to 59 days).
 * - [ABROAD_SERVED_IN_EGYPT]: Domiciled abroad but served in person inside Egypt
 *                             under Article 17 Para 3 (0 distance days).
 */
enum class DistanceType(
    val arabicDisplayName: String,
    val statutoryDays: Int?,
    val statutoryMaxDays: Int,
    val statutoryBasis: String
) {
    NONE(
        arabicDisplayName = "بدون ميعاد مسافة",
        statutoryDays = 0,
        statutoryMaxDays = 0,
        statutoryBasis = ""
    ),
    LOCAL(
        arabicDisplayName = "مسافة محلية داخل الجمهورية (المادة 16 مرافعات)",
        statutoryDays = null,
        statutoryMaxDays = 4,
        statutoryBasis = "المادة 16 من قانون المرافعات المدنية والتجارية رقم 13 لسنة 1968 (يوم لكل 50 كم، وما زاد على 30 كم يعتبر 50 كم، بحد أقصى 4 أيام)"
    ),
    BORDER_AREA(
        arabicDisplayName = "المناطق الحدودية النائية (المادة 16 فقرة 2 مرافعات)",
        statutoryDays = 15,
        statutoryMaxDays = 15,
        statutoryBasis = "المادة 16 فقرة 2 من قانون المرافعات المدنية والتجارية رقم 13 لسنة 1968 (ميعاد المسافة 15 يوماً للمناطق الحدودية والنائية الصادر بتحديدها قرار وزير العدل)"
    ),
    ABROAD(
        arabicDisplayName = "موطن بالخارج (المادة 17 مرافعات)",
        statutoryDays = 60,
        statutoryMaxDays = 60,
        statutoryBasis = "المادة 17 من قانون المرافعات المدنية والتجارية رقم 13 لسنة 1968 (ميعاد المسافة لمن يكون موطنه في الخارج ستون يوماً)"
    ),
    ABROAD_JUDICIAL_REDUCTION(
        arabicDisplayName = "موطن بالخارج - إنقاص الميعاد بأمر قضائي (المادة 17 فقرة 2 مرافعات)",
        statutoryDays = null,
        statutoryMaxDays = 59,
        statutoryBasis = "المادة 17 فقرة 2 من قانون المرافعات (جواز إنقاص ميعاد الستين يوماً بأمر على عريضة من قاضي الأمور الوقتية)"
    ),
    ABROAD_SERVED_IN_EGYPT(
        arabicDisplayName = "موطن بالخارج أُعلن لشخصه بمصر (المادة 17 فقرة 3 مرافعات)",
        statutoryDays = 0,
        statutoryMaxDays = 0,
        statutoryBasis = "المادة 17 فقرة 3 من قانون المرافعات (لا يسري ميعاد مسافة الخارج على من أعلن لشخصه أثناء وجوده في الجمهورية)"
    );

    companion object {
        /**
         * Calculates domestic distance days under Article 16 from distance in kilometers:
         * - 1 day per 50 km.
         * - Fraction exceeding 30 km adds 1 day.
         * - Maximum 4 days.
         */
        fun calculateLocalDistanceDays(distanceKm: Double): Int {
            if (distanceKm <= 0.0) return 0
            val full50s = (distanceKm / 50.0).toInt()
            val remainder = distanceKm % 50.0
            val extraDay = if (remainder > 30.0) 1 else 0
            val total = full50s + extraDay
            return total.coerceIn(0, 4)
        }
    }
}
