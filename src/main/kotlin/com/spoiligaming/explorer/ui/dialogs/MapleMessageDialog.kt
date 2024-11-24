package com.spoiligaming.explorer.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.SettingsViewModel
import com.spoiligaming.explorer.ui.dialogs.dialog.MapleDialogBase
import com.spoiligaming.explorer.ui.extensions.onHover
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.icons.IconFactory
import com.spoiligaming.explorer.ui.widgets.MergedText
import com.spoiligaming.explorer.utils.MinecraftTextUtils

@Composable
fun MapleConfirmationDialog(
    title: String,
    description: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) = MapleMessageDialog(false, title, description, onAccept, onDismiss)

@Composable
fun MapleInformationDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
) = MapleMessageDialog(true, title, description, {}, onDismiss)

@Composable
private fun MapleMessageDialog(
    isInformationOnly: Boolean,
    title: String,
    description: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) = MapleDialogBase(
    0,
    true,
    onDismiss,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                color = MapleColorPalette.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaMedium,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                    ),
            )
            Text(
                text = description,
                color = MapleColorPalette.fadedText,
                maxLines = 7,
                overflow = TextOverflow.Ellipsis,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaMedium,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                    ),
                textAlign = TextAlign.Center,
            )
        }

        DialogActionButtons(isInformationOnly, onAccept, onDismiss)
    }
}

@Composable
fun MapleMOTDDialog(
    serverName: String,
    serverAddress: String,
    motd: String,
    onDismiss: () -> Unit,
) = MapleDialogBase(
    0,
    true,
    onDismiss,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.width(IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
                MergedText(
                    serverName,
                    MapleColorPalette.fadedText,
                    FontFactory.comfortaaMedium,
                    FontWeight.Bold,
                    " ($serverAddress)",
                    MapleColorPalette.fadedText,
                    FontFactory.comfortaaRegular,
                    FontWeight.Normal,
                )
            }
            Surface(
                color = MapleColorPalette.control,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 2.dp,
            ) {
                Text(
                    text = MinecraftTextUtils.parseMinecraftMOTD(motd),
                    style =
                        TextStyle(
                            fontFamily = FontFactory.comfortaaMedium,
                            fontWeight = FontWeight.Normal,
                            fontSize = 24.sp,
                        ),
                    maxLines = 2,
                    lineHeight = 36.sp,
                    modifier =
                        Modifier
                            .padding(16.dp),
                )
            }
        }
        DialogActionButtons(true, {}, onDismiss)
    }
}

@Composable
private fun DialogActionButtons(
    isInformationOnly: Boolean,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) = Box(modifier = Modifier.padding(bottom = 66.dp)) {
    if (SettingsViewModel.experimentalIconifiedDialogOptions) {
        ExperimentalButtons(
            dismissOnly = isInformationOnly,
            onAccept = onAccept,
            onDismiss = onDismiss,
        )
    } else {
        Buttons(
            dismissOnly = isInformationOnly,
            onAccept = onAccept,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun Buttons(
    dismissOnly: Boolean,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) = Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
    ActionButton(
        text = if (dismissOnly) "OK" else "Yes",
        onClick = if (dismissOnly) onDismiss else onAccept,
        isHoveredState = remember { mutableStateOf(false) },
        showIcon = false,
    )
    if (!dismissOnly) {
        ActionButton(
            text = "No",
            onClick = onDismiss,
            isHoveredState = remember { mutableStateOf(false) },
            showIcon = false,
        )
    }
}

@Composable
private fun ExperimentalButtons(
    dismissOnly: Boolean,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) = Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
    ActionButton(
        onClick = if (dismissOnly) onDismiss else onAccept,
        isHoveredState = remember { mutableStateOf(false) },
        showIcon = true,
        iconType = ButtonType.ACCEPT,
    )
    if (!dismissOnly) {
        ActionButton(
            onClick = onDismiss,
            isHoveredState = remember { mutableStateOf(false) },
            showIcon = true,
            iconType = ButtonType.DISMISS,
        )
    }
}

@Composable
private fun ActionButton(
    text: String? = null,
    onClick: () -> Unit,
    isHoveredState: MutableState<Boolean>,
    showIcon: Boolean,
    iconType: ButtonType? = null,
) = Button(
    onClick = onClick,
    modifier =
        Modifier.width(75.dp)
            .height(40.dp)
            .onHover { isHoveredState.value = it }
            .pointerHoverIcon(PointerIcon.Hand),
    shape = RoundedCornerShape(12.dp),
    colors =
        ButtonDefaults.buttonColors(
            containerColor = MapleColorPalette.menu,
            contentColor = MapleColorPalette.fadedText,
        ),
) {
    Box(contentAlignment = Alignment.Center) {
        if (showIcon && iconType != null) {
            Image(
                bitmap =
                    when {
                        isHoveredState.value && iconType == ButtonType.ACCEPT ->
                            IconFactory.acceptIconGreen
                        !isHoveredState.value && iconType == ButtonType.ACCEPT ->
                            IconFactory.acceptIcon
                        isHoveredState.value && iconType == ButtonType.DISMISS ->
                            IconFactory.xIconRed
                        else -> IconFactory.xIcon
                    },
                contentDescription = "Icon for ${iconType.name} Button",
                modifier = Modifier.size(26.dp),
                contentScale = ContentScale.Fit,
            )
        } else if (text != null) {
            Text(
                text = text,
                color = MapleColorPalette.text,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaLight,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                    ),
            )
        }
    }
}

enum class ButtonType {
    ACCEPT,
    DISMISS,
}
