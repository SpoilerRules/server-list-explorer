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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.spoiligaming.explorer.minecraft.common.IModuleKind
import com.spoiligaming.explorer.minecraft.common.UnifiedModeInitializer
import com.spoiligaming.explorer.multiplayer.MultiplayerServer
import com.spoiligaming.explorer.multiplayer.ServerListMonitor
import com.spoiligaming.explorer.multiplayer.history.ServerListHistoryService
import com.spoiligaming.explorer.multiplayer.history.SortChange
import com.spoiligaming.explorer.multiplayer.repository.ServerListRepository
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalMultiplayerSettings
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.components.LoadingScreen
import com.spoiligaming.explorer.ui.navigation.MultiplayerServerListScreen
import com.spoiligaming.explorer.ui.t
import io.github.oshai.kotlinlogging.KotlinLogging
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.loading_init_repo
import server_list_explorer.ui.generated.resources.loading_setup_monitor

@Composable
internal fun MultiplayerScreenContainer(navController: NavController) {
    val scope = rememberCoroutineScope()
    val mp = LocalMultiplayerSettings.current
    val prefs = LocalPrefs.current

    var repo by remember { mutableStateOf<ServerListRepository?>(null) }
    var monitor by remember { mutableStateOf<ServerListMonitor?>(null) }

    var screenState by remember { mutableStateOf<MultiplayerScreenState>(MultiplayerScreenState.Initializing) }
    val historyService =
        remember(mp.serverListFile) {
            ServerListHistoryService(maxUndoEntries = prefs.maxUndoHistorySize)
        }

    val onReloadRequest = {
        navController.navigate(MultiplayerServerListScreen) {
            popUpTo(MultiplayerServerListScreen) { inclusive = true }
        }
    }

    val loadingInitRepoText = t(Res.string.loading_init_repo)
    val loadingSetupMonitorText = t(Res.string.loading_setup_monitor)

    val loadingSteps =
        remember(repo) {
            buildList {
                add(
                    loadingInitRepoText to
                        suspend {
                            @Suppress("unused", "UnusedVariable")
                            val result =
                                UnifiedModeInitializer.initialize<ServerListRepository>(
                                    IModuleKind.Multiplayer,
                                    mp.serverListFile,
                                ).onSuccess {
                                    repo = it
                                }.onFailure {
                                    logger.error(it) { "Error initializing server list repository" }
                                    screenState = MultiplayerScreenState.Error
                                }
                        },
                )
                if (repo != null) {
                    add(
                        loadingSetupMonitorText to
                            suspend {
                                monitor =
                                    ServerListMonitor(
                                        repo = repo!!,
                                        scope = scope,
                                        intervalMillis = MONITOR_INTERVAL_MILLIS,
                                    ).also { repo!!.monitor = it }
                            },
                    )
                }
            }
        }

    // lifecycle effect to start and stop the file monitor
    DisposableEffect(monitor) {
        monitor?.start()
        onDispose { monitor?.stop() }
    }

    val isCacheActive =
        screenState is MultiplayerScreenState.Ready || screenState is MultiplayerScreenState.Sorting
    if (isCacheActive) {
        DisposableEffect(screenState::class) {
            onDispose {
                ServerEntryController.clearCache()
                logger.info {
                    "Cleared ${ServerEntryController::class.simpleName} cache " +
                        "because ${screenState::class.simpleName} left the composition"
                }
            }
        }
    }

    when (val state = screenState) {
        is MultiplayerScreenState.Initializing ->
            LoadingScreen(
                displayAfterThreshold = true,
                steps = loadingSteps,
                modifier = Modifier.fillMaxSize(),
            ) {
                LaunchedEffect(repo) {
                    if (repo != null) {
                        screenState = MultiplayerScreenState.Ready
                    } else if (screenState !is MultiplayerScreenState.Error) {
                        screenState = MultiplayerScreenState.Error
                    }
                }
            }

        is MultiplayerScreenState.Ready ->
            repo?.let {
                MultiplayerScreen(
                    repo = it,
                    historyService = historyService,
                    onReloadRequest = onReloadRequest,
                    onSortRequest = { sortType, servers ->
                        screenState = MultiplayerScreenState.Sorting(servers, sortType)
                    },
                )
            } ?: LaunchedEffect(Unit) {
                logger.error {
                    "Repo was null in ${MultiplayerScreenState.Ready::class.simpleName} " +
                        "state, transitioning to ${MultiplayerScreenState.Error::class.simpleName}."
                }
                screenState = MultiplayerScreenState.Error
            }

        is MultiplayerScreenState.Sorting ->
            ServerListSortProcessScreen(
                servers = state.servers,
                sortType = state.sortType,
                onExitRequested = { screenState = MultiplayerScreenState.Initializing },
                onApplySortedList = { sortedList ->
                    val oldServers = repo?.all().orEmpty()
                    repo?.deleteAll()
                    sortedList.forEach { repo?.add(it) }
                    repo?.commit()
                    repo?.let {
                        historyService.clear()
                        historyService.recordChange(
                            SortChange(
                                oldServers = oldServers,
                                newServers = sortedList,
                            ),
                        )
                    }
                },
            )

        is MultiplayerScreenState.Error -> MultiplayerErrorScreen(navController)
    }
}

private sealed interface MultiplayerScreenState {
    /** The initial state while the server repository is being loaded. */
    data object Initializing : MultiplayerScreenState

    /** The state when the server repository failed to load. */
    data object Error : MultiplayerScreenState

    /** The state when the server list is ready to be displayed. */
    data object Ready : MultiplayerScreenState

    /** The state when the server list is being actively sorted. */
    data class Sorting(val servers: List<MultiplayerServer>, val sortType: SortType) : MultiplayerScreenState
}

private const val MONITOR_INTERVAL_MILLIS = 1000L
private val logger = KotlinLogging.logger {}
