package com.example.bleradar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bleradar.model.Proximity
import com.example.bleradar.storage.DeviceHistory
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(history: List<DeviceHistory>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { HistoryHeading(history.size) }
        if (history.isEmpty()) {
            item { HistoryEmpty() }
        } else {
            items(history, key = DeviceHistory::address) { device -> HistoryCard(device) }
        }
    }
}

@Composable
private fun HistoryHeading(count: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Jejak perangkat", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Perangkat yang pernah terdeteksi tersimpan otomatis di ponsel ini.",
            color = RadarColors.muted,
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth().background(RadarColors.mint, RoundedCornerShape(18.dp)).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("◷", color = RadarColors.teal, style = MaterialTheme.typography.headlineMedium)
            Column {
                Text("$count perangkat tersimpan", color = RadarColors.ink, fontWeight = FontWeight.Bold)
                Text("Terbaru ditampilkan lebih dulu", color = RadarColors.muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun HistoryEmpty() {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(20.dp)).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("◷", color = RadarColors.teal, style = MaterialTheme.typography.displaySmall)
        Text("Belum ada riwayat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Mulai pemindaian untuk menyimpan perangkat yang ditemukan.", color = RadarColors.muted)
    }
}

@Composable
private fun HistoryCard(device: DeviceHistory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(44.dp).background(RadarColors.mint, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text("⌁", color = RadarColors.teal, style = MaterialTheme.typography.headlineMedium) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        device.name ?: "Perangkat tanpa nama",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(device.address, color = RadarColors.muted, style = MaterialTheme.typography.bodySmall)
                }
            }
            HorizontalDivider(color = RadarColors.border)
            Text(
                "Terakhir terlihat · ${DateFormat.getDateTimeInstance().format(Date(device.lastSeen))}",
                color = RadarColors.muted,
                style = MaterialTheme.typography.bodySmall,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                SignalBars(device.lastRssi)
                Spacer(Modifier.size(9.dp))
                Text("${device.lastRssi} dBm", color = RadarColors.muted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                SignalBadge(Proximity.zone(device.lastRssi))
            }
        }
    }
}

@Preview(name = "Riwayat · terisi", showBackground = true, widthDp = 393, heightDp = 850)
@Preview(name = "Riwayat · layar kecil", showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun HistoryPopulatedPreview() {
    PreviewSurface { HistoryScreen(PreviewData.history) }
}

@Preview(name = "Riwayat · kosong", showBackground = true, widthDp = 393, heightDp = 850)
@Composable
private fun HistoryEmptyPreview() {
    PreviewSurface { HistoryScreen(emptyList()) }
}
