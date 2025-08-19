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

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class RequestHandlerTest {
    private fun clientResponding(
        status: HttpStatusCode,
        body: String = "{}",
        captureUrl: (String) -> Unit = {},
    ): HttpClient {
        val engine =
            MockEngine { request ->
                captureUrl(request.url.toString())
                val content = if (status == HttpStatusCode.OK) body else ""
                respond(content, status = status)
            }
        return HttpClient(engine)
    }

    @Test
    fun `returns success on 200 OK with non-empty body`() =
        runBlocking {
            val client = clientResponding(HttpStatusCode.OK, body = """{"ok":true}""")
            val handler = RequestHandler("hypixel.net", client)
            val result = handler.fetchResponseBody()
            assertTrue(result.isSuccess, "Expected success for 200 with non-empty body")
            assertEquals("""{"ok":true}""", result.getOrNull())
        }

    @Test
    fun `fails with IllegalStateException on 200 OK with empty body`() =
        runBlocking {
            val client = clientResponding(HttpStatusCode.OK, body = "")
            val handler = RequestHandler("hypixel.net", client)
            val result = handler.fetchResponseBody()
            assertTrue(result.isFailure, "Expected failure for empty 200 body")
            val ex = result.exceptionOrNull()
            assertIs<IllegalStateException>(ex)
            assertEquals(ex.message?.contains("Response body is empty"), true)
        }

    @Test
    fun `fails with RateLimitException on 429 Too Many Requests`() {
        runBlocking {
            val client = clientResponding(HttpStatusCode.TooManyRequests)
            val handler = RequestHandler("example.org", client)
            val result = handler.fetchResponseBody()
            assertTrue(result.isFailure, "Expected failure for 429")
            assertIs<RequestHandler.RateLimitException>(result.exceptionOrNull())
        }
    }

    @Test
    fun `fails with IllegalStateException containing status on 500 Internal Server Error`() =
        runBlocking {
            val client = clientResponding(HttpStatusCode.InternalServerError)
            val handler = RequestHandler("example.org", client)
            val result = handler.fetchResponseBody()
            assertTrue(result.isFailure, "Expected failure for 500")
            val ex = result.exceptionOrNull()
            assertIs<IllegalStateException>(ex)
            assertEquals(ex.message?.contains("Unexpected HTTP status: 500"), true)
        }

    @Test
    fun `fails with IllegalStateException containing status on 404 Not Found`() =
        runBlocking {
            val client = clientResponding(HttpStatusCode.NotFound)
            val handler = RequestHandler("example.org", client)
            val result = handler.fetchResponseBody()
            assertTrue(result.isFailure, "Expected failure for 404")
            val ex = result.exceptionOrNull()
            assertIs<IllegalStateException>(ex)
            assertEquals(ex.message?.contains("Unexpected HTTP status: 404"), true)
        }

    @Test
    fun `trims server address and requests expected URL`() =
        runBlocking {
            var seenUrl: String? = null
            val client = clientResponding(HttpStatusCode.OK, body = "x") { url -> seenUrl = url }
            val handler = RequestHandler("   hypixel.net   ", client)
            val result = handler.fetchResponseBody()
            assertTrue(result.isSuccess)
            assertEquals("https://api.mcsrvstat.us/3/hypixel.net", seenUrl)
        }

    @Test
    fun `constructor throws IllegalArgumentException for blank server address`() {
        val ex =
            assertFailsWith<IllegalArgumentException> {
                RequestHandler("   ", HttpClient(MockEngine { respond("x") }))
            }
        assertEquals("Server address must not be blank", ex.message)
    }
}
