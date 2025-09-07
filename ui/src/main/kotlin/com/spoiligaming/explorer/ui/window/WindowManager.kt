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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalWindowState
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.ProvideAppSettings
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.Dimension

internal object WindowManager {
    private const val WINDOW_TITLE = "Server List Explorer"

    private const val MIN_WIDTH_PX = 850
    private const val MIN_HEIGHT_PX = 500
    private val MIN_WINDOW_SIZE = Dimension(MIN_WIDTH_PX, MIN_HEIGHT_PX)

    private val CompactWidthThresold = 1000.dp
    private val ShortHeightThresold = 640.dp

    private const val SKIKO_VSYNC_PROPERTY = "skiko.vsync.enabled"

    private val DefaultAlignment = Alignment.Center

    var isWindowCompact by mutableStateOf(false)
        private set
    var isWindowShort by mutableStateOf(false)
        private set

    fun launch(content: @Composable () -> Unit) =
        application {
            ProvideAppSettings {
                val ws = LocalWindowState.current
                val prefs = LocalPrefs.current

                LaunchedEffect(prefs.vsync) {
                    System.setProperty(SKIKO_VSYNC_PROPERTY, prefs.vsync.toString())
                }

                val windowPlacement =
                    if (ws.isWindowMaximized) WindowPlacement.Maximized else WindowPlacement.Floating

                val windowState =
                    rememberWindowState(
                        placement = windowPlacement,
                        width = ws.width.dp,
                        height = ws.height.dp,
                        position = WindowPosition.Aligned(DefaultAlignment),
                    )

                SideEffect {
                    isWindowCompact =
                        !ws.isWindowMaximized && ws.currentWidth.dp < CompactWidthThresold
                    isWindowShort =
                        !ws.isWindowMaximized && ws.currentHeight.dp < ShortHeightThresold
                }

                Window(
                    onCloseRequest = ::exitApplication,
                    visible = true,
                    title = WINDOW_TITLE,
                    state = windowState,
                ) {
                    if (hostOs == OS.Windows) {
                        WindowEffects(window).applyEffects()
                    }

                    LaunchedEffect(window) {
                        window.minimumSize = MIN_WINDOW_SIZE

                        with(WindowSettingsBinder) {
                            window.listenMaximization()
                            window.listenResizes()
                        }
                    }

                    content()
                }
            }
        }
}
