package com.example.docvaultyape.domain.usecase

import com.example.docvaultyape.domain.model.Document
import com.example.docvaultyape.domain.model.DocumentType
import com.example.docvaultyape.domain.repository.DocumentRepository
import javax.inject.Inject

class AddDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(
        name: String,
        type: DocumentType,
        sourceFilePath: String,
        latitude: Double?,
        longitude: Double?,
        locationAddress: String?
    ): Result<Document> = repository.addDocument(name, type, sourceFilePath, latitude, longitude, locationAddress)
}
