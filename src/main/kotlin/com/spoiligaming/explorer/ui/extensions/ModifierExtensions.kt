package com.spoiligaming.explorer.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun Modifier.baseHoverColor(
    color: Color,
    other: (Color) -> Unit,
): Modifier {
    val modifiedColor =
        color.copy(
            red = color.red * 1.1f,
            blue = color.blue * 1.1f,
            green = color.green * 1.1f,
        )

    other(modifiedColor)
    return this
}

fun Modifier.onHover(onHover: (Boolean) -> Unit) =
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
