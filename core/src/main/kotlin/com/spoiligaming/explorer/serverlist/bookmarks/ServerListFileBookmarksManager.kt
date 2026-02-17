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

import com.spoiligaming.explorer.multiplayer.repository.ServerListRepository
import com.spoiligaming.explorer.settings.util.AppStoragePaths
import com.spoiligaming.explorer.settings.util.LegacyMultiplayerSettingsReader
import com.spoiligaming.explorer.util.canonicalize
import com.spoiligaming.explorer.util.serverListBookmarkKey
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object ServerListFileBookmarksManager {
    private val mutex = Mutex()
    private val store = ServerListFileBookmarksStore(AppStoragePaths.platformSettingsDir.toPath())
    private val _entries = MutableStateFlow<List<ServerListFileBookmarkEntry>>(emptyList())
    val entries = _entries.asStateFlow()
    private val _activePath = MutableStateFlow<Path?>(null)
    val activePath = _activePath.asStateFlow()

    private var initialized = false

    suspend fun load() {
        mutex.withLock {
            if (initialized) return
            val snapshot = store.load()
            _entries.value = snapshot.entries
            _activePath.value = snapshot.activePath
            ensureActiveResolvedLocked()
            initialized = true
            logger.info { "Loaded ${_entries.value.size} server list bookmarks" }
        }
    }

    suspend fun remember(
        path: Path,
        label: String? = null,
    ) {
        val canonical = path.canonicalize()
        val timestamp = Instant.now().toEpochMilli()
        val serverCount = resolveServerCount(canonical)
        val normalizedLabel = label?.trim().takeUnless { it.isNullOrEmpty() }

        mutate { list ->
            val key = canonical.serverListBookmarkKey()
            val existingIdx = list.indexOfFirst { it.path.serverListBookmarkKey() == key }

            if (existingIdx != -1) {
                val current = list[existingIdx]
                val updated =
                    current
                        .withLabel(normalizedLabel ?: current.label)
                        .touch(timestamp, serverCount)
                list[existingIdx] = updated
                logger.debug { "Refreshed bookmark ${current.id} for $canonical without adding a duplicate" }
                return@mutate
            }

            val newEntry =
                ServerListFileBookmarkEntry(
                    id = Uuid.random(),
                    path = canonical,
                    label = normalizedLabel,
                    serverCount = serverCount,
                    lastUsedEpochMillis = timestamp,
                ).touch(timestamp, serverCount)

            list.add(newEntry)
            logger.debug { "Added bookmark ${newEntry.id} for $canonical" }
        }
    }

    suspend fun setActivePath(path: Path?) {
        val canonical = path?.canonicalize()
        if (canonical != null) {
            remember(canonical)
        }

        mutex.withLock {
            ensureLoadedLocked()
            _activePath.value = canonical
            store.save(_entries.value, canonical)
        }
    }

    suspend fun remove(id: Uuid): ServerListFileBookmarkEntry =
        mutate { list ->
            check(list.size > 1) {
                "Cannot remove bookmark because at least one entry must remain."
            }

            val index = list.indexOfFirst { it.id == id }
            check(index != -1) {
                "No bookmark found with id $id"
            }

            list.removeAt(index)

            val fallbackIndex =
                when {
                    index < list.size -> index
                    index - 1 >= 0 -> index - 1
                    else -> error("Unexpected state after removing id $id because no fallback candidate exists.")
                }

            val fallback = list[fallbackIndex]

            logger.debug {
                "Removed bookmark $id and selected fallback ${fallback.id} at index $fallbackIndex"
            }

            fallback
        }

    suspend fun move(
        fromIndex: Int,
        toIndex: Int,
    ) = mutate { list ->
        if (fromIndex !in list.indices) return@mutate
        if (toIndex !in list.indices) return@mutate
        if (fromIndex == toIndex) return@mutate

        val entry = list.removeAt(fromIndex)
        list.add(toIndex, entry)
        logger.debug { "Moved bookmark from index $fromIndex to index $toIndex" }
    }

    internal suspend fun clear() =
        mutex.withLock {
            clearLocked()
        }

    suspend fun clearExceptActive(activePath: Path?) {
        mutex.withLock {
            if (_entries.value.isEmpty()) return
            if (activePath == null) {
                clearLocked()
                return
            }

            val activeKey = activePath.serverListBookmarkKey()
            val remaining =
                _entries.value.filter {
                    it.path.serverListBookmarkKey() == activeKey
                }

            _entries.value = remaining
            _activePath.value = activePath
            store.save(remaining, activePath)
            logger.info { "Cleared bookmarks except the active entry and kept ${remaining.size} items" }
        }
    }

    suspend fun pruneMissingFiles() =
        mutate { list ->
            val iterator = list.listIterator()
            var removedCount = 0
            val active = _activePath.value
            var activeRemoved = false
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (!Files.exists(entry.path)) {
                    iterator.remove()
                    removedCount++
                    if (active != null && entry.path.serverListBookmarkKey() == active.serverListBookmarkKey()) {
                        activeRemoved = true
                    }
                }
            }
            if (activeRemoved) {
                _activePath.value = null
            }
            logger.debug { "Pruned $removedCount missing file entries and now have ${list.size} bookmarks" }
        }

    suspend fun updateLabel(
        id: Uuid,
        transform: (String?) -> String?,
    ) = mutate { list ->
        val index = list.indexOfFirst { it.id == id }
        if (index == -1) return@mutate
        val current = list[index]
        val newLabel = transform(current.label)?.takeIf { it.isNotBlank() }
        list[index] = current.withLabel(newLabel)
        logger.debug { "Updated label for bookmark $id to $newLabel" }
    }

    suspend fun updateServerCount(
        id: Uuid,
        serverCount: Int?,
    ) = mutate { list ->
        val index = list.indexOfFirst { it.id == id }
        if (index == -1) return@mutate
        val current = list[index]
        if (current.serverCount == serverCount) return@mutate
        list[index] = current.withServerCount(serverCount)
        logger.debug { "Updated server count for bookmark $id to $serverCount" }
    }

    private suspend fun resolveServerCount(path: Path) =
        withContext(Dispatchers.IO) {
            if (!Files.exists(path)) return@withContext null

            runCatching {
                val repository = ServerListRepository(path)
                repository.load()
                repository.all().size
            }.onFailure { e ->
                logger.error(e) { "Failed resolving server count for $path" }
            }.getOrNull()
        }

    private suspend inline fun <T> mutate(block: (MutableList<ServerListFileBookmarkEntry>) -> T) =
        mutex.withLock {
            ensureLoadedLocked()
            val mutable = _entries.value.toMutableList()
            val result = block(mutable)
            _entries.value = mutable
            store.save(mutable, _activePath.value)
            result
        }

    private suspend fun ensureLoadedLocked() {
        if (initialized) return
        val snapshot = store.load()
        _entries.value = snapshot.entries
        _activePath.value = snapshot.activePath
        ensureActiveResolvedLocked()
        initialized = true
    }

    private suspend fun clearLocked() {
        if (_entries.value.isEmpty()) return
        _entries.value = emptyList()
        _activePath.value = null
        store.clear()
        logger.info { "Cleared server list bookmarks" }
    }

    private suspend fun ensureActiveResolvedLocked() {
        if (_activePath.value != null) {
            val added = ensureActiveEntryLocked(_activePath.value)
            if (added) {
                store.save(_entries.value, _activePath.value)
            }
            return
        }

        val legacyPath = LegacyMultiplayerSettingsReader.readServerListFile()?.canonicalize()
        if (legacyPath != null) {
            logger.info { "Migrating legacy server list file path from settings to bookmarks store." }
            _activePath.value = legacyPath
            ensureActiveEntryLocked(legacyPath)
            store.save(_entries.value, legacyPath)
            return
        }

        if (_entries.value.size == 1) {
            val fallback = _entries.value.first().path
            _activePath.value = fallback
            store.save(_entries.value, fallback)
        }
    }

    private fun ensureActiveEntryLocked(active: Path?): Boolean {
        if (active == null) return false
        val activeKey = active.serverListBookmarkKey()
        if (_entries.value.any { it.path.serverListBookmarkKey() == activeKey }) return false

        val timestamp = Instant.now().toEpochMilli()
        val fallback =
            ServerListFileBookmarkEntry(
                id = Uuid.random(),
                path = active,
                label = null,
                serverCount = null,
                lastUsedEpochMillis = timestamp,
            )
        _entries.value += fallback
        return true
    }
}

private val logger = KotlinLogging.logger {}
