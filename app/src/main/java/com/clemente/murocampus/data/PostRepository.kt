package com.clemente.murocampus.data

import com.clemente.murocampus.data.remote.PostApi
import com.clemente.murocampus.model.Comentario
import com.clemente.murocampus.model.NuevoPost
import com.clemente.murocampus.model.Post
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

interface PostRepository {
    suspend fun listar(): Result<List<Post>>
    suspend fun detalle(id: Int): Result<Post>
    suspend fun comentarios(id: Int): Result<List<Comentario>>
    suspend fun porUsuario(userId: Int): Result<List<Post>>
    suspend fun crear(nuevo: NuevoPost): Result<Post>
}

/** Error de datos traducido a un mensaje seguro, sin exponer Retrofit a la presentación. */
class ErrorDatos(mensaje: String, causa: Throwable) : Exception(mensaje, causa)

class PostRepositoryRemote(private val api: PostApi) : PostRepository {
    override suspend fun listar() = llamar { api.listar() }
    override suspend fun detalle(id: Int) = llamar { api.detalle(id) }
    override suspend fun comentarios(id: Int) = llamar { api.comentarios(id) }
    override suspend fun porUsuario(userId: Int) = llamar { api.porUsuario(userId) }
    override suspend fun crear(nuevo: NuevoPost) = llamar { api.crear(nuevo) }

    private suspend fun <T> llamar(bloque: suspend () -> T): Result<T> = try {
        Result.success(bloque())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val mensaje = when (e) {
            is HttpException -> "Error del servidor (${e.code()})"
            is SerializationException -> "Respuesta inesperada"
            is IOException -> "Sin conexión a internet.\nRevisa tu red e inténtalo otra vez."
            else -> "Error desconocido"
        }
        Result.failure(ErrorDatos(mensaje, e))
    }
}