package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.calculator.LegalDeadlineCalculator
import com.ateflaw.legaldeadlines.domain.model.DeadlineResult
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.repository.HolidayRepository
import java.time.LocalDate

/**
 * UseCase to execute procedural deadline calculations leveraging the repository holiday data.
 */
class CalculateDeadlineUseCase(
    private val calculator: LegalDeadlineCalculator = LegalDeadlineCalculator(),
    private val holidayRepository: HolidayRepository
) {
    suspend operator fun invoke(
        announcementDate: LocalDate,
        rule: LegalRule,
        additionalDistanceDays: Int = 0
    ): DeadlineResult {
        return calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            additionalDistanceDays = additionalDistanceDays,
            isHolidayProvider = { date ->
                // Synchronously checks cached holidays
                val isHol = kotlinx.coroutines.runBlocking { holidayRepository.isHoliday(date) }
                val holName = if (isHol) {
                    kotlinx.coroutines.runBlocking { holidayRepository.getHolidayName(date) }
                } else null
                Pair(isHol, holName)
            }
        )
    }
}
