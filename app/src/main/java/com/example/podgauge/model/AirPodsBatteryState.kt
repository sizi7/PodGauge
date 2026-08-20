package com.example.podgauge.model

data class AirPodsBatteryState(
    val leftBattery: Int?,
    val rightBattery: Int?,
    val caseBattery: Int?,
    val leftCharging: Boolean?,
    val rightCharging: Boolean?,
    val caseCharging: Boolean?,
    val model: AirPodsModel,
    val timestamp: Long,
    val deviceName: String? = null,
)
