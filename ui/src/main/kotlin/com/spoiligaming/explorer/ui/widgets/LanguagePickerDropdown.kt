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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.decodeToSvgPainter
import server_list_explorer.ui.generated.resources.Res
import java.util.Locale

private data class LanguageOption(
    val locale: Locale,
    val svgPath: String,
    val label: String,
)

@Composable
private fun loadSvgPainter(path: String): Painter? {
    val density = LocalDensity.current
    var painter: Painter? by remember(path) { mutableStateOf(null) }
    var bytes by remember { mutableStateOf(ByteArray(0)) }

    LaunchedEffect(path) {
        bytes = Res.readBytes(path)
        painter = bytes.decodeToSvgPainter(density)
    }

    return painter
}

@Composable
internal fun LanguagePickerDropdownMenu(
    selectedLocale: Locale,
    onLocaleSelected: (Locale) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options =
        listOf(
            LanguageOption(Locale.US, "drawable/flag_us.svg", "English (US)"),
            LanguageOption(Locale.UK, "drawable/flag_uk.svg", "English (UK)"),
        )

    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = ROTATION_DURATION_MS),
    )

    val selectedOption = options.find { it.locale == selectedLocale } ?: options.first()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.widthIn(min = DropdownMenuMinWidth, max = DropdownMenuMaxWidth),
    ) {
        OutlinedButton(
            shape = MaterialTheme.shapes.extraSmall,
            modifier =
                Modifier
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    .pointerHoverIcon(PointerIcon.Hand),
            onClick = {},
            contentPadding = PaddingValues(DropdownMenuButtonPadding),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth(),
            ) {
                loadSvgPainter(selectedOption.svgPath)?.let { svgPainter ->
                    Image(
                        painter = svgPainter,
                        contentDescription = null,
                        modifier = Modifier.size(FlagIconSize),
                    )
                }
                Spacer(Modifier.width(SpacingAfterIcon))
                Text(
                    text = selectedOption.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(MENU_BUTTON_WEIGHT),
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .rotate(rotationAngle)
                            .size(DropdownArrowIconSize),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    leadingIcon = {
                        loadSvgPainter(option.svgPath)?.let { svgPainter ->
                            Image(
                                painter = svgPainter,
                                contentDescription = null,
                                modifier = Modifier.size(FlagIconSize),
                            )
                        }
                    },
                    onClick = {
                        onLocaleSelected(option.locale)
                        expanded = false
                    },
                    modifier =
                        Modifier
                            .background(
                                if (option.locale == selectedLocale) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                },
                            ).pointerHoverIcon(PointerIcon.Hand),
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
internal fun ItemLanguagePickerDropdownMenu(
    title: String,
    description: String? = null,
    note: String? = null,
    conflictReason: String? = null,
    selectedLocale: Locale,
    onLocaleSelected: (Locale) -> Unit,
) = SettingTile(
    title = title,
    description = description,
    note = note,
    conflictReason = conflictReason,
    trailingContent = {
        LanguagePickerDropdownMenu(
            selectedLocale = selectedLocale,
            onLocaleSelected = onLocaleSelected,
            modifier = Modifier.fillMaxWidth(COMPONENT_WIDTH_FILL_FRACTION),
        )
    },
)

private val FlagIconSize = 24.dp
private val DropdownMenuMinWidth = 112.dp
private val DropdownMenuMaxWidth = 280.dp
private val DropdownMenuButtonPadding = 12.dp
private val SpacingAfterIcon = 12.dp
private val DropdownArrowIconSize = 24.dp
private const val MENU_BUTTON_WEIGHT = 1f
private const val ROTATION_DURATION_MS = 250
private const val COMPONENT_WIDTH_FILL_FRACTION = 0.25f
