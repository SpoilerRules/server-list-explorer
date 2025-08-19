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

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap

// luminance coefficients per ITU-R BT.709
private const val LUMINANCE_RED = 0.2126f
private const val LUMINANCE_GREEN = 0.7152f
private const val LUMINANCE_BLUE = 0.0722f

private const val DEFAULT_THRESHOLD = 0.5f
private const val DEFAULT_STRIDE = 4

/**
 * Computes the average luminance of a region in the bitmap.
 *
 * @param region  The rectangular area to sample (in pixels). Defaults to the entire bitmap.
 * @param stride  Number of pixels to skip per sample (higher = faster, lower = more accurate).
 * @return Average luminance in [0,1], or 1f if no samples were taken.
 */
private fun ImageBitmap.averageLuminance(
    region: Rect = Rect(0f, 0f, width.toFloat(), height.toFloat()),
    stride: Int = DEFAULT_STRIDE,
): Float {
    val pixelMap = toPixelMap()
    // clamp region to bitmap bounds
    val left = region.left.coerceIn(0f, width.toFloat()).toInt()
    val top = region.top.coerceIn(0f, height.toFloat()).toInt()
    val right = region.right.coerceIn(0f, width.toFloat()).toInt()
    val bottom = region.bottom.coerceIn(0f, height.toFloat()).toInt()

    var sum = 0f
    var count = 0

    for (y in top until bottom step stride) {
        for (x in left until right step stride) {
            pixelMap[x, y].let { color ->
                sum += color.red * LUMINANCE_RED +
                    color.green * LUMINANCE_GREEN +
                    color.blue * LUMINANCE_BLUE
            }
            count++
        }
    }
    return if (count > 0) sum / count else 1f
}

/**
 * Determines if the entire bitmap is considered dark.
 *
 * @param threshold  Luminance cutoff (0 = pure black, 1 = pure white).
 * @param stride     Sampling stride.
 */
internal fun ImageBitmap.isDark(
    threshold: Float = DEFAULT_THRESHOLD,
    stride: Int = DEFAULT_STRIDE,
) = averageLuminance(stride = stride) < threshold
