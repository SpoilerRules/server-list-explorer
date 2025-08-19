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

package com.spoiligaming.explorer.util

import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

fun Long.toHumanReadableDuration(): String {
    val d = this.milliseconds

    return when {
        d < 1.seconds ->
            "${d.inWholeMilliseconds} ms"

        d < 1.minutes -> {
            val sec = d.toDouble(DurationUnit.SECONDS)
            String.format("%.1f seconds", sec)
        }

        d < 1.hours -> {
            val m = d.inWholeMinutes
            "$m ${plural(m, "minute")}"
        }

        d < 1.days -> {
            val h = d.inWholeHours
            "$h ${plural(h, "hour")}"
        }

        d < 365.days -> {
            val dayCount = d.inWholeDays
            "$dayCount ${plural(dayCount, "day")}"
        }

        else -> {
            val years = d.inWholeDays / 365
            "$years ${plural(years, "year")}"
        }
    }
}

private fun plural(
    value: Long,
    unit: String,
) = if (value == 1L) unit else "${unit}s"
