package com.example.docvaultyape.domain.model

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.util.Date

class DocumentTest {
    @Test
    fun `Document se crea con todos los campos correctamente`() {
        val date = Date()
        val doc = Document(
            id = "doc-1",
            name = "foto.jpg",
            type = DocumentType.IMAGE,
            encryptedFilePath = "/secure/doc-1.enc.img",
            thumbnailPath = null,
            sizeBytes = 2048L,
            createdAt = date,
            locationAddress = "Av. Lima 123",
            latitude = -12.0,
            longitude = -77.0
        )

        assertEquals("doc-1", doc.id)
        assertEquals("foto.jpg", doc.name)
        assertEquals(DocumentType.IMAGE, doc.type)
        assertEquals(2048L, doc.sizeBytes)
        assertEquals(date, doc.createdAt)
        assertEquals("Av. Lima 123", doc.locationAddress)
        assertEquals(-12.0, doc.latitude!!, 0.0001)
        assertEquals(-77.0, doc.longitude!!, 0.0001)
        assertTrue(doc.accessLogs.isEmpty())
    }

    @Test
    fun `Document permite campos opcionales nulos`() {
        val doc = Document(
            id = "doc-2",
            name = "doc.pdf",
            type = DocumentType.PDF,
            encryptedFilePath = "/secure/doc-2.enc.pdf",
            thumbnailPath = null,
            sizeBytes = 1024L,
            createdAt = Date(),
            locationAddress = null,
            latitude = null,
            longitude = null
        )

        assertNull(doc.thumbnailPath)
        assertNull(doc.locationAddress)
        assertNull(doc.latitude)
        assertNull(doc.longitude)
    }

    @Test
    fun `DocumentType tiene valores PDF e IMAGE`() {
        assertEquals(2, DocumentType.entries.size)
        assertTrue(DocumentType.entries.contains(DocumentType.PDF))
        assertTrue(DocumentType.entries.contains(DocumentType.IMAGE))
    }

    @Test
    fun `AccessLog se crea correctamente`() {
        val date = Date()
        val log = AccessLog(
            id = "log-1",
            documentId = "doc-1",
            accessedAt = date,
            action = AccessAction.VIEW
        )

        assertEquals("log-1", log.id)
        assertEquals("doc-1", log.documentId)
        assertEquals(date, log.accessedAt)
        assertEquals(AccessAction.VIEW, log.action)
    }

    @Test
    fun `AccessAction tiene valores VIEW y DELETE_ATTEMPT`() {
        assertEquals(2, AccessAction.entries.size)
        assertTrue(AccessAction.entries.contains(AccessAction.VIEW))
        assertTrue(AccessAction.entries.contains(AccessAction.DELETE_ATTEMPT))
    }

    @Test
    fun `Document copy funciona correctamente`() {
        val original = Document(
            id = "doc-1",
            name = "foto.jpg",
            type = DocumentType.IMAGE,
            encryptedFilePath = "/secure/enc",
            thumbnailPath = null,
            sizeBytes = 1024L,
            createdAt = Date(),
            locationAddress = null,
            latitude = null,
            longitude = null
        )

        val copy = original.copy(name = "foto_editada.jpg", sizeBytes = 2048L)

        assertEquals("doc-1", copy.id)
        assertEquals("foto_editada.jpg", copy.name)
        assertEquals(2048L, copy.sizeBytes)
        assertEquals(DocumentType.IMAGE, copy.type)
    }
}
