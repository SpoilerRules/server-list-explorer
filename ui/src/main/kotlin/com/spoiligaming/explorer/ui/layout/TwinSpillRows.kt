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

package com.spoiligaming.explorer.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun TwinSpillRows(
    firstRowItems: List<@Composable () -> Unit>,
    secondRowItems: List<@Composable () -> Unit>,
    horizontalSpacing: Dp = DefaultHorizontalSpacing,
    verticalSpacing: Dp = DefaultVerticalSpacing,
    modifier: Modifier = Modifier,
) = Layout(
    content = {
        firstRowItems.forEach { it() }
        secondRowItems.forEach { it() }
    },
    modifier = modifier,
) { measurables, constraints ->
    val horizontalSpacingPx = horizontalSpacing.roundToPx()
    val verticalSpacingPx = verticalSpacing.roundToPx()
    val maxWidth = constraints.maxWidth

    val primaryCount = firstRowItems.size
    val primaryMeasurables = measurables.take(primaryCount)
    val secondaryMeasurables = measurables.drop(primaryCount)

    val firstRowPlaceables = mutableListOf<Placeable>()
    val overflowedPrimaryPlaceables = mutableListOf<Placeable>()

    var usedWidth = 0
    primaryMeasurables.forEach { measurable ->
        val placeable =
            measurable.measure(
                Constraints(
                    minWidth = 0,
                    maxWidth = constraints.maxWidth,
                    minHeight = 0,
                    maxHeight = constraints.maxHeight,
                ),
            )
        val widthWithSpacing =
            if (firstRowPlaceables.isEmpty()) {
                placeable.width
            } else {
                placeable.width + horizontalSpacingPx
            }
        if (usedWidth + widthWithSpacing <= maxWidth) {
            usedWidth += widthWithSpacing
            firstRowPlaceables.add(placeable)
        } else {
            overflowedPrimaryPlaceables.add(placeable)
        }
    }

    val secondRowPlaceables = mutableListOf<Placeable>()
    secondRowPlaceables.addAll(overflowedPrimaryPlaceables)
    secondaryMeasurables.forEach { measurable ->
        val placeable =
            measurable.measure(
                Constraints(
                    minWidth = 0,
                    maxWidth = constraints.maxWidth,
                    minHeight = 0,
                    maxHeight = constraints.maxHeight,
                ),
            )
        secondRowPlaceables.add(placeable)
    }

    val firstRowHeight = firstRowPlaceables.maxOfOrNull { it.height } ?: 0
    val secondRowHeight = secondRowPlaceables.maxOfOrNull { it.height } ?: 0
    val totalHeight =
        firstRowHeight +
            if (secondRowPlaceables.isNotEmpty()) {
                verticalSpacingPx + secondRowHeight
            } else {
                0
            }

    layout(width = maxWidth, height = totalHeight) {
        var xPosition = 0
        firstRowPlaceables.forEachIndexed { index, placeable ->
            if (index > 0) xPosition += horizontalSpacingPx
            val yOffset = (firstRowHeight - placeable.height) / 2
            placeable.placeRelative(x = xPosition, y = yOffset)
            xPosition += placeable.width
        }

        if (secondRowPlaceables.isNotEmpty()) {
            var xSecond = 0
            val yBase = firstRowHeight + verticalSpacingPx
            secondRowPlaceables.forEachIndexed { index, placeable ->
                if (index > 0) xSecond += horizontalSpacingPx
                val yOffset = (secondRowHeight - placeable.height) / 2
                placeable.placeRelative(x = xSecond, y = yBase + yOffset)
                xSecond += placeable.width
            }
        }
    }
}

private val DefaultHorizontalSpacing = 8.dp
private val DefaultVerticalSpacing = 8.dp
