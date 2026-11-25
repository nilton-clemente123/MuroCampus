package com.clemente.murocampus

import com.clemente.murocampus.model.Comentario
import com.clemente.murocampus.model.NuevoPost
import com.clemente.murocampus.model.Post
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class ModelosTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun decodificaPostIgnorandoCamposExtra() {
        val post = json.decodeFromString<Post>("""{"userId":1,"id":2,"title":"Título","body":"Texto","extra":true}""")
        assertEquals(Post(1, 2, "Título", "Texto"), post)
    }

    @Test fun decodificaComentario() {
        val comentario = json.decodeFromString<Comentario>("""{"postId":1,"id":2,"name":"Nombre","email":"a@example.com","body":"Texto"}""")
        assertEquals("a@example.com", comentario.email)
    }

    @Test fun nuevoPostNoEnviaId() {
        assertEquals("""{"userId":1,"title":"Título","body":"Texto"}""", json.encodeToString(NuevoPost(1, "Título", "Texto")))
    }

    @Test(expected = SerializationException::class)
    fun campoObligatorioAusenteFalla() {
        json.decodeFromString<Post>("""{"userId":1,"id":2,"title":"Título"}""")
    }
}