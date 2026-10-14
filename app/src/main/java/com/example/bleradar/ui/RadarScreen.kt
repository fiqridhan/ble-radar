package com.example.bleradar.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bleradar.model.DeviceReading
import com.example.bleradar.model.Proximity
import com.example.bleradar.model.SignalZone
import com.example.bleradar.storage.DeviceHistory
import java.text.DateFormat
import java.util.Date

@Composable
fun RadarScreen(
    address: String,
    reading: DeviceReading?,
    history: DeviceHistory?,
    recentRssi: List<Int>,
    scanning: Boolean,
    now: Long,
    onBack: () -> Unit,
) {
    val stale = reading == null || now - reading.lastSeen > 10_000
    val zone = if (scanning && reading != null) Proximity.zone(reading.rssi, stale) else SignalZone.LOST
    val color by animateColorAsState(zone.color(), label = "Signal zone")

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        TextButton(onClick = onBack) { Text("←  Kembali ke perangkat") }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("TARGET TERPILIH", color = RadarColors.teal, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(
                reading?.name ?: history?.name ?: "Perangkat tanpa nama",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(address, color = RadarColors.muted, style = MaterialTheme.typography.bodySmall)
        }
        RadarHero(zone, color, reading?.rssi, scanning)
        RadarDetails(reading, history, recentRssi, scanning, stale)
        Text(
            "Jarak hanya perkiraan berdasarkan RSSI. Dinding, posisi perangkat, dan interferensi dapat memengaruhi hasil.",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            color = RadarColors.muted,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun RadarHero(zone: SignalZone, color: Color, rssi: Int?, scanning: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.linearGradient(listOf(RadarColors.navy, Color(0xFF1B5260))), RoundedCornerShape(26.dp))
            .padding(horizontal = 20.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("ESTIMASI KEDEKATAN", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(14.dp))
        RadarGraphic(zone, color)
        Spacer(Modifier.height(12.dp))
        Text(
            if (zone == SignalZone.LOST) "—" else distanceLabel(rssi ?: -100),
            color = Color.White,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            when {
                !scanning -> "Pemindaian berhenti"
                zone == SignalZone.LOST -> "Sinyal tidak terdeteksi"
                else -> "Perkiraan jarak target"
            },
            color = Color.White.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            zone.label,
            modifier = Modifier.background(color.copy(alpha = 0.20f), RoundedCornerShape(100.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun RadarGraphic(zone: SignalZone, color: Color) {
    val coreRadius by animateFloatAsState(
        targetValue = when (zone) {
            SignalZone.VERY_CLOSE -> 0.18f
            SignalZone.CLOSE -> 0.16f
            SignalZone.GOOD -> 0.14f
            SignalZone.WEAK -> 0.12f
            SignalZone.VERY_WEAK -> 0.10f
            SignalZone.LOST -> 0.09f
        },
        label = "Radar radius",
    )
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.widthIn(max = 230.dp).fillMaxWidth().aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val unit = size.minDimension
                drawCircle(Color.White.copy(alpha = 0.06f), radius = unit * 0.49f)
                drawCircle(Color.White.copy(alpha = 0.16f), radius = unit * 0.47f, style = Stroke(1.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.23f), radius = unit * 0.34f, style = Stroke(1.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.32f), radius = unit * 0.22f, style = Stroke(1.dp.toPx()))
                drawCircle(color.copy(alpha = 0.23f), radius = unit * (coreRadius + 0.055f))
                drawCircle(color, radius = unit * coreRadius)
            }
            Text(
                if (zone == SignalZone.LOST) "×" else "⌁",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun RadarDetails(
    reading: DeviceReading?,
    history: DeviceHistory?,
    recentRssi: List<Int>,
    scanning: Boolean,
    stale: Boolean,
) {
    val spread = if (recentRssi.size >= 2) recentRssi.max() - recentRssi.min() else null
    val lastSeen = reading?.lastSeen ?: history?.lastSeen
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Detail sinyal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider(color = RadarColors.border)
            DetailLine("RSSI mentah", reading?.let { "${it.rssi} dBm" } ?: "—")
            DetailLine("Stabilitas", spread?.let {
                if (it <= 8) "Stabil · Δ $it dBm" else "Berfluktuasi · Δ $it dBm"
            } ?: "Menunggu sampel")
            DetailLine("Status", when {
                !scanning -> "Pemindaian berhenti"
                stale -> "Tidak terlihat dalam 10 detik"
                else -> "Terdeteksi langsung"
            })
            DetailLine("Terakhir terlihat", lastSeen?.let { DateFormat.getDateTimeInstance().format(Date(it)) } ?: "—")
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = RadarColors.muted, style = MaterialTheme.typography.bodySmall)
        Text(value, color = RadarColors.ink, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(name = "Radar · target dekat", showBackground = true, widthDp = 393, heightDp = 850)
@Preview(name = "Radar · layar kecil", showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun RadarActivePreview() {
    PreviewSurface {
        RadarScreen(
            address = PreviewData.devices.first().address,
            reading = PreviewData.devices.first(),
            history = PreviewData.history.first(),
            recentRssi = listOf(-40, -38, -37),
            scanning = true,
            now = PreviewData.now,
            onBack = {},
        )
    }
}

@Preview(name = "Radar · sinyal hilang", showBackground = true, widthDp = 393, heightDp = 850)
@Composable
private fun RadarLostPreview() {
    PreviewSurface {
        RadarScreen(
            address = PreviewData.devices.first().address,
            reading = PreviewData.devices.first().copy(lastSeen = PreviewData.now - 20_000),
            history = PreviewData.history.first(),
            recentRssi = emptyList(),
            scanning = true,
            now = PreviewData.now,
            onBack = {},
        )
    }
}
