package com.clemente.murocampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import com.clemente.murocampus.ui.theme.MuroCampusTheme
import com.clemente.murocampus.data.PostRepositoryRemote
import com.clemente.murocampus.data.remote.RetrofitClient
import com.clemente.murocampus.ui.PantallaLista
import com.clemente.murocampus.viewmodel.PostsViewModel
import com.clemente.murocampus.viewmodel.PostsViewModelFactory
import androidx.compose.foundation.layout.Box
import androidx.activity.compose.BackHandler
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import com.clemente.murocampus.ui.PantallaDetalle
import com.clemente.murocampus.ui.PantallaCrear
import androidx.compose.material3.FloatingActionButton

class MainActivity : ComponentActivity() {
    private val vm: PostsViewModel by viewModels {
        PostsViewModelFactory(PostRepositoryRemote(RetrofitClient.api))
    }
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MuroCampusTheme {
                var idSeleccionado by rememberSaveable { mutableStateOf<Int?>(null) }
                var creando by rememberSaveable { mutableStateOf(false) }
                LaunchedEffect(idSeleccionado) {
                    idSeleccionado?.let(vm::abrir) ?: vm.cerrarDetalle()
                }
                BackHandler(enabled = idSeleccionado != null || creando) {
                    idSeleccionado = null
                    creando = false
                }
                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text("MuroCampus") },
                        navigationIcon = {
                            if (idSeleccionado != null || creando) TextButton(onClick = { idSeleccionado = null; creando = false }) {
                                Text("Atrás", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }, floatingActionButton = {
                    if (idSeleccionado == null && !creando) {
                        FloatingActionButton(onClick = { vm.prepararCreacion(); creando = true }) {
                            Text("+ Nueva publicación")
                        }
                    }
                }) { innerPadding ->
                    Box(Modifier.fillMaxSize().padding(innerPadding)) {
                        val id = idSeleccionado
                        if (creando) PantallaCrear(vm)
                        else if (id == null) PantallaLista(vm, alAbrir = { idSeleccionado = it })
                        else PantallaDetalle(vm, id)
                    }
                }
            }
        }
    }
}
