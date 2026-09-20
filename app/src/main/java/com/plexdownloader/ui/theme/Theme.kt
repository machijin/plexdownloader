package com.plexdownloader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColors = lightColorScheme(
    primary = Color(0xFF4D6FE5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE4FF),
    secondary = Color(0xFF5D6177),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
    surfaceVariant = Color(0xFFE3E2EC),
)

@Composable
fun PlexDownloaderTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, content = content)
}

