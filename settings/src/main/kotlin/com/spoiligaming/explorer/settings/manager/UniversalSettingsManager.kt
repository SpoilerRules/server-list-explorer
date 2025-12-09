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

package com.spoiligaming.explorer.settings.manager

import com.spoiligaming.explorer.settings.util.SettingsFile
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.properties.ReadOnlyProperty

class UniversalSettingsManager<T : Any>(
    private val fileName: String,
    serializer: KSerializer<T>,
    private val defaultValueProvider: () -> T,
) : SettingsManager<T> {
    private val logger = KotlinLogging.logger(fileName.replace(".json", "Manager"))
    private val file = SettingsFile(fileName, serializer, defaultValueProvider)

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val _settingsFlow: MutableStateFlow<T>
    val settingsFlow get() = _settingsFlow

    @Volatile
    private var cachedSettings: T

    @Volatile
    private var lastLoadedTimestamp: Long? = null

    private val mutex = Mutex()

    init {
        val initial =
            runBlocking {
                mutex.withLock {
                    runCatching { file.read() }
                        .onFailure { e ->
                            logger.error(e) { "Could not read $fileName. Writing defaults." }
                            runCatching { file.write(defaultValueProvider()) }
                                .onFailure { e ->
                                    logger.error(e) { "Could not write defaults to $fileName." }
                                }
                        }.getOrElse {
                            defaultValueProvider()
                        }
                }
            }

        cachedSettings = initial
        _settingsFlow = MutableStateFlow(initial)
        lastLoadedTimestamp = file.lastModifiedMillis

        logger.debug { "Ready with settings from $fileName: $initial" }
    }

    override suspend fun loadSettings() =
        mutex.withLock {
            val cachedTimestamp = lastLoadedTimestamp
            val fileTimestamp = file.lastModifiedMillis
            if (cachedTimestamp != null && fileTimestamp != null && cachedTimestamp >= fileTimestamp) {
                logger.debug { "Skipping reload for $fileName; cached settings are up to date." }
                return@withLock cachedSettings
            }

            logger.debug { "Loading $fileName." }
            val loaded =
                runCatching { file.read() }
                    .getOrElse { e ->
                        logger.error(e) { "Could not load $fileName. Using defaults." }
                        defaultValueProvider().also {
                            runCatching { file.write(it) }
                                .onFailure { e ->
                                    logger.error(e) { "Could not write defaults to $fileName." }
                                }
                        }
                    }
            cachedSettings = loaded
            _settingsFlow.value = loaded
            lastLoadedTimestamp = file.lastModifiedMillis
            logger.debug { "Loaded settings from $fileName: $loaded" }
            loaded
        }

    override fun saveSettings(
        settings: T,
        onComplete: (() -> Unit)?,
    ) {
        scope.launch {
            mutex.withLock {
                logger.debug { "Saving to $fileName: $settings" }
                runCatching { file.write(settings) }
                    .onSuccess {
                        cachedSettings = settings
                        _settingsFlow.value = settings
                        lastLoadedTimestamp = file.lastModifiedMillis
                        logger.debug { "Saved to $fileName: $settings" }
                        onComplete?.invoke()
                    }.onFailure { e ->
                        logger.error(e) { "Could not save to $fileName." }
                    }
            }
        }
    }

    fun updateSettings(updater: (T) -> T) =
        scope.launch {
            mutex.withLock {
                val oldSettings = _settingsFlow.value
                val updated = updater(oldSettings)
                logger.debug { "Updating $fileName from $oldSettings to $updated" }

                runCatching { file.write(updated) }
                    .onSuccess {
                        cachedSettings = updated
                        _settingsFlow.value = updated
                        lastLoadedTimestamp = file.lastModifiedMillis
                        logger.debug { "Updated $fileName to $updated" }
                    }.onFailure { e ->
                        logger.error(e) { "Could not update $fileName." }
                    }
            }
        }

    override fun getCachedSettings() = cachedSettings

    companion object {
        internal val managers = mutableMapOf<String, UniversalSettingsManager<*>>()

        @Suppress("UNCHECKED_CAST")
        internal inline operator fun <reified T : Any> invoke(
            fileName: String,
            serializer: KSerializer<T> = serializer(),
            noinline defaultValueProvider: () -> T,
        ) = createDelegate(fileName, serializer, defaultValueProvider)

        @Suppress("UNCHECKED_CAST")
        internal inline operator fun <reified T : Any> invoke(
            noinline defaultValueProvider: () -> T,
            noinline fileNameProvider: () -> String,
        ) = createDelegate(fileNameProvider(), serializer(), defaultValueProvider)

        @Suppress("UNCHECKED_CAST")
        internal fun <T : Any> createDelegate(
            fileName: String,
            serializer: KSerializer<T>,
            defaultValueProvider: () -> T,
        ) = ReadOnlyProperty<Any?, UniversalSettingsManager<T>> { _, _ ->
            managers.getOrPut(fileName) {
                UniversalSettingsManager(fileName, serializer, defaultValueProvider)
            } as UniversalSettingsManager<T>
        }

        suspend fun loadAll() {
            managers.values.forEach { manager ->
                @Suppress("UNCHECKED_CAST")
                (manager as? UniversalSettingsManager<Any>)?.loadSettings()
            }
        }
    }
}
