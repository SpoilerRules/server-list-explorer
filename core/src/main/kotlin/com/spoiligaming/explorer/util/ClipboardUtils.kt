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

package com.spoiligaming.explorer.util

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO

object ClipboardUtils {
    private val exceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            logger.error(throwable) { "Clipboard operation failed." }
        }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)
    private val mutex = Mutex()
    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard

    fun copy(
        content: String?,
        stripMinecraftColorCodes: Boolean = false,
    ) = scope.launch {
        val text = content?.takeIf { it.isNotBlank() } ?: return@launch

        val result =
            text.takeIf { !stripMinecraftColorCodes }
                ?: text.stripMinecraftColorCodes()

        mutex.withLock {
            clipboard.setContents(StringSelection(result), null)
            logger.info { "Copied text content to clipboard: \"$result\"" }
        }
    }

    fun copyImageFromStream(stream: InputStream?) =
        scope.launch {
            val buffered =
                stream?.buffered()
                    ?: run {
                        logger.warn { "Provided InputStream for image is null. No image copied to clipboard." }
                        return@launch
                    }

            if (!buffered.markSupported()) {
                logger.warn { "InputStream does not support mark/reset. Cannot decode image safely." }
                return@launch
            }

            val image =
                runCatching {
                    buffered.mark(Int.MAX_VALUE)
                    ImageIO.read(buffered).also { buffered.reset() }
                }.onFailure { e ->
                    logger.error(e) { "Failed to decode image from InputStream." }
                }.getOrNull()

            if (image == null) {
                logger.warn { "ImageIO.read() could not decode an image from the provided InputStream." }
                return@launch
            }

            mutex.withLock {
                runCatching {
                    clipboard.setContents(BufferedImageTransferable(image), null)
                    logger.info { "Image copied to clipboard (size: ${image.width}×${image.height})." }
                }.onFailure { e ->
                    logger.error(e) { "Failed to copy image to clipboard." }
                }
            }
        }

    private class BufferedImageTransferable(
        private val image: BufferedImage,
    ) : Transferable {
        override fun getTransferData(flavor: DataFlavor) =
            if (flavor.equals(DataFlavor.imageFlavor)) {
                image
            } else {
                throw UnsupportedFlavorException(flavor)
            }

        override fun getTransferDataFlavors() = arrayOf(DataFlavor.imageFlavor)

        override fun isDataFlavorSupported(flavor: DataFlavor) =
            flavor.equals(
                DataFlavor.imageFlavor,
            )
    }
}

private val logger = KotlinLogging.logger {}
