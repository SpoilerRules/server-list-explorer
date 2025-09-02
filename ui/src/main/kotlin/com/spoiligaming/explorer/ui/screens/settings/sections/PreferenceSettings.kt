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

import androidx.compose.runtime.Composable
import com.spoiligaming.explorer.settings.manager.preferenceSettingsManager
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.screens.settings.components.SettingsSection
import com.spoiligaming.explorer.ui.widgets.ItemLanguagePickerDropdownMenu
import com.spoiligaming.explorer.ui.widgets.ItemSwitch
import com.spoiligaming.explorer.ui.widgets.ItemValueSlider

@Composable
internal fun PreferenceSettings() {
    val prefs = LocalPrefs.current

    SettingsSection(
        header = "Preferences",
        settings =
            listOf {
                ItemLanguagePickerDropdownMenu(
                    title = "Language",
                    selectedLocale = prefs.locale,
                    onLocaleSelected = { locale ->
                        preferenceSettingsManager.updateSettings {
                            it.copy(locale = locale)
                        }
                    },
                )
                ItemSwitch(
                    title = "Show snackbar at top",
                    description = "Display snackbar notifications at the top of the screen instead of the bottom.",
                    isChecked = prefs.snackbarAtTop,
                    onCheckedChange = { newValue ->
                        preferenceSettingsManager.updateSettings {
                            it.copy(snackbarAtTop = newValue)
                        }
                    },
                )
                ItemValueSlider(
                    title = "Undo history size",
                    description =
                        "The maximum number of changes that can be undone. " +
                            "Higher values allow you to undo more steps but require more memory.",
                    value = prefs.maxUndoHistorySize,
                    valueRange = 0f..1000f,
                    onValueChange = { newSize ->
                        preferenceSettingsManager.updateSettings {
                            it.copy(maxUndoHistorySize = newSize)
                        }
                    },
                )
                ItemValueSlider(
                    title = "Undo/Redo shortcut repeat delay (ms)",
                    description =
                        "Defines how long the app waits before starting to repeat Undo or Redo actions " +
                            "when you hold down their keyboard shortcuts. " +
                            "Lower values make repeated actions begin sooner.",
                    note = UNDO_REDO_SHORTCUT_NOTE,
                    value = prefs.undoRedoRepeatInitialDelayMillis,
                    valueRange = 100f..1000f,
                    onValueChange = { newDelay ->
                        preferenceSettingsManager.updateSettings {
                            it.copy(undoRedoRepeatInitialDelayMillis = newDelay)
                        }
                    },
                )
                ItemValueSlider(
                    title = "Undo/Redo shortcut repeat interval (ms)",
                    description =
                        "Controls the speed of repeated Undo or Redo actions while holding their " +
                            "keyboard shortcuts. " +
                            "Lower values make the actions repeat more quickly.",
                    note = UNDO_REDO_SHORTCUT_NOTE,
                    value = prefs.undoRedoRepeatIntervalMillis,
                    valueRange = 20f..300f,
                    onValueChange = { newInterval ->
                        preferenceSettingsManager.updateSettings {
                            it.copy(undoRedoRepeatIntervalMillis = newInterval)
                        }
                    },
                )
                ItemSwitch(
                    title = "Enable scroll after adding",
                    description = "Automatically scroll to a newly added multiplayer server or singleplayer world.",
                    isChecked = prefs.scrollAfterAdd,
                    onCheckedChange = { newValue ->
                        preferenceSettingsManager.updateSettings {
                            it.copy(scrollAfterAdd = newValue)
                        }
                    },
                )
                ItemSwitch(
                    title = "Highlight after scrolling to new entry",
                    description = "Highlight a newly added multiplayer server or singleplayer world once it's scrolled into view.",
                    isChecked = prefs.highlightAfterScroll,
                    onCheckedChange = { newValue ->
                        preferenceSettingsManager.updateSettings {
                            it.copy(highlightAfterScroll = newValue)
                        }
                    },
                )
                ItemValueSlider(
                    title = "Highlight delay after scroll (ms)",
                    description = "How long to wait before highlighting a newly added multiplayer server or singleplayer world after scrolling to it.",
                    note = "Only applies when highlighting after scrolling to new entry is enabled.",
                    value = prefs.highlightAfterScrollDelayMillis,
                    valueRange = 0f..1000f,
                    onValueChange = { newMs ->
                        preferenceSettingsManager.updateSettings {
                            it.copy(highlightAfterScrollDelayMillis = newMs)
                        }
                    },
                )
                ItemSwitch(
                    title = "VSync",
                    description = "Limits frame rate to save system resources, but may reduce UI smoothness.",
                    note = "Changes will apply after you restart the app.",
                    isChecked = prefs.vsync,
                    onCheckedChange = { newValue ->
                        preferenceSettingsManager.updateSettings {
                            it.copy(vsync = newValue)
                        }
                    },
                )
                ItemSwitch(
                    title = "Show FPS Overlay",
                    description = "Displays the current frames per second to help with debugging and benchmarking.",
                    isChecked = prefs.showFpsOverlay,
                    onCheckedChange = { newValue ->
                        preferenceSettingsManager.updateSettings {
                            it.copy(showFpsOverlay = newValue)
                        }
                    },
                )
            },
    )
}

private const val UNDO_REDO_SHORTCUT_NOTE =
    "Supported shortcuts:\n" +
        "• Undo: Ctrl+Z (Windows/Linux), Cmd+Z (macOS)\n" +
        "• Redo: Ctrl+Shift+Z or Ctrl+Y (Windows/Linux), Cmd+Shift+Z (macOS)"
