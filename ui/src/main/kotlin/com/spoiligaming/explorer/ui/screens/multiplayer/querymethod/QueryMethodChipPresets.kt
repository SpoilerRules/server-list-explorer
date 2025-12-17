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

package com.spoiligaming.explorer.ui.screens.multiplayer.querymethod

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.spoiligaming.explorer.settings.model.ServerQueryMethod
import com.spoiligaming.explorer.ui.t
import org.jetbrains.compose.resources.StringResource
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.query_method_chip_api_cached
import server_list_explorer.ui.generated.resources.query_method_chip_editors_choice
import server_list_explorer.ui.generated.resources.query_method_chip_editors_choice_badge
import server_list_explorer.ui.generated.resources.query_method_chip_gs4_query_udp
import server_list_explorer.ui.generated.resources.query_method_chip_hosted_api
import server_list_explorer.ui.generated.resources.query_method_chip_srv_support

private val EditorsChoicePreset =
    ChipPreset(
        label = Res.string.query_method_chip_editors_choice,
        icon =
            ChipPresetIcon(
                vector = Icons.Outlined.Verified,
                contentDescription = Res.string.query_method_chip_editors_choice_badge,
            ),
        colors = { QueryMethodChipStyles.green(1f) },
    )

private val SrvSupportPreset =
    ChipPreset(
        label = Res.string.query_method_chip_srv_support,
        colors = { QueryMethodChipStyles.common() },
    )

private val QueryUdpPreset =
    ChipPreset(
        label = Res.string.query_method_chip_gs4_query_udp,
        colors = { QueryMethodChipStyles.green(0.8f) },
    )

private val HostedApiPreset =
    ChipPreset(
        label = Res.string.query_method_chip_hosted_api,
        colors = { QueryMethodChipStyles.blue(0.8f) },
    )

private val ApiCachedPreset =
    ChipPreset(
        label = Res.string.query_method_chip_api_cached,
        colors = { QueryMethodChipStyles.yellow(0.5f) },
    )

@Composable
internal fun queryMethodChipsFor(method: ServerQueryMethod) =
    when (method) {
        ServerQueryMethod.McSrvStat ->
            listOf(
                HostedApiPreset,
                ApiCachedPreset,
                QueryUdpPreset,
                SrvSupportPreset,
            )

        ServerQueryMethod.McUtils ->
            listOf(
                EditorsChoicePreset,
                SrvSupportPreset,
            )
    }.map { preset ->
        val icon =
            preset.icon?.let { iconPreset ->
                QueryMethodChip.ChipIcon(
                    vector = iconPreset.vector,
                    contentDescription = t(iconPreset.contentDescription),
                )
            }
        QueryMethodChip(
            label = t(preset.label),
            icon = icon,
            colors = preset.colors(),
        )
    }

private data class ChipPreset(
    val label: StringResource,
    val icon: ChipPresetIcon? = null,
    val colors: @Composable () -> QueryMethodChipColors,
)

private data class ChipPresetIcon(
    val vector: ImageVector,
    val contentDescription: StringResource,
)
