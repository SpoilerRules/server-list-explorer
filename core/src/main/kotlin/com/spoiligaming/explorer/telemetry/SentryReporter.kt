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

@file:OptIn(ExperimentalAtomicApi::class)

package com.spoiligaming.explorer.telemetry

import com.spoiligaming.explorer.build.BuildConfig
import com.spoiligaming.explorer.settings.manager.privacySettingsManager
import com.spoiligaming.explorer.util.OSUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import io.sentry.Sentry
import io.sentry.SentryLevel
import java.io.File
import java.nio.file.Files
import kotlin.concurrent.atomics.ExperimentalAtomicApi

object SentryReporter {
    private const val APP_DIR = "ServerListExplorer"
    private const val SENTRY_DIR_NAME = "sentry"
    private const val SENTRY_DSN =
        "https://66d802aaa18a55b5238d395522130954@o4510539351982080.ingest.de.sentry.io/4510539356831824"

    private val isTelemetryEnabled
        get() = privacySettingsManager.getCachedSettings().usageDataEnabled

    private val isPortableWindows
        get() =
            OSUtils.isWindows &&
                BuildConfig.DISTRIBUTION.contains("portable", ignoreCase = true)

    private val platformDir
        get() =
            when {
                OSUtils.isWindows -> windowsDir()
                OSUtils.isMacOS -> macDir()
                else -> linuxDir()
            }

    private val sentryDir
        get() =
            if (isPortableWindows) {
                File(SENTRY_DIR_NAME)
            } else {
                platformDir.resolve(SENTRY_DIR_NAME)
            }

    fun init(
        environment: String,
        releaseVersion: String,
    ) {
        if (!isTelemetryEnabled) return

        val isDev = environment.equals("dev", ignoreCase = true)

        Sentry.init { options ->
            options.dsn = SENTRY_DSN
            options.environment = environment
            options.release = releaseVersion

            options.isDebug = isDev

            runCatching { Files.createDirectories(sentryDir.toPath()) }
                .onFailure { e ->
                    logger.error(e) { "Failed to create Sentry cache directory at ${sentryDir.absolutePath}" }
                }
            options.cacheDirPath = sentryDir.absolutePath

            options.isSendDefaultPii = false
        }
        captureApplicationStarted()
    }

    private fun captureApplicationStarted() {
        val os = runCatching { OSUtils.osSummary }.getOrElse { "Unknown OS" }

        Sentry.setTag("lifecycle", "startup")
        Sentry.setTag("os.summary", os)

        Sentry.captureMessage(
            "Application Started on $os",
            SentryLevel.INFO,
        )
    }

    private fun windowsDir(): File {
        val appData = System.getenv("APPDATA")?.takeIf { it.isNotBlank() }
        val base =
            appData?.let { File(it, APP_DIR) }
                ?: File(System.getProperty("user.home"), "AppData/Roaming/$APP_DIR")
        return base
    }

    private fun macDir() = File(System.getProperty("user.home"), "Library/Application Support/$APP_DIR")

    private fun linuxDir(): File {
        val xdg = System.getenv("XDG_CONFIG_HOME")?.takeIf { it.isNotBlank() }
        val base =
            xdg?.let { File(it, APP_DIR) }
                ?: File(System.getProperty("user.home"), ".config/$APP_DIR")
        return base
    }
}

private val logger = KotlinLogging.logger {}
