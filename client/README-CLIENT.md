# Client Module

## Überblick

Das Client-Modul implementiert die Benutzeroberflächen für das Meldestelle-System und bietet sowohl eine Web-Anwendung als auch eine Desktop-Anwendung. Es folgt einer modernen, komponentenbasierten Architektur mit Jetpack Compose und implementiert das Repository-Pattern für saubere Datenschicht-Abstraktion.

## Architektur

Das Client-Modul ist in drei Hauptkomponenten unterteilt:

```
client/
├── common-ui/                   # Gemeinsame UI-Komponenten
│   ├── api/                     # API-Client-Schicht
│   │   └── ApiClient.kt        # HTTP-Client für Backend-Kommunikation
│   ├── repository/             # Repository-Pattern
│   │   ├── Person.kt           # Person-Domain-Model
│   │   ├── PersonRepository.kt # Person-Repository-Interface
│   │   ├── ClientPersonRepository.kt # Person-Repository-Implementierung
│   │   ├── Event.kt            # Event-Domain-Model
│   │   ├── EventRepository.kt  # Event-Repository-Interface
│   │   └── ClientEventRepository.kt # Event-Repository-Implementierung
│   ├── components/             # Wiederverwendbare UI-Komponenten
│   │   └── events/             # Event-spezifische Komponenten
│   │       ├── EventComponent.kt
│   │       └── VeranstaltungsListe.kt
│   ├── theme/                  # Design System
│   │   └── Theme.kt            # Compose Theme-Definition
│   └── App.kt                  # Gemeinsame App-Komponente
├── web-app/                    # Web-Anwendung
│   ├── screens/                # Web-spezifische Screens
│   ├── viewmodel/              # ViewModels für Web-App
│   └── main.kt                 # Web-App Entry Point
└── desktop-app/                # Desktop-Anwendung
    ├── App.kt                  # Desktop-App-Komponente
    └── main.kt                 # Desktop-App Entry Point
```

## Common-UI Komponenten

### 1. API-Client (ApiClient.kt)

Zentrale HTTP-Client-Implementierung für Backend-Kommunikation.

#### Features
- **HTTP-Client**: Ktor-basierter HTTP-Client
- **JSON-Serialisierung**: Kotlinx Serialization Integration
- **Fehlerbehandlung**: Strukturierte Fehlerbehandlung mit ApiException
- **Caching**: Intelligentes Caching für GET-Requests
- **Request/Response Logging**: Debugging-Unterstützung

#### Implementierung
```kotlin
object ApiClient {
    val BASE_URL = "http://localhost:8080"

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
        }
    }

    // Cache für GET-Requests
    val cache = ConcurrentHashMap<String, Pair<Any, Long>>()
    val CACHE_TTL = 30_000L // 30 Sekunden

    suspend inline fun <reified T> get(
        endpoint: String,
        cacheable: Boolean = true
    ): T? {
        // Caching-Logik
        if (cacheable) {
            val cached = cache[endpoint]
            if (cached != null && System.currentTimeMillis() - cached.second < CACHE_TTL) {
                return cached.first as T
            }
        }

        return try {
            val response = httpClient.get("$BASE_URL$endpoint")
            val result = response.body<T>()

            if (cacheable && result != null) {
                cache[endpoint] = Pair(result, System.currentTimeMillis())
            }

            result
        } catch (e: Exception) {
            throw ApiException("Failed to fetch data from $endpoint", e)
        }
    }

    suspend inline fun <reified T> post(endpoint: String, body: Any): T {
        return try {
            httpClient.post("$BASE_URL$endpoint") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }.body()
        } catch (e: Exception) {
            throw ApiException("Failed to post data to $endpoint", e)
        }
    }

    suspend inline fun <reified T> put(endpoint: String, body: Any): T {
        return try {
            httpClient.put("$BASE_URL$endpoint") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }.body()
        } catch (e: Exception) {
            throw ApiException("Failed to update data at $endpoint", e)
        }
    }

    suspend inline fun <reified T> delete(endpoint: String): T {
        return try {
            httpClient.delete("$BASE_URL$endpoint").body()
        } catch (e: Exception) {
            throw ApiException("Failed to delete data at $endpoint", e)
        }
    }

    fun clearCache() {
        cache.clear()
    }

    fun invalidateCache(endpoint: String) {
        cache.remove(endpoint)
    }
}

class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)
```

