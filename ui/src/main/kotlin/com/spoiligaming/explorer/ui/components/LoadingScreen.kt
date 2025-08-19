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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
internal fun LoadingScreen(
    steps: List<Pair<String, suspend () -> Unit>>,
    modifier: Modifier = Modifier,
    displayAfterThreshold: Boolean,
    content: @Composable () -> Unit,
) {
    val total = steps.size
    var current by remember { mutableStateOf(0) }
    var shouldDisplay by remember { mutableStateOf(!displayAfterThreshold) }

    LaunchedEffect(steps) {
        if (displayAfterThreshold) {
            delay(DISPLAY_THRESHOLD_MS.toLong())
            shouldDisplay = true
        }
        steps.forEachIndexed { index, (_, block) ->
            current = index + 1
            block()
        }
        current = total + 1
    }

    val targetProgress =
        if (total == 0) {
            PROGRESS_MAX
        } else {
            current.coerceAtMost(total) / total.toFloat()
        }

    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec =
            tween(
                durationMillis = LOADING_ANIMATION_DURATION_MS,
                easing = DefaultEasing,
            ),
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = shouldDisplay && current <= total,
            enter = fadeIn(FadeAnimationSpec),
            exit = fadeOut(FadeAnimationSpec),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LabelSpacing),
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier =
                        Modifier
                            .fillMaxWidth(PROGRESS_BAR_WIDTH_FRACTION)
                            .height(LinearProgressHeight),
                )

                val label =
                    steps
                        .getOrNull(current.coerceIn(STEP_MIN_INDEX, total) - 1)
                        ?.first
                        .orEmpty()
                val stepText =
                    if (total > 0) {
                        "$label (${current.coerceAtMost(total)}/$total)"
                    } else {
                        ""
                    }
                Text(
                    text = stepText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        AnimatedVisibility(
            visible = current > total,
            enter = fadeIn(FadeAnimationSpec),
            exit = fadeOut(FadeAnimationSpec),
        ) {
            content()
        }
    }
}

@Composable
internal fun CircularLoadingScreen(
    label: String,
    progress: Float,
    task: suspend () -> Unit,
    modifier: Modifier = Modifier,
    displayAfterThreshold: Boolean,
    content: @Composable () -> Unit,
) {
    var isDone by remember { mutableStateOf(false) }
    var shouldDisplay by remember { mutableStateOf(!displayAfterThreshold) }

    LaunchedEffect(task) {
        if (displayAfterThreshold) {
            delay(DISPLAY_THRESHOLD_MS.toLong())
            shouldDisplay = true
        }
        task()
        isDone = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(PROGRESS_MIN, PROGRESS_MAX),
        animationSpec = CircularProgressAnimationSpec,
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = shouldDisplay && !isDone,
            enter = fadeIn(FadeAnimationSpec),
            exit = fadeOut(FadeAnimationSpec),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LabelSpacing),
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        AnimatedVisibility(
            visible = isDone,
            enter = fadeIn(FadeAnimationSpec),
            exit = fadeOut(FadeAnimationSpec),
        ) {
            content()
        }
    }
}

private const val LOADING_ANIMATION_DURATION_MS = 300
private const val DISPLAY_THRESHOLD_MS = 200
private const val CIRCULAR_PROGRESS_ANIMATION_DURATION_MS = 500
private const val PROGRESS_BAR_WIDTH_FRACTION = 0.3f
private const val PROGRESS_MIN = 0f
private const val PROGRESS_MAX = 1f
private const val STEP_MIN_INDEX = 1

private val LabelSpacing = 12.dp
private val LinearProgressHeight = 4.dp
private val DefaultEasing = FastOutSlowInEasing

private val FadeAnimationSpec: FiniteAnimationSpec<Float> =
    tween(durationMillis = LOADING_ANIMATION_DURATION_MS, easing = DefaultEasing)
private val CircularProgressAnimationSpec: FiniteAnimationSpec<Float> =
    tween(durationMillis = CIRCULAR_PROGRESS_ANIMATION_DURATION_MS, easing = DefaultEasing)
