package com.spoiligaming.explorer.ui.screens.settings

import androidx.compose.runtime.Composable
import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.ui.SettingsViewModel
import com.spoiligaming.explorer.ui.widgets.LabeledMapleToggleSwitch

@Composable
fun SettingsAdvanced() {
    LabeledMapleToggleSwitch(
        title = "Active Server File Monitoring",
        currentValue = ConfigurationHandler.getInstance().themeSettings.shortcutsInContextMenu,
        onValueChange = { newValue ->
            ConfigurationHandler.updateValue { themeSettings.shortcutsInContextMenu = newValue }
            SettingsViewModel.displayShortcutsInContextMenu = newValue
        },
    )

    LabeledMapleToggleSwitch(
        title = "Interpret Server File as Compressed",
        currentValue = ConfigurationHandler.getInstance().themeSettings.iconifiedDialogOptions,
        onValueChange = { newValue ->
            ConfigurationHandler.updateValue { themeSettings.iconifiedDialogOptions = newValue }
            SettingsViewModel.experimentalIconifiedDialogOptions = newValue
        },
    )

    LabeledMapleToggleSwitch(
        title = "Compress Server File on Save",
        currentValue = ConfigurationHandler.getInstance().themeSettings.iconifiedDialogOptions,
        onValueChange = { newValue ->
            ConfigurationHandler.updateValue { themeSettings.iconifiedDialogOptions = newValue }
            SettingsViewModel.experimentalIconifiedDialogOptions = newValue
        },
    )
}
