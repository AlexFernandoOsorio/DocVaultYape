package com.example.docvaultyape.domain.usecase

import com.example.docvaultyape.domain.model.Document
import com.example.docvaultyape.domain.model.DocumentType
import com.example.docvaultyape.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDocumentsUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(filter: DocumentType? = null): Flow<List<Document>> =
        if (filter == null) repository.getAllDocuments()
        else repository.getDocumentsByType(filter)
}
