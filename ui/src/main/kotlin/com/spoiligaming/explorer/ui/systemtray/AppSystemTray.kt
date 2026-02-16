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

package com.spoiligaming.explorer.ui.systemtray

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.window.ApplicationScope
import com.kdroid.composetray.tray.api.Tray
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.util.OSUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.compose.resources.decodeToImageBitmap
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.tray_content_description_icon
import server_list_explorer.ui.generated.resources.tray_item_exit
import server_list_explorer.ui.generated.resources.tray_item_hide
import server_list_explorer.ui.generated.resources.tray_item_open
import java.awt.SystemTray
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipFile
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.UIManager
import javax.swing.filechooser.FileSystemView

@Composable
internal fun ApplicationScope.AppSystemTray(
    isSystemTrayFeatureEnabled: Boolean,
    shouldMinimizeToSystemTrayOnClose: Boolean,
    isWindowVisible: Boolean,
    tooltip: String,
    onHide: () -> Unit,
    onOpen: () -> Unit,
    onExit: () -> Unit,
) {
    val shouldShowTray =
        isSystemTrayFeatureEnabled &&
            (!isWindowVisible || shouldMinimizeToSystemTrayOnClose)
    if (!shouldShowTray) return

    logger.info { "Attempting to render system tray" }
    val isSystemTraySupported = SystemTray.isSupported()
    if (!isSystemTraySupported) {
        logger.warn { "System tray is not supported on this environment. Restoring main window." }
        LaunchedEffect(isWindowVisible) {
            onOpen()
        }
        return
    }

    val trayIconBitmap = rememberTrayIconBitmap()
    if (trayIconBitmap == null) {
        logger.warn { "No JVM tray icon could be resolved. Restoring main window instead of rendering tray." }
        LaunchedEffect(isWindowVisible) {
            onOpen()
        }
        return
    }

    val trayIconContentDescription = t(Res.string.tray_content_description_icon)
    val trayHideItemLabel = t(Res.string.tray_item_hide)
    val trayOpenItemLabel = t(Res.string.tray_item_open)
    val trayExitItemLabel = t(Res.string.tray_item_exit)

    Tray(
        iconContent = {
            Image(
                bitmap = trayIconBitmap,
                contentDescription = trayIconContentDescription,
                modifier = Modifier.fillMaxSize(),
            )
        },
        tooltip = tooltip,
        primaryAction = onOpen,
    ) {
        if (isWindowVisible) {
            Item(trayHideItemLabel, onClick = onHide)
        } else {
            Item(trayOpenItemLabel, onClick = onOpen)
        }
        Item(trayExitItemLabel, onClick = onExit)
    }
}

@Composable
private fun rememberTrayIconBitmap() =
    remember {
        if (OSUtils.isWindows) {
            loadWindowsLauncherIconBitmapOrNull() ?: loadJvmTrayIconBitmapOrNull() ?: loadJvmUiIconBitmapOrNull()
        } else {
            loadJvmTrayIconBitmapOrNull() ?: loadJvmUiIconBitmapOrNull()
        }
    }

private fun loadWindowsLauncherIconBitmapOrNull() =
    runCatching {
        val launcher =
            resolveWindowsLauncherPathOrNull()?.takeIf { it.exists() && it.isFile }
                ?: return@runCatching null

        val shellIcon = FileSystemView.getFileSystemView().getSystemIcon(launcher) ?: return@runCatching null
        iconToImageBitmapOrNull(shellIcon)?.also {
            logger.debug { "Loaded tray icon from Windows launcher: ${launcher.absolutePath}" }
        }
    }.onFailure { e ->
        logger.warn(e) { "Failed to load tray icon from Windows launcher executable." }
    }.getOrNull()

