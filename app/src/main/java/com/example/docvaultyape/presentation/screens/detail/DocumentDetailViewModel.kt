package com.example.docvaultyape.presentation.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docvaultyape.domain.usecase.DeleteDocumentUseCase
import com.example.docvaultyape.domain.usecase.GetAccessLogsUseCase
import com.example.docvaultyape.domain.usecase.GetDecryptedFileUseCase
import com.example.docvaultyape.domain.usecase.GetDocumentsUseCase
import com.example.docvaultyape.presentation.screens.detail.interactor.DocumentDetailUiEvent
import com.example.docvaultyape.presentation.screens.detail.interactor.DocumentDetailUiIntent
import com.example.docvaultyape.presentation.screens.detail.interactor.DocumentDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    private val getDocumentsUseCase: GetDocumentsUseCase,
    private val getDecryptedFileUseCase: GetDecryptedFileUseCase,
    private val deleteDocumentUseCase: DeleteDocumentUseCase,
    private val getAccessLogsUseCase: GetAccessLogsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DocumentDetailUiState())
    val state: StateFlow<DocumentDetailUiState> = _state.asStateFlow()

    private val _effect = Channel<DocumentDetailUiEvent>()
    val effect = _effect.receiveAsFlow()

    fun processIntent(intent: DocumentDetailUiIntent) {
        when (intent) {
            is DocumentDetailUiIntent.LoadDocument -> loadDocument(intent.documentId)
            is DocumentDetailUiIntent.RequestBiometricAuth -> viewModelScope.launch { _effect.send(
                DocumentDetailUiEvent.LaunchBiometric) }
            is DocumentDetailUiIntent.OnBiometricSuccess -> decryptAndLoad(intent.documentId)
            is DocumentDetailUiIntent.ShowDeleteConfirmation -> _state.update { it.copy(showDeleteConfirmation = true) }
            is DocumentDetailUiIntent.DismissDeleteConfirmation -> _state.update { it.copy(showDeleteConfirmation = false) }
            is DocumentDetailUiIntent.ConfirmDelete -> deleteDocument(intent.documentId)
            is DocumentDetailUiIntent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadDocument(documentId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getDocumentsUseCase(null).take(1).collect { docs ->
                val doc = docs.find { it.id == documentId }
                _state.update { it.copy(document = doc, isLoading = false) }
            }
        }
        viewModelScope.launch {
            getAccessLogsUseCase(documentId).collect { logs ->
                _state.update { it.copy(accessLogs = logs) }
            }
        }
    }

    private fun decryptAndLoad(documentId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getDecryptedFileUseCase(documentId)
                .onSuccess { path -> _state.update { it.copy(decryptedFilePath = path, isAuthenticated = true, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    private fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showDeleteConfirmation = false) }
            deleteDocumentUseCase(documentId)
                .onSuccess {
                    _state.update { it.copy(isDeleted = true, isLoading = false) }
                    _effect.send(DocumentDetailUiEvent.NavigateBack)
                }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }
}
