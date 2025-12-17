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

package com.spoiligaming.explorer.minecraft.multiplayer.online

import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.McSrvStatRateLimitedServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OnlineServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OnlineServerDataResourceResult
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.mcsrvstat.McSrvStatServerQueryHandler
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.mcutils.McUtilsServerQueryHandler
import com.spoiligaming.explorer.settings.model.ServerQueryMethod
import com.spoiligaming.explorer.settings.model.ServerQueryMethodConfigurations
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.flow.flow

class OnlineServerDataFacade(
    private val serverAddress: String,
    private val queryMode: ServerQueryMethod,
    private val configurations: ServerQueryMethodConfigurations,
) {
    fun serverDataFlow() =
        flow {
            emit(OnlineServerDataResourceResult.Loading)

            val handler = buildQueryHandler(queryMode)

            runCatching { handler.getServerData() }
                .onSuccess { data ->
                    when (data) {
                        is OnlineServerData -> {
                            logger.info { "Fetched server data for $serverAddress" }
                            emit(OnlineServerDataResourceResult.Success(data))
                        }

                        McSrvStatRateLimitedServerData -> {
                            logger.warn { "Server $serverAddress is rate-limited." }
                            emit(
                                OnlineServerDataResourceResult.RateLimited(
                                    McSrvStatRateLimitedServerData,
                                ),
                            )
                        }

                        else -> {
                            logger.warn { "Server $serverAddress is offline or unreachable: $data" }
                            emit(
                                OnlineServerDataResourceResult.Error(
                                    Throwable("Server is offline or unreachable."),
                                ),
                            )
                        }
                    }
                }.onFailure { e ->
                    logger.error(e) { "Failed to fetch server data for $serverAddress" }
                    emit(OnlineServerDataResourceResult.Error(e))
                }
        }

    private fun buildQueryHandler(method: ServerQueryMethod) =
        when (method) {
            ServerQueryMethod.McSrvStat -> {
                val timeouts = configurations.mcSrvStat.timeouts
                McSrvStatServerQueryHandler(
                    serverAddress = serverAddress,
                    client =
                        HttpClient(CIO) {
                            install(HttpTimeout) {
                                connectTimeoutMillis = timeouts.connectionTimeoutMillis
                                requestTimeoutMillis =
                                    timeouts.connectionTimeoutMillis + REQUEST_TIMEOUT_BUFFER_MILLIS
                                socketTimeoutMillis = timeouts.responseTimeoutMillis
                            }
                            engine {
                                endpoint {
                                    connectTimeout = timeouts.connectionTimeoutMillis
                                    keepAliveTime =
                                        (timeouts.connectionTimeoutMillis + REQUEST_TIMEOUT_BUFFER_MILLIS) * 2
                                }
                            }
                        },
                )
            }

            ServerQueryMethod.McUtils -> {
                val timeoutMillis = configurations.mcUtils.timeouts.timeoutMillis
                McUtilsServerQueryHandler(
                    serverAddress = serverAddress,
                    enableSrv = configurations.mcUtils.options.enableSrvLookups,
                    timeoutMillis = timeoutMillis,
                    socketAttempts = DEFAULT_SOCKET_ATTEMPTS,
                    eofAttempts = DEFAULT_EOF_ATTEMPTS,
                )
            }
        }

    companion object {
        private const val REQUEST_TIMEOUT_BUFFER_MILLIS = 30_000L

        private const val DEFAULT_SOCKET_ATTEMPTS = 5
        private const val DEFAULT_EOF_ATTEMPTS = 3
    }
}

private val logger = KotlinLogging.logger {}
