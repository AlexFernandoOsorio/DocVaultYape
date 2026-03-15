package com.example.docvaultyape.presentation.screens.home.interactor

import com.example.docvaultyape.domain.model.Document
import com.example.docvaultyape.domain.model.DocumentType

data class HomeUiState(
    val documents: List<Document> = emptyList(),
    val selectedFilter: DocumentType? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddOptions: Boolean = false
)
