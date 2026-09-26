package com.onhand.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cream = Color(0xFFF6F1E8)
val Surface = Color(0xFFFFFCF7)
val Ink = Color(0xFF2C2416)
val Muted = Color(0xFF7A7164)
val Sage = Color(0xFF5B7A5A)
val SageSoft = Color(0xFFE4EDE3)
val Terracotta = Color(0xFFC2653A)
val TerracottaSoft = Color(0xFFF4E0D4)
val Line = Color(0xFFE6DCCB)

private val colors = lightColorScheme(
    primary = Sage,
    onPrimary = Surface,
    secondary = Terracotta,
    onSecondary = Surface,
    background = Cream,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    outline = Line,
)

@Composable
fun OnHandTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}
