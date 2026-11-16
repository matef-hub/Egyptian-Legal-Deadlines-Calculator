package com.ateflaw.legaldeadlines.domain.model

import java.time.LocalDate

/** A holiday record, including candidates that are not eligible for deadline extensions. */
data class Holiday(
    val id: Long = 0,
    val holidayDate: LocalDate,
    val name: String,
    val verificationStatus: String = "UNVERIFIED",
    val sourceUrl: String = ""
) {
    /** Shared eligibility check for calculations, today's status and calendar presentation. */
    val isVerifiedOfficial: Boolean
        get() = verificationStatus == "VERIFIED_OFFICIAL" && sourceUrl.startsWith("https://")
}
