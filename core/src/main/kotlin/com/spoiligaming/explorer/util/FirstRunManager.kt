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

import com.spoiligaming.explorer.settings.util.AppStoragePaths
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.file.Files

object FirstRunManager {
    const val MARKER_NAME = "first-run.marker"

    private val markerFile
        get() = AppStoragePaths.firstRunConfigDir.resolve(MARKER_NAME)

    private val _isFirstRun = MutableStateFlow(!markerFile.exists())
    val isFirstRun = _isFirstRun.asStateFlow()

    fun markFirstRunDone() {
        if (_isFirstRun.value.not()) return // already handled

        runCatching {
            Files.createDirectories(AppStoragePaths.firstRunConfigDir.toPath())
            markerFile.createNewFile()
        }.onFailure { e ->
            logger.error(e) { "Failed to create first-run marker at ${markerFile.absolutePath}" }
        }.onSuccess {
            _isFirstRun.value = false
            logger.info { "First-run marker created at: ${markerFile.absolutePath}" }
        }
    }
}

private val logger = KotlinLogging.logger {}
