package com.example.podgauge.repository

import com.example.podgauge.bluetooth.AirPodsScanner
import com.example.podgauge.bluetooth.ScanStatus
import com.example.podgauge.model.AirPodsBatteryState
import kotlinx.coroutines.flow.StateFlow

class AirPodsRepository(private val scanner: AirPodsScanner) {
    val batteryState: StateFlow<AirPodsBatteryState?> = scanner.batteryState
    val scanStatus: StateFlow<ScanStatus> = scanner.scanStatus

    fun startAirPodsScan() = scanner.startAirPodsScan()
    fun stopAirPodsScan() = scanner.stopAirPodsScan()
    fun refreshPrerequisites() = scanner.refreshPrerequisites()
}
