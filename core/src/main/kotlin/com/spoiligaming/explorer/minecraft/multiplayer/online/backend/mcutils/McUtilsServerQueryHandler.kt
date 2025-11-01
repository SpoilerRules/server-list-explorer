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

package com.spoiligaming.explorer.minecraft.multiplayer.online.backend.mcutils

import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.IServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.IServerQueryHandler
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.McUtilsOnlineServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OfflineServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OnlineServerData
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.sockets.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.aliorpse.mcutils.modules.server.status.JavaServer
import tech.aliorpse.mcutils.utils.hostPortOf
import java.io.EOFException
import java.net.ConnectException
import java.net.InetAddress
import java.net.UnknownHostException
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal class McUtilsServerQueryHandler(
    private val serverAddress: String,
    private val cacheDelay: Duration,
    private val connectionTimeoutMillis: Int,
    private val socketTimeoutMillis: Int,
    private val skipCache: Boolean,
) : IServerQueryHandler {
    private var cache: Cache? = null

    override suspend fun getServerData(): IServerData =
        withContext(Dispatchers.IO) {
            val now = Clock.System.now()

            val resolvedIp =
                try {
                    resolveToIp(serverAddress)
                } catch (_: UnknownHostException) {
                    logger.debug {
                        "Returning (from resolveToIp exception handler) for $serverAddress: OfflineServerData"
                    }
                    return@withContext OfflineServerData
                }

            if (
                !skipCache &&
                cache?.serverIp == resolvedIp &&
                cache!!.timestamp + cacheDelay > now
            ) {
                logger.info { "Cache hit for server IP $resolvedIp - returning cached data." }
                return@withContext cache!!.serverData
            }

            return@withContext runCatching {
                logger.info { "Fetching data via mcutils for server $serverAddress." }

                val status =
                    JavaServer.getStatus(
                        hostPort = hostPortOf(serverAddress),
                        connectionTimeout = connectionTimeoutMillis,
                        readTimeout = socketTimeoutMillis,
                    )

                val motd = status.description.toSectionString()
                val versionInfo = status.version.name
                val protocol = status.version.protocol.toInt()
                val onlinePlayers = status.players.online
                val maxPlayers = status.players.max
                val ping = status.ping ?: -1L
                val icon = (status.favicon ?: "").removePrefix("data:image/png;base64,")
                val info =
                    status.players.sample
                        ?.mapNotNull { player -> player.name.takeIf { it.isNotBlank() } }
                        ?.takeIf { it.isNotEmpty() }
                        ?.joinToString("\n")
                val enforcesSecureChat = status.enforcesSecureChat

                val serverData =
                    McUtilsOnlineServerData(
                        motd = motd,
                        onlinePlayers = onlinePlayers,
                        maxPlayers = maxPlayers,
                        versionInfo = versionInfo,
                        ping = ping,
                        icon = icon,
                        protocolVersion = protocol,
                        ip = resolvedIp,
                        info = info,
                        secureChatEnforced = enforcesSecureChat,
                    )

                if (!skipCache) {
                    cache = Cache(serverIp = resolvedIp, serverData = serverData, timestamp = now)
                    logger.info { "Successfully fetched and cached data for server $serverAddress." }
                } else {
                    logger.info { "Successfully fetched data for server $serverAddress (cache skipped)." }
                }

                serverData
            }.getOrElse { e ->
                val result =
                    when (e) {
                        is SocketTimeoutException -> {
                            logger.info {
                                "Connection to server $serverAddress timed out " +
                                    "(resolved IP: $resolvedIp, host: ${hostPortOf(serverAddress)})"
                            }
                            OfflineServerData
                        }

                        is ConnectException -> {
                            logger.info {
                                "Connection to server $serverAddress was refused " +
                                    "(resolved IP: $resolvedIp, host: ${hostPortOf(serverAddress)})"
                            }
                            OfflineServerData
                        }

                        is EOFException -> {
                            logger.info {
                                "Server $serverAddress closed the connection unexpectedly " +
                                    "(resolved IP: $resolvedIp, host: ${hostPortOf(serverAddress)})"
                            }
                            OfflineServerData
                        }

                        else -> {
                            logger.error(e) {
                                "Unexpected exception while fetching data for $serverAddress" +
                                    "(resolved IP: $resolvedIp, host: ${hostPortOf(serverAddress)})"
                            }
                            throw e
                        }
                    }

                logger.debug { "Returning fallback result for $serverAddress due to ${e::class.simpleName}: $result" }
                result
            }
        }

    private suspend fun resolveToIp(host: String) =
        withContext(Dispatchers.IO) {
            InetAddress.getByName(host).hostAddress
        }
}

private data class Cache(
    val serverIp: String,
    val serverData: OnlineServerData,
    val timestamp: Instant,
)

private val logger = KotlinLogging.logger {}
