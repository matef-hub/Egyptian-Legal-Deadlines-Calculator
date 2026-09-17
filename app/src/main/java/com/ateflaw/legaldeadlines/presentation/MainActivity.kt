package com.ateflaw.legaldeadlines.presentation

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ateflaw.legaldeadlines.LegalDeadlinesApplication
import com.ateflaw.legaldeadlines.R
import com.ateflaw.legaldeadlines.presentation.navigation.AppNavHost
import com.ateflaw.legaldeadlines.presentation.navigation.Screen
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.CalculatorViewModel
import com.ateflaw.legaldeadlines.presentation.viewmodel.DeadlineListViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val appContainer = (application as LegalDeadlinesApplication).container

        val calculatorViewModel: CalculatorViewModel by viewModels {
            CalculatorViewModel.provideFactory(
                getLegalRulesUseCase = appContainer.getLegalRulesUseCase,
                getHolidaysUseCase = appContainer.getHolidaysUseCase,
                calculateDeadlineUseCase = appContainer.calculateDeadlineUseCase,
                saveDeadlineUseCase = appContainer.saveDeadlineUseCase,
                syncHolidaysUseCase = appContainer.syncHolidaysUseCase
            )
        }

        val deadlineListViewModel: DeadlineListViewModel by viewModels {
            DeadlineListViewModel.provideFactory(
                getSavedDeadlinesUseCase = appContainer.getSavedDeadlinesUseCase,
                deleteSavedDeadlineUseCase = appContainer.deleteSavedDeadlineUseCase
            )
        }

        setContent {
            EgyptianLegalDeadlinesTheme {
                val view = LocalView.current
                val isDarkTheme = isSystemInDarkTheme()
                if (!view.isInEditMode) {
                    LaunchedEffect(isDarkTheme) {
                        val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
                    }
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val isLanding = currentRoute == Screen.Landing.route

                val navigateTo: (String) -> Unit = { route ->
                    if (currentRoute != route) {
                        if (route == Screen.Calculator.route) {
                            navController.popBackStack(Screen.Calculator.route, inclusive = false)
                        } else {
                            navController.navigate(route) {
                                popUpTo(Screen.Calculator.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }

                BackHandler(enabled = currentRoute == Screen.SavedDeadlines.route) {
                    navigateTo(Screen.Calculator.route)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AnimatedVisibility(
                            visible = !isLanding,
                            enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                                    slideInVertically(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)) { -it },
                            exit = fadeOut(animationSpec = tween(durationMillis = 300)) +
                                   slideOutVertically(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)) { -it }
                        ) {
                            ModernTopAppBar(
                                currentRoute = currentRoute,
                                onToggleScreen = {
                                    val target = if (currentRoute != Screen.SavedDeadlines.route) {
                                        Screen.SavedDeadlines.route
                                    } else {
                                        Screen.Calculator.route
                                    }
                                    navigateTo(target)
                                }
                            )
                        }
                    },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = !isLanding,
                            enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                                    slideInVertically(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)) { it },
                            exit = fadeOut(animationSpec = tween(durationMillis = 300)) +
                                   slideOutVertically(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)) { it }
                        ) {
                            FloatingBottomNavigationBar(
                                currentRoute = currentRoute,
                                onNavigateTo = navigateTo
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AppNavHost(
                            navController = navController,
                            mainViewModel = calculatorViewModel,
                            deadlineListViewModel = deadlineListViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopAppBar(
    currentRoute: String?,
    onToggleScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = stringResource(R.string.nav_legal_deadlines_desc),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (currentRoute == Screen.SavedDeadlines.route) {
                                stringResource(R.string.nav_saved)
                            } else {
                                stringResource(R.string.nav_calculator)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            actions = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    IconButton(
                        onClick = onToggleScreen
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = stringResource(R.string.nav_toggle_desc),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            windowInsets = TopAppBarDefaults.windowInsets,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun FloatingBottomNavigationBar(
    currentRoute: String?,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(bottom = 16.dp, top = 8.dp)
            .padding(horizontal = 24.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isCalculatorSelected = currentRoute == Screen.Calculator.route || currentRoute == null

                FloatingNavItem(
                    selected = isCalculatorSelected,
                    onClick = { onNavigateTo(Screen.Calculator.route) },
                    icon = Icons.Default.Calculate,
                    label = stringResource(R.string.nav_calculator),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingNavItem(
                    selected = currentRoute == Screen.SavedDeadlines.route,
                    onClick = { onNavigateTo(Screen.SavedDeadlines.route) },
                    icon = Icons.Default.Bookmark,
                    label = stringResource(R.string.nav_saved),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RowScope.FloatingNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "NavItemContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "NavItemContentColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "NavItemIconScale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
private fun ModernTopAppBarPreview() {
    EgyptianLegalDeadlinesTheme {
        ModernTopAppBar(
            currentRoute = Screen.Calculator.route,
            onToggleScreen = {}
        )
    }
}

@Preview
@Composable
private fun FloatingBottomNavigationBarPreview() {
    EgyptianLegalDeadlinesTheme {
        FloatingBottomNavigationBar(
            currentRoute = Screen.Calculator.route,
            onNavigateTo = {}
        )
    }
}

@Preview
@Composable
private fun FloatingNavItemPreview() {
    EgyptianLegalDeadlinesTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingNavItem(
                    selected = true,
                    onClick = {},
                    icon = Icons.Default.Calculate,
                    label = "حاسبة المواعيد"
                )
                FloatingNavItem(
                    selected = false,
                    onClick = {},
                    icon = Icons.Default.Bookmark,
                    label = "المحفوظات"
                )
            }
        }
    }
}
