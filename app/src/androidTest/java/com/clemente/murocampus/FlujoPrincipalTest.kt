package com.clemente.murocampus

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.Bitmap
import java.io.File
import org.junit.Rule
import org.junit.Test

/** Prueba de integración real: requiere internet y el servicio JSONPlaceholder disponible. */
class FlujoPrincipalTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun esperar(texto: String) {
        compose.waitUntil(30_000) {
            compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty()
        }
        compose.waitForIdle()
    }

    private fun captura(nombre: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val carpeta = File(instrumentation.targetContext.filesDir, "capturas").apply { mkdirs() }
        val imagen = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        File(carpeta, nombre).outputStream().use { imagen.compress(Bitmap.CompressFormat.PNG, 100, it) }
        imagen.recycle()
    }

    @Test fun listaDetalleFiltroYCrear() {
        val titulo = "sunt aut facere repellat provident occaecati excepturi optio reprehenderit"
        esperar(titulo)
        captura("lista.png")
        compose.onNodeWithText(titulo).performClick()
        esperar("Comentarios (5)")
        captura("detalle.png")
        compose.onNodeWithText("Atrás").performClick()
        compose.onNodeWithText("Usuario 2").performClick()
        esperar("et ea vero quia laudantium autem")
        captura("filtro.png")
        compose.onNodeWithText("+ Nueva publicación").performClick()
        compose.onNodeWithText("Publicar").assertIsNotEnabled()
        compose.onNodeWithText("Título").performTextInput("Publicación de prueba")
        compose.onNodeWithText("Contenido").performTextInput("Contenido enviado desde MuroCampus")
        compose.onNodeWithText("Publicar").performClick()
        esperar("Publicado con id 101")
        captura("crear.png")
    }
}