/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025-2026 SpoilerRules
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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
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
import server_list_explorer.ui.generated.resources.open_source_licenses_button
import server_list_explorer.ui.generated.resources.settings_navigator_title
import server_list_explorer.ui.generated.resources.settings_section_multiplayer
import server_list_explorer.ui.generated.resources.settings_section_preferences
import server_list_explorer.ui.generated.resources.settings_section_theme
import server_list_explorer.ui.generated.resources.settings_section_window_appearance
import kotlin.math.abs
import kotlin.math.absoluteValue

@Composable
internal fun SettingsScreen() {
    val prefs = LocalPrefs.current
    var openSourceDialogVisible by remember { mutableStateOf(false) }
    val sections =
        remember {
            mutableListOf<Pair<@Composable () -> String, @Composable () -> Unit>>().apply {
                add(@Composable { t(Res.string.settings_section_theme) } to { ThemeSettings() })
                add(@Composable { t(Res.string.settings_section_preferences) } to { PreferenceSettings() })
                add(@Composable { t(Res.string.settings_section_multiplayer) } to { MultiplayerSettings() })
                if (OSUtils.isWindows) {
                    add(
                        @Composable { t(Res.string.settings_section_window_appearance) } to
                            { WindowAppearenceSettings() },
                    )
                }
            }
        }

    val animatedPadding by animateDpAsState(
        targetValue = if (WindowManager.isWindowCompact) MainPaddingCompact else MainPaddingExpanded,
        animationSpec =
            tween(
                durationMillis = ANIMATION_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
        label = "SettingsMainPadding",
    )

    val animatedWidthFraction by animateFloatAsState(
        targetValue =
            if (WindowManager.isWindowCompact) {
                COMPACT_WINDOW_WIDTH_FRACTION
            } else {
                EXPANDED_WINDOW_WIDTH_FRACTION
            },
        animationSpec =
            tween(
                durationMillis = ANIMATION_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
        label = "SettingsContentWidthFraction",
    )

    val animatedSpacerWidth by animateDpAsState(
        targetValue = if (WindowManager.isWindowCompact) MainRowSpacerCompact else MainRowSpacerExpanded,
        animationSpec =
            tween(
                durationMillis = ANIMATION_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
        label = "SettingsNavigatorSpacerWidth",
    )

    val sectionHeights = remember { mutableStateListOf<Int>() }
    var totalContentHeight by remember { mutableStateOf(0) }
    var viewportHeightPx by remember { mutableStateOf(0) }

    val scrollState = rememberScrollState()

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
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .onSizeChanged { totalContentHeight = it.height },
                verticalArrangement = Arrangement.spacedBy(SectionSpacing),
            ) {
                sections.forEachIndexed { index, (_, contentComposable) ->
                    Box(
                        modifier =
                            Modifier.onSizeChanged { newSize ->
                                val h = newSize.height

                                // ensure list has space for this index, filling gaps with zeros
                                if (sectionHeights.size <= index) {
                                    sectionHeights += List(index - sectionHeights.size + 1) { 0 }
                                }

                                sectionHeights[index] = h
                            },
                    ) {
                        contentComposable()
                    }
                }

                TextButton(
                    onClick = { openSourceDialogVisible = true },
                    modifier = Modifier.align(Alignment.Start).pointerHoverIcon(PointerIcon.Hand),
                ) {
                    Text(t(Res.string.open_source_licenses_button))
                }
            }
        }

        Spacer(modifier = Modifier.width(MainRowSpacerWidth))

        AppVerticalScrollbar(
            adapter =
                rememberScrollbarAdapter(
                    scrollState = scrollState,
                    viewportHeightPx = viewportHeightPx,
                    totalContentHeight = totalContentHeight,
                ),
            alwaysVisible = prefs.settingsScrollbarAlwaysVisible,
        )

        Spacer(modifier = Modifier.width(animatedSpacerWidth))

        SectionNavigator(
            sectionTitles = sections.map { it.first },
            scrollState = scrollState,
            sectionHeights = sectionHeights,
            totalContentHeight = totalContentHeight,
            viewportHeightPx = viewportHeightPx,
            modifier = Modifier.width(IntrinsicSize.Max).fillMaxHeight(),
        )
    }

    OpenSourceLicensesDialog(
        visible = openSourceDialogVisible,
        onDismissRequest = { openSourceDialogVisible = false },
    )
}

