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

package com.spoiligaming.explorer

import com.spoiligaming.explorer.settings.util.AppStoragePaths
import com.spoiligaming.explorer.util.OSUtils
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinDef.DWORD
import io.github.oshai.kotlinlogging.KotlinLogging

internal object WindowsProcessPriority {
    private const val WINDOWS_IDLE_PRIORITY_CLASS = 0x00000040

    fun applyAutoStartupPriority() {
        if (!OSUtils.isWindows || OSUtils.isRunningOnBareJvm || AppStoragePaths.isPortableInstall) {
            return
        }

        if (!ArgsParser.isAutoStartupLaunch) {
            return
        }

        runCatching {
            val currentProcess = Kernel32.INSTANCE.GetCurrentProcess()
            val isApplied =
                Kernel32.INSTANCE.SetPriorityClass(
                    currentProcess,
                    DWORD(WINDOWS_IDLE_PRIORITY_CLASS.toLong()),
                )

            if (!isApplied) {
                val code = Kernel32.INSTANCE.GetLastError()
                error("Failed to lower process priority for auto startup. Win32Error=$code")
            }
        }.onFailure { e ->
            logger.error(e) { "Unable to apply low-priority mode for Windows auto startup." }
        }
    }
}

private val logger = KotlinLogging.logger {}
