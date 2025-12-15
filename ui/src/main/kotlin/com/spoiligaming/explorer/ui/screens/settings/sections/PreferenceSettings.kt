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
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.widgets.ItemLanguagePickerDropdownMenu
import com.spoiligaming.explorer.ui.widgets.ItemSwitch
import com.spoiligaming.explorer.ui.widgets.ItemValueSlider
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.setting_note_restart_required
import server_list_explorer.ui.generated.resources.setting_prefs_fps_overlay
import server_list_explorer.ui.generated.resources.setting_prefs_fps_overlay_desc
import server_list_explorer.ui.generated.resources.setting_prefs_highlight_after_scroll
import server_list_explorer.ui.generated.resources.setting_prefs_highlight_after_scroll_desc
import server_list_explorer.ui.generated.resources.setting_prefs_highlight_delay
import server_list_explorer.ui.generated.resources.setting_prefs_highlight_delay_desc
import server_list_explorer.ui.generated.resources.setting_prefs_highlight_delay_note
import server_list_explorer.ui.generated.resources.setting_prefs_language
import server_list_explorer.ui.generated.resources.setting_prefs_scroll_after_add
import server_list_explorer.ui.generated.resources.setting_prefs_scroll_after_add_desc
import server_list_explorer.ui.generated.resources.setting_prefs_settings_scrollbar_always_visible
import server_list_explorer.ui.generated.resources.setting_prefs_settings_scrollbar_always_visible_desc
import server_list_explorer.ui.generated.resources.setting_prefs_snackbar_at_top
import server_list_explorer.ui.generated.resources.setting_prefs_snackbar_at_top_desc
import server_list_explorer.ui.generated.resources.setting_prefs_undo_history_size
import server_list_explorer.ui.generated.resources.setting_prefs_undo_history_size_desc
import server_list_explorer.ui.generated.resources.setting_prefs_undo_redo_delay
import server_list_explorer.ui.generated.resources.setting_prefs_undo_redo_delay_desc
import server_list_explorer.ui.generated.resources.setting_prefs_undo_redo_interval
import server_list_explorer.ui.generated.resources.setting_prefs_undo_redo_interval_desc
import server_list_explorer.ui.generated.resources.setting_prefs_undo_redo_note
import server_list_explorer.ui.generated.resources.setting_prefs_vsync
import server_list_explorer.ui.generated.resources.setting_prefs_vsync_desc
import server_list_explorer.ui.generated.resources.setting_prefs_window_title_build_info
import server_list_explorer.ui.generated.resources.setting_prefs_window_title_build_info_desc
import server_list_explorer.ui.generated.resources.settings_section_preferences

