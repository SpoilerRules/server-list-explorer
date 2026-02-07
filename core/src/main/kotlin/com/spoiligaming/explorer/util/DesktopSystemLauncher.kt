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

package com.spoiligaming.explorer.util

import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

object DesktopSystemLauncher {
    private val logger = KotlinLogging.logger {}
    private const val WINDOWS_EXPLORER_EXE = "explorer.exe"
    private const val WINDOWS_SELECT_SWITCH = "/select,"

    fun openUrl(url: String): Result<Unit> {
        logger.debug { "Opening URL: $url" }
        val uri =
            try {
                URI(url)
            } catch (e: URISyntaxException) {
                return Result.failure(IllegalArgumentException("Invalid URL: $url", e))
            }

        val failures = ArrayList<Throwable>(2)

        if (Desktop.isDesktopSupported()) {
            runCatching {
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    logger.debug { "Opening URL via Desktop.browse: $uri" }
                    desktop.browse(uri)
                    return Result.success(Unit)
                }
            }.onFailure { e ->
                logger.warn(e) { "Desktop.browse failed for URL: $url" }
                failures.add(e)
            }
        }

        val command =
            when {
                OSUtils.isWindows -> listOf("rundll32", "url.dll,FileProtocolHandler", url)
                OSUtils.isMacOS -> listOf("open", url)
                else -> listOf("xdg-open", url)
            }

        runCatching {
            logger.debug { "Opening URL via command: ${command.joinToString(" ")}" }
            ProcessBuilder(command).start()
            Unit
        }.onSuccess {
            logger.debug { "URL open command launched successfully for: $url" }
            return Result.success(Unit)
        }.onFailure { e ->
            logger.warn(e) { "URL open command failed for: $url" }
            failures.add(e)
        }

        return Result.failure(failures.toFailure("Unable to open URL: $url"))
    }

    /**
     * Opens the system file manager and attempts to highlight [path].
     *
     * On platforms without a reliable "reveal" command, this falls back to opening the parent directory.
     */
    fun revealInFileManager(path: Path): Result<Unit> {
        val selection = path.canonicalize()
        logger.debug { "Revealing path in file manager: $selection" }
        if (!Files.exists(selection)) return Result.failure(NoSuchFileException(selection.toString()))

        val failures = ArrayList<Throwable>(3)

        if (Desktop.isDesktopSupported()) {
            runCatching {
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
                    logger.debug { "Revealing path via Desktop.browseFileDirectory: $selection" }
                    desktop.browseFileDirectory(selection.toFile())
                    return Result.success(Unit)
                }
            }.onFailure { e ->
                logger.warn(e) { "Desktop.browseFileDirectory failed for path: $selection" }
                failures.add(e)
            }
        }

        runCatching {
            when {
                OSUtils.isWindows -> revealWindows(selection)
                OSUtils.isMacOS -> revealMac(selection)
                else -> openDirectoryInternal(selection.parent ?: selection)
            }
        }.onSuccess {
            logger.debug { "Reveal command launched successfully for path: $selection" }
            return Result.success(Unit)
        }.onFailure { e ->
            logger.warn(e) { "Reveal command failed for path: $selection" }
            failures.add(e)
        }

        return Result.failure(failures.toFailure("Unable to reveal in file manager: $selection"))
    }

    /**
     * Opens the system file manager at [path].
     *
     * If [path] is a file, opens its parent directory.
     */
    fun openInFileManager(path: Path): Result<Unit> {
        val resolved = path.canonicalize()
        logger.debug { "Opening in file manager for path: $resolved" }

        val targetDirectory =
            when {
                Files.isDirectory(resolved) -> resolved
                resolved.parent != null && Files.exists(resolved.parent) -> resolved.parent
                Files.exists(resolved) -> resolved
                else -> return Result.failure(NoSuchFileException(resolved.toString()))
            }

        return runCatching { openDirectoryInternal(targetDirectory) }
            .onFailure { e -> logger.warn(e) { "Open in file manager failed for path: $targetDirectory" } }
    }

    private fun openDirectoryInternal(directory: Path) {
        if (!Files.exists(directory)) throw NoSuchFileException(directory.toString())

        if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                logger.debug { "Opening directory via Desktop.open: $directory" }
                desktop.open(directory.toFile())
                return
            }
        }

        val command =
            when {
                OSUtils.isWindows -> listOf(WINDOWS_EXPLORER_EXE, directory.toString())
                OSUtils.isMacOS -> listOf("open", directory.toString())
                else -> listOf("xdg-open", directory.toString())
            }

        logger.debug { "Opening directory via command: ${command.joinToString(" ")}" }
        val process = ProcessBuilder(command).start()
        if (!process.isAlive) throw IOException("Failed to launch file manager command: ${command.joinToString(" ")}")
    }

    private fun revealWindows(selection: Path) {
        val command = listOf(WINDOWS_EXPLORER_EXE, WINDOWS_SELECT_SWITCH, selection.toString())
        logger.debug { "Revealing path via Windows Explorer command: ${command.joinToString(" ")}" }
        ProcessBuilder(command).start()
    }

    private fun revealMac(selection: Path) {
        logger.debug { "Revealing path via macOS command: open -R $selection" }
        ProcessBuilder("open", "-R", selection.toString()).start()
    }
}

private fun List<Throwable>.toFailure(message: String): IOException {
    val root = IOException(message)
    forEach { root.addSuppressed(it) }
    return root
}
