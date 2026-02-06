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

@file:OptIn(ExperimentalSerializationApi::class)

package com.spoiligaming.explorer.serverlist.bookmarks

import com.spoiligaming.explorer.settings.serializer.PathSerializer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

internal class ServerListFileBookmarksStore(
    directory: Path,
    fileName: String = DEFAULT_FILE_NAME,
) {
    private val bookmarksDir = directory.toAbsolutePath().normalize()
    private val bookmarksFile = bookmarksDir.resolve(fileName)

    suspend fun load() =
        withContext(Dispatchers.IO) {
            val sourceFile = bookmarksFile.takeIf { Files.exists(it) }

            if (sourceFile == null) {
                logger.debug { "Bookmark file not found at $bookmarksFile, returning empty list" }
                return@withContext ServerListFileBookmarksSnapshot()
            }

            runCatching { Files.readAllBytes(sourceFile) }
                .onFailure { e ->
                    logger.error(e) { "Failed reading bookmarks from $sourceFile" }
                }.mapCatching { bytes ->
                    ProtoBuf.decodeFromByteArray(Snapshot.serializer(), bytes)
                }.onFailure { e ->
                    logger.error(e) { "Failed decoding bookmarks from $sourceFile" }
                }.getOrElse { Snapshot() }
                .let { snapshot ->
                    ServerListFileBookmarksSnapshot(
                        entries = snapshot.entries,
                        activePath = snapshot.activePath,
                    )
                }
        }

    suspend fun save(
        entries: List<ServerListFileBookmarkEntry>,
        activePath: Path?,
    ) = withContext(Dispatchers.IO) {
        ensureDirectory()
        val payload = Snapshot(entries = entries, activePath = activePath)

        val bytes =
            runCatching { ProtoBuf.encodeToByteArray(Snapshot.serializer(), payload) }
                .onFailure { e ->
                    logger.error(e) { "Failed to encode bookmarks snapshot" }
                }.getOrElse { return@withContext }

        runCatching {
            Files.write(
                bookmarksFile,
                bytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE,
            )
            logger.debug { "Persisted ${entries.size} server list bookmarks to $bookmarksFile" }
        }.onFailure {
            logger.error(it) { "Failed to write bookmarks snapshot to $bookmarksFile" }
        }
    }

    suspend fun clear() =
        withContext(Dispatchers.IO) {
            if (!Files.exists(bookmarksFile)) return@withContext
            runCatching { Files.delete(bookmarksFile) }
                .onFailure { e ->
                    logger.warn(e) { "Failed deleting bookmarks file at $bookmarksFile" }
                }.onSuccess { logger.info { "Deleted bookmarks file at $bookmarksFile" } }
        }

    private fun ensureDirectory() {
        if (Files.exists(bookmarksDir)) return
        runCatching { Files.createDirectories(bookmarksDir) }
            .onFailure { e ->
                logger.error(e) { "Failed creating directory $bookmarksDir" }
            }.onSuccess { logger.info { "Created bookmarks directory $bookmarksDir" } }
    }

    @Serializable
    private data class Snapshot(
        @SerialName("entries")
        val entries: List<ServerListFileBookmarkEntry> = emptyList(),
        @SerialName("active_path")
        @Serializable(with = PathSerializer::class)
        val activePath: Path? = null,
    )

    private companion object {
        const val DEFAULT_FILE_NAME = "bookmarks.pb"
    }
}

internal data class ServerListFileBookmarksSnapshot(
    val entries: List<ServerListFileBookmarkEntry> = emptyList(),
    val activePath: Path? = null,
)

private val logger = KotlinLogging.logger {}
