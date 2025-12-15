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

package com.spoiligaming.explorer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator

private const val NO_TELEMETRY_ARG = "--no-telemetry"
private const val VERBOSE_ARG = "--verbose"

internal data class ParsedArgs(
    val telemetryEnabled: Boolean,
    val verboseLoggingEnabled: Boolean,
)

internal object ArgsParser {
    fun parse(args: Array<String>): ParsedArgs {
        val verboseLoggingEnabled = VERBOSE_ARG in args
        if (verboseLoggingEnabled) {
            enableVerboseLogging()
        }

        return ParsedArgs(
            telemetryEnabled = NO_TELEMETRY_ARG !in args,
            verboseLoggingEnabled = verboseLoggingEnabled,
        )
    }

    private fun enableVerboseLogging() {
        Configurator.setRootLevel(Level.ALL)
        logger.info { "Verbose logging enabled." }
    }
}

private val logger = KotlinLogging.logger {}
