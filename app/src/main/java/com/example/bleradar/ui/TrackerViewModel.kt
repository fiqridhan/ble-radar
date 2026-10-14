package com.example.bleradar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bleradar.ble.BleScanner
import com.example.bleradar.model.DeviceReading
import com.example.bleradar.storage.DeviceHistory
import com.example.bleradar.storage.DeviceHistoryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScannerUiState(
    val devices: List<DeviceReading> = emptyList(),
    val recentRssi: Map<String, List<Int>> = emptyMap(),
    val scanning: Boolean = false,
    val scanRequested: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val scanner: BleScanner,
    private val historyDao: DeviceHistoryDao,
) : ViewModel() {
    private val _state = MutableStateFlow(ScannerUiState())
    val state: StateFlow<ScannerUiState> = _state.asStateFlow()
    val history = historyDao.observeAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    private val _now = MutableStateFlow(System.currentTimeMillis())
    val now: StateFlow<Long> = _now.asStateFlow()

    private var foreground = false
    private var scanJob: Job? = null
    private val lastSaved = mutableMapOf<String, Long>()

    init {
        viewModelScope.launch {
            while (true) {
                _now.value = System.currentTimeMillis()
                val cutoff = _now.value - 15_000
                _state.update { state ->
                    val active = state.devices.filter { it.lastSeen >= cutoff }
                    if (active.size == state.devices.size) state
                    else {
                        val activeAddresses = active.mapTo(mutableSetOf()) { it.address }
                        lastSaved.keys.retainAll(activeAddresses)
                        state.copy(
                            devices = active,
                            recentRssi = state.recentRssi.filterKeys(activeAddresses::contains),
                        )
                    }
                }
                delay(1_000)
            }
        }
    }

    fun setForeground(visible: Boolean) {
        foreground = visible
        if (visible) resumeScan() else pauseScan()
    }

    fun startScan() {
        _state.update { it.copy(devices = emptyList(), recentRssi = emptyMap(), scanRequested = true, message = null) }
        resumeScan()
    }

    fun stopScan() {
        _state.update { it.copy(scanRequested = false, scanning = false, message = null) }
        pauseScan()
    }

    fun showMessage(message: String) {
        _state.update { it.copy(message = message, scanning = false) }
    }

    fun bluetoothChanged() {
        if (scanner.isBluetoothEnabled()) {
            resumeScan()
        } else {
            pauseScan()
            showMessage("Bluetooth nonaktif. Aktifkan Bluetooth untuk melanjutkan pemindaian.")
        }
    }

    private fun resumeScan() {
        if (!foreground || !_state.value.scanRequested || scanJob?.isActive == true) return
        if (!scanner.isBluetoothEnabled()) {
            showMessage("Bluetooth nonaktif. Aktifkan Bluetooth untuk melanjutkan pemindaian.")
            return
        }
        _state.update { it.copy(scanning = true, message = null) }
        scanJob = viewModelScope.launch {
            try {
                scanner.scan().collect(::onReading)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                showMessage(error.message ?: "Pemindaian BLE gagal")
            } finally {
                _state.update { it.copy(scanning = false) }
            }
        }
    }

    private fun pauseScan() {
        scanJob?.cancel()
        scanJob = null
        _state.update { it.copy(scanning = false) }
    }

    private fun onReading(reading: DeviceReading) {
        val previous = _state.value.devices.firstOrNull { it.address == reading.address }
        val resolved = reading.copy(name = reading.name ?: previous?.name)
        _state.update { state ->
            state.copy(
                devices = (state.devices.filterNot { it.address == reading.address } + resolved)
                    .sortedByDescending(DeviceReading::rssi),
                recentRssi = state.recentRssi + (reading.address to
                    ((state.recentRssi[reading.address].orEmpty() + reading.rssi).takeLast(8))),
            )
        }
        if (reading.lastSeen - (lastSaved[reading.address] ?: 0L) >= 3_000) {
            lastSaved[reading.address] = reading.lastSeen
            viewModelScope.launch {
                val oldName = historyDao.nameFor(reading.address)
                historyDao.upsert(DeviceHistory.from(resolved, oldName))
            }
        }
    }
}
