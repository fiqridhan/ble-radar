package com.example.bleradar.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.example.bleradar.model.DeviceReading
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BleScanner(context: Context) {
    private val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter

    @SuppressLint("MissingPermission")
    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun scan(): Flow<DeviceReading> = callbackFlow {
        val scanner = adapter?.takeIf { it.isEnabled }?.bluetoothLeScanner
            ?: throw IllegalStateException("Bluetooth tidak aktif atau BLE tidak tersedia")
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                runCatching { result.toReading() }
                    .onSuccess { trySend(it) }
                    .onFailure { close(it) }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                results.forEach { result ->
                    runCatching { result.toReading() }
                        .onSuccess { trySend(it) }
                        .onFailure { close(it) }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                close(IllegalStateException("Pemindaian BLE gagal (kode $errorCode)"))
            }
        }
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner.startScan(null, settings, callback)
        awaitClose {
            try {
                scanner.stopScan(callback)
            } catch (_: SecurityException) {
                // Permission may have been revoked while scanning.
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun ScanResult.toReading(): DeviceReading = DeviceReading(
        address = device.address,
        name = scanRecord?.deviceName?.takeIf(String::isNotBlank)
            ?: device.name?.takeIf(String::isNotBlank),
        rssi = rssi,
        lastSeen = System.currentTimeMillis(),
    )
}
