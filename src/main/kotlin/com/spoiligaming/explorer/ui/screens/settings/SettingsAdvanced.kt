package com.spoiligaming.explorer.ui.screens.settings

import androidx.compose.runtime.Composable
import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.ui.widgets.MapleToggleSwitch

@Composable
fun SettingsAdvanced() {
    MapleToggleSwitch(
        label = "Active Server File Monitoring",
        initialValue = ConfigurationHandler.getInstance().advancedSettings.serverFileMonitoring,
        onToggle = { newValue ->
            ConfigurationHandler.updateValue { advancedSettings.serverFileMonitoring = newValue }
        },
    )

    MapleToggleSwitch(
        label = "Interpret Server File as Compressed",
        initialValue = ConfigurationHandler.getInstance().advancedSettings.isServerFileCompressed,
        onToggle = { newValue ->
            ConfigurationHandler.updateValue { advancedSettings.isServerFileCompressed = newValue }
        },
    )

    MapleToggleSwitch(
        label = "Compress Server File on Save",
        initialValue = ConfigurationHandler.getInstance().advancedSettings.compressServerFile,
        onToggle = { newValue ->
            ConfigurationHandler.updateValue { advancedSettings.compressServerFile = newValue }
        },
    )
}
