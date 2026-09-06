package com.ateflaw.legaldeadlines.presentation.navigation

sealed class Screen(val route: String, val title: String) {
    data object Calculator : Screen("calculator", "حاسبة المواعيد")
    data object SavedDeadlines : Screen("saved_deadlines", "المواعيد المحفوظة")
}
