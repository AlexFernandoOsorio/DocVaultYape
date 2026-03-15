package com.example.docvaultyape.presentation.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.docvaultyape.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun homeScreen_muestraEncabezadoDocVault() {
        composeRule
            .onNodeWithText("DocVault")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_muestraContadorDeDocumentos() {
        composeRule
            .onNodeWithText("2 documentos cifrados")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_muestraFiltroTodos() {
        composeRule
            .onNodeWithText("Todos")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_muestraFiltroPDF() {
        composeRule
            .onNodeWithText("PDF")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_muestraFiltroImagenes() {
        composeRule
            .onNodeWithText("Imágenes")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_muestraDocumentosEnLista() {
        composeRule
            .onNodeWithText("foto_test.jpg")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("reporte_test.pdf")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_muestraUbicacionDelDocumento() {
        composeRule
            .onNodeWithText("Av. Lima 123, Miraflores")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_filtroPDF_muestraSoloDocumentosPDF() {
        composeRule
            .onNodeWithText("PDF")
            .performClick()

        composeRule
            .onNodeWithText("reporte_test.pdf")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("foto_test.jpg")
            .assertDoesNotExist()
    }

    @Test
    fun homeScreen_filtroImagenes_muestraSoloImagenes() {
        composeRule
            .onNodeWithText("Imágenes")
            .performClick()

        composeRule
            .onNodeWithText("foto_test.jpg")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("reporte_test.pdf")
            .assertDoesNotExist()
    }

    @Test
    fun homeScreen_filtroTodos_muestraTodosLosDocumentos() {
        composeRule.onNodeWithText("PDF").performClick()
        composeRule.onNodeWithText("Todos").performClick()

        composeRule.onNodeWithText("foto_test.jpg").assertIsDisplayed()
        composeRule.onNodeWithText("reporte_test.pdf").assertIsDisplayed()
    }

    @Test
    fun homeScreen_fabBoton_estaVisible() {
        composeRule
            .onNodeWithContentDescription("Agregar")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_fabBoton_alHacerClick_muestraOpciones() {
        composeRule
            .onNodeWithContentDescription("Agregar")
            .performClick()

        composeRule
            .onNodeWithText("Desde Galería")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Desde Cámara")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_fabBoton_segundoClick_ocultaOpciones() {
        composeRule.onNodeWithContentDescription("Agregar").performClick()
        composeRule.onNodeWithText("Desde Galería").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Agregar").performClick()
        composeRule.onNodeWithText("Desde Galería").assertDoesNotExist()
    }

    @Test
    fun homeScreen_clickEnDocumento_navegaADetalle() {
        composeRule
            .onNodeWithText("foto_test.jpg")
            .performClick()

        composeRule
            .onNodeWithText("Autenticación requerida")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_listaVacia_muestraEstadoVacio() {
        composeRule
            .onNodeWithText("Sin documentos")
            .assertDoesNotExist()
    }
}