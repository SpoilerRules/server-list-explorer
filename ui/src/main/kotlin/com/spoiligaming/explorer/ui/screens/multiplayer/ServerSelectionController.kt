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

@file:OptIn(ExperimentalUuidApi::class)

package com.spoiligaming.explorer.ui.screens.multiplayer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class ServerSelectionController(
    private val onSelectionChanged: (() -> Unit)?,
) {
    private val _selectedIds = MutableStateFlow<Set<Uuid>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    private var anchorKey: Uuid? = null

    fun toggle(id: Uuid) {
        _selectedIds.value = _selectedIds.value.toggle(id)
        anchorKey = id
        onSelectionChanged?.invoke()
    }

    fun clear() {
        _selectedIds.value = emptySet()
        anchorKey = null
        onSelectionChanged?.invoke()
    }

    fun selectAll(allIds: Collection<Uuid>) {
        _selectedIds.value = allIds.toSet()
        anchorKey = allIds.firstOrNull()
        onSelectionChanged?.invoke()
    }

    fun handlePointerClick(
        id: Uuid,
        entries: List<Uuid>,
        ctrlMeta: Boolean,
        shift: Boolean,
    ) {
        val idx = entries.indexOf(id)
        val anchorIdx = anchorKey?.let { entries.indexOf(it) }?.takeIf { it >= 0 }

        when {
            // multi-select
            ctrlMeta -> toggle(id)

            // range-select
            shift && anchorIdx != null -> {
                val range = if (idx >= anchorIdx) anchorIdx..idx else idx..anchorIdx
                _selectedIds.value = range.map { entries[it] }.toSet()
                onSelectionChanged?.invoke()
            }

            // single-select
            else -> {
                _selectedIds.value = setOf(id)
                anchorKey = id
                onSelectionChanged?.invoke()
            }
        }
    }

    fun indicesOf(entries: List<Uuid>) =
        entries.mapIndexedNotNull { index, uuid ->
            if (uuid in _selectedIds.value) index else null
        }

    private fun Set<Uuid>.toggle(key: Uuid) = if (key in this) this - key else this + key
}
