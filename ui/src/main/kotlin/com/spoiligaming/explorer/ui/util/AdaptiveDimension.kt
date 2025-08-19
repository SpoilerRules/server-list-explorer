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

package com.spoiligaming.explorer.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalWindowState

/**
 * Computes an adaptive width based on the current window dimensions, clamped between [min] and [max].
 *
 * @param min The minimum width (default: 0.dp)
 * @param max The maximum width (default: Dp.Infinity)
 * @param fraction The fraction of the window width to use (default: 0.25f)
 * @return The clamped adaptive width in Dp.
 */
@Composable
internal fun rememberAdaptiveWidth(
    min: Dp = DefaultMinDp,
    max: Dp = Dp.Infinity,
    fraction: Float = DEFAULT_ADAPTIVE_FRACTION,
): Dp {
    val windowState = LocalWindowState.current
    val density = LocalDensity.current

    val adaptive by remember(windowState, min, max, fraction) {
        derivedStateOf {
            val baseDp = with(density) { windowState.currentWidth.toDp() }
            val calculated = baseDp * fraction

            when {
                calculated < min -> min
                calculated > max -> max
                else -> calculated
            }
        }
    }

    return adaptive
}

/**
 * Computes an adaptive height based on the current window dimensions, clamped between [min] and [max].
 *
 * @param min The minimum height (default: 0.dp)
 * @param max The maximum height (default: Dp.Infinity)
 * @param fraction The fraction of the window height to use (default: 0.25f)
 * @return The clamped adaptive height in Dp.
 */
@Composable
internal fun rememberAdaptiveHeight(
    min: Dp = DefaultMinDp,
    max: Dp = Dp.Infinity,
    fraction: Float = DEFAULT_ADAPTIVE_FRACTION,
): Dp {
    val windowState = LocalWindowState.current
    val density = LocalDensity.current

    val adaptive by remember(windowState, min, max, fraction) {
        derivedStateOf {
            val baseDp = with(density) { windowState.currentHeight.toDp() }
            val calculated = baseDp * fraction

            calculated.coerceIn(min, max)
        }
    }

    return adaptive
}

private const val DEFAULT_ADAPTIVE_FRACTION = 0.25f
private val DefaultMinDp = 0.dp
