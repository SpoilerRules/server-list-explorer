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

import com.spoiligaming.explorer.settings.manager.windowStateSettingsManager
import com.spoiligaming.explorer.settings.model.WindowState
import java.awt.Component
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowEvent
import javax.swing.JFrame

internal object WindowSettingsBinder {
    private const val LISTENERS_INSTALLED_PROPERTY = "WindowSettingsBinder.ListenersInstalled"

    fun JFrame.bind() {
        if (rootPane.getClientProperty(LISTENERS_INSTALLED_PROPERTY) == true) {
            return
        }

        rootPane.putClientProperty(LISTENERS_INSTALLED_PROPERTY, true)
        listenMaximization()
        listenResizes()
    }

    fun JFrame.listenResizes() {
        addComponentListener(
            object : ComponentAdapter() {
                override fun componentResized(event: ComponentEvent) {
                    val frame = event.component.asJFrameOrNull() ?: return
                    if (frame.isIconified) {
                        return
                    }

                    val newSize = frame.size
                    if (!newSize.isPersistableWindowSize) {
                        return
                    }

                    windowStateSettingsManager.updateSettings { current ->
                        current.updatedForResize(
                            width = newSize.width,
                            height = newSize.height,
                            isMaximized = frame.isMaximized,
                        )
                    }
                }
            },
        )
    }

    fun JFrame.listenMaximization() {
        addWindowStateListener { event: WindowEvent ->
            val isMaximized = event.newState and Frame.MAXIMIZED_BOTH == Frame.MAXIMIZED_BOTH

            windowStateSettingsManager.updateSettings { current ->
                if (current.isWindowMaximized == isMaximized) {
                    current
                } else {
                    current.copy(isWindowMaximized = isMaximized)
                }
            }
        }
    }
}

private val JFrame.isMaximized
    get() = extendedState and Frame.MAXIMIZED_BOTH == Frame.MAXIMIZED_BOTH

private val JFrame.isIconified
    get() = extendedState and Frame.ICONIFIED == Frame.ICONIFIED

private val Dimension.isPersistableWindowSize
    get() = width > 0 && height > 0

private fun Component?.asJFrameOrNull() = this as? JFrame

private fun WindowState.updatedForResize(
    width: Int,
    height: Int,
    isMaximized: Boolean,
) = if (isMaximized) {
    if (currentWidth == width && currentHeight == height) {
        this
    } else {
        copy(
            currentWidth = width,
            currentHeight = height,
        )
    }
} else {
    if (
        this.width == width &&
        this.height == height &&
        currentWidth == width &&
        currentHeight == height
    ) {
        this
    } else {
        copy(
            width = width,
            height = height,
            currentWidth = width,
            currentHeight = height,
        )
    }
}
