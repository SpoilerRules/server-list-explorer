/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2026 SpoilerRules
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

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.spoiligaming.explorer.settings.manager.startupSettingsManager
import com.spoiligaming.explorer.settings.model.ComputerStartupBehavior
import com.spoiligaming.explorer.settings.util.AppStoragePaths
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalStartupSettings
import com.spoiligaming.explorer.ui.screens.settings.components.SettingsSection
import com.spoiligaming.explorer.ui.snackbar.SnackbarController
import com.spoiligaming.explorer.ui.snackbar.SnackbarEvent
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.util.displayNameResource
import com.spoiligaming.explorer.ui.widgets.DropdownOption
import com.spoiligaming.explorer.ui.widgets.ItemSelectableDropdownMenu
import com.spoiligaming.explorer.ui.widgets.ItemSwitch
import com.spoiligaming.explorer.util.ComputerStartupRegistrationManager
import com.spoiligaming.explorer.util.OSUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.settings_section_startup
import server_list_explorer.ui.generated.resources.startup_minimize_to_tray_on_close_description
import server_list_explorer.ui.generated.resources.startup_minimize_to_tray_on_close_title
import server_list_explorer.ui.generated.resources.startup_restore_previous_state_description_long
import server_list_explorer.ui.generated.resources.startup_restore_previous_state_note
import server_list_explorer.ui.generated.resources.startup_restore_previous_state_title
import server_list_explorer.ui.generated.resources.startup_save_failed
import server_list_explorer.ui.generated.resources.startup_single_instance_handling_description
import server_list_explorer.ui.generated.resources.startup_single_instance_handling_title
import server_list_explorer.ui.generated.resources.startup_when_computer_starts_dropdown_description
import server_list_explorer.ui.generated.resources.startup_when_computer_starts_dropdown_title

@Composable
internal fun StartupSettingsSection() {
    val startupSettings = LocalStartupSettings.current
    val scope = rememberCoroutineScope()
    val startupSaveFailedMessage = t(Res.string.startup_save_failed)

    SettingsSection(
        header = t(Res.string.settings_section_startup),
        settings =
            buildList {
                if ((OSUtils.isWindows || OSUtils.isDebian) &&
                    !OSUtils.isRunningOnBareJvm &&
                    !AppStoragePaths.isPortableInstall
                ) {
                    add {
                        ComputerStartupBehaviorDropdown(
                            currentMode = startupSettings.computerStartupBehavior,
                            onModeSelected = { newBehavior ->
                                if (newBehavior == startupSettings.computerStartupBehavior) {
                                    return@ComputerStartupBehaviorDropdown
                                }

                                scope.launch {
                                    ComputerStartupRegistrationManager
                                        .applyBehavior(newBehavior)
                                        .onSuccess {
                                            startupSettingsManager.updateSettings {
                                                it.copy(computerStartupBehavior = newBehavior)
                                            }
                                        }.onFailure { e ->
                                            logger.error(e) {
                                                "Failed to apply computer startup behavior: $newBehavior"
                                            }
                                            SnackbarController.sendEvent(
                                                SnackbarEvent(
                                                    message = startupSaveFailedMessage,
                                                    duration = SnackbarDuration.Short,
                                                ),
                                            )
                                        }
                                }
                            },
                        )
                    }
                }
                add {
                    ItemSwitch(
                        title = t(Res.string.startup_minimize_to_tray_on_close_title),
                        description = t(Res.string.startup_minimize_to_tray_on_close_description),
                        isChecked = startupSettings.minimizeToSystemTrayOnClose,
                        onCheckedChange = { newValue ->
                            startupSettingsManager.updateSettings {
                                it.copy(minimizeToSystemTrayOnClose = newValue)
                            }
                        },
                    )
                }
                add {
                    ItemSwitch(
                        title = t(Res.string.startup_single_instance_handling_title),
                        description = t(Res.string.startup_single_instance_handling_description),
                        isChecked = startupSettings.singleInstanceHandling,
                        onCheckedChange = { newValue ->
                            startupSettingsManager.updateSettings {
                                it.copy(singleInstanceHandling = newValue)
                            }
                        },
                    )
                }
                add {
                    ItemSwitch(
                        title = t(Res.string.startup_restore_previous_state_title),
                        description = t(Res.string.startup_restore_previous_state_description_long),
                        note = t(Res.string.startup_restore_previous_state_note),
                        isChecked = startupSettings.persistentSessionState,
                        onCheckedChange = { newValue ->
                            startupSettingsManager.updateSettings {
                                it.copy(persistentSessionState = newValue)
                            }
                        },
                    )
                }
            },
    )
}

@Composable
private fun ComputerStartupBehaviorDropdown(
    currentMode: ComputerStartupBehavior,
    onModeSelected: (ComputerStartupBehavior) -> Unit,
) {
    val startupModes = ComputerStartupBehavior.entries
    val options =
        startupModes.map { mode ->
            DropdownOption(text = t(mode.displayNameResource))
        }
    val selectedOption = options[startupModes.indexOf(currentMode)]

    ItemSelectableDropdownMenu(
        title = t(Res.string.startup_when_computer_starts_dropdown_title),
        description = t(Res.string.startup_when_computer_starts_dropdown_description),
        selectedOption = selectedOption,
        options = options,
    ) { selected ->
        startupModes.getOrNull(options.indexOf(selected))?.let(onModeSelected)
    }
}

private val logger = KotlinLogging.logger {}
