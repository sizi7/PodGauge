package com.example.podgauge.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.podgauge.bluetooth.ScanStatus
import com.example.podgauge.model.AirPodsBatteryState
import com.example.podgauge.model.AirPodsModel
import com.example.podgauge.model.displayName
import com.example.podgauge.ui.components.BatteryCard
import com.example.podgauge.ui.theme.PodGaugeTheme
import java.text.DateFormat
import java.util.Date

@Composable
fun MainScreen(
    batteryState: AirPodsBatteryState?,
    scanStatus: ScanStatus,
    onRequestPermission: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Text("PodGauge", style = MaterialTheme.typography.headlineLarge)
            Text(
                "AirPods battery monitor",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(36.dp))

            when (scanStatus) {
                ScanStatus.PERMISSION_REQUIRED -> MessageState(
                    title = "Bluetooth permission is required.",
                    detail = "Allow nearby device access so PodGauge can scan for AirPods.",
                    buttonText = "Grant permission",
                    onClick = onRequestPermission,
                )
                ScanStatus.BLUETOOTH_OFF -> MessageState(
                    title = "Bluetooth is turned off.",
                    detail = "Turn on Bluetooth, then try again.",
                    buttonText = "Try again",
                    onClick = onRetry,
                )
                ScanStatus.BLE_UNAVAILABLE -> MessageState(
                    title = "Bluetooth LE is unavailable.",
                    detail = "This device does not provide a usable BLE scanner.",
                )
                ScanStatus.FAILED -> MessageState(
                    title = "Bluetooth scan failed.",
                    detail = "Wait a moment and try scanning again.",
                    buttonText = "Retry",
                    onClick = onRetry,
                )
                else -> if (batteryState == null) LookingForAirPods() else AirPodsContent(batteryState)
            }
        }
    }
}

@Composable
private fun LookingForAirPods() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Looking for AirPods...", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Open your AirPods case nearby.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun AirPodsContent(state: AirPodsBatteryState) {
    Text(state.model.displayName(), style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(20.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BatteryCard("Left", state.leftBattery, state.leftCharging, Modifier.weight(1f))
        BatteryCard("Right", state.rightBattery, state.rightCharging, Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    BatteryCard("Case", state.caseBattery, state.caseCharging)
    Spacer(Modifier.height(24.dp))
    Text(
        "Last updated: ${DateFormat.getTimeInstance(DateFormat.MEDIUM).format(Date(state.timestamp))}",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun MessageState(
    title: String,
    detail: String,
    buttonText: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (buttonText != null && onClick != null) Button(onClick = onClick) { Text(buttonText) }
    }
}

private val fakeState = AirPodsBatteryState(
    leftBattery = 84,
    rightBattery = 79,
    caseBattery = 52,
    leftCharging = false,
    rightCharging = false,
    caseCharging = true,
    model = AirPodsModel.AIRPODS_PRO_2,
    timestamp = 1_750_000_000_000L,
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenDetectedPreview() {
    PodGaugeTheme {
        MainScreen(fakeState, ScanStatus.SCANNING, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenLookingPreview() {
    PodGaugeTheme {
        MainScreen(null, ScanStatus.SCANNING, {}, {})
    }
}
