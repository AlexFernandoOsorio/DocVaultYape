package com.example.docvaultyape.presentation.screens.detail.interactor

import com.example.docvaultyape.domain.model.AccessLog
import com.example.docvaultyape.domain.model.Document

data class DocumentDetailUiState (
    val document: Document? = null,
    val decryptedFilePath: String? = null,
    val accessLogs: List<AccessLog> = emptyList(),
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val isDeleted: Boolean = false
)
