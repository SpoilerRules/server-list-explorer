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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.McUtilsOnlineServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OnlineServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OnlineServerDataResourceResult
import com.spoiligaming.explorer.multiplayer.MultiplayerServer
import com.spoiligaming.explorer.settings.model.ServerQueryMethod
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalMultiplayerSettings
import com.spoiligaming.explorer.ui.extensions.formatMillis
import com.spoiligaming.explorer.ui.t
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.dialog_cancel_button
import server_list_explorer.ui.generated.resources.sort_button_skip_pinging
import server_list_explorer.ui.generated.resources.sort_info_mode
import server_list_explorer.ui.generated.resources.sort_info_servers
import server_list_explorer.ui.generated.resources.sort_info_timeout
import server_list_explorer.ui.generated.resources.sort_live_activity
import server_list_explorer.ui.generated.resources.sort_max_time_remaining
import server_list_explorer.ui.generated.resources.sort_mode_ping
import server_list_explorer.ui.generated.resources.sort_mode_player_count
import server_list_explorer.ui.generated.resources.sort_pinging_servers
import server_list_explorer.ui.generated.resources.sort_skip_message
import server_list_explorer.ui.generated.resources.sort_status_applying
import server_list_explorer.ui.generated.resources.sort_status_done
import server_list_explorer.ui.generated.resources.sort_status_in_progress
import server_list_explorer.ui.generated.resources.sort_status_next
import server_list_explorer.ui.generated.resources.sort_status_preparing
import server_list_explorer.ui.generated.resources.sort_status_sorting_entries
import server_list_explorer.ui.generated.resources.sort_step_pinging
import server_list_explorer.ui.generated.resources.sort_step_sorting
import server_list_explorer.ui.generated.resources.sort_timeline_title
import server_list_explorer.ui.generated.resources.sort_title
import server_list_explorer.ui.generated.resources.sort_whats_happening_desc
import server_list_explorer.ui.generated.resources.sort_whats_happening_title
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private data class SortProcessState(
    val currentStep: SortStep,
    val pingProgress: Int,
    val pingTotal: Int,
    val timeRemainingMs: Long,
    val sortProgress: Float,
    val sortStatusText: StringResource,
    val sortComputed: Boolean,
    val onSkipPingingRequest: () -> Unit,
)

private enum class SortWorkUnit { InMemorySort, PersistSortedList }

