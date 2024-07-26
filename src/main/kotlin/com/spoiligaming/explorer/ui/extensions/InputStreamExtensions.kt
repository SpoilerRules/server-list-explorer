package com.spoiligaming.explorer.ui.extensions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import java.io.InputStream

fun InputStream?.asImageBitmap(): ImageBitmap? = this?.let { loadImageBitmap(it) }
