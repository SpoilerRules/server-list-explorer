package com.spoiligaming.explorer.ui.fonts

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

object FontFactory {
    val comfortaaLight =
        FontFamily(
            Font(
                resource = "font/comfortaa_light.ttf",
            ),
        )
    val comfortaaRegular =
        FontFamily(
            Font(
                resource = "font/comfortaa_regular.ttf",
            ),
        )
    val comfortaaMedium =
        FontFamily(
            Font(
                resource = "font/comfortaa_medium.ttf",
            ),
        )
    val comfortaaSemibold =
        FontFamily(
            Font(
                resource = "font/comfortaa_semibold.ttf",
                weight = FontWeight.SemiBold,
                style = FontStyle.Normal,
            ),
        )
    val comfortaaBold =
        FontFamily(
            Font(
                resource = "font/comfortaa_bold.ttf",
            ),
        )
}
