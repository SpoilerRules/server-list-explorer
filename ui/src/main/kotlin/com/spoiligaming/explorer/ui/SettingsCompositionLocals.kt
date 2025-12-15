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

package com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.spoiligaming.explorer.settings.manager.multiplayerSettingsManager
import com.spoiligaming.explorer.settings.manager.preferenceSettingsManager
import com.spoiligaming.explorer.settings.manager.privacySettingsManager
import com.spoiligaming.explorer.settings.manager.singleplayerSettingsManager
import com.spoiligaming.explorer.settings.manager.themeSettingsManager
import com.spoiligaming.explorer.settings.manager.windowAppearanceSettingsManager
import com.spoiligaming.explorer.settings.manager.windowStateSettingsManager
import com.spoiligaming.explorer.settings.model.MultiplayerSettings
import com.spoiligaming.explorer.settings.model.Preferences
import com.spoiligaming.explorer.settings.model.PrivacySettings
import com.spoiligaming.explorer.settings.model.SingleplayerSettings
import com.spoiligaming.explorer.settings.model.ThemeMode
import com.spoiligaming.explorer.settings.model.ThemeSettings
import com.spoiligaming.explorer.settings.model.WindowAppearance
import com.spoiligaming.explorer.settings.model.WindowState

internal val LocalPrefs =
    staticCompositionLocalOf<Preferences> {
        error("LocalPrefs not provided")
    }

internal val LocalPrivacySettings =
    staticCompositionLocalOf<PrivacySettings> {
        error("LocalPrivacySettings not provided")
    }

internal val LocalThemeSettings =
    staticCompositionLocalOf<ThemeSettings> {
        error("LocalThemeSettings not provided")
    }

internal val LocalWindowState =
    staticCompositionLocalOf<WindowState> {
        error("LocalWindowState not provided")
    }

internal val LocalWindowAppearance =
    staticCompositionLocalOf<WindowAppearance> {
        error("LocalWindowAppearance not provided")
    }

internal val LocalMultiplayerSettings =
    staticCompositionLocalOf<MultiplayerSettings> {
        error("LocalMultiplayerSettings not provided")
    }

internal val LocalSingleplayerSettings =
    staticCompositionLocalOf<SingleplayerSettings> {
        error("LocalSingleplayerSettings not provided")
    }
internal val LocalAmoledActive = compositionLocalOf { false }

@Composable
internal fun ProvideAppSettings(content: @Composable () -> Unit) {
    val prefs by preferenceSettingsManager.settingsFlow.collectAsState()
    val privacySettings by privacySettingsManager.settingsFlow.collectAsState()
    val themeSettings by themeSettingsManager.settingsFlow.collectAsState()
    val windowState by windowStateSettingsManager.settingsFlow.collectAsState()
    val windowAppearance by windowAppearanceSettingsManager.settingsFlow.collectAsState()
    val multiplayerSettings by multiplayerSettingsManager.settingsFlow.collectAsState()
    val singleplayerSettings by singleplayerSettingsManager.settingsFlow.collectAsState()

    val amoledOn = themeSettings.amoledMode && themeSettings.themeMode != ThemeMode.Light

    CompositionLocalProvider(
        LocalPrefs provides prefs,
        LocalPrivacySettings provides privacySettings,
        LocalThemeSettings provides themeSettings,
        LocalWindowState provides windowState,
        LocalWindowAppearance provides windowAppearance,
        LocalMultiplayerSettings provides multiplayerSettings,
        LocalSingleplayerSettings provides singleplayerSettings,
        LocalAmoledActive provides amoledOn,
    ) {
        content()
    }
}
