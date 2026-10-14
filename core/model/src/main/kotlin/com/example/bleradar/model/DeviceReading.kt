package com.example.bleradar.model

data class DeviceReading(
    val address: String,
    val name: String?,
    val rssi: Int,
    val lastSeen: Long,
)

enum class SignalZone(val label: String, val description: String) {
    VERY_CLOSE("Sangat dekat", "< 1 m"),
    CLOSE("Dekat", "1–3 m"),
    GOOD("Cukup / baik", "3–10 m"),
    WEAK("Lemah", "10–20 m"),
    VERY_WEAK("Sangat lemah", "> 20 m"),
    LOST("Sinyal hilang", "Di luar jangkauan"),
}

object Proximity {
    fun zone(rssi: Int, stale: Boolean = false): SignalZone = when {
        stale || rssi < -90 -> SignalZone.LOST
        rssi >= -30 -> SignalZone.VERY_CLOSE
        rssi >= -50 -> SignalZone.CLOSE
        rssi >= -70 -> SignalZone.GOOD
        rssi >= -80 -> SignalZone.WEAK
        else -> SignalZone.VERY_WEAK
    }

    // The brief defines broad RSSI bands, so interpolate only within its approximate ranges.
    fun estimatedMeters(rssi: Int): Double? = when (zone(rssi)) {
        SignalZone.VERY_CLOSE -> 0.2 + ((-rssi - 10).coerceIn(0, 20) / 20.0) * 0.8
        SignalZone.CLOSE -> 1.0 + ((-rssi - 30).coerceIn(0, 20) / 20.0) * 2.0
        SignalZone.GOOD -> 3.0 + ((-rssi - 50).coerceIn(0, 20) / 20.0) * 7.0
        SignalZone.WEAK -> 10.0 + ((-rssi - 70).coerceIn(0, 10) / 10.0) * 10.0
        SignalZone.VERY_WEAK -> 20.0 + ((-rssi - 80).coerceIn(0, 10) / 10.0) * 10.0
        SignalZone.LOST -> null
    }
}
