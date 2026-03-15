package com.example.docvaultyape.domain.repository

import com.example.docvaultyape.domain.model.AccessLog
import com.example.docvaultyape.domain.model.Document
import com.example.docvaultyape.domain.model.DocumentType
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getAllDocuments(): Flow<List<Document>>
    fun getDocumentsByType(type: DocumentType): Flow<List<Document>>
    suspend fun getDocumentById(id: String): Document?
    suspend fun addDocument(
        name: String,
        type: DocumentType,
        sourceFilePath: String,
        latitude: Double?,
        longitude: Double?,
        locationAddress: String?
    ): Result<Document>
    suspend fun deleteDocument(id: String): Result<Unit>
    suspend fun logAccess(documentId: String, accessLog: AccessLog)
    fun getAccessLogs(documentId: String): Flow<List<AccessLog>>
    suspend fun getDecryptedFilePath(documentId: String): Result<String>
}
