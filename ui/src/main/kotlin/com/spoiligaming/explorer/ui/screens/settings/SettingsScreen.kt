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

package com.spoiligaming.explorer.ui.screens.settings

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.spoiligaming.explorer.ui.screens.settings.sections.MultiplayerSettings
import com.spoiligaming.explorer.ui.screens.settings.sections.PreferenceSettings
import com.spoiligaming.explorer.ui.screens.settings.sections.ThemeSettings
import com.spoiligaming.explorer.ui.screens.settings.sections.WindowAppearenceSettings
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.widgets.AppVerticalScrollbar
import com.spoiligaming.explorer.ui.widgets.SlickTextButton
import com.spoiligaming.explorer.ui.window.WindowManager
import com.spoiligaming.explorer.util.OSUtils
import kotlinx.coroutines.launch
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.settings_navigator_title
import server_list_explorer.ui.generated.resources.settings_section_multiplayer
import server_list_explorer.ui.generated.resources.settings_section_preferences
import server_list_explorer.ui.generated.resources.settings_section_theme
import server_list_explorer.ui.generated.resources.settings_section_window_appearance
import kotlin.math.roundToInt

@Composable
internal fun SettingsScreen() {
    val sections =
        mutableListOf<Pair<@Composable () -> String, @Composable () -> Unit>>().apply {
            add((@Composable { t(Res.string.settings_section_theme) }) to { ThemeSettings() })
            add(
                (@Composable { t(Res.string.settings_section_preferences) }) to { PreferenceSettings() },
            )
            add(
                (@Composable { t(Res.string.settings_section_multiplayer) }) to { MultiplayerSettings() },
            )
            if (OSUtils.isWindows) {
                add(
                    (
                        @Composable {
                            t(Res.string.settings_section_window_appearance)
                        }
                    ) to { WindowAppearenceSettings() },
                )
            }
        }

    val animatedPadding by animateDpAsState(
        targetValue = if (WindowManager.isWindowCompact) MainPaddingCompact else MainPaddingExpanded,
        animationSpec =
            tween(
                durationMillis = ANIMATION_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
    )

    val animatedWidthFraction by animateFloatAsState(
        targetValue = if (WindowManager.isWindowCompact) 0.75f else 0.6f,
        animationSpec =
            tween(
                durationMillis = ANIMATION_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
    )

    val animatedSpacerWidth by animateDpAsState(
        targetValue = if (WindowManager.isWindowCompact) MainRowSpacerCompact else MainRowSpacerExpanded,
        animationSpec =
            tween(
                durationMillis = ANIMATION_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
    )

    val listState = rememberLazyListState()
    val itemHeights = remember { mutableStateMapOf<Int, Int>() }

    var viewportHeightPx by remember { mutableStateOf(0) }

    val scrollbarAdapter =
        rememberSettingsScrollbarAdapter(
            listState = listState,
            sectionCount = sections.size,
            itemHeights = itemHeights,
            viewportHeightPx = viewportHeightPx,
        )

    Row(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(start = animatedPadding),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(animatedWidthFraction)
                    .fillMaxSize()
                    .onSizeChanged { viewportHeightPx = it.height },
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(LazyColumnArrangement),
            ) {
                sections.forEachIndexed { index, (_, contentComposable) ->
                    item(key = "content_$index") {
                        Box(
                            modifier =
                                Modifier.onSizeChanged { size ->
                                    itemHeights[index] = size.height
                                },
                        ) {
                            contentComposable()
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(MainRowSpacerWidth))

        AppVerticalScrollbar(adapter = scrollbarAdapter)

        Spacer(modifier = Modifier.width(animatedSpacerWidth))

        SectionNavigator(
            sectionTitles = sections.map { it.first },
            listState = listState,
            modifier = Modifier.width(IntrinsicSize.Max).fillMaxHeight(),
        )
    }
}

@Composable
private fun rememberSettingsScrollbarAdapter(
    listState: LazyListState,
    sectionCount: Int,
    itemHeights: Map<Int, Int>,
    viewportHeightPx: Int,
): ScrollbarAdapter {
    val scope = rememberCoroutineScope()

    val averageItemHeightPx =
        remember(itemHeights.values.toList(), viewportHeightPx) {
            if (itemHeights.isNotEmpty()) {
                itemHeights.values.average().toFloat()
            } else {
                viewportHeightPx.toFloat().coerceAtLeast(1f)
            }
        }

    val totalContentHeightPx =
        remember(
            sectionCount,
            itemHeights.values.toList(),
            averageItemHeightPx,
        ) {
            (0 until sectionCount)
                .sumOf { index ->
                    itemHeights[index]?.toDouble() ?: averageItemHeightPx.toDouble()
                }.toFloat()
        }

    val scrollOffsetPx by remember(
        listState.firstVisibleItemIndex,
        listState.firstVisibleItemScrollOffset,
        itemHeights.values.toList(),
        averageItemHeightPx,
    ) {
        derivedStateOf {
            val beforeHeight =
                (0 until listState.firstVisibleItemIndex)
                    .sumOf { index ->
                        itemHeights[index]?.toDouble() ?: averageItemHeightPx.toDouble()
                    }.toFloat()
            beforeHeight + listState.firstVisibleItemScrollOffset
        }
    }

    return remember(scrollOffsetPx, totalContentHeightPx, viewportHeightPx) {
        object : ScrollbarAdapter {
            override val scrollOffset
                get() = scrollOffsetPx.toDouble()

            override val contentSize
                get() = totalContentHeightPx.toDouble()

            override val viewportSize
                get() = viewportHeightPx.toDouble()

            override suspend fun scrollTo(scrollOffset: Double) {
                // clamp to [0, maxScroll]
                val maxScroll = (totalContentHeightPx - viewportHeightPx).coerceAtLeast(0f)
                val targetPx = scrollOffset.coerceIn(0.0, maxScroll.toDouble())

                // find which section + intra‐section offset corresponds
                var accumulated = 0f
                var targetIndex = 0
                var offsetWithin = 0f

                for (index in 0 until sectionCount) {
                    val sectionHeight = itemHeights[index]?.toFloat() ?: averageItemHeightPx
                    if (accumulated + sectionHeight >= targetPx) {
                        targetIndex = index
                        offsetWithin = (targetPx - accumulated).toFloat()
                        break
                    }
                    accumulated += sectionHeight
                }

                // if targetPx >= totalContentHeightPx, scroll to last section
                if (targetPx >= totalContentHeightPx && sectionCount > 0) {
                    targetIndex = sectionCount - 1
                    offsetWithin = (itemHeights[targetIndex]?.toFloat() ?: averageItemHeightPx) - 1f
                }

                scope.launch {
                    listState.scrollToItem(
                        index = targetIndex,
                        scrollOffset = offsetWithin.roundToInt(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionNavigator(
    sectionTitles: List<@Composable () -> String>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val sectionButtonHeight = SectionButtonHeight

    val currentSectionIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex.coerceIn(0, sectionTitles.size - 1)
        }
    }

    val baseOffsetDp =
        with(LocalDensity.current) {
            (sectionButtonHeight.toPx() + NavigatorBaseOffset.toPx()).toDp()
        }

    val shaftHeightDp = sectionTitles.size * sectionButtonHeight

    val indicatorOffsetY by animateDpAsState(
        targetValue = baseOffsetDp + (currentSectionIndex * sectionButtonHeight),
        animationSpec =
            tween(
                durationMillis = ANIMATION_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
    )

    Box(modifier = modifier) {
        // background "track"
        Box(
            modifier =
                Modifier
                    .width(NavigatorShaftWidth)
                    .height(shaftHeightDp)
                    .offset(x = 0.dp, y = baseOffsetDp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = NAVIGATOR_SHAFT_OPACITY,
                        ),
                    ),
        )

        // "thumb" that moves as firstVisibleItemIndex changes
        Box(
            modifier =
                Modifier
                    .width(NavigatorThumbWidth)
                    .height(sectionButtonHeight)
                    .offset(x = 0.dp, y = indicatorOffsetY)
                    .background(MaterialTheme.colorScheme.primary),
        )

        Column(verticalArrangement = Arrangement.spacedBy(ColumnArrangement)) {
            Text(
                text = t(Res.string.settings_navigator_title),
                style = MaterialTheme.typography.titleLarge,
                color = ListItemDefaults.colors().supportingTextColor,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(modifier = Modifier.padding(start = SectionNavigatorPaddingStart)) {
                sectionTitles.forEachIndexed { index, titleProvider ->
                    val isActive = index == currentSectionIndex
                    val textColor =
                        if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                    SlickTextButton(
                        contentColor = textColor,
                        onClick = {
                            scope.launch { listState.animateScrollToItem(index) }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(sectionButtonHeight),
                    ) {
                        Text(
                            text = titleProvider(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

private const val ANIMATION_DURATION_MILLIS = 200
private const val NAVIGATOR_SHAFT_OPACITY = 0.3f

private val LazyColumnArrangement = 24.dp
private val ColumnArrangement = 8.dp
private val MainRowSpacerWidth = 4.dp
private val MainPaddingCompact = 0.dp
private val MainPaddingExpanded = 90.dp
private val MainRowSpacerCompact = 16.dp
private val MainRowSpacerExpanded = 32.dp

private val SectionButtonHeight = 40.dp
private val SectionNavigatorPaddingStart = 8.dp
private val NavigatorShaftWidth = 2.dp
private val NavigatorThumbWidth = 2.dp
private val NavigatorBaseOffset = -4.dp
