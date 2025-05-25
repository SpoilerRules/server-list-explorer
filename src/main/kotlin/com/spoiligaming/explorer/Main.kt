package com.spoiligaming.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.state.DialogController
import com.spoiligaming.explorer.utils.WindowUtility
import com.spoiligaming.logging.Logger
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostOs
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JFrame

val controlButtonAsString: String =
    when (hostOs) {
        OS.MacOS -> "Cmd"
        else -> "Control"
    }

lateinit var windowFrame: JFrame
lateinit var windowSize: Pair<Dp, Dp>
var isWindowMaximized by mutableStateOf(
    ConfigurationHandler.getInstance().windowProperties.isMaximized,
)

var disableIconIndexing = false
var disableServerInfoIndexing = false

var isBackupRestoreInProgress by mutableStateOf(false)

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
@OptIn(ExperimentalComposeUiApi::class)
fun main(args: Array<String>) {
    ConfigurationHandler.getInstance()
        .generalSettings
        .renderApi
        .takeIf { !it.equals("Default", ignoreCase = true) }
        ?.uppercase()
        ?.replace(" ", "_")
        ?.let { System.setProperty("skiko.renderApi", it) }
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

    val themeSettings = ConfigurationHandler.getInstance().themeSettings
    val windowProperties = ConfigurationHandler.getInstance().windowProperties
    val screenSize = Toolkit.getDefaultToolkit().screenSize

    application {
        val density = LocalDensity.current

        // Note: The following three variables are not cached as it's unclear whether they need to be dynamic.
        val windowScale =
            WindowUtility.windowScaleMapping[themeSettings.windowScale]
                ?: WindowUtility.windowScaleMapping[windowProperties.previousScale]
                ?: themeSettings.windowScale.toFloatOrNull()
                ?: 1f

        val (defaultWidth, defaultHeight) =
            (800 * windowScale).toInt() to (600 * windowScale).toInt()

        val (windowWidth, windowHeight) =
            when (themeSettings.windowScale) {
                "Maximized" -> WindowUtility.getUsableScreenSize().also { isWindowMaximized = true }
                "Resizable" ->
                    windowProperties.currentWindowSize?.let { it.first to it.second }
                        ?: (defaultWidth to defaultHeight)

                else -> defaultWidth to defaultHeight
            }

        val windowState =
            rememberWindowState(
                width = windowWidth.dp,
                height = windowHeight.dp,
                position =
                    remember(isWindowMaximized) {
                        if (isWindowMaximized) {
                            WindowPosition.PlatformDefault
                        } else {
                            WindowPosition(
                                ((screenSize.width - windowWidth) / 2).dp,
                                ((screenSize.height - windowHeight) / 2).dp,
                            )
                        }
                    },
            )

        Window(
            onCloseRequest = ::exitApplication,
            visible = true,
            title = "Server List Explorer",
            decoration = WindowDecoration.Undecorated(8.dp),
            transparent = true,
            state = windowState,
        ) {
            window.minimumSize = Dimension(800, 600)
            window.isResizable = themeSettings.windowScale == "Resizable"
            windowFrame = window
            windowSize =
                if (themeSettings.windowScale in listOf("Resizable", "Maximized")) {
                    with(density) {
                        Pair(window.size.width.toDp(), window.size.height.toDp())
                    }
                } else {
                    Pair(windowState.size.width, windowState.size.height)
                }

            window.apply {
                val screenInsets =
                    remember {
                        Toolkit.getDefaultToolkit().getScreenInsets(
                            GraphicsEnvironment.getLocalGraphicsEnvironment()
                                .defaultScreenDevice.defaultConfiguration,
                        )
                    }

                if (themeSettings.windowScale == "Maximized") {
                    setLocation(screenInsets.left, screenInsets.top)
                }

                addComponentListener(
                    object : ComponentAdapter() {
                        override fun componentResized(event: ComponentEvent) {
                            if (themeSettings.windowScale == "Resizable") {
                                val newSize = event.component.size
                                ConfigurationHandler.updateValue {
                                    windowProperties.currentWindowSize =
                                        newSize.width to newSize.height
                                }
                            }
                        }
                    },
                )
            }

            WindowDraggableArea(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            color = MapleColorPalette.menu,
                            shape = RoundedCornerShape(if (isWindowMaximized) 0.dp else 24.dp),
                        ),
            ) {
                StartupCoordinator.Coordinate()
                DialogController.RenderDialog()
            }
        }
    }
}
