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

package com.spoiligaming.explorer.multiplayer.repository

import com.spoiligaming.explorer.multiplayer.AcceptTexturesState
import com.spoiligaming.explorer.multiplayer.MultiplayerServer
import com.spoiligaming.explorer.multiplayer.ServerListMonitor
import com.spoiligaming.explorer.multiplayer.datasource.FileServerListDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.util.Base64
import javax.imageio.ImageIO
import kotlin.uuid.ExperimentalUuidApi

class ServerListRepository(internal val serverDatPath: Path) {
    private val ds = FileServerListDataSource(serverDatPath, false)

    private val mutex = Mutex()

    private val _servers = MutableStateFlow<List<MultiplayerServer>>(emptyList())
    val servers = _servers.asStateFlow()

    lateinit var monitor: ServerListMonitor

    suspend fun load() =
        mutex.withLock {
            _servers.value =
                runCatching { ds.loadServers() }
                    .onFailure { logger.warn(it) { "Failed to load servers from disk" } }
                    .getOrDefault(emptyList())
            logger.debug { "Loaded ${_servers.value.size} servers from disk" }
        }

    suspend fun commit() =
        mutex.withLock {
            if (::monitor.isInitialized) {
                monitor.willSaveInternally()
            } else {
                logger.warn {
                    "ServerListMonitor is not initialized. " +
                        "Attach monitor to avoid false file conflicts on internal saves."
                }
            }

            ds.saveServers(_servers.value)

            if (::monitor.isInitialized) {
                monitor.recordInternalSave()
            }

            logger.debug { "Changes committed to disk" }
        }

    suspend fun all(): List<MultiplayerServer> =
        mutex.withLock {
            _servers.value.toList()
        }

    suspend fun addAt(
        index: Int,
        server: MultiplayerServer,
    ) = mutate {
        val pos = index.coerceIn(0, size)
        add(pos, server)
        logger.debug { "Added server '${server.name}' at $pos" }
    }

    suspend fun add(server: MultiplayerServer) =
        mutate {
            add(server)
            logger.debug { "Added server '${server.name}'" }
        }

    suspend fun replace(
        index: Int,
        newServer: MultiplayerServer,
    ) = mutate {
        require(index in indices) { "Index $index out of bounds for size $size" }
        val old = this[index]
        this[index] = newServer.copy(id = old.id)
    }

    suspend fun delete(index: Int) =
        mutate {
            require(index in indices) { "Index $index out of bounds for size $size" }
            removeAt(index)
            logger.debug { "Deleted server at index $index" }
        }

    suspend fun deleteAll() =
        mutate {
            clear()
            logger.debug { "Cleared all servers" }
        }

    suspend fun move(
        from: Int,
        to: Int,
    ) = mutate {
        require(from in indices) { "from $from out of bounds" }
        require(to in 0..size) { "to $to out of bounds" }
        if (from != to) swap(from, to)
        logger.debug { "Moved server from $from to $to" }
    }

    suspend fun moveUp(index: Int) =
        mutate {
            if (index in 1 until size) {
                swap(index, index - 1)
                logger.debug { "Moved server at index $index up" }
            }
        }

    suspend fun moveDown(index: Int) =
        mutate {
            if (index in 0 until lastIndex) {
                swap(index, index + 1)
                logger.debug { "Moved server at index $index down" }
            }
        }

    suspend fun updateIcon(
        index: Int,
        iconBase64: String,
    ) = mutate {
        require(index in indices) { "Index $index out of bounds for size $size" }

        val imageBytes =
            withContext(Dispatchers.Default) {
                runCatching {
                    Base64.getDecoder().decode(iconBase64)
                }.getOrElse {
                    error("Invalid Base64 string for server at index $index: ${it.message}")
                }
            }

        withContext(Dispatchers.IO) {
            checkNotNull(ImageIO.read(ByteArrayInputStream(imageBytes))) {
                "Decoded image is null or invalid for server at index $index"
            }
        }

        this[index] = this[index].copy(iconBase64 = iconBase64)

        logger.info { "Updated icon for server at index $index" }
    }

    suspend fun removeIconAt(index: Int): Unit =
        mutate {
            require(index in indices) { "Index $index out of bounds for size $size" }

            logger.debug { "Clearing icon for server at index $index in memory" }
            this[index] = this[index].copy(iconBase64 = null)
            ds.removeFieldAtIndex(index, "icon")
        }

    suspend fun removeAcceptedTexturesAt(index: Int) =
        mutate {
            require(index in indices) { "Index $index out of bounds for size $size" }

            logger.debug { "Clearing acceptTextures for server at index $index" }
            this[index] = this[index].copy(acceptTextures = AcceptTexturesState.Prompt)
            ds.removeFieldAtIndex(index, "acceptTextures")
        }

    private suspend inline fun mutate(block: MutableList<MultiplayerServer>.() -> Unit) =
        mutex.withLock {
            val newList = _servers.value.toMutableList().apply(block)
            _servers.value = newList
        }

    suspend fun createTempServerListFile(): Path =
        withContext(Dispatchers.IO) {
            val tempFile = Files.createTempFile("servers", ".dat")
            FileServerListDataSource(tempFile, false).saveServers(all())
            Files.setLastModifiedTime(tempFile, FileTime.from(Instant.now()))
            tempFile
        }
}

private fun <T> MutableList<T>.swap(
    i: Int,
    j: Int,
) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

private val logger = KotlinLogging.logger {}
