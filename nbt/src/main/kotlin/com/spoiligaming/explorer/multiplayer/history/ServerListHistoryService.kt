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

package com.spoiligaming.explorer.multiplayer.history

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ServerListHistoryService(
    private val maxUndoEntries: Int,
) {
    private val mutex = Mutex()
    private val undoStack = ArrayDeque<ServerListChange>()
    private val redoStack = ArrayDeque<ServerListChange>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo = _canRedo.asStateFlow()

    private fun refreshFlags() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    suspend fun recordChange(change: ServerListChange) =
        mutex.withLock {
            undoStack.pushWithLimit(change, maxUndoEntries)
            redoStack.clear()
            refreshFlags()
            logger.debug {
                "Recorded change: ${change.description()} " +
                    "(undo=${undoStack.size}, redo=${redoStack.size})"
            }
        }

    suspend fun undo(): ServerListChange? =
        mutex.withLock {
            val lastChange = undoStack.removeLastOrNull() ?: return null
            redoStack.pushWithLimit(lastChange, maxUndoEntries)
            logger.info {
                "Undo: ${lastChange.description()} " +
                    "(undo=${undoStack.size}, redo=${redoStack.size})"
            }
            refreshFlags()
            lastChange
        }

    suspend fun redo(): ServerListChange? =
        mutex.withLock {
            val redoChange = redoStack.removeLastOrNull() ?: return null
            undoStack.pushWithLimit(redoChange, maxUndoEntries)
            logger.info {
                "Redo: ${redoChange.description()} " +
                    "(undo=${undoStack.size}, redo=${redoStack.size})"
            }
            refreshFlags()
            redoChange
        }

    suspend fun clear() =
        mutex.withLock {
            undoStack.clear()
            redoStack.clear()
            refreshFlags()
            logger.debug { "Cleared all history" }
        }

    private fun <T> ArrayDeque<T>.pushWithLimit(
        element: T,
        limit: Int,
    ) {
        if (limit <= 0) return
        while (size >= limit) removeFirst()
        addLast(element)
    }
}

private val logger = KotlinLogging.logger {}
