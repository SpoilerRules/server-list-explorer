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

package com.spoiligaming.explorer.ui.snackbar

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import kotlinx.coroutines.launch

@Composable
internal fun BoxScope.SnackbarHostManager(modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val eventsFlow = SnackbarController.events

    val prefs = LocalPrefs.current

    LaunchedEffect(eventsFlow, snackbarHostState) {
        scope.launch {
            eventsFlow.collect { event ->
                snackbarHostState.currentSnackbarData?.dismiss()

                val result =
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.action?.name,
                        duration = event.duration,
                    )

                if (result == SnackbarResult.ActionPerformed) {
                    event.action?.action?.invoke()
                }
            }
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier =
            modifier.align(
                if (prefs.snackbarAtTop) {
                    Alignment.TopCenter
                } else {
                    Alignment.BottomCenter
                },
            ),
    )
}
