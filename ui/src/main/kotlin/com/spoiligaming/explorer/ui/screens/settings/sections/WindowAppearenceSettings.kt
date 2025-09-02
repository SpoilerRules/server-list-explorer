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
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RoundedCorner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.spoiligaming.explorer.settings.manager.windowAppearanceSettingsManager
import com.spoiligaming.explorer.settings.model.TitleBarColorMode
import com.spoiligaming.explorer.settings.model.WindowCornerPreferenceSetting
import com.spoiligaming.explorer.ui.extensions.toComposeColor
import com.spoiligaming.explorer.ui.extensions.toHex
import com.spoiligaming.explorer.ui.screens.settings.components.SettingsSection
import com.spoiligaming.explorer.ui.widgets.DropdownOption
import com.spoiligaming.explorer.ui.widgets.ItemColorPicker
import com.spoiligaming.explorer.ui.widgets.ItemSelectableDropdownMenu
import com.spoiligaming.explorer.ui.widgets.ItemSwitch

@Composable
internal fun WindowAppearenceSettings() {
    val windowAppearence by windowAppearanceSettingsManager.settingsFlow.collectAsState()

    SettingsSection(
        header = "Window Appearance & Effects",
        settings =
            listOf {
                TitleBarColorModeDropdown(
                    currentMode = windowAppearence.titleBarColorMode,
                    onModeSelected = { newMode ->
                        windowAppearanceSettingsManager.updateSettings {
                            it.copy(titleBarColorMode = newMode)
                        }
                    },
                )
                ItemColorPicker(
                    "Title bar color",
                    "Choose a custom color for the title bar.",
                    "Only applies when title bar color mode is set to Custom.",
                    currentColor = windowAppearence.customTitleBarColor.toComposeColor(),
                    restoreButton = false,
                    onConfirm = { newColor ->
                        windowAppearanceSettingsManager.updateSettings {
                            it.copy(customTitleBarColor = "#" + newColor.toHex())
                        }
                    },
                )
                ItemSwitch(
                    title = "Custom window border color",
                    description = "Enable to apply your selected border color instead of the automatically derived one.",
                    isChecked = windowAppearence.useCustomBorderColor,
                    onCheckedChange = { newValue ->
                        windowAppearanceSettingsManager.updateSettings {
                            it.copy(useCustomBorderColor = newValue)
                        }
                    },
                )
                ItemColorPicker(
                    title = "Border color",
                    description = "Select a custom window border color.",
                    note = "Only applies when custom border color is enabled.",
                    currentColor = windowAppearence.customBorderColor.toComposeColor(),
                    restoreButton = false,
                    onConfirm = { newColor ->
                        windowAppearanceSettingsManager.updateSettings {
                            it.copy(customBorderColor = "#" + newColor.toHex())
                        }
                    },
                )
                CornerPreferenceDropdown(
                    currentMode = windowAppearence.windowCornerPreference,
                    onModeSelected = { newMode ->
                        windowAppearanceSettingsManager.updateSettings {
                            it.copy(windowCornerPreference = newMode)
                        }
                    },
                )
            },
    )
}

@Composable
private fun TitleBarColorModeDropdown(
    currentMode: TitleBarColorMode,
    onModeSelected: (TitleBarColorMode) -> Unit,
) {
    val options =
        TitleBarColorMode.entries.map { mode ->
            val (icon, selectedIcon) =
                when (mode) {
                    TitleBarColorMode.AUTO -> Icons.Outlined.Settings to Icons.Filled.Settings
                    TitleBarColorMode.MANUAL -> Icons.Outlined.Palette to Icons.Filled.Palette
                }
            DropdownOption(
                text = mode.displayName,
                icon = icon,
                selectedIcon = selectedIcon,
            )
        }

    val selectedOption = options.first { it.text == currentMode.displayName }

    ItemSelectableDropdownMenu(
        title = "Title bar color mode",
        description = "Select how your title bar's color is determined.",
        selectedOption = selectedOption,
        options = options,
    ) { selectedOption ->
        TitleBarColorMode.entries
            .firstOrNull { it.displayName == selectedOption.text }
            ?.let(onModeSelected)
    }
}

@Composable
private fun CornerPreferenceDropdown(
    currentMode: WindowCornerPreferenceSetting,
    onModeSelected: (WindowCornerPreferenceSetting) -> Unit,
) {
    val options =
        WindowCornerPreferenceSetting.entries.map { mode ->
            val (icon, selectedIcon) =
                when (mode) {
                    WindowCornerPreferenceSetting.SYSTEM_DEFAULT -> Icons.Outlined.Settings to Icons.Filled.Settings
                    WindowCornerPreferenceSetting.ROUNDED -> Icons.Outlined.RoundedCorner to Icons.Filled.RoundedCorner
                    WindowCornerPreferenceSetting.ELEVATED_SQUARE -> Icons.Outlined.Layers to Icons.Filled.Layers
                    WindowCornerPreferenceSetting.FLAT_SQUARE -> Icons.Outlined.CropSquare to Icons.Filled.CropSquare
                }
            DropdownOption(
                text = mode.displayName,
                icon = icon,
                selectedIcon = selectedIcon,
            )
        }

    val selectedOption = options.first { it.text == currentMode.displayName }

    ItemSelectableDropdownMenu(
        title = "Window corners",
        description = "Choose how window corners should appear. Rounded corners give a modern look, while square corners are more traditional.",
        note = "Rounded corners require Windows 11 (build 22000 or later).",
        selectedOption = selectedOption,
        options = options,
    ) { selectedOption ->
        WindowCornerPreferenceSetting.entries
            .firstOrNull { it.displayName == selectedOption.text }
            ?.let(onModeSelected)
    }
}
