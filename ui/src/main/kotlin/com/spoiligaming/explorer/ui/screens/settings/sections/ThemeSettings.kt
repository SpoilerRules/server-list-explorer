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
import com.spoiligaming.explorer.settings.model.ThemePaletteStyle
import com.spoiligaming.explorer.ui.extensions.toComposeColor
import com.spoiligaming.explorer.ui.extensions.toHex
import com.spoiligaming.explorer.ui.screens.settings.components.SettingsSection
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.theme.MAX_CONTRAST_LEVEL
import com.spoiligaming.explorer.ui.theme.MIN_CONTRAST_LEVEL
import com.spoiligaming.explorer.ui.widgets.DropdownOption
import com.spoiligaming.explorer.ui.widgets.ItemColorPicker
import com.spoiligaming.explorer.ui.widgets.ItemSelectableDropdownMenu
import com.spoiligaming.explorer.ui.widgets.ItemSwitch
import com.spoiligaming.explorer.ui.widgets.ItemValueSlider
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.palette_style_content
import server_list_explorer.ui.generated.resources.palette_style_expressive
import server_list_explorer.ui.generated.resources.palette_style_fidelity
import server_list_explorer.ui.generated.resources.palette_style_fruit_salad
import server_list_explorer.ui.generated.resources.palette_style_monochrome
import server_list_explorer.ui.generated.resources.palette_style_neutral
import server_list_explorer.ui.generated.resources.palette_style_rainbow
import server_list_explorer.ui.generated.resources.palette_style_tonal_spot
import server_list_explorer.ui.generated.resources.palette_style_vibrant
import server_list_explorer.ui.generated.resources.setting_theme_amoled_mode
import server_list_explorer.ui.generated.resources.setting_theme_amoled_mode_desc
import server_list_explorer.ui.generated.resources.setting_theme_contrast_level
import server_list_explorer.ui.generated.resources.setting_theme_contrast_level_desc
import server_list_explorer.ui.generated.resources.setting_theme_mode
import server_list_explorer.ui.generated.resources.setting_theme_mode_desc
import server_list_explorer.ui.generated.resources.setting_theme_palette_style
import server_list_explorer.ui.generated.resources.setting_theme_palette_style_desc
import server_list_explorer.ui.generated.resources.setting_theme_seed_color
import server_list_explorer.ui.generated.resources.setting_theme_seed_color_desc
import server_list_explorer.ui.generated.resources.setting_theme_use_system_accent
import server_list_explorer.ui.generated.resources.setting_theme_use_system_accent_desc
import server_list_explorer.ui.generated.resources.settings_section_theme
import server_list_explorer.ui.generated.resources.theme_mode_dark
import server_list_explorer.ui.generated.resources.theme_mode_light
import server_list_explorer.ui.generated.resources.theme_mode_system_default

