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
enum class ActionBarOrientation {
    @SerialName("right")
    Right,

    @SerialName("top")
    Top,

    @SerialName("left")
    Left,

    @SerialName("bottom")
    Bottom,
}

@Serializable
enum class ServerQueryMethod {
    @SerialName("mcsrvstat")
    McSrvStat,

    @SerialName("mcutils")
    McUtils,
}

@Serializable
data class MultiplayerSettings(
    @SerialName("server_query_method")
    val serverQueryMethod: ServerQueryMethod = ServerQueryMethod.McUtils,
    @SerialName("entry_size_scale")
    val serverEntryScale: Float = 1.0f,
    @SerialName("drag_shake_intensity_deg")
    val dragShakeIntensityDegrees: Int = 1,
    @SerialName("action_bar_orientation")
    val actionBarOrientation: ActionBarOrientation = ActionBarOrientation.Right,
    // TODO: auto refresh icons on load/reload
)
