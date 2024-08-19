package com.spoiligaming.explorer

import androidx.compose.ui.text.font.FontFamily
import com.spoiligaming.explorer.ui.fonts.FontFactory
import kotlin.reflect.full.memberProperties
import kotlin.test.Test
import kotlin.test.assertNotNull

class FontFactoryTest {
    @Test
    fun testAllFonts() =
        FontFactory::class
            .memberProperties
            .filter { it.returnType.classifier == FontFamily::class }
            .forEach { property ->
                assertNotNull(
                    property.get(FontFactory) as? FontFamily,
                    "${property.name} should not be null",
                )
            }
}

