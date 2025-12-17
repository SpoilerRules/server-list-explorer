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

package com.spoiligaming.explorer.settings.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ThemeMode {
    @SerialName("light")
    Light,

    @SerialName("dark")
    Dark,

    @SerialName("system")
    System,
}

@Serializable
enum class ThemePaletteStyle {
    TonalSpot,
    Neutral,
    Vibrant,
    Expressive,
    Rainbow,
    FruitSalad,
    Monochrome,
    Fidelity,
    Content,
}

@Serializable
data class ThemeSettings(
    @SerialName("theme_mode")
    val themeMode: ThemeMode = ThemeMode.System,
    @SerialName("seed_color")
    val seedColor: String = DEFAULT_SEED_COLOR,
    @SerialName("palette_style")
    val paletteStyle: ThemePaletteStyle = ThemePaletteStyle.TonalSpot,
    @SerialName("contrast_level")
    val contrastLevel: Double = DEFAULT_CONTRAST_LEVEL,
    @SerialName("use_system_accent_color")
    val useSystemAccentColor: Boolean = false,
    @SerialName("amoled_mode")
    val amoledMode: Boolean = false,
) {
    companion object {
        const val DEFAULT_SEED_COLOR = "#8D15FFED"
        const val DEFAULT_CONTRAST_LEVEL = 0.0
    }
}
