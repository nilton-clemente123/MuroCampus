package com.clemente.murocampus

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

/** Integración de las mejoras: requiere conexión al servicio real. */
class MejorasTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun esperar(texto: String) {
        compose.waitUntil(30_000) {
            compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
        compose.waitForIdle()
    }

    @Test fun buscarPorTituloSinCoincidencias() {
        esperar("sunt aut facere repellat provident occaecati excepturi optio reprehenderit")
        compose.onNodeWithText("Buscar por título").performTextInput("zzzz-no-existe-98765")
        esperar("Sin publicaciones")
    }

    @Test fun eliminarConConfirmacion() {
        val titulo = "sunt aut facere repellat provident occaecati excepturi optio reprehenderit"
        esperar(titulo)
        compose.onNodeWithText(titulo).performClick()
        esperar("Comentarios (5)")
        compose.onNodeWithText("Eliminar publicación").performScrollTo().performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithText("Eliminar publicación").performClick()
        compose.onNodeWithText("Confirmar eliminación").performClick()
        esperar("Publicación eliminada (simulación)")
        compose.onNodeWithText("Eliminar publicación").assertIsNotEnabled()
        compose.onNodeWithText("Atrás").performClick()
        esperar(titulo)
    }
}