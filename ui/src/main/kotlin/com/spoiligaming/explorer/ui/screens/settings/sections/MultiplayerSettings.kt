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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.spoiligaming.explorer.settings.manager.multiplayerSettingsManager
import com.spoiligaming.explorer.settings.model.ActionBarOrientation
import com.spoiligaming.explorer.ui.screens.settings.components.SettingsSection
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.widgets.DropdownOption
import com.spoiligaming.explorer.ui.widgets.ItemSelectableDropdownMenu
import com.spoiligaming.explorer.ui.widgets.ItemValueSlider
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.action_bar_orientation_bottom
import server_list_explorer.ui.generated.resources.action_bar_orientation_left
import server_list_explorer.ui.generated.resources.action_bar_orientation_right
import server_list_explorer.ui.generated.resources.action_bar_orientation_top
import server_list_explorer.ui.generated.resources.setting_mp_action_bar_orientation
import server_list_explorer.ui.generated.resources.setting_mp_action_bar_orientation_desc
import server_list_explorer.ui.generated.resources.setting_mp_connection_timeout
import server_list_explorer.ui.generated.resources.setting_mp_connection_timeout_desc
import server_list_explorer.ui.generated.resources.setting_mp_drag_shake_intensity
import server_list_explorer.ui.generated.resources.setting_mp_drag_shake_intensity_desc
import server_list_explorer.ui.generated.resources.setting_mp_entry_scale
import server_list_explorer.ui.generated.resources.setting_mp_entry_scale_desc
import server_list_explorer.ui.generated.resources.setting_mp_socket_timeout
import server_list_explorer.ui.generated.resources.setting_mp_socket_timeout_desc
import server_list_explorer.ui.generated.resources.settings_section_multiplayer

@Composable
internal fun MultiplayerSettings() {
    val mpSettings by multiplayerSettingsManager.settingsFlow.collectAsState()

    SettingsSection(
        header = t(Res.string.settings_section_multiplayer),
        settings =
            listOf {
                /* ItemSwitch(
                       title = "Cache server status results",
                       description = "Save responses from MCUtils to load your list faster and use less data when checking server status.",
                       isChecked = mpSettings.enableServerQueryCache,
                       onCheckedChange = { newValue ->
                           multiplayerSettingsManager.updateSettings {
                               it.copy(enableServerQueryCache = newValue)
                           }
                       },
                   )*/
                ItemValueSlider(
                    title = t(Res.string.setting_mp_entry_scale),
                    description = t(Res.string.setting_mp_entry_scale_desc),
                    value = mpSettings.serverEntryScale,
                    valueRange = 1f..4f,
                    onValueChange = { newScale ->
                        multiplayerSettingsManager.updateSettings {
                            it.copy(serverEntryScale = newScale)
                        }
                    },
                )
                ActionBarOrientationDropdown(
                    current = mpSettings.actionBarOrientation,
                    onSelected = { newOrientation ->
                        multiplayerSettingsManager.updateSettings {
                            it.copy(actionBarOrientation = newOrientation)
                        }
                    },
                )
                ItemValueSlider(
                    title = t(Res.string.setting_mp_drag_shake_intensity),
                    description = t(Res.string.setting_mp_drag_shake_intensity_desc),
                    value = mpSettings.dragShakeIntensityDegrees,
                    valueRange = 0f..10f,
                    onValueChange = { newValue ->
                        multiplayerSettingsManager.updateSettings {
                            it.copy(dragShakeIntensityDegrees = newValue)
                        }
                    },
                )
                ItemValueSlider(
                    title = t(Res.string.setting_mp_connection_timeout),
                    description = t(Res.string.setting_mp_connection_timeout_desc),
                    value = mpSettings.connectTimeoutMillis / 1000L,
                    valueRange = 1f..600f,
                    onValueChange = { newValueInSeconds ->
                        multiplayerSettingsManager.updateSettings {
                            it.copy(connectTimeoutMillis = newValueInSeconds * 1000)
                        }
                    },
                )

                ItemValueSlider(
                    title = t(Res.string.setting_mp_socket_timeout),
                    description = t(Res.string.setting_mp_socket_timeout_desc),
                    value = mpSettings.socketTimeoutMillis / 1000L,
                    valueRange = 1f..60f,
                    onValueChange = { newValueInSeconds ->
                        multiplayerSettingsManager.updateSettings {
                            it.copy(socketTimeoutMillis = newValueInSeconds * 1000)
                        }
                    },
                )
            },
    )
}

@Composable
private fun ActionBarOrientationDropdown(
    current: ActionBarOrientation,
    onSelected: (ActionBarOrientation) -> Unit,
) {
    val orientationToDisplayName =
        ActionBarOrientation.entries.associateWith { orientation ->
            when (orientation) {
                ActionBarOrientation.Right -> t(Res.string.action_bar_orientation_right)
                ActionBarOrientation.Top -> t(Res.string.action_bar_orientation_top)
                ActionBarOrientation.Left -> t(Res.string.action_bar_orientation_left)
                ActionBarOrientation.Bottom -> t(Res.string.action_bar_orientation_bottom)
            }
        }
    val displayNameToOrientation = orientationToDisplayName.entries.associate { (k, v) -> v to k }

    val options =
        orientationToDisplayName.map { (orientation, displayName) ->
            val (icon, selectedIcon) =
                when (orientation) {
                    ActionBarOrientation.Right ->
                        Icons.AutoMirrored.Outlined.ArrowForward to Icons.AutoMirrored.Filled.ArrowForward
                    ActionBarOrientation.Left ->
                        Icons.AutoMirrored.Outlined.ArrowBack to Icons.AutoMirrored.Filled.ArrowBack
                    ActionBarOrientation.Top ->
                        Icons.Outlined.ArrowUpward to Icons.Filled.ArrowUpward
                    ActionBarOrientation.Bottom ->
                        Icons.Outlined.ArrowDownward to Icons.Filled.ArrowDownward
                }
            DropdownOption(
                text = displayName,
                icon = icon,
                selectedIcon = selectedIcon,
            )
        }

    val selectedOption = options.first { it.text == orientationToDisplayName[current] }

    ItemSelectableDropdownMenu(
        title = t(Res.string.setting_mp_action_bar_orientation),
        description = t(Res.string.setting_mp_action_bar_orientation_desc),
        selectedOption = selectedOption,
        options = options,
    ) { selected ->
        displayNameToOrientation[selected.text]?.let(onSelected)
    }
}
