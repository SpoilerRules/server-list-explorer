package com.spoiligaming.explorer.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.SettingsViewModel
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.widgets.DropdownMenuWithLabel
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

@Composable
fun SettingsGeneral() =
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DropdownMenuWithLabel(
            label = "Scrollbar Visibility",
            currentValue = ConfigurationHandler.getInstance().generalSettings.scrollBarVisibility,
            options = listOf("Right Side", "Left Side", "Disabled"),
        ) { newValue ->
            ConfigurationHandler.updateValue { generalSettings.scrollBarVisibility = newValue }
        }

        DropdownMenuWithLabel(
            label = "Server List Control Panel Position",
            currentValue = ConfigurationHandler.getInstance().generalSettings.controlPanelPosition,
            options = listOf("Top", "Bottom"),
        ) { newValue ->
            ConfigurationHandler.updateValue { generalSettings.controlPanelPosition = newValue }
            SettingsViewModel.controlPanelPosition = newValue
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "A restart will be necessary for changes to the Render API to take effect.",
                color = MapleColorPalette.accent,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaLight,
                        fontWeight = FontWeight.Light,
                        fontSize = 15.sp,
                    ),
            )
            DropdownMenuWithLabel(
                label = "Render API",
                currentValue = ConfigurationHandler.getInstance().generalSettings.renderApi,
                options = getRenderApiOptions(),
            ) { newValue ->
                ConfigurationHandler.updateValue { generalSettings.renderApi = newValue }
            }
        }
    }

private fun getRenderApiOptions(): List<String> =
    mutableListOf("Unknown", "Software Fast", "Software Compat", "OpenGL").apply {
        if (hostOs == OS.MacOS) {
            add("Metal")
        } else if (hostOs == OS.Windows) {
            add("Direct3D")
        }
    }
