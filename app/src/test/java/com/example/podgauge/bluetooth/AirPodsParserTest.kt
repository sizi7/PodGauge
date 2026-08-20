package com.example.podgauge.bluetooth

import com.example.podgauge.model.AirPodsModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AirPodsParserTest {
    @Test fun rejectsNonAirPodsApplePayload() {
        assertFalse(isAirPodsPacket(byteArrayOf(0x01, 0x02)))
        assertNull(parseAirPodsData(ByteArray(27)))
    }

    @Test fun acceptsObservedProximityPairingEnvelopeWithoutGuessingFields() {
        val data = ByteArray(27).also {
            it[0] = 0x07
            it[1] = 0x19
            it[6] = 0xFF.toByte()
            it[7] = 0xF0.toByte()
        }
        val parsed = parseAirPodsData(data)
        assertTrue(isAirPodsPacket(data))
        assertEquals(AirPodsModel.UNKNOWN, parsed?.model)
        assertNull(parsed?.leftBattery)
    }

    @Test fun acceptsShorterEnvelopeCapturedFromGalaxy() {
        val data = byteArrayOf(
            0x07, 0x0F, 0x00, 0x0E, 0x20, 0x00, 0xC5.toByte(), 0x85.toByte(), 0x03,
            0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x00,
        )
        assertTrue(isAirPodsPacket(data))
        val parsed = parseAirPodsData(data)
        assertEquals(AirPodsModel.AIRPODS_PRO, parsed?.model)
        assertEquals(50, parsed?.leftBattery)
        assertEquals(100, parsed?.rightBattery)
        assertEquals(80, parsed?.caseBattery)
    }

    @Test fun rejectsEnvelopeWithInvalidDeclaredLength() {
        val data = ByteArray(17).also { it[0] = 0x07; it[1] = 0x19 }
        assertFalse(isAirPodsPacket(data))
    }

    @Test fun validatesBatteryRange() {
        assertEquals(0, validBatteryOrNull(0))
        assertEquals(100, validBatteryOrNull(100))
        assertNull(validBatteryOrNull(-1))
        assertNull(validBatteryOrNull(101))
    }

    @Test fun treatsUnavailableBatteryNibbleAsNullAndHonorsLeftPrimaryBit() {
        val data = ByteArray(17).also {
            it[0] = 0x07
            it[1] = 0x0F
            it[3] = 0x24
            it[4] = 0x20
            it[5] = 0x20
            it[6] = 0x4F
            it[7] = 0x35
        }
        val parsed = parseAirPodsData(data)
        assertEquals(AirPodsModel.AIRPODS_PRO_2, parsed?.model)
        assertEquals(40, parsed?.leftBattery)
        assertNull(parsed?.rightBattery)
        assertEquals(30, parsed?.caseBattery)
    }
}
