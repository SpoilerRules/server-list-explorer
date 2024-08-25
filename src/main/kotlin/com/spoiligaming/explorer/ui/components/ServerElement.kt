package com.spoiligaming.explorer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuState
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.server.ContemporaryServerEntryListData
import com.spoiligaming.explorer.server.ServerFileHandler
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.dialogs.ValueReplacementType
import com.spoiligaming.explorer.ui.extensions.onHover
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.icons.IconFactory
import com.spoiligaming.explorer.ui.presentation.MapleContextMenuRepresentation
import com.spoiligaming.explorer.ui.state.DialogController
import com.spoiligaming.explorer.ui.widgets.SelectableInteractiveText
import com.spoiligaming.explorer.ui.widgets.ServerIconImage
import com.spoiligaming.explorer.utils.ClipboardUtility
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

@Composable
fun ServerElement(
    serverIcon: ImageBitmap?,
    serverIconRaw: String?,
    serverName: String,
    serverAddress: String,
    serverPositionInList: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    val contextMenuState = remember { ContextMenuState() }
    val contextMenuRepresentation = MapleContextMenuRepresentation(serverName, 0)

    var isHovered by remember { mutableStateOf(false) }
    val backgroundColor by
        animateColorAsState(
            targetValue =
                when {
                    isHovered || isSelected -> MapleColorPalette.control
                    else -> MapleColorPalette.tertiaryControl
                },
            animationSpec = tween(durationMillis = 100),
        )

    val interactionSource = remember { MutableInteractionSource() }

    CompositionLocalProvider(LocalContextMenuRepresentation provides contextMenuRepresentation) {
        ContextMenuArea(
            items = {
                listOf(
                    ContextMenuItem("Rename") {
                        DialogController.showValueReplacementDialog(
                            serverName = serverName,
                            serverAddress = serverAddress,
                            serverIcon = serverIcon,
                            serverIconRaw = serverIconRaw,
                            serverPositionInList = serverPositionInList,
                            type = ValueReplacementType.NAME,
                        ) { newName ->
                            ContemporaryServerEntryListData.updateServerName(
                                serverPositionInList,
                                newName,
                            )
                        }
                    },
                    ContextMenuItem("Modify Address") {
                        DialogController.showValueReplacementDialog(
                            serverName = serverName,
                            serverAddress = serverAddress,
                            serverIcon = serverIcon,
                            serverIconRaw = serverIconRaw,
                            serverPositionInList = serverPositionInList,
                            type = ValueReplacementType.ADDRESS,
                        ) { newAddress ->
                            ContemporaryServerEntryListData.updateServerAddress(
                                serverPositionInList,
                                newAddress,
                            )
                        }
                    },
                    ContextMenuItem("Change Icon") {
                        DialogController.showValueReplacementDialog(
                            serverName = serverName,
                            serverAddress = serverAddress,
                            serverIcon = serverIcon,
                            serverIconRaw = serverIconRaw,
                            serverPositionInList = serverPositionInList,
                            type = ValueReplacementType.ICON,
                        ) { newIcon ->
                            ContemporaryServerEntryListData.updateServerIcon(
                                serverPositionInList,
                                newIcon,
                            )
                        }
                    },
                    ContextMenuItem("Copy as Toml") {
                        ClipboardUtility.copyServerInformationAsToml(
                            serverName,
                            serverAddress,
                            serverIconRaw,
                            ServerFileHandler.isHidden(serverPositionInList),
                            ServerFileHandler.getAcceptedTexturesState(serverPositionInList),
                            serverPositionInList,
                        )
                    },
                    ContextMenuItem("Copy Name") { ClipboardUtility.copy(serverName) },
                    ContextMenuItem("Copy Address") { ClipboardUtility.copy(serverAddress) },
                    ContextMenuItem("Copy Icon as Image") {
                        ClipboardUtility.copyIconAsImage(serverIconRaw)
                    },
                    ContextMenuItem("Copy Icon as Base64") { ClipboardUtility.copy(serverIconRaw) },
                    // ContextMenuItem("Classic View") {  },
                    // ContextMenuItem("Move to Specified Index") {  },
                    ContextMenuItem("Erase Icon") {
                        ServerFileHandler.deleteServerIcon(serverPositionInList)
                    },
                    ContextMenuItem("Delete Entry") {
                        DialogController.showDeletionConfirmationDialog(
                            serverPositionInList,
                            serverName,
                        )
                        focusRequester.freeFocus()
                    },
                )
            },
            state = contextMenuState,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor, shape = RoundedCornerShape(12.dp))
                        .border(
                            width = 2.dp,
                            color =
                                if (isSelected) {
                                    MapleColorPalette.secondaryControl
                                } else {
                                    Color.Transparent
                                },
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable(
                            indication = null,
                            interactionSource = interactionSource,
                            onClick = {
                                if (isSelected) {
                                    ContemporaryServerEntryListData.selectedServer = null
                                } else {
                                    onClick()
                                }
                            },
                        )
                        .onHover { isHovered = it }
                        .focusRequester(focusRequester)
                        .focusable()
                        .onKeyEvent {
                            if (!isHovered &&
                                (
                                    !isSelected ||
                                        ContemporaryServerEntryListData.selectedServer == null ||
                                        ContemporaryServerEntryListData.selectedServer !=
                                        serverPositionInList
                                )
                            ) {
                                return@onKeyEvent false
                            }

                            when {
                                (hostOs == OS.MacOS && it.isMetaPressed) ||
                                    (hostOs != OS.MacOS && it.isCtrlPressed) -> {
                                    when (it.key) {
                                        Key.R -> {
                                            DialogController.showValueReplacementDialog(
                                                serverName = serverName,
                                                serverAddress = serverAddress,
                                                serverIcon = serverIcon,
                                                serverIconRaw = serverIconRaw,
                                                serverPositionInList = serverPositionInList,
                                                type = ValueReplacementType.NAME,
                                            ) { newName ->
                                                ContemporaryServerEntryListData
                                                    .updateServerName(
                                                        serverPositionInList,
                                                        newName,
                                                    )
                                            }
                                            true
                                        }
                                        Key.M -> {
                                            DialogController.showValueReplacementDialog(
                                                serverName = serverName,
                                                serverAddress = serverAddress,
                                                serverIcon = serverIcon,
                                                serverIconRaw = serverIconRaw,
                                                serverPositionInList = serverPositionInList,
                                                type = ValueReplacementType.ADDRESS,
                                            ) {
                                                    newAddress ->
                                                ContemporaryServerEntryListData
                                                    .updateServerAddress(
                                                        serverPositionInList,
                                                        newAddress,
                                                    )
                                            }
                                            true
                                        }
                                        Key.I -> {
                                            DialogController.showValueReplacementDialog(
                                                serverName = serverName,
                                                serverAddress = serverAddress,
                                                serverIcon = serverIcon,
                                                serverIconRaw = serverIconRaw,
                                                serverPositionInList = serverPositionInList,
                                                type = ValueReplacementType.ICON,
                                            ) { newIcon ->
                                                ContemporaryServerEntryListData
                                                    .updateServerIcon(
                                                        serverPositionInList,
                                                        newIcon,
                                                    )
                                            }
                                            true
                                        }
                                        Key.T -> {
                                            ClipboardUtility.copyServerInformationAsToml(
                                                serverName,
                                                serverAddress,
                                                serverIconRaw,
                                                ServerFileHandler.isHidden(
                                                    serverPositionInList,
                                                ),
                                                ServerFileHandler.getAcceptedTexturesState(
                                                    serverPositionInList,
                                                ),
                                                serverPositionInList,
                                            )
                                            true
                                        }
                                        Key.N -> {
                                            ClipboardUtility.copy(serverName)
                                            true
                                        }
                                        Key.C -> {
                                            ClipboardUtility.copy(serverAddress)
                                            true
                                        }
                                        else -> false
                                    }
                                }
                                it.key == Key.Delete -> {
                                    if (it.isCtrlPressed) {
                                        ServerFileHandler.deleteServerIcon(serverPositionInList)
                                    } else {
                                        DialogController.showDeletionConfirmationDialog(
                                            serverPositionInList,
                                            serverName,
                                        )
                                    }
                                    true
                                }
                                else -> false
                            }
                        }
                        .padding(10.dp),
            ) {
                if (isHovered && contextMenuState.status !is ContextMenuState.Status.Closed) {
                    isHovered = false
                }

                LaunchedEffect(isHovered, ContemporaryServerEntryListData.selectedServer) {
                    if (isHovered && ContemporaryServerEntryListData.selectedServer == null) {
                        focusRequester.requestFocus()
                    } else {
                        if (isSelected &&
                            ContemporaryServerEntryListData.selectedServer ==
                            serverPositionInList
                        ) {
                            focusRequester.requestFocus()
                        } else {
                            focusRequester.freeFocus()
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    var isIconHovered by remember { mutableStateOf(false) }
                    var isOverlayHovered by remember { mutableStateOf(false) }
                    var isOverlayPressed by remember { mutableStateOf(false) }

                    val overlayAlpha by
                        animateFloatAsState(
                            targetValue = if (isIconHovered) 0.5f else 0f,
                            animationSpec = tween(durationMillis = 200),
                        )

                    val copyIconAlpha by
                        animateFloatAsState(
                            targetValue = if (isIconHovered) 1f else 0f,
                            animationSpec = tween(durationMillis = 300),
                        )

                    val secondaryColorAlpha by
                        animateFloatAsState(
                            targetValue = if (isOverlayHovered) 0.3f else 0f,
                            animationSpec = tween(durationMillis = 200),
                        )

                    val iconSize by
                        animateDpAsState(
                            targetValue = if (isOverlayPressed) 20.dp else 22.dp,
                            animationSpec = tween(durationMillis = 50),
                        )

                    Box(
                        modifier =
                            Modifier
                                .size(48.dp, 48.dp)
                                .background(Color.Transparent, shape = RoundedCornerShape(4.dp))
                                .onHover { isIconHovered = it },
                    ) {
                        ServerIconImage(
                            if (ServerFileHandler.getServerIcon(serverPositionInList) !=
                                null
                            ) {
                                serverIcon
                            } else {
                                null
                            },
                            serverName,
                        )

                        Box(
                            modifier =
                                Modifier.fillMaxSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        MapleColorPalette.quaternary.copy(
                                            alpha = overlayAlpha,
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(28.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .pointerHoverIcon(PointerIcon.Hand)
                                        .background(
                                            color =
                                                if (isOverlayHovered) {
                                                    MapleColorPalette.fadedText.copy(
                                                        alpha = secondaryColorAlpha,
                                                    )
                                                } else {
                                                    Color.Transparent
                                                },
                                            shape = RoundedCornerShape(4.dp),
                                        )
                                        .onHover { isOverlayHovered = it }
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onPress = {
                                                    isOverlayPressed = true
                                                    tryAwaitRelease()
                                                    isOverlayPressed = false
                                                    ClipboardUtility.copyIconAsImage(
                                                        serverIconRaw,
                                                    )
                                                },
                                            )
                                        },
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    bitmap = IconFactory.copyIcon,
                                    contentDescription =
                                        "Copy Icon for $serverName",
                                    modifier =
                                        Modifier.size(iconSize)
                                            .background(Color.Transparent)
                                            .alpha(copyIconAlpha),
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        SelectableInteractiveText(
                            text = serverName,
                            textColor = MapleColorPalette.fadedText,
                            fontFamily = FontFactory.comfortaaBold,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            onLongClick = {
                                DialogController.showValueReplacementDialog(
                                    serverName = serverName,
                                    serverAddress = serverAddress,
                                    serverIcon = serverIcon,
                                    serverIconRaw = serverIconRaw,
                                    serverPositionInList = serverPositionInList,
                                    type = ValueReplacementType.NAME,
                                ) { newName ->
                                    ContemporaryServerEntryListData.updateServerName(
                                        serverPositionInList,
                                        newName,
                                    )
                                }
                            },
                        )
                        SelectableInteractiveText(
                            text = serverAddress,
                            textColor = MapleColorPalette.fadedText,
                            fontFamily = FontFactory.comfortaaBold,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            onLongClick = {
                                DialogController.showValueReplacementDialog(
                                    serverName = serverName,
                                    serverAddress = serverAddress,
                                    serverIcon = serverIcon,
                                    serverIconRaw = serverIconRaw,
                                    serverPositionInList = serverPositionInList,
                                    type = ValueReplacementType.ADDRESS,
                                ) { newAddress ->
                                    ContemporaryServerEntryListData.updateServerAddress(
                                        serverPositionInList,
                                        newAddress,
                                    )
                                }
                            },
                        )
                    }

                    val (initialWidth, initialHeight) = 0.dp to 0.dp
                    val (expandedWidth, expandedHeight) = 104.dp to 48.dp

                    val shouldExpand = isHovered || isSelected
                    val animationSpec: AnimationSpec<Any> = tween(durationMillis = 200)

                    val animatedWidth by
                        animateDpAsState(
                            targetValue = if (shouldExpand) expandedWidth else initialWidth,
                            animationSpec = animationSpec as AnimationSpec<Dp>,
                        )
                    val animatedHeight by
                        animateDpAsState(
                            targetValue = if (shouldExpand) expandedHeight else initialHeight,
                            animationSpec = animationSpec as AnimationSpec<Dp>,
                        )
                    val animatedAlpha by
                        animateFloatAsState(
                            targetValue = if (shouldExpand) 1f else 0f,
                            animationSpec = animationSpec as AnimationSpec<Float>,
                        )

                    val createClickableIcon:
                        @Composable
                        (ImageBitmap, String, () -> Unit) -> Unit =
                        { iconBitmap, iconDescription, onClick ->
                            ClickableIcon(
                                boxSize = 28.dp,
                                boxCornerRadius = 4.dp,
                                hoverColor = MapleColorPalette.secondary,
                                unhoverColor = Color.Transparent,
                                hoverColorAlpha = 1f,
                                hoverColorAnimationSpec = tween(durationMillis = 200),
                                iconBitmap = iconBitmap,
                                iconDescription = iconDescription,
                                iconHoverSize = 22.dp,
                                iconHoverSizeAnimationSpec = tween(durationMillis = 100),
                                null,
                                null,
                                iconDefaultSize = 18.dp,
                                iconUnpressSize = 20.dp,
                                iconPressSizeAnimationSpec = tween(durationMillis = 5),
                                onClick = onClick,
                            )
                        }
                    Box(
                        modifier =
                            Modifier.size(
                                width = animatedWidth,
                                height = animatedHeight,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            modifier =
                                Modifier.width(animatedWidth)
                                    .height(animatedHeight),
                            shape = RoundedCornerShape(12.dp),
                            color = MapleColorPalette.control,
                            shadowElevation = 4.dp,
                        ) {
                            if (shouldExpand) {
                                Row(
                                    modifier =
                                        Modifier.fillMaxSize()
                                            .alpha(animatedAlpha),
                                    verticalAlignment =
                                        Alignment.CenterVertically,
                                    horizontalArrangement =
                                        Arrangement.Center,
                                ) {
                                    createClickableIcon(
                                        IconFactory.deleteIcon,
                                        "Delete Icon for Deleting Server Item",
                                    ) {
                                        DialogController
                                            .showDeletionConfirmationDialog(
                                                serverPositionInList,
                                                serverName,
                                            )
                                    }
                                    createClickableIcon(
                                        IconFactory.chevronUp,
                                        "Up Arrow Icon for Moving Server Up",
                                    ) {
                                        onMoveUp(serverPositionInList)
                                    }
                                    createClickableIcon(
                                        IconFactory.chevronDown,
                                        "Down Arrow Icon for Moving Server Down",
                                    ) {
                                        onMoveDown(serverPositionInList)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClickableIcon(
    boxSize: Dp,
    boxCornerRadius: Dp,
    hoverColor: Color,
    unhoverColor: Color = Color.Transparent,
    hoverColorAlpha: Float,
    hoverColorAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 200),
    iconBitmap: ImageBitmap,
    iconDescription: String,
    iconHoverSize: Dp? = null,
    iconHoverSizeAnimationSpec: AnimationSpec<Dp>?,
    iconHoverAlpha: Float? = null,
    iconHoverAlphaAnimationSpec: AnimationSpec<Float>?,
    iconDefaultSize: Dp,
    iconUnpressSize: Dp? = null,
    iconPressSizeAnimationSpec: AnimationSpec<Dp>?,
    onClick: () -> Unit,
) {
    var isHovered by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val iconAlpha by
        animateFloatAsState(
            targetValue = if (isHovered) iconHoverAlpha ?: 1f else 0f,
            animationSpec = iconHoverAlphaAnimationSpec ?: tween(durationMillis = 300),
        )

    val boxAlpha by
        animateFloatAsState(
            targetValue = if (isHovered) hoverColorAlpha else 0f,
            animationSpec = hoverColorAnimationSpec,
        )

    val iconSize by
        animateDpAsState(
            targetValue =
                when {
                    isPressed -> iconDefaultSize
                    isHovered && iconHoverSize != null -> iconHoverSize
                    else -> iconUnpressSize ?: iconDefaultSize
                },
            animationSpec =
                when {
                    isPressed -> iconPressSizeAnimationSpec ?: tween(durationMillis = 50)
                    isHovered && iconHoverSizeAnimationSpec != null -> iconHoverSizeAnimationSpec
                    else -> tween(durationMillis = 50)
                },
        )

    Box(
        modifier =
            Modifier.size(boxSize)
                .pointerHoverIcon(PointerIcon.Hand)
                .background(
                    color = if (isHovered) hoverColor.copy(alpha = boxAlpha) else unhoverColor,
                    shape = RoundedCornerShape(boxCornerRadius),
                )
                .onHover { isHovered = it }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                            onClick()
                        },
                    )
                },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = iconBitmap,
            contentDescription = iconDescription,
            modifier =
                Modifier.size(
                    if (iconUnpressSize != null && iconPressSizeAnimationSpec != null) {
                        iconSize
                    } else {
                        iconDefaultSize
                    },
                )
                    .background(color = Color.Transparent)
                    .alpha(
                        if (iconHoverAlpha != null && iconHoverAlphaAnimationSpec != null) {
                            iconAlpha
                        } else {
                            1f
                        },
                    ),
            contentScale = ContentScale.Fit,
        )
    }
}
