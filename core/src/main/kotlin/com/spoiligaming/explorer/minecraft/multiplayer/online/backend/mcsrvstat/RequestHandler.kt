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

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers

private val logger = KotlinLogging.logger {}

internal class RequestHandler(
    serverAddress: String,
    private val client: HttpClient,
) {
    companion object {
        private const val BASE_URL = "https://api.mcsrvstat.us/3"
    }

    init {
        require(serverAddress.isNotBlank()) { "Server address must not be blank" }
    }

    private val serverAddress = serverAddress.trim()

    internal class RateLimitException : IllegalStateException("Rate limit exceeded for MCSrvStatus")

    suspend fun fetchResponseBody(): Result<String> {
        val url = "$BASE_URL/$serverAddress"
        logger.debug { "Requesting server status at $url" }

        return runCatching {
            val response =
                client.get(url) {
                    headers {
                        append(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36",
                        )
                        append("Accept", "application/json")
                    }
                }

            when (response.status) {
                HttpStatusCode.TooManyRequests -> throw RateLimitException()
                HttpStatusCode.OK -> {
                    val body = response.bodyAsText()
                    check(body.isNotBlank()) { "Response body is empty" }
                    body
                }

                else -> error("Unexpected HTTP status: ${response.status.value}")
            }
        }.onFailure { e ->
            logger.error(e) { "Failed to fetch status for $serverAddress" }
        }
    }
}
