package com.spoiligaming.explorer.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    Column {
        DropdownMenuWithLabel(
            label = "Scrollbar Visibility for Server List",
            currentValue = ConfigurationHandler.getInstance().generalSettings.scrollBarVisibility,
            options = listOf("Right Side", "Left Side", "Disabled"),
        ) { newValue ->
            ConfigurationHandler.updateValue { generalSettings.scrollBarVisibility = newValue }
            SettingsViewModel.scrollbarVisibility = newValue
        }

        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(10.dp))

        DropdownMenuWithLabel(
            label = "Render API",
            currentValue = ConfigurationHandler.getInstance().generalSettings.renderApi,
            options = getRenderApiOptions(),
        ) { newValue ->
            ConfigurationHandler.updateValue { generalSettings.renderApi = newValue }
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
