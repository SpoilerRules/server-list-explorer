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

package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.dialog.FloatingDialogBuilder
import com.spoiligaming.explorer.ui.t
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.dialog_save_button
import server_list_explorer.ui.generated.resources.inline_edit_button_edit
import server_list_explorer.ui.generated.resources.inline_edit_dialog_title
import server_list_explorer.ui.generated.resources.inline_edit_label_new_value
import server_list_explorer.ui.generated.resources.inline_edit_textfield_placeholder

@Composable
internal fun InlineEditableLabel(
    text: AnnotatedString,
    textStyle: TextStyle,
    inlineContent: @Composable (content: AnnotatedString, style: TextStyle) -> Unit = { content, style ->
        OverflowAwareInlineTooltipText(content = content, textStyle = style)
    },
    dialogTitle: String? = null,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier,
    editButtonLabel: String? = null,
    saveButtonLabel: String? = null,
    textFieldPlaceholder: String? = null,
) {
    var showEditor by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf(text.text) }

    val editButtonText = editButtonLabel ?: t(Res.string.inline_edit_button_edit)
    val saveButtonText = saveButtonLabel ?: t(Res.string.dialog_save_button)
    val placeholderText = textFieldPlaceholder ?: t(Res.string.inline_edit_textfield_placeholder)
    val dialogTitleText = dialogTitle ?: t(Res.string.inline_edit_dialog_title)
    val newValueLabel = t(Res.string.inline_edit_label_new_value)

    LaunchedEffect(showEditor) {
        if (!showEditor) {
            editedText = text.text
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(CONTENT_WEIGHT)) {
            inlineContent(text, textStyle)
        }

        SlickTextButton(
            onClick = { showEditor = true },
            modifier = Modifier.height(EditButtonHeight),
        ) {
            Text(
                text = editButtonText,
                style = MaterialTheme.typography.labelMedium,
                maxLines = INLINE_TEXT_MAX_LINES,
            )

            FloatingDialogBuilder(
                visible = showEditor,
                onDismissRequest = { showEditor = false },
            ) {
                RichTooltip(
                    title = { Text(dialogTitleText) },
                    text = {
                        OutlinedTextField(
                            value = editedText,
                            onValueChange = { editedText = it },
                            singleLine = true,
                            label = { Text(newValueLabel) },
                            placeholder = { Text(placeholderText) },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = EditFieldTopPadding)
                                    .onKeyEvent { e ->
                                        if (e.key == Key.Enter || e.key == Key.NumPadEnter) {
                                            onSave(editedText)
                                            showEditor = false
                                            true
                                        } else {
                                            false
                                        }
                                    },
                        )
                    },
                    action = {
                        TextButton(
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                            onClick = {
                                onSave(editedText)
                                showEditor = false
                            },
                        ) {
                            Text(saveButtonText)
                        }
                    },
                )
            }
        }
    }
}

private const val CONTENT_WEIGHT = 1f
private const val INLINE_TEXT_MAX_LINES = 1
private val EditButtonHeight = 32.dp
private val EditFieldTopPadding = 4.dp
