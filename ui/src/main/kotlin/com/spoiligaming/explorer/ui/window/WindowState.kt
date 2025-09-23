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

import com.spoiligaming.explorer.settings.manager.windowStateSettingsManager
import java.awt.Frame
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowEvent
import javax.swing.JFrame

internal object WindowSettingsBinder {
    @Suppress("NOTHING_TO_INLINE")
    inline fun JFrame.listenResizes() {
        addComponentListener(
            object : ComponentAdapter() {
                override fun componentResized(event: ComponentEvent) {
                    val frame = event.component as JFrame
                    // same surgical procedure as in listenMaximization
                    val isMaximized = frame.extendedState and Frame.MAXIMIZED_BOTH == Frame.MAXIMIZED_BOTH

                    frame.size.let { newSize ->
                        windowStateSettingsManager.updateSettings { current ->
                            if (isMaximized) {
                                current.copy(
                                    currentWidth = newSize.width,
                                    currentHeight = newSize.height,
                                )
                            } else {
                                current.copy(
                                    width = newSize.width,
                                    height = newSize.height,
                                    currentWidth = newSize.width,
                                    currentHeight = newSize.height,
                                )
                            }
                        }
                    }
                }
            },
        )
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun JFrame.listenMaximization() {
        addWindowStateListener { event: WindowEvent ->
            // check if both bits of MAXIMIZED_BOTH are set
            val isMaximized = event.newState and Frame.MAXIMIZED_BOTH == Frame.MAXIMIZED_BOTH

            windowStateSettingsManager.updateSettings {
                it.copy(
                    isWindowMaximized = isMaximized,
                )
            }
        }
    }
}
