package com.clemente.murocampus

import com.clemente.murocampus.data.remote.PostApi
import com.clemente.murocampus.data.remote.RetrofitClient
import com.clemente.murocampus.model.NuevoPost
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PostApiTest {
    private val server = MockWebServer()
    private lateinit var api: PostApi
    private val post = """{"userId":1,"id":1,"title":"Título","body":"Texto","extra":true}"""

    @Before fun preparar() {
        server.start()
        api = RetrofitClient.crearApi(server.url("/").toString(), OkHttpClient())
    }
    @After fun cerrar() = server.shutdown()

    @Test fun listarUsaGet() = runTest {
        server.enqueue(MockResponse().setBody("[$post]"))
        assertEquals(1, api.listar().size)
        assertEquals("/posts", server.takeRequest().path)
    }

    @Test fun detalleUsaPath() = runTest {
        server.enqueue(MockResponse().setBody(post))
        assertEquals(1, api.detalle(1).id)
        assertEquals("/posts/1", server.takeRequest().path)
    }

    @Test fun comentariosUsaPath() = runTest {
        server.enqueue(MockResponse().setBody("[]"))
        assertTrue(api.comentarios(1).isEmpty())
        assertEquals("/posts/1/comments", server.takeRequest().path)
    }

    @Test fun filtroUsaQuery() = runTest {
        server.enqueue(MockResponse().setBody("[]"))
        api.porUsuario(2)
        assertEquals("/posts?userId=2", server.takeRequest().path)
    }

    @Test fun crearUsaPostYBody() = runTest {
        server.enqueue(MockResponse().setResponseCode(201).setBody(post.replace("\"id\":1", "\"id\":101")))
        assertEquals(101, api.crear(NuevoPost(1, "Título", "Texto")).id)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/posts", request.path)
        assertTrue(request.getHeader("Content-Type")!!.startsWith("application/json"))
        assertEquals("""{"userId":1,"title":"Título","body":"Texto"}""", request.body.readUtf8())
    }
}