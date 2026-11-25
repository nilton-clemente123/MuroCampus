package com.clemente.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clemente.murocampus.viewmodel.PostsViewModel
import com.clemente.murocampus.viewmodel.UiState

@Composable
fun PantallaDetalle(vm: PostsViewModel, id: Int) {
    val estado by vm.detalle.collectAsStateWithLifecycle()
    when (val actual = estado) {
        UiState.Cargando -> Cargando("Cargando publicación…")
        is UiState.Error -> ErrorConReintento(actual.mensaje) { vm.abrir(id) }
        is UiState.Exito -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Usuario ${actual.datos.post.userId}", color = MaterialTheme.colorScheme.primary) }
            item { Text(actual.datos.post.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            item { Text(actual.datos.post.body) }
            item { HorizontalDivider() }
            item { Text("Comentarios (${actual.datos.comentarios.size})", style = MaterialTheme.typography.titleLarge) }
            items(actual.datos.comentarios, key = { it.id }) { comentario ->
                Text(comentario.email, color = MaterialTheme.colorScheme.primary)
                Text(comentario.body)
                HorizontalDivider()
            }
        }
    }
}