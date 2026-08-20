package com.example.podgauge

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.podgauge.bluetooth.ScanStatus
import com.example.podgauge.ui.MainScreen
import com.example.podgauge.ui.theme.PodGaugeTheme
import com.example.podgauge.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants.values.all { it }) viewModel.startAirPodsScan() else viewModel.refreshPrerequisites()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PodGaugeTheme {
                val batteryState = viewModel.batteryState.collectAsStateWithLifecycle()
                val scanStatus = viewModel.scanStatus.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.refreshPrerequisites()
                    if (viewModel.scanStatus.value != ScanStatus.PERMISSION_REQUIRED) {
                        viewModel.startAirPodsScan()
                    }
                }

                MainScreen(
                    batteryState = batteryState.value,
                    scanStatus = scanStatus.value,
                    onRequestPermission = { permissionLauncher.launch(requiredPermissions()) },
                    onRetry = {
                        viewModel.stopAirPodsScan()
                        viewModel.startAirPodsScan()
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPrerequisites()
        viewModel.startAirPodsScan()
    }

    override fun onPause() {
        viewModel.stopAirPodsScan()
        super.onPause()
    }

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
}
