package com.clemente.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clemente.murocampus.data.PostRepository
import com.clemente.murocampus.model.Post
import com.clemente.murocampus.model.Comentario
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
    private var carga: Job? = null
    private var cargaDetalle: Job? = null
    private val _detalle = MutableStateFlow<UiState<DetalleUi>>(UiState.Cargando)
    val detalle = _detalle.asStateFlow()

    init { cargar() }

    fun cargar() {
        carga?.cancel()
        carga = viewModelScope.launch {
            _lista.value = UiState.Cargando
            repo.listar()
                .onSuccess { _lista.value = UiState.Exito(it) }
                .onFailure { _lista.value = UiState.Error(mensajeDe(it)) }
        }
    }

    fun abrir(id: Int) {
        cargaDetalle?.cancel()
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

    fun cerrarDetalle() { cargaDetalle?.cancel() }
}

internal fun mensajeDe(error: Throwable): String = error.message ?: "Error desconocido"