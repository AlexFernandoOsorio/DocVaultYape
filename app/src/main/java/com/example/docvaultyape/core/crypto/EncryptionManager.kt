package com.example.docvaultyape.core.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEY_ALIAS = "docvault_aes_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_SIZE = 12
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun getOrCreateKey(): SecretKey {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(false)
                    .build()
            )
            keyGenerator.generateKey()
        }
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    fun encryptFile(sourceFile: File, destFile: File): Result<Unit> = runCatching {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        FileInputStream(sourceFile).use { input ->
            FileOutputStream(destFile).use { output ->
                output.write(iv)
                val encrypted = cipher.doFinal(input.readBytes())
                output.write(encrypted)
            }
        }
    }

    fun decryptFile(encryptedFile: File, destFile: File): Result<Unit> = runCatching {
        FileInputStream(encryptedFile).use { input ->
            val iv = ByteArray(IV_SIZE).also { input.read(it) }
            val encryptedData = input.readBytes()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
            FileOutputStream(destFile).use { it.write(cipher.doFinal(encryptedData)) }
        }
    }

    fun getSecureDir(): File = File(context.filesDir, "secure_docs").also { if (!it.exists()) it.mkdirs() }
    fun getTempDir(): File = File(context.cacheDir, "temp_view").also { if (!it.exists()) it.mkdirs() }
    fun clearTempFiles() = getTempDir().listFiles()?.forEach { it.delete() }
}
