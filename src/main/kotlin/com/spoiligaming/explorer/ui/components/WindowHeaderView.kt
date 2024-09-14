package com.spoiligaming.explorer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.SoftwareInformation
import com.spoiligaming.explorer.isBackupRestoreInProgress
import com.spoiligaming.explorer.isWindowMaximized
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.icons.IconFactory
import com.spoiligaming.explorer.ui.navigation.NavigationController
import com.spoiligaming.explorer.ui.navigation.Screen
import com.spoiligaming.explorer.utils.WindowUtility
import com.spoiligaming.explorer.windowFrame
import java.awt.Cursor
import javax.swing.JFrame
import kotlin.system.exitProcess

@Composable
fun WindowHeaderView(allowNavigation: Boolean) {
    val currentScreen = NavigationController.currentScreen

    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        WindowTitle()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (allowNavigation && !isBackupRestoreInProgress) {
                        if (currentScreen is Screen.FileBackupScreen) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SettingsButton(
                                    currentScreen = Screen.Settings,
                                ) {
                                    NavigationController.navigateTo(Screen.Home)
                                }
                                SettingsButton(
                                    currentScreen = Screen.Home,
                                ) {
                                    NavigationController.navigateTo(Screen.Settings)
                                }
                            }
                        } else {
                            SettingsButton(
                                currentScreen = currentScreen,
                                onClick = {
                                    NavigationController.navigateTo(
                                        if (currentScreen is Screen.Settings) {
                                            Screen.Home
                                        } else {
                                            Screen.Settings
                                        },
                                    )
                                },
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ControlButton(ActionType.MINIMIZE)
                        ControlButton(ActionType.MAXIMIZE)
                        ControlButton(ActionType.EXIT)
                    }
                }
            }

            HorizontalDivider(
                color = MapleColorPalette.control,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth(0.98f),
            )
        }
    }
}

@Composable
private fun ControlButton(type: ActionType) =
    Button(
        onClick = {
            when (type) {
                ActionType.EXIT -> exitProcess(0)
                ActionType.MAXIMIZE -> {
                    val currentScale = ConfigurationHandler.getInstance().themeSettings.windowScale

                    if (isWindowMaximized) {
                        WindowUtility.restoreWindowSize(
                            ConfigurationHandler.getInstance().windowProperties.wasPreviousScaleResizable,
                        )
                    } else {
                        ConfigurationHandler.updateValue {
                            themeSettings.windowScale = "Maximized"
                            if (currentScale != "Resizable") {
                                windowProperties.previousScale = currentScale
                                if (currentScale != "Maximized") {
                                    windowProperties.wasPreviousScaleResizable = false
                                }
                            } else {
                                windowProperties.wasPreviousScaleResizable = true
                            }
                        }
                        WindowUtility.maximizeWindow()
                    }
                }
                ActionType.MINIMIZE -> windowFrame.extendedState = JFrame.ICONIFIED
            }
        },
        modifier = Modifier.size(60.dp, 40.dp).pointerHoverIcon(PointerIcon.Hand),
        shape = RoundedCornerShape(12.dp),
        colors =
            ButtonDefaults.buttonColors(
                backgroundColor = MapleColorPalette.menu,
                contentColor = MapleColorPalette.fadedText,
            ),
    ) {
        if (type != ActionType.MAXIMIZE) {
            Text(
                text = if (type == ActionType.MINIMIZE) "_" else "x",
                color = MapleColorPalette.fadedText,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaMedium,
                        fontWeight = FontWeight.Normal,
                        fontSize = 17.sp,
                    ),
                modifier = Modifier.padding(horizontal = 10.dp),
            )
        } else {
            Icon(
                imageVector =
                    if (isWindowMaximized) {
                        Icons.Filled.FullscreenExit
                    } else {
                        Icons.Filled.Fullscreen
                    },
                contentDescription = "Icon for window maximize/restore button",
                tint = MapleColorPalette.fadedText,
                modifier = Modifier.size(17.dp),
            )
        }
    }

@Composable
private fun SettingsButton(
    currentScreen: Screen,
    onClick: () -> Unit,
) {
    val (icon, buttonText) =
        if (currentScreen is Screen.Home) {
            IconFactory.toolsIcon to "Settings"
        } else {
            IconFactory.goBackIcon to "Home"
        }

    Button(
        onClick = onClick,
        modifier =
            Modifier
                .width(123.dp)
                .height(40.dp)
                .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
        shape = RoundedCornerShape(12.dp),
        colors =
            ButtonDefaults.buttonColors(
                backgroundColor = MapleColorPalette.menu,
                contentColor = MapleColorPalette.fadedText,
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = buttonText,
                color = MapleColorPalette.fadedText,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaMedium,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                    ),
            )
        }
    }
}

@Composable
private fun WindowTitle() =
    Box(
        modifier = Modifier.fillMaxWidth().height(40.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text = "Server List Explorer - ${SoftwareInformation.VERSION}",
            color = MapleColorPalette.accent,
            style =
                TextStyle(
                    fontFamily = FontFactory.comfortaaMedium,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                ),
        )
    }

enum class ActionType {
    EXIT,
    MAXIMIZE,
    MINIMIZE,
}
