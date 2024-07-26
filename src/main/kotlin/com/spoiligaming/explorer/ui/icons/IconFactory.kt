package com.spoiligaming.explorer.ui.icons

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource

object IconFactory {
    private fun loadIcon(resourcePath: String) = useResource(resourcePath) { loadImageBitmap(it) }

    val unknownServerIcon: ImageBitmap
        get() = loadIcon("texture_unknown-server.png")

    val discordLogoIcon: ImageBitmap
        get() = loadIcon("icon_discord.png")

    val upArrowIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_up-arrow.png")

    val downArrowIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_down-arrow.png")

    val dropdownMenuChavronIcon: ImageBitmap
        get() = loadIcon("icon_dropdown-menu-down-arrow.png")

    val toolsIcon: ImageBitmap
        get() = loadIcon("icon_settings.png")

    val goBackIcon: ImageBitmap
        get() = loadIcon("icon_go-back.png")

    val generalSettingsIcon: ImageBitmap
        get() = loadIcon("icon_general-settings.png")

    val themeSettingsIcon: ImageBitmap
        get() = loadIcon("icon_theme-settings.png")

    val resetServerIconIcon: ImageBitmap
        get() = loadIcon("icon_paper-reset.png")

    val deleteIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_trash-can.png")

    val deleteIconWhiteIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_trash-can.png")

    val copyIcon: ImageBitmap
        get() = loadIcon("icon_copy.png")

    val setCustomServerIconIcon: ImageBitmap
        get() = loadIcon("icon_encode-paper.png")

    val keyIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_key.png")

    val editPaperIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_edit-paper.png")

    val editIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_edit.png")

    val eraserIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_eraser.png")

    val informationFileIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_information-file.png")

    val bookmarkIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_bookmark.png")

    val acceptIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_accept.png")

    val acceptGreenIcon: ImageBitmap
        get() = loadIcon("icon_accept-green.png")

    val xIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_x.png")

    val xRedIcon: ImageBitmap
        get() = loadIcon("icon/icon_x-red.png")

    val copyRegularIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_regular-copy.png")

    val arrowRightOutIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_arrow-up-right-from-square.png")

    val chevronRightIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_chevron-right.png")

    val scissorIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_scissor.png")

    val pasteIcon: ImageBitmap
        get() = loadIcon("icon/white/icon_paste.png")

    val gearsIcon: ImageBitmap
        get() = loadIcon("icon/gray/icon_gears.png")

    /*
    private fun loadIcon(resourcePath: String): ImageBitmap =
        useResource(resourcePath) { loadImageBitmap(it) }

    init {
        val iconPaths = getIconPaths()
        val iconProperties = this::class.memberProperties
            .filterIsInstance<KMutableProperty<ImageBitmap>>()

        iconPaths.forEach { (iconName, resourcePath) ->
            val propertyName = convertToPropertyName(iconName)
            val property = iconProperties.find { it.name == propertyName }
            property?.let {
                it.isAccessible = true
                it.setter.call(this, loadIcon(resourcePath))
            }
        }
    }

    private fun getIconPaths(): Map<String, String> {
        val resourceDirectories = listOf("", "icons", "icons/gray", "icons/white")
        val iconPaths = mutableMapOf<String, String>()

        for (dir in resourceDirectories) {
            val resources = this::class.java.classLoader.getResources(dir).toList()
            for (resource in resources) {
                val file = File(resource.toURI())
                file.listFiles()?.forEach { iconFile ->
                    if (iconFile.isFile && iconFile.name.endsWith(".png")) {
                        val iconName = iconFile.nameWithoutExtension
                        val resourcePath = if (dir.isEmpty()) iconFile.name else "$dir/${iconFile.name}"
                        iconPaths[iconName] = resourcePath
                    }
                }
            }
        }
        return iconPaths
    }

    private fun convertToPropertyName(iconName: String): String {
        return iconName.split("_").joinToString("") { it.capitalize() } + "Icon"
    }

    val unknownServerIcon: ImageBitmap? = null
    val discordLogoIcon: ImageBitmap? = null
    val upArrowIcon: ImageBitmap? = null
    val downArrowIcon: ImageBitmap? = null
    val dropdownMenuChavronIcon: ImageBitmap? = null
    val toolsIcon: ImageBitmap? = null
    val goBackIcon: ImageBitmap? = null
    val generalSettingsIcon: ImageBitmap? = null
    val themeSettingsIcon: ImageBitmap? = null
    val resetServerIconIcon: ImageBitmap? = null
    val deleteIcon: ImageBitmap? = null
    val deleteIconWhiteIcon: ImageBitmap? = null
    val copyIcon: ImageBitmap? = null
    val setCustomServerIconIcon: ImageBitmap? = null
    val keyIcon: ImageBitmap? = null
    val editPaperIcon: ImageBitmap? = null
    val editIcon: ImageBitmap? = null
    val eraserIcon: ImageBitmap? = null
    val informationFileIcon: ImageBitmap? = null
    val bookmarkIcon: ImageBitmap? = null
    val acceptIcon: ImageBitmap? = null
    val acceptGreenIcon: ImageBitmap? = null
    val xIcon: ImageBitmap? = null
    val xRedIcon: ImageBitmap? = null
    val copyRegularIcon: ImageBitmap? = null
    val arrowRightOutIcon: ImageBitmap? = null
    val chevronRightIcon: ImageBitmap? = null
    val scissorIcon: ImageBitmap? = null
    val pasteIcon: ImageBitmap? = null
     */
}
