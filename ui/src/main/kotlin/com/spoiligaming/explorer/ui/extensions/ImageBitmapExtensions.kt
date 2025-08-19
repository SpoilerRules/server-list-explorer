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

package com.spoiligaming.explorer.ui.extensions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.io.encoding.Base64

private fun ImageBitmap.toPngBytes(): ByteArray {
    require(width > 0 && height > 0) {
        "ImageBitmap must have positive dimensions, but was ${width}x$height"
    }
    return runCatching {
        ByteArrayOutputStream().use { baos ->
            val success = ImageIO.write(toAwtImage(), "png", baos)
            check(success) { "ImageIO.write returned false while encoding PNG" }
            baos.toByteArray()
        }
    }.getOrElse { e ->
        logger.error(e) { "Failed to encode ImageBitmap to PNG" }
        throw e
    }
}

fun ImageBitmap.toPngInputStream() =
    runCatching {
        ByteArrayInputStream(toPngBytes())
    }.getOrElse { e ->
        logger.error(e) { "Failed to create InputStream from PNG bytes" }
        throw e
    }

fun ImageBitmap.toPngBase64() =
    runCatching {
        Base64.encode(toPngBytes())
    }.getOrElse { e ->
        logger.error(e) { "Failed to Base64-encode PNG bytes" }
        throw e
    }

private val logger = KotlinLogging.logger {}
