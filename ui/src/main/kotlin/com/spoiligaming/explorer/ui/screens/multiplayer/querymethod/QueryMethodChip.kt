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

package com.spoiligaming.explorer.ui.screens.multiplayer.querymethod

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.spoiligaming.explorer.ui.theme.isDarkTheme

@Immutable
internal data class QueryMethodChip(
    val label: String,
    val icon: ChipIcon? = null,
    val colors: QueryMethodChipColors,
) {
    @Immutable
    internal data class ChipIcon(
        val vector: ImageVector,
        val contentDescription: String?,
    )
}

@Composable
internal fun QueryMethodChipBadge(
    chip: QueryMethodChip,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .width(IntrinsicSize.Max)
                .height(ChipHeight),
        color = chip.colors.container,
        contentColor = chip.colors.content,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(ChipBorderWidth, chip.colors.border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = ChipHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            chip.icon?.let { iconData ->
                Icon(
                    imageVector = iconData.vector,
                    contentDescription = iconData.contentDescription,
                    tint = chip.colors.content,
                    modifier = Modifier.size(ChipIconSize),
                )
                Spacer(Modifier.width(ChipSpacing))
            }
            Text(
                text = chip.label,
                style = MaterialTheme.typography.labelSmall,
                color = chip.colors.content,
            )
        }
    }
}

private val ChipHeight = 24.dp
private val ChipIconSize = 16.dp
private val ChipHorizontalPadding = 8.dp
private val ChipSpacing = 4.dp
private val ChipBorderWidth = 0.5.dp

@Immutable
internal data class QueryMethodChipColors(
    val container: Color,
    val content: Color,
    val border: Color,
)

internal object QueryMethodChipStyles {
    private const val INTENSITY_MIN = 0f
    private const val INTENSITY_MAX = 1f

    private const val MIN_CONTENT_ALPHA = 0.72f
    private const val MAX_CONTENT_ALPHA = 0.95f

    private const val MIN_BORDER_ALPHA = 0.12f
    private const val MAX_BORDER_ALPHA = 0.95f

    private const val MAX_ALPHA = 1f

    @Composable
    fun green(intensity: Float) = toned(Color(0xFF2E7D32), Color(0xFF81C784), intensity)

    @Composable
    fun blue(intensity: Float) = toned(Color(0xFF1565C0), Color(0xFF90CAF9), intensity)

    @Composable
    fun red(intensity: Float) = toned(Color(0xFFC62828), Color(0xFFEF9A9A), intensity)

    @Composable
    fun yellow(intensity: Float) = toned(Color(0xFFF9A825), Color(0xFFFFF59D), intensity)

    @Composable
    fun common() =
        QueryMethodChipColors(
            container = MaterialTheme.colorScheme.surfaceContainerHigh,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            border = MaterialTheme.colorScheme.outlineVariant,
        )

    @Composable
    private fun toned(
        light: Color,
        dark: Color,
        intensity: Float,
    ): QueryMethodChipColors {
        val clamped = intensity.coerceIn(INTENSITY_MIN, INTENSITY_MAX)

        val t = clamped * clamped

        val base = if (isDarkTheme) dark else light
        val contentAlpha = lerp(MIN_CONTENT_ALPHA, MAX_CONTENT_ALPHA, t).coerceAtMost(MAX_ALPHA)
        val borderAlpha = lerp(MIN_BORDER_ALPHA, MAX_BORDER_ALPHA, t).coerceAtMost(MAX_ALPHA)

        return QueryMethodChipColors(
            container = MaterialTheme.colorScheme.surfaceContainerHigh, // unchanged
            content = base.copy(alpha = contentAlpha),
            border = base.copy(alpha = borderAlpha),
        )
    }
}
