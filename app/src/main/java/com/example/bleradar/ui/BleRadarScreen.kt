package com.example.bleradar.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bleradar.storage.DeviceHistory

private enum class Page { SCANNER, HISTORY, RADAR }

@Composable
fun BleRadarScreen(viewModel: TrackerViewModel, onStartScan: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val now by viewModel.now.collectAsStateWithLifecycle()
    BleRadarContent(state, history, now, onStartScan, viewModel::stopScan)
}

@Composable
private fun BleRadarContent(
    state: ScannerUiState,
    history: List<DeviceHistory>,
    now: Long,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
) {
    var pageName by rememberSaveable { mutableStateOf(Page.SCANNER.name) }
    var targetAddress by rememberSaveable { mutableStateOf<String?>(null) }
    val page = Page.valueOf(pageName)

    BackHandler(page == Page.RADAR) { pageName = Page.SCANNER.name }
    BleRadarTheme {
        Scaffold(
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = { AppHeader(page, state.scanning) },
            bottomBar = {
                if (page != Page.RADAR) {
                    NavigationBar(
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        containerColor = Color.White,
                        tonalElevation = 0.dp,
                    ) {
                        NavigationBarItem(
                            selected = page == Page.SCANNER,
                            onClick = { pageName = Page.SCANNER.name },
                            icon = { Text("◎", style = MaterialTheme.typography.headlineSmall) },
                            label = { Text("Pindai") },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = RadarColors.mint),
                        )
                        NavigationBarItem(
                            selected = page == Page.HISTORY,
                            onClick = { pageName = Page.HISTORY.name },
                            icon = { Text("◷", style = MaterialTheme.typography.headlineSmall) },
                            label = { Text("Riwayat") },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = RadarColors.mint),
                        )
                    }
                }
            },
        ) { padding ->
            Surface(modifier = Modifier.fillMaxSize().padding(padding), color = RadarColors.background) {
                when (page) {
                    Page.SCANNER -> ScannerScreen(
                        state = state,
                        now = now,
                        onStart = onStartScan,
                        onStop = onStopScan,
                        onSelect = { targetAddress = it; pageName = Page.RADAR.name },
                    )
                    Page.HISTORY -> HistoryScreen(history)
                    Page.RADAR -> RadarScreen(
                        address = targetAddress.orEmpty(),
                        reading = state.devices.firstOrNull { it.address == targetAddress },
                        history = history.firstOrNull { it.address == targetAddress },
                        recentRssi = state.recentRssi[targetAddress].orEmpty(),
                        scanning = state.scanning,
                        now = now,
                        onBack = { pageName = Page.SCANNER.name },
                    )
                }
            }
        }
    }
}

@Preview(name = "Aplikasi · layar utama", showBackground = true, showSystemUi = true, widthDp = 393, heightDp = 850)
@Preview(name = "Aplikasi · layar kecil", showBackground = true, showSystemUi = true, widthDp = 320, heightDp = 640)
@Composable
private fun BleRadarContentPreview() {
    BleRadarContent(
        state = ScannerUiState(devices = PreviewData.devices, scanning = true, scanRequested = true),
        history = PreviewData.history,
        now = PreviewData.now,
        onStartScan = {},
        onStopScan = {},
    )
}

@Composable
private fun AppHeader(page: Page, scanning: Boolean) {
    Surface(color = Color.White) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).background(RadarColors.navy, RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("◉", color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
                Column {
                    Text("BLE RADAR", style = MaterialTheme.typography.labelMedium, color = RadarColors.teal, fontWeight = FontWeight.Bold)
                    Text(
                        when (page) {
                            Page.SCANNER -> "Jelajahi sekitar"
                            Page.HISTORY -> "Riwayat perangkat"
                            Page.RADAR -> "Lacak perangkat"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            if (scanning) {
                Box(Modifier.background(RadarColors.mint, RoundedCornerShape(100.dp)).padding(horizontal = 10.dp, vertical = 7.dp)) {
                    Text("LIVE", color = RadarColors.teal, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
