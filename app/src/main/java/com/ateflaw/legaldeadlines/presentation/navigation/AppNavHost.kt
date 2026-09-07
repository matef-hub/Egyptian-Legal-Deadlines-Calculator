package com.ateflaw.legaldeadlines.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ateflaw.legaldeadlines.presentation.screens.CalculatorScreen
import com.ateflaw.legaldeadlines.presentation.screens.LandingScreen
import com.ateflaw.legaldeadlines.presentation.screens.SavedDeadlinesScreen
import com.ateflaw.legaldeadlines.presentation.viewmodel.DeadlineListViewModel
import com.ateflaw.legaldeadlines.presentation.viewmodel.MainViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    deadlineListViewModel: DeadlineListViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Landing.route,
        modifier = modifier
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
