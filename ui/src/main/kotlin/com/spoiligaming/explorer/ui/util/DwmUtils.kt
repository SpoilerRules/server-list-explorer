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

package com.spoiligaming.explorer.ui.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.spoiligaming.explorer.ui.snackbar.SnackbarController
import com.spoiligaming.explorer.ui.snackbar.SnackbarEvent
import com.spoiligaming.explorer.ui.t
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.snackbar_system_accent_unavailable

internal object DwmUtils {
    private const val DWM_REGISTRY_PATH = "Software\\Microsoft\\Windows\\DWM"
    private const val ACCENT_COLOR_VALUE = "AccentColor"

    fun getSystemAccentColorOrNull() =
        runCatching {
            if (Advapi32Util.registryValueExists(
                    WinReg.HKEY_CURRENT_USER,
                    DWM_REGISTRY_PATH,
                    ACCENT_COLOR_VALUE,
                )
            ) {
                val argbValue =
                    Advapi32Util.registryGetIntValue(
                        WinReg.HKEY_CURRENT_USER,
                        DWM_REGISTRY_PATH,
                        ACCENT_COLOR_VALUE,
                    )
                Color(
                    red = argbValue and 0xFF,
                    green = argbValue ushr 8 and 0xFF,
                    blue = argbValue ushr 16 and 0xFF,
                    alpha = argbValue ushr 24 and 0xFF,
                )
            } else {
                error("Registry value '$ACCENT_COLOR_VALUE' not found at '$DWM_REGISTRY_PATH'")
            }
        }.onFailure {
            logger.error(it) {
                "Unable to read Windows accent color from registry. " +
                    "Ensure '$ACCENT_COLOR_VALUE' exists at 'HKEY_CURRENT_USER\\$DWM_REGISTRY_PATH'."
            }
        }.getOrNull()
}

@Composable
internal fun rememberSystemAccentColor(
    useSystemAccentColor: Boolean,
    pollIntervalMillis: Long = 2_000,
): State<Color?> {
    var hasShownSnackbar by remember(useSystemAccentColor) {
        mutableStateOf(false)
    }

    val state =
        produceState<Color?>(initialValue = null, key1 = useSystemAccentColor) {
            if (!useSystemAccentColor) return@produceState

            value = DwmUtils.getSystemAccentColorOrNull()

            flow {
                while (true) {
                    emit(DwmUtils.getSystemAccentColorOrNull())
                    delay(pollIntervalMillis)
                }
            }.distinctUntilChanged()
                .collect { newColor -> value = newColor }
        }

    if (useSystemAccentColor && state.value != null) {
        hasShownSnackbar = false
    }

    val snackbarSystemAccentUnavailableText = t(Res.string.snackbar_system_accent_unavailable)
    LaunchedEffect(useSystemAccentColor, state.value) {
        if (useSystemAccentColor && state.value == null && !hasShownSnackbar) {
            hasShownSnackbar = true
            SnackbarController.sendEvent(
                SnackbarEvent(
                    message = snackbarSystemAccentUnavailableText,
                    duration = SnackbarDuration.Short,
                ),
            )
        }
    }

    return state
}

private val logger = KotlinLogging.logger {}
