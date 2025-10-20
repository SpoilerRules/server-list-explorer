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
        Text(
            text = item.text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    },
    onClick = {
        item.onClick()
        closeRoot()
    },
    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
    leadingIcon =
        item.icon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
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
                    maxLines = 1,
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
                            modifier = Modifier.size(24.dp),
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
                        maxLines = 1,
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
                                modifier = Modifier.size(24.dp),
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
                                maxLines = 1,
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
                                            modifier = Modifier.size(24.dp),
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
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
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(250),
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.widthIn(min = 112.dp, max = 280.dp),
    ) {
        OutlinedButton(
            shape = MaterialTheme.shapes.extraSmall,
            modifier =
                Modifier
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    .pointerHoverIcon(PointerIcon.Hand),
            onClick = { /* handled by ExposedDropdownMenuBox */ },
            contentPadding = PaddingValues(all = 12.dp),
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
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = selected.text,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation).size(24.dp),
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
                            maxLines = 1,
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
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