@Composable
internal fun ThemeSettings() {
    val themeSettings by themeSettingsManager.settingsFlow.collectAsState()

    SettingsSection(
        header = t(Res.string.settings_section_theme),
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
                    title = t(Res.string.setting_theme_seed_color),
                    description = t(Res.string.setting_theme_seed_color_desc),
                    currentColor = themeSettings.seedColor.toComposeColor(),
                    restoreButton = true,
                    onConfirm = { newColor ->
                        themeSettingsManager.updateSettings {
                            it.copy(seedColor = "#" + newColor.toHex())
                        }
                    },
                )
                PaletteStyleDropdown(
                    currentStyle = themeSettings.paletteStyle,
                    onStyleSelected = { newStyle ->
                        themeSettingsManager.updateSettings { current ->
                            current.copy(paletteStyle = newStyle)
                        }
                    },
                )
                ItemValueSlider(
                    title = t(Res.string.setting_theme_contrast_level),
                    description = t(Res.string.setting_theme_contrast_level_desc),
                    value = themeSettings.contrastLevel,
                    valueRange = MIN_CONTRAST_LEVEL.toFloat()..MAX_CONTRAST_LEVEL.toFloat(),
                    onValueChange = { newValue ->
                        themeSettingsManager.updateSettings { current ->
                            current.copy(contrastLevel = newValue.coerceIn(MIN_CONTRAST_LEVEL, MAX_CONTRAST_LEVEL))
                        }
                    },
                )
                ItemSwitch(
                    title = t(Res.string.setting_theme_amoled_mode),
                    description = t(Res.string.setting_theme_amoled_mode_desc),
                    isChecked = themeSettings.amoledMode,
                    onCheckedChange = { newValue ->
                        themeSettingsManager.updateSettings { current ->
                            current.copy(amoledMode = newValue)
                        }
                    },
                )
                ItemSwitch(
                    title = t(Res.string.setting_theme_use_system_accent),
                    description = t(Res.string.setting_theme_use_system_accent_desc),
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
    val modeToDisplayName =
        ThemeMode.entries.associateWith { mode ->
            when (mode) {
                ThemeMode.Light -> t(Res.string.theme_mode_light)
                ThemeMode.Dark -> t(Res.string.theme_mode_dark)
                ThemeMode.System -> t(Res.string.theme_mode_system_default)
            }
        }
    val displayNameToMode = modeToDisplayName.entries.associate { (k, v) -> v to k }

    val options =
        modeToDisplayName.map { (mode, displayName) ->
            val (icon, selectedIcon) =
                when (mode) {
                    ThemeMode.Light -> Icons.Outlined.LightMode to Icons.Filled.LightMode
                    ThemeMode.Dark -> Icons.Outlined.DarkMode to Icons.Filled.DarkMode
                    ThemeMode.System -> Icons.Outlined.Settings to Icons.Filled.Settings
                }
            DropdownOption(
                text = displayName,
                icon = icon,
                selectedIcon = selectedIcon,
            )
        }

    val selectedOption = options.first { it.text == modeToDisplayName[currentMode] }

    ItemSelectableDropdownMenu(
        title = t(Res.string.setting_theme_mode),
        description = t(Res.string.setting_theme_mode_desc),
        selectedOption = selectedOption,
        options = options,
    ) { selected ->
        displayNameToMode[selected.text]?.let(onModeSelected)
    }
}

@Composable
private fun PaletteStyleDropdown(
    currentStyle: ThemePaletteStyle,
    onStyleSelected: (ThemePaletteStyle) -> Unit,
) {
    val styleToDisplayName =
        ThemePaletteStyle.entries.associateWith { style ->
            when (style) {
                ThemePaletteStyle.TonalSpot -> t(Res.string.palette_style_tonal_spot)
                ThemePaletteStyle.Neutral -> t(Res.string.palette_style_neutral)
                ThemePaletteStyle.Vibrant -> t(Res.string.palette_style_vibrant)
                ThemePaletteStyle.Expressive -> t(Res.string.palette_style_expressive)
                ThemePaletteStyle.Rainbow -> t(Res.string.palette_style_rainbow)
                ThemePaletteStyle.FruitSalad -> t(Res.string.palette_style_fruit_salad)
                ThemePaletteStyle.Monochrome -> t(Res.string.palette_style_monochrome)
                ThemePaletteStyle.Fidelity -> t(Res.string.palette_style_fidelity)
                ThemePaletteStyle.Content -> t(Res.string.palette_style_content)
            }
        }
    val displayNameToStyle = styleToDisplayName.entries.associate { (k, v) -> v to k }

    val options =
        styleToDisplayName.map { (_, displayName) ->
            DropdownOption(
                text = displayName,
            )
        }

    val selectedOption = options.first { it.text == styleToDisplayName[currentStyle] }

    ItemSelectableDropdownMenu(
        title = t(Res.string.setting_theme_palette_style),
        description = t(Res.string.setting_theme_palette_style_desc),
        selectedOption = selectedOption,
        options = options,
    ) { selected ->
        displayNameToStyle[selected.text]?.let(onStyleSelected)
    }
}
