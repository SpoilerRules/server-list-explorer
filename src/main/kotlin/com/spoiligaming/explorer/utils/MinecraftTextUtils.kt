package com.spoiligaming.explorer.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

object MinecraftTextUtils {
    private val minecraftColorEnumMap =
        mapOf(
            '0' to Color.Black,
            '1' to Color(0xFF0000AA),
            '2' to Color(0xFF00AA00),
            '3' to Color(0xFF00AAAA),
            '4' to Color(0xFFAA0000),
            '5' to Color(0xFFAA00AA),
            '6' to Color(0xFFAA5500),
            '7' to Color.Gray,
            '8' to Color(0xFF555555),
            '9' to Color(0xFF5555FF),
            'a' to Color(0xFF55FF55),
            'b' to Color(0xFF55FFFF),
            'c' to Color(0xFFFF5555),
            'd' to Color(0xFFFF55FF),
            'e' to Color(0xFFFFFF55),
            'f' to Color.White,
        )

    private val minecraftStyleEnumMap =
        mapOf(
            'l' to SpanStyle(fontWeight = FontWeight.Bold),
            'o' to SpanStyle(fontStyle = FontStyle.Italic),
            'n' to SpanStyle(textDecoration = TextDecoration.Underline),
            'm' to SpanStyle(textDecoration = TextDecoration.LineThrough),
            'k' to SpanStyle(),
        )

    private val minecraftRegex = Regex("§[0-9a-fk-or]")

    fun parseMinecraftMOTD(message: String): AnnotatedString {
        var currentColor = Color.Unspecified
        var currentStyle = SpanStyle()
        var isObfuscated = false

        return AnnotatedString.Builder().apply {
            var lastIndex = 0
            minecraftRegex.findAll(message).forEach { match ->
                val start = match.range.first
                if (start > lastIndex) {
                    val textToAdd =
                        if (isObfuscated) {
                            // if we are in the obfuscated section, replace each character with '?', but not spaces, tabs, or newlines
                            message.substring(lastIndex, start).map { char ->
                                if (char.isWhitespace() || char == '\n' || char == '\t') char else '?'
                            }.joinToString("")
                        } else {
                            message.substring(lastIndex, start)
                        }

                    withStyle(style = currentStyle.copy(color = currentColor)) {
                        append(textToAdd)
                    }
                }

                val code = match.value[1] // skip '§' and get the code
                when {
                    minecraftColorEnumMap.containsKey(code) -> {
                        currentColor = minecraftColorEnumMap[code] ?: Color.Unspecified
                    }

                    minecraftStyleEnumMap.containsKey(code) -> {
                        currentStyle = minecraftStyleEnumMap[code] ?: SpanStyle()
                    }

                    code == 'k' -> {
                        isObfuscated = !isObfuscated
                    }

                    else -> {
                        currentColor = Color.Unspecified
                        currentStyle = SpanStyle()
                    }
                }

                lastIndex = match.range.last + 1
            }

            if (lastIndex < message.length) {
                val finalText =
                    if (isObfuscated) {
                        message.substring(lastIndex).map { char ->
                            if (char.isWhitespace() || char == '\n' || char == '\t') char else '?'
                        }.joinToString("")
                    } else {
                        message.substring(lastIndex)
                    }

                withStyle(style = currentStyle.copy(color = currentColor)) {
                    append(finalText)
                }
            }
        }.toAnnotatedString()
    }
}
