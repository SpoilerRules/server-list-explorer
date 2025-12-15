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

package com.spoiligaming.explorer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLinkStyles
import sh.calvin.autolinktext.SimpleTextMatchResult
import sh.calvin.autolinktext.TextMatcher
import sh.calvin.autolinktext.TextRule
import sh.calvin.autolinktext.TextRuleDefaults
import sh.calvin.autolinktext.rememberAutoLinkText

@Composable
internal fun rememberAutoLinkMarkdownAnnotatedString(
    text: String,
    parseMarkdownLinks: Boolean = true,
    includeDefaultAutoLinks: Boolean = true,
    defaultLinkStyles: TextLinkStyles,
    markdownLinkStyles: TextLinkStyles = defaultLinkStyles,
): AnnotatedString {
    val parsed =
        remember(text, parseMarkdownLinks) {
            if (parseMarkdownLinks) parseMarkdownLinks(text) else MarkdownParseResult(text, emptyList())
        }

    val markdownRule =
        remember(parsed.matches, markdownLinkStyles) {
            TextRule.Url(
                textMatcher = TextMatcher.FunctionMatcher { parsed.matches },
                styles = markdownLinkStyles,
                urlProvider = { it.data },
            )
        }

    val defaultRules = if (includeDefaultAutoLinks) TextRuleDefaults.defaultList() else emptyList()

    val rules =
        remember(markdownRule, includeDefaultAutoLinks, defaultRules) {
            buildList<TextRule<*>> {
                add(markdownRule)
                addAll(defaultRules)
            }
        }

    return AnnotatedString.rememberAutoLinkText(
        text = parsed.displayText,
        textRules = rules.asAnyRules(),
        defaultLinkStyles = defaultLinkStyles,
    )
}

private fun parseMarkdownLinks(input: String): MarkdownParseResult {
    val out = StringBuilder(input.length)
    val matches = ArrayList<SimpleTextMatchResult<String>>()

    var lastIndex = 0
    for (m in MarkdownLinkRegex.findAll(input)) {
        out.append(input, lastIndex, m.range.first)

        val label = m.groupValues[1]
        val url = m.groupValues[2]

        val start = out.length
        out.append(label)
        val end = out.length

        matches += SimpleTextMatchResult(start, end, url)

        lastIndex = m.range.last + 1
    }

    if (lastIndex < input.length) out.append(input, lastIndex, input.length)

    return MarkdownParseResult(
        displayText = out.toString(),
        matches = matches,
    )
}

@Immutable
private data class MarkdownParseResult(
    val displayText: String,
    val matches: List<SimpleTextMatchResult<String>>,
)

@Suppress("UNCHECKED_CAST")
private fun Collection<TextRule<*>>.asAnyRules() = this as Collection<TextRule<Any?>>

private val MarkdownLinkRegex = Regex("""\[(.+?)]\((https?://[^\s)]+)\)""")
