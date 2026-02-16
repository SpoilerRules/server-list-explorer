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

package com.spoiligaming.explorer.util

import com.spoiligaming.explorer.settings.model.ComputerStartupBehavior
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

object ComputerStartupRegistrationManager {
    private const val STARTUP_SOURCE_ARG = "--startup-source=os"

    private const val WINDOWS_RUN_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run"
    private const val WINDOWS_RUN_VALUE_NAME = "Server List Explorer"

    private const val LINUX_AUTOSTART_FILE_NAME = "server-list-explorer.desktop"

    private val linuxAutostartFile
        get() =
            File(
                File(linuxConfigHomeDirectory, "autostart"),
                LINUX_AUTOSTART_FILE_NAME,
            )

    private val linuxConfigHomeDirectory: File
        get() =
            System
                .getenv("XDG_CONFIG_HOME")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let(::File)
                ?: File(System.getProperty("user.home"), ".config")

    @Suppress("NOTHING_TO_INLINE")
    inline fun reconcile(behavior: ComputerStartupBehavior) = applyBehavior(behavior)

    fun applyBehavior(behavior: ComputerStartupBehavior) =
        runCatching {
            when {
                OSUtils.isWindows -> applyWindowsBehavior(behavior).getOrThrow()
                OSUtils.isLinux -> applyLinuxBehavior(behavior).getOrThrow()
                else -> logger.debug { "Skipping startup registration because this platform is unsupported." }
            }
        }

    private fun applyWindowsBehavior(behavior: ComputerStartupBehavior) =
        runCatching {
            if (behavior == ComputerStartupBehavior.DoNotStart) {
                removeWindowsRegistration()
                return@runCatching
            }

            val launcherPath = resolveLauncherPath().getOrThrow()
            writeWindowsRegistration(launcherPath)
        }

    private fun applyLinuxBehavior(behavior: ComputerStartupBehavior) =
        runCatching {
            if (behavior == ComputerStartupBehavior.DoNotStart) {
                removeLinuxRegistration()
                return@runCatching
            }

            val launcherPath = resolveLauncherPath().getOrThrow()
            writeLinuxRegistration(launcherPath)
        }

    private fun resolveLauncherPath(): Result<String> {
        val jPackageLauncherPath = OSUtils.jPackageLauncherPath
        if (jPackageLauncherPath != null) {
            return Result.success(jPackageLauncherPath)
        }

        val processCommand =
            ProcessHandle
                .current()
                .info()
                .command()
                .orElse(null)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { File(it) }
                ?.takeIf { it.exists() && it.isFile }
                ?.absolutePath

        if (processCommand == null) {
            return Result.failure(
                IllegalStateException("Unable to resolve launcher path from the current process."),
            )
        }

        if (OSUtils.isRunningOnBareJvm) {
            val message =
                "Startup registration requires a packaged launcher, but current process is JVM binary: $processCommand"
            logger.warn { message }
            return Result.failure(IllegalStateException(message))
        }

        return Result.success(processCommand)
    }

    private fun writeWindowsRegistration(launcherPath: String) {
        val command = "\"${launcherPath.escapeForCommandArgument()}\" $STARTUP_SOURCE_ARG"

        val result =
            executeCommand(
                "reg",
                "add",
                WINDOWS_RUN_KEY,
                "/v",
                WINDOWS_RUN_VALUE_NAME,
                "/t",
                "REG_SZ",
                "/d",
                command,
                "/f",
            )

        if (!result.isSuccess) {
            error("Failed to register Windows startup entry: ${result.output.ifBlank { "unknown error" }}")
        }
    }

    private fun removeWindowsRegistration() {
        val existsResult =
            executeCommand(
                "reg",
                "query",
                WINDOWS_RUN_KEY,
                "/v",
                WINDOWS_RUN_VALUE_NAME,
            )

        if (!existsResult.isSuccess) {
            return
        }

        val deleteResult =
            executeCommand(
                "reg",
                "delete",
                WINDOWS_RUN_KEY,
                "/v",
                WINDOWS_RUN_VALUE_NAME,
                "/f",
            )

        if (!deleteResult.isSuccess) {
            error("Failed to remove Windows startup entry: ${deleteResult.output.ifBlank { "unknown error" }}")
        }
    }

    private fun writeLinuxRegistration(launcherPath: String) {
        val autostartFile = linuxAutostartFile
        val autostartDirectory = autostartFile.parentFile
        if (autostartDirectory != null && !autostartDirectory.exists()) {
            val wasCreated = autostartDirectory.mkdirs()
            if (!wasCreated && !autostartDirectory.exists()) {
                error("Failed to create Linux autostart directory: ${autostartDirectory.absolutePath}")
            }
        }

        val startupCommand = "\"$launcherPath\" $STARTUP_SOURCE_ARG"
        val desktopEntry =
            """
            [Desktop Entry]
            Type=Application
            Version=1.0
            Name=Server List Explorer
            Comment=Server List Explorer for Minecraft
            Exec=$startupCommand
            Categories=Utility;Network;
            StartupNotify=true
            Terminal=false
            NoDisplay=false
            X-GNOME-Autostart-enabled=true
            """.trimIndent() + System.lineSeparator()

        runCatching {
            autostartFile.writeText(desktopEntry)
        }.onFailure { e ->
            error("Failed to write Linux startup entry: ${e.message.orEmpty()}")
        }
    }

    private fun removeLinuxRegistration() {
        val autostartFile = linuxAutostartFile
        if (!autostartFile.exists()) {
            return
        }

        val wasDeleted = autostartFile.delete()
        if (!wasDeleted && autostartFile.exists()) {
            error("Failed to remove Linux startup entry: ${autostartFile.absolutePath}")
        }
    }

    private fun executeCommand(vararg command: String) =
        runCatching {
            val process = ProcessBuilder(*command).start()
            val output =
                process.inputStream.bufferedReader().readText() + process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            CommandResult(exitCode = exitCode, output = output)
        }.getOrElse { e ->
            CommandResult(exitCode = -1, output = e.message.orEmpty())
        }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun String.escapeForCommandArgument() = replace("\"", "\"\"")

    private data class CommandResult(
        val exitCode: Int,
        val output: String,
    ) {
        val isSuccess
            get() = exitCode == 0
    }
}

private val logger = KotlinLogging.logger("ComputerStartupRegistrationManager")
