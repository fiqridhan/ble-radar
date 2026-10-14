package com.example.bleradar.ui

import androidx.compose.ui.graphics.Color
import com.example.bleradar.model.Proximity
import com.example.bleradar.model.SignalZone
import java.util.Locale

fun SignalZone.color(): Color = when (this) {
    SignalZone.VERY_CLOSE -> Color(0xFF087F5B)
    SignalZone.CLOSE -> Color(0xFF2B8A3E)
    SignalZone.GOOD -> Color(0xFF1C7ED6)
    SignalZone.WEAK -> Color(0xFFE67700)
    SignalZone.VERY_WEAK -> Color(0xFFD9480F)
    SignalZone.LOST -> Color(0xFF868E96)
}

fun distanceLabel(rssi: Int): String = Proximity.estimatedMeters(rssi)?.let {
    "± ${String.format(Locale("id", "ID"), "%.1f", it)} m"
} ?: "—"
