package com.spoiligaming.explorer.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object ColorPaletteUtility {
    fun getColorAsString(color: Color): String = "#%08X".format(color.toArgb())

    fun convertStringToColor(colorString: String): Color {
        require(
            colorString.startsWith("#") && (colorString.length == 7 || colorString.length == 9),
        ) {
            "Invalid color string format: $colorString"
        }

        val colorHex = colorString.drop(1)
        val (alpha, offset) =
            if (colorHex.length == 8) {
                colorHex.take(2).toInt(16) to 2
            } else {
                255 to 0
            }

        return Color(
            alpha = alpha,
            red = colorHex.substring(offset, offset + 2).toInt(16),
            green = colorHex.substring(offset + 2, offset + 4).toInt(16),
            blue = colorHex.substring(offset + 4).toInt(16),
        )
    }
}

fun Color.toHex(): String =
    String.format("%06X", toArgb() and 0x00FFFFFF) + String.format("%02X", (alpha * 255).toInt())

fun Color.toHexRgba(): String =
    "R:${(red * 255).toInt()}, G:${(green * 255).toInt()}, B:${(blue * 255).toInt()}, A:${(alpha * 255).toInt()}"

fun Color.toNumericRgba(): String =
    "${formatFloat(
        red,
    )}, ${formatFloat(green)}, ${formatFloat(blue)}, ${formatFloat(alpha)}"

fun formatFloat(value: Float): String = String.format("%.3f", value)
