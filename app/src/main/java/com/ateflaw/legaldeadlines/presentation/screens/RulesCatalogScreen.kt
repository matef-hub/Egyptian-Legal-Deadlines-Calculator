package com.ateflaw.legaldeadlines.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.RuleCatalogUiState
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.LegalRuleDraft
import com.ateflaw.legaldeadlines.domain.review.AuditLevel
import com.ateflaw.legaldeadlines.domain.review.LegalRuleReviewCatalog
import com.ateflaw.legaldeadlines.presentation.components.SweetAlertNotice
import com.ateflaw.legaldeadlines.presentation.components.NoticeKind
import com.ateflaw.legaldeadlines.presentation.viewmodel.RuleCatalogViewModel

private const val ALL_LAWS = "كل القوانين"

@Composable
fun RulesCatalogScreen(
    viewModel: RuleCatalogViewModel,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    onOpenBackup: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    RulesCatalogScreen(
        state = state,
        onDismissMessage = viewModel::dismissMessage,
        onSaveRule = viewModel::save,
        modifier = modifier,
        innerPadding = innerPadding,
        onOpenBackup = onOpenBackup
    )
}

@Composable
fun RulesCatalogScreen(
    state: RuleCatalogUiState,
    onDismissMessage: () -> Unit,
    onSaveRule: (LegalRule?, LegalRuleDraft) -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    onOpenBackup: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedLaw by remember { mutableStateOf(ALL_LAWS) }
    var selectedReview by remember { mutableStateOf("ALL") }
    var editingRule by remember { mutableStateOf<LegalRule?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var openedAtToken by remember { androidx.compose.runtime.mutableLongStateOf(0L) }
    val laws = remember(state.rules) {
        state.rules.map { it.lawName.ifBlank { "قانون غير مصنف" } }.distinct().sorted()
    }
    val auditCounts = remember(state.rules) {
        state.rules.groupingBy { LegalRuleReviewCatalog.finding(it).level }.eachCount()
    }
    val filtered = remember(state.rules, query, selectedLaw, selectedReview) {
        val search = query.trim()
        state.rules.filter { rule ->
            val law = rule.lawName.ifBlank { "قانون غير مصنف" }
            (selectedLaw == ALL_LAWS || law == selectedLaw) &&
                (selectedReview == "ALL" ||
                    LegalRuleReviewCatalog.finding(rule).level.name == selectedReview) &&
                (search.isBlank() || listOf(
                    rule.actionName, law, rule.lawArticle, rule.articleText, rule.notes,
                    LegalRuleReviewCatalog.finding(rule).note, rule.reviewerNote, rule.reviewMethod
                ).any { it.contains(search, ignoreCase = true) })
        }
    }
    val grouped = remember(filtered) {
        filtered.groupBy { it.lawName.ifBlank { "قانون غير مصنف" } }.toSortedMap()
    }

    LaunchedEffect(state.savedToken) {
        if (showEditor && state.savedToken > openedAtToken) {
            showEditor = false
            editingRule = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding() + 8.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "دليل مواعيد الإجراءات",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${state.rules.size} قاعدة • ${laws.size} قانون",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            androidx.compose.material3.IconButton(
                onClick = onOpenBackup,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = "نسخ احتياطي", tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    onDismissMessage()
                    editingRule = null
                    openedAtToken = state.savedToken
                    showEditor = true
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة ميعاد", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("إضافة")
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("ابحث عن إجراء أو مادة أو قانون") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (listOf(ALL_LAWS) + laws).forEach { law ->
                FilterChip(selected = selectedLaw == law, onClick = { selectedLaw = law },
                    label = { Text(law, maxLines = 1) })
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple("ALL", "جميع الحالات", state.rules.size),
                Triple("CORRECTION", "تحتاج تصحيح", auditCounts[AuditLevel.CORRECTION] ?: 0),
                Triple("BLOCKED", "محجوبة", auditCounts[AuditLevel.BLOCKED] ?: 0),
                Triple("VERIFIED", "تمت مراجعتها", auditCounts[AuditLevel.VERIFIED] ?: 0),
                Triple("HIGH", "تدقيق قانوني", auditCounts[AuditLevel.HIGH] ?: 0),
                Triple("REVIEW", "نص ناقص", auditCounts[AuditLevel.REVIEW] ?: 0),
                Triple("TEXT_ADDED", "النص مضاف", auditCounts[AuditLevel.TEXT_ADDED] ?: 0),
                Triple("ARTICLE_MATCHED", "النص مطابق", auditCounts[AuditLevel.ARTICLE_MATCHED] ?: 0)
            ).forEach { (key, label, count) ->
                FilterChip(selected = selectedReview == key,
                    onClick = { selectedReview = key },
                    label = { Text("$label ($count)") })
            }
        }
        Spacer(Modifier.height(8.dp))
        // Five-second SweetAlertNotice is displayed below.
        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد قواعد مطابقة للبحث.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grouped.forEach { (law, rules) ->
                    item(key = "law:$law") {
                        Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp)) {
                            Text(law, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("${rules.size} ميعاد", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            HorizontalDivider(Modifier.padding(top = 6.dp))
                        }
                    }
                    items(rules, key = { "rule:${it.id}" }) { rule ->
                        RuleCatalogCard(rule = rule, onEdit = {
                            onDismissMessage()
                            editingRule = rule
                            openedAtToken = state.savedToken
                            showEditor = true
                        })
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    SweetAlertNotice(
        message = state.error ?: state.notice,
        kind = if (state.error != null) NoticeKind.ERROR else NoticeKind.SUCCESS,
        onDismiss = onDismissMessage
    )

    if (showEditor) {
        RuleEditorDialog(
            rule = editingRule,
            selectedLaw = if (selectedLaw == ALL_LAWS) "" else selectedLaw,
            isSaving = state.saving,
            onDismiss = { if (!state.saving) { showEditor = false; editingRule = null } },
            onSave = { draft -> onSaveRule(editingRule, draft) }
        )
    }
}

@Composable
private fun RuleCatalogCard(rule: LegalRule, onEdit: () -> Unit) {
    var expanded by remember(rule.id, rule.revision) { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val finding = LegalRuleReviewCatalog.finding(rule)
    val reviewColor = when (finding.level) {
        AuditLevel.CORRECTION -> MaterialTheme.colorScheme.tertiaryContainer
        AuditLevel.BLOCKED -> MaterialTheme.colorScheme.errorContainer
        AuditLevel.HIGH -> MaterialTheme.colorScheme.tertiaryContainer
        AuditLevel.REVIEW -> MaterialTheme.colorScheme.surfaceVariant
        AuditLevel.TEXT_ADDED -> MaterialTheme.colorScheme.secondaryContainer
        AuditLevel.ARTICLE_MATCHED -> MaterialTheme.colorScheme.secondaryContainer
        AuditLevel.VERIFIED -> MaterialTheme.colorScheme.secondaryContainer
    }
    val reviewText = when (finding.level) {
        AuditLevel.CORRECTION -> "تحتاج تصحيحًا — راجع الملاحظة"
        AuditLevel.BLOCKED -> "محجوبة من الحساب"
        AuditLevel.HIGH -> "مراجعة قانونية مهمة"
        AuditLevel.REVIEW -> "نص المادة ناقص"
        AuditLevel.TEXT_ADDED -> "النص محفوظ — بانتظار المطابقة"
        AuditLevel.ARTICLE_MATCHED -> "النص والمدة مطابقان للمصدر — التطبيق يحتاج مراجعة"
        AuditLevel.VERIFIED -> if (rule.workbookDecision == "USER_APPROVED") "تم حفظ واعتماد التعديل" else "معتمدة بملف المراجعة"
    }
    Card(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = reviewColor),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(rule.actionName, modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("${rule.duration} ${rule.unit.arabicDisplayName}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
            Text(rule.lawArticle.ifBlank { "بدون رقم مادة" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis)
            Text("$reviewText • إصدار ${rule.revision}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium)
            Text("ملاحظة بالتعديل / التدقيق: ${finding.note}",
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall)
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    if (rule.reviewMethod.isNotBlank()) Text("نوع الميعاد: ${rule.reviewMethod}", style = MaterialTheme.typography.bodySmall)
                    Text("نص المادة", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge)
                    Text(rule.articleText.ifBlank { "لم يُسجل النص الكامل للمادة بعد." },
                        style = MaterialTheme.typography.bodyMedium)
                    if (rule.reviewVersion > 0 && rule.reviewerNote.isNotBlank()) {
                        Text("ملاحظات المراجع بالملف: ${rule.reviewerNote}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (rule.notes.isNotBlank()) {
                        Text("ملاحظات: ${rule.notes}", style = MaterialTheme.typography.bodySmall)
                    }
                    val source = if (LegalRuleReviewCatalog.matchesArticle940(rule)) {
                        LegalRuleReviewCatalog.ARTICLE_940_SOURCE
                    } else rule.officialSourceUrl
                    if (source.isNotBlank()) {
                        TextButton(onClick = { uriHandler.openUri(source) }) {
                            Text("فتح رابط المصدر الوارد بملف المراجعة")
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("تعديل")
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "إخفاء النص" else "عرض النص")
                }
            }
        }
    }
}

@Composable
private fun RuleEditorDialog(
    rule: LegalRule?,
    selectedLaw: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (LegalRuleDraft) -> Unit
) {
    val initial = remember(rule?.id, rule?.revision, selectedLaw) {
        rule?.let { LegalRuleDraft.fromRule(it) } ?: LegalRuleDraft(
            actionName = "", lawName = selectedLaw, lawArticle = "",
            articleText = "", durationText = "", unit = DurationUnit.DAYS
        )
    }
    var action by remember(initial) { mutableStateOf(initial.actionName) }
    var law by remember(initial) { mutableStateOf(initial.lawName) }
    var article by remember(initial) { mutableStateOf(initial.lawArticle) }
    var articleText by remember(initial) { mutableStateOf(initial.articleText) }
    var duration by remember(initial) { mutableStateOf(initial.durationText) }
    var unit by remember(initial) { mutableStateOf(initial.unit) }
    var notes by remember(initial) { mutableStateOf(initial.notes) }
    var sourceUrl by remember(initial) { mutableStateOf(initial.officialSourceUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (rule == null) "إضافة ميعاد قانوني" else "تعديل الميعاد القانوني") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 470.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(color = MaterialTheme.colorScheme.errorContainer) {
                    Text(
                        "سيُحفظ التعديل ويُعتمد نهائيًا في دليلك. ملاحظات ملف المراجعة وسجل النسخ السابقة محفوظان.",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OutlinedTextField(value = law, onValueChange = { law = it },
                    label = { Text("القانون *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = action, onValueChange = { action = it },
                    label = { Text("اسم الإجراء أو الميعاد *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = article, onValueChange = { article = it },
                    label = { Text("رقم المادة والسند القانوني *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = articleText, onValueChange = { articleText = it },
                    label = { Text("نص المادة بالكامل ${if (rule == null) "*" else ""}") },
                    minLines = 4, maxLines = 9, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = duration,
                    onValueChange = { input -> duration = input.filter { it.isDigit() }.take(6) },
                    label = { Text("مقدار المدة *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("وحدة المدة (أقل وحدة: يوم واحد)", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DurationUnit.entries.filter { it != DurationUnit.HOURS }.forEach { choice ->
                        FilterChip(selected = choice == unit, onClick = { unit = choice },
                            label = { Text(choice.arabicDisplayName) })
                    }
                }
                OutlinedTextField(value = sourceUrl, onValueChange = { sourceUrl = it },
                    label = { Text("رابط المصدر الرسمي (https://)") },
                    minLines = 1, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("ملاحظات قانونية (اختياري)") },
                    minLines = 2, modifier = Modifier.fillMaxWidth())
                // Validation feedback appears in the shared five-second notice.
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(LegalRuleDraft(
                    actionName = action,
                    lawName = law,
                    lawArticle = article,
                    articleText = articleText,
                    durationText = duration,
                    unit = unit,
                    notes = notes,
                    officialSourceUrl = sourceUrl
                ))
            }, enabled = !isSaving) {
                if (isSaving) CircularProgressIndicator(
                    modifier = Modifier.height(18.dp).width(18.dp),
                    strokeWidth = 2.dp
                ) else Text("حفظ واعتماد")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isSaving) { Text("إلغاء") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun RulesCatalogScreenPreview() {
    EgyptianLegalDeadlinesTheme {
        RulesCatalogScreen(
            state = RuleCatalogUiState(
                rules = listOf(
                    LegalRule(
                        id = 1,
                        actionName = "تقديم الطعن بالنقض",
                        lawName = "قانون المرافعات المدنية والتجارية",
                        lawArticle = "المادة 252",
                        articleText = "ميعاد الطعن بطريق النقض ستون يوماً.",
                        duration = 60,
                        unit = DurationUnit.DAYS,
                        revision = 1,
                        reviewMethod = "حساب الميعاد باليوم"
                    ),
                    LegalRule(
                        id = 2,
                        actionName = "الاستئناف",
                        lawName = "قانون المرافعات المدنية والتجارية",
                        lawArticle = "المادة 227",
                        articleText = "ميعاد الاستئناف أربعون يوماً.",
                        duration = 40,
                        unit = DurationUnit.DAYS,
                        revision = 1,
                        reviewMethod = "حساب الميعاد باليوم"
                    )
                ),
                loading = false
            ),
            onDismissMessage = {},
            onSaveRule = { _, _ -> }
        )
    }
}
