package at.mocode.client.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import at.mocode.client.common.BaseApp
import at.mocode.client.common.components.events.VeranstaltungsListe
import at.mocode.client.common.components.horses.PferdeListe
import at.mocode.client.common.components.masterdata.StammdatenListe
import at.mocode.client.web.screens.CreatePersonScreen
import at.mocode.client.web.screens.PersonListScreen
import at.mocode.core.domain.model.DatenQuelleE
import at.mocode.core.domain.model.PferdeGeschlechtE
import at.mocode.events.domain.model.Veranstaltung
import at.mocode.horses.domain.model.DomPferd
import at.mocode.masterdata.domain.model.LandDefinition
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

/**
 * Main application composable for the desktop application.
 * Implements a simple tab-based navigation between different screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    // State for navigation
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Define tabs
    val tabs = listOf(
        TabItem("Dashboard", Icons.Default.Home),
        TabItem("Veranstaltungen", Icons.Default.Event),
        TabItem("Pferde", Icons.Default.Pets),
        TabItem("Personen", Icons.Default.Person),
        TabItem("Stammdaten", Icons.Default.Settings)
    )

    BaseApp {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Meldestelle - Reitersport Management") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tab row for navigation
                TabRow(
                    selectedTabIndex = selectedTabIndex
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(tab.title) },
                            icon = { Icon(tab.icon, contentDescription = tab.title) }
                        )
                    }
                }

                // Content based on selected tab
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when (selectedTabIndex) {
                        0 -> DashboardScreen()
                        1 -> EventsScreen()
                        2 -> HorsesScreen()
                        3 -> PersonsScreen()
                        4 -> MasterDataScreen()
                    }
                }
            }
        }
    }
}

/**
 * Data class representing a tab item
 */
data class TabItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * Dashboard screen showing an overview of the application
 */
@Composable
fun DashboardScreen() {
    val coroutineScope = rememberCoroutineScope()
    var pingResult by remember { mutableStateOf<String?>(null) }
    var pingLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Willkommen bei Meldestelle",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Reitersport Management System",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Display ping result if available
        pingResult?.let { result ->
            Text(
                text = "Ping Result: $result",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Quick access buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { /* TODO: Implement quick action */ }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, contentDescription = "Neue Veranstaltung")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Neue Veranstaltung")
                }
            }

            Button(
                onClick = { /* TODO: Implement quick action */ }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Search, contentDescription = "Suche")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Suche")
                }
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        pingLoading = true
                        try {
                            val pingClient = HttpClient(CIO) {
                                install(ContentNegotiation) {
                                    json(Json { ignoreUnknownKeys = true })
                                }
                                install(Auth) {
                                    basic {
                                        credentials {
                                            BasicAuthCredentials(username = "admin", password = "admin")
                                        }
                                    }
                                }
                            }

                            val response: Map<String, String> = pingClient.get("http://localhost:8080/api/ping").body()
                            pingResult = response["status"] ?: "No status in response"

                            pingClient.close()
                        } catch (e: Exception) {
                            pingResult = "Error: ${e.message}"
                        } finally {
                            pingLoading = false
                        }
                    }
                },
                enabled = !pingLoading
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (pingLoading) Icons.Default.Refresh else Icons.Default.NetworkCheck,
                        contentDescription = "Ping Test"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (pingLoading) "Pinging..." else "Ping Test")
                }
            }
        }
    }
}

/**
 * Events screen showing a list of events
 */
@Composable
fun EventsScreen() {
    // Create some dummy event data for testing
    val dummyEvents = remember {
        listOf(
            Veranstaltung(
                name = "Reitturnier Wien",
                ort = "Wien",
                startDatum = LocalDate(2025, 8, 15),
                endDatum = LocalDate(2025, 8, 17),
                veranstalterVereinId = com.benasher44.uuid.uuid4(),
                beschreibung = "Internationales Reitturnier mit Springprüfungen",
                istAktiv = true,
                istOeffentlich = true,
                anmeldeschluss = LocalDate(2025, 8, 1),
                maxTeilnehmer = 100
            ),
            Veranstaltung(
                name = "Dressurturnier Salzburg",
                ort = "Salzburg",
                startDatum = LocalDate(2025, 9, 5),
                endDatum = LocalDate(2025, 9, 5),
                veranstalterVereinId = com.benasher44.uuid.uuid4(),
                beschreibung = "Dressurturnier für alle Altersklassen",
                istAktiv = true,
                istOeffentlich = true,
                anmeldeschluss = LocalDate(2025, 8, 25),
                maxTeilnehmer = 50
            )
        )
    }

    // Use the VeranstaltungsListe component to display the events
    VeranstaltungsListe(
        events = dummyEvents,
        isLoading = false,
        errorMessage = null
    )
}

/**
 * Horses screen showing a list of horses
 */
