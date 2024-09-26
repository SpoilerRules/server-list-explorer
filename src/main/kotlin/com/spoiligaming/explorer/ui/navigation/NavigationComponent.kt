package com.spoiligaming.explorer.ui.navigation

import androidx.compose.runtime.Composable
import com.spoiligaming.explorer.ui.screens.home.FileBackupSubScreen
import com.spoiligaming.explorer.ui.screens.home.HomeScreen
import com.spoiligaming.explorer.ui.screens.settings.SettingsScreen

@Composable
fun NavigationComponent() =
    when (NavigationController.currentScreen) {
        Screen.Home -> HomeScreen()
        Screen.Settings -> SettingsScreen()
        Screen.FileBackupScreen -> FileBackupSubScreen()
    }
