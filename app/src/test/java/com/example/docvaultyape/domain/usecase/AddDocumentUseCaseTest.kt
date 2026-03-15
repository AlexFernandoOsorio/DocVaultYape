package com.example.docvaultyape.domain.usecase

import com.example.docvaultyape.domain.model.Document
import com.example.docvaultyape.domain.model.DocumentType
import com.example.docvaultyape.domain.repository.DocumentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class AddDocumentUseCaseTest {

    private val repository: DocumentRepository = mockk()
    private lateinit var useCase: AddDocumentUseCase

    private val fakeDocument = Document(
        id = "doc-1",
        name = "foto.jpg",
        type = DocumentType.IMAGE,
        encryptedFilePath = "/secure/enc",
        thumbnailPath = null,
        sizeBytes = 1024L,
        createdAt = Date(),
        locationAddress = "Av. Lima 123",
        latitude = -12.0,
        longitude = -77.0
    )

    @Before
    fun setUp() {
        useCase = AddDocumentUseCase(repository)
    }

    @Test
    fun `invoke agrega documento imagen exitosamente`() = runTest {
        coEvery {
            repository.addDocument(
                name = "foto.jpg",
                type = DocumentType.IMAGE,
                sourceFilePath = "/temp/foto.jpg",
                latitude = -12.0,
                longitude = -77.0,
                locationAddress = "Av. Lima 123"
            )
        } returns Result.success(fakeDocument)

        val result = useCase(
            name = "foto.jpg",
            type = DocumentType.IMAGE,
            sourceFilePath = "/temp/foto.jpg",
            latitude = -12.0,
            longitude = -77.0,
            locationAddress = "Av. Lima 123"
        )

        assertTrue(result.isSuccess)
        assertEquals("doc-1", result.getOrNull()?.id)
        assertEquals(DocumentType.IMAGE, result.getOrNull()?.type)
        coVerify(exactly = 1) {
            repository.addDocument(
                name = "foto.jpg",
                type = DocumentType.IMAGE,
                sourceFilePath = "/temp/foto.jpg",
                latitude = -12.0,
                longitude = -77.0,
                locationAddress = "Av. Lima 123"
            )
        }
    }

    @Test
    fun `invoke agrega documento PDF exitosamente`() = runTest {
        val fakePdf = fakeDocument.copy(
            id = "doc-2",
            name = "reporte.pdf",
            type = DocumentType.PDF
        )
        coEvery {
            repository.addDocument(
                name = "reporte.pdf",
                type = DocumentType.PDF,
                sourceFilePath = "/temp/reporte.pdf",
                latitude = null,
                longitude = null,
                locationAddress = null
            )
        } returns Result.success(fakePdf)

        val result = useCase(
            name = "reporte.pdf",
            type = DocumentType.PDF,
            sourceFilePath = "/temp/reporte.pdf",
            latitude = null,
            longitude = null,
            locationAddress = null
        )

        assertTrue(result.isSuccess)
        assertEquals(DocumentType.PDF, result.getOrNull()?.type)
    }

    @Test
    fun `invoke retorna failure cuando el repositorio falla`() = runTest {
        coEvery {
            repository.addDocument(any(), any(), any(), any(), any(), any())
        } returns Result.failure(Exception("Error al cifrar"))

        val result = useCase(
            name = "foto.jpg",
            type = DocumentType.IMAGE,
            sourceFilePath = "/temp/foto.jpg",
            latitude = null,
            longitude = null,
            locationAddress = null
        )

        assertTrue(result.isFailure)
        assertEquals("Error al cifrar", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke con ubicacion nula agrega documento exitosamente`() = runTest {
        val docSinUbicacion = fakeDocument.copy(
            locationAddress = null,
            latitude = null,
            longitude = null
        )
        coEvery {
            repository.addDocument(
                name = any(),
                type = any(),
                sourceFilePath = any(),
                latitude = null,
                longitude = null,
                locationAddress = null
            )
        } returns Result.success(docSinUbicacion)

        val result = useCase(
            name = "foto.jpg",
            type = DocumentType.IMAGE,
            sourceFilePath = "/temp/foto.jpg",
            latitude = null,
            longitude = null,
            locationAddress = null
        )

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull()?.locationAddress)
        assertNull(result.getOrNull()?.latitude)
        assertNull(result.getOrNull()?.longitude)
    }
}
