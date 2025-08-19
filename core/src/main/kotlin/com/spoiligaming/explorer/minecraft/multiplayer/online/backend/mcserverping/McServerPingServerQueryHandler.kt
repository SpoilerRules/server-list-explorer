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

@file:OptIn(ExperimentalTime::class)

package com.spoiligaming.explorer.minecraft.multiplayer.online.backend.mcserverping

import br.com.azalim.mcserverping.MCPing
import br.com.azalim.mcserverping.MCPingOptions
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.IServerQueryHandler
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.McServerPingOnlineServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OfflineServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OnlineServerData
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.EOFException
import java.net.ConnectException
import java.net.InetAddress
import java.net.UnknownHostException
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal class McServerPingServerQueryHandler(
    private val serverAddress: String,
    private val cacheDelay: Duration,
    private val connectionTimeoutMillis: Int,
    private val socketTimeoutMillis: Int,
    private val forceQuery: Boolean,
    private val skipCache: Boolean,
) : IServerQueryHandler {
    private var cache: Cache? = null

    override suspend fun getServerData() =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now()

            val resolvedIp =
                try {
                    resolveToIp(serverAddress)
                } catch (_: UnknownHostException) {
                    logger.debug {
                        "Returning (from resolveToIp exception handler) for $serverAddress: " +
                            "OfflineServerData"
                    }
                    return@withContext OfflineServerData
                }

            if (!forceQuery && !skipCache &&
                cache?.serverIp == resolvedIp && (cache!!.timestamp + cacheDelay) > now
            ) {
                logger.info { "Cache hit for server IP $resolvedIp - returning cached data." }
                return@withContext cache!!.serverData
            }

            return@withContext runCatching {
                logger.info { "Fetching data for server $serverAddress..." }

                val pingResult =
                    MCPing.getPing(
                        MCPingOptions.builder()
                            .hostname(serverAddress)
                            .timeout(connectionTimeoutMillis)
                            .readTimeout(socketTimeoutMillis).build(),
                    )

                val motd = pingResult.description.text
                val versionInfo = pingResult.version.name
                val protocol = pingResult.version.protocol
                val onlinePlayers = pingResult.players.online
                val maxPlayers = pingResult.players.max
                val ping = pingResult.ping
                val icon = pingResult.favicon.removePrefix("data:image/png;base64,")

                val serverData =
                    McServerPingOnlineServerData(
                        motd = motd,
                        onlinePlayers = onlinePlayers,
                        maxPlayers = maxPlayers,
                        versionInfo = versionInfo,
                        ping = ping,
                        icon = icon,
                        protocolVersion = protocol,
                        ip = resolvedIp,
                    )

                if (!skipCache) {
                    cache = Cache(serverAddress, serverData, now)
                    logger.info {
                        "Successfully fetched and cached data for server $serverAddress."
                    }
                } else {
                    logger.info {
                        "Successfully fetched data for server $serverAddress " +
                            "(cache skipped)."
                    }
                }

                serverData
            }.getOrElse { e ->
                val result =
                    when (e) {
                        is ConnectException,
                        is UnknownHostException,
                        is EOFException,
                        -> OfflineServerData
                        else -> {
                            logger.info(e) { "Server $serverAddress is offline or unreachable." }
                            OfflineServerData
                        }
                    }
                logger.debug { "Returning (from exception handler) for $serverAddress: $result" }
                result
            }
        }

    private suspend fun resolveToIp(serverAddress: String) =
        withContext(Dispatchers.IO) {
            InetAddress.getByName(serverAddress).hostAddress
        }
}

private data class Cache(
    val serverIp: String,
    val serverData: OnlineServerData,
    val timestamp: Instant,
)

private val logger = KotlinLogging.logger {}
