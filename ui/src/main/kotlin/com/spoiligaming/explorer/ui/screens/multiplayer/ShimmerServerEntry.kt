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

@file:OptIn(ExperimentalUuidApi::class)

package com.spoiligaming.explorer.ui.screens.multiplayer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalAmoledActive
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer
import kotlin.uuid.ExperimentalUuidApi

// TODO
@Composable
internal fun ShimmerServerEntry(
    modifier: Modifier = Modifier,
    shimmer: Shimmer,
) {
    val amoledOn = LocalAmoledActive.current

    ElevatedCard(
        modifier =
            modifier
                .shimmer(shimmer)
                .border(
                    border =
                        if (amoledOn) {
                            CardDefaults.outlinedCardBorder()
                        } else {
                            BorderStroke(
                                0.dp,
                                Color.Transparent,
                            )
                        },
                    shape = CardDefaults.shape,
                ),
        colors =
            CardDefaults.cardColors()
                .copy(
                    containerColor = if (amoledOn) Color.Black else CardDefaults.cardColors().containerColor,
                ),
    ) {}
}
