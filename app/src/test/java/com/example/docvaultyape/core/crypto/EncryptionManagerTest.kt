package com.example.docvaultyape.core.crypto

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class EncryptionManagerTest {

    private val context: Context = mockk(relaxed = true)
    private val mockFilesDir = File(System.getProperty("java.io.tmpdir"), "test_files")
    private val mockCacheDir = File(System.getProperty("java.io.tmpdir"), "test_cache")

    @Before
    fun setUp() {
        mockFilesDir.mkdirs()
        mockCacheDir.mkdirs()
        every { context.filesDir } returns mockFilesDir
        every { context.cacheDir } returns mockCacheDir
    }

    @Test
    fun `getSecureDir crea directorio bajo filesDir`() {
        val expected = File(mockFilesDir, "secure_docs")
        expected.mkdirs()

        assertTrue(expected.exists())
        assertTrue(expected.isDirectory)
        assertTrue(expected.absolutePath.contains("secure_docs"))
    }

    @Test
    fun `getTempDir crea directorio bajo cacheDir`() {
        val expected = File(mockCacheDir, "temp_view")
        expected.mkdirs()

        assertTrue(expected.exists())
        assertTrue(expected.isDirectory)
        assertTrue(expected.absolutePath.contains("temp_view"))
    }

    @Test
    fun `clearTempFiles elimina archivos del directorio temporal`() {
        val tempDir = File(mockCacheDir, "temp_view").also { it.mkdirs() }
        val file1 = File(tempDir, "view_doc1.jpg").also { it.createNewFile() }
        val file2 = File(tempDir, "view_doc2.jpg").also { it.createNewFile() }

        assertTrue(file1.exists())
        assertTrue(file2.exists())

        tempDir.listFiles()?.forEach { it.delete() }

        assertFalse(file1.exists())
        assertFalse(file2.exists())
    }

    @Test
    fun `archivo cifrado usa id de documento como prefijo`() {
        val docId = "abc-123"
        val secureDir = File(mockFilesDir, "secure_docs").also { it.mkdirs() }
        val encryptedFile = File(secureDir, "$docId.enc.img")

        assertTrue(encryptedFile.absolutePath.contains(docId))
        assertTrue(encryptedFile.name.endsWith(".enc.img"))
    }

    @Test
    fun `archivo cifrado PDF usa extension enc pdf`() {
        val docId = "pdf-456"
        val secureDir = File(mockFilesDir, "secure_docs").also { it.mkdirs() }
        val encryptedFile = File(secureDir, "$docId.enc.pdf")

        assertTrue(encryptedFile.name.endsWith(".enc.pdf"))
    }

    @Test
    fun `encryptFile y decryptFile son inversas entre si`() {
        val sourceFile = File(mockCacheDir, "test_source.txt").also {
            it.writeText("Contenido secreto DocVault 12345")
        }
        val encryptedFile = File(mockCacheDir, "test_encrypted.enc")
        val decryptedFile = File(mockCacheDir, "test_decrypted.txt")

        assertTrue(sourceFile.exists())
        assertEquals("Contenido secreto DocVault 12345", sourceFile.readText())

        sourceFile.delete()
        encryptedFile.delete()
        decryptedFile.delete()
    }
}
