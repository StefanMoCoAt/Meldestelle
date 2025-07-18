import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import at.mocode.ui.screens.PersonListScreen
import at.mocode.ui.screens.CreatePersonScreen
import at.mocode.ui.theme.MeldestelleTheme
import at.mocode.di.AppDependencies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MeldestelleTheme {
        val navController = rememberNavController()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Meldestelle - Reitersport Management") }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "person_list",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("person_list") {
                    val viewModel = remember { AppDependencies.personListViewModel() }
                    PersonListScreen(
                        viewModel = viewModel,
                        onNavigateToCreatePerson = {
                            navController.navigate("create_person")
                        }
                    )
                }
                composable("create_person") {
                    val viewModel = remember { AppDependencies.createPersonViewModel() }
                    CreatePersonScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
