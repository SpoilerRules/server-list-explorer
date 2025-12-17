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

import androidx.compose.runtime.Composable
import com.spoiligaming.explorer.settings.model.ServerQueryMethod
import com.spoiligaming.explorer.settings.model.ServerQueryMethodConfigurations
import com.spoiligaming.explorer.ui.t
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.query_method_mc_srv_stat
import server_list_explorer.ui.generated.resources.query_method_mc_utils
import server_list_explorer.ui.generated.resources.query_method_mcutils_srv_lookup_desc
import server_list_explorer.ui.generated.resources.query_method_mcutils_srv_lookup_title
import server_list_explorer.ui.generated.resources.query_method_timeout_common_desc
import server_list_explorer.ui.generated.resources.query_method_timeout_mcsrvstat_connection_desc
import server_list_explorer.ui.generated.resources.query_method_timeout_mcsrvstat_connection_title
import server_list_explorer.ui.generated.resources.query_method_timeout_mcsrvstat_response_desc
import server_list_explorer.ui.generated.resources.query_method_timeout_mcsrvstat_response_title
import server_list_explorer.ui.generated.resources.query_method_timeout_mcutils_title
import server_list_explorer.ui.generated.resources.query_method_tooltip_title_mcsrvstat
import server_list_explorer.ui.generated.resources.query_method_tooltip_title_mcutils

internal typealias QueryMethodConfigurationUpdater =
    ((ServerQueryMethodConfigurations) -> ServerQueryMethodConfigurations) -> Unit

@Composable
internal fun queryMethodDefinitions(
    configurations: ServerQueryMethodConfigurations,
    updateConfigurations: QueryMethodConfigurationUpdater,
) = listOf(
    mcSrvStatDefinition(
        configurations = configurations,
        updateConfigurations = updateConfigurations,
    ),
    mcUtilsDefinition(
        configurations = configurations,
        updateConfigurations = updateConfigurations,
    ),
)

@Composable
private fun mcSrvStatDefinition(
    configurations: ServerQueryMethodConfigurations,
    updateConfigurations: QueryMethodConfigurationUpdater,
): QueryMethodDefinition {
    val mcSrvStatTimeouts = configurations.mcSrvStat.timeouts

    return QueryMethodDefinition(
        method = ServerQueryMethod.McSrvStat,
        title = t(Res.string.query_method_mc_srv_stat),
        chips = queryMethodChipsFor(ServerQueryMethod.McSrvStat),
        configuration =
            QueryMethodConfiguration(
                title = t(Res.string.query_method_tooltip_title_mcsrvstat),
                items =
                    listOf(
                        TimeoutSliderSpec(
                            title = t(Res.string.query_method_timeout_mcsrvstat_connection_title),
                            description = t(Res.string.query_method_timeout_mcsrvstat_connection_desc),
                            valueSeconds =
                                (mcSrvStatTimeouts.connectionTimeoutMillis / MILLIS_PER_SECOND).toInt(),
                            valueRangeSeconds = 1f..600f,
                        ) { newSeconds ->
                            updateConfigurations { current ->
                                val updatedMcSrvStat =
                                    current.mcSrvStat.timeouts.copy(
                                        connectionTimeoutMillis = newSeconds.toLong() * MILLIS_PER_SECOND,
                                    )
                                current.copy(
                                    mcSrvStat = current.mcSrvStat.copy(timeouts = updatedMcSrvStat),
                                )
                            }
                        },
                        TimeoutSliderSpec(
                            title = t(Res.string.query_method_timeout_mcsrvstat_response_title),
                            description = t(Res.string.query_method_timeout_mcsrvstat_response_desc),
                            valueSeconds =
                                (mcSrvStatTimeouts.responseTimeoutMillis / MILLIS_PER_SECOND).toInt(),
                            valueRangeSeconds = 1f..60f,
                        ) { newSeconds ->
                            updateConfigurations { current ->
                                val updatedMcSrvStat =
                                    current.mcSrvStat.timeouts.copy(
                                        responseTimeoutMillis = newSeconds.toLong() * MILLIS_PER_SECOND,
                                    )
                                current.copy(
                                    mcSrvStat = current.mcSrvStat.copy(timeouts = updatedMcSrvStat),
                                )
                            }
                        },
                    ),
            ),
    )
}

@Composable
private fun mcUtilsDefinition(
    configurations: ServerQueryMethodConfigurations,
    updateConfigurations: QueryMethodConfigurationUpdater,
): QueryMethodDefinition {
    val mcUtilsTimeouts = configurations.mcUtils.timeouts
    val mcUtilsOptions = configurations.mcUtils.options

    return QueryMethodDefinition(
        method = ServerQueryMethod.McUtils,
        title = t(Res.string.query_method_mc_utils),
        chips = queryMethodChipsFor(ServerQueryMethod.McUtils),
        configuration =
            QueryMethodConfiguration(
                title = t(Res.string.query_method_tooltip_title_mcutils),
                items =
                    listOf(
                        TimeoutSliderSpec(
                            title = t(Res.string.query_method_timeout_mcutils_title),
                            description = t(Res.string.query_method_timeout_common_desc),
                            valueSeconds =
                                (mcUtilsTimeouts.timeoutMillis / MILLIS_PER_SECOND).toInt(),
                            valueRangeSeconds = 1f..120f,
                        ) { newSeconds ->
                            updateConfigurations { current ->
                                val updatedMcUtils =
                                    current.mcUtils.timeouts.copy(
                                        timeoutMillis = newSeconds.toLong() * MILLIS_PER_SECOND,
                                    )
                                current.copy(
                                    mcUtils = current.mcUtils.copy(timeouts = updatedMcUtils),
                                )
                            }
                        },
                        QueryMethodCheckboxSpec(
                            title = t(Res.string.query_method_mcutils_srv_lookup_title),
                            description = t(Res.string.query_method_mcutils_srv_lookup_desc),
                            checked = mcUtilsOptions.enableSrvLookups,
                        ) { enabled ->
                            updateConfigurations { current ->
                                current.copy(
                                    mcUtils =
                                        current.mcUtils.copy(
                                            options = current.mcUtils.options.copy(enableSrvLookups = enabled),
                                        ),
                                )
                            }
                        },
                    ),
            ),
    )
}

private const val MILLIS_PER_SECOND = 1000L
