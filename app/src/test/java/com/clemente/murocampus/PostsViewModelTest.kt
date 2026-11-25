package com.clemente.murocampus

import com.clemente.murocampus.data.PostRepository
import com.clemente.murocampus.model.Comentario
import com.clemente.murocampus.model.NuevoPost
import com.clemente.murocampus.model.Post
import com.clemente.murocampus.viewmodel.PostsViewModel
import com.clemente.murocampus.viewmodel.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val post = Post(1, 1, "Título", "Texto")
    private class RepoFalso : PostRepository {
        var resultado: Result<List<Post>> = Result.success(emptyList())
        var llamadas = 0
        override suspend fun listar(): Result<List<Post>> { llamadas++; return resultado }
        override suspend fun detalle(id: Int): Result<Post> = Result.success(Post(1, id, "Título", "Texto"))
        override suspend fun comentarios(id: Int): Result<List<Comentario>> = Result.success(emptyList())
        override suspend fun porUsuario(userId: Int): Result<List<Post>> = resultado
        override suspend fun crear(nuevo: NuevoPost): Result<Post> = Result.success(Post(nuevo.userId, 101, nuevo.title, nuevo.body))
    }
    @Before fun preparar() = Dispatchers.setMain(dispatcher)
    @After fun cerrar() = Dispatchers.resetMain()

    @Test fun cargaInicialYExito() = runTest(dispatcher) {
        val repo = RepoFalso().apply { resultado = Result.success(listOf(post)) }
        val vm = PostsViewModel(repo)
        assertEquals(UiState.Cargando, vm.lista.value)
        advanceUntilIdle()
        assertEquals(UiState.Exito(listOf(post)), vm.lista.value)
        assertEquals(1, repo.llamadas)
    }

    @Test fun errorYReintento() = runTest(dispatcher) {
        val repo = RepoFalso().apply { resultado = Result.failure(Exception("Sin conexión a internet")) }
        val vm = PostsViewModel(repo)
        advanceUntilIdle()
        assertEquals(UiState.Error("Sin conexión a internet"), vm.lista.value)
        repo.resultado = Result.success(listOf(post))
        vm.cargar()
        advanceUntilIdle()
        assertTrue(vm.lista.value is UiState.Exito)
        assertEquals(2, repo.llamadas)
    }
}