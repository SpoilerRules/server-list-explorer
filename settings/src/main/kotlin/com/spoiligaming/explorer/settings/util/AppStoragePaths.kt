/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2026 SpoilerRules
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

import com.spoiligaming.explorer.build.BuildConfig
import com.spoiligaming.explorer.build.PlatformDirs
import java.io.File

object AppStoragePaths {
    private const val LEGACY_CONFIG_DIR_NAME = "config"
    private const val LOGS_DIR_NAME = "logs"
    private const val SETTINGS_DIR_NAME = "config"
    const val LEGACY_APP_DIR_NAME = "ServerListExplorer"

    private val osName = System.getProperty("os.name")?.lowercase().orEmpty()
    private val isMac = osName.contains("mac")
    private val isWindows = osName.contains("win")
    val isPortableWindows = isWindows && BuildConfig.DISTRIBUTION.contains("portable", ignoreCase = true)

    val preferredAppDirName =
        if (isWindows || isMac) {
            PlatformDirs.WINDOWS_MACOS_APP_DIR_NAME
        } else {
            PlatformDirs.LINUX_APP_DIR_NAME
        }

    val homeDir = File(System.getProperty("user.home"))

    val windowsRoamingDir =
        System
            .getenv("APPDATA")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(::File)
            ?: homeDir.resolve("AppData/Roaming")

    val windowsLocalDir =
        System
            .getenv("LOCALAPPDATA")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(::File)
            ?: homeDir.resolve("AppData/Local")

    val xdgConfigDir =
        System
            .getenv("XDG_CONFIG_HOME")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(::File)
            ?: homeDir.resolve(".config")

    val xdgStateDir =
        System
            .getenv("XDG_STATE_HOME")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(::File)
            ?: homeDir.resolve(".local/state")

    val configParentDir =
        when {
            isWindows -> windowsRoamingDir
            isMac -> homeDir.resolve("Library/Application Support")
            else -> xdgConfigDir
        }

    val logsParentDir =
        when {
            isWindows -> windowsLocalDir
            isMac -> homeDir.resolve("Library/Logs")
            else -> xdgStateDir
        }

    val legacyConfigDir = File(LEGACY_CONFIG_DIR_NAME)

    val legacyLogsDir = File(LOGS_DIR_NAME)

    val platformConfigRootDir = configParentDir.resolve(preferredAppDirName)

    val platformLogsRootDir = logsParentDir.resolve(preferredAppDirName)

    val legacyNamedPlatformConfigRootDir = configParentDir.resolve(LEGACY_APP_DIR_NAME)

    val legacyNamedPlatformLogsRootDir = logsParentDir.resolve(LEGACY_APP_DIR_NAME)

    val platformSettingsDir = platformConfigRootDir.resolve(SETTINGS_DIR_NAME)

    val platformLogsDir = platformLogsRootDir.resolve(LOGS_DIR_NAME)

    val settingsDir = if (isPortableWindows) legacyConfigDir else platformSettingsDir

    val firstRunConfigDir = if (isPortableWindows) legacyConfigDir else platformConfigRootDir

    val logsDir = if (isPortableWindows) legacyLogsDir else platformLogsDir
}
