package com.ateflaw.legaldeadlines.presentation

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ateflaw.legaldeadlines.LegalDeadlinesApplication
import com.ateflaw.legaldeadlines.presentation.components.FloatingBottomNavigationBar
import com.ateflaw.legaldeadlines.presentation.components.ModernTopAppBar
import com.ateflaw.legaldeadlines.presentation.navigation.AppNavHost
import com.ateflaw.legaldeadlines.presentation.navigation.Screen
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.CalculatorViewModel
import com.ateflaw.legaldeadlines.presentation.viewmodel.DeadlineListViewModel
import com.ateflaw.legaldeadlines.presentation.viewmodel.HolidaysViewModel
import com.ateflaw.legaldeadlines.presentation.viewmodel.RuleCatalogViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splash ->
                val rise = 24f * resources.displayMetrics.density
                ObjectAnimator.ofPropertyValuesHolder(
                    splash.view,
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -rise),
                    PropertyValuesHolder.ofFloat(View.ALPHA, 0f)
                ).apply {
                    duration = 180L
                    doOnEnd { splash.remove() }
                    start()
                }
            }
        }

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

        val ruleCatalogViewModel: RuleCatalogViewModel by viewModels {
            RuleCatalogViewModel.factory(appContainer.legalRuleRepository)
        }

        setContent {
            EgyptianLegalDeadlinesTheme {
                LifecycleResumeEffect(calculatorViewModel) {
                    calculatorViewModel.refreshTodayStatus()
                    onPauseOrDispose { }
                }
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

                BackHandler(
                    enabled = currentRoute == Screen.SavedDeadlines.route ||
                            currentRoute == Screen.Holidays.route ||
                            currentRoute == Screen.RulesCatalog.route ||
                            currentRoute == Screen.BackupRestore.route
                ) {
                    if (currentRoute == Screen.BackupRestore.route) {
                        navController.popBackStack()
                    } else {
                        navigateTo(Screen.Calculator.route)
                    }
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
                                        Screen.SavedDeadlines.route -> Screen.RulesCatalog.route
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            0.0f to Color.Transparent,
                                            0.35f to MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                                            0.65f to MaterialTheme.colorScheme.background,
                                            1.0f to MaterialTheme.colorScheme.background
                                        )
                                    )
                            ) {
                                FloatingBottomNavigationBar(
                                    currentRoute = currentRoute,
                                    onNavigateTo = navigateTo
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AppNavHost(
                            navController = navController,
                            mainViewModel = calculatorViewModel,
                            deadlineListViewModel = deadlineListViewModel,
                            holidaysViewModel = holidaysViewModel,
                            ruleCatalogViewModel = ruleCatalogViewModel,
                            innerPadding = innerPadding,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
