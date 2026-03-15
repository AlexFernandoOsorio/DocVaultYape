package com.example.docvaultyape.presentation.screens.home.interactor

import android.net.Uri
import com.example.docvaultyape.domain.model.DocumentType

sealed class HomeUiIntent {
    object LoadDocuments : HomeUiIntent()
    data class FilterDocuments(val type: DocumentType?) : HomeUiIntent()
    data class AddDocumentFromGallery(val uri: Uri, val name: String) : HomeUiIntent()
    data class AddDocumentFromCamera(val uri: Uri, val name: String) : HomeUiIntent()
    object ToggleAddOptions : HomeUiIntent()
    object DismissError : HomeUiIntent()
}
