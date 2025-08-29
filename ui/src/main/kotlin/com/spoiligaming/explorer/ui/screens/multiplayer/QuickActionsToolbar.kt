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

package com.spoiligaming.explorer.ui.screens.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.DatasetLinked
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.settings.model.ActionBarOrientation
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalMultiplayerSettings
import com.spoiligaming.explorer.ui.t
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.label_server_list
import server_list_explorer.ui.generated.resources.quick_action_add
import server_list_explorer.ui.generated.resources.quick_action_delete_all
import server_list_explorer.ui.generated.resources.quick_action_delete_selected
import server_list_explorer.ui.generated.resources.quick_action_deselect_all
import server_list_explorer.ui.generated.resources.quick_action_query_method
import server_list_explorer.ui.generated.resources.quick_action_refresh_all
import server_list_explorer.ui.generated.resources.quick_action_refresh_selected
import server_list_explorer.ui.generated.resources.quick_action_select_all
import server_list_explorer.ui.generated.resources.quick_action_sort

@Composable
internal fun QuickActionsToolbar(
    shimmer: Shimmer?,
    totalCount: Int,
    selectedCount: Int,
    onAdd: () -> Unit,
    onSort: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onRefresh: (onlySelected: Boolean) -> Unit,
    onDeleteAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onShowQueryMethodDialog: () -> Unit,
    onShowServerListFileConfigurationDialog: () -> Unit,
) {
    val mp = LocalMultiplayerSettings.current
    val isVertical =
        mp.actionBarOrientation == ActionBarOrientation.Left || mp.actionBarOrientation == ActionBarOrientation.Right

    val content: @Composable (@Composable () -> Unit) -> Unit = { inner ->
        if (isVertical) {
            Column(
                modifier =
                    Modifier
                        .width(IntrinsicSize.Min)
                        .widthIn(VerticalToolbarMinWidth)
                        .fillMaxHeight()
                        .padding(ToolbarPadding)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ToolbarSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { inner() }
        } else {
            Row(
                modifier =
                    Modifier
                        .height(IntrinsicSize.Min)
                        .heightIn(min = HorizontalToolbarMinHeight)
                        .fillMaxWidth()
                        .padding(ToolbarPadding),
                horizontalArrangement = Arrangement.spacedBy(ToolbarSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) { inner() }
        }
    }

    val divider = @Composable {
        if (isVertical) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(vertical = DividerPadding),
                thickness = DividerThickness,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        } else {
            VerticalDivider(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .padding(horizontal = DividerPadding),
                thickness = DividerThickness,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }

    val elevatedCardModifier =
        when (mp.actionBarOrientation) {
            ActionBarOrientation.Right, ActionBarOrientation.Left ->
                Modifier.fillMaxHeight().padding(bottom = BottomPadding)

            ActionBarOrientation.Top ->
                Modifier.fillMaxWidth()

            ActionBarOrientation.Bottom ->
                Modifier.fillMaxWidth().padding(bottom = BottomPadding)
        }

    ElevatedCard(
        modifier = elevatedCardModifier,
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        content {
            QuickActionButton(
                icon = Icons.Default.Add,
                label = t(Res.string.quick_action_add),
                shimmer = null,
                onClick = onAdd,
            )
            QuickActionButton(
                icon = Icons.AutoMirrored.Default.Sort,
                label = t(Res.string.quick_action_sort),
                shimmer = shimmer,
                onClick = onSort,
            )
            divider()
            QuickActionButton(
                icon = Icons.Default.Refresh,
                label = t(Res.string.quick_action_refresh_selected),
                shimmer = shimmer,
                onClick = { onRefresh(true) },
                enabled = selectedCount > 0,
            )
            QuickActionButton(
                icon = Icons.Default.Delete,
                label = t(Res.string.quick_action_delete_selected),
                shimmer = shimmer,
                onClick = onDeleteSelected,
                enabled = selectedCount > 0,
            )
            divider()
            QuickActionButton(
                icon = Icons.Default.Refresh,
                label = t(Res.string.quick_action_refresh_all),
                shimmer = shimmer,
                onClick = { onRefresh(false) },
                enabled = totalCount > 0,
            )
            QuickActionButton(
                icon = Icons.Default.DeleteForever,
                label = t(Res.string.quick_action_delete_all),
                shimmer = shimmer,
                onClick = onDeleteAll,
                enabled = totalCount > 0,
            )
            val allSelected = selectedCount == totalCount && totalCount > 0
            QuickActionButton(
                icon = if (allSelected) Icons.Default.CheckBoxOutlineBlank else Icons.Default.SelectAll,
                label =
                    if (allSelected) {
                        t(
                            Res.string.quick_action_deselect_all,
                        )
                    } else {
                        t(Res.string.quick_action_select_all)
                    },
                shimmer = shimmer,
                onClick = { if (allSelected) onClearSelection() else onSelectAll() },
            )
            divider()
            QuickActionButton(
                icon = Icons.Default.DatasetLinked,
                label = t(Res.string.quick_action_query_method),
                shimmer = null,
                onClick = onShowQueryMethodDialog,
            )
            QuickActionButton(
                icon = Icons.Default.Storage,
                label = t(Res.string.label_server_list),
                shimmer = null,
                onClick = onShowServerListFileConfigurationDialog,
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    shimmer: Shimmer?,
    onClick: () -> Unit,
    enabled: Boolean = shimmer == null,
) {
    val textColor =
        MaterialTheme.colorScheme.onSurfaceVariant.copy(
            alpha = if (enabled) ENABLED_ICON_ALPHA else DISABLED_ICON_ALPHA,
        )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            modifier =
                Modifier.pointerHoverIcon(
                    if (shimmer == null && enabled) PointerIcon.Hand else PointerIcon.Default,
                ),
            enabled = enabled,
            onClick = onClick,
        ) {
            if (shimmer != null) {
                Box(
                    modifier =
                        Modifier
                            .shimmer(shimmer)
                            .size(ShimmerIconSize)
                            .background(
                                color =
                                    MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = SHIMMER_ICON_ALPHA,
                                    ),
                                shape = CircleShape,
                            ),
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = textColor,
                )
            }
        }
        if (shimmer != null) {
            Box(
                modifier =
                    Modifier
                        .shimmer(shimmer)
                        .width((label.length * TEXT_LABEL_LENGTH_DP_MULTIPLIER).dp)
                        .height(ShimmerLabelHeight)
                        .background(
                            color =
                                MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = SHIMMER_LABEL_ALPHA,
                                ),
                            shape = MaterialTheme.shapes.small,
                        ),
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val ToolbarPadding = 8.dp
private val ToolbarSpacing = 8.dp
private val VerticalToolbarMinWidth = 96.dp
private val HorizontalToolbarMinHeight = 96.dp
private val DividerPadding = 4.dp
private val DividerThickness = 1.dp
private val BottomPadding = 12.dp
private val ShimmerIconSize = 24.dp
private val ShimmerLabelHeight = 16.dp
private const val SHIMMER_LABEL_ALPHA = 0.15f
private const val SHIMMER_ICON_ALPHA = 0.12f
private const val TEXT_LABEL_LENGTH_DP_MULTIPLIER = 6
private const val DISABLED_ICON_ALPHA = 0.38f
private const val ENABLED_ICON_ALPHA = 1f
