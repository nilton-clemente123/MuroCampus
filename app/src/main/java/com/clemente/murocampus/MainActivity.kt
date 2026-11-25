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
                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text("MuroCampus") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }) { innerPadding ->
                    Box(Modifier.fillMaxSize().padding(innerPadding)) {
                        PantallaLista(vm)
                    }
                }
            }
        }
    }
}
