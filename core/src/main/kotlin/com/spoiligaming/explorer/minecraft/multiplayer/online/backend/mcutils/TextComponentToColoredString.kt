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

import tech.aliorpse.mcutils.model.server.status.TextComponent
import tech.aliorpse.mcutils.model.server.status.TextStyle

internal fun TextComponent.toSectionString(): String {
    fun estimate(node: TextComponent): Int = node.text.length + node.extra.sumOf { estimate(it) }

    val out = StringBuilder(estimate(this) + DEFAULT_STRING_BUILDER_SLACK)

    data class Frame(
        val node: TextComponent,
        val inheritedColor: String,
    )

    val stack = ArrayDeque<Frame>(DEFAULT_STACK_CAPACITY)
    stack.add(Frame(this, ""))

    var activeColor = ""
    var activeStyleBits = 0

    while (stack.isNotEmpty()) {
        val frame = stack.removeLast()
        val node = frame.node
        val parentColor = frame.inheritedColor

        val nodeColor = node.color.ifEmpty { parentColor }
        val targetBits = StyleBits.encode(node.styles)

        val colorChanged = nodeColor != activeColor
        val styleRemoved = activeStyleBits and targetBits.inv() != 0
        val needReset = styleRemoved || (colorChanged && nodeColor.isEmpty() && activeColor.isNotEmpty())

        if (needReset) {
            out.append(RESET)
            activeColor = ""
            activeStyleBits = 0
        }

        if (nodeColor.isNotEmpty() && nodeColor != activeColor) {
            out.append(colorCodeFor(nodeColor))
            activeColor = nodeColor
        } else if (nodeColor.isEmpty()) {
            activeColor = ""
        }

        val newStyleBits = targetBits and activeStyleBits.inv()
        if (newStyleBits != 0) StyleBits.append(newStyleBits, out)
        activeStyleBits = targetBits

        if (node.text.isNotEmpty()) out.append(node.text)

        // push children in reverse order
        val children = node.extra
        for (i in children.size - 1 downTo 0) {
            stack.add(Frame(children[i], nodeColor))
        }
    }

    // trim trailing §r if added
    if (out.length >= RESET_LENGTH && out[out.length - RESET_LENGTH] == SECTION && out[out.length - 1] == RESET_CHAR) {
        out.setLength(out.length - RESET_LENGTH)
    }

    return out.toString()
}

private fun colorCodeFor(c: String): String {
    if (c.isEmpty()) return ""

    if (c[0] == HEX_PREFIX) {
        val len = c.length
        if (len != HEX_COLOR_LEN_SHORT && len != HEX_COLOR_LEN_LONG) return ""

        val sb = StringBuilder(len)
        sb.append(HEX_PREFIX)
        for (i in 1 until len) {
            val up =
                when (val ch = c[i]) {
                    in '0'..'9' -> ch
                    in 'a'..'f' -> (ch.code - 32).toChar() // to upper for hex
                    in 'A'..'F' -> ch
                    else -> return ""
                }
            sb.append(up)
        }
        val upHex = sb.toString() // like #RRGGBB or #RRGGBBAA
        reverseColorMap[upHex]?.let { return "$SECTION$it" }
        return "$SECTION$upHex" // §#RRGGBB[AA]
    }

    val up = c.uppercase()
    reverseColorMap[up]?.let { return "$SECTION$it" }
    return ""
}

private object StyleBits {
    const val BOLD = 1 shl 0
    const val ITALIC = 1 shl 1
    const val UNDERLINED = 1 shl 2
    const val STRIKETHROUGH = 1 shl 3
    const val OBFUSCATED = 1 shl 4

    fun encode(set: Set<TextStyle>): Int {
        var b = 0
        if (TextStyle.BOLD in set) b = b or BOLD
        if (TextStyle.ITALIC in set) b = b or ITALIC
        if (TextStyle.UNDERLINED in set) b = b or UNDERLINED
        if (TextStyle.STRIKETHROUGH in set) b = b or STRIKETHROUGH
        if (TextStyle.OBFUSCATED in set) b = b or OBFUSCATED
        return b
    }

    fun append(
        bits: Int,
        dest: StringBuilder,
    ) {
        if (bits and OBFUSCATED != 0) dest.append(SECTION).append('k')
        if (bits and STRIKETHROUGH != 0) dest.append(SECTION).append('m')
        if (bits and UNDERLINED != 0) dest.append(SECTION).append('n')
        if (bits and ITALIC != 0) dest.append(SECTION).append('o')
        if (bits and BOLD != 0) dest.append(SECTION).append('l')
    }
}

// mirror of originalColorMap from JavaServer.kt from the MCUtils dependency
private val originalColorMap =
    mapOf(
        '0' to "#000000",
        '1' to "#0000AA",
        '2' to "#00AA00",
        '3' to "#00AAAA",
        '4' to "#AA0000",
        '5' to "#AA00AA",
        '6' to "#FFAA00",
        '7' to "#AAAAAA",
        '8' to "#555555",
        '9' to "#5555FF",
        'a' to "#55FF55",
        'b' to "#55FFFF",
        'c' to "#FF5555",
        'd' to "#FF55FF",
        'e' to "#FFFF55",
        'f' to "#FFFFFF",
    )

private val reverseColorMap =
    originalColorMap.entries.associate { (k, v) -> v.uppercase() to k }

private const val DEFAULT_STRING_BUILDER_SLACK = 16
private const val DEFAULT_STACK_CAPACITY = 32

private const val HEX_COLOR_LEN_SHORT = 7
private const val HEX_COLOR_LEN_LONG = 9
private const val RESET_LENGTH = 2

private const val RESET = "§r"

private const val RESET_CHAR = RESET[1]
private const val SECTION = '§'
private const val HEX_PREFIX = '#'
