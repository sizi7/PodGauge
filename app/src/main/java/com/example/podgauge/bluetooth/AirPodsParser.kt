package com.example.podgauge.bluetooth

import com.example.podgauge.model.AirPodsBatteryState
import com.example.podgauge.model.AirPodsModel

private const val PROXIMITY_PAIRING_TYPE = 0x07
private const val ENVELOPE_HEADER_SIZE = 2
private const val MINIMUM_BODY_SIZE = 0x0F

/**
 * Recognizes Apple's Proximity Pairing manufacturer payload shape used by AirPods.
 * The company identifier (0x004C) has already been removed by Android's
 * getManufacturerSpecificData(). Apple does not publish this packet schema, so this
 * intentionally validates only the well-observed type byte and self-described length.
 * Real Galaxy captures include multiple envelope lengths (for example 07 0F and 07 19),
 * so a single generation-specific length must not be required here.
 */
fun isAirPodsPacket(data: ByteArray): Boolean {
    if (data.size < ENVELOPE_HEADER_SIZE) return false
    val type = data[0].toInt() and 0xFF
    val declaredBodySize = data[1].toInt() and 0xFF
    return type == PROXIMITY_PAIRING_TYPE &&
        declaredBodySize >= MINIMUM_BODY_SIZE &&
        declaredBodySize == data.size - ENVELOPE_HEADER_SIZE
}

/**
 * Returns a safe partial state for a recognized AirPods-shaped packet.
 *
 * Battery, charging, and model offsets are reverse-engineered and are not stable across
 * every AirPods generation. Until real packets are captured and verified per model, this
 * MVP deliberately reports those fields as unknown instead of presenting guessed values.
 */
fun parseAirPodsData(data: ByteArray): AirPodsBatteryState? {
    if (!isAirPodsPacket(data)) return null
    if (data.size <= CASE_AND_FLAGS_INDEX) return null

    // Reverse-engineered Proximity Pairing layout, verified against this Galaxy capture:
    // byte 5 bit 5 selects which nibble in byte 6 represents the left/primary pod.
    // Battery nibbles 0..9 mean 0..90%, A..E mean 100%, and F means unavailable.
    val status = data[STATUS_INDEX].toInt() and 0xFF
    val podBatteries = data[PODS_BATTERY_INDEX].toInt() and 0xFF
    val primaryBattery = decodeBatteryNibble(podBatteries ushr 4)
    val secondaryBattery = decodeBatteryNibble(podBatteries and 0x0F)
    val isLeftPrimary = status and LEFT_PRIMARY_MASK != 0
    val leftBattery = if (isLeftPrimary) primaryBattery else secondaryBattery
    val rightBattery = if (isLeftPrimary) secondaryBattery else primaryBattery
    val caseBattery = decodeBatteryNibble(
        (data[CASE_AND_FLAGS_INDEX].toInt() and 0xFF) ushr 4,
    )

    return AirPodsBatteryState(
        leftBattery = leftBattery,
        rightBattery = rightBattery,
        caseBattery = caseBattery,
        leftCharging = null,
        rightCharging = null,
        caseCharging = null,
        model = getAirPodsModel(data),
        timestamp = System.currentTimeMillis(),
    )
}

/** Maps model identifiers corroborated by the captured packet and public decoders. */
fun getAirPodsModel(data: ByteArray): AirPodsModel =
    if (!isAirPodsPacket(data) || data.size <= MODEL_LOW_INDEX) {
        AirPodsModel.UNKNOWN
    } else {
        val modelId = ((data[MODEL_HIGH_INDEX].toInt() and 0xFF) shl 8) or
            (data[MODEL_LOW_INDEX].toInt() and 0xFF)
        when (modelId) {
            0x0220 -> AirPodsModel.AIRPODS
            0x0F20 -> AirPodsModel.AIRPODS_2
            0x1320 -> AirPodsModel.AIRPODS_3
            0x1920, 0x1B20 -> AirPodsModel.AIRPODS_4
            0x0E20 -> AirPodsModel.AIRPODS_PRO
            0x1420, 0x2420 -> AirPodsModel.AIRPODS_PRO_2
            0x0A20, 0x1F20 -> AirPodsModel.AIRPODS_MAX
            else -> AirPodsModel.UNKNOWN
        }
    }

private fun decodeBatteryNibble(nibble: Int): Int? = when (nibble) {
    in 0x0..0x9 -> validBatteryOrNull(nibble * 10)
    in 0xA..0xE -> 100
    else -> null
}

internal fun validBatteryOrNull(value: Int): Int? = value.takeIf { it in 0..100 }

private const val MODEL_HIGH_INDEX = 3
private const val MODEL_LOW_INDEX = 4
private const val STATUS_INDEX = 5
private const val PODS_BATTERY_INDEX = 6
private const val CASE_AND_FLAGS_INDEX = 7
private const val LEFT_PRIMARY_MASK = 0x20
