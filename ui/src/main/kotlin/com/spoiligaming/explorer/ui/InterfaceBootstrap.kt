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

package com.spoiligaming.explorer.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.DarkDefaultContextMenuRepresentation
import androidx.compose.foundation.LightDefaultContextMenuRepresentation
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spoiligaming.explorer.settings.manager.themeSettingsManager
import com.spoiligaming.explorer.settings.model.ThemeMode
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.components.FPSDisplay
import com.spoiligaming.explorer.ui.navigation.AppNavigationRail
import com.spoiligaming.explorer.ui.navigation.MultiplayerServerListScreen
import com.spoiligaming.explorer.ui.navigation.NavGraph
import com.spoiligaming.explorer.ui.screens.setup.SetupWizard
import com.spoiligaming.explorer.ui.snackbar.SnackbarHostManager
import com.spoiligaming.explorer.ui.theme.AppTheme
import com.spoiligaming.explorer.ui.theme.isDarkTheme
import com.spoiligaming.explorer.ui.theme.isSystemDarkTheme
import com.spoiligaming.explorer.ui.window.WindowManager
import com.spoiligaming.explorer.util.FirstRunManager
import kotlinx.coroutines.launch

fun launchInterface() {
    WindowManager.launch { AppContainer() }
}

@Composable
private fun AppContainer() {
    val isFirstRun by FirstRunManager.isFirstRun.collectAsState()
    val prefs = LocalPrefs.current

    val contextMenuRepresentation =
        if (isDarkTheme) {
            DarkDefaultContextMenuRepresentation
        } else {
            LightDefaultContextMenuRepresentation
        }

    AppTheme {
        AppLocaleProvider {
            CompositionLocalProvider(
                LocalContextMenuRepresentation provides contextMenuRepresentation,
            ) {
                AnimatedContent(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                    targetState = isFirstRun,
                    transitionSpec = {
                        val enter =
                            slideInVertically(
                                animationSpec = tween(ANIMATION_DURATION_MS),
                            ) { fullHeight -> fullHeight } +
                                fadeIn(
                                    animationSpec = tween(ANIMATION_DURATION_MS),
                                )
                        val exit =
                            slideOutVertically(
                                animationSpec = tween(ANIMATION_DURATION_MS),
                            ) { fullHeight -> -fullHeight } +
                                fadeOut(
                                    animationSpec = tween(ANIMATION_DURATION_MS),
                                )
                        enter togetherWith exit using SizeTransform(clip = false)
                    },
                ) { firstRun ->
                    if (firstRun) {
                        SetupWizardScreen()
                    } else {
                        MainAppScreen()
                    }
                    if (prefs.showFpsOverlay) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(FPS_OVERLAY_PADDING),
                        ) {
                            FPSDisplay()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupWizardScreen() {
    val scope = rememberCoroutineScope()
    SetupWizard(
        onFinished = {
            scope.launch { FirstRunManager.markFirstRunDone() }
        },
        intOffsetAnimationSpec = offsetAnimationSpec,
        floatAnimationSpec = floatAnimationSpec,
    )
}

@Composable
private fun MainAppScreen() {
    val prefs = LocalPrefs.current
    val navController = rememberNavController()

    Row(modifier = Modifier.fillMaxSize()) {
        AppNavigationRail(navController) {
            themeSettingsManager.updateSettings {
                val nextMode =
                    when (it.themeMode) {
                        ThemeMode.Light -> ThemeMode.Dark
                        ThemeMode.Dark -> ThemeMode.Light
                        ThemeMode.System ->
                            if (isSystemDarkTheme) ThemeMode.Light else ThemeMode.Dark
                    }
                it.copy(themeMode = nextMode)
            }
        }

        val paddingValue =
            if (
                navController
                    .currentBackStackEntryAsState()
                    .value
                    ?.destination
                    ?.route
                == MultiplayerServerListScreen::class.qualifiedName
            ) {
                NO_CONTENT_PADDING
            } else {
                DEFAULT_CONTENT_PADDING
            }

        Box {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValue)) {
                NavGraph(navController = navController)
            }
            SnackbarHostManager(
                modifier =
                    Modifier.align(
                        if (prefs.snackbarAtTop) {
                            Alignment.TopCenter
                        } else {
                            Alignment.BottomCenter
                        },
                    ),
            )
        }
    }
}

private const val ANIMATION_DURATION_MS = 300
private val FPS_OVERLAY_PADDING = 16.dp
private val DEFAULT_CONTENT_PADDING = 16.dp
private val NO_CONTENT_PADDING = 0.dp

private val floatAnimationSpec: FiniteAnimationSpec<Float> =
    tween(
        durationMillis = ANIMATION_DURATION_MS,
        easing = FastOutSlowInEasing,
    )

private val offsetAnimationSpec: FiniteAnimationSpec<IntOffset> =
    tween(
        durationMillis = ANIMATION_DURATION_MS,
        easing = FastOutSlowInEasing,
    )
