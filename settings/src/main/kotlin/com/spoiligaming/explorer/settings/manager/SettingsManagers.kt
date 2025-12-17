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

package com.spoiligaming.explorer.settings.manager

import com.spoiligaming.explorer.settings.model.MultiplayerSettings
import com.spoiligaming.explorer.settings.model.Preferences
import com.spoiligaming.explorer.settings.model.PrivacySettings
import com.spoiligaming.explorer.settings.model.ServerQueryMethodConfigurations
import com.spoiligaming.explorer.settings.model.SingleplayerSettings
import com.spoiligaming.explorer.settings.model.ThemeSettings
import com.spoiligaming.explorer.settings.model.WindowAppearance
import com.spoiligaming.explorer.settings.model.WindowState

/*
 * CONTRIBUTOR NOTICE:
 *
 * If you add, remove, or rename a UniversalSettingsManager or any settings model in this file,
 * you must also update com.spoiligaming.explorer.ui.SettingsCompositionLocals.kt
 * to keep the CompositionLocals in sync with these managers.
 *
 * Failing to do so will break global settings propagation in the UI.
 */

val windowStateSettingsManager by UniversalSettingsManager<WindowState>(
    fileName = "window_state.json",
    defaultValueProvider = { WindowState() },
)

val windowAppearanceSettingsManager by UniversalSettingsManager<WindowAppearance>(
    fileName = "window_appearance.json",
    defaultValueProvider = { WindowAppearance() },
)

val preferenceSettingsManager by UniversalSettingsManager<Preferences>(
    fileName = "preferences.json",
    defaultValueProvider = { Preferences() },
)

val privacySettingsManager by UniversalSettingsManager<PrivacySettings>(
    fileName = "privacy.json",
    defaultValueProvider = { PrivacySettings() },
)

val themeSettingsManager by UniversalSettingsManager<ThemeSettings>(
    fileName = "theme.json",
    defaultValueProvider = { ThemeSettings() },
)

val multiplayerSettingsManager by UniversalSettingsManager<MultiplayerSettings>(
    fileName = "multiplayer.json",
    defaultValueProvider = { MultiplayerSettings() },
)

val serverQueryMethodConfigurationsManager by UniversalSettingsManager<ServerQueryMethodConfigurations>(
    fileName = "server_query_method_configs.json",
    defaultValueProvider = { ServerQueryMethodConfigurations() },
)

val singleplayerSettingsManager by UniversalSettingsManager<SingleplayerSettings>(
    fileName = "singleplayer.json",
    defaultValueProvider = { SingleplayerSettings() },
)
