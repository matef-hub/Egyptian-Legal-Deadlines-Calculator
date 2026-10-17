package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.calculator.LegalDeadlineCalculator
import com.ateflaw.legaldeadlines.domain.model.DeadlineResult
import com.ateflaw.legaldeadlines.domain.model.DistanceType
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.repository.HolidayRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * UseCase to execute procedural deadline calculations leveraging the repository holiday data.
 * Highly optimized with in-memory lookup for instantaneous zero-latency calculations.
 */
class CalculateDeadlineUseCase(
    private val calculator: LegalDeadlineCalculator = LegalDeadlineCalculator(),
    private val holidayRepository: HolidayRepository
) {
    suspend operator fun invoke(
        announcementDate: LocalDate,
        rule: LegalRule,
        distanceType: DistanceType = DistanceType.NONE,
        additionalDistanceDays: Int = 0,
        judicialOrderDetails: String? = null
    ): DeadlineResult = withContext(Dispatchers.Default) {
        val holidaysList = withContext(Dispatchers.IO) {
            holidayRepository.getAllHolidaysList()
        }
        val holidayMap: Map<LocalDate, String> = holidaysList.associate { it.holidayDate to it.name }

        calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = distanceType,
            additionalDistanceDays = additionalDistanceDays,
            judicialOrderDetails = judicialOrderDetails,
            isHolidayProvider = { date ->
                val name = holidayMap[date]
                Pair(name != null, name)
            }
        )
    }

    suspend operator fun invoke(
        announcementDate: LocalDate,
        rule: LegalRule,
        additionalDistanceDays: Int
    ): DeadlineResult {
        val distanceType = when {
            additionalDistanceDays <= 0 -> DistanceType.NONE
            additionalDistanceDays <= 4 -> DistanceType.LOCAL
            additionalDistanceDays == 15 -> DistanceType.BORDER_AREA
            additionalDistanceDays == 60 -> DistanceType.ABROAD
            else -> DistanceType.LOCAL
        }
        return invoke(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = distanceType,
            additionalDistanceDays = additionalDistanceDays
        )
    }
}
