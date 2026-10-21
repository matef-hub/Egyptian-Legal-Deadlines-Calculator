package com.ateflaw.legaldeadlines.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ateflaw.legaldeadlines.domain.model.DistanceType
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.StartRule
import com.ateflaw.legaldeadlines.presentation.components.CalculationResultCard
import com.ateflaw.legaldeadlines.presentation.components.custom.EditorialMargin
import com.ateflaw.legaldeadlines.presentation.components.custom.TechnicalTextField
import com.ateflaw.legaldeadlines.presentation.ui.theme.AntiqueBrass
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.ui.theme.JudicialNavy
import com.ateflaw.legaldeadlines.presentation.ui.theme.VintageBackground
import com.ateflaw.legaldeadlines.presentation.viewmodel.CalculatorViewModel
import com.ateflaw.legaldeadlines.presentation.viewmodel.MainUiState
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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
    onDistanceTypeChanged: (DistanceType) -> Unit = {},
    onAdditionalDistanceDaysChanged: (String) -> Unit,
    onCaseNumberChanged: (String) -> Unit,
    onClientNameChanged: (String) -> Unit,
    onCalculateDeadline: () -> Unit,
    onResetCalculation: () -> Unit,
    onSaveCurrentDeadline: (Int?) -> Unit,
    onReminderDaysChanged: (Int?) -> Unit = {},
    onDismissMessages: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val ruleFocusRequester = remember { FocusRequester() }
    var isRuleError by remember { mutableStateOf(false) }
    var ruleErrorText by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf(uiState.selectedRule?.actionName ?: "") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var isAdditionalDistanceExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.selectedRule) {
        if (uiState.selectedRule == null) searchQuery = ""
        else if (searchQuery.isBlank() || searchQuery != uiState.selectedRule.actionName) {
            searchQuery = uiState.selectedRule.actionName
        }
    }

    val filteredRules by remember(uiState.rules, searchQuery) {
        derivedStateOf {
            val query = searchQuery.trim()
            if (query.isBlank() || (uiState.selectedRule != null && query == uiState.selectedRule.actionName)) {
                uiState.rules
            } else {
                uiState.rules.filter { rule ->
                    rule.actionName.contains(query, ignoreCase = true) ||
                    rule.lawArticle.contains(query, ignoreCase = true) ||
                    rule.notes.contains(query, ignoreCase = true)
                }
            }
        }
    }

    val isPreview = LocalInspectionMode.current
    LaunchedEffect(uiState.result) {
        if (!isPreview && uiState.result != null) {
            delay(250.milliseconds)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(VintageBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "القانون\nليس مجرد نصوص\nبل مواعيد ..",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start,
                        lineHeight = 16.sp
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "مِيْعَاد",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = JudicialNavy
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Balance,
                            contentDescription = null,
                            tint = AntiqueBrass,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    Text(
                        text = "حاسبة المواعيد القانونية المصرية",
                        style = MaterialTheme.typography.labelSmall,
                        color = AntiqueBrass
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "احسب ميعادك",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = JudicialNavy
                )
                Spacer(modifier = Modifier.width(16.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.width(8.dp))
                HorizontalDivider(modifier = Modifier.width(32.dp), color = AntiqueBrass, thickness = 2.dp)
            }

            if (uiState.errorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(uiState.errorMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onDismissMessages() }) { Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer) }
                    }
                }
            }

            EditorialMargin(number = "01", label = "بيانات الإجراء", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // FIELD 1
                    ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = { isDropdownExpanded = it }, modifier = Modifier.fillMaxWidth()) {
                        TechnicalTextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query; isDropdownExpanded = true; isRuleError = false; ruleErrorText = null
                                if (uiState.selectedRule != null && query != uiState.selectedRule.actionName) onRuleSelected(null)
                            },
                            label = "نوع الإجراء",
                            icon = Icons.AutoMirrored.Filled.ListAlt,
                            isError = isRuleError,
                            errorText = ruleErrorText ?: "يرجى اختيار الإجراء القانوني",
                            modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth().focusRequester(ruleFocusRequester),
                            innerContent = {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.foundation.text.BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { query ->
                                            searchQuery = query; isDropdownExpanded = true; isRuleError = false; ruleErrorText = null
                                            if (uiState.selectedRule != null && query != uiState.selectedRule.actionName) onRuleSelected(null)
                                        },
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = JudicialNavy),
                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(JudicialNavy),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (uiState.selectedRule != null && !isDropdownExpanded) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(color = AntiqueBrass.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AntiqueBrass, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("${uiState.selectedRule.duration} يوم", style = MaterialTheme.typography.labelSmall, color = AntiqueBrass)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                                }
                            }
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }, modifier = Modifier.exposedDropdownSize().heightIn(max = 280.dp), shape = RoundedCornerShape(12.dp)) {
                                if (filteredRules.isEmpty()) {
                                    DropdownMenuItem(text = { Text("لا توجد نتائج") }, onClick = {})
                                } else {
                                    filteredRules.forEachIndexed { index, rule ->
                                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), thickness = 1.dp)
                                        DropdownMenuItem(
                                            text = {
                                                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                                    Text(rule.actionName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text("المدة: ${rule.duration} ${rule.unit.arabicDisplayName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = { searchQuery = rule.actionName; onRuleSelected(rule); isDropdownExpanded = false; isRuleError = false; ruleErrorText = null }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // FIELD 2
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val arabicDate = DateFormatterUtils.formatDisplayDate(uiState.announcementDate)
                            .replace("0", "٠").replace("1", "١").replace("2", "٢").replace("3", "٣")
                            .replace("4", "٤").replace("5", "٥").replace("6", "٦").replace("7", "٧")
                            .replace("8", "٨").replace("9", "٩").replace("/", " / ")
                        
                        Box(modifier = Modifier.fillMaxWidth().clickable { showDatePickerDialog = true }) {
                            TechnicalTextField(
                                value = arabicDate,
                                onValueChange = {},
                                label = "تاريخ الإعلان",
                                icon = Icons.Default.CalendarMonth,
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 2.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("لا يُحسب يوم الإعلان ضمن الميعاد", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }

                    // FIELDS 3 & 4
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TechnicalTextField(
                            value = uiState.caseNumber,
                            onValueChange = onCaseNumberChanged,
                            label = "رقم الدعوى",
                            icon = Icons.Default.Gavel,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                        TechnicalTextField(
                            value = uiState.clientName,
                            onValueChange = onClientNameChanged,
                            label = "اسم الموكل",
                            icon = Icons.Default.PersonOutline,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // FIELD 5
                    val distanceLabel = when (uiState.distanceType) {
                        DistanceType.NONE -> "بدون إضافة"
                        DistanceType.LOCAL -> "محلية (${uiState.additionalDistanceDaysInput} أيام)"
                        DistanceType.ABROAD -> "بالخارج (${uiState.additionalDistanceDaysInput} يوماً)"
                        else -> "بدون إضافة"
                    }
                    ExposedDropdownMenuBox(expanded = isAdditionalDistanceExpanded, onExpandedChange = { isAdditionalDistanceExpanded = it }, modifier = Modifier.fillMaxWidth()) {
                        TechnicalTextField(
                            value = distanceLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = "ميعاد المسافة",
                            icon = Icons.Default.LocationOn,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAdditionalDistanceExpanded) },
                            modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable).fillMaxWidth()
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            ExposedDropdownMenu(expanded = isAdditionalDistanceExpanded, onDismissRequest = { isAdditionalDistanceExpanded = false }) {
                                DropdownMenuItem(text = { Text("بدون إضافة") }, onClick = { onDistanceTypeChanged(DistanceType.NONE); isAdditionalDistanceExpanded = false })
                                DropdownMenuItem(text = { Text("محلية (يوم)") }, onClick = { onDistanceTypeChanged(DistanceType.LOCAL); onAdditionalDistanceDaysChanged("1"); isAdditionalDistanceExpanded = false })
                                DropdownMenuItem(text = { Text("مسافة بالخارج (60 يوماً)") }, onClick = { onDistanceTypeChanged(DistanceType.ABROAD); onAdditionalDistanceDaysChanged("60"); isAdditionalDistanceExpanded = false })
                            }
                        }
                    }
                }
            }

            if (uiState.result != null) {
                EditorialMargin(number = "02", label = "نتيجة الحساب", modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalculationResultCard(result = uiState.result, isSaved = false)
                        
                        Button(
                            onClick = onCalculateDeadline,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JudicialNavy)
                        ) {
                            Text("إعادة الحساب", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Refresh, contentDescription = null)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        TextButton(
                            onClick = { onSaveCurrentDeadline(null) },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = JudicialNavy)
                        ) {
                            Text("حفظ الميعاد", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.BookmarkBorder, contentDescription = null)
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (uiState.selectedRule == null) {
                            isRuleError = true
                            ruleErrorText = "يرجى اختيار الإجراء القانوني أولاً من القائمة"
                            isDropdownExpanded = true
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                                try {
                                    ruleFocusRequester.requestFocus()
                                } catch (_: Exception) {}
                                snackbarHostState.showSnackbar("تنبيه: يجب اختيار الإجراء القانوني أولاً")
                            }
                        } else {
                            onCalculateDeadline()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = JudicialNavy)
                ) {
                    Text("احسب الميعاد", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            if (uiState.result != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "EGYPT • LAW • JUSTICE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = AntiqueBrass.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.announcementDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        onAnnouncementDateChanged(date)
                    }
                    showDatePickerDialog = false
                }) {
                    Text("موافق")
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

@Preview(showBackground = true)
@Composable
fun CalculatorScreenPreview() {
    EgyptianLegalDeadlinesTheme {
        CalculatorScreenContent(
            uiState = MainUiState(
                clientName = "أحمد محمد أحمد",
                caseNumber = "٢٠٢٦ / ٢٠٤",
                announcementDate = LocalDate.of(2026, 10, 12),
                selectedRule = LegalRule(
                    id = 1,
                    actionName = "استئناف حكم مدني",
                    duration = 40,
                    unit = com.ateflaw.legaldeadlines.domain.model.DurationUnit.DAYS
                ),
                result = com.ateflaw.legaldeadlines.domain.model.DeadlineResult(
                    startDate = LocalDate.of(2026, 10, 13),
                    provisionalDate = LocalDate.of(2026, 11, 21),
                    duration = 40,
                    unit = com.ateflaw.legaldeadlines.domain.model.DurationUnit.DAYS,
                    ruleDistanceDays = 0,
                    additionalDistanceDays = 0,
                    totalDistanceDays = 0,
                    excludedDays = emptyList(),
                    finalDeadline = LocalDate.of(2026, 11, 21),
                    explanation = "",
                    lawArticle = "",
                    notes = "",
                    actionSummary = "استئناف حكم مدني",
                    milestones = emptyList()
                )
            ),
            onRuleSelected = {},
            onAnnouncementDateChanged = {},
            onDistanceTypeChanged = {},
            onAdditionalDistanceDaysChanged = {},
            onCaseNumberChanged = {},
            onClientNameChanged = {},
            onCalculateDeadline = {},
            onResetCalculation = {},
            onSaveCurrentDeadline = {},
            onReminderDaysChanged = {},
            onDismissMessages = {}
        )
    }
}
