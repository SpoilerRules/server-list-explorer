package com.spoiligaming.explorer.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.icons.IconFactory
import com.spoiligaming.explorer.ui.widgets.MergedInfoText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class SettingsCategory(
    val title: String,
    val icon: ImageBitmap,
    val index: Int,
    val content: @Composable () -> Unit,
    val outerContent: (@Composable () -> Unit)? = null,
)

private val categories =
    listOf(
        SettingsCategory(
            title = "General",
            icon = IconFactory.generalSettingsIcon,
            index = 0,
            content = { SettingsGeneral() },
        ),
        SettingsCategory(
            title = "Theme",
            icon = IconFactory.themeSettingsIcon,
            index = 1,
            content = { SettingsTheme() },
            outerContent = { SettingsThemeOuter() },
        ),
        SettingsCategory(
            title = "Advanced",
            icon = IconFactory.gearsIcon,
            index = 2,
            content = { SettingsAdvanced() },
        ),
    )

@Composable
fun SettingsScreen() {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier =
            Modifier.fillMaxWidth().height(526.dp).offset(y = 75.dp).background(Color.Transparent),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier =
                Modifier.fillMaxWidth(0.98f)
                    .height(517.dp)
                    .offset(y = (-2).dp)
                    .background(MapleColorPalette.quaternary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.TopCenter,
        ) {
            categories.forEach { category -> category.outerContent?.invoke() }

            LazyColumn(
                state = listState,
                modifier =
                    Modifier.fillMaxSize()
                        .background(Color.Transparent)
                        .padding(top = 68.dp, start = 12.dp),
            ) {
                itemsIndexed(categories) { index, category ->
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = category.title,
                            color = MapleColorPalette.fadedText,
                            style =
                                TextStyle(
                                    fontFamily = FontFactory.comfortaaRegular,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                ),
                        )

                        Column(
                            modifier = Modifier.offset(x = 5.dp),
                            verticalArrangement = Arrangement.spacedBy((-40).dp),
                        ) {
                            category.content()
                        }
                    }

                    if (index != categories.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth(0.98f),
                            contentAlignment = Alignment.Center,
                        ) {
                            HorizontalDivider(
                                color = MapleColorPalette.control,
                                thickness = 1.dp,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier.offset(x = 5.dp),
                            verticalArrangement = Arrangement.spacedBy((-40).dp),
                        ) {
                            MergedInfoText("Developed by ", "Spoili", MapleColorPalette.accent)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            SettingsCategoryBar(listState, coroutineScope)
        }
    }
}

@Composable
private fun SettingsCategoryBar(
    listState: LazyListState,
    coroutineScope: CoroutineScope,
) {
    Box(
        modifier =
            Modifier.fillMaxWidth(0.98f)
                .height(54.dp)
                .offset(y = 8.dp)
                .background(MapleColorPalette.tertiary, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 7.dp),
        ) {
            categories.forEach { category ->
                SettingsCategoryButton(category, listState, coroutineScope)
            }
        }
    }
}

@Composable
private fun SettingsCategoryButton(
    category: SettingsCategory,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier =
            Modifier.width(136.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(color = Color.White),
                ) {
                    coroutineScope.launch { listState.animateScrollToItem(category.index) }
                }
                .pointerHoverIcon(PointerIcon.Hand)
                .background(MapleColorPalette.secondary, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(4.dp))
            Image(
                bitmap = category.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = category.title,
                color = MapleColorPalette.fadedText,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaRegular,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                    ),
            )
        }
    }
}
