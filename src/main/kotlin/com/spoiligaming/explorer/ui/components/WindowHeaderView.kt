package com.spoiligaming.explorer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.spoiligaming.explorer.SoftwareInformation
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.icons.IconFactory
import com.spoiligaming.explorer.ui.navigation.NavigationController
import com.spoiligaming.explorer.ui.navigation.Screen
import com.spoiligaming.explorer.windowFrame
import java.awt.Cursor
import javax.swing.JFrame
import kotlin.system.exitProcess

@Composable
fun WindowHeaderView(displaySettingsButton: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
        ControlButtons(displaySettingsButton)
    }

    Divider()

    WindowTitle()
}

@Composable
private fun ControlButtons(displaySettingsButton: Boolean) {
    val currentScreen = NavigationController.currentScreen

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
        horizontalArrangement = Arrangement.spacedBy((-7.5).dp),
        verticalAlignment = Alignment.Top,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        ControlButton(ActionType.MINIMIZE)
        ControlButton(ActionType.EXIT)
    }

    if (displaySettingsButton) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            horizontalArrangement = Arrangement.spacedBy((-7.5).dp),
            verticalAlignment = Alignment.Top,
        ) {
            SettingsButton(
                currentScreen = currentScreen,
                onClick = {
                    NavigationController.navigateTo(
                        if (currentScreen is Screen.Settings) Screen.Main else Screen.Settings,
                    )
                },
            )
        }
    }
}

@Composable
private fun ControlButton(type: ActionType) {
    Button(
        onClick = {
            if (type == ActionType.MINIMIZE) {
                windowFrame.extendedState = JFrame.ICONIFIED
            } else {
                exitProcess(0)
            }
        },
        modifier = Modifier.padding(6.dp).size(60.dp, 40.dp).pointerHoverIcon(PointerIcon.Hand),
        shape = RoundedCornerShape(12.dp),
        colors =
            ButtonDefaults.buttonColors(
                backgroundColor = MapleColorPalette.menu,
                contentColor = MapleColorPalette.fadedText,
            ),
    ) {
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
    }
}

@Composable
private fun SettingsButton(
    currentScreen: Screen,
    onClick: () -> Unit,
) {
    val (icon, buttonText) =
        if (currentScreen is Screen.Main) {
            IconFactory.toolsIcon to "Settings"
        } else {
            IconFactory.goBackIcon to "Home"
        }

    Button(
        onClick = onClick,
        modifier =
            Modifier.padding(6.dp)
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
private fun WindowTitle() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 23.dp),
        contentAlignment = Alignment.TopCenter,
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
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(bottom = 475.dp),
        contentAlignment = Alignment.Center,
    ) {
        HorizontalDivider(
            color = MapleColorPalette.control,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth(0.98f),
        )
    }
}

enum class ActionType {
    EXIT,
    MINIMIZE,
}
