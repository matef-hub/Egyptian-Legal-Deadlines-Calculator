package com.ateflaw.legaldeadlines.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ateflaw.legaldeadlines.domain.model.DistanceType
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.Holiday
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.presentation.screens.CalculatorScreenContent
import com.ateflaw.legaldeadlines.presentation.screens.HolidaysScreenContent
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.HolidaysUiState
import com.ateflaw.legaldeadlines.presentation.viewmodel.MainUiState
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DistanceAndHolidaysUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun abroadAndJudicialOrderHaveDistinctInputs() {
        val rule = LegalRule(id = 2, actionName = "إجراء تجريبي", duration = 15, unit = DurationUnit.DAYS)
        compose.setContent {
            var state by remember { mutableStateOf(MainUiState(rules = listOf(rule), selectedRule = rule)) }
            EgyptianLegalDeadlinesTheme {
                CalculatorScreenContent(
                    uiState = state,
                    onRuleSelected = {},
                    onAnnouncementDateChanged = {},
                    onDistanceTypeChanged = { type ->
                        state = state.copy(
                            distanceType = type,
                            additionalDistanceDaysInput = type.statutoryDays?.toString() ?: ""
                        )
                    },
                    onAdditionalDistanceDaysChanged = { state = state.copy(additionalDistanceDaysInput = it) },
                    onJudicialOrderInputChanged = { state = state.copy(judicialOrderInput = it) },
                    onCaseNumberChanged = {}, onClientNameChanged = {},
                    onCalculateDeadline = {}, onResetCalculation = {},
                    onSaveCurrentDeadline = {}, onDismissMessages = {}
                )
            }
        }
        compose.onNodeWithTag("distance_section_toggle").performScrollTo().performClick()
        compose.onNodeWithTag("distance_type_ABROAD").performScrollTo().performClick()
        compose.onNodeWithTag("fixed_distance_days").performScrollTo().assertTextContains("٦٠", substring = true)
        compose.onNodeWithText("30 يوماً (مخفض)").assertDoesNotExist()
        compose.onNodeWithTag("distance_type_ABROAD_JUDICIAL_REDUCTION").performScrollTo().performClick()
        compose.onNodeWithTag("distance_days_input").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("judicial_order_input").performScrollTo().assertIsDisplayed()
    }

    @Test fun thursdayCandidateIsNotLabeledShiftedOrVerified() {
        val candidate = Holiday(
            id = 1, holidayDate = LocalDate.of(2026, 3, 19), name = "عطلة للتجربة",
            verificationStatus = "THIRD_PARTY_UNVERIFIED", sourceUrl = "https://example.com"
        )
        compose.setContent {
            EgyptianLegalDeadlinesTheme {
                HolidaysScreenContent(
                    uiState = HolidaysUiState(holidays = listOf(candidate), isLoading = false),
                    onRefreshClick = {}, onDismissSnackbar = {}
                )
            }
        }
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("عطلة مرشحة — تحتاج مراجعة"))
        compose.onNodeWithText("عطلة مرشحة — تحتاج مراجعة").assertIsDisplayed()
        compose.onNodeWithText("عطلة رسمية (الخميس/مرحّلة)").assertDoesNotExist()
    }
}