private fun resolveWindowsLauncherPathOrNull(): File? {
    val jpackagePath =
        System
            .getProperty("jpackage.app-path")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(::File)
            ?.takeIf { it.name.equals(WINDOWS_LAUNCHER_NAME, ignoreCase = true) }

    if (jpackagePath != null) return jpackagePath

    val processCommand =
        ProcessHandle
            .current()
            .info()
            .command()
            .orElse(null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(::File)
            ?.takeIf { it.name.equals(WINDOWS_LAUNCHER_NAME, ignoreCase = true) }

    if (processCommand != null) return processCommand

    val userDir = System.getProperty("user.dir")?.takeIf { it.isNotBlank() }?.let(::File) ?: return null
    val localLauncher = File(userDir, WINDOWS_LAUNCHER_NAME)
    if (localLauncher.exists()) return localLauncher

    val appSubdirLauncher = File(userDir, "$APP_SUBDIRECTORY_NAME/$WINDOWS_LAUNCHER_NAME")
    if (appSubdirLauncher.exists()) return appSubdirLauncher

    return null
}

private fun loadJvmTrayIconBitmapOrNull(): ImageBitmap? {
    val candidate =
        JavaTrayIconResourceCandidates
            .firstNotNullOfOrNull { path ->
                loadJvmResourceBytesOrNull(path)?.let { bytes -> path to bytes }
            } ?: return null

    val (resourcePath, iconBytes) = candidate
    logger.debug { "Selected JVM tray icon resource candidate: $resourcePath" }

    return runCatching {
        ByteArrayInputStream(iconBytes).use { stream ->
            stream.readAllBytes().decodeToImageBitmap()
        }
    }.getOrElse { e ->
        logger.warn(e) { "Failed to decode extracted JVM icon bytes into Bitmap" }
        null
    }
}

private fun loadJvmUiIconBitmapOrNull() =
    runCatching {
        val icon =
            JvmUiIconCandidates
                .firstNotNullOfOrNull { key ->
                    UIManager.getIcon(key)?.takeIf {
                        it.iconWidth > 0 && it.iconHeight > 0
                    }
                } ?: return@runCatching null

        iconToImageBitmapOrNull(icon)
    }.onFailure { e ->
        logger.warn(e) { "Failed to load tray icon from JVM UI defaults." }
    }.getOrNull()

private fun loadJvmResourceBytesOrNull(resourcePath: String) =
    runCatching {
        // try module encapsulation break (java 9+)
        ModuleLayer
            .boot()
            .findModule("java.desktop")
            .orElse(null)
            ?.getResourceAsStream(resourcePath)
            ?.use { it.readAllBytes() }
            ?.also { logger.debug { "Loaded JVM resource via ModuleLayer (java.desktop): $resourcePath" } }

            // try classloader (works if open or on classpath)
            ?: ClassLoader
                .getSystemResourceAsStream(resourcePath)
                ?.use { it.readAllBytes() }
                ?.also { logger.debug { "Loaded JVM resource via system ClassLoader: $resourcePath" } }

            // try direct JMOD extraction
            ?: loadJvmResourceBytesFromJmodOrNull(resourcePath)
                ?.also { logger.debug { "Loaded JVM resource via JMOD extraction: $resourcePath" } }
    }.getOrNull()

private fun loadJvmResourceBytesFromJmodOrNull(resourcePath: String) =
    runCatching {
        val javaHome = System.getProperty("java.home") ?: return@runCatching null
        val javaHomeDirectory = File(javaHome)

        val jmodCandidates =
            listOf(
                File(javaHomeDirectory, "jmods/java.desktop.jmod"), // JDK standard
                File(javaHomeDirectory.parentFile, "jmods/java.desktop.jmod"), // JRE inside JDK
            )

        jmodCandidates
            .asSequence()
            .filter { it.exists() && it.isFile }
            .firstNotNullOfOrNull { jmodFile ->
                runCatching {
                    ZipFile(jmodFile).use { zip ->
                        zip.getEntry("$JMOD_CLASSES_DIRECTORY/$resourcePath")?.let { entry ->
                            zip.getInputStream(entry).use { it.readAllBytes() }
                        }
                    }
                }.getOrNull()?.also {
                    logger.debug {
                        "Loaded JVM resource from JMOD candidate: ${jmodFile.absolutePath} (resource: $resourcePath)"
                    }
                }
            }
    }.getOrNull()

private fun iconToImageBitmapOrNull(icon: Icon): ImageBitmap? {
    val width = icon.iconWidth.coerceAtLeast(MIN_TRAY_ICON_SIZE)
    val height = icon.iconHeight.coerceAtLeast(MIN_TRAY_ICON_SIZE)
    val buffered = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val graphics = buffered.createGraphics()

    return try {
        icon.paintIcon(null, graphics, ICON_RENDER_OFFSET, ICON_RENDER_OFFSET)
        ByteArrayOutputStream().use { output ->
            ImageIO.write(buffered, PNG_IMAGE_FORMAT, output)
            output.toByteArray().decodeToImageBitmap()
        }
    } finally {
        graphics.dispose()
    }
}

private const val APP_SUBDIRECTORY_NAME = "app"
private const val ICON_RENDER_OFFSET = 0
private const val JMOD_CLASSES_DIRECTORY = "classes"
private const val MIN_TRAY_ICON_SIZE = 16
private const val PNG_IMAGE_FORMAT = "png"
private const val WINDOWS_LAUNCHER_NAME = "ServerListExplorer.exe"

private val JavaTrayIconResourceCandidates =
    listOf(
        "com/sun/java/swing/plaf/windows/icons/JavaCup32.png",
        "com/sun/java/swing/plaf/windows/icons/JavaCup16.png",
    )

private val JvmUiIconCandidates =
    listOf(
        "FileView.computerIcon",
        "FileChooser.homeFolderIcon",
        "OptionPane.informationIcon",
        "Tree.closedIcon",
    )

private val logger = KotlinLogging.logger {}
