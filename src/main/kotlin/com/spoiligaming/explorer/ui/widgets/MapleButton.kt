package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory

enum class MapleButtonWidth(val width: Dp?) {
    FILL_MAX(null),
    PROFILE(149.2.dp),
    STANDARD(70.dp),
    FILE_PICKER_DIALOG(210.dp),
    VALUE_REPLACEMENT_DIALOG(195.dp),
    SERVER_ENTRY_CREATION_DIALOG(400.dp),
}

enum class MapleButtonHeight(val height: Dp?) {
    FILL_MAX(null),
    WEB_VERSION(36.dp),
    ORIGINAL(26.dp),
    DIALOG(28.dp),
}

@Composable
fun MapleButton(
    modifier: Modifier = Modifier,
    width: Dp? = MapleButtonWidth.STANDARD.width,
    height: Dp? = MapleButtonHeight.ORIGINAL.height,
    backgroundColor: Color = MapleColorPalette.control,
    text: String,
    textColor: Color = MapleColorPalette.text,
    fontSize: TextUnit = 15.sp,
    padding: Dp? = null,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    var mod = modifier

    width?.let {
        mod =
            mod.then(
                if (it == MapleButtonWidth.FILL_MAX.width) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier.width(it)
                },
            )
    }

    height?.let {
        mod =
            mod.then(
                if (it == MapleButtonHeight.FILL_MAX.height) {
                    Modifier.fillMaxHeight()
                } else {
                    Modifier.height(it)
                },
            )
    }

    Box(
        modifier =
            mod.padding(padding ?: 0.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(backgroundColor, RoundedCornerShape(10.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(color = Color.White),
                ) {
                    onClick()
                },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            style =
                TextStyle(
                    fontFamily = FontFactory.comfortaaLight,
                    fontWeight = FontWeight.Normal,
                    fontSize = fontSize,
                ),
        )
    }
}

@Composable
fun DisabledMapleButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MapleColorPalette.control,
    text: String,
    textColor: Color = MapleColorPalette.text,
    hoverTooltipText: String,
    fontSize: TextUnit = 15.sp,
) = MapleTooltip(hoverTooltipText, MapleColorPalette.text, 500) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(10.dp))
                .background(backgroundColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = 0.5f),
            style =
                TextStyle(
                    fontFamily = FontFactory.comfortaaLight,
                    fontWeight = FontWeight.Normal,
                    fontSize = fontSize,
                ),
        )
    }
}
