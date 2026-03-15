package com.example.docvaultyape.core.biometric

sealed class BiometricResult {
    object Success : BiometricResult()
    data class Error(val message: String) : BiometricResult()
    object NotAvailable : BiometricResult()
    object UserCancelled : BiometricResult()
}
