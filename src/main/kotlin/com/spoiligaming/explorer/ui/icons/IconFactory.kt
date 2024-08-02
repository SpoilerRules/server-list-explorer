package com.spoiligaming.explorer.ui.icons

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource

object IconFactory {
    private fun loadIcon(resourcePath: String) = useResource(resourcePath) { loadImageBitmap(it) }

    val unknownServerIcon: ImageBitmap
        get() = loadIcon("icon/other/texture_unknown-server.png")

    val upArrowIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_up-arrow.png")

    val downArrowIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_down-arrow.png")

    val toolsIcon: ImageBitmap
        get() = loadIcon("icon_settings.png")

    val goBackIcon: ImageBitmap
        get() = loadIcon("icon_go-back.png")

    val generalSettingsIcon: ImageBitmap
        get() = loadIcon("icon_general-settings.png")

    val themeSettingsIcon: ImageBitmap
        get() = loadIcon("icon_theme-settings.png")

    val deleteIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_trash-can.png")

    val deleteIconWhiteIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_trash-can.png")

    val copyIcon: ImageBitmap
        get() = loadIcon("icon_copy.png")

    val keyIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_key.png")

    val editPaperIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_edit-paper.png")

    val editIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_edit.png")

    val eraserIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_eraser.png")

    val acceptIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_accept.png")

    val acceptGreenIcon: ImageBitmap
        get() = loadIcon("icon/other/solid/icon_accept-green.png")

    val xIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_x.png")

    val xRedIcon: ImageBitmap
        get() = loadIcon("icon/other/solid/icon_x-red.png")

    val copyRegularIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_regular-copy.png")

    val arrowRightOutIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_arrow-up-right-from-square.png")

    val scissorIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_scissor.png")

    val pasteIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_paste.png")

    val gearsIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_gears.png")
}
