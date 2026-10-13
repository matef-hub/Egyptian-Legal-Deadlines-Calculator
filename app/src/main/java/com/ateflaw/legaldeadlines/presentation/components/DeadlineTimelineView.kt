package com.ateflaw.legaldeadlines.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ateflaw.legaldeadlines.domain.model.CalculationMilestone
import com.ateflaw.legaldeadlines.domain.model.MilestoneType
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Modern interactive milestone timeline view displaying the procedural lifecycle of a legal deadline.
 * Visualizes:
 * 1. Trigger event (announcement/judgment)
 * 2. Counting start (Article 15 Procedures Law)
 * 3. Base duration expiration
 * 4. Distance days extension (Article 16/17 Procedures Law)
 * 5. Holiday and Friday rollover (Article 18 Procedures Law)
 * 6. Final binding deadline with live countdown
 */
@Composable
fun DeadlineTimelineView(
    milestones: List<CalculationMilestone>,
    finalDeadline: LocalDate,
    modifier: Modifier = Modifier
) {
    if (milestones.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            // Header with Live Countdown Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "المسار الإجرائي لسريان الميعاد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "تحليل محطات الحساب وفقاً لقانون المرافعات",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                CountdownBadge(finalDeadline = finalDeadline)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Milestones Vertical Flow
            milestones.forEachIndexed { index, milestone ->
                val isLast = index == milestones.lastIndex
                TimelineNodeItem(
                    milestone = milestone,
                    isLast = isLast
                )
            }
        }
    }
}

@Composable
fun CountdownBadge(
    finalDeadline: LocalDate,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val daysRemaining = ChronoUnit.DAYS.between(today, finalDeadline)

    val (badgeBg, badgeText, badgeColor, icon) = when {
        daysRemaining < 0 -> {
            val daysAgo = -daysRemaining
            Quad(
                Color(0xFFFEE2E2),
                "منقضٍ ($daysAgo يوم)",
                Color(0xFFB91C1C),
                Icons.Default.EventBusy
            )
        }
        daysRemaining == 0L -> {
            Quad(
                Color(0xFFFEF3C7),
                "اليوم هو الميعاد!",
                Color(0xFFB45309),
                Icons.Default.Alarm
            )
        }
        daysRemaining in 1L..5L -> {
            Quad(
                Color(0xFFFFFBEB),
                "متبقي $daysRemaining أيام (عاجل)",
                Color(0xFFD97706),
                Icons.Default.HourglassBottom
            )
        }
        else -> {
            Quad(
                Color(0xFFECFDF5),
                "متبقي $daysRemaining يوماً",
                Color(0xFF047857),
                Icons.Default.HourglassTop
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = badgeBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun TimelineNodeItem(
    milestone: CalculationMilestone,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val dayName = DateFormatterUtils.getArabicDayName(milestone.date.dayOfWeek)
    val formattedDate = DateFormatterUtils.formatArabicFullDate(milestone.date)

    val (icon, nodeColor, isHighlighted) = when (milestone.type) {
        MilestoneType.TRIGGER_EVENT -> Triple(Icons.Default.Gavel, MaterialTheme.colorScheme.primary, false)
        MilestoneType.COUNTING_START -> Triple(Icons.Default.PlayArrow, MaterialTheme.colorScheme.secondary, false)
        MilestoneType.BASE_DEADLINE -> Triple(Icons.Default.CalendarMonth, MaterialTheme.colorScheme.tertiary, false)
        MilestoneType.DISTANCE_EXTENSION -> Triple(Icons.Default.Straighten, MaterialTheme.colorScheme.secondary, false)
        MilestoneType.HOLIDAY_EXTENSION -> Triple(Icons.Default.EventBusy, Color(0xFFD97706), true)
        MilestoneType.FINAL_DEADLINE -> Triple(Icons.Default.CheckCircle, Color(0xFF059669), true)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        // Timeline vertical indicator line and circle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(nodeColor.copy(alpha = if (isHighlighted) 0.22f else 0.12f))
                    .border(
                        1.5.dp,
                        nodeColor,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = nodeColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(38.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isHighlighted) nodeColor else MaterialTheme.colorScheme.onSurface
                )

                milestone.legalBasis?.let { basis ->
                    Text(
                        text = basis,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$dayName، $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Text(
                        text = milestone.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
