package com.ateflaw.legaldeadlines.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ateflaw.legaldeadlines.domain.model.DeadlineResult
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.StartRule
import com.ateflaw.legaldeadlines.presentation.components.CalculationResultCard
import com.ateflaw.legaldeadlines.presentation.components.HolidayStatusBar
import com.ateflaw.legaldeadlines.presentation.components.NoticeKind
import com.ateflaw.legaldeadlines.presentation.components.SweetAlertNotice
import com.ateflaw.legaldeadlines.presentation.components.custom.DiamondDivider
import com.ateflaw.legaldeadlines.presentation.ui.theme.AntiqueBrass
import com.ateflaw.legaldeadlines.presentation.ui.theme.ArefRuqaaFontFamily
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.CalculatorViewModel
import com.ateflaw.legaldeadlines.presentation.viewmodel.MainUiState
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils.toArabicDigits
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalculatorScreenContent(
        uiState = uiState,
        onRuleSelected = viewModel::onRuleSelected,
        onAnnouncementDateChanged = viewModel::onAnnouncementDateChanged,
        onDistanceTypeChanged = viewModel::onDistanceTypeChanged,
        onAdditionalDistanceDaysChanged = viewModel::onAdditionalDistanceDaysChanged,
        onCaseNumberChanged = viewModel::onCaseNumberChanged,
        onClientNameChanged = viewModel::onClientNameChanged,
        onCalculateDeadline = viewModel::calculateDeadline,
        onResetCalculation = viewModel::resetCalculation,
        onSaveCurrentDeadline = { days -> viewModel.saveCurrentDeadline(days) },
        onReminderDaysChanged = viewModel::onProactiveReminderDaysChanged,
        onDismissMessages = viewModel::dismissMessages,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreenContent(
    modifier: Modifier = Modifier,
    uiState: MainUiState,
    onRuleSelected: (LegalRule?) -> Unit,
    onAnnouncementDateChanged: (LocalDate) -> Unit,
    onDistanceTypeChanged: (com.ateflaw.legaldeadlines.domain.model.DistanceType) -> Unit = {},
    onAdditionalDistanceDaysChanged: (String) -> Unit,
    onCaseNumberChanged: (String) -> Unit,
    onClientNameChanged: (String) -> Unit,
    onCalculateDeadline: () -> Unit,
    onResetCalculation: () -> Unit,
    onSaveCurrentDeadline: (Int?) -> Unit,
    onReminderDaysChanged: (Int?) -> Unit = {},
    onDismissMessages: () -> Unit
) {
    val scrollState = rememberScrollState()
    val resultScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var transientAlert by remember { mutableStateOf<String?>(null) }

    // Form focus requesters for automatic validation navigation
    val searchFocusRequester = remember { FocusRequester() }
    val distanceFocusRequester = remember { FocusRequester() }

    var isRuleError by remember { mutableStateOf(false) }
    var ruleErrorText by remember { mutableStateOf<String?>(null) }
    var isDistanceError by remember { mutableStateOf(false) }
    var distanceErrorText by remember { mutableStateOf<String?>(null) }

    // Searchable dropdown state
    var searchQuery by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Staggered Entrance Animation State
    var isScreenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isScreenVisible = true
    }

    val formAlpha by animateFloatAsState(
        targetValue = if (isScreenVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "FormAlpha"
    )
    val formTranslationY by animateFloatAsState(
        targetValue = if (isScreenVisible) 0f else 80f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "FormTranslationY"
    )

    // Collapsible sections state
    var isAdditionalDistanceExpanded by remember { mutableStateOf(false) }
    var isCaseDetailsExpanded by remember { mutableStateOf(false) }

    // Expand distance section automatically if distance days input is > 0
    LaunchedEffect(uiState.additionalDistanceDaysInput) {
        val days = uiState.additionalDistanceDaysInput.toIntOrNull() ?: 0
        if (days > 0) {
            isAdditionalDistanceExpanded = true
        }
    }

    // Expand case details automatically if fields are filled
    LaunchedEffect(uiState.caseNumber, uiState.clientName) {
        if (uiState.caseNumber.isNotBlank() || uiState.clientName.isNotBlank()) {
            isCaseDetailsExpanded = true
        }
    }

    // Filter rules based on typed query (Limit to 5 results to prevent lag)
    val filteredRules by remember(uiState.rules, searchQuery) {
        derivedStateOf {
            val query = searchQuery.trim()
            if (query.isBlank() || (uiState.selectedRule != null && query == uiState.selectedRule.actionName)) {
                uiState.rules.take(30)
            } else {
                uiState.rules.filter { rule ->
                    rule.actionName.contains(query, ignoreCase = true) ||
                    rule.lawArticle.contains(query, ignoreCase = true) ||
                    rule.notes.contains(query, ignoreCase = true)
                }.take(30)
            }
        }
    }

    // Always reset the independent result scroll position for each calculation.
    LaunchedEffect(uiState.result) {
        if (uiState.result != null) resultScrollState.scrollTo(0)
    }

    // The result is shown in its own scrollable page starting at the top.

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        if (uiState.result != null) {
            // Dedicated result view prevents the form from appearing cut off above
            // the result and always starts at the top of the final deadline.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(resultScrollState)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
            ) {
                CalculationResultCard(
                    result = uiState.result,
                    isSaved = uiState.isSavedSuccessfully,
                    initialReminderDays = uiState.proactiveReminderDays,
                    onReminderDaysChanged = onReminderDaysChanged,
                    onSaveClick = { days -> onSaveCurrentDeadline(days) },
                    onBackClick = { onResetCalculation() }
                )
            }
        } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
                .graphicsLayer {
                    alpha = formAlpha
                    translationY = formTranslationY
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Compact title: the app bar already provides the screen heading.
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "احتساب الميعاد",
                    fontFamily = ArefRuqaaFontFamily,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "دفتر المواعيد الإجرائية",
                    style = MaterialTheme.typography.labelSmall,
                    color = AntiqueBrass
                )
            }

            // 1. Top Holiday & Calendar Status Bar
            HolidayStatusBar(
                todayDate = uiState.todayDate,
                isTodayHoliday = uiState.isTodayHoliday,
                holidayName = uiState.todayHolidayName,
                loadedHolidaysCount = uiState.totalHolidaysCount
            )

            // 2. Calculation Input Form Card ("بيانات احتساب الميعاد")
            val cardElevation by animateDpAsState(
                targetValue = if (uiState.selectedRule != null) 4.dp else 2.dp,
                animationSpec = tween(durationMillis = 200),
                label = "FormCardElevation"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    Text(
                        text = "بيانات احتساب الميعاد",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // -------------------------------------------------------------
                    // FIELD 1: Legal Action (الإجراء القانوني)
                    // -------------------------------------------------------------
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "الإجراء القانوني",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // The selected value is read-only. Search happens in a
                            // focusable Dialog window, not inside a popup menu.
                            Surface(
                                onClick = {
                                    searchQuery = ""
                                    isDropdownExpanded = true
                                },
                                modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isRuleError) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                ),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = uiState.selectedRule?.actionName?.toArabicDigits()
                                            ?: "اختر الإجراء القانوني",
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (uiState.selectedRule != null) {
                                IconButton(
                                    onClick = {
                                        onRuleSelected(null)
                                        searchQuery = ""
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "مسح الإجراء"
                                    )
                                }
                            }
                        }

                        if (isRuleError) {
                            Text(
                                text = ruleErrorText ?: "يرجى اختيار الإجراء القانوني",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                            )
                        } else if (uiState.selectedRule != null) {
                            val rule = uiState.selectedRule
                            val durationLabel = if (rule.unit == DurationUnit.DAYS && rule.duration > 10L)
                                "يومًا" else rule.unit.arabicDisplayName
                            Text(
                                text = ("الميعاد المحدد: ${rule.duration} $durationLabel (${rule.lawArticle})").toArabicDigits(),
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    val statutoryDistance = uiState.selectedRule?.distanceDays ?: 15

                    // -------------------------------------------------------------
                    // FIELD 2: Trigger Event Date (تاريخ الواقعة المنشئة للميعاد)
                    // -------------------------------------------------------------
                    val triggerLabel = if (uiState.selectedRule?.id == 10L) {
                        "تاريخ قرار شطب الدعوى"
                    } else {
                        uiState.selectedRule?.triggerEventLabel?.ifBlank { null }
                            ?: "تاريخ الواقعة المنشئة للميعاد"
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = triggerLabel.toArabicDigits(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (uiState.selectedRule != null && uiState.selectedRule.startRule == StartRule.NEXT_DAY) {
                            Text(
                                text = "إعمالاً للمادة 15 مرافعات: لا يُحسب هذا اليوم، ويبدأ سريان الميعاد من اليوم التالي.".toArabicDigits(),
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }

                        val dateFormattedDisplay = DateFormatterUtils.formatArabicFullDate(uiState.announcementDate)
                        OutlinedTextField(
                            value = dateFormattedDisplay,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showDatePickerDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "اختر التاريخ",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }

                    // -------------------------------------------------------------
                    // COLLAPSIBLE 1: Extra Distance (ميعاد مسافة الطريق - المواد 16 و17)
                    // -------------------------------------------------------------
                    val distanceArrowRotation by animateFloatAsState(
                        targetValue = if (isAdditionalDistanceExpanded) 180f else 0f,
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        label = "DistanceArrowRotation"
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = if (isAdditionalDistanceExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Header Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = uiState.selectedRule?.id != 10L) {
                                        isAdditionalDistanceExpanded = !isAdditionalDistanceExpanded
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isAdditionalDistanceExpanded) Icons.Default.RemoveCircle else Icons.Default.Straighten,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "ميعاد المسافة",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (uiState.selectedRule?.id == 10L) {
                                                "لا تُضاف أيام مسافة تلقائيًا لتجديد الدعوى من الشطب"
                                            } else if (isAdditionalDistanceExpanded) {
                                                "المسافة القانونية المقررة بالقاعدة: $statutoryDistance يوم"
                                            } else "إضافة أيام مسافة (محلية أو بالخارج)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.graphicsLayer { rotationZ = distanceArrowRotation }
                                )
                            }

                            // Expanded Content
                            AnimatedVisibility(
                                visible = isAdditionalDistanceExpanded && uiState.selectedRule?.id != 10L,
                                enter = expandVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(200)),
                                exit = shrinkVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(180))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "اختر نوع ميعاد المسافة الواجب تطبيقه وفقاً لقانون المرافعات:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )

                                    // Category Type Selector: NONE vs LOCAL vs ABROAD
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            com.ateflaw.legaldeadlines.domain.model.DistanceType.NONE to "بدون مسافة",
                                            com.ateflaw.legaldeadlines.domain.model.DistanceType.LOCAL to "محلية (م16)",
                                            com.ateflaw.legaldeadlines.domain.model.DistanceType.ABROAD to "بالخارج (م17)"
                                        ).forEach { (dType, dLabel) ->
                                            val isTypeSelected = uiState.distanceType == dType
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isTypeSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, if (isTypeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        onDistanceTypeChanged(dType)
                                                        isDistanceError = false
                                                        distanceErrorText = null
                                                    }
                                            ) {
                                                Text(
                                                    text = dLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isTypeSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isTypeSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    when (uiState.distanceType) {
                                        com.ateflaw.legaldeadlines.domain.model.DistanceType.NONE -> {
                                            Text(
                                                text = "محل إقامة الخصم في نفس دائرة المحكمة، لا تضاف أيام مسافة إضافية.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline,
                                                fontSize = 11.sp
                                            )
                                        }
                                        com.ateflaw.legaldeadlines.domain.model.DistanceType.LOCAL -> {
                                            Text(
                                                text = "المادة 16 مرافعات: يوم لكل 50 كم بحد أقصى 4 أيام للمسافة المحلية.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 11.sp
                                            )

                                            // Presets for local (1 to 4 days)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                listOf(1 to "يوم (50 كم)", 2 to "يومان", 3 to "3 أيام", 4 to "4 أيام (أقصى)").forEach { (days, label) ->
                                                    val isSelected = uiState.additionalDistanceDaysInput == days.toString()
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clickable {
                                                                onAdditionalDistanceDaysChanged(days.toString())
                                                                isDistanceError = false
                                                                distanceErrorText = null
                                                            }
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        com.ateflaw.legaldeadlines.domain.model.DistanceType.ABROAD -> {
                                            Text(
                                                text = "المادة 17 مرافعات: 60 يوماً كأصل عام لمن موطنه بالخارج، ويجوز حتى 90 يوماً.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 11.sp
                                            )

                                            // Presets for abroad
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                listOf(30 to "30 يوماً (مخفض)", 60 to "60 يوماً (متوسط/أحمر)", 90 to "90 يوماً (دول أخرى)").forEach { (days, label) ->
                                                    val isSelected = uiState.additionalDistanceDaysInput == days.toString()
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clickable {
                                                                onAdditionalDistanceDaysChanged(days.toString())
                                                                isDistanceError = false
                                                                distanceErrorText = null
                                                            }
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        com.ateflaw.legaldeadlines.domain.model.DistanceType.BORDER_AREA -> {
                                            Text(
                                                text = "المادة 16 فقرة 2 مرافعات: ميعاد المسافة 15 يوماً للمناطق الحدودية النائية.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 11.sp
                                            )
                                        }
                                        com.ateflaw.legaldeadlines.domain.model.DistanceType.ABROAD_JUDICIAL_REDUCTION -> {
                                            Text(
                                                text = "المادة 17 فقرة 2 مرافعات: يجوز إنقاص ميعاد الستين يوماً بأمر على عريضة.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 11.sp
                                            )
                                        }
                                        com.ateflaw.legaldeadlines.domain.model.DistanceType.ABROAD_SERVED_IN_EGYPT -> {
                                            Text(
                                                text = "المادة 17 فقرة 3 مرافعات: لا يسري ميعاد مسافة الخارج على من أُعلن لشخصه بمصر.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    if (uiState.distanceType != com.ateflaw.legaldeadlines.domain.model.DistanceType.NONE) {
                                        OutlinedTextField(
                                            value = uiState.additionalDistanceDaysInput,
                                            onValueChange = {
                                                onAdditionalDistanceDaysChanged(it)
                                                isDistanceError = false
                                                distanceErrorText = null
                                            },
                                            label = { Text("عدد الأيام الفعلي") },
                                            suffix = { Text("يوم") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Straighten,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            isError = isDistanceError,
                                            supportingText = {
                                                if (isDistanceError) {
                                                    Text(
                                                        text = distanceErrorText ?: "أرقام صحيحة فقط",
                                                        color = MaterialTheme.colorScheme.error,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(distanceFocusRequester),
                                            shape = RoundedCornerShape(10.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    }

                                    // Reset / Remove Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onDistanceTypeChanged(com.ateflaw.legaldeadlines.domain.model.DistanceType.NONE)
                                                onAdditionalDistanceDaysChanged("0")
                                                isAdditionalDistanceExpanded = false
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "إلغاء ميعاد المسافة",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "إلغاء ميعاد المسافة (العودة للوضع الافتراضي)",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // -------------------------------------------------------------
                    // COLLAPSIBLE 2: Case Details (بيانات القضية - اختياري)
                    // -------------------------------------------------------------
                    val caseDetailsArrowRotation by animateFloatAsState(
                        targetValue = if (isCaseDetailsExpanded) 180f else 0f,
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        label = "CaseDetailsArrowRotation"
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = if (isCaseDetailsExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Header Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isCaseDetailsExpanded = !isCaseDetailsExpanded }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "بيانات القضية (اختياري)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.graphicsLayer { rotationZ = caseDetailsArrowRotation }
                                )
                            }

                            // Expanded Content
                            AnimatedVisibility(
                                visible = isCaseDetailsExpanded,
                                enter = expandVertically(animationSpec = tween(200, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(200)),
                                exit = shrinkVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(180))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.caseNumber,
                                        onValueChange = { onCaseNumberChanged(it) },
                                        placeholder = { Text("رقم القضية / الدعوى") },
                                        leadingIcon = {
                                            Icon(imageVector = Icons.Default.Folder, contentDescription = null)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )

                                    OutlinedTextField(
                                        value = uiState.clientName,
                                        onValueChange = { onClientNameChanged(it) },
                                        placeholder = { Text("اسم الموكل / الخصم") },
                                        leadingIcon = {
                                            Icon(imageVector = Icons.Default.Person, contentDescription = null)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // -------------------------------------------------------------
                    // MAIN ACTION BUTTON: Calculate (احسب الميعاد)
                    // -------------------------------------------------------------
                    val calculateButtonInteractionSource = remember { MutableInteractionSource() }
                    val isCalculatePressed by calculateButtonInteractionSource.collectIsPressedAsState()
                    val calculateButtonScale by animateFloatAsState(
                        targetValue = if (isCalculatePressed) 0.97f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "CalculateButtonScale"
                    )

                    Button(
                        onClick = {
                            if (uiState.selectedRule == null) {
                                isRuleError = true
                                ruleErrorText = "يرجى اختيار الإجراء القانوني أولاً من القائمة"
                                isDropdownExpanded = true
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(0)
                                    transientAlert = "يجب اختيار الإجراء القانوني أولًا."
                                }
                            } else {
                                val distanceDays = uiState.additionalDistanceDaysInput.toIntOrNull()
                                if (uiState.additionalDistanceDaysInput.isNotBlank() && distanceDays == null) {
                                    isDistanceError = true
                                    distanceErrorText = "يرجى إدخال أرقام صحيحة لميعاد المسافة"
                                    coroutineScope.launch {
                                        scrollState.animateScrollTo(150)
                                        try {
                                            distanceFocusRequester.requestFocus()
                                        } catch (_: Exception) {}
                                        transientAlert = "يرجى تصحيح أيام المسافة أولًا."
                                    }
                                } else {
                                    isRuleError = false
                                    ruleErrorText = null
                                    isDistanceError = false
                                    distanceErrorText = null
                                    onCalculateDeadline()
                                }
                            }
                        },
                        interactionSource = calculateButtonInteractionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .graphicsLayer {
                                scaleX = calculateButtonScale
                                scaleY = calculateButtonScale
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !uiState.isCalculating
                    ) {
                        if (uiState.isCalculating) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (uiState.selectedRule?.reviewStatus == "VERIFIED_ACCURATE") "احسب الميعاد" else "احسب تاريخًا استرشاديًا",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }


        }
        } // End of editable input form

        // Dialog-style feedback, no persistent error banner or Snackbar.
        SweetAlertNotice(
            message = uiState.errorMessage ?: transientAlert ?: uiState.successMessage,
            kind = if (uiState.errorMessage != null || transientAlert != null) {
                NoticeKind.ERROR
            } else NoticeKind.SUCCESS,
            onDismiss = {
                transientAlert = null
                onDismissMessages()
            }
        )
    }

    if (isDropdownExpanded) {
        // Dialog has its own focusable window: unlike a non-editable exposed
        // dropdown, its editable search field can acquire IME focus.
        Dialog(onDismissRequest = { isDropdownExpanded = false }) {
            val dialogKeyboardController = LocalSoftwareKeyboardController.current
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "اختيار الإجراء القانوني",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            isDropdownExpanded = false
                            dialogKeyboardController?.hide()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق البحث")
                        }
                    }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester),
                        placeholder = { Text("ابحث باسم الإجراء أو رقم المادة") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                        contentPadding = PaddingValues(bottom = 4.dp)
                    ) {
                        if (filteredRules.isEmpty()) {
                            item {
                                Text(
                                    text = "لا توجد إجراءات مطابقة للبحث",
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(filteredRules, key = { it.id }) { rule ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onRuleSelected(rule)
                                            isRuleError = false
                                            ruleErrorText = null
                                            searchQuery = ""
                                            isDropdownExpanded = false
                                            dialogKeyboardController?.hide()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = rule.actionName.toArabicDigits(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "المدة: ${rule.duration.toString().toArabicDigits()} ${rule.unit.arabicDisplayName} — ${rule.lawArticle.toArabicDigits()}",
                                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
            LaunchedEffect(Unit) {
                // Wait until the dialog is attached before requesting focus.
                delay(120L)
                searchFocusRequester.requestFocus()
                dialogKeyboardController?.show()
            }
        }
    }

    // Do not show a warning dialog when registering/calculating a deadline.
    // Important legal review comments remain visible with the relevant article
    // in the catalog; explicit errors and save success retain 5-second notices.

    // DatePickerDialog Component
    if (showDatePickerDialog) {
        val initialEpochMillis = uiState.announcementDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialEpochMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedLocalDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            onAnnouncementDateChanged(selectedLocalDate)
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("تأكيد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("إلغاء")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, fontScale = 1.15f, name = "Long RTL legal procedure")
@Composable
fun CalculatorScreenPreview() {
    val sampleRule = LegalRule(
        id = 1,
        actionName = "استئناف الأحكام الصادرة في الدعاوى المدنية والتجارية",
        duration = 40,
        unit = DurationUnit.DAYS,
        lawArticle = "المادة 227 مرافعات",
        notes = "يحتسب من اليوم التالي لتاريخ صدور الحكم"
    )

    val sampleUiState = MainUiState(
        rules = listOf(sampleRule),
        selectedRule = sampleRule,
        announcementDate = LocalDate.now(),
        todayDate = LocalDate.now(),
        totalHolidaysCount = 12
    )

    EgyptianLegalDeadlinesTheme {
        CalculatorScreenContent(
            uiState = sampleUiState,
            onRuleSelected = {},
            onAnnouncementDateChanged = {},
            onAdditionalDistanceDaysChanged = {},
            onCaseNumberChanged = {},
            onClientNameChanged = {},
            onCalculateDeadline = {},
            onResetCalculation = {},
            onSaveCurrentDeadline = { _ -> },
            onDismissMessages = {}
        )
    }
}

@Preview(showBackground = true, name = "With Result")
@Composable
fun CalculatorScreenWithResultPreview() {
    val fixedDate = LocalDate.of(2026, 9, 12)
    val sampleRule = LegalRule(
        id = 1,
        actionName = "صحيفة دعوى",
        duration = 13,
        unit = DurationUnit.DAYS,
        lawArticle = "قانون المرافعات"
    )

    val sampleResult = DeadlineResult(
        startDate = fixedDate,
        provisionalDate = fixedDate.plusDays(13),
        duration = 13,
        unit = DurationUnit.DAYS,
        ruleDistanceDays = 15,
        additionalDistanceDays = 0,
        totalDistanceDays = 15,
        excludedDays = emptyList(),
        finalDeadline = fixedDate.plusDays(13),
        explanation = "تم احتساب الميعاد وفقًا للقاعدة القانونية المحددة مع مراعاة الإجازات والعطلات الرسمية.",
        lawArticle = sampleRule.lawArticle,
        notes = "",
        actionSummary = "صحيفة دعوى"
    )

    val sampleUiState = MainUiState(
        rules = listOf(sampleRule),
        selectedRule = sampleRule,
        result = sampleResult,
        announcementDate = fixedDate,
        todayDate = fixedDate
    )

    EgyptianLegalDeadlinesTheme {
        CalculatorScreenContent(
            uiState = sampleUiState,
            onRuleSelected = {},
            onAnnouncementDateChanged = {},
            onAdditionalDistanceDaysChanged = {},
            onCaseNumberChanged = {},
            onClientNameChanged = {},
            onCalculateDeadline = {},
            onResetCalculation = {},
            onSaveCurrentDeadline = { _ -> },
            onDismissMessages = {}
        )
    }
}
