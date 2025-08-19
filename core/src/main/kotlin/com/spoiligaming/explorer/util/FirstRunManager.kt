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

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.file.Paths

object FirstRunChecker {
    private val configDir = Paths.get("config").toFile()
    private val markerFile = configDir.resolve("first-run.marker")

    private val _isFirstRun = MutableStateFlow(!markerFile.exists())
    val isFirstRun = _isFirstRun.asStateFlow()

    fun markFirstRunDone() {
        if (_isFirstRun.value.not()) return // already handled
        if (!configDir.exists()) configDir.mkdirs()

        markerFile.createNewFile()
        _isFirstRun.value = false
        logger.info { "Marked first run complete: ${markerFile.path}" }
    }
}

private val logger = KotlinLogging.logger {}
