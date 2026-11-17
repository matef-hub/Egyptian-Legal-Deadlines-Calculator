package com.ateflaw.legaldeadlines.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ateflaw.legaldeadlines.domain.model.SavedDeadline
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils.toArabicDigits

@Composable
fun SavedDeadlineItem(
    deadline: SavedDeadline,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val finalDateFormatted = DateFormatterUtils.formatArabicFullDate(deadline.finalDeadline)
    val dayNameArabic = DateFormatterUtils.getArabicDayName(deadline.finalDeadline.dayOfWeek)
    val context = LocalContext.current
    var calendarError by remember { mutableStateOf<String?>(null) }
    val explanation = if (deadline.isProvisional) {
        deadline.calculationExplanation.replace("الميعاد النهائي واجب الالتزام به", "تاريخ نهاية المدة المحسوبة")
    } else deadline.calculationExplanation

    val onAddFinalDeadlineToCalendar = {
        val title = if (deadline.caseNumber.isNotBlank()) {
            "⚠️ ميعاد محفوظ للمراجعة: ${deadline.actionName} (دعوى ${deadline.caseNumber})".toArabicDigits()
        } else {
            "⚠️ ميعاد محفوظ للمراجعة: ${deadline.actionName}".toArabicDigits()
        }
        val descriptionText = "موعد محفوظ للمراجعة وليس ضماناً بصحة حساب الميعاد: $dayNameArabic، $finalDateFormatted\n" +
            "السند التشريعي: ${deadline.lawArticle.toArabicDigits()}\n\n" +
            explanation

        if (!openAllDayCalendarEvent(context, deadline.finalDeadline, title, descriptionText)) {
            calendarError = "تعذر فتح التقويم أو المتصفح."
        }
    }

    val onAddProactiveAlertToCalendar = {
        val proactiveDays = checkNotNull(deadline.proactiveReminderDays)
        val proactiveDate = deadline.finalDeadline.minusDays(proactiveDays.toLong())
        val proactiveFormatted = DateFormatterUtils.formatArabicFullDate(proactiveDate)
        val proactiveDayName = DateFormatterUtils.getArabicDayName(proactiveDate.dayOfWeek)

        val title = if (deadline.caseNumber.isNotBlank()) {
            "🔔 تنبيه استباقي: ${deadline.actionName} (دعوى ${deadline.caseNumber})".toArabicDigits()
        } else {
            "🔔 تنبيه استباقي: ${deadline.actionName}".toArabicDigits()
        }

        val descriptionText = "تنبيه إجرائي استباقي قبل فوات الميعاد بـ ${proactiveDays.toString().toArabicDigits()} أيام.\n" +
            "تاريخ التنبيه: $proactiveDayName، $proactiveFormatted.\n" +
            "الموعد المسجل للمراجعة: $dayNameArabic، $finalDateFormatted.\n" +
            "السند: ${deadline.lawArticle.toArabicDigits()}\n\n" +
            "الإجراء المطلوب: سرعة قيد الطعن وإيداع الصحيفة بقلم الكتاب.\n\n" +
            explanation

        if (!openAllDayCalendarEvent(context, proactiveDate, title, descriptionText, webTitle = "تنبيه لمراجعة تاريخ قانوني")) {
            calendarError = "تعذر فتح التقويم أو المتصفح."
        }
    }


    SweetAlertNotice(
        message = calendarError,
        kind = NoticeKind.ERROR,
        onDismiss = { calendarError = null }
    )

    val cardInteractionSource = remember { MutableInteractionSource() }
    val isCardPressed by cardInteractionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isCardPressed) 0.985f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "CardPressScale"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (isExpanded) 4.dp else 2.dp,
        animationSpec = tween(durationMillis = 200),
        label = "CardElevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = cardInteractionSource,
                indication = null,
                onClick = { isExpanded = !isExpanded }
            )
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row: Action Name and Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = deadline.actionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onAddFinalDeadlineToCalendar,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "إضافة الميعاد النهائي إلى تقويم Google / الجهاز",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف الميعاد",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "طي" else "عرض التفاصيل",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Metadata: Case Number & Client Name displayed in full-width rows to prevent vertical text compression
            if (deadline.caseNumber.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "رقم الدعوى: ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = deadline.caseNumber.toArabicDigits(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (deadline.clientName.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الموكل: ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = deadline.clientName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Highlighted Final Deadline Banner: Day and Date positioned side-by-side on the same line
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (deadline.isProvisional) "تاريخ حسابي للمراجعة" else "الميعاد النهائي",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Day name and date together horizontally e.g. "الأربعاء، 21 سبتمبر 2026"
                    Text(
                        text = "$dayNameArabic، $finalDateFormatted",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Proactive Reminder Indicator
            if (deadline.proactiveReminderDays != null && deadline.proactiveReminderDays > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "التنبيه في التقويم قبل الميعاد بـ ${deadline.proactiveReminderDays.toString().toArabicDigits()} أيام",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "إضافة للتقويم",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onAddProactiveAlertToCalendar)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "بدون تنبيه استباقي",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }


            // Expandable details (law article and calculation breakdown)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    if (deadline.lawArticle.isNotBlank()) {
                        Text(
                            text = "السند القانوني:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = deadline.lawArticle.toArabicDigits(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = "تفاصيل الاحتساب الإجرائي:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
