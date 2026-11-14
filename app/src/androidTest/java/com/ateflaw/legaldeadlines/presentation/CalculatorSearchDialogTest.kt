package com.ateflaw.legaldeadlines.presentation

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.presentation.screens.CalculatorScreenContent
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.MainUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Device/emulator regression: a searchable legal procedure must accept typing
 * in a focusable dialog (the old exposed dropdown popup did not).
 * Run with connectedDebugAndroidTest on a device or emulator.
 */
@RunWith(AndroidJUnit4::class)
class CalculatorSearchDialogTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun legalProcedureSearchDialogAcceptsInputAndFiltersResults() {
        val rules = listOf(
            LegalRule(
                id = 1L,
                actionName = "استئناف حكم مدني",
                duration = 40,
                unit = DurationUnit.DAYS,
                lawArticle = "المادة 227 مرافعات"
            ),
            LegalRule(
                id = 2L,
                actionName = "إعلان الرغبة في الأخذ بالشفعة",
                duration = 15,
                unit = DurationUnit.DAYS,
                lawArticle = "المادة 940 مدني"
            )
        )

        composeTestRule.setContent {
            EgyptianLegalDeadlinesTheme {
                CalculatorScreenContent(
                    uiState = MainUiState(rules = rules),
                    onRuleSelected = {},
                    onAnnouncementDateChanged = {},
                    onAdditionalDistanceDaysChanged = {},
                    onCaseNumberChanged = {},
                    onClientNameChanged = {},
                    onCalculateDeadline = {},
                    onResetCalculation = {},
                    onSaveCurrentDeadline = {},
                    onDismissMessages = {}
                )
            }
        }

        composeTestRule.onNodeWithText("اختر الإجراء القانوني").performClick()
        composeTestRule.onNodeWithTag("legal_procedure_search_input")
            .performTextInput("الشفعة")
        composeTestRule.onNodeWithText("إعلان الرغبة في الأخذ بالشفعة").assertExists()
    }
}
