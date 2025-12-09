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

package com.spoiligaming.explorer

import com.spoiligaming.explorer.build.BuildConfig
import java.io.File

internal object LogStorage {
    private const val APP_DIR = "ServerListExplorer"

    private val isPortableWindows
        get() = isWindows && BuildConfig.DISTRIBUTION.contains("portable", ignoreCase = true)

    private val osName
        get() = System.getProperty("os.name")?.lowercase().orEmpty()

    private val isWindows
        get() = osName.contains("win")

    private val isMac
        get() = osName.contains("mac")

    val logsDir
        get() =
            if (isPortableWindows) {
                File("logs")
            } else {
                when {
                    isWindows -> windowsLogs()
                    isMac -> macLogs()
                    else -> linuxLogs()
                }
            }

    private fun windowsLogs(): File {
        val local = System.getenv("LOCALAPPDATA")?.takeIf { it.isNotBlank() }
        val base =
            local?.let { File(it, APP_DIR) }
                ?: File(System.getProperty("user.home"), "AppData/Local/$APP_DIR")
        return base.resolve("logs")
    }

    private fun macLogs() = File(System.getProperty("user.home"), "Library/Logs/$APP_DIR")

    private fun linuxLogs(): File {
        val state = System.getenv("XDG_STATE_HOME")?.takeIf { it.isNotBlank() }
        val base =
            state?.let { File(it, APP_DIR) }
                ?: File(System.getProperty("user.home"), ".local/state/$APP_DIR")
        return base.resolve("logs")
    }
}
