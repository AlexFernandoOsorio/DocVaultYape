package com.example.docvaultyape.fake

import com.example.docvaultyape.domain.model.AccessLog
import com.example.docvaultyape.domain.model.Document
import com.example.docvaultyape.domain.model.DocumentType
import com.example.docvaultyape.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID

class FakeDocumentRepository : DocumentRepository {

    private val documents = MutableStateFlow<List<Document>>(emptyList())
    private val accessLogs = MutableStateFlow<List<AccessLog>>(emptyList())

    val fakeDocuments = listOf(
        Document(
            id = "doc-1",
            name = "foto_test.jpg",
            type = DocumentType.IMAGE,
            encryptedFilePath = "/fake/doc-1.enc.img",
            thumbnailPath = null,
            sizeBytes = 1024L,
            createdAt = Date(),
            locationAddress = "Av. Lima 123, Miraflores",
            latitude = -12.0,
            longitude = -77.0
        ),
        Document(
            id = "doc-2",
            name = "reporte_test.pdf",
            type = DocumentType.PDF,
            encryptedFilePath = "/fake/doc-2.enc.pdf",
            thumbnailPath = null,
            sizeBytes = 2048L,
            createdAt = Date(),
            locationAddress = null,
            latitude = null,
            longitude = null
        )
    )

    init {
        documents.value = fakeDocuments
    }

    override fun getAllDocuments(): Flow<List<Document>> = documents

    override fun getDocumentsByType(type: DocumentType): Flow<List<Document>> =
        documents.map { list -> list.filter { it.type == type } }

    override suspend fun getDocumentById(id: String): Document? =
        documents.value.find { it.id == id }

    override suspend fun addDocument(
        name: String,
        type: DocumentType,
        sourceFilePath: String,
        latitude: Double?,
        longitude: Double?,
        locationAddress: String?
    ): Result<Document> {
        val doc = Document(
            id = UUID.randomUUID().toString(),
            name = name,
            type = type,
            encryptedFilePath = "/fake/$name.enc",
            thumbnailPath = null,
            sizeBytes = 512L,
            createdAt = Date(),
            locationAddress = locationAddress,
            latitude = latitude,
            longitude = longitude
        )
        documents.value = documents.value + doc
        return Result.success(doc)
    }

    override suspend fun deleteDocument(id: String): Result<Unit> {
        documents.value = documents.value.filter { it.id != id }
        return Result.success(Unit)
    }

    override suspend fun logAccess(documentId: String, accessLog: AccessLog) {
        accessLogs.value = accessLogs.value + accessLog
    }

    override fun getAccessLogs(documentId: String): Flow<List<AccessLog>> =
        accessLogs.map { list -> list.filter { it.documentId == documentId } }

    override suspend fun getDecryptedFilePath(documentId: String): Result<String> =
        Result.success("/fake/decrypted_$documentId.jpg")

    fun clearDocuments() {
        documents.value = emptyList()
    }

    fun setDocuments(docs: List<Document>) {
        documents.value = docs
    }
}