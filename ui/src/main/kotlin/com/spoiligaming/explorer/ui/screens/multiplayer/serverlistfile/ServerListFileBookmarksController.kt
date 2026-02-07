/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2026 SpoilerRules
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

package com.spoiligaming.explorer.ui.screens.multiplayer.serverlistfile

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.spoiligaming.explorer.serverlist.bookmarks.ServerListFileBookmarkEntry
import com.spoiligaming.explorer.serverlist.bookmarks.ServerListFileBookmarksManager
import com.spoiligaming.explorer.ui.snackbar.SnackbarController
import com.spoiligaming.explorer.ui.snackbar.SnackbarEvent
import com.spoiligaming.explorer.util.DesktopSystemLauncher
import com.spoiligaming.explorer.util.canonicalize
import com.spoiligaming.explorer.util.readableName
import com.spoiligaming.explorer.util.serverListBookmarkKey
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.server_list_file_bookmarks_error_add
import server_list_explorer.ui.generated.resources.server_list_file_bookmarks_error_clear_dead_entries
import server_list_explorer.ui.generated.resources.server_list_file_bookmarks_error_clear_inactive_entries
import server_list_explorer.ui.generated.resources.server_list_file_bookmarks_error_load
import server_list_explorer.ui.generated.resources.server_list_file_bookmarks_error_open_in_file_manager
import server_list_explorer.ui.generated.resources.server_list_file_bookmarks_error_remove
import server_list_explorer.ui.generated.resources.server_list_file_bookmarks_error_rename
import server_list_explorer.ui.generated.resources.server_list_file_bookmarks_error_reorder
import server_list_explorer.ui.generated.resources.server_list_file_bookmarks_warning_already_added
import server_list_explorer.ui.generated.resources.server_list_file_bookmarks_warning_missing
import java.nio.file.Files
import java.nio.file.Path
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
internal fun rememberServerListFileBookmarksController(): ServerListFileBookmarksController {
    val scope = rememberCoroutineScope()
    val controller = remember { ServerListFileBookmarksController(scope) }

    LaunchedEffect(Unit) {
        controller.ensureLoaded()
    }

    return controller
}

