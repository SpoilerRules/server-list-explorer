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

package com.spoiligaming.explorer.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.t
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.dialog_cancel_button
import server_list_explorer.ui.generated.resources.dialog_save_button

@Composable
internal fun SimpleTextEditDialog(
    title: String,
    initialText: String,
    onConfirm: (String) -> Unit,
    onDismissRequest: () -> Unit,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    confirmButtonText: String = t(Res.string.dialog_save_button),
    cancelButtonText: String = t(Res.string.dialog_cancel_button),
    autoFocus: Boolean = true,
    textValidator: ((String) -> String?)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
) {
    var text by rememberSaveable { mutableStateOf(initialText) }
    var error by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    ExpressiveDialog(
        onDismissRequest = onDismissRequest,
    ) {
        title(title)
        supportText(supportingText)

        body {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = DialogVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(FieldVerticalSpacing),
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        error = textValidator?.invoke(it)
                    },
                    label = label?.let { { Text(it) } },
                    placeholder = placeholder?.let { { Text(it) } },
                    isError = error != null,
                    singleLine = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .let { if (autoFocus) it.focusRequester(focusRequester) else it },
                    keyboardOptions = keyboardOptions,
                    keyboardActions =
                        KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            val validationError = textValidator?.invoke(text)
                            if (validationError == null) {
                                onConfirm(text)
                            } else {
                                error = validationError
                            }
                        }),
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                if (autoFocus) {
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                }
            }
        }

        cancel(cancelButtonText onClick onDismissRequest)
        accept(
            confirmButtonText onClick {
                val validationError = textValidator?.invoke(text)
                if (validationError == null) {
                    onConfirm(text)
                } else {
                    error = validationError
                }
            },
        )
        modifier = Modifier.fillMaxWidth(0.85f)
    }
}

private val DialogVerticalPadding = 8.dp
private val FieldVerticalSpacing = 8.dp
