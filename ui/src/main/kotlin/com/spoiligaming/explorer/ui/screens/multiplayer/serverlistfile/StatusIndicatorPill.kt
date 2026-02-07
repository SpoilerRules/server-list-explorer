/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025-2026 SpoilerRules
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

package com.spoiligaming.explorer.ui.screens.multiplayer.serverlistfile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun StatusIndicatorPill(
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    leadingIconContentDescription: String?,
    colors: StatusIndicatorPillColors = StatusIndicatorPillDefaults.primaryColors(),
    contentHorizontalPadding: Dp = StatusIndicatorPillDefaults.defaultContentHorizontalPadding(),
) = Surface(
    shape = StatusIndicatorPillTokens.ContainerShape,
    color = colors.containerColor,
    modifier = modifier.height(StatusIndicatorPillTokens.ContainerHeight),
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StatusIndicatorPillTokens.ContentSpacing),
        modifier = Modifier.padding(horizontal = contentHorizontalPadding),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = leadingIconContentDescription,
                modifier = Modifier.size(StatusIndicatorPillTokens.IconSize),
                tint = colors.iconTint,
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.labelColor,
            maxLines = StatusIndicatorPillTokens.MAX_LINES,
        )
    }
}

internal data class StatusIndicatorPillColors(
    val containerColor: Color,
    val labelColor: Color,
    val iconTint: Color,
)

internal object StatusIndicatorPillDefaults {
    @Composable
    fun primaryColors() =
        StatusIndicatorPillColors(
            containerColor = StatusIndicatorPillTokens.PrimaryContainerColor,
            labelColor = StatusIndicatorPillTokens.PrimaryContentColor,
            iconTint = StatusIndicatorPillTokens.PrimaryContentColor,
        )

    @Composable
    fun errorColors() =
        StatusIndicatorPillColors(
            containerColor = StatusIndicatorPillTokens.ErrorContainerColor,
            labelColor = StatusIndicatorPillTokens.ErrorContentColor,
            iconTint = StatusIndicatorPillTokens.ErrorContentColor,
        )

    fun defaultContentHorizontalPadding() = StatusIndicatorPillTokens.ContentHorizontalPadding

    fun compactContentHorizontalPadding() = StatusIndicatorPillTokens.CompactHorizontalPadding
}

private object StatusIndicatorPillTokens {
    val ContainerHeight = 22.dp
    val ContainerShape = RoundedCornerShape(8.dp)

    val ContentHorizontalPadding = 8.dp
    val CompactHorizontalPadding = 6.dp
    val ContentSpacing = 4.dp
    val IconSize = 14.dp
    const val MAX_LINES = 1

    const val PRIMARY_CONTAINER_ALPHA = 0.08f
    const val ERROR_CONTAINER_ALPHA = 0.10f

    val PrimaryContainerColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.primary.copy(alpha = PRIMARY_CONTAINER_ALPHA)

    val PrimaryContentColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.primary

    val ErrorContainerColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.error.copy(alpha = ERROR_CONTAINER_ALPHA)

    val ErrorContentColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.error
}
