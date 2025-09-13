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

package com.spoiligaming.explorer.minecraft.multiplayer

import com.spoiligaming.explorer.minecraft.common.IModeLoader
import com.spoiligaming.explorer.multiplayer.repository.ServerListRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

object ServerListModule : IModeLoader<ServerListRepository> {
    var repository: ServerListRepository? = null
        private set

    override suspend fun autoResolvePath() =
        withContext(Dispatchers.Default) {
            ServerListPaths.findDefault()
        }

    override suspend fun initialize(target: Path?) =
        withContext(Dispatchers.Default) {
            runCatching { ServerListPaths.validate(target) }
                .onFailure { e ->
                    logger.error(e) { "Validation failed for server list path: $target" }
                }.map {
                    ServerListRepository(target!!).also { it.load() }.also { repository = it }
                }
        }
}

private val logger = KotlinLogging.logger {}
