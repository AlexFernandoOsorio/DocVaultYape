package com.example.docvaultyape.domain.usecase

import com.example.docvaultyape.domain.repository.DocumentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteDocumentUseCaseTest {

    private val repository: DocumentRepository = mockk()
    private lateinit var useCase: DeleteDocumentUseCase

    @Before
    fun setUp() {
        useCase = DeleteDocumentUseCase(repository)
    }

    @Test
    fun `invoke elimina documento exitosamente`() = runTest {
        coEvery { repository.deleteDocument("doc-1") } returns Result.success(Unit)

        val result = useCase("doc-1")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.deleteDocument("doc-1") }
    }

    @Test
    fun `invoke retorna failure cuando el repositorio falla`() = runTest {
        coEvery { repository.deleteDocument("doc-1") } returns Result.failure(Exception("Archivo no encontrado"))

        val result = useCase("doc-1")

        assertTrue(result.isFailure)
        assertEquals("Archivo no encontrado", result.exceptionOrNull()?.message)
    }
}
