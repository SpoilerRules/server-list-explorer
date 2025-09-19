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
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.spoiligaming.explorer.settings.manager.UniversalSettingsManager
import com.spoiligaming.explorer.ui.AppLocaleProvider
import com.spoiligaming.explorer.ui.components.LoadingScreen
import com.spoiligaming.explorer.ui.screens.multiplayer.MultiplayerScreenContainer
import com.spoiligaming.explorer.ui.screens.settings.SettingsScreen
import com.spoiligaming.explorer.ui.t
import server_list_explorer.ui.generated.resources.Res
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
            MultiplayerScreenContainer(navController)
        }

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
