package com.example.podgauge.bluetooth

import com.example.podgauge.model.AirPodsBatteryState
import com.example.podgauge.model.AirPodsModel

private const val PROXIMITY_PAIRING_TYPE = 0x07
private const val PROXIMITY_PAIRING_LENGTH = 0x19
private const val EXPECTED_PAYLOAD_SIZE = 27

/**
 * Recognizes Apple's Proximity Pairing manufacturer payload shape used by AirPods.
 * The company identifier (0x004C) has already been removed by Android's
 * getManufacturerSpecificData(). Apple does not publish this packet schema, so this
 * intentionally performs only the well-observed 07 19 envelope check.
 */
fun isAirPodsPacket(data: ByteArray): Boolean =
    data.size >= EXPECTED_PAYLOAD_SIZE &&
        data[0].toInt() and 0xFF == PROXIMITY_PAIRING_TYPE &&
        data[1].toInt() and 0xFF == PROXIMITY_PAIRING_LENGTH

/**
 * Returns a safe partial state for a recognized AirPods-shaped packet.
 *
 * Battery, charging, and model offsets are reverse-engineered and are not stable across
 * every AirPods generation. Until real packets are captured and verified per model, this
 * MVP deliberately reports those fields as unknown instead of presenting guessed values.
 */
fun parseAirPodsData(data: ByteArray): AirPodsBatteryState? {
    if (!isAirPodsPacket(data)) return null
    return AirPodsBatteryState(
        leftBattery = null,
        rightBattery = null,
        caseBattery = null,
        leftCharging = null,
        rightCharging = null,
        caseCharging = null,
        model = getAirPodsModel(data),
        timestamp = System.currentTimeMillis(),
    )
}

/** Model bytes are intentionally not mapped until verified against physical hardware. */
fun getAirPodsModel(data: ByteArray): AirPodsModel =
    if (isAirPodsPacket(data)) AirPodsModel.UNKNOWN else AirPodsModel.UNKNOWN

internal fun validBatteryOrNull(value: Int): Int? = value.takeIf { it in 0..100 }
