package com.clemente.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clemente.murocampus.viewmodel.PostsViewModel
import com.clemente.murocampus.viewmodel.UiState

@Composable
fun PantallaCrear(vm: PostsViewModel) {
    val estado by vm.creacion.collectAsStateWithLifecycle()
    var titulo by rememberSaveable { mutableStateOf("") }
    var contenido by rememberSaveable { mutableStateOf("") }
    val enviando = estado == UiState.Cargando
    LaunchedEffect(estado) {
        if (estado is UiState.Exito) { titulo = ""; contenido = "" }
    }
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Nueva publicación", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = titulo, onValueChange = { titulo = it }, label = { Text("Título") },
            modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !enviando
        )
        OutlinedTextField(
            value = contenido, onValueChange = { contenido = it }, label = { Text("Contenido") },
            modifier = Modifier.fillMaxWidth(), minLines = 4, enabled = !enviando
        )
        Button(
            onClick = { vm.publicar(titulo, contenido) },
            enabled = titulo.isNotBlank() && contenido.isNotBlank() && !enviando
        ) { Text("Publicar") }
        when (val actual = estado) {
            null -> Unit
            UiState.Cargando -> LinearProgressIndicator(Modifier.fillMaxWidth())
            is UiState.Exito -> Text("Publicado con id ${actual.datos.id}", color = MaterialTheme.colorScheme.primary)
            is UiState.Error -> ErrorConReintento(actual.mensaje) { vm.publicar(titulo, contenido) }
        }
        Text("El servicio simula el envío: la publicación no se guarda en el servidor.", style = MaterialTheme.typography.bodySmall)
    }
}