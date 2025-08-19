/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025 SpoilerRules
 *
 * Server List Explorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Server List Explorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Server List Explorer.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.spoiligaming.explorer.ui.extensions

import androidx.compose.ui.graphics.Color

internal fun String.toComposeColor(): Color {
    require(startsWith("#")) { "Color must start with '#'" }
    require(length == 7 || length == 9) {
        "Invalid color length: must be 7 (#RRGGBB) or 9 (#AARRGGBB) characters"
    }

    val hex = substring(1)
    val (alphaHex, colorHex) =
        when (hex.length) {
            6 -> null to hex
            8 -> hex.take(2) to hex.drop(2)
            else -> throw IllegalArgumentException("Invalid hex length: ${hex.length} after '#'")
        }

    fun parseHexComponent(
        hex: String,
        component: String,
    ): Int {
        require(hex.length == 2) { "Invalid $component hex length" }
        return hex.toIntOrNull(16)?.takeIf { it in 0..255 }
            ?: throw IllegalArgumentException("Invalid $component hex value: '$hex'")
    }

    val alpha = alphaHex?.let { parseHexComponent(it, "alpha") } ?: 255
    val red = parseHexComponent(colorHex.substring(0, 2), "red")
    val green = parseHexComponent(colorHex.substring(2, 4), "green")
    val blue = parseHexComponent(colorHex.substring(4, 6), "blue")

    return Color(alpha, red, green, blue)
}
