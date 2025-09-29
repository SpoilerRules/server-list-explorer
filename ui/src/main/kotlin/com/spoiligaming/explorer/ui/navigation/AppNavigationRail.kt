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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalAmoledActive
import com.spoiligaming.explorer.ui.extensions.onHover
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.theme.isDarkTheme
import com.spoiligaming.explorer.ui.util.rememberAdaptiveWidth
import com.spoiligaming.explorer.ui.window.WindowManager
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.cd_collapse_rail
import server_list_explorer.ui.generated.resources.cd_expand_rail
import server_list_explorer.ui.generated.resources.cd_switch_to_dark_mode
import server_list_explorer.ui.generated.resources.cd_switch_to_light_mode
import server_list_explorer.ui.generated.resources.nav_label_multiplayer
import server_list_explorer.ui.generated.resources.nav_label_settings

@Composable
internal fun AppNavigationRail(
    navController: NavHostController,
    onThemeToggle: () -> Unit,
) {
    val amoledOn = LocalAmoledActive.current

    var userRequestedExpand by remember { mutableStateOf(false) }
    val isActuallyExpanded = if (WindowManager.isWindowCompact) false else userRequestedExpand

    val expandedRailWidth =
        rememberAdaptiveWidth(min = RailExpandedMinWidth, max = RailExpandedMaxWidth)

    val targetRailWidth = if (isActuallyExpanded) expandedRailWidth else RailCollapsedWidth
    val animatedRailWidth by animateDpAsState(
        targetValue = targetRailWidth,
        animationSpec =
            tween(
                durationMillis = RailWidthAnimDurationMs,
                easing = FastOutSlowInEasing,
            ),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val railItems = getNavigationRailItems()

    Row {
        NavigationRail(
            containerColor =
                if (amoledOn) {
                    Color.Black
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
            modifier = Modifier.width(animatedRailWidth).fillMaxHeight(),
            header = {
                AnimatedVisibility(
                    visible = !WindowManager.isWindowCompact,
                    enter =
                        fadeIn(animationSpec = HeaderFadeSpec) +
                            slideInVertically(
                                initialOffsetY = { -it / SlideOffsetDivider },
                                animationSpec = HeaderSlideSpec,
                            ),
                    exit =
                        fadeOut(animationSpec = HeaderFadeSpec) +
                            slideOutVertically(
                                targetOffsetY = { -it / SlideOffsetDivider },
                                animationSpec = HeaderSlideSpec,
                            ),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max)
                                .padding(
                                    horizontal =
                                        if (isActuallyExpanded) {
                                            BottomPaddingExpandedHorizontal
                                        } else {
                                            BottomPaddingCollapsedHorizontal
                                        },
                                ),
                        contentAlignment =
                            if (isActuallyExpanded) {
                                Alignment.CenterStart
                            } else {
                                Alignment.Center
                            },
                    ) {
                        ExpandCollapseButton(
                            expanded = isActuallyExpanded,
                            onClick = {
                                userRequestedExpand = !userRequestedExpand
                            },
                        )
                    }
                }
            },
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(ItemsSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                railItems.forEach { item ->
                    if (item.showDividerAbove) {
                        HorizontalDivider(
                            modifier =
                                Modifier.padding(
                                    horizontal =
                                        if (isActuallyExpanded) {
                                            DividerPaddingExpandedHorizontal
                                        } else {
                                            DividerPaddingCollapsedHorizontal
                                        },
                                    vertical =
                                        if (isActuallyExpanded) {
                                            DividerPaddingExpandedVertical
                                        } else {
                                            DividerPaddingCollapsedVertical
                                        },
                                ),
                        )
                    }

                    val route = item.screen::class.qualifiedName
                    val isSelected = currentRoute == route
                    val onClick = { if (!isSelected) navController.navigate(item.screen) }

                    if (isActuallyExpanded) {
                        NavigationDrawerItem(
                            item = item,
                            isSelected = isSelected,
                            disabled = item.disabled,
                            onClick = onClick,
                        )
                    } else {
                        NavigationRailItem(
                            item = item,
                            isSelected = isSelected,
                            disabled = item.disabled,
                            onClick = onClick,
                        )
                    }
                }

                Spacer(Modifier.weight(SpacerExpandWeight))

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max)
                            .padding(
                                horizontal =
                                    if (isActuallyExpanded) {
                                        BottomPaddingExpandedHorizontal
                                    } else {
                                        BottomPaddingCollapsedHorizontal
                                    },
                            ),
                    contentAlignment =
                        if (isActuallyExpanded) {
                            Alignment.CenterStart
                        } else {
                            Alignment.Center
                        },
                ) {
                    ThemeToggleButton(
                        onClick = onThemeToggle,
                    )
                }

                Spacer(Modifier.height(BottomSpacerHeight))
            }
        }

        if (amoledOn) {
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = DividerThicknessThin,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun <E> NavigationRailItem(
    item: Item<E>,
    isSelected: Boolean,
    disabled: Boolean,
    onClick: () -> Unit,
) {
    val contentColor =
        if (isSelected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    NavigationRailItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Icon(
                modifier = Modifier.size(IconSize),
                imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                contentDescription = item.label,
                tint = contentColor,
            )
        },
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand).focusProperties { canFocus = false },
        enabled = !disabled,
        label = {
            Text(
                text = item.label,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
            )
        },
    )
}

