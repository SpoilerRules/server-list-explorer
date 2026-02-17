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

package com.spoiligaming.explorer.settings.util

import com.spoiligaming.explorer.settings.serializer.PathSerializer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path

// TEMP: will be removed in Server List Explorer 2.4
object LegacyMultiplayerSettingsReader {
    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    fun readServerListFile(): Path? {
        val file = AppStoragePaths.settingsDir.resolve(SETTINGS_FILE_NAME)
        if (!file.exists() || file.length() == 0L) return null

        return runCatching {
            val raw = file.readText()
            if (raw.isBlank()) return@runCatching null
            json.decodeFromString(LegacyMultiplayerSettings.serializer(), raw).serverListFile
        }.onFailure { e ->
            logger.warn(e) { "Failed to read legacy server list file from ${file.absolutePath}" }
        }.getOrNull()
    }
}

@Serializable
private data class LegacyMultiplayerSettings(
    @SerialName("server_list_file")
    @Serializable(with = PathSerializer::class)
    val serverListFile: Path? = null,
)

private const val SETTINGS_FILE_NAME = "multiplayer.json"
private val logger = KotlinLogging.logger {}
