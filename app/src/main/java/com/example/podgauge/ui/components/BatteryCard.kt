package com.example.podgauge.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.podgauge.ui.theme.PodGaugeTheme

@Composable
fun BatteryCard(
    label: String,
    battery: Int?,
    charging: Boolean?,
    modifier: Modifier = Modifier,
) {
    val safeBattery = battery?.takeIf { it in 0..100 }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Text(
                text = safeBattery?.let { "$it%" } ?: "--",
                fontSize = 34.sp,
                style = MaterialTheme.typography.headlineLarge,
            )
            if (charging == true) {
                Text(
                    text = "Charging",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BatteryCardPreview() {
    PodGaugeTheme { BatteryCard(label = "Left", battery = 84, charging = true) }
}
