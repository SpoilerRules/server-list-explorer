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

package com.spoiligaming.explorer.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.spoiligaming.explorer.minecraft.common.IModuleKind
import com.spoiligaming.explorer.minecraft.common.UnifiedModeInitializer
import com.spoiligaming.explorer.multiplayer.ServerListMonitor
import com.spoiligaming.explorer.multiplayer.repository.ServerListRepository
import com.spoiligaming.explorer.settings.manager.UniversalSettingsManager
import com.spoiligaming.explorer.ui.AppLocaleProvider
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalMultiplayerSettings
import com.spoiligaming.explorer.ui.components.LoadingScreen
import com.spoiligaming.explorer.ui.screens.multiplayer.MultiplayerErrorScreen
import com.spoiligaming.explorer.ui.screens.multiplayer.MultiplayerScreen
import com.spoiligaming.explorer.ui.screens.settings.SettingsScreen
import com.spoiligaming.explorer.ui.t
import io.github.oshai.kotlinlogging.KotlinLogging
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.loading_init_repo
import server_list_explorer.ui.generated.resources.loading_setup_monitor
import server_list_explorer.ui.generated.resources.loading_user_settings

@Composable
internal fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = MultiplayerServerListScreen,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable<MultiplayerServerListScreen> {
            val serverListFile = LocalMultiplayerSettings.current.serverListFile
            val scope = rememberCoroutineScope()

            var repo by remember { mutableStateOf<ServerListRepository?>(null) }
            var monitor by remember { mutableStateOf<ServerListMonitor?>(null) }

            LoadingScreen(
                displayAfterThreshold = true,
                steps =
                    buildList {
                        add(
                            t(Res.string.loading_init_repo) to {
                                UnifiedModeInitializer
                                    .initialize<ServerListRepository>(
                                        IModuleKind.Multiplayer,
                                        serverListFile,
                                    )
                                    .onSuccess { repo = it }
                                    .onFailure {
                                        logger.error(it) { "Error initializing server list repository" }
                                    }
                            },
                        )
                        if (repo != null) {
                            add(
                                t(Res.string.loading_setup_monitor) to {
                                    monitor =
                                        ServerListMonitor(
                                            repo = repo!!,
                                            scope = scope,
                                            intervalMillis = 1000,
                                        ).also { repo!!.monitor = it }
                                },
                            )
                        }
                    },
                modifier = Modifier.fillMaxSize(),
            ) {
                repo?.let {
                    MultiplayerScreen(
                        repo = it,
                        onReloadRequest = {
                            navController.navigate(MultiplayerServerListScreen) {
                                popUpTo(MultiplayerServerListScreen) { inclusive = true }
                            }
                        },
                    )
                } ?: MultiplayerErrorScreen(navController)
            }

            DisposableEffect(monitor) {
                monitor?.start()
                onDispose { monitor?.stop() }
            }
        }

        composable<SingleplayerWorldListScreen> { }

        composable<SettingsScreen> {
            AppLocaleProvider {
                LoadingScreen(
                    displayAfterThreshold = true,
                    steps =
                        listOf(
                            t(Res.string.loading_user_settings) to { UniversalSettingsManager.loadAll() },
                        ),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    SettingsScreen()
                }
            }
        }
    }
}

private val logger = KotlinLogging.logger {}
