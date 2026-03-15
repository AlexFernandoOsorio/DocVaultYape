package com.example.docvaultyape.domain.usecase

import com.example.docvaultyape.domain.model.AccessAction
import com.example.docvaultyape.domain.model.AccessLog
import com.example.docvaultyape.domain.repository.DocumentRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class GetDecryptedFileUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(documentId: String): Result<String> {
        val result = repository.getDecryptedFilePath(documentId)
        if (result.isSuccess) {
            repository.logAccess(
                documentId,
                AccessLog(UUID.randomUUID().toString(), documentId, Date(), AccessAction.VIEW)
            )
        }
        return result
    }
}
