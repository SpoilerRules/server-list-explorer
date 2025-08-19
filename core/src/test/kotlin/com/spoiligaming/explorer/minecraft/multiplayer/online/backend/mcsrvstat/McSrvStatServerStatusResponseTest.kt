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

import kotlin.test.Test
import kotlin.test.assertEquals

internal class McSrvStatServerStatusResponseTest {
    private fun responseWithIcon(rawIcon: String) =
        McSrvStatServerStatusResponse(
            ip = "1.6.9",
            port = 25565,
            debug =
                DebugInfo(
                    ping = false,
                    query = false,
                    bedrock = false,
                    srv = false,
                    querymismatch = false,
                    ipinsrv = false,
                    cnameinsrv = false,
                    animatedmotd = false,
                    cachehit = false,
                    cachetime = 4,
                    cacheexpire = 2,
                    apiversion = 0,
                    dns = DnsInfo(),
                    error = null,
                ),
            motd = Motd(),
            players = Players(online = 0, max = 20),
            versionInfo = "4.20",
            online = true,
            protocol = Protocol(version = 764),
            hostname = "noballs727",
            rawIcon = rawIcon,
            info = null,
            eulaBlocked = false,
        )

    @Test
    fun `icon strips data URL base64 prefix`() {
        val raw = "data:image/png;base64,QUJDREVGRw=="
        val response = responseWithIcon(raw)
        assertEquals("QUJDREVGRw==", response.icon)
    }

    @Test
    fun `icon returns raw icon unchanged when no prefix`() {
        val raw = "QUJDREVGRw=="
        val response = responseWithIcon(raw)
        assertEquals(raw, response.icon)
    }
}
