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
        var errorComentarios: Exception? = null
        var ultimoUsuario: Int? = null
        var errorCrear: Exception? = null
        var ultimoNuevo: NuevoPost? = null
        var errorEliminar: Exception? = null
        var eliminaciones = 0
        override suspend fun listar(): Result<List<Post>> { llamadas++; return resultado }
        override suspend fun detalle(id: Int): Result<Post> = Result.success(Post(1, id, "Título", "Texto"))
        override suspend fun comentarios(id: Int): Result<List<Comentario>> = errorComentarios?.let { Result.failure(it) }
            ?: Result.success(List(5) { Comentario(id, it, "Nombre", "a@example.com", "Texto") })
        override suspend fun porUsuario(userId: Int): Result<List<Post>> { ultimoUsuario = userId; return resultado }
        override suspend fun crear(nuevo: NuevoPost): Result<Post> {
            ultimoNuevo = nuevo
            return errorCrear?.let { Result.failure(it) }
                ?: Result.success(Post(nuevo.userId, 101, nuevo.title, nuevo.body))
        }
        override suspend fun eliminar(id: Int): Result<Unit> {
            eliminaciones++
            return errorEliminar?.let { Result.failure(it) } ?: Result.success(Unit)
        }
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

    @Test fun detalleIncluyeComentariosSinRecargarLista() = runTest(dispatcher) {
        val repo = RepoFalso()
        val vm = PostsViewModel(repo)
        advanceUntilIdle()
        vm.abrir(1)
        advanceUntilIdle()
        val detalle = (vm.detalle.value as UiState.Exito).datos
        assertEquals(1, detalle.post.id)
        assertEquals(5, detalle.comentarios.size)
        vm.cerrarDetalle()
        assertEquals(1, repo.llamadas)
    }

    @Test fun falloDeComentariosFallaTodoElDetalle() = runTest(dispatcher) {
        val vm = PostsViewModel(RepoFalso().apply { errorComentarios = Exception("Sin conexión a internet") })
        vm.abrir(1)
        advanceUntilIdle()
        assertEquals(UiState.Error("Sin conexión a internet"), vm.detalle.value)
    }

    @Test fun filtroPorUsuarioYTodos() = runTest(dispatcher) {
        val repo = RepoFalso()
        val vm = PostsViewModel(repo)
        advanceUntilIdle()
        vm.filtrar(2)
        advanceUntilIdle()
        assertEquals(2, vm.usuarioFiltro.value)
        assertEquals(2, repo.ultimoUsuario)
        vm.cargar()
        advanceUntilIdle()
        assertEquals(1, repo.llamadas)
        vm.filtrar(null)
        advanceUntilIdle()
        assertNull(vm.usuarioFiltro.value)
        assertEquals(2, repo.llamadas)
    }

    @Test fun publicarValidaCamposYDevuelve101() = runTest(dispatcher) {
        val repo = RepoFalso()
        val vm = PostsViewModel(repo)
        vm.publicar(" ", "Texto")
        assertNull(vm.creacion.value)
        vm.publicar(" Título ", " Texto ")
        assertEquals(UiState.Cargando, vm.creacion.value)
        advanceUntilIdle()
        assertEquals(NuevoPost(1, "Título", "Texto"), repo.ultimoNuevo)
        assertEquals(101, (vm.creacion.value as UiState.Exito).datos.id)
        vm.prepararCreacion()
        assertNull(vm.creacion.value)
    }

    @Test fun publicarPuedeReintentarTrasError() = runTest(dispatcher) {
        val repo = RepoFalso().apply { errorCrear = Exception("Sin conexión a internet") }
        val vm = PostsViewModel(repo)
        vm.publicar("Título", "Texto")
        advanceUntilIdle()
        assertTrue(vm.creacion.value is UiState.Error)
        repo.errorCrear = null
        vm.publicar("Título", "Texto")
        advanceUntilIdle()
        assertTrue(vm.creacion.value is UiState.Exito)
    }

    @Test fun eliminarRequiereDetalleYNoDuplicaExito() = runTest(dispatcher) {
        val repo = RepoFalso()
        val vm = PostsViewModel(repo)
        vm.eliminar(1)
        advanceUntilIdle()
        assertEquals(0, repo.eliminaciones)
        vm.abrir(1)
        advanceUntilIdle()
        vm.eliminar(2)
        assertNull(vm.eliminacion.value)
        vm.eliminar(1)
        assertEquals(UiState.Cargando, vm.eliminacion.value)
        advanceUntilIdle()
        assertEquals(UiState.Exito(Unit), vm.eliminacion.value)
        vm.eliminar(1)
        advanceUntilIdle()
        assertEquals(1, repo.eliminaciones)
    }

    @Test fun eliminarPermiteReintentarYReiniciaAlCambiarDetalle() = runTest(dispatcher) {
        val repo = RepoFalso().apply { errorEliminar = Exception("Error del servidor (500)") }
        val vm = PostsViewModel(repo)
        vm.abrir(1)
        advanceUntilIdle()
        vm.eliminar(1)
        advanceUntilIdle()
        assertTrue(vm.eliminacion.value is UiState.Error)
        repo.errorEliminar = null
        vm.eliminar(1)
        advanceUntilIdle()
        assertEquals(UiState.Exito(Unit), vm.eliminacion.value)
        vm.abrir(2)
        assertNull(vm.eliminacion.value)
    }

    @Test fun busquedaLocalIgnoraMayusculasYNoDescarga() = runTest(dispatcher) {
        val repo = RepoFalso().apply {
            resultado = Result.success(listOf(post, Post(2, 2, "Otra publicación", "Título en el cuerpo")))
        }
        val vm = PostsViewModel(repo)
        advanceUntilIdle()
        vm.buscar("  TÍTULO  ")
        advanceUntilIdle()
        assertEquals(UiState.Exito(listOf(post)), vm.lista.value)
        vm.buscar("no coincide")
        advanceUntilIdle()
        assertEquals(UiState.Exito(emptyList<Post>()), vm.lista.value)
        vm.buscar("   ")
        advanceUntilIdle()
        assertEquals(2, (vm.lista.value as UiState.Exito).datos.size)
        assertEquals(1, repo.llamadas)
    }

    @Test fun busquedaConservaErroresYSeAplicaAlFiltroRemoto() = runTest(dispatcher) {
        val repo = RepoFalso().apply { resultado = Result.failure(Exception("Sin conexión a internet")) }
        val vm = PostsViewModel(repo)
        advanceUntilIdle()
        vm.buscar("Título")
        advanceUntilIdle()
        assertEquals(UiState.Error("Sin conexión a internet"), vm.lista.value)
        repo.resultado = Result.success(listOf(Post(2, 11, "Otro", "Texto")))
        vm.filtrar(2)
        advanceUntilIdle()
        assertEquals("Título", vm.busqueda.value)
        assertEquals(UiState.Exito(emptyList<Post>()), vm.lista.value)
        assertEquals(2, repo.ultimoUsuario)
    }
}