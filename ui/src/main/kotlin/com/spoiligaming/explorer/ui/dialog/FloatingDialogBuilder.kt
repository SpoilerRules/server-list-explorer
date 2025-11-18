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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.spoiligaming.explorer.ui.dialog

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.TooltipScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Composable
internal fun FloatingDialogBuilder(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    positionProvider: PopupPositionProvider =
        rememberTooltipPositionProvider(
            TooltipAnchorPosition.End,
        ),
    content: @Composable TooltipScope.() -> Unit,
) {
    var anchorCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val realScope =
        remember {
            val clazz = Class.forName(TooltipScopeImplClassName)
            val ctor =
                clazz.declaredConstructors
                    .first { it.parameterCount == TooltipCtorParamCount }
                    .apply { isAccessible = true }

            ctor.newInstance({ anchorCoords }, positionProvider) as TooltipScope
        }

    val transitionState =
        remember {
            MutableTransitionState(false).apply { targetState = false }
        }
    LaunchedEffect(visible) {
        transitionState.targetState = visible
    }

    if (transitionState.currentState || transitionState.targetState) {
        val transition = rememberTransition(transitionState, label = "floating dialog")
        Popup(
            popupPositionProvider = positionProvider,
            properties = PopupProperties(focusable = PopupIsFocusable),
            onDismissRequest = onDismissRequest,
        ) {
            Box(
                modifier =
                    Modifier
                        .onGloballyPositioned { anchorCoords = it }
                        .animateFloatingDialog(transition),
            ) {
                with(realScope) { content() }
            }
        }
    }
}

@Composable
private fun Modifier.animateFloatingDialog(transition: Transition<Boolean>): Modifier {
    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // show dialog
                tween(durationMillis = DialogFadeInDurationMs, easing = ScaleShowEasing)
            } else {
                // dismiss dialog
                tween(durationMillis = DialogFadeOutDurationMs, easing = ScaleHideEasing)
            }
        },
        label = "dialog transition: scaling",
    ) { shown -> if (shown) ScaleShown else ScaleHidden }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // show dialog
                tween(durationMillis = DialogFadeInDurationMs, easing = AlphaShowEasing)
            } else {
                // dismiss dialog
                tween(durationMillis = DialogFadeOutDurationMs, easing = AlphaHideEasing)
            }
        },
        label = "dialog transition: alpha",
    ) { shown -> if (shown) AlphaShown else AlphaHidden }

    return this.graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
}

private const val DialogFadeInDurationMs = 150
internal const val DialogFadeOutDurationMs = 75

private const val TooltipScopeImplClassName = "androidx.compose.material3.TooltipScopeImpl"
private const val TooltipCtorParamCount = 2

private const val PopupIsFocusable = true

private const val ScaleShown = 1f
private const val ScaleHidden = 0.8f
private const val AlphaShown = 1f
private const val AlphaHidden = 0f

private val ScaleShowEasing = LinearOutSlowInEasing
private val ScaleHideEasing = LinearOutSlowInEasing
private val AlphaShowEasing = LinearEasing
private val AlphaHideEasing = LinearEasing
