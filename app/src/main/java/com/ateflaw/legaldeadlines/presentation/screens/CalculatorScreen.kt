package com.ateflaw.legaldeadlines.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ateflaw.legaldeadlines.domain.model.DeadlineResult
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.presentation.components.CalculationResultCard
import com.ateflaw.legaldeadlines.presentation.components.HolidayStatusBar
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.MainUiState
import com.ateflaw.legaldeadlines.presentation.viewmodel.MainViewModel
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CalculatorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    CalculatorScreenContent(
        uiState = uiState,
        onRuleSelected = viewModel::onRuleSelected,
        onAnnouncementDateChanged = viewModel::onAnnouncementDateChanged,
        onAdditionalDistanceDaysChanged = viewModel::onAdditionalDistanceDaysChanged,
        onCaseNumberChanged = viewModel::onCaseNumberChanged,
        onClientNameChanged = viewModel::onClientNameChanged,
        onCalculateDeadline = viewModel::calculateDeadline,
        onResetCalculation = viewModel::resetCalculation,
        onSaveCurrentDeadline = viewModel::saveCurrentDeadline,
        onDismissMessages = viewModel::dismissMessages,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreenContent(
    uiState: MainUiState,
    onRuleSelected: (LegalRule?) -> Unit,
    onAnnouncementDateChanged: (LocalDate) -> Unit,
    onAdditionalDistanceDaysChanged: (String) -> Unit,
    onCaseNumberChanged: (String) -> Unit,
    onClientNameChanged: (String) -> Unit,
    onCalculateDeadline: () -> Unit,
    onResetCalculation: () -> Unit,
    onSaveCurrentDeadline: () -> Unit,
    onDismissMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Form focus requesters for automatic validation navigation
    val ruleFocusRequester = remember { FocusRequester() }
    val distanceFocusRequester = remember { FocusRequester() }

    var isRuleError by remember { mutableStateOf(false) }
    var ruleErrorText by remember { mutableStateOf<String?>(null) }
    var isDistanceError by remember { mutableStateOf(false) }
    var distanceErrorText by remember { mutableStateOf<String?>(null) }

    // Searchable dropdown state
    var searchQuery by remember { mutableStateOf(uiState.selectedRule?.actionName ?: "") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Collapsible sections state
    var isAdditionalDistanceExpanded by remember { mutableStateOf(false) }
    var isCaseDetailsExpanded by remember { mutableStateOf(false) }

    // Keep searchQuery in sync when selectedRule changes externally (e.g. reset)
    LaunchedEffect(uiState.selectedRule) {
        if (uiState.selectedRule == null) {
            searchQuery = ""
        } else if (searchQuery.isBlank() || searchQuery != uiState.selectedRule.actionName) {
            searchQuery = uiState.selectedRule.actionName
        }
    }

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

    // Filter rules based on typed query
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

    // Scroll to result when calculated
    val isPreview = LocalInspectionMode.current
    LaunchedEffect(uiState.result) {
        if (!isPreview && uiState.result != null) {
            delay(250.milliseconds)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Holiday & Calendar Status Bar
            HolidayStatusBar(
                todayDate = uiState.todayDate,
                isTodayHoliday = uiState.isTodayHoliday,
                holidayName = uiState.todayHolidayName,
                loadedHolidaysCount = uiState.totalHolidaysCount
            )

            // General error banner if present from ViewModel
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onDismissMessages() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // 2. Calculation Input Form Card ("بيانات احتساب الميعاد")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Text(
                        text = "بيانات احتساب الميعاد",
                        style = MaterialTheme.typography.titleLarge,
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

                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = { isDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { query ->
                                    searchQuery = query
                                    isDropdownExpanded = true
                                    isRuleError = false
                                    ruleErrorText = null
                                    if (uiState.selectedRule != null && query != uiState.selectedRule.actionName) {
                                        onRuleSelected(null)
                                    }
                                },
                                placeholder = { Text("اختر الإجراء القانوني") },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    searchQuery = ""
                                                    onRuleSelected(null)
                                                    isDropdownExpanded = true
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "مسح"
                                                )
                                            }
                                        }
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                                    }
                                },
                                isError = isRuleError,
                                modifier = Modifier
                                    .menuAnchor(
                                        type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                                        enabled = true
                                    )
                                    .fillMaxWidth()
                                    .focusRequester(ruleFocusRequester),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                ExposedDropdownMenu(
                                    expanded = isDropdownExpanded,
                                    onDismissRequest = { isDropdownExpanded = false },
                                    modifier = Modifier
                                        .exposedDropdownSize()
                                        .heightIn(max = 280.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 4.dp,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    if (filteredRules.isEmpty()) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "لا توجد نتائج مطابقة لـ \"$searchQuery\"",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.Right,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            },
                                            onClick = {},
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        filteredRules.forEachIndexed { index, rule ->
                                            if (index > 0) {
                                                HorizontalDivider(
                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                                    thickness = 1.dp
                                                )
                                            }
                                            DropdownMenuItem(
                                                text = {
                                                    Column(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalAlignment = Alignment.End
                                                    ) {
                                                        Text(
                                                            text = rule.actionName,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            textAlign = TextAlign.Right,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.fillMaxWidth()
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "المدة: ${rule.duration} ${rule.unit.arabicDisplayName} | السند: ${rule.lawArticle}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            textAlign = TextAlign.Right,
                                                            modifier = Modifier.fillMaxWidth()
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    searchQuery = rule.actionName
                                                    onRuleSelected(rule)
                                                    isDropdownExpanded = false
                                                    isRuleError = false
                                                    ruleErrorText = null
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
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
                            Text(
                                text = "الميعاد المحدد: ${uiState.selectedRule.duration} ${uiState.selectedRule.unit.arabicDisplayName} (${uiState.selectedRule.lawArticle})",
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                            )
                        }
                    }

                    val statutoryDistance = uiState.selectedRule?.distanceDays ?: 15

                    // -------------------------------------------------------------
                    // FIELD 2: Date of Notice (تاريخ الإعلان)
                    // -------------------------------------------------------------
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
                                text = "تاريخ الإعلان",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val dateFormattedDisplay = DateFormatterUtils.formatDisplayDate(uiState.announcementDate)
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
                    // COLLAPSIBLE 1: Extra Distance (إضافة مسافة إضافية - اختياري)
                    // -------------------------------------------------------------
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
                                    .clickable { isAdditionalDistanceExpanded = !isAdditionalDistanceExpanded }
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
                                            text = "ميعاد المسافة الإضافي",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (isAdditionalDistanceExpanded) {
                                            Text(
                                                text = "المسافة القانونية: $statutoryDistance يوم",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            Text(
                                                text = "إضافة مسافة إضافية (اختياري)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Icon(
                                    imageVector = if (isAdditionalDistanceExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Expanded Content
                            AnimatedVisibility(
                                visible = isAdditionalDistanceExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.additionalDistanceDaysInput,
                                        onValueChange = {
                                            onAdditionalDistanceDaysChanged(it)
                                            isDistanceError = false
                                            distanceErrorText = null
                                        },
                                        placeholder = { Text("عدد الأيام") },
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

                                    // Red Reset/Remove Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onAdditionalDistanceDaysChanged("0")
                                                isAdditionalDistanceExpanded = false
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "إزالة المسافة الإضافية",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "إزالة المسافة الإضافية",
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
                                    imageVector = if (isCaseDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Expanded Content
                            AnimatedVisibility(
                                visible = isCaseDetailsExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
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
                                val distanceDays = uiState.additionalDistanceDaysInput.toIntOrNull()
                                if (uiState.additionalDistanceDaysInput.isNotBlank() && distanceDays == null) {
                                    isDistanceError = true
                                    distanceErrorText = "يرجى إدخال أرقام صحيحة لميعاد المسافة"
                                    coroutineScope.launch {
                                        scrollState.animateScrollTo(150)
                                        try {
                                            distanceFocusRequester.requestFocus()
                                        } catch (_: Exception) {}
                                        snackbarHostState.showSnackbar("تنبيه: ميعاد المسافة غير صحيح")
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
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
                                text = "احسب الميعاد",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. Calculation Result Card or Initial Welcome Graphic
            if (uiState.result != null) {
                CalculationResultCard(
                    result = uiState.result,
                    isSaved = uiState.isSavedSuccessfully,
                    onSaveClick = { onSaveCurrentDeadline() },
                    onBackClick = { onResetCalculation() }
                )
            } else {
                // Initial Welcome Banner matching Left Image Concept
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Golden Scales Graphic
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Balance,
                            contentDescription = "ميزان العدالة",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "دقة في الحساب .. أمان في المواعيد",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "لأن كل يوم يصنع فرقًا",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Snackbar Host for feedback
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // DatePickerDialog Component
    if (showDatePickerDialog) {
        val initialEpochMillis = uiState.announcementDate
            .atStartOfDay(ZoneId.systemDefault())
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
                                .atZone(ZoneId.systemDefault())
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

@Preview(showBackground = true)
@Composable
fun CalculatorScreenPreview() {
    val sampleRule = LegalRule(
        id = 1,
        actionName = "استئناف حكم مدني",
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
            onSaveCurrentDeadline = {},
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
            onSaveCurrentDeadline = {},
            onDismissMessages = {}
        )
    }
}
