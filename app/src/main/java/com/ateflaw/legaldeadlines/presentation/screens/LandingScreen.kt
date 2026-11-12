package com.ateflaw.legaldeadlines.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ateflaw.legaldeadlines.R
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.StartRule
import com.ateflaw.legaldeadlines.presentation.components.custom.DiamondDivider
import com.ateflaw.legaldeadlines.presentation.ui.theme.AntiqueBrass
import com.ateflaw.legaldeadlines.presentation.ui.theme.ArefRuqaaFontFamily
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.CalculatorViewModel

@Composable
fun LandingScreen(
    viewModel: CalculatorViewModel,
    onNavigateToCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LandingScreenContent(
        rules = uiState.rules,
        totalHolidaysCount = uiState.totalHolidaysCount,
        onNavigateToCalculator = onNavigateToCalculator,
        modifier = modifier
    )
}

@Composable
fun LandingScreenContent(
    rules: List<LegalRule>,
    totalHolidaysCount: Int,
    onNavigateToCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Readiness must reflect actual persisted rules, not simulated progress.
    val isDataReady = rules.isNotEmpty()
    val reviewedRuleCount = rules.count { it.reviewStatus == "VERIFIED_ACCURATE" && it.officialSourceUrl.isNotBlank() }
    val animatedProgress = if (isDataReady) 1.0f else 0.0f
    val currentStageText = if (isDataReady) {
        "تم تحميل ${rules.size} قاعدة، منها $reviewedRuleCount معتمدة؛ والعطلات المسجلة ${totalHolidaysCount}. لا تعتمد على الحساب قبل مراجعة القواعد والسند الرسمي."
    } else {
        "جارٍ تحميل القواعد القانونية من قاعدة البيانات..."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Legal Badge & Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Visual identity
                Image(
                    painter = painterResource(R.drawable.ic_meead_illustration),
                    contentDescription = "رسم توضيحي لملف قضائي وتقويم مواعيد",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
                Text(
                    text = "ميعاد",
                    fontFamily = ArefRuqaaFontFamily,
                    fontSize = 38.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "قانون  •  دقة  •  التزام",
                    style = MaterialTheme.typography.labelMedium,
                    color = AntiqueBrass,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "المساعد الإجرائي للسادة المحامين",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "وفقاً لأحكام القوانين المصرية",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            DiamondDivider(modifier = Modifier.padding(vertical = 2.dp))

            // Middle Section: Loading Status & Progress Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = "حالة تهيئة البيانات: ${if (isDataReady) "البيانات محملة وقيد التدقيق القانوني" else "جاري تهيئة البيانات"}"
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDataReady) Icons.Default.CheckCircle else Icons.Default.Security,
                                contentDescription = if (isDataReady) "التحميل اكتمل والاعتماد القانوني قيد المراجعة" else "حالة جاري التهيئة",
                                tint = if (isDataReady) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDataReady) "قيد التدقيق القانوني" else "جاري تهيئة البيانات",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isDataReady) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .semantics {
                                progressBarRangeInfo = ProgressBarRangeInfo(animatedProgress, 0f..1f)
                                contentDescription = "شريط تقدم التهيئة"
                            },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentStageText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom Section: Navigation Action & Guarantee Note
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = isDataReady,
                    enter = fadeIn() + scaleIn()
                ) {
                    Button(
                        onClick = onNavigateToCalculator,
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "استعراض الحاسبة القانونية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "يعمل دون اتصال بالإنترنت • تُعرض حالة توثيق العطلات قبل الاعتماد",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Social Links
                val uriHandler = LocalUriHandler.current
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // GitHub
                    SocialBrandButton(
                        painter = painterResource(R.drawable.ic_github),
                        brandColor = Color(0xFF24292E),
                        onClick = { uriHandler.openUri("https://github.com/matef-hub") },
                        contentDescription = "GitHub"
                    )

                    // LinkedIn
                    SocialBrandButton(
                        painter = painterResource(R.drawable.ic_linkedin),
                        brandColor = Color(0xFF0077B5),
                        onClick = { uriHandler.openUri("https://www.linkedin.com/in/moateflawyer/") },
                        contentDescription = "LinkedIn"
                    )

                    // Personal Web
                    SocialBrandButton(
                        painter = painterResource(R.drawable.ic_web),
                        brandColor = MaterialTheme.colorScheme.tertiary,
                        onClick = { uriHandler.openUri("https://ateflaw.com") },
                        contentDescription = "Website"
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialBrandButton(
    painter: Painter,
    brandColor: Color,
    onClick: () -> Unit,
    contentDescription: String
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = brandColor.copy(alpha = 0.08f),
        modifier = Modifier.size(48.dp),
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(28.dp),
                tint = Color.Unspecified // Keep original SVG colors
            )
        }
    }
}

@Preview(showBackground = true, name = "Landing Screen Loading Stage")
@Composable
fun LandingScreenLoadingPreview() {
    EgyptianLegalDeadlinesTheme {
        LandingScreenContent(
            rules = emptyList(),
            totalHolidaysCount = 0,
            onNavigateToCalculator = {}
        )
    }
}

@Preview(showBackground = true, name = "Landing Screen Ready Stage")
@Composable
fun LandingScreenReadyPreview() {
    EgyptianLegalDeadlinesTheme {
        LandingScreenContent(
            rules = listOf(
                LegalRule(
                    id = 1,
                    actionName = "الاستئناف",
                    duration = 40,
                    unit = DurationUnit.DAYS,
                    startRule = StartRule.NEXT_DAY
                )
            ),
            totalHolidaysCount = 10,
            onNavigateToCalculator = {}
        )
    }
}
