package com.example.docvaultyape.domain.usecase

import com.example.docvaultyape.domain.model.AccessLog
import com.example.docvaultyape.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccessLogsUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(documentId: String): Flow<List<AccessLog>> =
        repository.getAccessLogs(documentId)
}
