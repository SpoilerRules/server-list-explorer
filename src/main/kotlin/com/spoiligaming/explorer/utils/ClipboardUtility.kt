package com.spoiligaming.explorer.utils

import com.spoiligaming.logging.Logger
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.Base64
import javax.imageio.ImageIO

object ClipboardUtility {
    private val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard

    fun copy(content: String?) =
        content?.let {
            clipboard.setContents(StringSelection(it), null)
            Logger.printSuccess("Copied text content to clipboard: \"$it\"")
        }

    fun copyIconAsImage(base64Icon: String?) =
        base64Icon?.let {
            clipboard.setContents(
                BufferedImageTransferable(
                    ImageIO.read(ByteArrayInputStream(Base64.getDecoder().decode(it))),
                ),
                null,
            )
            Logger.printSuccess("Icon image copied to clipboard.")
        }

    fun copyServerInformationAsToml(
        serverName: String,
        serverAddress: String,
        serverIcon: String?,
        hidden: Boolean,
        acceptedTextures: Int?,
        serverPosition: Int,
    ) {
        val toml =
            """
        [server.info]
        name = "$serverName"
        address = "$serverAddress"
        icon = ${serverIcon?.let { "\"$it\"" } ?: "none"}

        [server.attributes]
        hidden = $hidden
        accepted_textures = ${acceptedTextures ?: "unknown"}
        position = $serverPosition
    """
                .trimIndent()

        clipboard.setContents(StringSelection(toml), null)
        Logger.printSuccess("Server information copied as TOML to clipboard.")
    }

    private class BufferedImageTransferable(private val image: BufferedImage) : Transferable {
        override fun getTransferData(flavor: DataFlavor): Any =
            if (flavor.equals(DataFlavor.imageFlavor)) {
                image
            } else {
                throw UnsupportedFlavorException(flavor)
            }

        override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)

        override fun isDataFlavorSupported(flavor: DataFlavor): Boolean =
            flavor.equals(
                DataFlavor.imageFlavor,
            )
    }
}
