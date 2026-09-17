package com.ateflaw.legaldeadlines.presentation

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

                val onNavigateTo: (String) -> Unit = { route ->
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Calculator.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }

                fun navigateTo(route: String) {
                    onNavigateTo(route)
                }

                BackHandler(enabled = currentRoute == Screen.SavedDeadlines.route) {
                    onNavigateTo(Screen.Calculator.route)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        AnimatedVisibility(
                            visible = !isLanding,
                            enter = fadeIn() + slideInVertically { -it },
                            exit = fadeOut() + slideOutVertically { -it }
                        ) {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = stringResource(R.string.app_name),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                navigationIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Gavel,
                                        contentDescription = stringResource(R.string.nav_legal_deadlines_desc),
                                        modifier = Modifier
                                            .padding(start = 12.dp, end = 8.dp)
                                            .size(24.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                },
                                actions = {
                                    IconButton(
                                        onClick = {
                                            val target = if (currentRoute != Screen.SavedDeadlines.route) {
                                                Screen.SavedDeadlines.route
                                            } else {
                                                Screen.Calculator.route
                                            }
                                            navigateTo(target)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                            contentDescription = stringResource(R.string.nav_toggle_desc),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = !isLanding,
                            enter = fadeIn() + slideInVertically { it },
                            exit = fadeOut() + slideOutVertically { it }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 8.dp,
                                    tonalElevation = 2.dp,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        mainViewModel = calculatorViewModel,
                        deadlineListViewModel = deadlineListViewModel,
                        modifier = Modifier.padding(
                            if (isLanding) PaddingValues() else innerPadding
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "FloatingNavItemBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "FloatingNavItemContentColor"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        contentColor = contentColor,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
