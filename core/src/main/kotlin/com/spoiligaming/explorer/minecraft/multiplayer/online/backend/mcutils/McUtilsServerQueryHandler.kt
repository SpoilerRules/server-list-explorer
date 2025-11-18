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

package com.spoiligaming.explorer.minecraft.multiplayer.online.backend.mcutils

import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.IServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.IServerQueryHandler
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.McUtilsOnlineServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OfflineServerData
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.sockets.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.aliorpse.mcutils.modules.server.status.JavaServer
import tech.aliorpse.mcutils.utils.hostPortOf
import java.io.EOFException
import java.net.ConnectException
import java.net.InetAddress
import java.net.SocketException
import java.net.UnknownHostException

internal class McUtilsServerQueryHandler(
    private val serverAddress: String,
    private val connectionTimeoutMillis: Int,
    private val socketTimeoutMillis: Int,
    private val socketAttempts: Int,
    private val eofAttempts: Int,
) : IServerQueryHandler {
    init {
        require(socketAttempts >= 1) { "socketAttempts must be >= 1 (was $socketAttempts)" }
        require(eofAttempts >= 1) { "eofAttempts must be >= 1 (was $eofAttempts)" }
    }

    override suspend fun getServerData(): IServerData =
        withContext(Dispatchers.IO) {
            val resolvedIp =
                resolveIpOrNull(serverAddress)
                    ?: return@withContext OfflineServerData

            val hostPort = hostPortOf(serverAddress)

            try {
                logger.info {
                    "Fetching data via mcutils for server $serverAddress " +
                        "(resolved IP: $resolvedIp, host: $hostPort)."
                }

                val status =
                    retry(
                        retryPolicy<SocketException>(
                            maxAttempts = socketAttempts,
                        ) { attempt, maxAttempts, cause ->
                            logRetry(
                                cause = cause,
                                resolvedIp = resolvedIp,
                                hostPort = hostPort,
                                attempt = attempt,
                                maxAttempts = maxAttempts,
                            )
                        },
                        retryPolicy<EOFException>(
                            maxAttempts = eofAttempts,
                        ) { attempt, maxAttempts, cause ->
                            logRetry(
                                cause = cause,
                                resolvedIp = resolvedIp,
                                hostPort = hostPort,
                                attempt = attempt,
                                maxAttempts = maxAttempts,
                            )
                        },
                    ) {
                        JavaServer.getStatus(
                            hostPort = hostPort,
                            connectionTimeout = connectionTimeoutMillis,
                            readTimeout = socketTimeoutMillis,
                        )
                    }

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
                ).also {
                    logger.info {
                        "Successfully fetched data for server $serverAddress " +
                            "(resolved IP: $resolvedIp, host: $hostPort)."
                    }
                }
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                handleException(e, resolvedIp, hostPort)
            }
        }

    private fun resolveIpOrNull(host: String): String? =
        try {
            resolveToIp(host)
        } catch (_: UnknownHostException) {
            logger.debug {
                "Returning (from resolveToIp exception handler) for $serverAddress: OfflineServerData"
            }
            null
        }

    private fun resolveToIp(host: String): String = InetAddress.getByName(host).hostAddress

    @Suppress("NOTHING_TO_INLINE")
    private inline fun <reified E : Throwable> logRetry(
        cause: E,
        resolvedIp: String,
        hostPort: Any?,
        attempt: Int,
        maxAttempts: Int,
    ) {
        val name = E::class.simpleName
        logger.info(cause) {
            "JavaServer.getStatus failed with $name for $serverAddress " +
                "(resolved IP: $resolvedIp, host: $hostPort, attempt ${attempt + 1} of $maxAttempts). Retrying..."
        }
    }

    private fun handleException(
        e: Throwable,
        resolvedIp: String,
        hostPort: Any?,
    ): IServerData {
        val result =
            when (e) {
                is SocketTimeoutException -> {
                    logger.info {
                        "Connection to server $serverAddress timed out " +
                            "(resolved IP: $resolvedIp, host: $hostPort)"
                    }
                    OfflineServerData
                }

                is ConnectException -> {
                    logger.info {
                        "Connection to server $serverAddress was refused " +
                            "(resolved IP: $resolvedIp, host: $hostPort)"
                    }
                    OfflineServerData
                }

                is SocketException -> {
                    logger.info {
                        "Socket error while connecting to $serverAddress: ${e.message} " +
                            "(resolved IP: $resolvedIp, host: $hostPort)"
                    }
                    OfflineServerData
                }

                is EOFException -> {
                    logger.info {
                        "Server $serverAddress closed the connection unexpectedly " +
                            "(resolved IP: $resolvedIp, host: $hostPort)"
                    }
                    OfflineServerData
                }

                else -> {
                    logger.error(e) {
                        "Unexpected exception while fetching data for $serverAddress " +
                            "(resolved IP: $resolvedIp, host: $hostPort)"
                    }
                    throw e
                }
            }

        logger.debug {
            "Returning fallback result for $serverAddress due to ${e::class.simpleName}: $result"
        }

        return result
    }
}

private val logger = KotlinLogging.logger {}
