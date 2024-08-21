package com.spoiligaming.explorer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.state.DialogController
import com.spoiligaming.logging.Logger
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostOs
import java.awt.Toolkit
import javax.swing.JFrame

val controlButtonAsString: String =
    when (hostOs) {
        OS.MacOS -> "Cmd"
        else -> "Control"
    }

lateinit var windowFrame: JFrame
lateinit var windowSize: Pair<Dp, Dp>

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

    val windowScale by mutableStateOf(
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
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val windowWidth = (800 * windowScale).toInt().dp
    val windowHeight = (600 * windowScale).toInt().dp

    application {
        val state =
            rememberWindowState(
                width = windowWidth,
                height = windowHeight,
                position =
                    WindowPosition(
                        ((screenSize.width - windowWidth.value.toInt()) / 2).dp,
                        ((screenSize.height - windowHeight.value.toInt()) / 2).dp,
                    ),
            )

        Window(
            onCloseRequest = ::exitApplication,
            visible = true,
            title = "Server List Explorer",
            resizable = false,
            undecorated = true,
            transparent = true,
            state = state,
        ) {
            windowFrame = window
            windowSize = remember { Pair(state.size.width, state.size.height) }

            DialogController.Initialize()

            Surface(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Transparent, RoundedCornerShape(24.dp)),
                color = MapleColorPalette.menu,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    WindowDraggableArea(
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                StartupCoordinator.Coordinate()
            }
        }
    }
}
