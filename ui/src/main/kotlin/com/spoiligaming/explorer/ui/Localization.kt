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

package com.spoiligaming.explorer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import java.util.Locale

private object AppLocale {
    private var initialDefaultLocale: Locale? = null
    private val LocalLocaleTag = staticCompositionLocalOf { Locale.getDefault().toLanguageTag() }

    val currentTag: String
        @Composable get() = LocalLocaleTag.current

    @Composable
    infix fun provides(localeTag: String?): ProvidedValue<*> {
        if (initialDefaultLocale == null) {
            initialDefaultLocale = Locale.getDefault()
        }

        val resolvedLocale =
            when (localeTag) {
                null -> initialDefaultLocale!!
                else -> Locale.forLanguageTag(localeTag)
            }

        Locale.setDefault(resolvedLocale)

        return LocalLocaleTag.provides(resolvedLocale.toLanguageTag())
    }
}

@Composable
internal fun AppLocaleProvider(content: @Composable () -> Unit) {
    val locale = LocalPrefs.current.locale.toLanguageTag()

    CompositionLocalProvider(
        AppLocale provides locale,
        content = content,
    )
}

@Composable
internal fun t(stringRes: StringResource) = stringResource(stringRes)

@Composable
internal fun t(
    stringRes: StringResource,
    vararg args: Any,
) = stringResource(stringRes, *args)
