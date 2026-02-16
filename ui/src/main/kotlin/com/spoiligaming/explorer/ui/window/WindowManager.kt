/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025-2026 SpoilerRules
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.spoiligaming.explorer.build.BuildConfig
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalStartupSettings
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalWindowState
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.ProvideAppSettings
import com.spoiligaming.explorer.ui.systemtray.AppSystemTray
import com.spoiligaming.explorer.util.AppActivationSignal
import com.spoiligaming.explorer.util.FirstRunManager
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.Dimension
import java.awt.Frame

internal object WindowManager {
    private const val WINDOW_TITLE = "Server List Explorer"
    private const val BUILD_SUFFIX = " ${BuildConfig.VERSION} - ${BuildConfig.DISTRIBUTION}"
    private const val WINDOW_TITLE_WITH_BUILD = WINDOW_TITLE + BUILD_SUFFIX

    private const val MIN_WIDTH_PX = 850
    private const val MIN_HEIGHT_PX = 520
    private val MIN_WINDOW_SIZE = Dimension(MIN_WIDTH_PX, MIN_HEIGHT_PX)

    private val CompactWidthThreshold = 1000.dp
    private val ShortHeightThreshold = 640.dp

    private const val SKIKO_VSYNC_PROPERTY = "skiko.vsync.enabled"

    private val DefaultAlignment = Alignment.Center

    var isWindowCompact by mutableStateOf(false)
        private set
    var isWindowShort by mutableStateOf(false)
        private set

    fun launch(
        isAutoStartupLaunch: Boolean,
        content: @Composable () -> Unit,
    ) = application {
        var shouldFocusWindow by remember { mutableStateOf(false) }

        ProvideAppSettings {
            val ws = LocalWindowState.current
            val prefs = LocalPrefs.current
            val startupSettings = LocalStartupSettings.current
            val isFirstRun by FirstRunManager.isFirstRun.collectAsState()
            var isWindowVisible by
                remember(isAutoStartupLaunch, startupSettings.shouldStartMinimizedToSystemTray) {
                    mutableStateOf(
                        !(isAutoStartupLaunch && startupSettings.shouldStartMinimizedToSystemTray),
                    )
                }
            var shouldPrimeWindowComposition by
                remember(
                    isAutoStartupLaunch,
                    startupSettings.isSystemTrayFeatureEnabled,
                    startupSettings.shouldStartMinimizedToSystemTray,
                ) {
                    mutableStateOf(
                        isAutoStartupLaunch &&
                            startupSettings.isSystemTrayFeatureEnabled &&
                            startupSettings.shouldStartMinimizedToSystemTray,
                    )
                }

            LaunchedEffect(prefs.vsync) {
                System.setProperty(SKIKO_VSYNC_PROPERTY, prefs.vsync.toString())
            }

            LaunchedEffect(Unit) {
                AppActivationSignal.events.collect {
                    isWindowVisible = true
                    shouldFocusWindow = true
                }
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
                    !ws.isWindowMaximized &&
                    ws.currentWidth.dp < CompactWidthThreshold
                isWindowShort =
                    !ws.isWindowMaximized &&
                    ws.currentHeight.dp < ShortHeightThreshold
            }

            AppSystemTray(
                isSystemTrayFeatureEnabled = startupSettings.isSystemTrayFeatureEnabled,
                shouldMinimizeToSystemTrayOnClose = startupSettings.minimizeToSystemTrayOnClose,
                isWindowVisible = isWindowVisible,
                tooltip = WINDOW_TITLE,
                onHide = {
                    isWindowVisible = false
                },
                onOpen = {
                    isWindowVisible = true
                    shouldFocusWindow = true
                },
                onExit = {
                    exitApplication()
                },
            )

            val shouldKeepWindowComposed =
                startupSettings.persistentSessionState && startupSettings.isSystemTrayFeatureEnabled
            val shouldComposeWindow =
                isWindowVisible || shouldKeepWindowComposed || shouldPrimeWindowComposition

            if (shouldComposeWindow) {
                val windowTitle =
                    if (prefs.windowTitleShowBuildInfo) WINDOW_TITLE_WITH_BUILD else WINDOW_TITLE

                Window(
                    onCloseRequest = {
                        if (startupSettings.minimizeToSystemTrayOnClose && isFirstRun.not()) {
                            isWindowVisible = false
                        } else {
                            exitApplication()
                        }
                    },
                    visible = isWindowVisible,
                    title = windowTitle,
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

                    LaunchedEffect(isWindowVisible) {
                        if (isWindowVisible) {
                            shouldPrimeWindowComposition = false
                        }
                    }

                    LaunchedEffect(window, isWindowVisible, shouldFocusWindow) {
                        if (isWindowVisible && shouldFocusWindow) {
                            shouldFocusWindow = false
                            runCatching {
                                if (window.extendedState == Frame.ICONIFIED) {
                                    window.extendedState = Frame.NORMAL
                                }
                                window.toFront()
                                window.requestFocus()
                                window.requestFocusInWindow()
                            }.onFailure { e ->
                                logger.error(e) { "Window activation request failed during tray restore." }
                            }
                        }
                    }

                    content()
                }
            }
        }
    }
}

private val logger = KotlinLogging.logger {}
