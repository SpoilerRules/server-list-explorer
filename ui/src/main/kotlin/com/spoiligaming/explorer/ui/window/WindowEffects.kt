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

package com.spoiligaming.explorer.ui.window

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.spoiligaming.explorer.settings.manager.windowAppearanceSettingsManager
import com.spoiligaming.explorer.settings.model.TitleBarColorMode
import com.spoiligaming.explorer.ui.extensions.toComposeColor
import com.spoiligaming.explorer.ui.theme.AppTheme
import com.spoiligaming.explorer.ui.window.dwm.dwmStyler
import com.spoiligaming.explorer.util.OSUtils
import kotlinx.coroutines.delay
import javax.swing.JFrame

internal class WindowEffects(private val window: JFrame) {
    companion object {
        private const val DEBOUNCE_MILLIS = 25L
    }

    @Composable
    fun applyEffects() =
        AppTheme {
            val windowAppearance by windowAppearanceSettingsManager.settingsFlow.collectAsState()

            val background = MaterialTheme.colorScheme.surface
            val outline = MaterialTheme.colorScheme.outlineVariant
            val windowCornerPreference = windowAppearance.windowCornerPreference.dwmValue

            val debouncedBackground by rememberDebouncedState(background, DEBOUNCE_MILLIS)
            val debouncedOutline by rememberDebouncedState(outline, DEBOUNCE_MILLIS)

            window.dwmStyler().apply {
                LaunchedEffect(
                    windowAppearance.titleBarColorMode,
                    windowAppearance.customTitleBarColor,
                    debouncedBackground,
                ) {
                    when (windowAppearance.titleBarColorMode) {
                        TitleBarColorMode.AUTO -> {
                            setCaptionColor(debouncedBackground)
                        }

                        TitleBarColorMode.MANUAL -> {
                            setCaptionColor(windowAppearance.customTitleBarColor.toComposeColor())
                        }
                    }
                }
                LaunchedEffect(
                    windowAppearance.useCustomBorderColor,
                    windowAppearance.customBorderColor,
                    debouncedOutline,
                ) {
                    if (windowAppearance.useCustomBorderColor) {
                        setBorderColor(windowAppearance.customBorderColor.toComposeColor())
                    } else {
                        setBorderColor(debouncedOutline)
                    }
                }
                LaunchedEffect(windowCornerPreference) {
                    if (OSUtils.supportsDwmCornerPreference) {
                        setCornerPreference(windowCornerPreference)
                    }
                }
            }
        }
}

@Composable
private fun <T> rememberDebouncedState(
    value: T,
    debounceMillis: Long,
): State<T> {
    var debouncedValue by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        delay(debounceMillis)
        debouncedValue = value
    }

    return rememberUpdatedState(debouncedValue)
}
