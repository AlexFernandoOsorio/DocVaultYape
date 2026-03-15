package com.example.docvaultyape.domain.model

import java.util.Date

enum class DocumentType { PDF, IMAGE }

data class Document(
    val id: String,
    val name: String,
    val type: DocumentType,
    val encryptedFilePath: String,
    val thumbnailPath: String?,
    val sizeBytes: Long,
    val createdAt: Date,
    val locationAddress: String?,
    val latitude: Double?,
    val longitude: Double?,
    val accessLogs: List<AccessLog> = emptyList()
)

data class AccessLog(
    val id: String,
    val documentId: String,
    val accessedAt: Date,
    val action: AccessAction
)

enum class AccessAction { VIEW, DELETE_ATTEMPT }
