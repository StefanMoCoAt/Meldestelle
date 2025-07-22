# Client Data Fetching and State Management - Implementation Summary

This document provides a summary of the client-side data fetching and state management implementation.

## Overview

We have implemented a comprehensive data fetching and state management solution for the client modules. The implementation follows a clean architecture approach with clear separation of concerns between layers.

## Key Components

### 1. API Client Layer

The `ApiClient` singleton in the common-ui module provides:

- Generic HTTP methods (GET, POST, PUT, DELETE) for making API requests
- Response deserialization using Kotlinx Serialization
- Error handling with a custom `ApiException` class
- Caching for GET requests with configurable TTL

```kotlin
object ApiClient {
    val BASE_URL = "http://localhost:8080"
    val json = Json { ignoreUnknownKeys = true; isLenient = true }
    val httpClient = HttpClient(CIO) {
        // Configuration omitted for brevity
    }
    val cache = ConcurrentHashMap<String, Pair<Any, Long>>()
    val CACHE_TTL = 30_000L // 30 seconds

    suspend inline fun <reified T> get(endpoint: String, cacheable: Boolean = true): T? {
        // Implementation omitted for brevity
        return null
    }

    suspend inline fun <reified T> post(endpoint: String, body: Any): T {
        // Implementation omitted for brevity
        throw IllegalStateException("Not implemented")
    }

    suspend inline fun <reified T> put(endpoint: String, body: Any): T {
        // Implementation omitted for brevity
        throw IllegalStateException("Not implemented")
    }

    suspend inline fun <reified T> delete(endpoint: String): T {
        // Implementation omitted for brevity
        throw IllegalStateException("Not implemented")
    }

    fun clearCache() {
        cache.clear()
    }

    fun invalidateCache(endpoint: String) {
        cache.remove(endpoint)
    }
}
```

### 2. Repository Layer

We've implemented client-side repositories that follow the same interface as their server-side counterparts:

- **Models**: Simplified client-side models (`Person`, `Event`)
- **Repository Interfaces**: Define the contract for data access (`PersonRepository`, `EventRepository`)
- **Repository Implementations**: Use `ApiClient` to fetch data from the backend (`ClientPersonRepository`, `ClientEventRepository`)

Example repository implementation:

```kotlin
class ClientPersonRepository : PersonRepository {
    private val baseEndpoint = "/api/persons"

    override suspend fun findById(id: String): Person? {
        // Implementation omitted for brevity
        return null
    }

    override suspend fun findAllActive(limit: Int, offset: Int): List<Person> {
        // Implementation omitted for brevity
        return emptyList()
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<Person> {
        // Implementation omitted for brevity
        return emptyList()
    }

    override suspend fun save(person: Person): Person {
        // Implementation omitted for brevity
        return person
    }

    override suspend fun delete(id: String): Boolean {
        // Implementation omitted for brevity
        return false
    }

    override suspend fun countActive(): Long {
        // Implementation omitted for brevity
        return 0L
    }
}
```

### 3. Dependency Injection

The `AppDependencies` singleton in the web-app module provides:

- Repository instances
- Factory methods for creating ViewModels with proper dependencies

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
        // Initialize ApiClient if needed
        println("AppDependencies initialized")
    }
}
```

### 4. ViewModel Layer

ViewModels in the web-app module:

- Take repositories as constructor parameters
- Use coroutines for asynchronous data fetching
- Maintain UI state (loading, error, data)
- Map domain models to UI models

Example ViewModel:

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

## Benefits of the Implementation

1. **Clean Architecture**: Clear separation of concerns between layers
2. **Testability**: Components can be tested in isolation
3. **Reusability**: Common components shared between web-app and desktop-app
4. **Type Safety**: Strongly typed API calls and responses
5. **Error Handling**: Consistent error handling throughout the application
6. **Performance**: Efficient data fetching with caching

## Future Improvements

See [Client Data Fetching Improvements](client-data-fetching-improvements.md) for potential future improvements.

## Conclusion

The implementation provides a solid foundation for data fetching and state management in the client modules. It follows best practices for clean architecture and provides a consistent approach to handling data across the application.