@Composable
internal fun PreferenceSettings() {
    val prefs = LocalPrefs.current

    SettingsSection(
        header = t(Res.string.settings_section_preferences),
        settings =
            buildList {
                add {
                    ItemLanguagePickerDropdownMenu(
                        title = t(Res.string.setting_prefs_language),
                        selectedLocale = prefs.locale,
                        onLocaleSelected = { locale ->
                            preferenceSettingsManager.updateSettings {
                                it.copy(locale = locale)
                            }
                        },
                    )
                }
                add {
                    ItemSwitch(
                        title = t(Res.string.setting_prefs_snackbar_at_top),
                        description = t(Res.string.setting_prefs_snackbar_at_top_desc),
                        isChecked = prefs.snackbarAtTop,
                        onCheckedChange = { newValue ->
                            preferenceSettingsManager.updateSettings {
                                it.copy(snackbarAtTop = newValue)
                            }
                        },
                    )
                }
                add {
                    ItemSwitch(
                        title = t(Res.string.setting_prefs_settings_scrollbar_always_visible),
                        description = t(Res.string.setting_prefs_settings_scrollbar_always_visible_desc),
                        isChecked = prefs.settingsScrollbarAlwaysVisible,
                        onCheckedChange = { newValue ->
                            preferenceSettingsManager.updateSettings {
                                it.copy(settingsScrollbarAlwaysVisible = newValue)
                            }
                        },
                    )
                }
                add {
                    ItemValueSlider(
                        title = t(Res.string.setting_prefs_undo_history_size),
                        description = t(Res.string.setting_prefs_undo_history_size_desc),
                        value = prefs.maxUndoHistorySize,
                        valueRange = 0f..1000f,
                        onValueChange = { newSize ->
                            preferenceSettingsManager.updateSettings {
                                it.copy(maxUndoHistorySize = newSize)
                            }
                        },
                    )
                }
                add {
                    ItemValueSlider(
                        title = t(Res.string.setting_prefs_undo_redo_delay),
                        description = t(Res.string.setting_prefs_undo_redo_delay_desc),
                        note = t(Res.string.setting_prefs_undo_redo_note),
                        value = prefs.undoRedoRepeatInitialDelayMillis,
                        valueRange = 100f..1000f,
                        onValueChange = { newDelay ->
                            preferenceSettingsManager.updateSettings {
                                it.copy(undoRedoRepeatInitialDelayMillis = newDelay)
                            }
                        },
                    )
                }
                add {
                    ItemValueSlider(
                        title = t(Res.string.setting_prefs_undo_redo_interval),
                        description = t(Res.string.setting_prefs_undo_redo_interval_desc),
                        note = t(Res.string.setting_prefs_undo_redo_note),
                        value = prefs.undoRedoRepeatIntervalMillis,
                        valueRange = 20f..300f,
                        onValueChange = { newInterval ->
                            preferenceSettingsManager.updateSettings {
                                it.copy(undoRedoRepeatIntervalMillis = newInterval)
                            }
                        },
                    )
                }
                add {
                    ItemSwitch(
                        title = t(Res.string.setting_prefs_scroll_after_add),
                        description = t(Res.string.setting_prefs_scroll_after_add_desc),
                        isChecked = prefs.scrollAfterAdd,
                        onCheckedChange = { newValue ->
                            preferenceSettingsManager.updateSettings {
                                it.copy(scrollAfterAdd = newValue)
                            }
                        },
                    )
                }
                add {
                    ItemSwitch(
                        title = t(Res.string.setting_prefs_highlight_after_scroll),
                        description = t(Res.string.setting_prefs_highlight_after_scroll_desc),
                        isChecked = prefs.highlightAfterScroll,
                        onCheckedChange = { newValue ->
                            preferenceSettingsManager.updateSettings {
                                it.copy(highlightAfterScroll = newValue)
                            }
                        },
                    )
                }
                add {
                    ItemValueSlider(
                        title = t(Res.string.setting_prefs_highlight_delay),
                        description = t(Res.string.setting_prefs_highlight_delay_desc),
                        note = t(Res.string.setting_prefs_highlight_delay_note),
                        value = prefs.highlightAfterScrollDelayMillis,
                        valueRange = 0f..1000f,
                        onValueChange = { newMs ->
                            preferenceSettingsManager.updateSettings {
                                it.copy(highlightAfterScrollDelayMillis = newMs)
                            }
                        },
                    )
                }
                add {
                    ItemSwitch(
                        title = t(Res.string.setting_prefs_vsync),
                        description = t(Res.string.setting_prefs_vsync_desc),
                        note = t(Res.string.setting_note_restart_required),
                        isChecked = prefs.vsync,
                        onCheckedChange = { newValue ->
                            preferenceSettingsManager.updateSettings {
                                it.copy(vsync = newValue)
                            }
                        },
                    )
                }
                add {
                    ItemSwitch(
                        title = t(Res.string.setting_prefs_fps_overlay),
                        description = t(Res.string.setting_prefs_fps_overlay_desc),
                        isChecked = prefs.showFpsOverlay,
                        onCheckedChange = { newValue ->
                            preferenceSettingsManager.updateSettings {
                                it.copy(showFpsOverlay = newValue)
                            }
                        },
                    )
                }
                add {
                    ItemSwitch(
                        title = t(Res.string.setting_prefs_window_title_build_info),
                        description = t(Res.string.setting_prefs_window_title_build_info_desc),
                        isChecked = prefs.windowTitleShowBuildInfo,
                        onCheckedChange = { newValue ->
                            preferenceSettingsManager.updateSettings {
                                it.copy(windowTitleShowBuildInfo = newValue)
                            }
                        },
                    )
                }
            },
    )
}
