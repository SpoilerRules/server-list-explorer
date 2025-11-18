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

package com.spoiligaming.explorer.minecraft.multiplayer.online.backend.mcutils

internal class RetryPolicy(
    val exceptionClass: Class<out Throwable>,
    val maxAttempts: Int,
    val onRetry: (attempt: Int, maxAttempts: Int, cause: Throwable) -> Unit,
)

internal inline fun <reified E : Throwable> retryPolicy(
    maxAttempts: Int,
    noinline onRetry: (attempt: Int, maxAttempts: Int, cause: E) -> Unit,
): RetryPolicy {
    require(maxAttempts >= 1) {
        "maxAttempts must be >= 1 (was $maxAttempts for ${E::class.simpleName})"
    }

    return RetryPolicy(
        exceptionClass = E::class.java,
        maxAttempts = maxAttempts,
        onRetry = { attempt, max, cause ->
            @Suppress("UNCHECKED_CAST")
            onRetry(attempt, max, cause as E)
        },
    )
}

internal inline fun <T> retry(
    vararg policies: RetryPolicy,
    block: () -> T,
): T {
    if (policies.isEmpty()) return block()

    val size = policies.size
    val attemptsUsed = IntArray(size)

    while (true) {
        try {
            return block()
        } catch (t: Throwable) {
            var index = -1
            for (i in 0 until size) {
                if (policies[i].exceptionClass.isInstance(t)) {
                    index = i
                    break
                }
            }

            if (index == -1) {
                throw t
            }

            val policy = policies[index]
            val used = attemptsUsed[index]

            if (used + 1 >= policy.maxAttempts) {
                throw t
            }

            attemptsUsed[index] = used + 1
            policy.onRetry(used, policy.maxAttempts, t)
        }
    }
}
