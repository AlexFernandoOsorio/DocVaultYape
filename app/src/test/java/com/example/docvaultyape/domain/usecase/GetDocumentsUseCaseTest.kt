package com.example.docvaultyape.domain.usecase

import app.cash.turbine.test
import com.example.docvaultyape.domain.model.Document
import com.example.docvaultyape.domain.model.DocumentType
import com.example.docvaultyape.domain.repository.DocumentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date

class GetDocumentsUseCaseTest {

    private val repository: DocumentRepository = mockk()
    private lateinit var useCase: GetDocumentsUseCase

    private val fakeDocuments = listOf(
        Document("1", "foto.jpg", DocumentType.IMAGE, "/path/enc1", null, 1024L, Date(), "Av. Lima 123", -12.0, -77.0),
        Document("2", "reporte.pdf", DocumentType.PDF, "/path/enc2", null, 2048L, Date(), null, null, null)
    )

    @Before
    fun setUp() {
        useCase = GetDocumentsUseCase(repository)
    }

    @Test
    fun `invoke sin filtro retorna todos los documentos`() = runTest {
        every { repository.getAllDocuments() } returns flowOf(fakeDocuments)

        useCase(null).test {
            val result = awaitItem()
            assertEquals(2, result.size)
            awaitComplete()
        }

        verify(exactly = 1) { repository.getAllDocuments() }
    }

    @Test
    fun `invoke con filtro PDF retorna solo PDFs`() = runTest {
        every { repository.getDocumentsByType(DocumentType.PDF) } returns flowOf(listOf(fakeDocuments[1]))

        useCase(DocumentType.PDF).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(DocumentType.PDF, result.first().type)
            awaitComplete()
        }
    }

    @Test
    fun `invoke con filtro IMAGE retorna solo imagenes`() = runTest {
        every { repository.getDocumentsByType(DocumentType.IMAGE) } returns flowOf(listOf(fakeDocuments[0]))

        useCase(DocumentType.IMAGE).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(DocumentType.IMAGE, result.first().type)
            awaitComplete()
        }
    }

    @Test
    fun `invoke retorna lista vacia cuando no hay documentos`() = runTest {
        every { repository.getAllDocuments() } returns flowOf(emptyList())

        useCase(null).test {
            assertEquals(0, awaitItem().size)
            awaitComplete()
        }
    }
}
