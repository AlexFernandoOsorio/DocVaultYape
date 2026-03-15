package com.example.docvaultyape.presentation.screens.detail.interactor

sealed class DocumentDetailUiIntent {
    data class LoadDocument(val documentId: String) : DocumentDetailUiIntent()
    object RequestBiometricAuth : DocumentDetailUiIntent()
    data class OnBiometricSuccess(val documentId: String) : DocumentDetailUiIntent()
    object ShowDeleteConfirmation : DocumentDetailUiIntent()
    object DismissDeleteConfirmation : DocumentDetailUiIntent()
    data class ConfirmDelete(val documentId: String) : DocumentDetailUiIntent()
    object DismissError : DocumentDetailUiIntent()
}
