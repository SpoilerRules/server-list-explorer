package com.spoiligaming.explorer.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.utils.ColorPaletteUtility

object MapleColorPalette {
    private val config = ConfigurationHandler.getInstance().themeSettings

    val defaultTertiary = Color(0xFF282828)
    val defaultQuaternary = Color(0xFF343434)
    val defaultMenu = Color(0xFF404040)
    val defaultSecondaryControl = Color(0xFF3F3F3F)
    val defaultControl = Color(0xFF4C4C4C)
    val defaultSecondary = Color(0xFF727272)
    val defaultAccent = Color(0xFFE85D9B)

    private val _accent =
        mutableStateOf(ColorPaletteUtility.convertStringToColor(config.accentColor))
    var accent: Color
        get() = _accent.value
        set(value) {
            _accent.value = value
        }

    private val _menu = mutableStateOf(ColorPaletteUtility.convertStringToColor(config.menuColor))
    var menu: Color
        get() = _menu.value
        set(value) {
            _menu.value = value
        }

    private val _control =
        mutableStateOf(ColorPaletteUtility.convertStringToColor(config.controlColor))
    var control: Color
        get() = _control.value
        set(value) {
            _control.value = value
        }

    private val _secondaryControl =
        mutableStateOf(ColorPaletteUtility.convertStringToColor(config.secondaryControlColor))
    var secondaryControl: Color
        get() = _secondaryControl.value
        set(value) {
            _secondaryControl.value = value
        }

    private val _secondary =
        mutableStateOf(ColorPaletteUtility.convertStringToColor(config.secondaryColor))
    var secondary: Color
        get() = _secondary.value
        set(value) {
            _secondary.value = value
        }

    private val _tertiary =
        mutableStateOf(ColorPaletteUtility.convertStringToColor(config.tertiaryColor))
    var tertiary: Color
        get() = _tertiary.value
        set(value) {
            _tertiary.value = value
        }

    private val _quaternary =
        mutableStateOf(ColorPaletteUtility.convertStringToColor(config.quaternaryColor))
    var quaternary: Color
        get() = _quaternary.value
        set(value) {
            _quaternary.value = value
        }

    val mapleUndetected = Color(0xFF00FF00) // not modifiable
    val mapleOutdated = Color(0xFFFE4501) // not modifiable
    val mapleWebsiteOutdated = Color(0xFFFAC800) // not modifiable
    val mapleDetected = Color.Red // not modifiable
    val text = Color.White // not modifiable
    val fadedText = Color(0xFFCCCCCC) // not modifiable
}
