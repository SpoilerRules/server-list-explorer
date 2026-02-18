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
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal object StartupAppDataMigration {
    private const val RETRY_COUNT = 40
    private const val RETRY_DELAY_MS = 150L

    private data class MigrationSpec(
        val name: String,
        val sourceDir: File,
        val targetDir: File,
        val sourceFilter: (File) -> Boolean = { true },
        val deleteSourceDirectoryWhenFinished: Boolean = true,
    )

    fun migrateBeforeLogging(): File {
        if (AppStoragePaths.isPortableInstall) {
            return AppStoragePaths.logsDir
        }

        val specs =
            listOf(
                MigrationSpec(
                    name = "LegacyNamedPlatformConfigRootToPreferredConfigRoot",
                    sourceDir = AppStoragePaths.legacyNamedPlatformConfigRootDir,
                    targetDir = AppStoragePaths.platformConfigRootDir,
                ),
                MigrationSpec(
                    name = "LegacyNamedPlatformLogsToPreferredLogsInAppData",
                    sourceDir = AppStoragePaths.legacyNamedPlatformLogsRootDir,
                    targetDir = AppStoragePaths.platformLogsDir,
                ),
            )

        specs.forEach(::runMigration)
        ensureDirectoryExists(AppStoragePaths.platformLogsDir, "platform logs")
        return AppStoragePaths.platformLogsDir
    }

    private fun runMigration(spec: MigrationSpec) {
        val source = spec.sourceDir
        if (!source.exists() || !source.isDirectory) return
        if (source.absoluteFile == spec.targetDir.absoluteFile) return

        println("Starting migration ${spec.name}: ${source.absolutePath} -> ${spec.targetDir.absolutePath}")

        val merged =
            runCatching {
                ensureDirectoryExists(spec.targetDir, spec.name)
                mergeDirectory(source, spec.targetDir, spec.sourceFilter)
                true
            }.getOrElse { e ->
                System.err.println(
                    "Migration ${spec.name} failed while copying to ${spec.targetDir.absolutePath}: ${e.message.orEmpty()}",
                )
                false
            }

        if (!merged) return

        if (!spec.deleteSourceDirectoryWhenFinished) {
            tryDeleteDirectoryIfEmpty(source, spec.name)
            return
        }

        if (tryDeleteRecursively(source)) {
            println("Migration ${spec.name} completed and removed ${source.absolutePath}")
            return
        }

        System.err.println(
            "Migration ${spec.name} copied files but could not delete ${source.absolutePath}. " +
                "This is likely a temporary file lock.",
        )
    }

    private fun ensureDirectoryExists(
        dir: File,
        context: String,
    ) = runCatching { Files.createDirectories(dir.toPath()) }.onFailure { e ->
        System.err.println("Failed to create directory for $context at ${dir.absolutePath}: ${e.message.orEmpty()}")
    }

    private fun mergeDirectory(
        sourceDir: File,
        targetDir: File,
        sourceFilter: (File) -> Boolean,
    ) = sourceDir
        .walkTopDown()
        .forEach { source ->
            if (source.absoluteFile == sourceDir.absoluteFile) return@forEach
            if (!sourceFilter(source)) return@forEach

            val relativePath = sourceDir.toPath().relativize(source.toPath())
            val destination = targetDir.toPath().resolve(relativePath)

            if (source.isDirectory) {
                Files.createDirectories(destination)
                return@forEach
            }

            if (!destination.toFile().exists()) {
                moveOrCopyFile(source, destination.toFile())
                return@forEach
            }

            if (source.exists()) {
                runCatching { Files.deleteIfExists(source.toPath()) }
                    .onFailure { e ->
                        System.err.println(
                            "Could not remove duplicate file ${source.absolutePath}: ${e.message.orEmpty()}",
                        )
                    }
            }
        }

    private fun moveOrCopyFile(
        source: File,
        destination: File,
    ) = runCatching {
        destination.parentFile?.let { Files.createDirectories(it.toPath()) }
        Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }.recoverCatching {
        destination.parentFile?.let { Files.createDirectories(it.toPath()) }
        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
        Files.deleteIfExists(source.toPath())
    }.onFailure { e ->
        System.err.println(
            "Could not migrate ${source.absolutePath} to ${destination.absolutePath}: ${e.message.orEmpty()}",
        )
    }

    private fun tryDeleteDirectoryIfEmpty(
        dir: File,
        context: String,
    ) {
        if (!dir.exists() || !dir.isDirectory) return
        val entries = dir.listFiles().orEmpty()
        if (entries.isNotEmpty()) return

        val deleted = tryDeleteRecursively(dir)
        if (!deleted) {
            System.err.println("Could not delete empty directory after $context: ${dir.absolutePath}")
        }
    }

    private fun tryDeleteRecursively(file: File): Boolean {
        if (!file.exists()) return true

        repeat(RETRY_COUNT) {
            if (deleteRecursivelySinglePass(file)) {
                return true
            }
            Thread.sleep(RETRY_DELAY_MS)
        }

        return !file.exists()
    }

    private fun deleteRecursivelySinglePass(file: File): Boolean {
        var allDeleted = true
        file.walkBottomUp().forEach { entry ->
            if (entry.exists() && !entry.delete()) {
                allDeleted = false
            }
        }
        return allDeleted
    }
}
