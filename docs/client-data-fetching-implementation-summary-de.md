# Client-Datenabruf und Zustandsverwaltung - Implementierungszusammenfassung

Dieses Dokument bietet eine Zusammenfassung der clientseitigen Datenabruf- und Zustandsverwaltungsimplementierung.

## Überblick

Wir haben eine umfassende Datenabruf- und Zustandsverwaltungslösung für die Client-Module implementiert. Die Implementierung folgt einem Clean-Architecture-Ansatz mit klarer Trennung der Verantwortlichkeiten zwischen den Schichten.

## Hauptkomponenten

### 1. API-Client-Schicht

Der `ApiClient`-Singleton im common-ui-Modul bietet:

- Generische HTTP-Methoden (GET, POST, PUT, DELETE) für API-Anfragen
- Response-Deserialisierung mit Kotlinx Serialization
- Fehlerbehandlung mit einer benutzerdefinierten `ApiException`-Klasse
- Caching für GET-Anfragen mit konfigurierbarer TTL

```kotlin
object ApiClient {
    val BASE_URL = "http://localhost:8080"
    val json = Json { ignoreUnknownKeys = true; isLenient = true }
    val httpClient = HttpClient(CIO) {
        // Konfiguration der Kürze halber weggelassen
    }
    val cache = ConcurrentHashMap<String, Pair<Any, Long>>()
    val CACHE_TTL = 30_000L // 30 Sekunden

    suspend inline fun <reified T> get(endpoint: String, cacheable: Boolean = true): T? {
        // Implementierung der Kürze halber weggelassen
        return null
    }

    suspend inline fun <reified T> post(endpoint: String, body: Any): T {
        // Implementierung der Kürze halber weggelassen
        throw IllegalStateException("Nicht implementiert")
    }

    suspend inline fun <reified T> put(endpoint: String, body: Any): T {
        // Implementierung der Kürze halber weggelassen
        throw IllegalStateException("Nicht implementiert")
    }

    suspend inline fun <reified T> delete(endpoint: String): T {
        // Implementierung der Kürze halber weggelassen
        throw IllegalStateException("Nicht implementiert")
    }

    fun clearCache() {
        cache.clear()
    }

    fun invalidateCache(endpoint: String) {
        cache.remove(endpoint)
    }
}
```

### 2. Repository-Schicht

Wir haben clientseitige Repositories implementiert, die derselben Schnittstelle wie ihre serverseitigen Gegenstücke folgen:

- **Modelle**: Vereinfachte clientseitige Modelle (`Person`, `Event`)
- **Repository-Interfaces**: Definieren den Vertrag für Datenzugriff (`PersonRepository`, `EventRepository`)
- **Repository-Implementierungen**: Verwenden `ApiClient`, um Daten vom Backend abzurufen (`ClientPersonRepository`, `ClientEventRepository`)

Beispiel Repository-Implementierung:

```kotlin
class ClientPersonRepository : PersonRepository {
    private val baseEndpoint = "/api/persons"

    override suspend fun findById(id: String): Person? {
        // Implementierung der Kürze halber weggelassen
        return null
    }

    override suspend fun findAllActive(limit: Int, offset: Int): List<Person> {
        // Implementierung der Kürze halber weggelassen
        return emptyList()
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<Person> {
        // Implementierung der Kürze halber weggelassen
        return emptyList()
    }

    override suspend fun save(person: Person): Person {
        // Implementierung der Kürze halber weggelassen
        return person
    }

    override suspend fun delete(id: String): Boolean {
        // Implementierung der Kürze halber weggelassen
        return false
    }

    override suspend fun countActive(): Long {
        // Implementierung der Kürze halber weggelassen
        return 0L
    }
}
```

### 3. Dependency Injection

Der `AppDependencies`-Singleton im web-app-Modul bietet:

- Repository-Instanzen
- Factory-Methoden zur Erstellung von ViewModels mit ordnungsgemäßen Abhängigkeiten

```kotlin
object AppDependencies {
    private val personRepository: PersonRepository by lazy { ClientPersonRepository() }
    private val eventRepository: EventRepository by lazy { ClientEventRepository() }

    fun createPersonViewModel(): CreatePersonViewModel {
        return CreatePersonViewModel(personRepository)
    }

    fun personListViewModel(): PersonListViewModel {
        return PersonListViewModel(personRepository)
    }

    fun initialize() {
        // ApiClient initialisieren, falls erforderlich
        println("AppDependencies initialisiert")
    }
}
```

### 4. ViewModel-Schicht

ViewModels im web-app-Modul:

- Nehmen Repositories als Konstruktor-Parameter
- Verwenden Coroutines für asynchronen Datenabruf
- Verwalten UI-Zustand (Laden, Fehler, Daten)
- Mappen Domain-Modelle zu UI-Modellen

Beispiel ViewModel:

```kotlin
class PersonListViewModel(
    private val personRepository: PersonRepository
) {
    var persons by mutableStateOf<List<PersonUiModel>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadPersons()
    }

    fun loadPersons() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val personList = personRepository.findAllActive(limit = 100, offset = 0)
                persons = personList.map { it.toUiModel() }
            } catch (e: Exception) {
                errorMessage = "Fehler beim Laden der Personen: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // ...
}
```

## Vorteile der Implementierung

1. **Clean Architecture**: Klare Trennung der Verantwortlichkeiten zwischen Schichten
2. **Testbarkeit**: Komponenten können isoliert getestet werden
3. **Wiederverwendbarkeit**: Gemeinsame Komponenten zwischen web-app und desktop-app geteilt
4. **Typsicherheit**: Stark typisierte API-Aufrufe und Antworten
5. **Fehlerbehandlung**: Konsistente Fehlerbehandlung in der gesamten Anwendung
6. **Performance**: Effizienter Datenabruf mit Caching

## Zukünftige Verbesserungen

Siehe [Client-Datenabruf-Verbesserungen](client-data-fetching-improvements-de.md) für potenzielle zukünftige Verbesserungen.

## Fazit

Die Implementierung bietet eine solide Grundlage für Datenabruf und Zustandsverwaltung in den Client-Modulen. Sie folgt Best Practices für Clean Architecture und bietet einen konsistenten Ansatz für die Datenbehandlung in der gesamten Anwendung.

---

**Letzte Aktualisierung**: 25. Juli 2025
