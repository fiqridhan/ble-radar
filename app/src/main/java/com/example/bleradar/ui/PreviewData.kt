package com.example.bleradar.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bleradar.model.DeviceReading
import com.example.bleradar.storage.DeviceHistory

internal object PreviewData {
    const val now = 1_700_000_000_000L

    val devices = listOf(
        DeviceReading("C1:2A:03:4B:5C:6D", "Kunci BLE", -37, now),
        DeviceReading("D2:3B:14:5C:6D:7E", "Sensor Ruang", -63, now - 2_000),
        DeviceReading("E3:4C:25:6D:7E:8F", null, -84, now - 5_000),
    )

    val history = listOf(
        DeviceHistory("C1:2A:03:4B:5C:6D", "Kunci BLE", -37, now),
        DeviceHistory("D2:3B:14:5C:6D:7E", "Sensor Ruang", -63, now - 60_000),
    )
}

@Composable
internal fun PreviewSurface(content: @Composable () -> Unit) {
    BleRadarTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = RadarColors.background, content = content)
    }
}
