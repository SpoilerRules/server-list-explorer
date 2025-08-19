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

@file:OptIn(ExperimentalAtomicApi::class)

package com.spoiligaming.explorer.ui.snackbar

import androidx.compose.material3.SnackbarDuration
import com.spoiligaming.explorer.ui.snackbar.SnackbarController.FIRST_SNACKBAR_DELAY_MS
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal data class SnackbarEvent(
    val message: String,
    val duration: SnackbarDuration,
    val action: SnackbarAction? = null,
)

internal data class SnackbarAction(
    val name: String,
    val action: suspend () -> Unit,
)

internal object SnackbarController {
    private val logging = KotlinLogging.logger { }

    private const val FIRST_SNACKBAR_DELAY_MS = 600L

    private val _events = Channel<SnackbarEvent>()
    val events = _events.receiveAsFlow()

    private val firstSend = AtomicBoolean(true)

    /**
     * Sends a [SnackbarEvent] to the UI snackbar host.
     *
     * On the very first invocation after app startup, this function intentionally applies a short delay
     * (see [FIRST_SNACKBAR_DELAY_MS]) before sending the event. This gives the Compose UI on desktop
     * enough time to fully initialize, preventing a noticeable UI hitch or lag when showing the very first snackbar.
     *
     * All subsequent snackbar events are sent immediately without delay.
     */
    suspend fun sendEvent(event: SnackbarEvent) {
        if (firstSend.compareAndExchange(expectedValue = true, newValue = false)) {
            delay(FIRST_SNACKBAR_DELAY_MS)
            logging.debug {
                "Applied one-time delay of $FIRST_SNACKBAR_DELAY_MS ms before first snackbar event."
            }
        }
        _events.send(event)
    }
}
