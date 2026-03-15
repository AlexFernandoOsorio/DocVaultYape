package com.example.docvaultyape.presentation.screens.detail

import app.cash.turbine.test
import com.example.docvaultyape.domain.model.AccessLog
import com.example.docvaultyape.domain.model.AccessAction
import com.example.docvaultyape.domain.model.Document
import com.example.docvaultyape.domain.model.DocumentType
import com.example.docvaultyape.domain.usecase.DeleteDocumentUseCase
import com.example.docvaultyape.domain.usecase.GetAccessLogsUseCase
import com.example.docvaultyape.domain.usecase.GetDecryptedFileUseCase
import com.example.docvaultyape.domain.usecase.GetDocumentsUseCase
import com.example.docvaultyape.presentation.screens.detail.interactor.DocumentDetailUiEvent
import com.example.docvaultyape.presentation.screens.detail.interactor.DocumentDetailUiIntent
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getDocumentsUseCase: GetDocumentsUseCase = mockk()
    private val getDecryptedFileUseCase: GetDecryptedFileUseCase = mockk()
    private val deleteDocumentUseCase: DeleteDocumentUseCase = mockk()
    private val getAccessLogsUseCase: GetAccessLogsUseCase = mockk()
    private lateinit var viewModel: DocumentDetailViewModel

    private val fakeDoc = Document(
        "doc-1", "foto.jpg", DocumentType.IMAGE,
        "/secure/enc", null, 1024L, Date(),
        "Av. Larco 123", -12.1, -77.0
    )

    private val fakeLogs = listOf(
        AccessLog("log-1", "doc-1", Date(), AccessAction.VIEW)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getDocumentsUseCase(any()) } returns flowOf(listOf(fakeDoc))
        every { getAccessLogsUseCase(any()) } returns flowOf(fakeLogs)
        viewModel = DocumentDetailViewModel(
            getDocumentsUseCase,
            getDecryptedFileUseCase,
            deleteDocumentUseCase,
            getAccessLogsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LoadDocument carga el documento y los logs`() = runTest {
        viewModel.processIntent(DocumentDetailUiIntent.LoadDocument("doc-1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("doc-1", state.document?.id)
        assertEquals(1, state.accessLogs.size)
    }

    @Test
    fun `RequestBiometricAuth emite efecto LaunchBiometric`() = runTest {
        viewModel.effect.test {
            viewModel.processIntent(DocumentDetailUiIntent.RequestBiometricAuth)
            assertEquals(DocumentDetailUiEvent.LaunchBiometric, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnBiometricSuccess descifra y autentica`() = runTest {
        coEvery { getDecryptedFileUseCase("doc-1") } returns Result.success("/cache/view_doc-1.jpg")

        viewModel.processIntent(DocumentDetailUiIntent.OnBiometricSuccess("doc-1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isAuthenticated)
        assertEquals("/cache/view_doc-1.jpg", state.decryptedFilePath)
    }

    @Test
    fun `OnBiometricSuccess con error actualiza estado de error`() = runTest {
        coEvery { getDecryptedFileUseCase("doc-1") } returns Result.failure(Exception("Error al descifrar"))

        viewModel.processIntent(DocumentDetailUiIntent.OnBiometricSuccess("doc-1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isAuthenticated)
        assertEquals("Error al descifrar", state.error)
    }

    @Test
    fun `ShowDeleteConfirmation actualiza showDeleteConfirmation`() = runTest {
        viewModel.processIntent(DocumentDetailUiIntent.ShowDeleteConfirmation)
        assertTrue(viewModel.state.value.showDeleteConfirmation)
    }

    @Test
    fun `DismissDeleteConfirmation oculta el dialogo`() = runTest {
        viewModel.processIntent(DocumentDetailUiIntent.ShowDeleteConfirmation)
        viewModel.processIntent(DocumentDetailUiIntent.DismissDeleteConfirmation)
        assertFalse(viewModel.state.value.showDeleteConfirmation)
    }

    @Test
    fun `ConfirmDelete elimina y navega atras`() = runTest {
        coEvery { deleteDocumentUseCase("doc-1") } returns Result.success(Unit)

        viewModel.effect.test {
            viewModel.processIntent(DocumentDetailUiIntent.ConfirmDelete("doc-1"))
            advanceUntilIdle()
            assertEquals(DocumentDetailUiEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(viewModel.state.value.isDeleted)
    }

    @Test
    fun `ConfirmDelete con error actualiza estado de error`() = runTest {
        coEvery { deleteDocumentUseCase("doc-1") } returns Result.failure(Exception("No se pudo eliminar"))

        viewModel.processIntent(DocumentDetailUiIntent.ConfirmDelete("doc-1"))
        advanceUntilIdle()

        assertEquals("No se pudo eliminar", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isDeleted)
    }

    @Test
    fun `DismissError limpia el error`() = runTest {
        viewModel.processIntent(DocumentDetailUiIntent.DismissError)
        assertNull(viewModel.state.value.error)
    }
}
