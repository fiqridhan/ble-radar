package com.example.bleradar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bleradar.model.DeviceReading
import com.example.bleradar.model.Proximity

@Composable
fun ScannerScreen(
    state: ScannerUiState,
    now: Long,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onSelect: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filterEnabled by rememberSaveable { mutableStateOf(false) }
    var threshold by rememberSaveable { mutableFloatStateOf(-80f) }
    val visible = state.devices.filter { device ->
        now - device.lastSeen <= 15_000 &&
            (!filterEnabled || device.rssi >= threshold.toInt()) &&
            (query.isBlank() || device.address.contains(query, true) ||
                device.name?.contains(query, true) == true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item { ScanHero(state, state.devices.count { now - it.lastSeen <= 15_000 }, onStart, onStop) }
        state.message?.let { message -> item { ErrorCard(message) } }
        item { ScanFilters(query, { query = it }, filterEnabled, { filterEnabled = it }, threshold, { threshold = it }) }
        item { DeviceSectionHeader(visible.size) }
        if (visible.isEmpty()) {
            item { EmptyDevices(state.scanning, query.isNotBlank() || filterEnabled) }
        } else {
            items(visible, key = DeviceReading::address) { device ->
                DeviceCard(device, onClick = { onSelect(device.address) })
            }
        }
    }
}

@Composable
private fun ScanHero(state: ScannerUiState, count: Int, onStart: () -> Unit, onStop: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.linearGradient(listOf(RadarColors.navy, Color(0xFF175D67))), RoundedCornerShape(24.dp))
            .padding(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallDot(if (state.scanning) Color(0xFF66E5BC) else Color(0xFF9AAEBA))
            Text(
                if (state.scanning) "PEMINDAIAN AKTIF" else "PEMINDAIAN SIAP",
                color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(18.dp))
        Text("$count", color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Text("perangkat di sekitar", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            if (state.scanning) "Daftar diperbarui otomatis saat sinyal terdeteksi."
            else "Mulai pemindaian untuk menemukan perangkat Bluetooth.",
            color = Color.White.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = if (state.scanning) onStop else onStart,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = RadarColors.navy),
        ) {
            Text(if (state.scanning) "■  Hentikan pemindaian" else "◎  Mulai pemindaian", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Text(
        message,
        modifier = Modifier.fillMaxWidth().background(Color(0xFFFFEDEA), RoundedCornerShape(14.dp))
            .padding(16.dp),
        color = Color(0xFFAD3D2F),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun ScanFilters(
    query: String,
    onQuery: (String) -> Unit,
    enabled: Boolean,
    onEnabled: (Boolean) -> Unit,
    threshold: Float,
    onThreshold: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Cari perangkat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            placeholder = { Text("Nama atau alamat MAC") },
            leadingIcon = { Text("⌕", style = MaterialTheme.typography.headlineSmall) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        FilterChip(
            selected = enabled,
            onClick = { onEnabled(!enabled) },
            label = { Text(if (enabled) "RSSI ≥ ${threshold.toInt()} dBm" else "Filter kekuatan sinyal") },
        )
        if (enabled) {
            Slider(value = threshold, onValueChange = onThreshold, valueRange = -100f..-30f, steps = 69)
        }
    }
}

@Composable
private fun DeviceSectionHeader(count: Int) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Perangkat terdeteksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Diurutkan dari sinyal terkuat", color = RadarColors.muted, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            "$count", modifier = Modifier.background(RadarColors.mint, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp),
            color = RadarColors.teal, fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun EmptyDevices(scanning: Boolean, filtered: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(20.dp)).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("◎", color = RadarColors.teal, style = MaterialTheme.typography.displaySmall)
        Text(
            when {
                filtered -> "Tidak ada hasil yang cocok"
                scanning -> "Sedang mencari perangkat"
                else -> "Belum ada perangkat"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            when {
                filtered -> "Ubah kata kunci atau longgarkan filter sinyal."
                scanning -> "Dekatkan perangkat dan pastikan Bluetooth aktif."
                else -> "Tekan Mulai pemindaian untuk melihat perangkat di sekitar."
            },
            color = RadarColors.muted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DeviceCard(device: DeviceReading, onClick: () -> Unit) {
    val zone = Proximity.zone(device.rssi)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(46.dp).background(RadarColors.mint, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text("⌁", color = RadarColors.teal, style = MaterialTheme.typography.headlineMedium) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        device.name ?: "Perangkat tanpa nama",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(device.address, color = RadarColors.muted, style = MaterialTheme.typography.bodySmall)
                }
                Text("›", color = RadarColors.muted, style = MaterialTheme.typography.headlineMedium)
            }
            HorizontalDivider(color = RadarColors.border)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SignalBars(device.rssi)
                Spacer(Modifier.size(9.dp))
                Text("${device.rssi} dBm", color = RadarColors.muted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                Text(distanceLabel(device.rssi), fontWeight = FontWeight.Bold, color = RadarColors.ink)
            }
            SignalBadge(zone)
        }
    }
}

@Preview(name = "Scanner · memindai", showBackground = true, widthDp = 393, heightDp = 850)
@Preview(name = "Scanner · layar kecil", showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun ScannerActivePreview() {
    PreviewSurface {
        ScannerScreen(
            state = ScannerUiState(devices = PreviewData.devices, scanning = true, scanRequested = true),
            now = PreviewData.now,
            onStart = {},
            onStop = {},
            onSelect = {},
        )
    }
}

@Preview(name = "Scanner · kosong", showBackground = true, widthDp = 393, heightDp = 850)
@Composable
private fun ScannerEmptyPreview() {
    PreviewSurface { ScannerScreen(ScannerUiState(), PreviewData.now, {}, {}, {}) }
}
