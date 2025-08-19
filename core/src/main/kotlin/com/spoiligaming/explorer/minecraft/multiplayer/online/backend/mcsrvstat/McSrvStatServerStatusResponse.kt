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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class McSrvStatServerStatusResponse(
    val ip: String,
    val port: Long,
    val debug: DebugInfo,
    val motd: Motd,
    val players: Players,
    @SerialName("version")
    val versionInfo: String,
    val online: Boolean,
    val protocol: Protocol,
    val hostname: String,
    @SerialName("icon")
    val rawIcon: String,
    val info: Info? = null,
    @SerialName("eula_blocked")
    val eulaBlocked: Boolean,
) {
    val icon
        get() = rawIcon.removePrefix("data:image/png;base64,")
}

@Serializable
data class DebugInfo(
    val ping: Boolean,
    val query: Boolean,
    val bedrock: Boolean,
    val srv: Boolean,
    val querymismatch: Boolean,
    val ipinsrv: Boolean,
    val cnameinsrv: Boolean,
    val animatedmotd: Boolean,
    val cachehit: Boolean,
    val cachetime: Long,
    val cacheexpire: Long,
    val apiversion: Long,
    val dns: DnsInfo,
    val error: QueryError? = null,
)

@Serializable
data class DnsInfo(
    @SerialName("srv")
    val srv: List<DnsRecord> = emptyList(),
    @SerialName("srv_a")
    val srvA: List<DnsRecord> = emptyList(),
)

@Serializable
data class DnsRecord(
    val name: String,
    val type: String,
    @SerialName("class")
    val recordClass: String,
    val ttl: Long,
    val rdlength: Long,
    val rdata: String,
    val priority: Long? = null,
    val weight: Long? = null,
    val port: Long? = null,
    val target: String? = null,
    val typecovered: String? = null,
    val algorithm: Long? = null,
    val labels: Long? = null,
    val origttl: Long? = null,
    val sigexp: String? = null,
    val sigincep: String? = null,
    val keytag: Long? = null,
    val signname: String? = null,
    val signature: String? = null,
    val cname: String? = null,
    val address: String? = null,
)

@Serializable
data class QueryError(
    val query: String,
)

@Serializable
data class Motd(
    val raw: List<String> = emptyList(),
    val clean: List<String> = emptyList(),
    val html: List<String> = emptyList(),
)

@Serializable
data class Players(
    val online: Long,
    val max: Long,
)

@Serializable
data class Protocol(
    val version: Long,
    val name: String? = null,
)

@Serializable
data class Info(
    val raw: List<String> = emptyList(),
    val clean: List<String> = emptyList(),
    val html: List<String> = emptyList(),
)
