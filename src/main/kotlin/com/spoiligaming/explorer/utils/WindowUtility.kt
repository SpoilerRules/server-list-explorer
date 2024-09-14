package com.spoiligaming.explorer.utils

import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.isWindowMaximized
import com.spoiligaming.explorer.windowFrame
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import javax.swing.JFrame

object WindowUtility {
    val windowScaleMapping =
        mapOf(
            "150%" to 1.5f,
            "125%" to 1.25f,
            "100%" to 1f,
        )

    private val toolkit = Toolkit.getDefaultToolkit()
    private val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
    private val defaultScreenDevice = graphicsEnvironment.defaultScreenDevice

    fun maximizeWindow() {
        isWindowMaximized = true

        toolkit.run {
            val screenInsets = getScreenInsets(defaultScreenDevice.defaultConfiguration)
            val screenSize = screenSize

            val usableWidth = screenSize.width - screenInsets.left - screenInsets.right
            val usableHeight = screenSize.height - screenInsets.top - screenInsets.bottom

            windowFrame.apply {
                size = Dimension(usableWidth, usableHeight)
                setLocation(screenInsets.left, screenInsets.top)
            }
        }
    }

    fun getUsableScreenSize() =
        toolkit.run {
            val screenInsets = getScreenInsets(defaultScreenDevice.defaultConfiguration)
            val screenSize = screenSize

            Pair(
                screenSize.width - screenInsets.left - screenInsets.right,
                screenSize.height - screenInsets.top - screenInsets.bottom,
            )
        }

    fun restoreWindowSize(restoreToResizable: Boolean) {
        val config = ConfigurationHandler.getInstance()
        val scale =
            windowScaleMapping[config.themeSettings.windowScale]
                ?: windowScaleMapping[config.windowProperties.previousScale]
                ?: config.themeSettings.windowScale.toFloatOrNull()
                ?: 1f

        val (width, height) =
            if (restoreToResizable) {
                ConfigurationHandler.updateValue {
                    themeSettings.windowScale = "Resizable"
                }
                config.windowProperties.currentWindowSize
                    ?: (800 * scale).toInt().let { defaultSize ->
                        ConfigurationHandler.updateValue {
                            windowProperties.currentWindowSize = defaultSize to defaultSize
                        }
                        defaultSize to defaultSize
                    }
            } else {
                ConfigurationHandler.updateValue {
                    themeSettings.windowScale = config.windowProperties.previousScale
                }
                (800 * scale).toInt() to (600 * scale).toInt()
            }

        windowFrame.apply {
            size = Dimension(width, height)
            centerOnScreen()
        }

        isWindowMaximized = false
    }

    fun JFrame.centerOnScreen() =
        toolkit.run {
            val screenSize = screenSize
            size.let { windowSize ->
                Dimension(
                    (screenSize.width - windowSize.width) / 2,
                    (screenSize.height - windowSize.height) / 2,
                ).let { centerLocation ->
                    setLocation(centerLocation.width, centerLocation.height)
                }
            }
        }
}