internal class ServerListFileBookmarksController(
    private val scope: CoroutineScope,
) {
    val entries: StateFlow<List<ServerListFileBookmarkEntry>> = ServerListFileBookmarksManager.entries

    var activePath by mutableStateOf<Path?>(null)
        private set

    var selectedEntryId by mutableStateOf<Uuid?>(null)

    suspend fun ensureLoaded() {
        runWithSnackbarOnFailure(
            block = { ServerListFileBookmarksManager.load() },
            userFacingMessage = { getString(Res.string.server_list_file_bookmarks_error_load) },
            logMessage = { "Failed to load server file list bookmarks" },
        )

        val currentActive = ServerListFileBookmarksManager.activePath.value
        activePath = currentActive
        selectedEntryId =
            currentActive?.let { path ->
                entries.value
                    .firstOrNull { entry ->
                        entry.path.serverListBookmarkKey() == path.serverListBookmarkKey()
                    }?.id
            }
    }

    suspend fun addFile(
        rawPath: Path,
        autoSelect: Boolean,
    ): AddResult {
        val canonical = rawPath.canonicalize()
        val key = canonical.serverListBookmarkKey()
        val existing =
            entries.value.firstOrNull { entry ->
                entry.path.serverListBookmarkKey() == key
            }

        if (existing != null) {
            if (autoSelect) setActive(existing.path, existing.id)
            return AddResult.Duplicate(existing)
        }

        val remembered =
            runWithSnackbarOnFailure(
                block = { ServerListFileBookmarksManager.remember(canonical) },
                userFacingMessage = {
                    getString(
                        Res.string.server_list_file_bookmarks_error_add,
                        canonical.readableName(),
                    )
                },
                logMessage = { "Failed to remember server file list bookmark for $canonical" },
            )

        if (remembered == null) return AddResult.Error

        val added =
            entries.value.firstOrNull { entry ->
                entry.path.serverListBookmarkKey() == key
            } ?: return AddResult.Error

        if (autoSelect) setActive(added.path, added.id)

        return AddResult.Added(added)
    }

    suspend fun selectEntry(entry: ServerListFileBookmarkEntry?): Boolean {
        if (entry == null) {
            setActive(null, null)
            return true
        }

        if (!Files.exists(entry.path)) {
            showWarningSnackbar(
                getString(
                    Res.string.server_list_file_bookmarks_warning_missing,
                    entry.path.readableName(),
                ),
            )
            return false
        }

        setActive(entry.path, entry.id)
        return true
    }

    suspend fun remove(entry: ServerListFileBookmarkEntry) =
        runWithSnackbarOnFailure(
            block = { ServerListFileBookmarksManager.remove(entry.id) },
            userFacingMessage = {
                getString(
                    Res.string.server_list_file_bookmarks_error_remove,
                    entry.path.readableName(),
                )
            },
            logMessage = { "Failed to remove server file list bookmark ${entry.id} at ${entry.path}" },
        )

    fun moveAsync(
        fromIndex: Int,
        toIndex: Int,
    ) {
        scope.launch {
            move(fromIndex, toIndex)
        }
    }

    suspend fun move(
        fromIndex: Int,
        toIndex: Int,
    ) {
        require(fromIndex >= 0 && toIndex >= 0) { "Indices must be non-negative." }

        runWithSnackbarOnFailure(
            block = { ServerListFileBookmarksManager.move(fromIndex, toIndex) },
            userFacingMessage = { getString(Res.string.server_list_file_bookmarks_error_reorder) },
            logMessage = { "Failed to reorder bookmarks from $fromIndex to $toIndex" },
        )
    }

    fun pruneMissingAsync() {
        scope.launch { pruneMissing() }
    }

    suspend fun pruneMissing() {
        runWithSnackbarOnFailure(
            block = { ServerListFileBookmarksManager.pruneMissingFiles() },
            userFacingMessage = { getString(Res.string.server_list_file_bookmarks_error_clear_dead_entries) },
            logMessage = { "Failed to prune missing server file list entries" },
        )
    }

    fun clearExceptActiveAsync() {
        scope.launch { clearExceptActive() }
    }

    suspend fun clearExceptActive() {
        val current = activePath
        runWithSnackbarOnFailure(
            block = { ServerListFileBookmarksManager.clearExceptActive(current) },
            userFacingMessage = { getString(Res.string.server_list_file_bookmarks_error_clear_inactive_entries) },
            logMessage = { "Failed to clear inactive entries while keeping $current" },
        )
    }

    suspend fun updateLabel(
        entry: ServerListFileBookmarkEntry,
        value: String,
    ) {
        val normalized = value.trim().takeIf { it.isNotEmpty() }
        runWithSnackbarOnFailure(
            block = { ServerListFileBookmarksManager.updateLabel(entry.id) { _ -> normalized } },
            userFacingMessage = {
                getString(
                    Res.string.server_list_file_bookmarks_error_rename,
                    entry.path.readableName(),
                )
            },
            logMessage = { "Failed to update label for bookmark ${entry.id}" },
        )
    }

    suspend fun showDuplicateFileWarning() {
        showWarningSnackbar(getString(Res.string.server_list_file_bookmarks_warning_already_added))
    }

    fun openFileLocation(path: Path) {
        scope.launch {
            runWithSnackbarOnFailure(
                block = { DesktopSystemLauncher.revealInFileManager(path).getOrThrow() },
                userFacingMessage = {
                    getString(
                        Res.string.server_list_file_bookmarks_error_open_in_file_manager,
                        path.readableName(),
                    )
                },
                logMessage = { "Failed to reveal $path in the file manager" },
            )
        }
    }

    fun openContainingFolder(path: Path) {
        val folder = path.parent ?: path
        scope.launch {
            runWithSnackbarOnFailure(
                block = { DesktopSystemLauncher.openInFileManager(folder).getOrThrow() },
                userFacingMessage = {
                    getString(
                        Res.string.server_list_file_bookmarks_error_open_in_file_manager,
                        folder.readableName(),
                    )
                },
                logMessage = { "Failed to reveal folder $folder in the file manager" },
            )
        }
    }

    private suspend fun setActive(
        path: Path?,
        entryId: Uuid?,
    ) = withContext(Dispatchers.Default) {
        ServerListFileBookmarksManager.setActivePath(path)
        activePath = ServerListFileBookmarksManager.activePath.value
        selectedEntryId = entryId
    }
}

internal sealed interface AddResult {
    data class Added(
        val entry: ServerListFileBookmarkEntry,
    ) : AddResult

    data class Duplicate(
        val entry: ServerListFileBookmarkEntry,
    ) : AddResult

    data object Error : AddResult
}

internal suspend fun showWarningSnackbar(
    message: String,
    duration: SnackbarDuration = SnackbarDuration.Short,
) = SnackbarController.sendEvent(
    SnackbarEvent(
        message = message,
        duration = duration,
    ),
)

internal suspend inline fun <T> runWithSnackbarOnFailure(
    crossinline block: suspend () -> T,
    crossinline userFacingMessage: suspend () -> String,
    crossinline logMessage: () -> String,
) = try {
    block()
} catch (e: Throwable) {
    logger.error(e) { logMessage() }
    showWarningSnackbar(userFacingMessage())
    null
}

private val logger = KotlinLogging.logger {}
