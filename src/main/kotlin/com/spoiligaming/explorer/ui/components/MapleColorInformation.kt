package com.spoiligaming.explorer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.utils.formatFloat
import com.spoiligaming.explorer.utils.toHex

@Composable
fun MapleColorInformation(colorToPreview: Color) =
    Box(
        modifier =
            Modifier.width(235.dp)
                .height(60.dp)
                .background(MapleColorPalette.menu, shape = RoundedCornerShape(18.dp)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier.size(60.dp)
                        .background(colorToPreview, shape = RoundedCornerShape(12.dp)),
            )
            Column(Modifier.offset(x = 2.dp), verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
                ColorInfoText("#${colorToPreview.toHex()}", 16.sp, FontFactory.comfortaaLight)
                ColorInfoText(
                    "R:${(colorToPreview.red * 255).toInt()}, G:${(colorToPreview.green * 255).toInt()}, B:${(colorToPreview.blue * 255).toInt()}, A:${(colorToPreview.alpha * 255).toInt()}",
                    15.sp,
                    FontFactory.comfortaaLight,
                )
                ColorInfoText(
                    "(${formatFloat(colorToPreview.red)}, ${formatFloat(colorToPreview.green)}, ${
                        formatFloat(
                            colorToPreview.blue,
                        )
                    }, ${formatFloat(colorToPreview.alpha)})",
                    15.sp,
                    FontFactory.comfortaaLight,
                )
            }
        }
    }

@Composable
private fun ColorInfoText(
    text: String,
    fontSize: TextUnit,
    fontFamily: FontFamily,
) = Text(
    text = text,
    color = MapleColorPalette.text,
    style =
        TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Light, fontSize = fontSize),
    maxLines = 1,
    overflow = TextOverflow.Visible,
)
