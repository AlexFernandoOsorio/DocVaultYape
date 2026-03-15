package com.example.docvaultyape.presentation.screens.home.interactor

sealed class HomeUiEvent {
    object LaunchGallery : HomeUiEvent()
    object LaunchCamera : HomeUiEvent()
    data class ShowError(val message: String) : HomeUiEvent()
}
