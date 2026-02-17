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

package com.spoiligaming.explorer.settings.util

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File

internal class SettingsFile<T>(
    fileName: String,
    private val serializer: KSerializer<T>,
    private val defaultValueProvider: () -> T,
) {
    private val json =
        Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

    private val settingsDir = AppStoragePaths.settingsDir
    private val settingsFile = settingsDir.resolve(fileName)

    val lastModifiedMillis
        get() = settingsFile.takeIf { it.exists() }?.lastModified()

    suspend fun read() =
        withContext(Dispatchers.IO) {
            val file = settingsFile

            logger.debug { "Attempting to read settings from: ${file.absolutePath}" }

            if (!file.exists()) {
                ensureDirExists(settingsDir)

                val defaultObj = defaultValueProvider()
                file.writeText(json.encodeToString(serializer, defaultObj))

                logger.info { "Created new settings file with default: $defaultObj" }
                return@withContext defaultObj
            }

            val raw =
                file.readText().also {
                    logger.debug { "Read raw JSON: ${it.take(100).replace("\n", "\\n")}..." }
                }

            check(raw.isNotBlank()) {
                "SettingsFile.read(): existing file is empty: ${file.absolutePath}"
            }

            val loaded = json.decodeFromString(serializer, raw)
            logger.info { "Loaded settings from disk: $loaded" }
            loaded
        }

    suspend fun write(
        data: T,
        onComplete: (() -> Unit)? = null,
    ) = withContext(Dispatchers.IO) {
        val file = settingsFile

        ensureDirExists(settingsDir)

        val serialized = json.encodeToString(serializer, data)
        file.writeText(serialized)
        logger.debug { "Saved settings to disk: $data" }
        onComplete?.invoke()
    }

    private fun ensureDirExists(dir: File) {
        if (!dir.exists()) {
            dir.mkdirs().also { created ->
                if (created) {
                    logger.info { "Created settings directory: ${dir.absolutePath}" }
                } else {
                    logger.warn { "Failed to create settings directory: ${dir.absolutePath}" }
                }
            }
        }
    }
}

private val logger = KotlinLogging.logger {}
