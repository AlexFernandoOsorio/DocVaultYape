package com.example.docvaultyape.presentation.screens

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
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun navegacion_iniciaEnHomeScreen() {
        composeRule
            .onNodeWithText("DocVault")
            .assertIsDisplayed()
    }

    @Test
    fun navegacion_clickDocumento_vaADetalle() {
        composeRule
            .onNodeWithText("foto_test.jpg")
            .performClick()

        composeRule
            .onNodeWithText("Autenticación requerida")
            .assertIsDisplayed()
    }

    @Test
    fun navegacion_detalle_volverRegresaAHome() {
        composeRule.onNodeWithText("foto_test.jpg").performClick()
        composeRule.onNodeWithText("Autenticación requerida").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Volver").performClick()

        composeRule.onNodeWithText("DocVault").assertIsDisplayed()
    }

    @Test
    fun navegacion_clickSegundoDocumento_vaADetalle() {
        composeRule
            .onNodeWithText("reporte_test.pdf")
            .performClick()

        composeRule
            .onNodeWithText("reporte_test.pdf")
            .assertIsDisplayed()
    }

    @Test
    fun navegacion_flujoCCompleto_homeDetalleHome() {
        composeRule.onNodeWithText("DocVault").assertIsDisplayed()

        composeRule.onNodeWithText("foto_test.jpg").performClick()
        composeRule.onNodeWithText("Autenticación requerida").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Volver").performClick()
        composeRule.onNodeWithText("DocVault").assertIsDisplayed()

        composeRule.onNodeWithText("reporte_test.pdf").performClick()
        composeRule.onNodeWithText("reporte_test.pdf").assertIsDisplayed()
    }
}