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

package com.spoiligaming.explorer.ui.screens.multiplayer.querymethod

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.dialog.FloatingDialogBuilder
import com.spoiligaming.explorer.ui.t
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.query_method_tooltip_title

@Composable
internal fun QueryMethodOptionCard(
    definition: QueryMethodDefinition,
    selected: Boolean,
    onSelect: () -> Unit,
    configurationVisible: Boolean = false,
    onConfigurationDismiss: () -> Unit = {},
    onConfigure: () -> Unit = {},
) {
    val configurationItems = definition.configuration?.items.orEmpty()
    val hasConfiguration = configurationItems.isNotEmpty()

    val border = rememberOptionCardBorder(selected = selected)

    OutlinedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .pointerHoverIcon(pointerIconForOption(isInteractive = !selected))
                .clickable(
                    enabled = !selected,
                    onClick = onSelect,
                ),
        shape = MaterialTheme.shapes.medium,
        border = border,
        colors = rememberOptionCardColors(selected = selected),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = CardContentHorizontalPadding,
                        vertical = CardContentVerticalPadding,
                    ),
            verticalArrangement = Arrangement.spacedBy(CardContentSpacing),
        ) {
            QueryMethodOptionHeaderRow(
                definition = definition,
                selected = selected,
                onSelect = onSelect,
                hasConfiguration = hasConfiguration,
                configurationVisible = configurationVisible,
                onConfigurationDismiss = onConfigurationDismiss,
                onConfigure = onConfigure,
                configurationItems = configurationItems,
            )
        }
    }
}

@Composable
private fun QueryMethodOptionHeaderRow(
    definition: QueryMethodDefinition,
    selected: Boolean,
    onSelect: () -> Unit,
    hasConfiguration: Boolean,
    configurationVisible: Boolean,
    onConfigurationDismiss: () -> Unit,
    onConfigure: () -> Unit,
    configurationItems: List<QueryMethodSettingItem>,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(HeaderRowSpacing),
    modifier = Modifier.fillMaxWidth(),
) {
    QueryMethodOptionRadioButton(
        selected = selected,
        onSelect = onSelect,
    )
    QueryMethodTitleAndChips(
        title = definition.title,
        chips = definition.chips,
        modifier = Modifier.weight(TITLE_COLUMN_WEIGHT),
    )
    if (hasConfiguration) {
        QueryMethodConfigurationButton(
            selected = selected,
            configurationVisible = configurationVisible,
            onConfigurationDismiss = onConfigurationDismiss,
            onConfigure = onConfigure,
            configurationTitle = definition.configuration?.title,
            configurationItems = configurationItems,
        )
    }
}

@Composable
private fun QueryMethodOptionRadioButton(
    selected: Boolean,
    enabled: Boolean = true,
    onSelect: () -> Unit,
) = RadioButton(
    selected = selected,
    onClick = onSelect,
    enabled = enabled,
    modifier =
        Modifier
            .wrapContentSize()
            .pointerHoverIcon(pointerIconForOption(isInteractive = enabled)),
)

@Composable
private fun QueryMethodTitleAndChips(
    title: String,
    chips: List<QueryMethodChip>,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(TitleBlockSpacing),
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
    if (chips.isNotEmpty()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(ChipRowSpacing),
            verticalArrangement = Arrangement.spacedBy(ChipRowSpacing),
        ) {
            chips.forEach { chip ->
                QueryMethodChipBadge(chip = chip)
            }
        }
    }
}

@Composable
private fun QueryMethodConfigurationButton(
    selected: Boolean,
    configurationVisible: Boolean,
    onConfigurationDismiss: () -> Unit,
    onConfigure: () -> Unit,
    configurationTitle: String?,
    configurationItems: List<QueryMethodSettingItem>,
) = IconButton(
    onClick = onConfigure,
    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
) {
    Icon(
        imageVector = Icons.Outlined.Settings,
        contentDescription = t(Res.string.query_method_tooltip_title),
        tint =
            if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
    )

    FloatingDialogBuilder(
        visible = configurationVisible,
        onDismissRequest = onConfigurationDismiss,
        positionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.End,
            ),
    ) {
        QueryMethodConfigurationTooltip(
            title = configurationTitle ?: t(Res.string.query_method_tooltip_title),
            items = configurationItems,
        )
    }
}

@Composable
private fun rememberOptionCardBorder(selected: Boolean) =
    if (selected) {
        BorderStroke(
            SelectedBorderWidth,
            MaterialTheme.colorScheme.primary.copy(alpha = SELECTED_BORDER_ALPHA),
        )
    } else {
        CardDefaults.outlinedCardBorder()
    }

@Composable
private fun rememberOptionCardColors(selected: Boolean) =
    CardDefaults.outlinedCardColors(
        containerColor =
            if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = SELECTED_CONTAINER_ALPHA)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = UNSELECTED_CONTAINER_ALPHA)
            },
    )

private const val SELECTED_BORDER_ALPHA = 0.9f
private const val SELECTED_CONTAINER_ALPHA = 0.04f
private const val UNSELECTED_CONTAINER_ALPHA = 0.25f
private const val TITLE_COLUMN_WEIGHT = 1f

private val SelectedBorderWidth = 1.5.dp
private val CardContentHorizontalPadding = 16.dp
private val CardContentVerticalPadding = 8.dp

private val CardContentSpacing = 8.dp
private val HeaderRowSpacing = 8.dp
private val TitleBlockSpacing = 4.dp
private val ChipRowSpacing = 4.dp

private fun pointerIconForOption(isInteractive: Boolean) =
    if (isInteractive) {
        PointerIcon.Hand
    } else {
        PointerIcon.Default
    }
