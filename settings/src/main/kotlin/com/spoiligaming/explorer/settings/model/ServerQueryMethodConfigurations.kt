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

package com.spoiligaming.explorer.settings.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerQueryMethodConfigurations(
    @SerialName("mc_srv_status")
    val mcSrvStat: McSrvStatusQueryConfiguration = McSrvStatusQueryConfiguration(),
    @SerialName("mc_utils")
    val mcUtils: McUtilsQueryConfiguration = McUtilsQueryConfiguration(),
)

sealed interface QueryMethodRequestKey

data class McSrvStatRequestKey(
    val connectTimeoutMillis: Long,
    val socketTimeoutMillis: Long,
) : QueryMethodRequestKey

data class McUtilsRequestKey(
    val timeoutMillis: Long,
    val enableSrvLookups: Boolean,
) : QueryMethodRequestKey

interface ServerQueryMethodConfig {
    val requestKey: QueryMethodRequestKey
}

@Serializable
data class McSrvStatusQueryConfiguration(
    @SerialName("timeouts")
    val timeouts: McSrvStatusTimeouts = McSrvStatusTimeouts(),
) : ServerQueryMethodConfig {
    override val requestKey
        get() =
            McSrvStatRequestKey(
                connectTimeoutMillis = timeouts.connectionTimeoutMillis,
                socketTimeoutMillis = timeouts.responseTimeoutMillis,
            )
}

@Serializable
data class McUtilsQueryConfiguration(
    @SerialName("timeouts")
    val timeouts: McUtilsTimeouts = McUtilsTimeouts(),
    @SerialName("options")
    val options: McUtilsQueryOptions = McUtilsQueryOptions(),
) : ServerQueryMethodConfig {
    override val requestKey
        get() =
            McUtilsRequestKey(
                timeoutMillis = timeouts.timeoutMillis,
                enableSrvLookups = options.enableSrvLookups,
            )
}

@Serializable
data class McSrvStatusTimeouts(
    @SerialName("connection_timeout_ms")
    val connectionTimeoutMillis: Long = 120_000L,
    @SerialName("response_timeout_ms")
    val responseTimeoutMillis: Long = 15_000L,
)

@Serializable
data class McUtilsTimeouts(
    @SerialName("timeout_ms")
    val timeoutMillis: Long = 45_000L,
)

@Serializable
data class McUtilsQueryOptions(
    @SerialName("enable_srv")
    val enableSrvLookups: Boolean = false,
)

fun ServerQueryMethodConfigurations.requestKeyFor(method: ServerQueryMethod): QueryMethodRequestKey =
    when (method) {
        ServerQueryMethod.McSrvStat -> mcSrvStat.requestKey
        ServerQueryMethod.McUtils -> mcUtils.requestKey
    }
