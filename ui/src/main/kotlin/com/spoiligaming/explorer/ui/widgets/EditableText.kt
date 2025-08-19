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

package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.extensions.onHover
import kotlinx.coroutines.launch

@Composable
internal fun EditableText(
    value: String,
    onValueChange: (String) -> Unit,
    validate: (String) -> Boolean = { true },
    onConfirm: (String) -> Unit = {},
    onCancel: () -> Unit = {},
    textStyle: TextStyle = LocalTextStyle.current,
) {
    var isEditing by remember { mutableStateOf(value.isEmpty()) }
    var draft by remember { mutableStateOf(value) }
    var hovered by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(isEditing) {
        if (isEditing) scope.launch { focusRequester.requestFocus() }
    }

    val hoverMod =
        Modifier
            .fillMaxWidth()
            .onHover { hovered = it }
            .background(
                if (!isEditing && hovered) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surface
                },
            )
            .clickable(enabled = !isEditing) {
                draft = value
                isEditing = true
            }
            .padding(4.dp)

    if (isEditing) {
        BasicTextField(
            value = draft,
            onValueChange = {
                draft = it
                if (validate(it)) onValueChange(it)
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onPreviewKeyEvent { ev ->
                        if (ev.type != KeyEventType.KeyUp) return@onPreviewKeyEvent false
                        when (ev.key) {
                            Key.Enter -> {
                                tryConfirm(draft, validate, onConfirm) { isEditing = false }
                                true
                            }

                            Key.Escape -> {
                                draft = value
                                isEditing = false
                                onCancel()
                                true
                            }

                            else -> false
                        }
                    }
                    .onFocusChanged { fs ->
                        if (!fs.isFocused && isEditing) {
                            tryConfirm(draft, validate, onConfirm) { isEditing = false }
                        }
                    }
                    .padding(4.dp),
            textStyle = textStyle,
        )
    } else {
        SelectionContainer {
            Box(modifier = hoverMod, contentAlignment = Alignment.CenterStart) {
                Text(
                    text = value.ifEmpty { "\u2014" },
                    style = textStyle,
                    modifier = Modifier.wrapContentHeight(),
                )
            }
        }
    }
}

private fun tryConfirm(
    candidate: String,
    validate: (String) -> Boolean,
    onConfirm: (String) -> Unit,
    onEditDone: () -> Unit,
) {
    if (!validate(candidate)) return
    onConfirm(candidate)
    onEditDone()
}
