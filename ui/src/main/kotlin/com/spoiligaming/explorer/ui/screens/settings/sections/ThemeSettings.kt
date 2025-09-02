/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025 SpoilerRules
 *
 * Server List Explorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Server List Explorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Server List Explorer.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.spoiligaming.explorer.ui.screens.settings.sections

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.spoiligaming.explorer.settings.manager.themeSettingsManager
import com.spoiligaming.explorer.settings.model.ThemeMode
import com.spoiligaming.explorer.ui.extensions.toComposeColor
import com.spoiligaming.explorer.ui.extensions.toHex
import com.spoiligaming.explorer.ui.screens.settings.components.SettingsSection
import com.spoiligaming.explorer.ui.widgets.DropdownOption
import com.spoiligaming.explorer.ui.widgets.ItemColorPicker
import com.spoiligaming.explorer.ui.widgets.ItemSelectableDropdownMenu
import com.spoiligaming.explorer.ui.widgets.ItemSwitch

@Composable
internal fun ThemeSettings() {
    val themeSettings by themeSettingsManager.settingsFlow.collectAsState()

    SettingsSection(
        header = "Theme",
        settings =
            listOf {
                ThemeModeDropdown(
                    currentMode = themeSettings.themeMode,
                    onModeSelected = { newMode ->
                        themeSettingsManager.updateSettings {
                            it.copy(themeMode = newMode)
                        }
                    },
                )
                ItemColorPicker(
                    title = "Seed color",
                    description = "Pick the primary hue that drives the entire color theme. From this single color, the system algorithmically generates all related shades and accents for both light and dark modes.",
                    currentColor = themeSettings.seedColor.toComposeColor(),
                    restoreButton = true,
                    onConfirm = { newColor ->
                        themeSettingsManager.updateSettings {
                            it.copy(seedColor = "#" + newColor.toHex())
                        }
                    },
                )
                ItemSwitch(
                    title = "AMOLED mode",
                    description = "Enable pure-black backgrounds on OLED screens when dark mode is active.",
                    isChecked = themeSettings.amoledMode,
                    onCheckedChange = { newValue ->
                        themeSettingsManager.updateSettings { current ->
                            current.copy(amoledMode = newValue)
                        }
                    },
                )
                ItemSwitch(
                    title = "Use system accent color",
                    description =
                        "When enabled, the app will adopt your Windows accent color as the seed color. " +
                            "Toggle on to keep your app in sync with your OS accent.",
                    isChecked = themeSettings.useSystemAccentColor,
                    onCheckedChange = { newValue ->
                        themeSettingsManager.updateSettings { current ->
                            current.copy(useSystemAccentColor = newValue)
                        }
                    },
                )
            },
    )
}

@Composable
private fun ThemeModeDropdown(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
) {
    val options =
        ThemeMode.entries.map { mode ->
            val (icon, selectedIcon) =
                when (mode) {
                    ThemeMode.Light -> Icons.Outlined.LightMode to Icons.Filled.LightMode
                    ThemeMode.Dark -> Icons.Outlined.DarkMode to Icons.Filled.DarkMode
                    ThemeMode.System -> Icons.Outlined.Settings to Icons.Filled.Settings
                }
            DropdownOption(
                text = mode.displayName,
                icon = icon,
                selectedIcon = selectedIcon,
            )
        }

    val selectedOption = options.first { it.text == currentMode.displayName }

    ItemSelectableDropdownMenu(
        title = "Theme mode",
        description = "Choose Light for a bright theme, Dark for a darker look, or System Default to match your device's current setting.",
        selectedOption = selectedOption,
        options = options,
    ) { selectedOption ->
        ThemeMode.entries
            .firstOrNull { it.displayName == selectedOption.text }
            ?.let(onModeSelected)
    }
}