@Composable
fun HorsesScreen() {
    // Create some dummy horse data for testing
    val dummyHorses = remember {
        listOf(
            DomPferd(
                pferdeName = "Maestoso Bella",
                geschlecht = PferdeGeschlechtE.STUTE,
                geburtsdatum = LocalDate(2018, 5, 12),
                rasse = "Lipizzaner",
                farbe = "Schimmel",
                lebensnummer = "AT2018123456",
                chipNummer = "276098100123456",
                oepsNummer = "AT12345",
                stockmass = 165,
                istAktiv = true,
                datenQuelle = DatenQuelleE.MANUELL
            ),
            DomPferd(
                pferdeName = "Donnerhall",
                geschlecht = PferdeGeschlechtE.HENGST,
                geburtsdatum = LocalDate(2020, 3, 24),
                rasse = "Hannoveraner",
                farbe = "Rappe",
                lebensnummer = "DE2020654321",
                passNummer = "DE98765",
                feiNummer = "FEI10293847",
                vaterName = "Dressage King",
                mutterName = "Hannelore",
                stockmass = 172,
                istAktiv = true,
                datenQuelle = DatenQuelleE.MANUELL
            ),
            DomPferd(
                pferdeName = "Lucky Star",
                geschlecht = PferdeGeschlechtE.WALLACH,
                geburtsdatum = LocalDate(2015, 7, 8),
                rasse = "Haflinger",
                farbe = "Fuchs",
                chipNummer = "276098100654321",
                istAktiv = true,
                datenQuelle = DatenQuelleE.MANUELL
            )
        )
    }

    // Use the PferdeListe component to display the horses
    PferdeListe(
        horses = dummyHorses,
        isLoading = false,
        errorMessage = null,
        onHorseClick = { /* Handle horse click */ }
    )
}

/**
 * Persons screen showing a list of persons
 */
@Composable
fun PersonsScreen() {
    // State for navigation
    var showCreatePerson by remember { mutableStateOf(false) }

    // Create view models using AppDependencies
    val personListViewModel = remember { at.mocode.client.web.di.AppDependencies.personListViewModel() }
    val createPersonViewModel = remember { at.mocode.client.web.di.AppDependencies.createPersonViewModel() }

    if (showCreatePerson) {
        // Show create person screen
        CreatePersonScreen(
            viewModel = createPersonViewModel,
            onNavigateBack = {
                // When navigating back, refresh the person list if a person was created
                if (createPersonViewModel.isSuccess) {
                    personListViewModel.refreshPersons()
                }
                showCreatePerson = false
            }
        )
    } else {
        // Show person list screen
        PersonListScreen(
            viewModel = personListViewModel,
            onNavigateToCreatePerson = { showCreatePerson = true }
        )
    }
}

/**
 * Master data screen showing master data like countries
 */
@Composable
fun MasterDataScreen() {
    // Create some dummy country data for testing
    val dummyCountries = remember {
        listOf(
            LandDefinition(
                isoAlpha2Code = "AT",
                isoAlpha3Code = "AUT",
                isoNumerischerCode = "040",
                nameDeutsch = "Österreich",
                nameEnglisch = "Austria",
                istEuMitglied = true,
                istEwrMitglied = true,
                istAktiv = true,
                sortierReihenfolge = 1
            ),
            LandDefinition(
                isoAlpha2Code = "DE",
                isoAlpha3Code = "DEU",
                isoNumerischerCode = "276",
                nameDeutsch = "Deutschland",
                nameEnglisch = "Germany",
                istEuMitglied = true,
                istEwrMitglied = true,
                istAktiv = true,
                sortierReihenfolge = 2
            ),
            LandDefinition(
                isoAlpha2Code = "CH",
                isoAlpha3Code = "CHE",
                isoNumerischerCode = "756",
                nameDeutsch = "Schweiz",
                nameEnglisch = "Switzerland",
                istEuMitglied = false,
                istEwrMitglied = false,
                istAktiv = true,
                sortierReihenfolge = 3
            ),
            LandDefinition(
                isoAlpha2Code = "IT",
                isoAlpha3Code = "ITA",
                isoNumerischerCode = "380",
                nameDeutsch = "Italien",
                nameEnglisch = "Italy",
                istEuMitglied = true,
                istEwrMitglied = true,
                istAktiv = true,
                sortierReihenfolge = 4
            ),
            LandDefinition(
                isoAlpha2Code = "FR",
                isoAlpha3Code = "FRA",
                isoNumerischerCode = "250",
                nameDeutsch = "Frankreich",
                nameEnglisch = "France",
                istEuMitglied = true,
                istEwrMitglied = true,
                istAktiv = true,
                sortierReihenfolge = 5
            )
        )
    }

    // Use the StammdatenListe component to display the countries
    StammdatenListe(
        countries = dummyCountries,
        isLoading = false,
        errorMessage = null,
        onCountryClick = { /* Handle country click */ }
    )
}

/**
 * A generic placeholder screen
 */
@Composable
fun PlaceholderScreen(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = title,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { /* TODO: Implement action */ }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "Hinzufügen")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hinzufügen")
            }
        }
    }
}
