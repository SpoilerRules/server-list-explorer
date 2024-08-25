package com.spoiligaming.explorer.server

import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.StartupCoordinator
import com.spoiligaming.explorer.isBackupRestoreInProgress
import com.spoiligaming.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object BackupController {
    data class BackupEntry(
        val fileName: String,
        val formattedDate: String,
        val lastAccessTime: String,
        val formattedFileSize: String,
    )

    private val backupArchivePath = Paths.get("server-file-backups.zip")

    suspend fun create(serverFile: File) =
        withContext(Dispatchers.IO) {
            val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS").format(Date())
            val zipEntryName = "$timestamp.${serverFile.extension}"
            val temporaryZipFilePath =
                backupArchivePath.resolveSibling(
                    "${backupArchivePath.fileName}.tmp",
                )

            runCatching {
                if (Files.notExists(backupArchivePath)) {
                    Files.createFile(backupArchivePath)
                    Logger.printSuccess("Created new backup archive: $backupArchivePath")
                }

                if (Files.exists(backupArchivePath) && Files.size(backupArchivePath) > 0) {
                    Logger.printSuccess("Appending to existing backup archive: $backupArchivePath")
                    ZipFile(backupArchivePath.toFile()).use { existingZipFile ->
                        Files.newOutputStream(temporaryZipFilePath).use { tempZipFileOutputStream ->
                            ZipOutputStream(tempZipFileOutputStream).use { zipOutputStream ->
                                existingZipFile.entries().asSequence().forEach { entry ->
                                    zipOutputStream.putNextEntry(ZipEntry(entry.name))
                                    existingZipFile.getInputStream(entry).copyTo(zipOutputStream)
                                    zipOutputStream.closeEntry()
                                }

                                zipOutputStream.putNextEntry(ZipEntry(zipEntryName))
                                Files.newInputStream(serverFile.toPath()).use {
                                        serverFileInputStream ->
                                    serverFileInputStream.copyTo(zipOutputStream)
                                }
                                zipOutputStream.closeEntry()
                            }
                        }
                    }
                } else {
                    Logger.printSuccess("Creating new backup archive with file: $zipEntryName")
                    Files.newOutputStream(temporaryZipFilePath).use { tempZipFileOutputStream ->
                        ZipOutputStream(tempZipFileOutputStream).use { zipOutputStream ->
                            zipOutputStream.putNextEntry(ZipEntry(zipEntryName))
                            Files.newInputStream(serverFile.toPath()).use { serverFileInputStream ->
                                serverFileInputStream.copyTo(zipOutputStream)
                            }
                            zipOutputStream.closeEntry()
                        }
                    }
                }

                Files.deleteIfExists(backupArchivePath)
                Files.move(temporaryZipFilePath, backupArchivePath)
                Logger.printSuccess("Backup creation completed successfully.")
            }.onFailure { error ->
                Logger.printError(
                    "Error creating server file backup entry: ${error.localizedMessage}",
                )
            }.also {
                Files.deleteIfExists(temporaryZipFilePath)
            }
        }

    suspend fun deleteBackup(fileName: String) =
        withContext(Dispatchers.IO) {
            val temporaryZipFilePath =
                backupArchivePath.resolveSibling(
                    "${backupArchivePath.fileName}.tmp",
                )

            runCatching {
                Files.newOutputStream(temporaryZipFilePath).use { tempOutputStream ->
                    ZipOutputStream(tempOutputStream).use { zipOutputStream ->
                        ZipFile(backupArchivePath.toFile()).use { zipFile ->
                            zipFile.entries().asSequence().forEach { entry ->
                                if (entry.name != fileName) {
                                    zipOutputStream.putNextEntry(ZipEntry(entry.name))
                                    zipFile.getInputStream(entry).copyTo(zipOutputStream)
                                    zipOutputStream.closeEntry()
                                }
                            }
                        }
                    }
                }

                Files.deleteIfExists(backupArchivePath)
                Files.move(temporaryZipFilePath, backupArchivePath)
                Logger.printSuccess("Backup entry $fileName deleted successfully.")
            }.onFailure { exception ->
                Logger.printError("Error deleting backup entry: ${exception.localizedMessage}")
            }.also {
                Files.deleteIfExists(temporaryZipFilePath)
            }
        }

    suspend fun wipeBackup() =
        withContext(Dispatchers.IO) {
            runCatching {
                Files.deleteIfExists(backupArchivePath)
                Files.createFile(backupArchivePath)
                Logger.printSuccess("Backup archive wiped successfully.")
            }.onFailure { exception ->
                Logger.printError("Error wiping backup entries: ${exception.localizedMessage}")
            }
        }

    suspend fun restoreBackup(backupFileName: String) =
        withContext(Dispatchers.IO) {
            val serverFilePath =
                Paths.get(
                    ConfigurationHandler.getInstance().generalSettings.serverFilePath,
                )
            val temporaryBackupFilePath = Files.createTempFile("backup_restore", null)

            Logger.printSuccess("Starting restoration of backup file: $backupFileName")

            runCatching {
                if (Files.notExists(backupArchivePath)) {
                    throw IOException("Backup ZIP file does not exist.")
                }

                ZipFile(backupArchivePath.toFile()).use { zipFile ->
                    val zipEntry =
                        zipFile.getEntry(backupFileName)
                            ?: throw IOException(
                                "Backup file $backupFileName not found in ZIP archive.",
                            )

                    zipFile.getInputStream(zipEntry).use { inputStream ->
                        Files.newOutputStream(temporaryBackupFilePath).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }

                if (Files.exists(serverFilePath) && !Files.deleteIfExists(serverFilePath)) {
                    throw IOException("Failed to delete the existing server file.")
                }

                Files.move(
                    temporaryBackupFilePath,
                    serverFilePath,
                    StandardCopyOption.REPLACE_EXISTING,
                )
                Logger.printSuccess("Backup file $backupFileName restored successfully.")
            }.onSuccess {
                StartupCoordinator.retryLoad()
            }.onFailure { exception ->
                Logger.printError("Error restoring backup: ${exception.localizedMessage}")
            }.also {
                Files.deleteIfExists(temporaryBackupFilePath)
                isBackupRestoreInProgress = false
            }
        }

    fun getBackups(): List<BackupEntry> {
        val entries = mutableListOf<BackupEntry>()
        val zipFile = backupArchivePath.toFile()

        if (zipFile.length() == 0L) return entries

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val now = Date()

        Logger.printSuccess("Fetching backup entries from $backupArchivePath")

        runCatching {
            ZipFile(zipFile).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    val fileName = entry.name
                    val timestampString = fileName.removeSuffix(".dat")

                    runCatching {
                        val entryDate =
                            dateFormat.parse(timestampString)
                                ?: throw IllegalArgumentException("Invalid date format")
                        val lastUsage = formatLastUsage(now, entryDate)
                        val sizeString = formatFileSize(entry.size)
                        val formattedDate = displayDateFormat.format(entryDate)

                        entries.add(BackupEntry(fileName, formattedDate, lastUsage, sizeString))
                    }.onFailure { error ->
                        Logger.printError(
                            "Error parsing date for entry $fileName: ${error.localizedMessage}",
                        )
                    }
                }
            }
        }.onFailure { error ->
            Logger.printError("Error reading backup ZIP file: ${error.localizedMessage}")
        }

        return entries
    }

    private fun formatLastUsage(
        now: Date,
        entryDate: Date,
    ): String {
        val elapsedMillis = now.time - entryDate.time
        val secondsAgo = (elapsedMillis / 1000).toInt()
        val minutesAgo = (secondsAgo / 60)
        val hoursAgo = (minutesAgo / 60)
        val daysAgo = (hoursAgo / 24)
        val yearsAgo = (daysAgo / 365)

        return buildString {
            when {
                yearsAgo > 0 -> {
                    val yearWord = if (yearsAgo == 1) "year" else "years"
                    append("$yearsAgo $yearWord")
                    if (daysAgo % 365 > 0) {
                        append(", ${daysAgo % 365} ${if (daysAgo % 365 == 1) "day" else "days"}")
                    }
                    append(" ago")
                }
                daysAgo > 0 -> {
                    append("$daysAgo ${if (daysAgo == 1) "day" else "days"}")
                    val remainingHours = hoursAgo % 24
                    val remainingMinutes = minutesAgo % 60
                    val parts = mutableListOf<String>()
                    if (remainingHours > 0) {
                        parts.add("$remainingHours ${if (remainingHours == 1) "hour" else "hours"}")
                    }
                    if (remainingMinutes > 0) {
                        parts.add(
                            "$remainingMinutes ${
                                if (remainingMinutes == 1) "minute" else "minutes"
                            }",
                        )
                    }
                    if (parts.isNotEmpty()) {
                        append(", ${parts.joinToString(", ")}")
                    }
                    append(" ago")
                }
                hoursAgo > 0 -> {
                    append("${hoursAgo % 24} ${if (hoursAgo % 24 == 1) "hour" else "hours"}")
                    if (minutesAgo % 60 > 0) {
                        append(
                            ", ${minutesAgo % 60} ${
                                if (minutesAgo % 60 == 1) "minute" else "minutes"
                            } ago",
                        )
                    } else {
                        append(" ago")
                    }
                }
                minutesAgo > 0 -> {
                    append(
                        "${minutesAgo % 60} ${
                            if (minutesAgo % 60 == 1) "minute" else "minutes"
                        } ago",
                    )
                }
                else -> append("just now")
            }
        }
    }

    private fun formatFileSize(fileSize: Long): String =
        when {
            fileSize < 1024 -> "$fileSize bytes"
            fileSize < 1024L * 1024 -> "${fileSize / 1024} KB"
            fileSize < 1024L * 1024 * 1024 -> "${fileSize / (1024L * 1024)} MB"
            fileSize < 1024L * 1024 * 1024 * 1024 -> "${fileSize / (1024L * 1024 * 1024)} GB"
            else -> "${fileSize / (1024L * 1024 * 1024 * 1024)} TB"
        }
}
