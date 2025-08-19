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

package com.spoiligaming.explorer

import com.spoiligaming.explorer.settings.manager.UniversalSettingsManager
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.nio.file.Files
import java.nio.file.Paths

class ResourceInitializer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        logger.info { "Initializing resources" }
        ensureLogsDirectoryExists()
        scope.launch {
            initializeSettings()
        }
        logger.info { "Resources initialized" }
    }

    private suspend fun initializeSettings() =
        supervisorScope {
            async { UniversalSettingsManager.loadAll() }.await()
        }

    private fun ensureLogsDirectoryExists() {
        Files.createDirectories(Paths.get("logs"))
    }
}

private val logger = KotlinLogging.logger {}
