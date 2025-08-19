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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.spoiligaming.explorer.settings.model.ThemeSettings
import com.spoiligaming.explorer.ui.dialog.ExpressiveDialog
import com.spoiligaming.explorer.ui.dialog.onClick
import com.spoiligaming.explorer.ui.extensions.toComposeColor
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.util.rememberAdaptiveHeight
import com.spoiligaming.explorer.ui.util.rememberAdaptiveWidth
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.color_picker_button_restore
import server_list_explorer.ui.generated.resources.color_picker_dialog_title
import server_list_explorer.ui.generated.resources.dialog_cancel_button
import server_list_explorer.ui.generated.resources.dialog_save_button

@Composable
internal fun ItemColorPicker(
    currentColor: Color,
    showRestoreButton: Boolean,
    onConfirm: (Color) -> Unit,
) {
    var showColorDialog by remember { mutableStateOf(false) }

    Box(
        modifier =
            Modifier
                .size(ItemColorPickerSize)
                .background(
                    color = currentColor,
                    shape = CardDefaults.shape,
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    showColorDialog = true
                }
                .pointerHoverIcon(PointerIcon.Hand),
    )

    if (showColorDialog) {
        ColorPickerDialog(
            initialColor = currentColor,
            showRestoreButton = showRestoreButton,
            onDismissRequest = { showColorDialog = false },
            onColorSelected = { newColor ->
                onConfirm(newColor)
                showColorDialog = false
            },
        )
    }
}

@Composable
private fun ColorPickerDialog(
    initialColor: Color,
    showRestoreButton: Boolean,
    onDismissRequest: () -> Unit,
    onColorSelected: (selectedColor: Color) -> Unit,
) {
    var hsvColor by rememberSaveable { mutableStateOf(HsvColor.from(initialColor)) }
    var resetToggle by remember { mutableStateOf(false) }

    LaunchedEffect(initialColor) {
        hsvColor = HsvColor.from(initialColor)
    }

    val colorPickerWidth =
        rememberAdaptiveWidth(
            min = ColorPickerWidthMin,
            max = ColorPickerWidthMax,
            fraction = COLOR_PICKER_WIDTH_FRACTION,
        )
    val colorPickerHeight =
        rememberAdaptiveHeight(
            min = ColorPickerHeightMin,
            max = ColorPickerHeightMax,
            fraction = COLOR_PICKER_HEIGHT_FRACTION,
        )

    val colorPickerDialogTitleText = t(Res.string.color_picker_dialog_title)
    val colorPickerButtonRestore = t(Res.string.color_picker_button_restore)
    val cancelButtonText = t(Res.string.dialog_cancel_button)
    val saveButtonText = t(Res.string.dialog_save_button)

    ExpressiveDialog(onDismissRequest = onDismissRequest) {
        title(colorPickerDialogTitleText)

        body {
            Column(verticalArrangement = Arrangement.spacedBy(ColorPickerColumnArrangement)) {
                key(resetToggle) {
                    ClassicColorPicker(
                        modifier =
                            Modifier
                                .width(colorPickerWidth)
                                .height(colorPickerHeight),
                        color = hsvColor,
                        onColorChanged = { newHsv ->
                            hsvColor = newHsv
                        },
                    )
                }
            }
        }

        if (showRestoreButton) {
            extra(
                colorPickerButtonRestore onClick {
                    hsvColor = HsvColor.from(ThemeSettings.DEFAULT_SEED_COLOR.toComposeColor())
                    resetToggle = !resetToggle
                },
            )
        }

        cancel(cancelButtonText onClick { onDismissRequest() })

        accept(
            saveButtonText onClick {
                onColorSelected(hsvColor.toColor())
            },
        )
        modifier = Modifier.width(IntrinsicSize.Max)
    }
}

@Composable
internal fun ItemColorPicker(
    title: String,
    description: String,
    note: String? = null,
    conflictReason: String? = null,
    currentColor: Color,
    restoreButton: Boolean,
    onConfirm: (Color) -> Unit,
) = SettingTile(
    title = title,
    description = description,
    note = note,
    conflictReason = conflictReason,
    trailingContent = {
        ItemColorPicker(
            currentColor = currentColor,
            showRestoreButton = restoreButton,
            onConfirm = onConfirm,
        )
    },
)

private val ItemColorPickerSize = 48.dp

private val ColorPickerColumnArrangement = 16.dp
private val ColorPickerWidthMin = 280.dp
private val ColorPickerWidthMax = 560.dp
private const val COLOR_PICKER_WIDTH_FRACTION = 0.27f
private val ColorPickerHeightMin = 180.dp
private val ColorPickerHeightMax = 560.dp
private const val COLOR_PICKER_HEIGHT_FRACTION = 0.3f