### 2. Repository-Pattern

Saubere Abstraktion der Datenschicht mit Repository-Pattern.

#### Domain Models

```kotlin
// Person.kt
@Serializable
data class Person(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
) {
    fun getFullName(): String = "$firstName $lastName"

    fun toUiModel(): PersonUiModel {
        return PersonUiModel(
            id = id,
            fullName = getFullName(),
            email = email,
            phone = phone,
            isActive = isActive
        )
    }
}

// Event.kt
@Serializable
data class Event(
    val id: String,
    val name: String,
    val description: String? = null,
    val startDate: String,
    val endDate: String,
    val location: String,
    val isPublic: Boolean = true,
    val maxParticipants: Int? = null,
    val createdAt: String,
    val updatedAt: String
) {
    fun toUiModel(): EventUiModel {
        return EventUiModel(
            id = id,
            name = name,
            description = description,
            startDate = startDate,
            endDate = endDate,
            location = location,
            isPublic = isPublic,
            maxParticipants = maxParticipants
        )
    }
}
```

#### Repository Interfaces

```kotlin
// PersonRepository.kt
interface PersonRepository {
    suspend fun findById(id: String): Person?
    suspend fun findAllActive(limit: Int = 100, offset: Int = 0): List<Person>
    suspend fun findByName(searchTerm: String, limit: Int = 50): List<Person>
    suspend fun save(person: Person): Person
    suspend fun delete(id: String): Boolean
    suspend fun countActive(): Long
}

// EventRepository.kt
interface EventRepository {
    suspend fun findById(id: String): Event?
    suspend fun findAllActive(limit: Int = 100, offset: Int = 0): List<Event>
    suspend fun findByName(searchTerm: String, limit: Int = 50): List<Event>
    suspend fun findPublicEvents(): List<Event>
    suspend fun save(event: Event): Event
    suspend fun delete(id: String): Boolean
    suspend fun countActive(): Long
}
```

#### Repository Implementierungen

```kotlin
// ClientPersonRepository.kt
class ClientPersonRepository : PersonRepository {
    private val baseEndpoint = "/api/persons"

    override suspend fun findById(id: String): Person? {
        return try {
            ApiClient.get<Person>("$baseEndpoint/$id")
        } catch (e: ApiException) {
            null
        }
    }

    override suspend fun findAllActive(limit: Int, offset: Int): List<Person> {
        return try {
            ApiClient.get<List<Person>>("$baseEndpoint?limit=$limit&offset=$offset") ?: emptyList()
        } catch (e: ApiException) {
            emptyList()
        }
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<Person> {
        return try {
            ApiClient.get<List<Person>>("$baseEndpoint/search?name=$searchTerm&limit=$limit") ?: emptyList()
        } catch (e: ApiException) {
            emptyList()
        }
    }

    override suspend fun save(person: Person): Person {
        return if (person.id.isEmpty()) {
            ApiClient.post<Person>(baseEndpoint, person)
        } else {
            ApiClient.put<Person>("$baseEndpoint/${person.id}", person)
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            ApiClient.delete<Unit>("$baseEndpoint/$id")
            true
        } catch (e: ApiException) {
            false
        }
    }

    override suspend fun countActive(): Long {
        return try {
            ApiClient.get<Map<String, Long>>("$baseEndpoint/count")?.get("count") ?: 0L
        } catch (e: ApiException) {
            0L
        }
    }
}
```

### 3. UI-Komponenten

Wiederverwendbare Compose-Komponenten für verschiedene Domänen.

#### Event-Komponenten

