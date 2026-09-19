package com.misgastos.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = HeaderDark,
    background = Background,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = PrimarySoft,
    outline = BorderColor
)

private val DarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = HeaderDarkVariant,
    background = Color(0xFF121316),
    surface = Color(0xFF1C1E23),
    onSurface = Color(0xFFEDEFF3),
    surfaceVariant = Color(0xFF23262D),
    outline = Color(0xFF2E313A)
)

@Composable
fun MisGastosTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
