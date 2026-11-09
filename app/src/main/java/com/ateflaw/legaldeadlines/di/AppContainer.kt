package com.ateflaw.legaldeadlines.di

import android.content.Context
import com.ateflaw.legaldeadlines.data.database.AppDatabase
import com.ateflaw.legaldeadlines.data.remote.RetrofitClient
import com.ateflaw.legaldeadlines.data.repository.DeadlineRepositoryImpl
import com.ateflaw.legaldeadlines.data.repository.HolidayRepositoryImpl
import com.ateflaw.legaldeadlines.data.repository.LegalRuleRepositoryImpl
import com.ateflaw.legaldeadlines.data.service.OfficialHolidayService
import com.ateflaw.legaldeadlines.domain.calculator.LegalDeadlineCalculator
import com.ateflaw.legaldeadlines.domain.repository.DeadlineRepository
import com.ateflaw.legaldeadlines.domain.repository.HolidayRepository
import com.ateflaw.legaldeadlines.domain.repository.LegalRuleRepository
import com.ateflaw.legaldeadlines.domain.usecase.CalculateDeadlineUseCase
import com.ateflaw.legaldeadlines.domain.usecase.DeleteSavedDeadlineUseCase
import com.ateflaw.legaldeadlines.domain.usecase.GetHolidaysUseCase
import com.ateflaw.legaldeadlines.domain.usecase.GetLegalRulesUseCase
import com.ateflaw.legaldeadlines.domain.usecase.GetSavedDeadlinesUseCase
import com.ateflaw.legaldeadlines.domain.usecase.SaveDeadlineUseCase
import com.ateflaw.legaldeadlines.domain.usecase.SyncHolidaysUseCase

/**
 * Dependency container providing singletons for repositories, services, and use cases.
 */
class AppContainer(private val context: Context) {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    val holidayService: OfficialHolidayService by lazy {
        OfficialHolidayService(
            holidayDao = database.holidayDao(),
            apiService = RetrofitClient.apiService
        )
    }

    val legalRuleRepository: LegalRuleRepository by lazy {
        LegalRuleRepositoryImpl(database.legalRuleDao())
    }

    val holidayRepository: HolidayRepository by lazy {
        HolidayRepositoryImpl(holidayService)
    }

    val deadlineRepository: DeadlineRepository by lazy {
        DeadlineRepositoryImpl(database.savedDeadlineDao())
    }

    val calculator: LegalDeadlineCalculator by lazy {
        LegalDeadlineCalculator()
    }

    // Use cases
    val calculateDeadlineUseCase: CalculateDeadlineUseCase by lazy {
        CalculateDeadlineUseCase(calculator, holidayRepository)
    }

    val getLegalRulesUseCase: GetLegalRulesUseCase by lazy {
        GetLegalRulesUseCase(legalRuleRepository)
    }

    val getHolidaysUseCase: GetHolidaysUseCase by lazy {
        GetHolidaysUseCase(holidayRepository)
    }

    val getSavedDeadlinesUseCase: GetSavedDeadlinesUseCase by lazy {
        GetSavedDeadlinesUseCase(deadlineRepository)
    }

    val saveDeadlineUseCase: SaveDeadlineUseCase by lazy {
        SaveDeadlineUseCase(deadlineRepository)
    }

    val deleteSavedDeadlineUseCase: DeleteSavedDeadlineUseCase by lazy {
        DeleteSavedDeadlineUseCase(deadlineRepository)
    }

    val syncHolidaysUseCase: SyncHolidaysUseCase by lazy {
        SyncHolidaysUseCase(holidayRepository)
    }
}
