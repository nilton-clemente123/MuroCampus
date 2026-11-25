package com.clemente.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clemente.murocampus.data.PostRepository
import com.clemente.murocampus.model.Post
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostsViewModel(private val repo: PostRepository) : ViewModel() {
    private val _lista = MutableStateFlow<UiState<List<Post>>>(UiState.Cargando)
    val lista = _lista.asStateFlow()
    private var carga: Job? = null

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
}

internal fun mensajeDe(error: Throwable): String = error.message ?: "Error desconocido"