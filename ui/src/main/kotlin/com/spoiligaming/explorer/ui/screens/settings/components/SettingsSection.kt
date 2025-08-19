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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceColorAtElevation(BoxElevation),
                    shape = CardDefaults.outlinedShape,
                )
                .border(CardDefaults.outlinedCardBorder(), shape = CardDefaults.outlinedShape),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(InnerPadding),
            verticalArrangement = Arrangement.spacedBy(InnerArrangement),
        ) {
            settings.forEachIndexed { index, setting ->
                setting()

                if (index != settings.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

private val OuterArrangement = 8.dp
private val BoxElevation = 2.dp
private val InnerPadding = 16.dp
private val InnerArrangement = 12.dp
