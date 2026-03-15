package com.example.docvaultyape.presentation.screens.detail.interactor

sealed class DocumentDetailUiEvent {
    object LaunchBiometric : DocumentDetailUiEvent()
    object NavigateBack : DocumentDetailUiEvent()
    data class ShowError(val message: String) : DocumentDetailUiEvent()
}
