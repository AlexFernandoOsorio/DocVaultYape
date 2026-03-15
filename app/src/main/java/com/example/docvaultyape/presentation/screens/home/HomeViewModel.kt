package com.example.docvaultyape.presentation.screens.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docvaultyape.core.location.LocationManager
import com.example.docvaultyape.domain.model.DocumentType
import com.example.docvaultyape.domain.usecase.AddDocumentUseCase
import com.example.docvaultyape.domain.usecase.GetDocumentsUseCase
import com.example.docvaultyape.presentation.screens.home.interactor.HomeUiEvent
import com.example.docvaultyape.presentation.screens.home.interactor.HomeUiIntent
import com.example.docvaultyape.presentation.screens.home.interactor.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDocumentsUseCase: GetDocumentsUseCase,
    private val addDocumentUseCase: AddDocumentUseCase,
    private val locationManager: LocationManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _effect = Channel<HomeUiEvent>()
    val effect = _effect.receiveAsFlow()

    init { processIntent(HomeUiIntent.LoadDocuments) }

    fun processIntent(intent: HomeUiIntent) {
        when (intent) {
            is HomeUiIntent.LoadDocuments -> loadDocuments(null)
            is HomeUiIntent.FilterDocuments -> {
                _state.update { it.copy(selectedFilter = intent.type) }
                loadDocuments(intent.type)
            }
            is HomeUiIntent.ToggleAddOptions -> _state.update { it.copy(showAddOptions = !it.showAddOptions) }
            is HomeUiIntent.AddDocumentFromGallery -> addFromUri(intent.uri, intent.name)
            is HomeUiIntent.AddDocumentFromCamera -> addFromCameraUri(intent.uri, intent.name)
            is HomeUiIntent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadDocuments(filter: DocumentType?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getDocumentsUseCase(filter).collect { docs ->
                _state.update { it.copy(documents = docs, isLoading = false) }
            }
        }
    }

    private fun addFromUri(uri: Uri, name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showAddOptions = false) }
            runCatching {
                val tempFile = copyUriToTemp(uri, name)
                val type = if (name.endsWith(".pdf", true)) DocumentType.PDF else DocumentType.IMAGE
                val location = locationManager.getCurrentLocation()
                addDocumentUseCase(name, type, tempFile.absolutePath, location?.latitude, location?.longitude, location?.address)
                    .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
                tempFile.delete()
            }.onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    private fun addFromCameraUri(uri: Uri, name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showAddOptions = false) }
            runCatching {
                val tempFile = copyUriToTemp(uri, name)
                val location = locationManager.getCurrentLocation()
                addDocumentUseCase(
                    name = name,
                    type = DocumentType.IMAGE,
                    sourceFilePath = tempFile.absolutePath,
                    latitude = location?.latitude,
                    longitude = location?.longitude,
                    locationAddress = location?.address
                ).onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
                tempFile.delete()
            }.onFailure { e ->
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }


    private fun copyUriToTemp(uri: Uri, name: String): File {
        val tempFile = File(context.cacheDir, name)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
        }
        return tempFile
    }
}
