package com.spoiligaming.explorer.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class Screen {
    data object Home : Screen()

    data object Settings : Screen()

    data object FileBackupScreen : Screen()
}

object NavigationController {
    var currentScreen by mutableStateOf<Screen>(Screen.Home)
        private set

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }
}
