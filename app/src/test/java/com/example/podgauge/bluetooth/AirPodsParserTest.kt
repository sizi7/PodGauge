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
        val data = ByteArray(27).also { it[0] = 0x07; it[1] = 0x19 }
        val parsed = parseAirPodsData(data)
        assertTrue(isAirPodsPacket(data))
        assertEquals(AirPodsModel.UNKNOWN, parsed?.model)
        assertNull(parsed?.leftBattery)
    }

    @Test fun validatesBatteryRange() {
        assertEquals(0, validBatteryOrNull(0))
        assertEquals(100, validBatteryOrNull(100))
        assertNull(validBatteryOrNull(-1))
        assertNull(validBatteryOrNull(101))
    }
}
