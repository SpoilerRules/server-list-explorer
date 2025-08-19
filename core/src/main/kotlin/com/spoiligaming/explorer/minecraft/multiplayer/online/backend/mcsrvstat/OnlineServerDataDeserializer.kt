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

package com.spoiligaming.explorer.minecraft.multiplayer.online.backend.mcsrvstat

import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.McSrvStatOnlineServerData
import kotlinx.serialization.json.Json

internal object OnlineServerDataSerializer {
    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
        }
    }

    fun decode(rawJson: String) = json.decodeFromString(McSrvStatServerStatusResponse.serializer(), rawJson)

    fun reduce(response: McSrvStatServerStatusResponse) =
        McSrvStatOnlineServerData(
            motd = response.motd.raw.joinToString(separator = "\n"),
            onlinePlayers = response.players.online.toInt(),
            maxPlayers = response.players.max.toInt(),
            versionInfo = response.versionInfo,
            icon = response.icon,
            versionName = response.protocol.name,
            protocolVersion = response.protocol.version.toInt(),
            ip = response.ip,
            info = response.info?.raw?.joinToString(separator = "\n"),
            eulaBlocked = response.eulaBlocked,
        )

    // just in case
    fun fromJson(rawJson: String) = reduce(decode(rawJson))
}
