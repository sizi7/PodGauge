package com.example.podgauge.model

enum class AirPodsModel {
    AIRPODS,
    AIRPODS_2,
    AIRPODS_3,
    AIRPODS_4,
    AIRPODS_PRO,
    AIRPODS_PRO_2,
    AIRPODS_MAX,
    UNKNOWN,
}

fun AirPodsModel.displayName(): String = when (this) {
    AirPodsModel.AIRPODS -> "AirPods"
    AirPodsModel.AIRPODS_2 -> "AirPods 2"
    AirPodsModel.AIRPODS_3 -> "AirPods 3"
    AirPodsModel.AIRPODS_4 -> "AirPods 4"
    AirPodsModel.AIRPODS_PRO -> "AirPods Pro"
    AirPodsModel.AIRPODS_PRO_2 -> "AirPods Pro 2"
    AirPodsModel.AIRPODS_MAX -> "AirPods Max"
    AirPodsModel.UNKNOWN -> "AirPods"
}
