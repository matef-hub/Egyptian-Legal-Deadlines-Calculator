package com.ateflaw.legaldeadlines.presentation.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
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

    // Form inputs and focus requesters for automatic validation navigation
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

    // Keep searchQuery in sync when selectedRule changes externally (e.g. reset)
    LaunchedEffect(uiState.selectedRule) {
        if (uiState.selectedRule == null) {
            searchQuery = ""
        } else if (searchQuery.isBlank() || searchQuery != uiState.selectedRule.actionName) {
            searchQuery = uiState.selectedRule.actionName
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
    LaunchedEffect(uiState.result) {
        if (uiState.result != null) {
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

            // 2. Calculation Input Form
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
                    Text(
                        text = "بيانات احتساب الميعاد",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Searchable Legal Action with RTL-aligned Dropdown
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
                                // If user altered text away from current selection, clear selected rule
                                if (uiState.selectedRule != null && query != uiState.selectedRule.actionName) {
                                    onRuleSelected(null)
                                }
                            },
                            label = { Text("الإجراء القانوني (اكتب للبحث أو اختر)") },
                            placeholder = { Text("مثلاً: استئناف، تجديد، نقض، معارضة...") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Gavel, contentDescription = null)
                            },
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
                                                contentDescription = "مسح النص"
                                            )
                                        }
                                    }
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                                }
                            },
                            isError = isRuleError,
                            supportingText = {
                                if (isRuleError) {
                                    Text(
                                        text = ruleErrorText ?: "يرجى اختيار الإجراء القانوني",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else if (uiState.selectedRule != null) {
                                    Text(
                                        text = "الميعاد المحدد: ${uiState.selectedRule.duration} ${uiState.selectedRule.unit.arabicDisplayName} (${uiState.selectedRule.lawArticle})",
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                } else {
                                    Text("يمكنك كتابة كلمة مثل 'تجديد' لتصفية القائمة")
                                }
                            },
                            modifier = Modifier
                                .menuAnchor(
                                    type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                                    enabled = true
                                )
                                .fillMaxWidth()
                                .focusRequester(ruleFocusRequester),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        // Dropdown with strict Right-to-Left Arabic alignment
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (filteredRules.isEmpty()) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "لا توجد إجراءات قضائية مطابقة لكلمة \"$searchQuery\"",
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
                                    filteredRules.forEach { rule ->
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

                    // Announcement Date Picker (Formatted in clear Arabic: e.g. "الإثنين، 7 سبتمبر 2026")
                    val dateFormattedArabic = DateFormatterUtils.formatArabicDateWithDay(uiState.announcementDate)
                    OutlinedTextField(
                        value = dateFormattedArabic,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("تاريخ الإعلان / صدور الحكم") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showDatePickerDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "اختر التاريخ"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePickerDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Additional Distance Days Input
                    OutlinedTextField(
                        value = uiState.additionalDistanceDaysInput,
                        onValueChange = {
                            onAdditionalDistanceDaysChanged(it)
                            isDistanceError = false
                            distanceErrorText = null
                        },
                        label = { Text("ميعاد المسافة الإضافي (بالأيام - اختياري)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Straighten, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = isDistanceError,
                        supportingText = {
                            if (isDistanceError) {
                                Text(
                                    text = distanceErrorText ?: "ميعاد مسافة غير صحيح",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                val originalDistance = uiState.selectedRule?.distanceDays ?: 0
                                Text("ميعاد المسافة القانوني للقاعدة: $originalDistance يوم")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(distanceFocusRequester),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Case Number (Optional)
                    OutlinedTextField(
                        value = uiState.caseNumber,
                        onValueChange = { onCaseNumberChanged(it) },
                        label = { Text("رقم القضية / الدعوى (اختياري)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Client Name (Optional)
                    OutlinedTextField(
                        value = uiState.clientName,
                        onValueChange = { onClientNameChanged(it) },
                        label = { Text("اسم الموكل / الخصم (اختياري)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calculate Button with instant validation navigation
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
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !uiState.isCalculating
                    ) {
                        if (uiState.isCalculating) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(24.dp)
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Calculate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "احسب الميعاد",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. Calculation Result Card Display
            if (uiState.result != null) {
                CalculationResultCard(
                    result = uiState.result,
                    isSaved = uiState.isSavedSuccessfully,
                    onSaveClick = { onSaveCurrentDeadline() },
                    onBackClick = { onResetCalculation() }
                )
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

    // Material 3 DatePickerDialog
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
    val sampleRule = LegalRule(
        id = 1,
        actionName = "نقض في مواد الجنح",
        duration = 60,
        unit = DurationUnit.DAYS,
        lawArticle = "المادة 34 من قانون حالات وإجراءات الطعن أمام محكمة النقض"
    )

    val sampleResult = DeadlineResult(
        startDate = LocalDate.now(),
        provisionalDate = LocalDate.now().plusDays(60),
        duration = 60,
        unit = DurationUnit.DAYS,
        ruleDistanceDays = 0,
        additionalDistanceDays = 0,
        totalDistanceDays = 0,
        excludedDays = emptyList(),
        finalDeadline = LocalDate.now().plusDays(60),
        explanation = "يحتسب الميعاد 60 يوماً من اليوم التالي لتاريخ صدور الحكم.",
        lawArticle = sampleRule.lawArticle,
        notes = ""
    )

    val sampleUiState = MainUiState(
        rules = listOf(sampleRule),
        selectedRule = sampleRule,
        result = sampleResult,
        announcementDate = LocalDate.now(),
        todayDate = LocalDate.now()
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
