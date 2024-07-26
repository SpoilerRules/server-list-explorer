package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.onClick
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.spoiligaming.explorer.ui.extensions.baseHoverColor
import com.spoiligaming.explorer.ui.extensions.onHover
import com.spoiligaming.logging.Logger

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MapleHyperlink(
    text: String,
    color: Color,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    url: String,
) {
    val uriHandler = LocalUriHandler.current
    var isHovered by remember { mutableStateOf(false) }

    var textColor by remember { mutableStateOf(color) }

    Text(
        text = text,
        color = textColor,
        style = TextStyle(fontFamily = fontFamily, fontWeight = fontWeight, fontSize = fontSize),
        modifier =
            Modifier.baseHoverColor(color) { modifiedColor ->
                textColor =
                    if (isHovered) {
                        modifiedColor
                    } else {
                        color
                    }
            }
                .pointerHoverIcon(PointerIcon.Hand)
                .onClick {
                    runCatching {
                        uriHandler.openUri(url)
                        Logger.printSuccess("Link opened successfully: $url")
                    }.onFailure { error ->
                        Logger.printError("Failed to open link: $url - ${error.message}")
                    }
                }
                .onHover { isHovered = it },
    )
}
