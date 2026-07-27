package com.mingzao.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cream = Color(0xFFFBF8F0)
val Ink = Color(0xFF243B53)
val SoftInk = Color(0xFF65758B)
val Honey = Color(0xFFF5C76B)
val Moss = Color(0xFF5D8C70)
val Coral = Color(0xFFE28B7C)
val Night = Color(0xFF111C2B)
val NightBlue = Color(0xFF20334B)

private val MingzaoColors = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    secondary = Moss,
    tertiary = Honey,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
)

@Composable
fun MingzaoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MingzaoColors,
        typography = Typography(),
        content = content,
    )
}
