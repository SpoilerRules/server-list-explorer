package com.spoiligaming.explorer.server

import br.com.azalim.mcserverping.MCPing
import br.com.azalim.mcserverping.MCPingOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.Instant
import java.util.regex.Pattern

object ServerDataDelegate {
    data class ServerData(
        val normalizedServerVersion: String,
        val minimumServerProtocol: String,
        val ping: String,
        val maxPlayerCount: String,
        val onlinePlayerCount: String,
        val icon: String,
    )

    sealed class ServerDelegateResult {
        data class Success(val serverData: ServerData) : ServerDelegateResult()

        data object Failure : ServerDelegateResult()
    }

    private const val CACHE_DURATION_SECONDS = 30L
    private const val TIMEOUT_DURATION_MS = 30000L
    private var cache: Cache? = null

    private data class Cache(
        val serverAddress: String,
        val serverData: ServerData,
        val timestamp: Instant,
    )

    private val colorCodePattern = Pattern.compile("§[0-9a-fk-or]")

    private fun removeMinecraftColorCodes(text: String) =
        colorCodePattern.matcher(
            text,
        ).replaceAll("")

    suspend fun getServerData(
        serverAddress: String,
        forceRefresh: Boolean,
    ): ServerDelegateResult =
        withContext(Dispatchers.IO) {
            if (!forceRefresh &&
                cache?.serverAddress == serverAddress &&
                cache?.timestamp?.plusSeconds(CACHE_DURATION_SECONDS)?.isAfter(Instant.now()) ==
                true
            ) {
                return@withContext ServerDelegateResult.Success(cache!!.serverData)
            }

            runCatching {
                withTimeout(TIMEOUT_DURATION_MS) {
                    val serverData =
                        MCPing.getPing(MCPingOptions.builder().hostname(serverAddress).build())
                            .let {
                                ServerData(
                                    normalizedServerVersion =
                                        removeMinecraftColorCodes(it.version.name),
                                    minimumServerProtocol = it.version.protocol.toString(),
                                    ping = it.ping.toString(),
                                    maxPlayerCount = it.players.max.toString(),
                                    onlinePlayerCount = it.players.online.toString(),
                                    icon = it.favicon.toString(),
                                )
                            }
                    cache = Cache(serverAddress, serverData, Instant.now())
                    ServerDelegateResult.Success(serverData)
                }
            }
                .getOrElse { ServerDelegateResult.Failure }
        }

    suspend fun getServerIcon(serverAddress: String): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val favicon =
                    MCPing.getPing(MCPingOptions.builder().hostname(serverAddress).build())
                        .favicon

                favicon.removePrefix("data:image/png;base64,").takeIf {
                    favicon.startsWith("data:image/png;base64,")
                }
            }
                .getOrNull()
        }
}
