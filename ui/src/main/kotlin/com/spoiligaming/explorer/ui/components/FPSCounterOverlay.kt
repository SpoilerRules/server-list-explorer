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

package com.spoiligaming.explorer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.skiko.FPSCounter

@Composable
internal fun FPSOverlay(onFpsUpdate: (Int) -> Unit) =
    LaunchedEffect(Unit) {
        val counter = FPSCounter(logOnTick = false)
        while (true) {
            withFrameNanos {
                counter.tick()
                val fps = counter.average
                onFpsUpdate(fps)
            }
        }
    }

@Composable
internal fun BoxScope.FPSDisplay() {
    var fps by remember { mutableIntStateOf(0) }
    FPSOverlay { fps = it }
    Box(
        Modifier.align(Alignment.BottomEnd),
    ) {
        ElevatedCard(
            shape = MaterialTheme.shapes.extraSmall,
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
        ) {
            Text(
                text = "$fps FPS",
                modifier = Modifier.padding(FpsCardPadding),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontSize = FpsTextSize,
                    ),
            )
        }
    }
}

private val FpsCardPadding = 4.dp
private val FpsTextSize = 16.sp
