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
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.widgets.DropdownOption
import com.spoiligaming.explorer.ui.widgets.ItemColorPicker
import com.spoiligaming.explorer.ui.widgets.ItemSelectableDropdownMenu
import com.spoiligaming.explorer.ui.widgets.ItemSwitch
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.setting_window_border_color
import server_list_explorer.ui.generated.resources.setting_window_border_color_desc
import server_list_explorer.ui.generated.resources.setting_window_border_color_note
import server_list_explorer.ui.generated.resources.setting_window_corners
import server_list_explorer.ui.generated.resources.setting_window_corners_desc
import server_list_explorer.ui.generated.resources.setting_window_corners_note
import server_list_explorer.ui.generated.resources.setting_window_custom_border_color
import server_list_explorer.ui.generated.resources.setting_window_custom_border_color_desc
import server_list_explorer.ui.generated.resources.setting_window_title_bar_color
import server_list_explorer.ui.generated.resources.setting_window_title_bar_color_desc
import server_list_explorer.ui.generated.resources.setting_window_title_bar_color_mode
import server_list_explorer.ui.generated.resources.setting_window_title_bar_color_mode_desc
import server_list_explorer.ui.generated.resources.setting_window_title_bar_color_note
import server_list_explorer.ui.generated.resources.settings_section_window_appearance
import server_list_explorer.ui.generated.resources.title_bar_color_mode_auto
import server_list_explorer.ui.generated.resources.title_bar_color_mode_manual
import server_list_explorer.ui.generated.resources.window_corner_preference_elevated_square
import server_list_explorer.ui.generated.resources.window_corner_preference_flat_square
import server_list_explorer.ui.generated.resources.window_corner_preference_rounded
import server_list_explorer.ui.generated.resources.window_corner_preference_system_default

@Composable
internal fun WindowAppearenceSettings() {
    val windowAppearence by windowAppearanceSettingsManager.settingsFlow.collectAsState()

    SettingsSection(
        header = t(Res.string.settings_section_window_appearance),
        settings =
            buildList {
                add {
                    TitleBarColorModeDropdown(
                        currentMode = windowAppearence.titleBarColorMode,
                        onModeSelected = { newMode ->
                            windowAppearanceSettingsManager.updateSettings {
                                it.copy(titleBarColorMode = newMode)
                            }
                        },
                    )
                }
                add {
                    ItemColorPicker(
                        t(Res.string.setting_window_title_bar_color),
                        t(Res.string.setting_window_title_bar_color_desc),
                        t(Res.string.setting_window_title_bar_color_note),
                        currentColor = windowAppearence.customTitleBarColor.toComposeColor(),
                        restoreButton = false,
                        onConfirm = { newColor ->
                            windowAppearanceSettingsManager.updateSettings {
                                it.copy(customTitleBarColor = "#" + newColor.toHex())
                            }
                        },
                    )
                }
                add {
                    ItemSwitch(
                        title = t(Res.string.setting_window_custom_border_color),
                        description = t(Res.string.setting_window_custom_border_color_desc),
                        isChecked = windowAppearence.useCustomBorderColor,
                        onCheckedChange = { newValue ->
                            windowAppearanceSettingsManager.updateSettings {
                                it.copy(useCustomBorderColor = newValue)
                            }
                        },
                    )
                }
                add {
                    ItemColorPicker(
                        title = t(Res.string.setting_window_border_color),
                        description = t(Res.string.setting_window_border_color_desc),
                        note = t(Res.string.setting_window_border_color_note),
                        currentColor = windowAppearence.customBorderColor.toComposeColor(),
                        restoreButton = false,
                        onConfirm = { newColor ->
                            windowAppearanceSettingsManager.updateSettings {
                                it.copy(customBorderColor = "#" + newColor.toHex())
                            }
                        },
                    )
                }
                add {
                    CornerPreferenceDropdown(
                        currentMode = windowAppearence.windowCornerPreference,
                        onModeSelected = { newMode ->
                            windowAppearanceSettingsManager.updateSettings {
                                it.copy(windowCornerPreference = newMode)
                            }
                        },
                    )
                }
            },
    )
}

