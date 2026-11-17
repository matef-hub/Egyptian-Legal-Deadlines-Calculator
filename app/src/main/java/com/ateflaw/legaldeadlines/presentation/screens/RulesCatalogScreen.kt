package com.ateflaw.legaldeadlines.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.ateflaw.legaldeadlines.utils.normalizedArabicSearchText
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.LegalRuleDraft
import com.ateflaw.legaldeadlines.domain.review.AuditLevel
import com.ateflaw.legaldeadlines.domain.review.LegalRuleReviewCatalog
import com.ateflaw.legaldeadlines.presentation.components.NoticeKind
import com.ateflaw.legaldeadlines.presentation.components.SweetAlertNotice
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.RuleCatalogUiState
import com.ateflaw.legaldeadlines.presentation.viewmodel.RuleCatalogViewModel

private const val ALL_LAWS = "كل القوانين"

@Composable
fun RulesCatalogScreen(
    viewModel: RuleCatalogViewModel,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RulesCatalogScreen(
        state = state,
        onDismissMessage = viewModel::dismissMessage,
        onSaveRule = viewModel::save,
        modifier = modifier,
        innerPadding = innerPadding
    )
}

@Composable
fun RulesCatalogScreen(
    state: RuleCatalogUiState,
    onDismissMessage: () -> Unit,
    onSaveRule: (LegalRule?, LegalRuleDraft) -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedLaw by rememberSaveable { mutableStateOf(ALL_LAWS) }
    var selectedReview by rememberSaveable { mutableStateOf("ALL") }
    var editingRuleId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingRevision by rememberSaveable { mutableStateOf<Int?>(null) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var openedAtToken by rememberSaveable { mutableLongStateOf(0L) }
    // Retain the revision opened by the user so a concurrent edit cannot be overwritten.
    val editingRule = editingRuleId?.let { id ->
        state.rules.firstOrNull { it.id == id }?.let { rule ->
            rule.copy(revision = editingRevision ?: rule.revision)
        }
    }

    val laws = remember(state.rules) {
        state.rules.map { it.lawName.ifBlank { "قانون غير مصنف" } }.distinct().sorted()
    }
    val searchRows = remember(state.rules) {
        state.rules.map { rule ->
            val finding = LegalRuleReviewCatalog.finding(rule)
            val fields = listOf(rule.actionName, rule.lawName, rule.lawArticle, rule.articleText,
                rule.notes, finding.note, rule.reviewerNote, rule.reviewMethod)
            Triple(rule, finding.level, fields.map { it.normalizedArabicSearchText() })
        }
    }
    val auditCounts = remember(searchRows) { searchRows.groupingBy { it.second }.eachCount() }
    val filtered = remember(searchRows, query, selectedLaw, selectedReview) {
        val term = query.normalizedArabicSearchText()
        searchRows.mapNotNull { (rule, level, fields) ->
            rule.takeIf {
                (selectedLaw == ALL_LAWS || selectedLaw == rule.lawName.ifBlank { "قانون غير مصنف" }) &&
                    (selectedReview == "ALL" || level.name == selectedReview) &&
                    (term.isBlank() || fields.any { it.contains(term) })
            }
        }
    }
    val grouped = remember(filtered) {
        filtered.groupBy { it.lawName.ifBlank { "قانون غير مصنف" } }.toSortedMap()
    }

    LaunchedEffect(state.savedToken) {
        if (showEditor && state.savedToken < openedAtToken) {
            openedAtToken = state.savedToken
        } else if (showEditor && state.savedToken > openedAtToken) {
            showEditor = false
            editingRuleId = null
            editingRevision = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding() + 6.dp, start = 16.dp, end = 16.dp)
    ) {
        // 1. رأس الشاشة المدمج (Compact Header) - Stats only
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "${state.rules.size} قاعدة • ${laws.size} قانون",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Button(
                onClick = {
                    onDismissMessage()
                    editingRuleId = null
                    editingRevision = null
                    openedAtToken = state.savedToken
                    showEditor = true
                },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة ميعاد", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("إضافة", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.height(10.dp))

        // 2. شريط البحث
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("ابحث عن إجراء أو مادة أو قانون...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // 3. فلاتر القوانين والحالات
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            (listOf(ALL_LAWS) + laws).forEach { law ->
                FilterChip(
                    selected = selectedLaw == law,
                    onClick = { selectedLaw = law },
                    label = { Text(law, maxLines = 1, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
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
                FilterChip(
                    selected = selectedReview == key,
                    onClick = { selectedReview = key },
                    label = { Text("$label ($count)", style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // 4. قائمة القواعد القانونية
        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد قواعد مطابقة للبحث.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                grouped.forEach { (law, rules) ->
                    item(key = "law:$law") {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = law,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${rules.size} ميعاد",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    items(rules, key = { "rule:${it.id}" }) { rule ->
                        RuleCatalogCard(
                            rule = rule,
                            onEdit = {
                                onDismissMessage()
                                editingRuleId = rule.id
                                editingRevision = rule.revision
                                openedAtToken = state.savedToken
                                showEditor = true
                            }
                        )
                    }
                }
            }
        }
    }

    SweetAlertNotice(
        message = state.error ?: state.notice,
        kind = if (state.error != null) NoticeKind.ERROR else NoticeKind.SUCCESS,
        onDismiss = onDismissMessage
    )

    if (showEditor && (editingRuleId == null || editingRule != null)) {
        RuleEditorDialog(
            rule = editingRule,
            selectedLaw = if (selectedLaw == ALL_LAWS) "" else selectedLaw,
            isSaving = state.saving,
            onDismiss = {
                if (!state.saving) {
                    showEditor = false
                    editingRuleId = null
                    editingRevision = null
                }
            },
            onSave = { draft -> onSaveRule(editingRule, draft) }
        )
    }
}

@Composable
private fun RuleCatalogCard(rule: LegalRule, onEdit: () -> Unit) {
    var expanded by remember(rule.id, rule.revision) { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val finding = LegalRuleReviewCatalog.finding(rule)

    // ألوان هادئة متناسقة مع الهوية الكلاسيكية
    val cardContainerColor = when (finding.level) {
        AuditLevel.CORRECTION -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
        AuditLevel.BLOCKED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
        AuditLevel.HIGH -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
        AuditLevel.REVIEW -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
    }

    // لون الشريط الجانبي الدال على حالة القاعدة
    val accentStripColor = when (finding.level) {
        AuditLevel.CORRECTION, AuditLevel.HIGH -> Color(0xFFB45309) // كهرماني دافئ
        AuditLevel.BLOCKED -> MaterialTheme.colorScheme.error
        AuditLevel.REVIEW -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.primary // كحلي للمعتمدة
    }

    val reviewText = when (finding.level) {
        AuditLevel.CORRECTION -> "تحتاج تصحيحًا"
        AuditLevel.BLOCKED -> "محجوبة"
        AuditLevel.HIGH -> "مراجعة مهمة"
        AuditLevel.REVIEW -> "نص ناقص"
        AuditLevel.TEXT_ADDED -> "بانتظار المطابقة"
        AuditLevel.ARTICLE_MATCHED -> "تحتاج مراجعة تطبيق"
        AuditLevel.VERIFIED -> if (rule.workbookDecision == "USER_APPROVED") "معتمدة (معدلة)" else "معتمدة"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        onClick = { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // شريط جانبي رفيع لتمييز الحالة بصرياً
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentStripColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // الصف العلوي: اسم الإجراء + كبسولة مدة الميعاد البارزة
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = rule.actionName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "${rule.duration} ${rule.unit.arabicDisplayName}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // رقم المادة وحالة الاعتماد في سطر واحد منظم
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = rule.lawArticle.ifBlank { "بدون رقم مادة" },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$reviewText (إصدار ${rule.revision})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = accentStripColor,
                        maxLines = 1
                    )
                }

                // ملاحظة التدقيق المختصرة
                if (finding.note.isNotBlank()) {
                    Text(
                        text = "ملاحظة: ${finding.note}",
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }

                // التفاصيل الموسعة عند الضغط على البطاقة أو زر "عرض النص"
                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (rule.reviewMethod.isNotBlank()) {
                            Text(
                                text = "طريقة الحساب: ${rule.reviewMethod}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "نص المادة:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = rule.articleText.ifBlank { "لم يُسجل النص الكامل للمادة بعد." },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (rule.reviewVersion > 0 && rule.reviewerNote.isNotBlank()) {
                            Text(
                                text = "ملاحظات المراجع: ${rule.reviewerNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (rule.notes.isNotBlank()) {
                            Text(
                                text = "ملاحظات إضافية: ${rule.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val source = if (LegalRuleReviewCatalog.matchesArticle940(rule)) {
                            LegalRuleReviewCatalog.ARTICLE_940_SOURCE
                        } else rule.officialSourceUrl
                        if (source.isNotBlank()) {
                            TextButton(
                                onClick = { uriHandler.openUri(source) },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("فتح رابط المصدر الرسمي", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // صف الإجراءات المصغر والمضغوط في أسفل البطاقة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("تعديل", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton(
                        onClick = { expanded = !expanded },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Outlined.VisibilityOff else Icons.Outlined.Description,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (expanded) "إخفاء النص" else "عرض النص",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
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
    var action by rememberSaveable(initial) { mutableStateOf(initial.actionName) }
    var law by rememberSaveable(initial) { mutableStateOf(initial.lawName) }
    var article by rememberSaveable(initial) { mutableStateOf(initial.lawArticle) }
    var articleText by rememberSaveable(initial) { mutableStateOf(initial.articleText) }
    var duration by rememberSaveable(initial) { mutableStateOf(initial.durationText) }
    var unit by rememberSaveable(initial) { mutableStateOf(initial.unit) }
    var notes by rememberSaveable(initial) { mutableStateOf(initial.notes) }
    var sourceUrl by rememberSaveable(initial) { mutableStateOf(initial.officialSourceUrl) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(decorFitsSystemWindows = false)
    ) {
        BoxWithConstraints(modifier = Modifier.imePadding()) {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = maxHeight * 0.92f),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (rule == null) "إضافة ميعاد قانوني" else "تعديل الميعاد القانوني",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss, enabled = !isSaving) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "سيُحفظ التعديل ويُعتمد نهائيًا في دليلك. ملاحظات ملف المراجعة وسجل النسخ السابقة محفوظان.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        OutlinedTextField(
                            value = law,
                            onValueChange = { law = it },
                            label = { Text("القانون *") },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = action,
                            onValueChange = { action = it },
                            label = { Text("اسم الإجراء أو الميعاد *") },
                            maxLines = 3,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("rule_editor_action")
                        )
                        OutlinedTextField(
                            value = article,
                            onValueChange = { article = it },
                            label = { Text("رقم المادة والسند القانوني *") },
                            maxLines = 3,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = articleText,
                            onValueChange = { articleText = it },
                            label = { Text("نص المادة بالكامل ${if (rule == null) "*" else ""}") },
                            minLines = 4,
                            maxLines = 9,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("rule_editor_article_text")
                        )
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { input -> duration = input.filter { it.isDigit() }.take(6) },
                            label = { Text("مقدار المدة *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("rule_editor_duration")
                        )
                        Text(
                            "وحدة المدة (أقل وحدة: يوم واحد)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DurationUnit.entries.filter { it != DurationUnit.HOURS }.forEach { choice ->
                                FilterChip(
                                    selected = choice == unit,
                                    onClick = { unit = choice },
                                    label = { Text(choice.arabicDisplayName) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = sourceUrl,
                            onValueChange = { sourceUrl = it },
                            label = { Text("رابط المصدر الرسمي (https://)") },
                            minLines = 1,
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات قانونية (اختياري)") },
                            minLines = 2,
                            maxLines = 5,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onSave(
                                    LegalRuleDraft(
                                        actionName = action,
                                        lawName = law,
                                        lawArticle = article,
                                        articleText = articleText,
                                        durationText = duration,
                                        unit = unit,
                                        notes = notes,
                                        officialSourceUrl = sourceUrl
                                    )
                                )
                            },
                            enabled = !isSaving,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.weight(1.4f).heightIn(min = 48.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("حفظ واعتماد", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isSaving,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) {
                            Text("إلغاء", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
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