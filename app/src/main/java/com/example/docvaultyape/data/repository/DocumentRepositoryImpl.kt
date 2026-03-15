package com.example.docvaultyape.data.repository

import com.example.docvaultyape.core.crypto.EncryptionManager
import com.example.docvaultyape.data.local.dao.DocumentDao
import com.example.docvaultyape.data.local.entity.AccessLogEntity
import com.example.docvaultyape.data.local.entity.DocumentEntity
import com.example.docvaultyape.domain.model.AccessAction
import com.example.docvaultyape.domain.model.AccessLog
import com.example.docvaultyape.domain.model.Document
import com.example.docvaultyape.domain.model.DocumentType
import com.example.docvaultyape.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepositoryImpl @Inject constructor(
    private val dao: DocumentDao,
    private val encryptionManager: EncryptionManager
) : DocumentRepository {

    override fun getAllDocuments(): Flow<List<Document>> =
        dao.getAllDocuments().map { it.map { e -> e.toDomain() } }

    override fun getDocumentsByType(type: DocumentType): Flow<List<Document>> =
        dao.getDocumentsByType(type.name).map { it.map { e -> e.toDomain() } }

    override suspend fun getDocumentById(id: String): Document? = dao.getDocumentById(id)?.toDomain()

    override suspend fun addDocument(name: String, type: DocumentType, sourceFilePath: String,
        latitude: Double?, longitude: Double?, locationAddress: String?): Result<Document> = runCatching {
        val id = UUID.randomUUID().toString()
        val ext = if (type == DocumentType.PDF) ".enc.pdf" else ".enc.img"
        val encryptedFile = File(encryptionManager.getSecureDir(), "$id$ext")
        val sourceFile = File(sourceFilePath)
        encryptionManager.encryptFile(sourceFile, encryptedFile).getOrThrow()
        val entity = DocumentEntity(id, name, type.name, encryptedFile.absolutePath,
            null, sourceFile.length(), System.currentTimeMillis(), locationAddress, latitude, longitude)
        dao.insertDocument(entity)
        entity.toDomain()
    }

    override suspend fun deleteDocument(id: String): Result<Unit> = runCatching {
        dao.getDocumentById(id)?.let { File(it.encryptedFilePath).delete() }
        dao.deleteDocumentById(id)
    }

    override suspend fun logAccess(documentId: String, accessLog: AccessLog) =
        dao.insertAccessLog(accessLog.toEntity())

    override fun getAccessLogs(documentId: String): Flow<List<AccessLog>> =
        dao.getAccessLogs(documentId).map { it.map { e -> e.toDomain() } }

    override suspend fun getDecryptedFilePath(documentId: String): Result<String> = runCatching {
        val entity = dao.getDocumentById(documentId) ?: throw IllegalArgumentException("Document not found")
        val ext = if (entity.type == DocumentType.PDF.name) ".pdf" else ".jpg"
        val tempFile = File(encryptionManager.getTempDir(), "view_$documentId$ext")
        encryptionManager.decryptFile(File(entity.encryptedFilePath), tempFile).getOrThrow()
        tempFile.absolutePath
    }

    private fun DocumentEntity.toDomain() = Document(id, name, DocumentType.valueOf(type),
        encryptedFilePath, thumbnailPath, sizeBytes, Date(createdAt), locationAddress, latitude, longitude)
    private fun AccessLogEntity.toDomain() = AccessLog(id, documentId, Date(accessedAt), AccessAction.valueOf(action))
    private fun AccessLog.toEntity() = AccessLogEntity(id, documentId, accessedAt.time, action.name)
}
