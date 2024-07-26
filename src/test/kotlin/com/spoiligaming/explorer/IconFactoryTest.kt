package com.spoiligaming.explorer

import androidx.compose.ui.graphics.ImageBitmap
import com.spoiligaming.explorer.ui.icons.IconFactory
import kotlin.reflect.full.memberProperties
import kotlin.test.Test
import kotlin.test.assertNotNull

class IconFactoryTest {
    @Test
    fun testAllIcons() =
        IconFactory::class
            .memberProperties
            .filter { it.returnType.classifier == ImageBitmap::class }
            .forEach { property ->
                assertNotNull(
                    property.get(IconFactory) as? ImageBitmap,
                    "${property.name} should not be null",
                )
            }
}