@Composable
private fun <E> NavigationDrawerItem(
    item: Item<E>,
    isSelected: Boolean,
    disabled: Boolean,
    onClick: () -> Unit,
) {
    val contentColor =
        if (isSelected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    NavigationDrawerItem(
        label = {
            Text(
                text = item.label,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        selected = isSelected,
        onClick = onClick,
        // disabled = disabled,
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand).focusProperties { canFocus = false },
        icon = {
            Icon(
                modifier = Modifier.size(IconSize),
                imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                contentDescription = item.label,
                tint = contentColor,
            )
        },
    )
}

@Composable
private fun ExpandCollapseButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconVector = if (expanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Filled.Menu
    val contentDesc = if (expanded) t(Res.string.cd_collapse_rail) else t(Res.string.cd_expand_rail)

    IconButton(
        onClick = onClick,
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand).focusProperties { canFocus = false },
    ) {
        Icon(
            modifier = Modifier.size(IconSize),
            imageVector = iconVector,
            contentDescription = contentDesc,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ThemeToggleButton(onClick: () -> Unit) {
    var isHovered by remember { mutableStateOf(false) }
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val transition = updateTransition(targetState = isDarkTheme, label = "theme transition")

    val sunIcon = if (isHovered) Icons.Filled.LightMode else Icons.Outlined.LightMode
    val moonIcon = if (isHovered) Icons.Filled.DarkMode else Icons.Outlined.DarkMode

    val switchToLightModeText = t(Res.string.cd_switch_to_light_mode)
    val switchToDarkModeText = t(Res.string.cd_switch_to_dark_mode)

    OutlinedIconButton(
        onClick = onClick,
        modifier =
            Modifier
                .size(ToggleButtonSize)
                .pointerHoverIcon(PointerIcon.Hand)
                .focusProperties { canFocus = false }
                .onHover { isHovered = it }
                .semantics {
                    contentDescription =
                        if (isDarkTheme) {
                            switchToLightModeText
                        } else {
                            switchToDarkModeText
                        }
                },
    ) {
        Box(Modifier.size(ToggleIconBoxSize)) {
            // sun animation
            transition.AnimatedVisibility(
                visible = { !it },
                enter =
                    fadeIn() +
                        slideInVertically(
                            initialOffsetY = { fullHeight -> -fullHeight },
                            animationSpec = ToggleSlideSpec,
                        ),
                exit =
                    fadeOut() +
                        slideOutVertically(
                            targetOffsetY = { fullHeight -> -fullHeight },
                            animationSpec = ToggleSlideSpec,
                        ),
            ) {
                Icon(
                    modifier = Modifier.size(IconSize),
                    imageVector = sunIcon,
                    contentDescription = null,
                    tint = iconTint,
                )
            }

            // moon animation
            transition.AnimatedVisibility(
                visible = { it },
                enter =
                    fadeIn() +
                        slideInVertically(
                            initialOffsetY = { fullHeight -> fullHeight },
                            animationSpec = ToggleSlideSpec,
                        ),
                exit =
                    fadeOut() +
                        slideOutVertically(
                            targetOffsetY = { fullHeight -> fullHeight },
                            animationSpec = ToggleSlideSpec,
                        ),
            ) {
                Icon(
                    modifier = Modifier.size(IconSize),
                    imageVector = moonIcon,
                    contentDescription = null,
                    tint = iconTint,
                )
            }
        }
    }
}

private data class Item<E>(
    val screen: E,
    val label: String,
    val badge: String? = null,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val showDividerAbove: Boolean = false,
    val disabled: Boolean = false,
)

@Composable
private fun getNavigationRailItems() =
    listOf(
        Item(
            screen = MultiplayerServerListScreen,
            label = t(Res.string.nav_label_multiplayer),
            filledIcon = Icons.Filled.Group,
            outlinedIcon = Icons.Outlined.Group,
        ),
        /*
        Item(
            screen = SingleplayerWorldListScreen,
            label = t(Res.string.nav_label_singleplayer),
            filledIcon = Icons.Filled.Terrain,
            outlinedIcon = Icons.Outlined.Terrain,
        ),
         */
        Item(
            screen = SettingsScreen,
            label = t(Res.string.nav_label_settings),
            filledIcon = Icons.Filled.Settings,
            outlinedIcon = Icons.Outlined.Settings,
            showDividerAbove = true,
        ),
    )

private val RailCollapsedWidth = 96.dp
private val RailExpandedMinWidth = 220.dp
private val RailExpandedMaxWidth = 360.dp
private const val RailWidthAnimDurationMs = 120

private const val HeaderAnimDurationMs = 200
private const val SlideOffsetDivider = 2

private val ItemsSpacing = 4.dp
private val DividerPaddingExpandedHorizontal = 28.dp
private val DividerPaddingCollapsedHorizontal = 16.dp
private val DividerPaddingExpandedVertical = 12.dp
private val DividerPaddingCollapsedVertical = 8.dp

private val BottomPaddingExpandedHorizontal = 16.dp
private val BottomPaddingCollapsedHorizontal = 0.dp
private val BottomSpacerHeight = 12.dp

private val IconSize = 24.dp
private val ToggleButtonSize = 56.dp
private val ToggleIconBoxSize = 24.dp
private const val ToggleAnimDurationMs = 250

private val DividerThicknessThin = 0.5.dp
private const val SpacerExpandWeight = 1f

private val HeaderFadeSpec: FiniteAnimationSpec<Float> =
    tween(durationMillis = HeaderAnimDurationMs, easing = LinearOutSlowInEasing)
private val HeaderSlideSpec: FiniteAnimationSpec<IntOffset> =
    tween(durationMillis = HeaderAnimDurationMs, easing = LinearOutSlowInEasing)
private val ToggleSlideSpec: FiniteAnimationSpec<IntOffset> =
    tween(durationMillis = ToggleAnimDurationMs, easing = FastOutLinearInEasing)
