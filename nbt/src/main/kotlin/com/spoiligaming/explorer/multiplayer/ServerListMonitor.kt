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

package com.spoiligaming.explorer.multiplayer

import com.spoiligaming.explorer.multiplayer.repository.ServerListRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.util.zip.CRC32

private data class FileStats(
    val lastModified: FileTime,
    val size: Long,
    val crc32: Long,
)

class ServerListMonitor(
    private val repo: ServerListRepository,
    private val scope: CoroutineScope,
    private val intervalMillis: Long,
) {
    private var monitorJob: Job? = null
    private val _changeFlow = MutableStateFlow<Path?>(null)
    val changeFlow = _changeFlow
    private var lastStats: FileStats? = null

    @Volatile
    internal var internalSavePending = false
        private set

    init {
        require(intervalMillis > 0) { "intervalMillis must be positive" }
    }

    fun start() {
        stop()
        val filePath = repo.serverDatPath
        lastStats = filePath.readStats()
        logger.info { "Started monitoring external file: $filePath" }

        monitorJob =
            scope.launch(Dispatchers.IO) {
                while (isActive) {
                    delay(intervalMillis)
                    val current = filePath.readStats()
                    val previous = lastStats!!
                    val changed = current.lastModified != previous.lastModified || current.size != previous.size
                    if (changed) {
                        if (internalSavePending) {
                            internalSavePending = false
                            lastStats = current
                            logger.debug { "Suppressed change from internal save for $filePath" }
                        } else {
                            logger.debug {
                                "Detected change in $filePath: " +
                                    "modified=${current.lastModified}, " +
                                    "size=${current.size}, " +
                                    "crc32=${current.crc32}"
                            }
                            _changeFlow.value = filePath
                            logger.info { "External file change reported for $filePath" }
                            lastStats = current
                        }
                    }
                }
            }
    }

    internal fun recordInternalSave() {
        val filePath = repo.serverDatPath
        val baseline = filePath.readStats()
        lastStats = baseline
        internalSavePending = false
        logger.debug { "Internal save baseline recorded for $filePath: $baseline" }
    }

    fun stop() {
        monitorJob?.cancel()
        monitorJob = null
        logger.info { "Stopped monitoring external file" }
    }

    internal fun willSaveInternally() {
        internalSavePending = true
    }
}

private val logger = KotlinLogging.logger {}

private fun Path.readStats(): FileStats {
    val attrs = Files.readAttributes(this, BasicFileAttributes::class.java)
    val crc =
        Files.newInputStream(this).use { stream ->
            BufferedInputStream(stream).use { bis ->
                val buffer = ByteArray(8192)
                val crc32 = CRC32()
                var bytesRead = bis.read(buffer)
                while (bytesRead >= 0) {
                    crc32.update(buffer, 0, bytesRead)
                    bytesRead = bis.read(buffer)
                }
                crc32.value
            }
        }
    return FileStats(attrs.lastModifiedTime(), attrs.size(), crc)
}
