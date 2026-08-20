package com.example.podgauge.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.podgauge.BuildConfig
import com.example.podgauge.model.AirPodsBatteryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AirPodsScanner(private val context: Context) {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter get() = bluetoothManager?.adapter

    private val _batteryState = MutableStateFlow<AirPodsBatteryState?>(null)
    val batteryState: StateFlow<AirPodsBatteryState?> = _batteryState.asStateFlow()

    private val _scanStatus = MutableStateFlow(ScanStatus.IDLE)
    val scanStatus: StateFlow<ScanStatus> = _scanStatus.asStateFlow()

    private var scanning = false
    private var lastPacket: ByteArray? = null
    private var lastPublishedAt = 0L

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach(::handleResult)
        }

        override fun onScanFailed(errorCode: Int) {
            scanning = false
            _scanStatus.value = ScanStatus.FAILED
            Log.e(TAG, "BLE scan failed with code $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun startAirPodsScan() {
        if (scanning) return
        if (!hasScanPermission()) {
            _scanStatus.value = ScanStatus.PERMISSION_REQUIRED
            return
        }

        val adapter = bluetoothAdapter
        if (adapter == null || !context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            _scanStatus.value = ScanStatus.BLE_UNAVAILABLE
            return
        }
        if (!adapter.isEnabled) {
            _scanStatus.value = ScanStatus.BLUETOOTH_OFF
            return
        }
        val scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            _scanStatus.value = ScanStatus.BLE_UNAVAILABLE
            return
        }

        val filter = ScanFilter.Builder()
            .setManufacturerData(
                APPLE_COMPANY_ID,
                byteArrayOf(0x07),
                byteArrayOf(0xFF.toByte()),
            )
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner.startScan(listOf(filter), settings, scanCallback)
            scanning = true
            _scanStatus.value = ScanStatus.SCANNING
        } catch (_: SecurityException) {
            _scanStatus.value = ScanStatus.PERMISSION_REQUIRED
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Unable to start BLE scan", exception)
            _scanStatus.value = ScanStatus.FAILED
        }
    }

    @SuppressLint("MissingPermission")
    fun stopAirPodsScan() {
        if (!scanning) return
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (_: SecurityException) {
            // Permission may be revoked while the activity is paused.
        } finally {
            scanning = false
            if (_scanStatus.value == ScanStatus.SCANNING) _scanStatus.value = ScanStatus.IDLE
        }
    }

    fun refreshPrerequisites() {
        if (!hasScanPermission()) {
            _scanStatus.value = ScanStatus.PERMISSION_REQUIRED
            return
        }
        try {
            if (bluetoothAdapter?.isEnabled != true) _scanStatus.value = ScanStatus.BLUETOOTH_OFF
        } catch (_: SecurityException) {
            _scanStatus.value = ScanStatus.PERMISSION_REQUIRED
        }
    }

    private fun handleResult(result: ScanResult) {
        val data = result.scanRecord?.getManufacturerSpecificData(APPLE_COMPANY_ID) ?: return
        if (!isAirPodsPacket(data)) return

        if (BuildConfig.DEBUG) Log.d(TAG, "AirPods packet:\n${data.toHexString()}")

        val now = System.currentTimeMillis()
        val isDuplicate = lastPacket?.contentEquals(data) == true
        if (isDuplicate && now - lastPublishedAt < DUPLICATE_REFRESH_MS) return

        val parsed = parseAirPodsData(data) ?: return
        lastPacket = data.copyOf()
        lastPublishedAt = now
        _batteryState.value = parsed.copy(timestamp = now)
    }

    private fun hasScanPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_SCAN
        } else {
            Manifest.permission.ACCESS_FINE_LOCATION
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun ByteArray.toHexString(): String = joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }

    private companion object {
        const val TAG = "PodGaugeScanner"
        const val APPLE_COMPANY_ID = 0x004C
        const val DUPLICATE_REFRESH_MS = 5_000L
    }
}
