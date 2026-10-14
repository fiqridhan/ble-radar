package com.example.bleradar

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.bleradar.ui.BleRadarScreen
import com.example.bleradar.ui.TrackerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: TrackerViewModel by viewModels()
    private var receiverRegistered = false

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED && hasPermissions()) {
                viewModel.bluetoothChanged()
            }
        }
    }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (requiredPermissions().all { grants[it] == true || hasPermission(it) }) {
            requestBluetooth()
        } else {
            viewModel.showMessage("Izin Bluetooth dan lokasi diperlukan untuk mencari perangkat.")
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (isBluetoothEnabled()) startIfLocationReady()
        else viewModel.showMessage("Aktifkan Bluetooth untuk memulai pemindaian.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BleRadarScreen(viewModel, ::requestScan) }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        receiverRegistered = true
        if (hasPermissions()) viewModel.setForeground(true)
        else viewModel.stopScan()
    }

    override fun onStop() {
        viewModel.setForeground(false)
        if (receiverRegistered) {
            unregisterReceiver(bluetoothReceiver)
            receiverRegistered = false
        }
        super.onStop()
    }

    private fun requestScan() {
        if (hasPermissions()) requestBluetooth()
        else permissionsLauncher.launch(requiredPermissions())
    }

    private fun requestBluetooth() {
        if (isBluetoothEnabled()) startIfLocationReady()
        else enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private fun startIfLocationReady() {
        val location = getSystemService(LocationManager::class.java)
        val locationEnabled = if (Build.VERSION.SDK_INT >= 28) {
            location?.isLocationEnabled == true
        } else {
            location?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                location?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        }
        if (!locationEnabled) {
            viewModel.showMessage("Aktifkan layanan lokasi perangkat agar pemindaian BLE tersedia.")
        } else {
            viewModel.startScan()
        }
    }

    private fun requiredPermissions(): Array<String> = if (Build.VERSION.SDK_INT >= 31) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun hasPermissions() = requiredPermissions().all(::hasPermission)

    private fun hasPermission(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun isBluetoothEnabled(): Boolean =
        getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true
}
