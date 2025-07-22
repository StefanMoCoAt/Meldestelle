package at.mocode.client.web

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.mocode.client.common.BaseApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    BaseApp {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Meldestelle - Reitersport Management") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                // Placeholder content
                Text("Welcome to Meldestelle - Reitersport Management")
                Text("This is a desktop application for managing equestrian events")
            }
        }
    }
}