```kotlin
// EventComponent.kt
@Composable
fun EventCard(
    event: EventUiModel,
    onClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(event.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            event.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Ort: ${event.location}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Start: ${event.startDate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Ende: ${event.endDate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (event.isPublic) {
                        Badge {
                            Text("Öffentlich")
                        }
                    }

                    event.maxParticipants?.let { max ->
                        Text(
                            text = "Max: $max Teilnehmer",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// VeranstaltungsListe.kt
@Composable
fun VeranstaltungsListe(
    events: List<EventUiModel>,
    isLoading: Boolean = false,
    onEventClick: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Veranstaltungen",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Aktualisieren"
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(events) { event ->
                    EventCard(
                        event = event,
                        onClick = onEventClick
                    )
                }
            }
        }
    }
}
```

### 4. Theme System (Theme.kt)

Konsistentes Design System mit Material Design 3.

```kotlin
// Theme.kt
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFFEFBFF),
    onSurface = Color(0xFFFEFBFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFFEFBFF),
    surface = Color(0xFFFEFBFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun MeldestelleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )
)
```

## Web-App Komponenten

### 1. Screens

Web-spezifische Bildschirme und Navigation.

```kotlin
// PersonListScreen.kt
@Composable
fun PersonListScreen(
    viewModel: PersonListViewModel = remember { AppDependencies.personListViewModel() }
) {
    val persons by viewModel.persons.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Personen",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(persons) { person ->
                    PersonCard(person = person)
                }
            }
        }
    }
}
```

### 2. ViewModels

State Management für Web-App Screens.

```kotlin
// PersonListViewModel.kt
class PersonListViewModel(
    private val personRepository: PersonRepository
) {
    private val _persons = MutableStateFlow<List<PersonUiModel>>(emptyList())
    val persons: StateFlow<List<PersonUiModel>> = _persons.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        loadPersons()
    }

    fun loadPersons() {
        coroutineScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val personList = personRepository.findAllActive(limit = 100, offset = 0)
                _persons.value = personList.map { it.toUiModel() }
            } catch (e: Exception) {
                _errorMessage.value = "Fehler beim Laden der Personen: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPersons(searchTerm: String) {
        if (searchTerm.isBlank()) {
            loadPersons()
            return
        }

        coroutineScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val personList = personRepository.findByName(searchTerm, limit = 50)
                _persons.value = personList.map { it.toUiModel() }
            } catch (e: Exception) {
                _errorMessage.value = "Fehler bei der Suche: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        ApiClient.clearCache()
        loadPersons()
    }
}
```

### 3. Dependency Injection

```kotlin
// AppDependencies.kt
object AppDependencies {
    private val personRepository: PersonRepository by lazy { ClientPersonRepository() }
    private val eventRepository: EventRepository by lazy { ClientEventRepository() }

    fun createPersonViewModel(): CreatePersonViewModel {
        return CreatePersonViewModel(personRepository)
    }

    fun personListViewModel(): PersonListViewModel {
        return PersonListViewModel(personRepository)
    }

    fun eventListViewModel(): EventListViewModel {
        return EventListViewModel(eventRepository)
    }

    fun initialize() {
        // Initialize ApiClient if needed
        println("AppDependencies initialized")
    }
}
```

## Desktop-App Komponenten

### 1. Desktop-spezifische Implementierung

```kotlin
// desktop-app/main.kt
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Meldestelle Desktop",
        state = rememberWindowState(
            width = 1200.dp,
            height = 800.dp
        )
    ) {
        MeldestelleTheme {
            DesktopApp()
        }
    }
}

// desktop-app/App.kt
@Composable
fun DesktopApp() {
    var selectedTab by remember { mutableStateOf(0) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Navigation Rail
        NavigationRail(
            modifier = Modifier.width(80.dp)
        ) {
            NavigationRailItem(
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                label = { Text("Personen") },
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 }
            )
            NavigationRailItem(
                icon = { Icon(Icons.Default.Event, contentDescription = null) },
                label = { Text("Events") },
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 }
            )
            NavigationRailItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text("Settings") },
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 }
            )
        }

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> PersonListScreen()
                1 -> EventListScreen()
                2 -> SettingsScreen()
            }
        }
    }
}
```