@Composable
private fun rememberScrollbarAdapter(
    scrollState: ScrollState,
    viewportHeightPx: Int,
    totalContentHeight: Int,
): ScrollbarAdapter =
    remember(scrollState, viewportHeightPx, totalContentHeight) {
        object : ScrollbarAdapter {
            override val scrollOffset
                get() = scrollState.value.toDouble()

            override val contentSize
                get() = totalContentHeight.toDouble().coerceAtLeast(viewportHeightPx.toDouble())

            override val viewportSize
                get() = viewportHeightPx.toDouble()

            override suspend fun scrollTo(scrollOffset: Double) {
                // compute the desired scroll position, clamped within valid bounds
                val maxScroll = (totalContentHeight - viewportHeightPx).coerceAtLeast(0)
                val targetScroll = scrollOffset.coerceIn(0.0, maxScroll.toDouble()).toFloat()

                // calculate the difference between the desired and current scroll positions
                val currentScroll = scrollState.value.toFloat()
                val delta = targetScroll - currentScroll

                // only scroll if the difference is large enough to be meaningful
                // this avoids tiny, jittery corrections caused by floating-point precision errors
                if (delta.absoluteValue > SCROLL_DELTA_THRESHOLD) {
                    scrollState.scrollBy(delta)
                }
            }
        }
    }

@Composable
private fun SectionNavigator(
    sectionTitles: List<@Composable () -> String>,
    scrollState: ScrollState,
    sectionHeights: List<Int>,
    totalContentHeight: Int,
    viewportHeightPx: Int,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val spacingPx = with(density) { SectionSpacing.toPx() }

    val currentSectionIndex by remember(scrollState.value, sectionHeights, viewportHeightPx) {
        derivedStateOf {
            if (sectionHeights.isEmpty() || viewportHeightPx <= 0) return@derivedStateOf 0

            val anchor = scrollState.value + viewportHeightPx / 2

            val gap = with(density) { SectionSpacing.toPx() }.toInt()

            var offset = 0
            val centers =
                sectionHeights.map { height ->
                    val center = offset + height / 2
                    offset += height + gap
                    center
                }

            centers
                .withIndex()
                .minByOrNull { (_, center) -> abs(center - anchor) }
                ?.index
                ?.coerceIn(0, sectionTitles.lastIndex)
                ?: 0
        }
    }

    val baseOffsetDp =
        with(density) {
            (SectionButtonHeight.toPx() + NavigatorBaseOffset.toPx()).toDp()
        }

    val shaftHeightDp = sectionTitles.size * SectionButtonHeight

    val indicatorOffsetY by animateDpAsState(
        targetValue = baseOffsetDp + (currentSectionIndex * SectionButtonHeight),
        animationSpec =
            tween(
                durationMillis = ANIMATION_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
        label = "SettingsNavigatorIndicatorOffset",
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

        // "thumb" that moves as current section changes
        Box(
            modifier =
                Modifier
                    .width(NavigatorThumbWidth)
                    .height(SectionButtonHeight)
                    .offset(x = 0.dp, y = indicatorOffsetY)
                    .background(MaterialTheme.colorScheme.primary),
        )

        Column(verticalArrangement = Arrangement.spacedBy(ColumnArrangement)) {
            Text(
                text = t(Res.string.settings_navigator_title),
                style = MaterialTheme.typography.titleLarge,
                color = ListItemDefaults.colors().supportingContentColor,
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

                    val onClickAction: () -> Unit = {
                        scope.launch {
                            if (sectionHeights.isEmpty()) return@launch

                            val spacing = spacingPx.toInt()
                            val gaps = (index - 1).coerceAtLeast(0)
                            val target = sectionHeights.take(index).sum() + spacing * gaps

                            val maxScroll = (totalContentHeight - viewportHeightPx).coerceAtLeast(0)
                            val clamped = target.coerceIn(0, maxScroll)

                            scrollState.animateScrollTo(
                                clamped,
                                animationSpec =
                                    tween(
                                        durationMillis = ANIMATION_DURATION_MILLIS,
                                        easing = LinearOutSlowInEasing,
                                    ),
                            )
                        }
                    }

                    SlickTextButton(
                        contentColor = textColor,
                        onClick = onClickAction,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(SectionButtonHeight),
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
private const val COMPACT_WINDOW_WIDTH_FRACTION = 0.75f
private const val EXPANDED_WINDOW_WIDTH_FRACTION = 0.6f
private const val SCROLL_DELTA_THRESHOLD = 0.5f

private val SectionSpacing = 24.dp
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
