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

@file:OptIn(ExperimentalUuidApi::class)

package com.spoiligaming.explorer.multiplayer.history

import com.spoiligaming.explorer.multiplayer.AcceptTexturesState
import com.spoiligaming.explorer.multiplayer.MultiplayerServer
import java.io.Serializable
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed class ServerListChange : Serializable {
    abstract val timestamp: Instant

    abstract fun description(): String
}

data class AddServerAtIndexChange(
    val index: Int,
    val server: MultiplayerServer,
    override val timestamp: Instant = Instant.now(),
) : ServerListChange() {
    override fun description() = "Added server '${server.name}' (${server.ip}) at position $index"
}

data class AddServerChange(
    val server: MultiplayerServer,
    override val timestamp: Instant = Instant.now(),
) : ServerListChange() {
    override fun description() = "Added server '${server.name}' (${server.ip})"
}

data class DeleteServerChange(
    val index: Int,
    val server: MultiplayerServer,
    override val timestamp: Instant = Instant.now(),
) : ServerListChange() {
    override fun description() = "Deleted server '${server.name}' (${server.ip}) at position $index"
}

data class DeleteMultipleServersChange(
    val serversWithIndices: List<Pair<Int, MultiplayerServer>>,
    override val timestamp: Instant = Instant.now(),
) : ServerListChange() {
    override fun description() = "Deleted ${serversWithIndices.size} servers"
}

data class DeleteAllServersChange(
    val servers: List<MultiplayerServer>,
    override val timestamp: Instant = Instant.now(),
) : ServerListChange() {
    override fun description() = "Deleted all servers (${servers.size} entries)"
}

data class EditAcceptedTexturesChange(
    val server: MultiplayerServer,
    val oldState: AcceptTexturesState,
    val newState: AcceptTexturesState,
    override val timestamp: Instant = Instant.now(),
) : ServerListChange() {
    override fun description() = "Set server resource packs to ${newState.name} for '${server.name}' (${server.ip})"
}

data class MoveServerChange(
    val server: MultiplayerServer,
    val fromIndex: Int,
    val toIndex: Int,
    override val timestamp: Instant = Instant.now(),
) : ServerListChange() {
    override fun description() = "Moved '${server.name}' (${server.ip}) from position $fromIndex to $toIndex"
}

data class SetHiddenChange(
    val serverId: Uuid,
    val oldHidden: Boolean,
    val newHidden: Boolean,
    override val timestamp: Instant = Instant.now(),
) : ServerListChange() {
    override fun description() = "Set hidden status for server $serverId from $oldHidden to $newHidden"
}

data class EditServerFieldsChange(
    val serverId: Uuid,
    val oldName: String,
    val newName: String,
    val oldIp: String,
    val newIp: String,
    override val timestamp: Instant = Instant.now(),
) : ServerListChange() {
    override fun description() =
        buildString {
            append("Edited server $serverId")
            if (oldName != newName) append(", renamed from '$oldName' to '$newName'")
            if (oldIp != newIp) append(", changed address from '$oldIp' to '$newIp'")
        }
}

data class UpdateIconChange(
    val serverId: Uuid,
    val oldIconBase64: String,
    val newIconBase64: String,
    override val timestamp: Instant = Instant.now(),
) : ServerListChange() {
    override fun description() = "Updated icon for server $serverId"
}

data class RemoveIconChange(
    val server: MultiplayerServer,
    val oldIconBase64: String,
    override val timestamp: Instant = Instant.now(),
) : ServerListChange() {
    override fun description() = "Removed icon for server '${server.name}' (${server.ip})"
}
