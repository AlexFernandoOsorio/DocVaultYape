package com.example.docvaultyape.presentation.screens.detail

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
class DocumentDetailScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule
            .onNodeWithText("foto_test.jpg")
            .performClick()
    }

    @Test
    fun detailScreen_muestraPantallaBiometrica() {
        composeRule
            .onNodeWithText("Autenticación requerida")
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_muestraTextoDeVerificacion() {
        composeRule
            .onNodeWithText("Verifica tu identidad para acceder al documento seguro")
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_muestraBotonAutenticar() {
        composeRule
            .onNodeWithText("Autenticar")
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_muestraNombreDocumentoEnTopBar() {
        composeRule
            .onNodeWithText("foto_test.jpg")
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_botonVolver_estaVisible() {
        composeRule
            .onNodeWithContentDescription("Volver")
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_botonVolver_navegaDeVuelta() {
        composeRule
            .onNodeWithContentDescription("Volver")
            .performClick()

        composeRule
            .onNodeWithText("DocVault")
            .assertIsDisplayed()
    }
}