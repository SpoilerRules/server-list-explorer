package com.spoiligaming.explorer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.spoiligaming.explorer.ui.screens.home.HomeScreen
import com.spoiligaming.explorer.ui.screens.settings.SettingsScreen

@Composable
fun NavigationComponent() {
    val controller = remember { NavigationController }

    when (controller.currentScreen) {
        is Screen.Main -> HomeScreen()
        is Screen.Settings -> SettingsScreen()
    }
}
