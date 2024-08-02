package com.spoiligaming.explorer.ui.screens.settings

import androidx.compose.runtime.Composable
import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.ui.widgets.LabeledMapleToggleSwitch

@Composable
fun SettingsAdvanced() {
    LabeledMapleToggleSwitch(
        title = "Active Server File Monitoring",
        currentValue = ConfigurationHandler.getInstance().advancedSettings.serverFileMonitoring,
        onValueChange = { newValue ->
            ConfigurationHandler.updateValue { advancedSettings.serverFileMonitoring = newValue }
        },
    )

    LabeledMapleToggleSwitch(
        title = "Interpret Server File as Compressed",
        currentValue = ConfigurationHandler.getInstance().advancedSettings.isServerFileCompressed,
        onValueChange = { newValue ->
            ConfigurationHandler.updateValue { advancedSettings.isServerFileCompressed = newValue }
        },
    )

    LabeledMapleToggleSwitch(
        title = "Compress Server File on Save",
        currentValue = ConfigurationHandler.getInstance().advancedSettings.compressServerFile,
        onValueChange = { newValue ->
            ConfigurationHandler.updateValue { advancedSettings.compressServerFile = newValue }
        },
    )
}
