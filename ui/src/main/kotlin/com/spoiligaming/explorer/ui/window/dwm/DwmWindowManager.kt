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

package com.spoiligaming.explorer.ui.window.dwm

import androidx.compose.ui.graphics.Color
import com.spoiligaming.explorer.util.OSUtils
import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinNT.HRESULT
import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Window
import javax.swing.JFrame

@Suppress("FunctionName")
internal interface DwmApi : Library {
    companion object {
        val INSTANCE: DwmApi = Native.load("dwmapi", DwmApi::class.java)
    }

    fun DwmSetWindowAttribute(
        hWnd: HWND,
        dwAttribute: Int,
        pvAttribute: Pointer,
        cbAttribute: Int,
    ): HRESULT
}

internal object DwmWindowAttribute {
    const val USE_IMMERSIVE_DARK_MODE = 20 // DWMWA_USE_IMMERSIVE_DARK_MODE
    const val WINDOW_CORNER_PREFERENCE = 33 // DWMWA_WINDOW_CORNER_PREFERENCE
    const val BORDER_COLOR = 34 // DWMWA_BORDER_COLOR
    const val CAPTION_COLOR = 35 // DWMWA_CAPTION_COLOR
    const val SYSTEM_BACKDROP_TYPE = 38 // DWMWA_SYSTEMBACKDROP_TYPE
}

internal enum class BackdropType(
    val value: Int,
) {
    AUTO(0),
    NONE(1),
    MICA(2),
    DesktopAcrylic(3),
    MicaAlt(4),
}

internal enum class CornerPreference(
    val value: Int,
) {
    Default(0),
    DoNotRound(1),
    Round(2),
    RoundSmall(3),
}

internal class DwmWindowStyler private constructor(
    private val hWnd: HWND,
) {
    companion object {
        fun fromFrame(frame: JFrame): DwmWindowStyler {
            val awtWindow = frame.rootPane.topLevelAncestor as Window
            val ptr = Native.getWindowPointer(awtWindow)
            return DwmWindowStyler(HWND(ptr))
        }
    }

    fun setDarkMode(enabled: Boolean) = setIntAttr(DwmWindowAttribute.USE_IMMERSIVE_DARK_MODE, if (enabled) 1 else 0)

    fun setCaptionColor(color: Color) = setColorAttr(DwmWindowAttribute.CAPTION_COLOR, color)

    fun setBorderColor(color: Color) = setColorAttr(DwmWindowAttribute.BORDER_COLOR, color)

    fun setCornerPreference(pref: CornerPreference) =
        setIntAttr(DwmWindowAttribute.WINDOW_CORNER_PREFERENCE, pref.value)

    fun setCornerPreference(prefValue: Int) = setIntAttr(DwmWindowAttribute.WINDOW_CORNER_PREFERENCE, prefValue)

    fun setBackdropType(type: BackdropType): Boolean {
        if (!OSUtils.supportsDwmBackdropTypes) {
            logger.debug { "SYSTEM_BACKDROP_TYPE not supported on this OS" }
            return false
        }
        return setIntAttr(DwmWindowAttribute.SYSTEM_BACKDROP_TYPE, type.value)
    }

    fun applyMica(
        useAlt: Boolean = true,
        darkMode: Boolean? = null,
        tint: Color? = null,
    ): Boolean {
        val ok = setBackdropType(if (useAlt) BackdropType.MicaAlt else BackdropType.MICA)
        if (!ok) return false
        darkMode?.let { setDarkMode(it) }
        tint?.let {
            // keep these best-effort; do not fail whole call
            setCaptionColor(it)
            setBorderColor(it)
        }
        return true
    }

    fun applyAcrylic(tint: Color? = null): Boolean {
        val ok = setBackdropType(BackdropType.DesktopAcrylic)
        if (!ok) return false
        tint?.let {
            setCaptionColor(it)
            setBorderColor(it)
        }
        return true
    }

    private fun setIntAttr(
        attribute: Int,
        value: Int,
    ) = runCatching {
        val mem = Memory(Int.SIZE_BYTES.toLong()).apply { setInt(0, value) }
        DwmApi.INSTANCE.DwmSetWindowAttribute(hWnd, attribute, mem, Int.SIZE_BYTES)
    }.fold(
        onSuccess = { hr ->
            if (hr.succeeded()) true else hr.also { logHResult(attribute, it) }.let { false }
        },
        onFailure = { e ->
            logger.error(e) { "DwmSetWindowAttribute($attribute) threw" }
            false
        },
    )

    private fun setColorAttr(
        attribute: Int,
        color: Color,
    ): Boolean {
        val colorRef = color.toWindowsColorRef()
        val mem = Memory(Int.SIZE_BYTES.toLong()).apply { setInt(0, colorRef) }
        val hr = DwmApi.INSTANCE.DwmSetWindowAttribute(hWnd, attribute, mem, Int.SIZE_BYTES)
        val hex =
            "#%02X%02X%02X".format(
                (color.red * 255).toInt() and 0xFF,
                (color.green * 255).toInt() and 0xFF,
                (color.blue * 255).toInt() and 0xFF,
            )
        return if (hr.succeeded()) {
            logger.debug { "DWM attribute $attribute set to color $hex" }
            true
        } else {
            logHResult(attribute, hr)
            false
        }
    }

    private fun logHResult(
        attribute: Int,
        hr: HRESULT,
    ) {
        val v = hr.toLong() and 0xFFFFFFFFL
        logger.warn { "DwmSetWindowAttribute($attribute) failed: HRESULT=0x${v.toString(16)}" }
    }

    private fun HRESULT.succeeded() = this == WinNT.S_OK

    private fun Color.toWindowsColorRef(): Int {
        val r = (red * 255).toInt() and 0xFF
        val g = (green * 255).toInt() and 0xFF
        val b = (blue * 255).toInt() and 0xFF
        // COLORREF is 0x00BBGGRR
        return (b shl 16) or (g shl 8) or r
    }
}

private val logger = KotlinLogging.logger {}

internal fun JFrame.dwmStyler() = DwmWindowStyler.fromFrame(this)
