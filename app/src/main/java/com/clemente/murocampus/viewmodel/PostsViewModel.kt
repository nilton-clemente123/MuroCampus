package com.clemente.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clemente.murocampus.data.PostRepository
import com.clemente.murocampus.model.Post
import com.clemente.murocampus.model.Comentario
import com.clemente.murocampus.model.NuevoPost
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class DetalleUi(val post: Post, val comentarios: List<Comentario>)

class PostsViewModel(private val repo: PostRepository) : ViewModel() {
    private val _lista = MutableStateFlow<UiState<List<Post>>>(UiState.Cargando)
    val lista = _lista.asStateFlow()
    private val _usuarioFiltro = MutableStateFlow<Int?>(null)
    val usuarioFiltro = _usuarioFiltro.asStateFlow()
    private var carga: Job? = null
    private var cargaDetalle: Job? = null
    private val _detalle = MutableStateFlow<UiState<DetalleUi>>(UiState.Cargando)
    val detalle = _detalle.asStateFlow()
    private val _creacion = MutableStateFlow<UiState<Post>?>(null)
    val creacion = _creacion.asStateFlow()
    private var envio: Job? = null
    private var borrado: Job? = null
    private val _eliminacion = MutableStateFlow<UiState<Unit>?>(null)
    val eliminacion = _eliminacion.asStateFlow()

    init { cargar() }

    fun cargar() {
        carga?.cancel()
        carga = viewModelScope.launch {
            _lista.value = UiState.Cargando
            val usuario = _usuarioFiltro.value
            val resultado = if (usuario == null) repo.listar() else repo.porUsuario(usuario)
            resultado
                .onSuccess { _lista.value = UiState.Exito(it) }
                .onFailure { _lista.value = UiState.Error(mensajeDe(it)) }
        }
    }

    fun abrir(id: Int) {
        cargaDetalle?.cancel()
        borrado?.cancel()
        _eliminacion.value = null
        _detalle.value = UiState.Cargando
        cargaDetalle = viewModelScope.launch {
            coroutineScope {
                val post = async { repo.detalle(id) }
                val comentarios = async { repo.comentarios(id) }
                val resultadoPost = post.await()
                val resultadoComentarios = comentarios.await()
                val error = resultadoPost.exceptionOrNull() ?: resultadoComentarios.exceptionOrNull()
                _detalle.value = if (error != null) UiState.Error(mensajeDe(error))
                else UiState.Exito(DetalleUi(resultadoPost.getOrThrow(), resultadoComentarios.getOrThrow()))
            }
        }
    }

    fun cerrarDetalle() {
        cargaDetalle?.cancel()
        borrado?.cancel()
    }

    fun filtrar(userId: Int?) {
        require(userId == null || userId in 1..10)
        _usuarioFiltro.value = userId
        cargar()
    }

    fun publicar(titulo: String, texto: String) {
        if (titulo.isBlank() || texto.isBlank() || envio?.isActive == true) return
        _creacion.value = UiState.Cargando
        envio = viewModelScope.launch {
            repo.crear(NuevoPost(1, titulo.trim(), texto.trim()))
                .onSuccess { _creacion.value = UiState.Exito(it) }
                .onFailure { _creacion.value = UiState.Error(mensajeDe(it)) }
        }
    }

    fun prepararCreacion() { _creacion.value = null }

    fun eliminar(id: Int) {
        if (borrado?.isActive == true || _eliminacion.value is UiState.Exito) return
        val actual = _detalle.value as? UiState.Exito ?: return
        if (actual.datos.post.id != id) return
        _eliminacion.value = UiState.Cargando
        borrado = viewModelScope.launch {
            repo.eliminar(id)
                .onSuccess { _eliminacion.value = UiState.Exito(Unit) }
                .onFailure { _eliminacion.value = UiState.Error(mensajeDe(it)) }
        }
    }
}

internal fun mensajeDe(error: Throwable): String = error.message ?: "Error desconocido"