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

package com.spoiligaming.explorer.util

import com.spoiligaming.explorer.build.BuildConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object FirstRunManager {
    private const val APP_DIR = "ServerListExplorer"
    private const val MARKER_NAME = "first-run.marker"

    private val isPortableWindows
        get() =
            OSUtils.isWindows &&
                BuildConfig.DISTRIBUTION.contains("portable", ignoreCase = true)

    private val platformDir
        get() =
            when {
                OSUtils.isWindows -> windowsDir()
                OSUtils.isMacOS -> macDir()
                else -> linuxDir()
            }

    private val legacyDir = File("config")

    val configDir
        get() =
            if (isPortableWindows) {
                legacyDir
            } else {
                migrateLegacyMarkerIfNeeded()
                platformDir
            }

    private val markerFile
        get() = configDir.resolve(MARKER_NAME)

    private val _isFirstRun = MutableStateFlow(!markerFile.exists())
    val isFirstRun = _isFirstRun.asStateFlow()

    fun markFirstRunDone() {
        if (_isFirstRun.value.not()) return // already handled

        runCatching {
            Files.createDirectories(configDir.toPath())
            markerFile.createNewFile()
        }.onFailure { e ->
            logger.error(e) { "Failed to create first-run marker at ${markerFile.absolutePath}" }
        }.onSuccess {
            _isFirstRun.value = false
            logger.info { "First-run marker created at: ${markerFile.absolutePath}" }
        }
    }

    private fun migrateLegacyMarkerIfNeeded() {
        val legacyMarker = legacyDir.resolve(MARKER_NAME)
        if (!legacyMarker.exists()) return

        val target = platformDir
        val newMarker = target.resolve(MARKER_NAME)

        if (newMarker.exists()) {
            runCatching { legacyMarker.delete() }
            return
        }

        runCatching { Files.createDirectories(target.toPath()) }
            .onFailure { e ->
                logger.error(e) {
                    "Could not create directory for first-run marker: ${target.absolutePath}"
                }
                return
            }

        val moved =
            moveOrCopyFile(
                legacyMarker.toPath(),
                newMarker.toPath(),
            )

        if (moved) {
            logger.info { "Migrated first-run marker to ${newMarker.absolutePath}" }
        }
    }

    private fun moveOrCopyFile(
        src: Path,
        dest: Path,
    ) = runCatching {
        Files.createDirectories(dest.parent)
        Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING)
        true
    }.recoverCatching {
        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING)
        Files.deleteIfExists(src)
        true
    }.onFailure { e ->
        logger.error(e) { "Failed to migrate first-run marker from $src to $dest" }
    }.getOrDefault(false)

    private fun windowsDir(): File {
        val appData = System.getenv("APPDATA")?.takeIf { it.isNotBlank() }
        val base =
            appData?.let { File(it, APP_DIR) }
                ?: File(System.getProperty("user.home"), "AppData/Roaming/$APP_DIR")
        return base
    }

    private fun macDir() = File(System.getProperty("user.home"), "Library/Application Support/$APP_DIR")

    private fun linuxDir(): File {
        val xdg = System.getenv("XDG_CONFIG_HOME")?.takeIf { it.isNotBlank() }
        val base =
            xdg?.let { File(it, APP_DIR) }
                ?: File(System.getProperty("user.home"), ".config/$APP_DIR")
        return base
    }
}

private val logger = KotlinLogging.logger {}