@Composable
private fun TitleBarColorModeDropdown(
    currentMode: TitleBarColorMode,
    onModeSelected: (TitleBarColorMode) -> Unit,
) {
    val modeToDisplayName =
        TitleBarColorMode.entries.associateWith { mode ->
            when (mode) {
                TitleBarColorMode.AUTO -> t(Res.string.title_bar_color_mode_auto)
                TitleBarColorMode.MANUAL -> t(Res.string.title_bar_color_mode_manual)
            }
        }
    val displayNameToMode = modeToDisplayName.entries.associate { (k, v) -> v to k }

    val options =
        modeToDisplayName.map { (mode, displayName) ->
            val (icon, selectedIcon) =
                when (mode) {
                    TitleBarColorMode.AUTO -> Icons.Outlined.Settings to Icons.Filled.Settings
                    TitleBarColorMode.MANUAL -> Icons.Outlined.Palette to Icons.Filled.Palette
                }
            DropdownOption(
                text = displayName,
                icon = icon,
                selectedIcon = selectedIcon,
            )
        }

    val selectedOption = options.first { it.text == modeToDisplayName[currentMode] }

    ItemSelectableDropdownMenu(
        title = t(Res.string.setting_window_title_bar_color_mode),
        description = t(Res.string.setting_window_title_bar_color_mode_desc),
        selectedOption = selectedOption,
        options = options,
    ) { selected ->
        displayNameToMode[selected.text]?.let(onModeSelected)
    }
}

@Composable
private fun CornerPreferenceDropdown(
    currentMode: WindowCornerPreferenceSetting,
    onModeSelected: (WindowCornerPreferenceSetting) -> Unit,
) {
    val modeToDisplayName =
        WindowCornerPreferenceSetting.entries.associateWith { mode ->
            when (mode) {
                WindowCornerPreferenceSetting.SYSTEM_DEFAULT -> t(Res.string.window_corner_preference_system_default)
                WindowCornerPreferenceSetting.ROUNDED -> t(Res.string.window_corner_preference_rounded)
                WindowCornerPreferenceSetting.ELEVATED_SQUARE -> t(Res.string.window_corner_preference_elevated_square)
                WindowCornerPreferenceSetting.FLAT_SQUARE -> t(Res.string.window_corner_preference_flat_square)
            }
        }
    val displayNameToMode = modeToDisplayName.entries.associate { (k, v) -> v to k }

    val options =
        modeToDisplayName.map { (mode, displayName) ->
            val (icon, selectedIcon) =
                when (mode) {
                    WindowCornerPreferenceSetting.SYSTEM_DEFAULT -> Icons.Outlined.Settings to Icons.Filled.Settings
                    WindowCornerPreferenceSetting.ROUNDED -> Icons.Outlined.RoundedCorner to Icons.Filled.RoundedCorner
                    WindowCornerPreferenceSetting.ELEVATED_SQUARE -> Icons.Outlined.Layers to Icons.Filled.Layers
                    WindowCornerPreferenceSetting.FLAT_SQUARE -> Icons.Outlined.CropSquare to Icons.Filled.CropSquare
                }
            DropdownOption(
                text = displayName,
                icon = icon,
                selectedIcon = selectedIcon,
            )
        }

    val selectedOption = options.first { it.text == modeToDisplayName[currentMode] }

    ItemSelectableDropdownMenu(
        title = t(Res.string.setting_window_corners),
        description = t(Res.string.setting_window_corners_desc),
        note = t(Res.string.setting_window_corners_note),
        selectedOption = selectedOption,
        options = options,
    ) { selected ->
        displayNameToMode[selected.text]?.let(onModeSelected)
    }
}
