package com.example.docvaultyape.presentation.screens.home

import app.cash.turbine.test
import com.example.docvaultyape.core.location.LocationManager
import com.example.docvaultyape.domain.model.Document
import com.example.docvaultyape.domain.model.DocumentType
import com.example.docvaultyape.domain.usecase.AddDocumentUseCase
import com.example.docvaultyape.domain.usecase.GetDocumentsUseCase
import com.example.docvaultyape.presentation.screens.home.interactor.HomeUiIntent
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getDocumentsUseCase: GetDocumentsUseCase = mockk()
    private val addDocumentUseCase: AddDocumentUseCase = mockk()
    private val locationManager: LocationManager = mockk()
    private val context: android.content.Context = mockk(relaxed = true)
    private lateinit var viewModel: HomeViewModel

    private val fakeDocuments = listOf(
        Document("1", "foto.jpg", DocumentType.IMAGE, "/path", null, 512L, Date(), "Calle Lima 100", -12.0, -77.0),
        Document("2", "doc.pdf", DocumentType.PDF, "/path2", null, 1024L, Date(), null, null, null)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getDocumentsUseCase(any()) } returns flowOf(fakeDocuments)
        viewModel = HomeViewModel(getDocumentsUseCase, addDocumentUseCase, locationManager, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial carga todos los documentos`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(2, state.documents.size)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filtrar por PDF actualiza selectedFilter`() = runTest {
        every { getDocumentsUseCase(DocumentType.PDF) } returns flowOf(listOf(fakeDocuments[1]))

        viewModel.processIntent(HomeUiIntent.FilterDocuments(DocumentType.PDF))

        assertEquals(DocumentType.PDF, viewModel.state.value.selectedFilter)
    }

    @Test
    fun `filtrar por null muestra todos los documentos`() = runTest {
        every { getDocumentsUseCase(null) } returns flowOf(fakeDocuments)

        viewModel.processIntent(HomeUiIntent.FilterDocuments(null))

        assertNull(viewModel.state.value.selectedFilter)
    }

    @Test
    fun `toggle add options cambia showAddOptions`() = runTest {
        assertFalse(viewModel.state.value.showAddOptions)
        viewModel.processIntent(HomeUiIntent.ToggleAddOptions)
        assertTrue(viewModel.state.value.showAddOptions)
        viewModel.processIntent(HomeUiIntent.ToggleAddOptions)
        assertFalse(viewModel.state.value.showAddOptions)
    }

    @Test
    fun `dismiss error limpia el error del estado`() = runTest {
        viewModel.processIntent(HomeUiIntent.DismissError)
        assertNull(viewModel.state.value.error)
    }
}
