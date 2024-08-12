package com.spoiligaming.explorer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.state.DialogController
import com.spoiligaming.logging.Logger
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostOs
import java.awt.Point
import java.awt.Toolkit
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.RoundRectangle2D
import javax.swing.JFrame
import javax.swing.SwingUtilities

val controlButtonAsString: String =
    when (hostOs) {
        OS.MacOS -> "Cmd"
        else -> "Control"
    }

lateinit var windowFrame: JFrame
lateinit var windowSize: Pair<Dp, Dp>

var disableIconIndexing = false
var disableServerInfoIndexing = false

/**
 * Entry point for the Server List Explorer application.
 *
 * This function initializes the application, sets up the rendering API, processes command-line
 * arguments, and launches the main application window using Compose Multiplatform for Desktop.
 *
 * @param args Command-line arguments passed to the application. Supported arguments:
 *     - `--no-index-icon`: Disables loading icons for indexed servers.
 *     - `--no-index-serverinfo`: Disables loading the server list.
 */
fun main(args: Array<String>) {
    System.setProperty(
        "skiko.renderApi",
        ConfigurationHandler.getInstance().generalSettings.renderApi.uppercase().replace(" ", "_"),
    )
    Logger.printSuccess("Skiko Render API: ${SkiaLayer().renderApi}")

    val argumentActions =
        mapOf(
            "--no-index-icon" to
                {
                    disableIconIndexing = true
                    Logger.printSuccess(
                        "Icons of indexed servers will not be loaded (--no-index-icon used).",
                    )
                },
            "--no-index-serverinfo" to
                {
                    disableServerInfoIndexing = true
                    Logger.printSuccess(
                        "Server list will not be loaded (--no-index-serverinfo used).",
                    )
                },
        )

    args.forEach { argumentActions[it]?.invoke() }

    application {
        val windowScale by remember {
            mutableStateOf(
                ConfigurationHandler.getInstance().themeSettings.windowScale.run {
                    when (this) {
                        "150%" -> 1.5f
                        "125%" -> 1.25f
                        "100%" -> 1f
                        else ->
                            toFloatOrNull()
                                ?: 1f.also {
                                    Logger.printWarning(
                                        "Invalid window scale value. Falling back to 100%.",
                                    )
                                }
                    }
                },
            )
        }

        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val windowWidth = (800 * windowScale).toInt().dp
        val windowHeight = (600 * windowScale).toInt().dp
        val state = rememberWindowState(width = windowWidth, height = windowHeight)

        Window(
            onCloseRequest = ::exitApplication,
            visible = true,
            title = "Server List Explorer",
            resizable = false,
            undecorated = true,
            state = state,
        ) {
            val initialClick = remember { Point() }
            var isDragging = remember { false }

            SwingUtilities.invokeLater {
                window.setLocation(
                    (screenSize.width - windowWidth.value.toInt()) / 2,
                    (screenSize.height - windowHeight.value.toInt()) / 2,
                )
                window.shape =
                    RoundRectangle2D.Double(
                        0.0, 0.0, window.width.toDouble(), window.height.toDouble(), 30.0, 30.0,
                    )

                window.addMouseListener(
                    object : MouseAdapter() {
                        override fun mousePressed(event: MouseEvent) {
                            if (event.point.y < 58) {
                                initialClick.location = event.point
                                isDragging = true
                            }
                        }

                        override fun mouseReleased(event: MouseEvent) {
                            isDragging = false
                        }
                    },
                )

                window.addMouseMotionListener(
                    object : MouseAdapter() {
                        override fun mouseDragged(event: MouseEvent) {
                            if (isDragging) {
                                val currentLocation = window.location
                                window.location =
                                    Point(
                                        currentLocation.x + event.x - initialClick.x,
                                        currentLocation.y + event.y - initialClick.y,
                                    )
                            }
                        }
                    },
                )
            }

            windowFrame = window
            windowSize = Pair(state.size.width, state.size.height)

            DialogController.Initialize()

            Surface(modifier = Modifier.fillMaxSize(), color = MapleColorPalette.menu) {
                StartupCoordinator.Coordinate()
            }
        }
    }
}
