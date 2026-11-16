package com.ateflaw.legaldeadlines.presentation

import com.ateflaw.legaldeadlines.domain.model.*
import com.ateflaw.legaldeadlines.domain.repository.*
import com.ateflaw.legaldeadlines.domain.usecase.*
import com.ateflaw.legaldeadlines.presentation.viewmodel.CalculatorViewModel
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorDistanceAndHolidayTest {
    private val rule = LegalRule(
        id = 2, actionName = "إجراء تجريبي", duration = 15, unit = DurationUnit.DAYS
    )
    private val ruleRepository = object : LegalRuleRepository {
        override fun getAllRules(): Flow<List<LegalRule>> = MutableStateFlow(listOf(rule))
        override suspend fun getRuleById(id: Long) = rule
        override suspend fun getRulesCount() = 1
        override suspend fun insertRules(rules: List<LegalRule>) {}
        override suspend fun addDraft(draft: LegalRuleDraft) = 1L
        override suspend fun updateDraft(id: Long, expectedRevision: Int, draft: LegalRuleDraft) {}
        override suspend fun clearRules() {}
    }
    private val holidays = MutableStateFlow<List<Holiday>>(emptyList())
    private val holidayRepository = object : HolidayRepository {
        override fun getAllHolidays(): Flow<List<Holiday>> = holidays
        override suspend fun getAllHolidaysList() = holidays.value
        override suspend fun getHolidaysCount() = 1
        override suspend fun getHolidayByDate(date: LocalDate) = holidays.value.firstOrNull { it.holidayDate == date }
        override suspend fun isHoliday(date: LocalDate) = holidays.value.any { it.holidayDate == date && it.isVerifiedOfficial }
        override suspend fun getHolidayName(date: LocalDate) = holidays.value.firstOrNull { it.holidayDate == date && it.isVerifiedOfficial }?.name
        override suspend fun insertHolidays(holidays: List<Holiday>) {}
        override suspend fun syncHolidays(year: Int, forceRefresh: Boolean) = Result.success(0)
    }
    private val deadlineRepository = object : DeadlineRepository {
        override fun getAllSavedDeadlines(): Flow<List<SavedDeadline>> = MutableStateFlow(emptyList())
        override suspend fun getDeadlineById(id: Long): SavedDeadline? = null
        override suspend fun insertDeadline(deadline: SavedDeadline) = 1L
        override suspend fun updateDeadline(deadline: SavedDeadline) {}
        override suspend fun deleteDeadline(deadline: SavedDeadline) {}
        override suspend fun deleteDeadlineById(id: Long) {}
    }

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = CalculatorViewModel(
        GetLegalRulesUseCase(ruleRepository), GetHolidaysUseCase(holidayRepository),
        CalculateDeadlineUseCase(holidayRepository = holidayRepository),
        SaveDeadlineUseCase(deadlineRepository), SyncHolidaysUseCase(holidayRepository)
    ).also { it.onRuleSelected(rule) }

    @Test fun abroadCannotSilentlyUseAnEditedThirtyDays() = runBlocking {
        val vm = viewModel()
        vm.onDistanceTypeChanged(DistanceType.ABROAD)
        vm.onAdditionalDistanceDaysChanged("30")
        assertEquals("60", vm.uiState.value.additionalDistanceDaysInput)
        vm.calculateDeadline()
        val result = withTimeout(5_000) { vm.uiState.first { it.result != null }.result!! }
        assertEquals(60, result.totalDistanceDays)
    }

    @Test fun judicialReductionRequiresOrderAndUsesItsDays() = runBlocking {
        val vm = viewModel()
        vm.onDistanceTypeChanged(DistanceType.ABROAD_JUDICIAL_REDUCTION)
        vm.onAdditionalDistanceDaysChanged("30")
        vm.calculateDeadline()
        assertTrue(vm.uiState.value.errorMessage.orEmpty().contains("الأمر القضائي"))
        assertNull(vm.uiState.value.result)

        vm.onJudicialOrderInputChanged("الأمر رقم ١٢")
        vm.calculateDeadline()
        val result = withTimeout(5_000) { vm.uiState.first { it.result != null }.result!! }
        assertEquals(30, result.totalDistanceDays)
        assertEquals("الأمر رقم ١٢", result.judicialOrderDetails)
    }

    @Test fun holidayBannerUsesOnlyVerifiedSourcedRecords() {
        val today = LocalDate.now()
        holidays.value = listOf(
            Holiday(holidayDate = today, name = "مرشحة", verificationStatus = "THIRD_PARTY_UNVERIFIED", sourceUrl = "https://example.com"),
            Holiday(holidayDate = today.plusDays(1), name = "بلا مصدر", verificationStatus = "VERIFIED_OFFICIAL"),
            Holiday(holidayDate = today.plusDays(2), name = "موثقة", verificationStatus = "VERIFIED_OFFICIAL", sourceUrl = "https://example.gov.eg")
        )
        val vm = viewModel()
        assertFalse(vm.uiState.value.isTodayHoliday)
        assertNull(vm.uiState.value.todayHolidayName)
        assertEquals(1, vm.uiState.value.totalHolidaysCount)

        holidays.value = listOf(Holiday(
            holidayDate = today, name = "موثقة", verificationStatus = "VERIFIED_OFFICIAL",
            sourceUrl = "https://example.gov.eg"
        ))
        assertTrue(vm.uiState.value.isTodayHoliday)
        assertEquals("موثقة", vm.uiState.value.todayHolidayName)
        assertEquals(1, vm.uiState.value.totalHolidaysCount)
    }
}
