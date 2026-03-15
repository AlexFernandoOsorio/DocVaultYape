package com.example.docvaultyape.domain.usecase

import com.example.docvaultyape.domain.model.AccessAction
import com.example.docvaultyape.domain.model.AccessLog
import com.example.docvaultyape.domain.repository.DocumentRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class GetDecryptedFileUseCaseTest {

    private val repository: DocumentRepository = mockk()
    private lateinit var useCase: GetDecryptedFileUseCase

    @Before
    fun setUp() {
        useCase = GetDecryptedFileUseCase(repository)
    }

    @Test
    fun `invoke descifra archivo y registra acceso exitosamente`() = runTest {
        coEvery { repository.getDecryptedFilePath("doc-1") } returns Result.success("/cache/view_doc-1.jpg")
        coEvery { repository.logAccess(eq("doc-1"), any()) } just Runs

        val result = useCase("doc-1")

        assertTrue(result.isSuccess)
        assertEquals("/cache/view_doc-1.jpg", result.getOrNull())
        coVerify(exactly = 1) { repository.logAccess(eq("doc-1"), any()) }
    }

    @Test
    fun `invoke registra accion VIEW en el log de acceso`() = runTest {
        coEvery { repository.getDecryptedFilePath("doc-1") } returns Result.success("/cache/view_doc-1.jpg")
        val logSlot = slot<AccessLog>()
        coEvery { repository.logAccess(any(), capture(logSlot)) } just Runs

        useCase("doc-1")

        assertEquals(AccessAction.VIEW, logSlot.captured.action)
        assertEquals("doc-1", logSlot.captured.documentId)
    }

    @Test
    fun `invoke NO registra acceso cuando el descifrado falla`() = runTest {
        coEvery { repository.getDecryptedFilePath("doc-1") } returns Result.failure(Exception("Error al descifrar"))

        val result = useCase("doc-1")

        assertTrue(result.isFailure)
        assertEquals("Error al descifrar", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.logAccess(any(), any()) }
    }

    @Test
    fun `invoke retorna failure cuando documento no existe`() = runTest {
        coEvery { repository.getDecryptedFilePath("no-existe") } returns Result.failure(
            IllegalArgumentException("Document not found")
        )

        val result = useCase("no-existe")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `invoke genera id unico para cada log de acceso`() = runTest {
        coEvery { repository.getDecryptedFilePath(any()) } returns Result.success("/cache/file.jpg")
        val logs = mutableListOf<AccessLog>()
        coEvery { repository.logAccess(any(), capture(logs)) } just Runs

        useCase("doc-1")
        useCase("doc-1")

        assertEquals(2, logs.size)
        assertNotEquals(logs[0].id, logs[1].id)
    }
}
