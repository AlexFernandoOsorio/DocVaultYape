package com.example.docvaultyape.domain.usecase

import com.example.docvaultyape.domain.repository.DocumentRepository
import javax.inject.Inject

class DeleteDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(documentId: String): Result<Unit> =
        repository.deleteDocument(documentId)
}
