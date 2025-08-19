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

@file:OptIn(ExperimentalUuidApi::class)

package com.spoiligaming.explorer.multiplayer.datasource

import com.spoiligaming.explorer.multiplayer.AcceptTexturesState
import com.spoiligaming.explorer.multiplayer.HiddenState
import com.spoiligaming.explorer.multiplayer.MultiplayerServer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.querz.nbt.io.NBTUtil
import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.ListTag
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import kotlin.uuid.ExperimentalUuidApi

internal class FileServerListDataSource(
    private val serverDatPath: Path,
    private val compressed: Boolean,
) {
    private val cachedRoot = AtomicReference<CompoundTag?>()

    suspend fun loadServers() =
        withContext(Dispatchers.IO) {
            logger.debug { "Loading servers from $serverDatPath" }
            val root =
                runCatching {
                    val read = readRoot()
                    checkNotNull(read.getListTag("servers")) { "Missing 'servers' tag" }
                    read
                }.getOrElse { e ->
                    logger.info(e) { "Initializing empty server list" }
                    initEmptyRoot()
                }
            cachedRoot.set(root)
            extractServers(root)
        }

    private fun readRoot(): CompoundTag {
        val namedTag = NBTUtil.read(serverDatPath.toFile(), compressed)
        val tag = namedTag.tag
        check(tag is CompoundTag) { "Root tag is not a CompoundTag" }
        return tag
    }

    private fun initEmptyRoot() =
        CompoundTag().apply {
            put("servers", ListTag(CompoundTag::class.java))
        }.also { newRoot ->
            NBTUtil.write(newRoot, serverDatPath.toFile(), compressed)
        }

    @Suppress("UNCHECKED_CAST")
    private fun extractServers(root: CompoundTag) =
        (root.getListTag("servers") as? ListTag<CompoundTag>)
            ?.map { entry -> entry.toMultiplayerServerEntry() }
            ?.toMutableList()
            ?: mutableListOf()

    @Suppress("UNCHECKED_CAST")
    suspend fun removeFieldAtIndex(
        index: Int,
        field: String,
    ) = withContext(Dispatchers.IO) {
        val root = cachedRoot.get() ?: readRoot().also { cachedRoot.set(it) }
        val serversTag =
            root.getListTag("servers")
                .also { require(it is ListTag<*>) { "No 'servers' list found in NBT" } }
                as ListTag<CompoundTag>

        require(index in 0 until serversTag.size()) {
            "Index $index is out of bounds (must be between 0 and ${serversTag.size() - 1})"
        }

        serversTag[index].remove(field)
        NBTUtil.write(root, serverDatPath.toFile(), compressed)
        cachedRoot.set(root)
    }

    suspend fun saveServers(servers: List<MultiplayerServer>) =
        withContext(Dispatchers.IO) {
            logger.debug { "Saving ${servers.size} servers to $serverDatPath" }
            val root =
                cachedRoot.updateAndGet { current ->
                    (current ?: CompoundTag()).apply {
                        put("servers", toListTag(servers))
                    }
                }
            NBTUtil.write(root, serverDatPath.toFile(), compressed)
        }

    private fun toListTag(servers: List<MultiplayerServer>) =
        ListTag(CompoundTag::class.java).apply {
            servers.forEach { add(it.toCompound()) }
        }

    private fun CompoundTag.toMultiplayerServerEntry() =
        MultiplayerServer(
            name = getString("name"),
            ip = getString("ip"),
            iconBase64 = getString("icon").takeIf { containsKey("icon") },
            hidden =
                if (getByte(
                        "hidden",
                    ).toBoolean()
                ) {
                    HiddenState.Hidden
                } else {
                    HiddenState.NotHidden
                },
            acceptTextures =
                if (containsKey("acceptTextures")) {
                    when (getByte("acceptTextures").toInt()) {
                        1 -> AcceptTexturesState.Enabled
                        0 -> AcceptTexturesState.Disabled
                        else -> AcceptTexturesState.Prompt
                    }
                } else {
                    AcceptTexturesState.Prompt
                },
        )

    private fun MultiplayerServer.toCompound() =
        CompoundTag().apply {
            putString("name", name)
            putString("ip", ip)
            iconBase64?.let { putString("icon", it) }
            putByte("hidden", hidden.toByte())
            when (acceptTextures) {
                AcceptTexturesState.Enabled -> putByte("acceptTextures", 1)
                AcceptTexturesState.Disabled -> putByte("acceptTextures", 0)
                AcceptTexturesState.Prompt -> { // omit tag so default = Prompt
                }
            }
        }
}

private fun HiddenState.toByte(): Byte = if (this == HiddenState.Hidden) 1 else 0

private fun Byte.toBoolean() = this.toInt() != 0

private val logger = KotlinLogging.logger {}