## Konfiguration

### Gradle Dependencies

```kotlin
// common-ui/build.gradle.kts
dependencies {
    api(compose.runtime)
    api(compose.foundation)
    api(compose.material3)
    api(compose.ui)
    api(compose.components.resources)

    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

// web-app/build.gradle.kts
dependencies {
    implementation(project(":client:common-ui"))
    implementation(compose.html.core)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

// desktop-app/build.gradle.kts
dependencies {
    implementation(project(":client:common-ui"))
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
}
```

## Tests

### Unit Tests

```kotlin
class ApiClientTest {

    @Test
    fun `should cache GET requests`() = runTest {
        // Test caching functionality
    }

    @Test
    fun `should handle API errors gracefully`() = runTest {
        // Test error handling
    }
}

class PersonRepositoryTest {

    @Test
    fun `should fetch persons from API`() = runTest {
        // Test repository functionality
    }

    @Test
    fun `should handle empty responses`() = runTest {
        // Test edge cases
    }
}

class PersonListViewModelTest {

    @Test
    fun `should load persons on initialization`() = runTest {
        // Test ViewModel behavior
    }

    @Test
    fun `should handle loading states correctly`() = runTest {
        // Test state management
    }
}
```

## Deployment

### Web-App Deployment

```bash
# Build für Produktion
./gradlew :client:web-app:jsBrowserDistribution

# Statische Dateien werden generiert in:
# client/web-app/build/dist/js/productionExecutable/
```

### Desktop-App Deployment

```bash
# Desktop-App für aktuelles OS erstellen
./gradlew :client:desktop-app:createDistributable

# Plattform-spezifische Builds
./gradlew :client:desktop-app:packageDmg        # macOS
./gradlew :client:desktop-app:packageMsi        # Windows
./gradlew :client:desktop-app:packageDeb        # Linux
```

## Entwicklung

### Lokale Entwicklung

```bash
# Web-App im Development-Modus starten
./gradlew :client:web-app:jsBrowserDevelopmentRun

# Desktop-App starten
./gradlew :client:desktop-app:run

# Tests ausführen
./gradlew :client:test
```

### Hot Reload

- **Web-App**: Automatisches Hot Reload bei Änderungen
- **Desktop-App**: Neustart erforderlich bei Änderungen

## Best Practices

### 1. State Management

- **StateFlow/MutableStateFlow** für reaktive State-Verwaltung
- **Compose State** für UI-spezifischen State
- **Repository Pattern** für Datenschicht-Abstraktion

### 2. Error Handling

- **Strukturierte Exceptions** mit ApiException
- **Loading States** für bessere UX
- **Retry-Mechanismen** für fehlgeschlagene Requests

### 3. Performance

- **Lazy Loading** für große Listen
- **Caching** für häufig abgerufene Daten
- **Coroutines** für asynchrone Operationen

### 4. Testing

- **Unit Tests** für ViewModels und Repositories
- **UI Tests** für Compose-Komponenten
- **Integration Tests** für API-Client

## Zukünftige Erweiterungen

1. **Offline-Unterstützung** - Lokale Datenspeicherung
2. **Push-Benachrichtigungen** - Real-time Updates
3. **Progressive Web App** - PWA-Features für Web-App
4. **Erweiterte Navigation** - Multi-Screen Navigation
5. **Accessibility** - Barrierefreiheit-Features
6. **Internationalisierung** - Multi-Language Support
7. **Dark/Light Theme Toggle** - Theme-Umschaltung
8. **Advanced Caching** - Intelligentere Cache-Strategien
9. **Real-time Collaboration** - WebSocket-Integration
10. **Mobile App** - React Native oder Flutter Implementation

---

**Letzte Aktualisierung**: 25. Juli 2025

Für weitere Informationen zur Gesamtarchitektur siehe [README.md](../README.md).
