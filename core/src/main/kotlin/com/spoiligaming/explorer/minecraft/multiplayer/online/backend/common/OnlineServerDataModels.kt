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

package com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common

sealed interface OnlineServerData : IServerData {
    val motd: String
    val onlinePlayers: Int
    val maxPlayers: Int
    val icon: String
    val versionInfo: String
    val protocolVersion: Int
    val ip: String
    val info: String?
}

data class McSrvStatOnlineServerData(
    override val motd: String,
    override val onlinePlayers: Int,
    override val maxPlayers: Int,
    override val icon: String,
    override val versionInfo: String,
    override val protocolVersion: Int,
    override val ip: String,
    override val info: String?,
    val versionName: String?,
    val eulaBlocked: Boolean,
) : OnlineServerData

data class McUtilsOnlineServerData(
    override val motd: String,
    override val onlinePlayers: Int,
    override val maxPlayers: Int,
    override val icon: String,
    override val versionInfo: String,
    override val protocolVersion: Int,
    override val ip: String,
    override val info: String?,
    val ping: Long,
    val secureChatEnforced: Boolean,
) : OnlineServerData
