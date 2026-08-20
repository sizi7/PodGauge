package com.example.podgauge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.podgauge.bluetooth.AirPodsScanner
import com.example.podgauge.bluetooth.ScanStatus
import com.example.podgauge.model.AirPodsBatteryState
import com.example.podgauge.repository.AirPodsRepository
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AirPodsRepository(AirPodsScanner(application.applicationContext))

    val batteryState: StateFlow<AirPodsBatteryState?> = repository.batteryState
    val scanStatus: StateFlow<ScanStatus> = repository.scanStatus

    fun startAirPodsScan() = repository.startAirPodsScan()
    fun stopAirPodsScan() = repository.stopAirPodsScan()
    fun refreshPrerequisites() = repository.refreshPrerequisites()

    override fun onCleared() {
        repository.stopAirPodsScan()
        super.onCleared()
    }
}
