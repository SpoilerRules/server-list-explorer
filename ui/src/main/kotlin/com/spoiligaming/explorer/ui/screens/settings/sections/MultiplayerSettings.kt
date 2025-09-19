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
import com.spoiligaming.explorer.ui.widgets.DropdownOption
import com.spoiligaming.explorer.ui.widgets.ItemSelectableDropdownMenu
import com.spoiligaming.explorer.ui.widgets.ItemValueSlider

@Composable
internal fun MultiplayerSettings() {
    val mpSettings by multiplayerSettingsManager.settingsFlow.collectAsState()

    SettingsSection(
        header = "Multiplayer Server List",
        settings =
            listOf {
                /* ItemSwitch(
                       title = "Cache server status results",
                       description = "Save responses from MCServerPing to load your list faster and use less data when checking server status.",
                       isChecked = mpSettings.enableServerQueryCache,
                       onCheckedChange = { newValue ->
                           multiplayerSettingsManager.updateSettings {
                               it.copy(enableServerQueryCache = newValue)
                           }
                       },
                   )*/
                ItemValueSlider(
                    title = "Server entry scale",
                    description = "Change how wide server entries appear in the list.",
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
                    title = "Drag shake intensity (degrees)",
                    description = "Controls how much a server entry shakes when dragged.",
                    value = mpSettings.dragShakeIntensityDegrees,
                    valueRange = 0f..10f,
                    onValueChange = { newValue ->
                        multiplayerSettingsManager.updateSettings {
                            it.copy(dragShakeIntensityDegrees = newValue)
                        }
                    },
                )
                ItemValueSlider(
                    title = "Connection timeout (s)",
                    description = "Time to wait while connecting to a server.",
                    value = mpSettings.connectTimeoutMillis / 1000L,
                    valueRange = 1f..600f,
                    onValueChange = { newValueInSeconds ->
                        multiplayerSettingsManager.updateSettings {
                            it.copy(connectTimeoutMillis = newValueInSeconds * 1000)
                        }
                    },
                )

                ItemValueSlider(
                    title = "Socket timeout (s)",
                    description = "Time to wait for a server to send data.",
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
    val options =
        ActionBarOrientation.entries.map { orientation ->
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
                text = orientation.displayName,
                icon = icon,
                selectedIcon = selectedIcon,
            )
        }

    val selectedOption = options.first { it.text == current.displayName }

    ItemSelectableDropdownMenu(
        title = "Action bar orientation",
        description = "Set where the server list's action bar appears: Right, Top, Left, or Bottom.",
        selectedOption = selectedOption,
        options = options,
    ) { selected ->
        ActionBarOrientation.entries
            .firstOrNull { it.displayName == selected.text }
            ?.let(onSelected)
    }
}
