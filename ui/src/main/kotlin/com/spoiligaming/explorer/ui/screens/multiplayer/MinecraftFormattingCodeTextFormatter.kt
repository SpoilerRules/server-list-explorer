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

package com.spoiligaming.explorer.ui.screens.multiplayer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.spoiligaming.explorer.ui.screens.multiplayer.MinecraftFormattingCodeTextFormatter.appendMinecraftText
import com.spoiligaming.explorer.util.MinecraftColorCodeRegex
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.random.Random

internal object MinecraftFormattingCodeTextFormatter {
    private const val ALPHA_MASK = 0xFF000000u
    private const val RGB_HEX_LENGTH = 6
    private const val ARGB_HEX_LENGTH = 8

    /**
     * Mapping of Minecraft Java Edition color codes to corresponding Compose [Color] values.
     *
     * References:
     * - Minecraft Wiki: [Formatting codes – Color codes](https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes)
     *
     * Note:
     * - This map is specific to Java Edition color formatting.
     * - Colors are ARGB hex values as specified by the game, with `Color.Black` and `Color.White` used for clarity where exact.
     */
    private val colorMap =
        mapOf(
            '0' to Color.Black,
            '1' to Color(0xFF0000AA),
            '2' to Color(0xFF00AA00),
            '3' to Color(0xFF00AAAA),
            '4' to Color(0xFFAA0000),
            '5' to Color(0xFFAA00AA),
            '6' to Color(0xFFFFAA00),
            '7' to Color(0xFFAAAAAA),
            '8' to Color(0xFF555555),
            '9' to Color(0xFF5555FF),
            'a' to Color(0xFF55FF55),
            'b' to Color(0xFF55FFFF),
            'c' to Color(0xFFFF5555),
            'd' to Color(0xFFFF55FF),
            'e' to Color(0xFFFFFF55),
            'f' to Color.White,
        )

    private val styleMap =
        mapOf(
            'l' to SpanStyle(fontWeight = FontWeight.Bold),
            'o' to SpanStyle(fontStyle = FontStyle.Italic),
            'n' to SpanStyle(textDecoration = TextDecoration.Underline),
            'm' to SpanStyle(textDecoration = TextDecoration.LineThrough),
        )

    private const val OBFUSCATION_CHAR = 'k'
    private const val RESET_CHAR = 'r'

    private val glyphs = (('A'..'Z') + ('a'..'z') + ('0'..'9')).toList()

    fun AnnotatedString.Builder.appendMinecraftText(
        message: String,
        obfuscationSeed: Int,
    ) {
        var currentColor = Color.Unspecified
        var currentStyle = SpanStyle()
        var obfuscate = false
        var lastIndex = 0

        for (match in MinecraftColorCodeRegex.findAll(message)) {
            val idx = match.range.first
            if (idx > lastIndex) {
                withStyle(currentStyle.copy(color = currentColor)) {
                    val segment = message.substring(lastIndex, idx)
                    append(obfuscateIfNeeded(segment, obfuscate, obfuscationSeed))
                }
            }

            val value = match.value
            if (value.startsWith("§#")) {
                currentColor = parseMinecraftHexColor(value.removePrefix("§#"))
                currentStyle = SpanStyle()
                obfuscate = false
            } else {
                val code = match.value[1].lowercaseChar()
                when (code) {
                    in colorMap -> {
                        currentColor = colorMap.getValue(code)
                        // color resets all styles
                        currentStyle = SpanStyle()
                        obfuscate = false
                    }

                    in styleMap -> {
                        // merge new style to allow stacking
                        currentStyle = currentStyle.merge(styleMap.getValue(code))
                    }

                    OBFUSCATION_CHAR -> {
                        obfuscate = !obfuscate
                    }

                    RESET_CHAR -> {
                        // full reset
                        currentColor = Color.Unspecified
                        currentStyle = SpanStyle()
                        obfuscate = false
                    }
                }
            }
            lastIndex = match.range.last + 1
        }

        if (lastIndex < message.length) {
            withStyle(currentStyle.copy(color = currentColor)) {
                val tail = message.substring(lastIndex)
                append(obfuscateIfNeeded(tail, obfuscate, obfuscationSeed))
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun obfuscateIfNeeded(
        text: String,
        obfuscate: Boolean,
        seed: Int,
    ) = if (obfuscate) {
        val random = Random(seed)
        text.map { char ->
            if (char.isWhitespace()) char else glyphs[random.nextInt(glyphs.size)]
        }.joinToString("")
    } else {
        text
    }

    private fun parseMinecraftHexColor(hex: String) =
        when (hex.length) {
            RGB_HEX_LENGTH -> {
                runCatching {
                    val color = (ALPHA_MASK or hex.toUInt(16)).toInt()
                    Color(color)
                }.getOrElse {
                    logger.error { "Failed to parse 6-digit hex color: §#$hex" }
                    Color.Unspecified
                }
            }

            ARGB_HEX_LENGTH -> {
                runCatching {
                    Color(hex.toUInt(16).toInt())
                }.getOrElse {
                    logger.error { "Failed to parse 8-digit hex color: §#$hex" }
                    Color.Unspecified
                }
            }

            else -> {
                logger.error { "Invalid hex color code length: §#$hex (length ${hex.length})" }
                Color.Unspecified
            }
        }
}

internal fun String.toMinecraftAnnotatedString(obfuscationSeed: Int) =
    buildAnnotatedString {
        appendMinecraftText(this@toMinecraftAnnotatedString, obfuscationSeed)
    }

private val logger = KotlinLogging.logger {}
