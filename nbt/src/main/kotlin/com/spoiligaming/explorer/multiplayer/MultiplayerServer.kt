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

package com.spoiligaming.explorer.multiplayer

import androidx.compose.runtime.Immutable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.io.encoding.Base64
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Immutable
data class MultiplayerServer(
    val id: Uuid = Uuid.random(),
    val name: String,
    val ip: String,
    val iconBase64: String? = null,
    val hidden: HiddenState = HiddenState.NotHidden,
    val acceptTextures: AcceptTexturesState = AcceptTexturesState.Prompt,
) {
    val iconBytes
        get() =
            iconBase64
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    runCatching { Base64.decode(it) }
                        .onFailure { e ->
                            logger.error(e) { "Failed to decode icon for '$name': ${e.message}" }
                        }
                        .getOrNull()
                }
                ?.takeIf { it.size <= MAX_ICON_SIZE_BYTES }
                ?: run {
                    iconBase64?.let {
                        logger.error {
                            "Icon for '$name' too large (${it.length} bytes), " +
                                "max is $MAX_ICON_SIZE_BYTES"
                        }
                    }
                    null
                }

    val iconStream get() = iconBytes?.inputStream()

    companion object {
        const val MAX_ICON_SIZE_BYTES = 96 * 1024
        val logger = KotlinLogging.logger {}
    }
}

// TODO: localize
enum class AcceptTexturesState(val displayName: String) {
    Enabled("Enabled"),
    Disabled("Disabled"),
    Prompt("Prompt"),
}

// TODO: localize
enum class HiddenState(val displayName: String) {
    Hidden("Hidden"),
    NotHidden("Not Hidden"),
}
