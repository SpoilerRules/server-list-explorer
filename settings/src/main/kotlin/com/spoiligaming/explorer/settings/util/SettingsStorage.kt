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

package com.spoiligaming.explorer.settings.util

import com.spoiligaming.explorer.build.BuildConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicBoolean

internal object SettingsStorage {
    private const val APP_DIR = "ServerListExplorer"

    private val migrated = AtomicBoolean(false)

    private val isPortableWindows
        get() = isWindows && BuildConfig.DISTRIBUTION.contains("portable", ignoreCase = true)

    private val osName
        get() = System.getProperty("os.name")?.lowercase().orEmpty()

    private val isWindows
        get() = osName.contains("win")

    private val isMac
        get() = osName.contains("mac")

    val settingsDir
        get() =
            if (isPortableWindows) {
                legacyDir
            } else {
                migrateLegacyIfNeeded()
                platformDir
            }

    val legacyDir
        get() = File("config")

    private val platformDir
        get() =
            when {
                isWindows -> windowsDir()
                isMac -> macDir()
                else -> linuxDir()
            }

    private fun windowsDir(): File {
        val appData = System.getenv("APPDATA")?.takeIf { it.isNotBlank() }
        val base =
            appData?.let { File(it, APP_DIR) }
                ?: File(System.getProperty("user.home"), "AppData/Roaming/$APP_DIR")
        return base.resolve("config")
    }

    private fun macDir() = File(System.getProperty("user.home"), "Library/Application Support/$APP_DIR/config")

    private fun linuxDir(): File {
        val xdg = System.getenv("XDG_CONFIG_HOME")?.takeIf { it.isNotBlank() }
        val base =
            xdg?.let { File(it, APP_DIR) }
                ?: File(System.getProperty("user.home"), ".config/$APP_DIR")
        return base.resolve("config")
    }

    private fun migrateLegacyIfNeeded() {
        if (!migrated.compareAndSet(false, true)) return

        val legacy = legacyDir
        if (!legacy.exists() || !legacy.isDirectory) {
            logger.debug { "No legacy settings directory found at ${legacy.absolutePath}" }
            return
        }

        val files =
            legacy
                .listFiles()
                ?.asSequence()
                ?.filter { it.isFile }
                ?.filter { it.extension.equals("json", ignoreCase = true) }
                ?.toList()
                .orEmpty()

        if (files.isEmpty()) {
            logger.info { "Legacy settings directory is empty at ${legacy.absolutePath}" }
            return
        }

        val target = platformDir
        runCatching { Files.createDirectories(target.toPath()) }
            .onFailure { e ->
                logger.error(e) { "Could not create new settings directory at ${target.absolutePath}" }
                return
            }

        var movedAny = false

        files.forEach { src ->
            if (src.length() == 0L) {
                logger.warn { "Skipping empty legacy settings file: ${src.absolutePath}" }
                return@forEach
            }

            val dest = target.resolve(src.name)

            if (dest.exists() && dest.length() > 0L) {
                logger.debug { "New settings file already exists, skipping legacy: ${dest.absolutePath}" }
                return@forEach
            }

            movedAny = moveOrCopyFile(src.toPath(), dest.toPath()) || movedAny
        }

        if (movedAny) {
            logger.info {
                "Migrated settings from ${legacy.absolutePath} to ${target.absolutePath}"
            }
        } else {
            logger.debug { "No legacy settings files required migration." }
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
        logger.error(e) { "Failed to migrate settings file from $src to $dest" }
    }.getOrDefault(false)
}

private val logger = KotlinLogging.logger {}
