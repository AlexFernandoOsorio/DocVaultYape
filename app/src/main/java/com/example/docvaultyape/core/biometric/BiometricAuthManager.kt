package com.example.docvaultyape.core.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthManager @Inject constructor() {

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onResult: (BiometricResult) -> Unit
    ) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) =
                onResult(BiometricResult.Success)

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onResult(
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) BiometricResult.UserCancelled else BiometricResult.Error(
                        errString.toString()
                    )
                )
            }

            override fun onAuthenticationFailed() {}
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle(title).setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback).authenticate(
            promptInfo
        )
    }
}
