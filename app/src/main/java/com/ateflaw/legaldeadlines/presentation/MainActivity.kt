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
import androidx.compose.material.icons.filled.EventAvailable
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
import com.ateflaw.legaldeadlines.presentation.components.FloatingBottomNavigationBar
import com.ateflaw.legaldeadlines.presentation.components.ModernTopAppBar
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.CalculatorViewModel
import com.ateflaw.legaldeadlines.presentation.viewmodel.DeadlineListViewModel
import com.ateflaw.legaldeadlines.presentation.viewmodel.HolidaysViewModel

class MainActivity : ComponentActivity() {

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

        val holidaysViewModel: HolidaysViewModel by viewModels {
            HolidaysViewModel.provideFactory(
                getHolidaysUseCase = appContainer.getHolidaysUseCase,
                syncHolidaysUseCase = appContainer.syncHolidaysUseCase
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

                BackHandler(enabled = currentRoute == Screen.SavedDeadlines.route || currentRoute == Screen.Holidays.route) {
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
                                    val target = when (currentRoute) {
                                        Screen.Calculator.route -> Screen.Holidays.route
                                        Screen.Holidays.route -> Screen.SavedDeadlines.route
                                        else -> Screen.Calculator.route
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
                            holidaysViewModel = holidaysViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
