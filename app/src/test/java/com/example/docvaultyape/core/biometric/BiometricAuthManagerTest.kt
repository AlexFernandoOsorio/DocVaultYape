package com.example.docvaultyape.core.biometric

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BiometricAuthManagerTest {

    @Test
    fun `BiometricResult Success es instancia correcta`() {
        val result: BiometricResult = BiometricResult.Success
        assertTrue(result is BiometricResult.Success)
    }

    @Test
    fun `BiometricResult Error contiene mensaje`() {
        val result: BiometricResult = BiometricResult.Error("Huella no reconocida")
        assertTrue(result is BiometricResult.Error)
        assertEquals("Huella no reconocida", (result as BiometricResult.Error).message)
    }

    @Test
    fun `BiometricResult UserCancelled es instancia correcta`() {
        val result: BiometricResult = BiometricResult.UserCancelled
        assertTrue(result is BiometricResult.UserCancelled)
    }

    @Test
    fun `BiometricResult NotAvailable es instancia correcta`() {
        val result: BiometricResult = BiometricResult.NotAvailable
        assertTrue(result is BiometricResult.NotAvailable)
    }

    @Test
    fun `BiometricResult tipos son distintos entre si`() {
        val success = BiometricResult.Success
        val error = BiometricResult.Error("error")
        val cancelled = BiometricResult.UserCancelled
        val notAvailable = BiometricResult.NotAvailable

        assertNotEquals(success, error)
        assertNotEquals(success, cancelled)
        assertNotEquals(success, notAvailable)
        assertNotEquals(error, cancelled)
    }

    @Test
    fun `BiometricAuthManager se puede instanciar`() {
        val manager = BiometricAuthManager()
        assertNotNull(manager)
    }
}
