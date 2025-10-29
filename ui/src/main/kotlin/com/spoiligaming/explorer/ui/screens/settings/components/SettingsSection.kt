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

package com.spoiligaming.explorer.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
internal fun SettingsSection(
    header: String,
    settings: List<@Composable () -> Unit>,
) = Column(verticalArrangement = Arrangement.spacedBy(OuterArrangement)) {
    Text(
        text = header,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleLarge,
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(SectionContainerElevation)
        val dividerColor = MaterialTheme.colorScheme.background

        settings.forEachIndexed { index, setting ->
            val shape =
                when {
                    settings.size == 1 -> SectionSingleShape
                    index == 0 -> SectionTopShape
                    index == settings.lastIndex -> SectionBottomShape
                    else -> RectangleShape
                }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = containerColor,
                tonalElevation = SectionContainerElevation,
                shape = shape,
            ) {
                setting()
            }

            if (index != settings.lastIndex) {
                Spacer(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(SectionDividerSpacing)
                            .background(dividerColor),
                )
            }
        }
    }
}

private val OuterArrangement = 8.dp
private val SectionContainerElevation = 8.dp
private val SectionCornerRadius = 16.dp
private val SectionDividerSpacing = 4.dp
private val SectionSingleShape = RoundedCornerShape(SectionCornerRadius)
private val SectionTopShape =
    RoundedCornerShape(
        topStart = SectionCornerRadius,
        topEnd = SectionCornerRadius,
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
    )
private val SectionBottomShape =
    RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = SectionCornerRadius,
        bottomEnd = SectionCornerRadius,
    )
