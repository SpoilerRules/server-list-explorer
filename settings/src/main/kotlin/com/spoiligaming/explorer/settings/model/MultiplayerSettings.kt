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

import com.spoiligaming.explorer.settings.serializer.PathSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.nio.file.Path

@Serializable
enum class ActionBarOrientation(val displayName: String) {
    @SerialName("right")
    Right("Right"),

    @SerialName("top")
    Top("Top"),

    @SerialName("left")
    Left("Left"),

    @SerialName("bottom")
    Bottom("Bottom"),
}

@Serializable
enum class ServerQueryMethod(val displayName: String) {
    @SerialName("mcsrvstat")
    McSrvStat("MCSrvStatus"),

    @SerialName("mcserverping")
    McServerPing("MCServerPing"),
}

@Serializable
data class MultiplayerSettings(
    @SerialName("server_list_file")
    @Serializable(with = PathSerializer::class)
    val serverListFile: Path? = null,
    @SerialName("server_query_method")
    val serverQueryMethod: ServerQueryMethod = ServerQueryMethod.McServerPing,
    @SerialName("entry_size_pct")
    val serverEntrySizePercent: Int = 20,
    @SerialName("drag_shake_intensity_deg")
    val dragShakeIntensityDegrees: Int = 1,
    @SerialName("action_bar_orientation")
    val actionBarOrientation: ActionBarOrientation = ActionBarOrientation.Left,
    @SerialName("connect_timeout_ms")
    val connectTimeoutMillis: Long = 120000,
    @SerialName("socket_timeout_ms")
    val socketTimeoutMillis: Long = 15000,
    // TODO: auto refresh icons on load/reload
)
