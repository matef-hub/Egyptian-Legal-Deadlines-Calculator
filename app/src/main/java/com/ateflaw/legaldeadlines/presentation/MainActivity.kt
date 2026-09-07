package com.ateflaw.legaldeadlines.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ateflaw.legaldeadlines.LegalDeadlinesApplication
import com.ateflaw.legaldeadlines.presentation.navigation.AppNavHost
import com.ateflaw.legaldeadlines.presentation.navigation.Screen
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.DeadlineListViewModel
import com.ateflaw.legaldeadlines.presentation.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as LegalDeadlinesApplication).container

        val mainViewModel: MainViewModel by viewModels {
            MainViewModel.provideFactory(
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
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "حاسبة المواعيد القانونية",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                Icon(
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == Screen.Calculator.route || currentRoute == null,
                                onClick = {
                                    if (currentRoute != Screen.Calculator.route) {
                                        navController.navigate(Screen.Calculator.route) {
                                            popUpTo(Screen.Calculator.route) { inclusive = true }
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.Calculate, contentDescription = "الحاسبة") },
                                label = { Text("الحاسبة") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )

                            NavigationBarItem(
                                selected = currentRoute == Screen.SavedDeadlines.route,
                                onClick = {
                                    if (currentRoute != Screen.SavedDeadlines.route) {
                                        navController.navigate(Screen.SavedDeadlines.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Default.Bookmark, contentDescription = "المواعيد المحفوظة") },
                                label = { Text("المحفوظات") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        mainViewModel = mainViewModel,
                        deadlineListViewModel = deadlineListViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
