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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

internal data class DropdownOption(
    val text: String,
    val icon: ImageVector? = null,
    val selectedIcon: ImageVector? = null,
)

internal sealed interface MenuEntry

internal data class ActionItem(
    val text: String,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
) : MenuEntry

internal data class SubmenuItem(
    val text: String,
    val icon: ImageVector? = null,
    val children: List<MenuEntry>,
) : MenuEntry

internal data class SelectableGroupItem(
    val text: String,
    val icon: ImageVector? = null,
    val options: List<DropdownOption>,
    val selected: DropdownOption,
    val onOptionSelected: (DropdownOption) -> Unit,
) : MenuEntry

@Composable
internal fun HierarchicalDropdownMenu(
    entries: List<MenuEntry>,
    modifier: Modifier = Modifier,
    anchor: @Composable (expanded: Boolean, toggle: () -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        anchor(expanded) { expanded = !expanded }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            entries.forEach { entry ->
                MenuEntryItem(entry, closeRoot = { expanded = false })
            }
        }
    }
}

@Composable
private fun MenuEntryItem(
    entry: MenuEntry,
    closeRoot: () -> Unit,
) = when (entry) {
    is ActionItem -> ActionItemContent(entry, closeRoot)
    is SubmenuItem -> SubmenuItemContent(entry, closeRoot)
    is SelectableGroupItem -> SelectableGroupContent(entry, closeRoot)
}

@Composable
private fun ActionItemContent(
    item: ActionItem,
    closeRoot: () -> Unit,
) = DropdownMenuItem(
    text = {
        val textColor =
            if (item.enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
            }
        Text(
            text = item.text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            maxLines = SINGLE_LINE_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
        )
    },
    onClick = {
        item.onClick()
        closeRoot()
    },
    enabled = item.enabled,
    modifier =
        Modifier.pointerHoverIcon(
            if (item.enabled) PointerIcon.Hand else PointerIcon.Default,
        ),
    leadingIcon =
        item.icon?.let { icon ->
            val iconTint =
                if (item.enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DISABLED_CONTENT_ALPHA)
                }
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(MenuItemIconSize),
                )
            }
        },
    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
)

@Composable
private fun SubmenuItemContent(
    item: SubmenuItem,
    closeRoot: () -> Unit,
) {
    var childExpanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    var itemSize by remember { mutableStateOf(IntSize.Zero) }

    val hasChildren = item.children.isNotEmpty()

    Box(Modifier.onGloballyPositioned { itemSize = it.size }) {
        DropdownMenuItem(
            text = {
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = SINGLE_LINE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            onClick = {
                if (hasChildren) childExpanded = !childExpanded else closeRoot()
            },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
            leadingIcon =
                item.icon?.let { icon ->
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(MenuItemIconSize),
                        )
                    }
                },
            trailingIcon = {
                Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null)
            },
            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
        )
    }

    if (hasChildren && childExpanded) {
        val xOffset = with(density) { itemSize.width.toDp() }
        val yOffset = with(density) { -itemSize.height.toDp() }

        DropdownMenu(
            expanded = true,
            onDismissRequest = { childExpanded = false },
            offset = DpOffset(x = xOffset, y = yOffset),
        ) {
            item.children.forEach { child ->
                MenuEntryItem(child, closeRoot)
            }
        }
    }
}

@Composable
private fun SelectableGroupContent(
    item: SelectableGroupItem,
    closeRoot: () -> Unit,
) {
    var childExpanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    var itemSize by remember { mutableStateOf(IntSize.Zero) }

    Box {
        Box(Modifier.onGloballyPositioned { itemSize = it.size }) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = SINGLE_LINE_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                onClick = { childExpanded = !childExpanded },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                leadingIcon =
                    item.icon?.let { icon ->
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(MenuItemIconSize),
                            )
                        }
                    },
                trailingIcon = {
                    TrailingOptionRow(selected = item.selected)
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )
        }

        if (childExpanded) {
            val xOffset = with(density) { itemSize.width.toDp() }
            val yOffset = with(density) { -itemSize.height.toDp() }

            DropdownMenu(
                expanded = true,
                onDismissRequest = { childExpanded = false },
                offset = DpOffset(x = xOffset, y = yOffset),
            ) {
                item.options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.text,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = SINGLE_LINE_MAX_LINES,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        onClick = {
                            item.onOptionSelected(option)
                            closeRoot()
                        },
                        modifier =
                            Modifier
                                .pointerHoverIcon(PointerIcon.Hand)
                                .then(
                                    if (option == item.selected) {
                                        Modifier.background(
                                            MaterialTheme.colorScheme.secondaryContainer,
                                        )
                                    } else {
                                        Modifier
                                    },
                                ),
                        leadingIcon =
                            option
                                .resolveIcon(isSelected = option == item.selected)
                                ?.let { icon ->
                                    {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(MenuItemIconSize),
                                        )
                                    }
                                },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@Composable
private fun TrailingOptionRow(selected: DropdownOption) =
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = selected.text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = SINGLE_LINE_MAX_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(MenuItemIconSize),
        )
    }

private fun DropdownOption.resolveIcon(isSelected: Boolean) =
    when {
        !isSelected -> icon
        else -> selectedIcon ?: icon
    }

@Composable
internal fun SelectableDropdown(
    selected: DropdownOption,
    options: List<DropdownOption>,
    onOptionSelected: (DropdownOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) DROPDOWN_ROTATION_EXPANDED_DEGREES else DROPDOWN_ROTATION_COLLAPSED_DEGREES,
        animationSpec = tween(DROPDOWN_ANIMATION_DURATION_MILLIS),
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.widthIn(min = DropdownMinWidth, max = DropdownMaxWidth),
    ) {
        OutlinedButton(
            shape = MaterialTheme.shapes.extraSmall,
            modifier =
                Modifier
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    .pointerHoverIcon(PointerIcon.Hand),
            onClick = { /* handled by ExposedDropdownMenuBox */ },
            contentPadding = PaddingValues(all = DropdownContentPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                selected.resolveIcon(isSelected = true)?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(MenuItemIconSize),
                    )
                }
                Spacer(Modifier.width(DropdownContentPadding))
                Text(
                    text = selected.text,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = SINGLE_LINE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(MENU_TEXT_WEIGHT),
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation).size(MenuItemIconSize),
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
                            text = option.text,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = SINGLE_LINE_MAX_LINES,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    modifier =
                        Modifier
                            .background(
                                if (selected == option) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                },
                            ).pointerHoverIcon(PointerIcon.Hand),
                    leadingIcon =
                        option.resolveIcon(isSelected = option == selected)?.let { icon ->
                            {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(MenuItemIconSize),
                                )
                            }
                        },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

private const val DISABLED_CONTENT_ALPHA = 0.38f
private const val SINGLE_LINE_MAX_LINES = 1
private const val DROPDOWN_ROTATION_EXPANDED_DEGREES = 180f
private const val DROPDOWN_ROTATION_COLLAPSED_DEGREES = 0f
private const val DROPDOWN_ANIMATION_DURATION_MILLIS = 250
private const val MENU_TEXT_WEIGHT = 1f

private val MenuItemIconSize = 24.dp
private val DropdownContentPadding = 12.dp
private val DropdownMinWidth = 112.dp
private val DropdownMaxWidth = 280.dp
