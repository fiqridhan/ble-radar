package com.example.bleradar.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal object RadarColors {
    val background = Color(0xFFF4F7FA)
    val ink = Color(0xFF162B3D)
    val muted = Color(0xFF64798A)
    val navy = Color(0xFF112B3D)
    val teal = Color(0xFF087F78)
    val mint = Color(0xFFE5F5F1)
    val border = Color(0xFFE4EBF0)
}

@Composable
fun BleRadarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = RadarColors.teal,
            secondary = Color(0xFF4A7075),
            background = RadarColors.background,
            surface = Color.White,
            onSurface = RadarColors.ink,
            onBackground = RadarColors.ink,
        ),
        content = content,
    )
}
