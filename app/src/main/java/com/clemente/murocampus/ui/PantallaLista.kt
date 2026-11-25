package com.clemente.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clemente.murocampus.viewmodel.PostsViewModel
import com.clemente.murocampus.viewmodel.UiState

@Composable
fun PantallaLista(vm: PostsViewModel, alAbrir: (Int) -> Unit = {}) {
    val estado by vm.lista.collectAsStateWithLifecycle()
    when (val actual = estado) {
        UiState.Cargando -> Cargando()
        is UiState.Error -> ErrorConReintento(actual.mensaje, vm::cargar)
        is UiState.Exito -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (actual.datos.isEmpty()) item { Text("Sin publicaciones") }
            items(actual.datos, key = { it.id }) { post ->
                TarjetaPost(post.userId, post.title, post.body) { alAbrir(post.id) }
            }
        }
    }
}