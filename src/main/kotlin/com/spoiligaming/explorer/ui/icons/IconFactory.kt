package com.spoiligaming.explorer.ui.icons

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource

/**
 * Provides access to various image assets used throughout the application.
 *
 * The `IconFactory` object is responsible for loading and providing `ImageBitmap` instances
 * for different icons used in the user interface. Icons are categorized for easy access,
 * including general icons, navigation icons, settings category icons, dialog button icons,
 * file-related icons, and miscellaneous icons.
 *
 * Icons are loaded from the resources directory using the specified paths. The `loadIcon`
 * function is used internally to handle the loading process.
 */
object IconFactory {
    private fun loadIcon(resourcePath: String) = useResource(resourcePath) { loadImageBitmap(it) }

    // Unknown server texture
    val unknownServerIcon: ImageBitmap
        get() = loadIcon("icon/other/texture_unknown-server.png")

    // Navigation Icons
    val toolsIcon: ImageBitmap
        get() = loadIcon("icon/gray/solid/icon_tools.png")
    val goBackIcon: ImageBitmap
        get() = loadIcon("icon/gray/solid/icon_go-back.png")

    // Dialog Button Icons
    val xIcon: ImageBitmap
        get() = loadIcon("icon/gray/regular/icon_x.png")
    val xIconRed: ImageBitmap
        get() = loadIcon("icon/other/solid/icon_x-red.png")
    val acceptIcon: ImageBitmap
        get() = loadIcon("icon/gray/regular/icon_accept.png")
    val acceptIconGreen: ImageBitmap
        get() = loadIcon("icon/other/solid/icon_accept-green.png")

    // File-related Icons
    val copyIcon: ImageBitmap
        get() = loadIcon("icon/gray/solid/icon_copy.png")
    val copyIconRegular: ImageBitmap
        get() = loadIcon("icon/white/regular/icon_copy.png")
    val pasteIcon: ImageBitmap
        get() = loadIcon("icon/white/regular/icon_paste.png")

    // Miscellaneous Icons
    val deleteIconWhite: ImageBitmap
        get() = loadIcon("icon/white/solid/icon_delete.png")
    val editIcon: ImageBitmap
        get() = loadIcon("icon/white/regular/icon_edit.png")
    val eraserIcon: ImageBitmap
        get() = loadIcon("icon/white/solid/icon_eraser.png")
    val arrowRightIcon: ImageBitmap
        get() = loadIcon("icon/white/solid/icon_arrow-up-right-from-square.png")
}
