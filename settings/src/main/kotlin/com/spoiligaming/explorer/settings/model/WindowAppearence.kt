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

import com.spoiligaming.explorer.settings.model.WindowCornerPreferenceSetting.ELEVATED_SQUARE
import com.spoiligaming.explorer.settings.model.WindowCornerPreferenceSetting.FLAT_SQUARE
import com.spoiligaming.explorer.settings.model.WindowCornerPreferenceSetting.ROUNDED
import com.spoiligaming.explorer.settings.model.WindowCornerPreferenceSetting.SYSTEM_DEFAULT
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the window corner styling preference, mapped to the underlying Windows DWM setting.
 *
 * These values correspond to how corners are rendered for top-level windows:
 * - [SYSTEM_DEFAULT] – Uses the system's default corner style (typically rounded on Windows 11).
 * - [ROUNDED] – Forces rounded corners.
 * - [ELEVATED_SQUARE] – Forces square corners with visual elevation (shadow).
 * - [FLAT_SQUARE] – Forces square corners without elevation, producing a flat appearance.
 *
 * Note: Rounded corners require Windows 11 (build 22000 or later).
 *
 * @see <a href="https://learn.microsoft.com/en-us/windows/win32/api/dwmapi/ne-dwmapi-dwm_window_corner_preference">
 * DWM_WINDOW_CORNER_PREFERENCE – Windows API Documentation
 * </a>
 */
@Serializable
enum class WindowCornerPreferenceSetting(
    val dwmValue: Int,
) {
    @SerialName("system_default")
    SYSTEM_DEFAULT(0), // DWMWCP_DEFAULT

    @SerialName("rounded")
    ROUNDED(2), // DWMWCP_ROUND,

    @SerialName("square_elevated")
    ELEVATED_SQUARE(1), // DWMWCP_DONOTROUND

    @SerialName("square_flat")
    FLAT_SQUARE(3), // DWMWCP_ROUNDSMALL
}

@Serializable
enum class TitleBarColorMode {
    @SerialName("auto")
    AUTO,

    @SerialName("manual")
    MANUAL,
}

@Serializable
data class WindowAppearance(
    @SerialName("corner_preference")
    val windowCornerPreference: WindowCornerPreferenceSetting = SYSTEM_DEFAULT,
    @SerialName("titlebar_color_mode")
    val titleBarColorMode: TitleBarColorMode = TitleBarColorMode.AUTO,
    @SerialName("custom_title_bar_color")
    val customTitleBarColor: String = "#FFFFFF",
    @SerialName("use_custom_border_color")
    val useCustomBorderColor: Boolean = false,
    @SerialName("custom_border_color")
    val customBorderColor: String = "#FFFFFF",
)
