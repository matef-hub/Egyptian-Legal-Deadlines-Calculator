package com.ateflaw.legaldeadlines.presentation.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ateflaw.legaldeadlines.presentation.screens.CalculatorScreen
import com.ateflaw.legaldeadlines.presentation.screens.LandingScreen
import com.ateflaw.legaldeadlines.presentation.screens.SavedDeadlinesScreen
import com.ateflaw.legaldeadlines.presentation.viewmodel.CalculatorViewModel
import com.ateflaw.legaldeadlines.presentation.viewmodel.DeadlineListViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    mainViewModel: CalculatorViewModel,
    deadlineListViewModel: DeadlineListViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Landing.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) +
            slideInVertically(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) { 20 }
        },
        exitTransition = {
            fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) +
            slideOutVertically(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) { -20 }
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) +
            slideInVertically(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) { -20 }
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) +
            slideOutVertically(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) { 20 }
        }
    ) {
        composable(Screen.Landing.route) {
            LandingScreen(
                viewModel = mainViewModel,
                onNavigateToCalculator = {
                    navController.navigate(Screen.Calculator.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Calculator.route) {
            CalculatorScreen(viewModel = mainViewModel)
        }
        composable(Screen.SavedDeadlines.route) {
            SavedDeadlinesScreen(viewModel = deadlineListViewModel)
        }
    }
}
