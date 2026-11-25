package com.clemente.murocampus

import com.clemente.murocampus.data.PostRepositoryRemote
import com.clemente.murocampus.data.remote.PostApi
import com.clemente.murocampus.data.remote.RetrofitClient
import com.clemente.murocampus.model.Comentario
import com.clemente.murocampus.model.NuevoPost
import com.clemente.murocampus.model.Post
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class PostRepositoryTest {
    private class ApiFallida(private val error: Exception) : PostApi {
        override suspend fun listar(): List<Post> = throw error
        override suspend fun detalle(id: Int): Post = throw error
        override suspend fun comentarios(id: Int): List<Comentario> = throw error
        override suspend fun porUsuario(userId: Int): List<Post> = throw error
        override suspend fun crear(nuevo: NuevoPost): Post = throw error
        override suspend fun eliminar(id: Int): retrofit2.Response<Unit> = throw error
    }

    @Test fun todasLasOperacionesDevuelvenErrorDeRed() = runTest {
        val repo = PostRepositoryRemote(ApiFallida(IOException("sin red")))
        val resultados = listOf(repo.listar(), repo.detalle(1), repo.comentarios(1), repo.porUsuario(2), repo.crear(NuevoPost(1, "a", "b")), repo.eliminar(1))
        resultados.forEach {
            assertTrue(it.isFailure)
            assertTrue(it.exceptionOrNull()!!.message!!.startsWith("Sin conexión a internet"))
        }
    }

    @Test fun cancelacionNoSeConvierteEnError() = runTest {
        val error = CancellationException("cancelado")
        val repo = PostRepositoryRemote(ApiFallida(error))
        val operaciones: List<suspend () -> Any> = listOf(
            { repo.listar() }, { repo.detalle(1) }, { repo.comentarios(1) },
            { repo.porUsuario(2) }, { repo.crear(NuevoPost(1, "a", "b")) }, { repo.eliminar(1) }
        )
        operaciones.forEach { operacion ->
            try {
                operacion()
                fail("Debe relanzar la cancelación")
            } catch (e: CancellationException) {
                assertSame(error, e)
            }
        }
    }

    @Test fun transforma404YJsonInvalidoYConservaExito() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            val repo = PostRepositoryRemote(RetrofitClient.crearApi(server.url("/").toString(), OkHttpClient()))
            server.enqueue(MockResponse().setResponseCode(404))
            assertEquals("Error del servidor (404)", repo.detalle(9999).exceptionOrNull()!!.message)
            server.enqueue(MockResponse().setBody("""{"id":1}"""))
            assertEquals("Respuesta inesperada", repo.detalle(1).exceptionOrNull()!!.message)
            server.enqueue(MockResponse().setBody("[]"))
            assertEquals(emptyList<Post>(), repo.listar().getOrThrow())
        } finally {
            server.shutdown()
        }
    }

    @Test fun eliminarCompruebaCodigoInclusoSinCuerpo() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            val repo = PostRepositoryRemote(RetrofitClient.crearApi(server.url("/").toString(), OkHttpClient()))
            for (codigo in listOf(200, 204, 404, 500)) {
                server.enqueue(MockResponse().setResponseCode(codigo))
                val resultado = repo.eliminar(1)
                if (codigo < 300) assertTrue(resultado.isSuccess)
                else assertEquals("Error del servidor ($codigo)", resultado.exceptionOrNull()!!.message)
                val request = server.takeRequest()
                assertEquals("DELETE", request.method)
                assertEquals("/posts/1", request.path)
            }
        } finally {
            server.shutdown()
        }
    }
}