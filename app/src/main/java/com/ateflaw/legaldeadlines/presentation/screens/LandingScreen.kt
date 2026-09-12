package com.ateflaw.legaldeadlines.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.ateflaw.legaldeadlines.R
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.StartRule
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun LandingScreen(
    viewModel: MainViewModel,
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
    var targetProgress by remember { mutableFloatStateOf(0.35f) }
    var currentStageText by remember { mutableStateOf("تحميل نصوص ومواعيد قانون المرافعات المصري...") }
    var isDataReady by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "LandingProgress"
    )

    // Monitor data readiness and animate stages
    LaunchedEffect(Unit) {
        // Stage 1
        targetProgress = 0.35f
        currentStageText = "تحميل نصوص ومواعيد قانون المرافعات المصري..."
        delay(350.milliseconds)

        // Stage 2
        val loadedRulesCount = if (rules.isNotEmpty()) rules.size else 20
        targetProgress = 0.70f
        currentStageText = "تم تحميل $loadedRulesCount ميعاداً إجرائياً، جاري فحص العطلات الرسمية..."
        delay(350.milliseconds)

        // Stage 3: Ready
        val holidaysCount = if (totalHolidaysCount > 0) totalHolidaysCount else 16
        targetProgress = 1.0f
        currentStageText = "اكتملت جاهزية المحرك الإجرائي ($holidaysCount عطلة معتمدة)"
        isDataReady = true
        delay(600.milliseconds)
        onNavigateToCalculator()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Legal Badge & Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Emblem
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "ميزان العدالة",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "حاسبة المواعيد القانونية",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "المساعد الإجرائي للسادة المحامين",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "وفقاً لأحكام قانون المرافعات المدنية والتجارية المصري",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Middle Section: Loading Status & Progress Bar
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
                                contentDescription = null,
                                tint = if (isDataReady) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDataReady) "جاهز للاستخدام" else "جاري تهيئة البيانات",
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(14.dp))

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
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "دخول الحاسبة القانونية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward, // RTL arrow points left
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "يعمل دون اتصال بالإنترنت • حساب تلقائي لعطلات الجمعة والأعياد الرسمية",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                        onClick = { uriHandler.openUri("https://www.linkedin.com/in/atef-law") },
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