@Composable
private fun rememberSortProcessState(
    servers: List<MultiplayerServer>,
    sortType: SortType,
    onApplySortedList: suspend (List<MultiplayerServer>) -> Unit,
    onExitRequested: () -> Unit,
): SortProcessState {
    val mp = LocalMultiplayerSettings.current
    val scope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(SortStep.Pinging) }
    var pingProgress by remember { mutableStateOf(0) }
    val pingedData = remember { mutableStateMapOf<Uuid, OnlineServerData>() }
    var timeRemaining by remember { mutableStateOf(mp.connectTimeoutMillis) }
    var skipPinging by remember { mutableStateOf(false) }

    val sortWorkUnits = remember { listOf(SortWorkUnit.InMemorySort, SortWorkUnit.PersistSortedList) }
    val totalSortUnits = sortWorkUnits.size
    var completedSortUnits by remember { mutableStateOf(0) }

    var sortStatusText by remember { mutableStateOf(Res.string.sort_status_preparing) }

    val sortComputed = completedSortUnits >= totalSortUnits
    val sortProgress = if (totalSortUnits == 0) 1f else completedSortUnits.toFloat() / totalSortUnits.toFloat()

    LaunchedEffect(currentStep, mp.connectTimeoutMillis) {
        if (currentStep == SortStep.Pinging) {
            var t = mp.connectTimeoutMillis
            while (t > 0 && currentStep == SortStep.Pinging) {
                delay(COUNTDOWN_INTERVAL_MS.toLong())
                t -= COUNTDOWN_INTERVAL_MS
                timeRemaining = t
            }
        }
    }

    LaunchedEffect(servers, sortType, mp.connectTimeoutMillis, mp.socketTimeoutMillis) {
        val suggestedCap =
            max(
                MIN_CONCURRENCY_CAP,
                min(
                    MAX_CONCURRENCY_CAP,
                    Runtime.getRuntime().availableProcessors() * CPU_THREADS_MULTIPLIER,
                ),
            )
        val maxConcurrentPings = min(servers.size, suggestedCap)
        val gate = Semaphore(maxConcurrentPings)

        val pingMutex = Mutex()
        val pingJobs =
            servers.map { server ->
                launch {
                    if (skipPinging) return@launch
                    try {
                        gate.withPermit {
                            if (skipPinging) return@withPermit
                            val flow =
                                ServerEntryController.getServerDataFlow(
                                    address = server.ip,
                                    // use MCUtils for reliability reasons
                                    queryMode = ServerQueryMethod.McUtils,
                                    connectTimeoutMillis = mp.connectTimeoutMillis,
                                    socketTimeoutMillis = mp.socketTimeoutMillis,
                                )
                            val result = flow.first { it !is OnlineServerDataResourceResult.Loading }
                            if (result is OnlineServerDataResourceResult.Success) {
                                (result.data as? OnlineServerData)?.let { pingedData[server.id] = it }
                            }
                        }
                    } finally {
                        pingMutex.withLock { pingProgress++ }
                    }
                }
            }

        // wait until pinging completes or is skipped (without busy-waiting)
        snapshotFlow { skipPinging || pingProgress == servers.size }.first { it }

        // cancel any remaining ping jobs and move on
        pingJobs.forEach { it.cancel() }
        currentStep = SortStep.Sorting

        // step 1: in-memory sort
        sortStatusText = Res.string.sort_status_sorting_entries
        val sortedList =
            when (sortType) {
                SortType.Ping ->
                    servers.sortedWith(
                        compareBy { server ->
                            val ping = (pingedData[server.id] as? McUtilsOnlineServerData)?.ping ?: Long.MAX_VALUE
                            if (ping < 0) Long.MAX_VALUE else ping
                        },
                    )
                SortType.MaxPlayerCount ->
                    servers.sortedByDescending { server ->
                        pingedData[server.id]?.onlinePlayers ?: -1
                    }
            }

        completedSortUnits = (completedSortUnits + 1).coerceAtMost(totalSortUnits)

        // step 2: apply to disk
        sortStatusText = Res.string.sort_status_applying
        onApplySortedList(sortedList)

        completedSortUnits = (completedSortUnits + 1).coerceAtMost(totalSortUnits)
        sortStatusText = Res.string.sort_status_done

        // give the user a short moment to read the "Done" state
        delay(READY_DELAY_AFTER_DONE_MS)
        onExitRequested()
    }

    val skipAction: () -> Unit = { scope.launch { skipPinging = true } }

    return SortProcessState(
        currentStep = currentStep,
        pingProgress = pingProgress,
        pingTotal = servers.size,
        timeRemainingMs = timeRemaining,
        sortProgress = sortProgress,
        sortStatusText = sortStatusText,
        sortComputed = sortComputed,
        onSkipPingingRequest = skipAction,
    )
}

