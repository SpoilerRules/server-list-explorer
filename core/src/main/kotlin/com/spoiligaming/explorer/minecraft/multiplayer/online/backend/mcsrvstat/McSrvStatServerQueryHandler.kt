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

package com.spoiligaming.explorer.minecraft.multiplayer.online.backend.mcsrvstat

import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.IServerQueryHandler
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.McSrvStatRateLimitedServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OfflineServerData
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * `IServerQueryHandler` implementation powered by the public [https://api.mcsrvstat.us](https://api.mcsrvstat.us) service.
 */
internal class McSrvStatServerQueryHandler(
    private val serverAddress: String,
    private val client: HttpClient,
) : IServerQueryHandler {
    override suspend fun getServerData() =
        withContext(Dispatchers.IO) {
            try {
                val rawJson =
                    RequestHandler(serverAddress, client)
                        .fetchResponseBody()
                        .getOrThrow()

                if (!rawJson.contains("\"online\":true")) {
                    logger.warn { "Server is offline: $serverAddress" }
                    return@withContext OfflineServerData
                }

                val decoded = OnlineServerDataSerializer.decode(rawJson)
                OnlineServerDataSerializer.reduce(decoded)
            } catch (_: RequestHandler.RateLimitException) {
                McSrvStatRateLimitedServerData
            } catch (e: Exception) {
                logger.error(e) { "Failed to get server data for $serverAddress" }
                OfflineServerData
            }
        }
}

private val logger = KotlinLogging.logger {}
