package com.spoiligaming.explorer.ui.screens.settings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.widgets.MergedInfoText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class SettingsCategory(
    val title: String,
    val icon: ImageVector,
    val index: Int,
    val content: @Composable () -> Unit,
    val outerContent: (@Composable () -> Unit)? = null,
)

private val categories =
    listOf(
        SettingsCategory(
            title = "General",
            icon = Icons.Filled.Tune,
            index = 0,
            content = { SettingsGeneral() },
        ),
        SettingsCategory(
            title = "Theme",
            icon = Icons.Filled.Palette,
            index = 1,
            content = { SettingsTheme() },
            outerContent = { SettingsThemeOuter() },
        ),
        SettingsCategory(
            title = "Advanced",
            icon = Icons.Filled.Code,
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
            Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        MapleColorPalette.quaternary,
                        RoundedCornerShape(12.dp),
                    )
                    .padding(10.dp),
        ) {
            categories.forEach { category -> category.outerContent?.invoke() }
            SettingsCategoryBar(listState, coroutineScope)

            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .weight(1f)
                        .background(Color.Transparent),
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
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            HorizontalDivider(
                                color = MapleColorPalette.control,
                                thickness = 1.dp,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            MergedInfoText("Developed by ", "Spoili", MapleColorPalette.accent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoryBar(
    listState: LazyListState,
    coroutineScope: CoroutineScope,
) = Box(
    modifier =
        Modifier.fillMaxWidth()
            .background(MapleColorPalette.tertiary, RoundedCornerShape(12.dp)),
    contentAlignment = Alignment.CenterStart,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(10.dp),
    ) {
        categories.forEach { category ->
            SettingsCategoryButton(category, listState, coroutineScope)
        }
    }
}

@Composable
private fun SettingsCategoryButton(
    category: SettingsCategory,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
) = Box(
    modifier =
        Modifier
            .width(133.dp)
            .height(40.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White),
            ) {
                coroutineScope.launch { listState.animateScrollToItem(category.index) }
            }
            .pointerHoverIcon(PointerIcon.Hand)
            .background(MapleColorPalette.secondary, RoundedCornerShape(8.dp)),
    contentAlignment = Alignment.CenterStart,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp),
        ) {
            Icon(
                imageVector = category.icon,
                tint = MapleColorPalette.fadedText,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
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