@Composable
internal fun ServerListSortProcessScreen(
    servers: List<MultiplayerServer>,
    sortType: SortType,
    onExitRequested: () -> Unit,
    onApplySortedList: suspend (List<MultiplayerServer>) -> Unit,
) {
    val mp = LocalMultiplayerSettings.current

    val state = rememberSortProcessState(servers, sortType, onApplySortedList, onExitRequested)
    val animatedSortProgress by animateFloatAsState(
        targetValue = state.sortProgress,
        animationSpec = tween(STATUS_TWEEN_MS, easing = FastOutSlowInEasing),
    )

    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(WindowPadding)
                    .widthIn(max = ContentMaxWidth),
            verticalArrangement = Arrangement.spacedBy(ContentSpacing),
        ) {
            ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.padding(ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(ContentSpacing),
                ) {
                    Text(
                        text = t(Res.string.sort_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    InfoRowGrid(
                        items =
                            listOf(
                                InfoItem(
                                    Icons.Outlined.Cloud,
                                    t(Res.string.sort_info_servers),
                                    servers.size.toString(),
                                ),
                                InfoItem(
                                    Icons.Outlined.Schedule,
                                    t(Res.string.sort_info_timeout),
                                    mp.connectTimeoutMillis.formatMillis(),
                                ),
                                InfoItem(Icons.Outlined.SwapVert, t(Res.string.sort_info_mode), t(sortType.label)),
                            ),
                    )
                }
            }

            Card(shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(ScreenPadding), verticalArrangement = Arrangement.spacedBy(ContentSpacing)) {
                    Text(t(Res.string.sort_live_activity), style = MaterialTheme.typography.titleMedium)
                    AnimatedContent(
                        targetState = state.currentStep,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                    ) { step ->
                        when (step) {
                            SortStep.Pinging ->
                                PingingPane(
                                    serversCount = state.pingTotal,
                                    pingProgress = state.pingProgress,
                                    timeRemainingMs = state.timeRemainingMs,
                                    onSkip = state.onSkipPingingRequest,
                                )
                            SortStep.Sorting ->
                                SortingPane(
                                    status = state.sortStatusText,
                                    progress = animatedSortProgress,
                                )
                        }
                    }
                }
            }

            OutlinedCard(shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(ScreenPadding), verticalArrangement = Arrangement.spacedBy(InfoRowSpacing)) {
                    Text(t(Res.string.sort_timeline_title), style = MaterialTheme.typography.titleMedium)
                    StepRow(
                        icon = Icons.Outlined.Cloud,
                        title = t(Res.string.sort_step_pinging),
                        state =
                            when {
                                state.currentStep == SortStep.Pinging -> StepState.Active
                                // skipped
                                state.pingProgress >= state.pingTotal || state.sortComputed -> StepState.Done
                                else -> StepState.Upcoming
                            },
                    )
                    StepRow(
                        icon = Icons.Outlined.SwapVert,
                        title = t(Res.string.sort_step_sorting),
                        state =
                            when {
                                state.sortComputed -> StepState.Done
                                state.currentStep == SortStep.Sorting -> StepState.Active
                                else -> StepState.Upcoming
                            },
                    )
                }
            }

            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(ScreenPadding), verticalArrangement = Arrangement.spacedBy(SmallSpacing)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SmallSpacing),
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = t(Res.string.sort_whats_happening_title),
                        )
                        Text(t(Res.string.sort_whats_happening_title), style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        t(Res.string.sort_whats_happening_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (state.currentStep == SortStep.Pinging) {
                    TextButton(
                        onClick = state.onSkipPingingRequest,
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                    ) { Text(t(Res.string.sort_button_skip_pinging)) }
                    Spacer(Modifier.width(SmallSpacing))
                }
                if (!state.sortComputed) {
                    TextButton(onClick = onExitRequested, modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)) {
                        Text(t(Res.string.dialog_cancel_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun PingingPane(
    serversCount: Int,
    pingProgress: Int,
    timeRemainingMs: Long,
    onSkip: () -> Unit,
) {
    val progress = if (serversCount == 0) 0f else pingProgress.coerceAtMost(serversCount) / serversCount.toFloat()

    Column(verticalArrangement = Arrangement.spacedBy(InfoRowSpacing)) {
        Text(t(Res.string.sort_pinging_servers), style = MaterialTheme.typography.titleSmall)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(LinearProgressHeight),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "$pingProgress/$serversCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val secs = ceil(timeRemainingMs / MILLIS_IN_SECOND).toInt()
            Text(
                stringResource(Res.string.sort_max_time_remaining, secs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AssistiveText(
            leading = Icons.Outlined.Info,
            text = t(Res.string.sort_skip_message),
        )
        FilledTonalButton(
            onClick = onSkip,
            enabled = pingProgress < serversCount,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
        ) { Text(t(Res.string.sort_button_skip_pinging)) }
    }
}

@Composable
private fun SortingPane(
    status: StringResource,
    progress: Float,
) = Column(verticalArrangement = Arrangement.spacedBy(InfoRowSpacing)) {
    Text(t(status), style = MaterialTheme.typography.titleSmall)
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(LinearProgressHeight),
    )
}

private enum class StepState { Active, Done, Upcoming }

@Composable
private fun StepRow(
    icon: ImageVector,
    title: String,
    state: StepState,
) {
    val (container, content) =
        when (state) {
            StepState.Active ->
                MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
            StepState.Done ->
                MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
            StepState.Upcoming ->
                MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = RowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Surface(color = container, contentColor = content, shape = MaterialTheme.shapes.medium) {
            Row(
                Modifier.padding(horizontal = ChipHorizontalPadding, vertical = ChipVerticalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MediumSpacing),
            ) {
                Icon(icon, contentDescription = title)
                Text(title, style = MaterialTheme.typography.bodyMedium)
            }
        }
        when (state) {
            StepState.Active -> StatusChip(text = t(Res.string.sort_status_in_progress))
            StepState.Done -> StatusChip(text = t(Res.string.sort_status_done))
            StepState.Upcoming -> StatusChip(text = t(Res.string.sort_status_next))
        }
    }
}

@Composable
private fun StatusChip(text: String) =
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = ChipHorizontalPadding, vertical = ChipVerticalPadding / 2),
            style = MaterialTheme.typography.labelMedium,
        )
    }

private data class InfoItem(
    val icon: ImageVector,
    val label: String,
    val value: String,
)

@Composable
private fun InfoRowGrid(
    items: List<InfoItem>,
    spacing: Dp = InfoRowSpacing,
) = Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
    items.chunked(INFO_ROW_COLUMNS).forEach { rowItems ->
        Row(horizontalArrangement = Arrangement.spacedBy(spacing), modifier = Modifier.fillMaxWidth()) {
            rowItems.forEach { item ->
                InfoRow(item, Modifier.weight(1f))
            }
            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun InfoRow(
    item: InfoItem,
    modifier: Modifier = Modifier,
) = Row(
    modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(MediumSpacing),
) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
        Icon(item.icon, contentDescription = item.label, modifier = Modifier.padding(IconPadding))
    }
    Column {
        Text(
            item.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(item.value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun AssistiveText(
    leading: ImageVector,
    text: String,
) = Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SmallSpacing)) {
    Icon(leading, contentDescription = text)
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private enum class SortStep { Pinging, Sorting }

internal enum class SortType(
    val label: StringResource,
) {
    Ping(Res.string.sort_mode_ping),
    MaxPlayerCount(Res.string.sort_mode_player_count),
}

const val MIN_CONCURRENCY_CAP = 4
const val MAX_CONCURRENCY_CAP = 32
const val CPU_THREADS_MULTIPLIER = 2

const val STATUS_TWEEN_MS = 300
const val COUNTDOWN_INTERVAL_MS = 1_000
const val READY_DELAY_AFTER_DONE_MS = 2_000L
const val INFO_ROW_COLUMNS = 2
const val MILLIS_IN_SECOND = 1000.0

private val WindowPadding = 24.dp
private val ScreenPadding = 20.dp
private val ContentMaxWidth = 880.dp
private val LinearProgressHeight = 8.dp

private val ContentSpacing = 16.dp
private val InfoRowSpacing = 16.dp
private val SmallSpacing = 8.dp
private val MediumSpacing = 12.dp

private val IconPadding = 8.dp
private val ChipHorizontalPadding = 12.dp
private val ChipVerticalPadding = 8.dp
private val RowVerticalPadding = 8.dp
