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

package com.spoiligaming.explorer.ui.screens.multiplayer.querymethod

import com.spoiligaming.explorer.settings.model.ServerQueryMethod

internal data class QueryMethodDefinition(
    val method: ServerQueryMethod,
    val title: String,
    val chips: List<QueryMethodChip>,
    val configuration: QueryMethodConfiguration? = null,
)

internal data class QueryMethodConfiguration(
    val title: String,
    val items: List<QueryMethodSettingItem>,
)

internal sealed interface QueryMethodSettingItem

internal data class TimeoutSliderSpec(
    val title: String,
    val description: String?,
    val valueSeconds: Int,
    val valueRangeSeconds: ClosedFloatingPointRange<Float>,
    val onValueChangeSeconds: (Int) -> Unit,
) : QueryMethodSettingItem

internal data class QueryMethodCheckboxSpec(
    val title: String,
    val description: String?,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
) : QueryMethodSettingItem
