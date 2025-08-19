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

package com.spoiligaming.explorer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.jthemedetecor.OsThemeDetector
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicMaterialThemeState
import com.spoiligaming.explorer.settings.manager.themeSettingsManager
import com.spoiligaming.explorer.settings.model.ThemeMode
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalThemeSettings
import com.spoiligaming.explorer.ui.extensions.toComposeColor
import com.spoiligaming.explorer.ui.util.rememberSystemAccentColor

internal val LocalDarkTheme = staticCompositionLocalOf { false }
internal var isDarkTheme by mutableStateOf(false)
    private set
internal var isSystemDarkTheme by mutableStateOf(OsThemeDetector.getDetector().isDark)
    private set

@Composable
private fun rememberThemeState(): Pair<State<Boolean>, Boolean> {
    val themeSettings by themeSettingsManager.settingsFlow.collectAsState()
    val themeMode = themeSettings.themeMode

    val osDetector = remember { OsThemeDetector.getDetector() }
    var systemDark by remember { mutableStateOf(osDetector.isDark) }

    DisposableEffect(themeMode) {
        val listener: (Boolean) -> Unit = { nowIsDark ->
            systemDark = nowIsDark
            isSystemDarkTheme = nowIsDark
        }

        if (themeMode == ThemeMode.System) {
            osDetector.registerListener(listener)
        }

        onDispose {
            if (themeMode == ThemeMode.System) {
                osDetector.removeListener(listener)
            }
        }
    }

    val appIsDark =
        remember(themeMode, systemDark) {
            derivedStateOf {
                when (themeMode) {
                    ThemeMode.Light -> false
                    ThemeMode.Dark -> true
                    ThemeMode.System -> systemDark
                }
            }
        }

    return appIsDark to systemDark
}

@Composable
internal fun AppTheme(content: @Composable () -> Unit) {
    val themeSettings = LocalThemeSettings.current

    val (darkThemeState, currentSystemDarkTheme) = rememberThemeState()
    val currentAppDarkTheme = darkThemeState.value

    LaunchedEffect(currentAppDarkTheme) {
        isDarkTheme = currentAppDarkTheme
    }
    LaunchedEffect(currentSystemDarkTheme) {
        isSystemDarkTheme = currentSystemDarkTheme
    }

    val explicitSeedColor = themeSettings.seedColor.toComposeColor()
    val systemAccentState = rememberSystemAccentColor(themeSettings.useSystemAccentColor)
    val systemAccentColor = systemAccentState.value

    val chosenSeedColor =
        remember(
            themeSettings.useSystemAccentColor,
            explicitSeedColor,
            systemAccentColor,
        ) {
            if (themeSettings.useSystemAccentColor && systemAccentColor != null) {
                systemAccentColor
            } else {
                explicitSeedColor
            }
        }

    val dynamicThemeState =
        rememberDynamicMaterialThemeState(
            isAmoled = themeSettings.amoledMode,
            isDark = currentAppDarkTheme,
            style = PaletteStyle.TonalSpot,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
            seedColor = chosenSeedColor,
        )

    CompositionLocalProvider(LocalDarkTheme provides currentAppDarkTheme) {
        DynamicMaterialTheme(
            state = dynamicThemeState,
            animate = true,
            content = content,
        )
    }
}
