package com.example.docvaultyape.core.location

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class LocationManagerTest {

    @Test
    fun `LocationData contiene latitud longitud y direccion`() {
        val data = LocationData(
            latitude = -12.046374,
            longitude = -77.042793,
            address = "Av. Larco 1301, Miraflores, Lima"
        )

        assertEquals(-12.046374, data.latitude, 0.000001)
        assertEquals(-77.042793, data.longitude, 0.000001)
        assertEquals("Av. Larco 1301, Miraflores, Lima", data.address)
    }

    @Test
    fun `LocationData permite direccion nula`() {
        val data = LocationData(
            latitude = -12.0,
            longitude = -77.0,
            address = null
        )

        assertNull(data.address)
    }

    @Test
    fun `LocationData con coordenadas cero es valida`() {
        val data = LocationData(
            latitude = 0.0,
            longitude = 0.0,
            address = null
        )

        assertEquals(0.0, data.latitude, 0.0)
        assertEquals(0.0, data.longitude, 0.0)
    }
}
