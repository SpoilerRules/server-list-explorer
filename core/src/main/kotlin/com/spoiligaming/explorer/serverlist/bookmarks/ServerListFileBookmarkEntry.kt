/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025-2026 SpoilerRules
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

package com.spoiligaming.explorer.serverlist.bookmarks

import com.spoiligaming.explorer.settings.serializer.PathSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.nio.file.Path
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ServerListFileBookmarkEntry(
    @SerialName("id")
    val id: Uuid = Uuid.random(),
    @SerialName("path")
    @Serializable(with = PathSerializer::class)
    val path: Path,
    @SerialName("label")
    val label: String? = null,
    @SerialName("server_count")
    val serverCount: Int? = null,
    @SerialName("last_used_epoch_ms")
    val lastUsedEpochMillis: Long,
) {
    init {
        require(lastUsedEpochMillis > 0) { "lastUsedEpochMillis must be a positive epoch millis value" }
    }

    internal fun withLabel(newLabel: String?) = copy(label = newLabel?.takeIf { it.isNotBlank() })

    internal fun withServerCount(count: Int?) = copy(serverCount = count)

    internal fun touch(
        nowEpochMillis: Long,
        updatedServerCount: Int? = serverCount,
    ) = copy(lastUsedEpochMillis = nowEpochMillis, serverCount = updatedServerCount)
}
