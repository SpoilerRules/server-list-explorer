package com.spoiligaming.explorer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.spoiligaming.explorer.ConfigurationHandler

object SettingsViewModel {
    private val configurationHandler = ConfigurationHandler.getInstance()

    var scrollbarVisibility by
        mutableStateOf(configurationHandler.generalSettings.scrollBarVisibility)
    var displayShortcutsInContextMenu by
        mutableStateOf(configurationHandler.themeSettings.shortcutsInContextMenu)
    var experimentalIconifiedDialogOptions by
        mutableStateOf(configurationHandler.themeSettings.iconifiedDialogOptions)
}
