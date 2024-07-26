package com.spoiligaming.explorer.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class Screen {
    data object Main : Screen()

    data object Settings : Screen()
}

object NavigationController {
    var currentScreen by mutableStateOf<Screen>(Screen.Main)
        private set

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }
}
