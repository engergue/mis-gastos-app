package com.misgastos.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Primary,
    background = Background,
    surface = Surface,
    onSurface = OnSurface
)

private val DarkColors = darkColorScheme(
    primary = Primary,
    background = androidx.compose.ui.graphics.Color(0xFF14161C),
    surface = androidx.compose.ui.graphics.Color(0xFF1E212A),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE9EBF1)
)

@Composable
fun MisGastosTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
