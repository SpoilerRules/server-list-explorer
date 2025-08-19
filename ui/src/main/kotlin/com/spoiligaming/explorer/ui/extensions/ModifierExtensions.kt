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

package com.spoiligaming.explorer.ui.extensions

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints

internal fun Modifier.onHover(onHover: (Boolean) -> Unit) =
    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                when (event.type) {
                    PointerEventType.Enter -> onHover(true)
                    PointerEventType.Exit -> onHover(false)
                }
            }
        }
    }

internal fun Modifier.clickWithModifiers(
    onClick: (ctrlPressed: Boolean, shiftPressed: Boolean, metaPressed: Boolean) -> Unit,
) = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val downEvent = awaitPointerEvent(PointerEventPass.Final)
            if (downEvent.type == PointerEventType.Press && downEvent.buttons.isPrimaryPressed) {
                if (downEvent.changes.any { it.isConsumed }) {
                    continue
                }
                val mods = downEvent.keyboardModifiers
                while (true) {
                    val upEvent = awaitPointerEvent(PointerEventPass.Main)
                    if (upEvent.changes.all { !it.pressed }) {
                        onClick(
                            mods.isCtrlPressed,
                            mods.isShiftPressed,
                            mods.isMetaPressed,
                        )
                        break
                    }
                }
            }
        }
    }
}

internal fun Modifier.blockIntrinsics() =
    then(
        object : LayoutModifier {
            override fun MeasureScope.measure(
                measurable: Measurable,
                constraints: Constraints,
            ): MeasureResult {
                val placeable = measurable.measure(constraints)
                return layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }

            override fun IntrinsicMeasureScope.minIntrinsicWidth(
                measurable: IntrinsicMeasurable,
                height: Int,
            ) = 0

            override fun IntrinsicMeasureScope.maxIntrinsicWidth(
                measurable: IntrinsicMeasurable,
                height: Int,
            ) = 0

            override fun IntrinsicMeasureScope.minIntrinsicHeight(
                measurable: IntrinsicMeasurable,
                width: Int,
            ) = 0

            override fun IntrinsicMeasureScope.maxIntrinsicHeight(
                measurable: IntrinsicMeasurable,
                width: Int,
            ) = 0
        },
    )
