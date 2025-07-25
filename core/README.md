# Core Module

## Überblick

Das Core-Modul bildet das Fundament des gesamten Meldestelle-Systems und implementiert den **Shared Kernel** nach Domain-Driven Design Prinzipien. Es stellt gemeinsame Domänenkonzepte, Utilities und Infrastrukturkomponenten bereit, die von allen anderen Modulen (members, horses, events, masterdata, infrastructure) verwendet werden.

## Architektur

Das Core-Modul ist in zwei Hauptkomponenten unterteilt:

```
core/
├── core-domain/                 # Shared Domain Layer
│   ├── model/                   # Gemeinsame Domain Models
│   │   ├── BaseDto.kt          # Basis-DTO-Klassen
│   │   └── Enums.kt            # Gemeinsame Enumerationen
│   ├── serialization/          # Serialisierung
│   │   └── Serializers.kt      # Custom Serializers
│   └── event/                  # Domain Events
│       └── DomainEvent.kt      # Event-Infrastruktur
└── core-utils/                 # Shared Utilities
    ├── config/                 # Konfiguration
    │   ├── AppConfig.kt        # Anwendungskonfiguration
    │   └── AppEnvironment.kt   # Umgebungskonfiguration
    ├── database/               # Datenbank-Utilities
    │   ├── DatabaseConfig.kt   # Datenbank-Konfiguration
    │   ├── DatabaseFactory.kt  # Datenbank-Factory
    │   └── DatabaseMigrator.kt # Schema-Migrationen
    ├── discovery/              # Service Discovery
    │   └── ServiceRegistration.kt # Service-Registrierung
    ├── error/                  # Fehlerbehandlung
    │   └── Result.kt           # Result-Type für Fehlerbehandlung
    ├── serialization/          # Serialisierung
    │   └── Serialization.kt    # Serialisierungs-Utilities
    └── validation/             # Validierung
        ├── ValidationResult.kt  # Validierungsergebnisse
        ├── ValidationUtils.kt   # Validierungs-Utilities
        └── ApiValidationUtils.kt # API-Validierung
```

## Core-Domain Komponenten

### 1. Gemeinsame Enumerationen (Enums.kt)

Zentrale Enumerationen, die modulübergreifend verwendet werden.

#### PferdeGeschlechtE
```kotlin
enum class PferdeGeschlechtE {
    HENGST,     // Männlich, nicht kastriert
    STUTE,      // Weiblich
    WALLACH     // Männlich, kastriert
}
```

#### SparteE (Sportsparten)
```kotlin
enum class SparteE {
    DRESSUR,           // Dressurreiten
    SPRINGEN,          // Springreiten
    VIELSEITIGKEIT,    // Vielseitigkeitsreiten
    FAHREN,            // Fahrsport
    VOLTIGIEREN,       // Voltigieren
    WESTERN,           // Westernreiten
    DISTANZ,           // Distanzreiten
    PARA_DRESSUR,      // Para-Dressur
    PARA_FAHREN        // Para-Fahren
}
```

#### DatenQuelleE
```kotlin
enum class DatenQuelleE {
    MANUELL,           // Manuelle Eingabe
    IMPORT,            // Datenimport
    SYNCHRONISATION,   // Externe Synchronisation
    MIGRATION          // Datenmigration
}
```

### 2. Basis-DTOs (BaseDto.kt)

Gemeinsame Basisklassen für Data Transfer Objects.

```kotlin
@Serializable
abstract class BaseDto {
    abstract val id: String
    abstract val version: Long
    abstract val createdAt: Instant
    abstract val updatedAt: Instant
}

@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val success: Boolean = true,
    val message: String? = null,
    val errors: List<String> = emptyList(),
    val timestamp: Instant = Clock.System.now()
)

@Serializable
data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)
```

### 3. Domain Events (DomainEvent.kt)

Event-Sourcing Infrastruktur für Domain-Driven Design.

```kotlin
interface DomainEvent {
    val eventId: Uuid
    val aggregateId: Uuid
    val eventType: String
    val occurredAt: Instant
    val version: Long
}

abstract class BaseDomainEvent(
    override val eventId: Uuid = uuid4(),
    override val aggregateId: Uuid,
    override val eventType: String,
    override val occurredAt: Instant = Clock.System.now(),
    override val version: Long
) : DomainEvent

// Event Publisher Interface
interface DomainEventPublisher {
    suspend fun publish(event: DomainEvent)
    suspend fun publishAll(events: List<DomainEvent>)
}

// Event Handler Interface
interface DomainEventHandler<T : DomainEvent> {
    suspend fun handle(event: T)
    fun canHandle(eventType: String): Boolean
}
```

### 4. Custom Serializers (Serializers.kt)

Spezialisierte Serializer für Kotlin-Typen.

```kotlin
object UuidSerializer : KSerializer<Uuid> {
    override val descriptor = PrimitiveSerialDescriptor("Uuid", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Uuid) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Uuid {
        return uuidFrom(decoder.decodeString())
    }
}

object KotlinInstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

object KotlinLocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }
}
```

## Core-Utils Komponenten

### 1. Fehlerbehandlung (Result.kt)

Funktionale Fehlerbehandlung ohne Exceptions.

```kotlin
sealed class Result<out T, out E> {
    data class Success<out T>(val value: T) : Result<T, Nothing>()
    data class Failure<out E>(val error: E) : Result<Nothing, E>()

    inline fun <R> map(transform: (T) -> R): Result<R, E> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    inline fun <R> flatMap(transform: (T) -> Result<R, E>): Result<R, E> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }

    inline fun mapError(transform: (E) -> E): Result<T, E> = when (this) {
        is Success -> this
        is Failure -> Failure(transform(error))
    }

    fun isSuccess(): Boolean = this is Success
    fun isFailure(): Boolean = this is Failure

    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    fun getOrElse(defaultValue: T): T = when (this) {
        is Success -> value
        is Failure -> defaultValue
    }
}

// Extension Functions
inline fun <T> Result<T, *>.onSuccess(action: (T) -> Unit): Result<T, *> {
    if (this is Result.Success) action(value)
    return this
}

inline fun <E> Result<*, E>.onFailure(action: (E) -> Unit): Result<*, E> {
    if (this is Result.Failure) action(error)
    return this
}
```

### 2. Konfiguration (AppConfig.kt, AppEnvironment.kt)

Zentrale Anwendungskonfiguration.

```kotlin
// AppEnvironment.kt
enum class AppEnvironment {
    DEVELOPMENT,
    TESTING,
    STAGING,
    PRODUCTION;

    companion object {
        fun fromString(env: String): AppEnvironment {
            return values().find { it.name.equals(env, ignoreCase = true) }
                ?: DEVELOPMENT
        }
    }
}

// AppConfig.kt
data class AppConfig(
    val environment: AppEnvironment,
    val applicationName: String,
    val version: String,
    val database: DatabaseConfig,
    val redis: RedisConfig,
    val kafka: KafkaConfig,
    val security: SecurityConfig,
    val monitoring: MonitoringConfig
) {
    companion object {
        fun load(): AppConfig {
            val environment = AppEnvironment.fromString(
                System.getenv("APP_ENVIRONMENT") ?: "development"
            )

            return AppConfig(
                environment = environment,
                applicationName = System.getenv("APP_NAME") ?: "meldestelle",
                version = System.getenv("APP_VERSION") ?: "1.0.0",
                database = DatabaseConfig.load(),
                redis = RedisConfig.load(),
                kafka = KafkaConfig.load(),
                security = SecurityConfig.load(),
                monitoring = MonitoringConfig.load()
            )
        }
    }
}
```

### 3. Datenbank-Utilities (DatabaseConfig.kt, DatabaseFactory.kt, DatabaseMigrator.kt)

Datenbank-Abstraktion und -Migration.

```kotlin
// DatabaseConfig.kt
data class DatabaseConfig(
    val url: String,
    val driver: String,
    val username: String,
    val password: String,
    val maxPoolSize: Int,
    val connectionTimeout: Duration,
    val migrationEnabled: Boolean
) {
    companion object {
        fun load(): DatabaseConfig {
            return DatabaseConfig(
                url = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/meldestelle",
                driver = System.getenv("DATABASE_DRIVER") ?: "org.postgresql.Driver",
                username = System.getenv("DATABASE_USERNAME") ?: "meldestelle",
                password = System.getenv("DATABASE_PASSWORD") ?: "password",
                maxPoolSize = System.getenv("DATABASE_MAX_POOL_SIZE")?.toInt() ?: 10,
                connectionTimeout = Duration.ofSeconds(
                    System.getenv("DATABASE_CONNECTION_TIMEOUT")?.toLong() ?: 30
                ),
                migrationEnabled = System.getenv("DATABASE_MIGRATION_ENABLED")?.toBoolean() ?: true
            )
        }
    }
}

// DatabaseFactory.kt
object DatabaseFactory {
    fun create(config: DatabaseConfig): Database {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.url
            driverClassName = config.driver
            username = config.username
            password = config.password
            maximumPoolSize = config.maxPoolSize
            connectionTimeout = config.connectionTimeout.toMillis()
        }

        val dataSource = HikariDataSource(hikariConfig)
        return Database.connect(dataSource)
    }
}

// DatabaseMigrator.kt
class DatabaseMigrator(private val database: Database) {
    suspend fun migrate() {
        database.useConnection { connection ->
            val flyway = Flyway.configure()
                .dataSource(connection.metaData.url, null, null)
                .load()

            flyway.migrate()
        }
    }

    suspend fun clean() {
        database.useConnection { connection ->
            val flyway = Flyway.configure()
                .dataSource(connection.metaData.url, null, null)
                .load()

            flyway.clean()
        }
    }
}
```

### 4. Validierung (ValidationUtils.kt, ValidationResult.kt, ApiValidationUtils.kt)

Umfassende Validierungsinfrastruktur.

```kotlin
// ValidationResult.kt
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList()
) {
    companion object {
        fun valid() = ValidationResult(true)
        fun invalid(errors: List<ValidationError>) = ValidationResult(false, errors)
        fun invalid(error: ValidationError) = ValidationResult(false, listOf(error))
    }

    fun and(other: ValidationResult): ValidationResult {
        return ValidationResult(
            isValid = this.isValid && other.isValid,
            errors = this.errors + other.errors
        )
    }
}

data class ValidationError(
    val field: String,
    val message: String,
    val code: String? = null
)

// ValidationUtils.kt
object ValidationUtils {
    fun validateEmail(email: String): ValidationResult {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return if (emailRegex.matches(email)) {
            ValidationResult.valid()
        } else {
            ValidationResult.invalid(ValidationError("email", "Invalid email format"))
        }
    }

    fun validateRequired(value: String?, fieldName: String): ValidationResult {
        return if (!value.isNullOrBlank()) {
            ValidationResult.valid()
        } else {
            ValidationResult.invalid(ValidationError(fieldName, "$fieldName is required"))
        }
    }

    fun validateLength(value: String?, fieldName: String, min: Int, max: Int): ValidationResult {
        return when {
            value == null -> ValidationResult.invalid(ValidationError(fieldName, "$fieldName is required"))
            value.length < min -> ValidationResult.invalid(ValidationError(fieldName, "$fieldName must be at least $min characters"))
            value.length > max -> ValidationResult.invalid(ValidationError(fieldName, "$fieldName must not exceed $max characters"))
            else -> ValidationResult.valid()
        }
    }

    fun validateUuid(value: String?, fieldName: String): ValidationResult {
        return try {
            if (value != null) {
                uuidFrom(value)
                ValidationResult.valid()
            } else {
                ValidationResult.invalid(ValidationError(fieldName, "$fieldName is required"))
            }
        } catch (e: Exception) {
            ValidationResult.invalid(ValidationError(fieldName, "Invalid UUID format"))
        }
    }
}
```

### 5. Service Discovery (ServiceRegistration.kt)

Service-Registrierung für Microservices.

```kotlin
data class ServiceInfo(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val healthCheckUrl: String,
    val tags: Set<String> = emptySet(),
    val metadata: Map<String, String> = emptyMap()
)

interface ServiceRegistry {
    suspend fun register(serviceInfo: ServiceInfo)
    suspend fun deregister(serviceId: String)
    suspend fun discover(serviceName: String): List<ServiceInfo>
    suspend fun getHealthyServices(serviceName: String): List<ServiceInfo>
}

class ConsulServiceRegistry(private val consulClient: ConsulClient) : ServiceRegistry {
    override suspend fun register(serviceInfo: ServiceInfo) {
        val service = NewService().apply {
            id = serviceInfo.id
            name = serviceInfo.name
            address = serviceInfo.host
            port = serviceInfo.port
            tags = serviceInfo.tags.toList()
            meta = serviceInfo.metadata
            check = NewService.Check().apply {
                http = serviceInfo.healthCheckUrl
                interval = "10s"
                timeout = "5s"
            }
        }

        consulClient.agentServiceRegister(service)
    }

    override suspend fun deregister(serviceId: String) {
        consulClient.agentServiceDeregister(serviceId)
    }

    override suspend fun discover(serviceName: String): List<ServiceInfo> {
        val services = consulClient.getHealthServices(serviceName, true, QueryParams.DEFAULT)
        return services.response.map { serviceHealth ->
            val service = serviceHealth.service
            ServiceInfo(
                id = service.id,
                name = service.service,
                host = service.address,
                port = service.port,
                healthCheckUrl = "http://${service.address}:${service.port}/actuator/health",
                tags = service.tags.toSet(),
                metadata = service.meta ?: emptyMap()
            )
        }
    }

    override suspend fun getHealthyServices(serviceName: String): List<ServiceInfo> {
        return discover(serviceName).filter { service ->
            // Additional health check logic if needed
            true
        }
    }
}
```

### 6. Serialisierung (Serialization.kt)

JSON-Serialisierung mit Kotlinx Serialization.

```kotlin
object JsonConfig {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
        coerceInputValues = true
        useAlternativeNames = false
    }

    val prettyJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
        coerceInputValues = true
        useAlternativeNames = false
    }
}

inline fun <reified T> T.toJson(): String {
    return JsonConfig.json.encodeToString(this)
}

inline fun <reified T> String.fromJson(): T {
    return JsonConfig.json.decodeFromString(this)
}

inline fun <reified T> T.toPrettyJson(): String {
    return JsonConfig.prettyJson.encodeToString(this)
}
```

## Verwendung in anderen Modulen

### Domain Models

```kotlin
// In members-domain
@Serializable
data class Member(
    @Serializable(with = UuidSerializer::class)
    val memberId: Uuid = uuid4(),

    var firstName: String,
    var lastName: String,
    var email: String,

    @Serializable(with = KotlinInstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),

    @Serializable(with = KotlinInstantSerializer::class)
    var updatedAt: Instant = Clock.System.now()
) {
    fun validate(): ValidationResult {
        return ValidationUtils.validateRequired(firstName, "firstName")
            .and(ValidationUtils.validateRequired(lastName, "lastName"))
            .and(ValidationUtils.validateEmail(email))
    }
}
```

### API Responses

```kotlin
// In API Controllers
@RestController
class MemberController {

    @GetMapping("/api/members/{id}")
    fun getMember(@PathVariable id: String): ApiResponse<Member> {
        return try {
            val member = memberService.findById(id)
            ApiResponse(data = member, success = true)
        } catch (e: Exception) {
            ApiResponse(
                success = false,
                message = "Member not found",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }
}
```

### Result-Type Usage

```kotlin
// In Use Cases
class CreateMemberUseCase {
    suspend fun execute(member: Member): Result<Member, ValidationError> {
        val validation = member.validate()

        return if (validation.isValid) {
            try {
                val savedMember = memberRepository.save(member)
                Result.Success(savedMember)
            } catch (e: Exception) {
                Result.Failure(ValidationError("system", "Failed to save member"))
            }
        } else {
            Result.Failure(validation.errors.first())
        }
    }
}
```

## Tests

### Unit Tests

```kotlin
class ValidationUtilsTest {

    @Test
    fun `should validate email correctly`() {
        val validEmail = "test@example.com"
        val invalidEmail = "invalid-email"

        assertTrue(ValidationUtils.validateEmail(validEmail).isValid)
        assertFalse(ValidationUtils.validateEmail(invalidEmail).isValid)
    }

    @Test
    fun `should validate required fields`() {
        val validValue = "test"
        val invalidValue = ""

        assertTrue(ValidationUtils.validateRequired(validValue, "field").isValid)
        assertFalse(ValidationUtils.validateRequired(invalidValue, "field").isValid)
    }
}

class ResultTest {

    @Test
    fun `should map success result`() {
        val result = Result.Success(5)
        val mapped = result.map { it * 2 }

        assertTrue(mapped.isSuccess())
        assertEquals(10, mapped.getOrNull())
    }

    @Test
    fun `should handle failure result`() {
        val result = Result.Failure("error")
        val mapped = result.map { it * 2 }

        assertTrue(mapped.isFailure())
        assertNull(mapped.getOrNull())
    }
}
```

## Konfiguration

### Gradle Dependencies

```kotlin
// core-domain/build.gradle.kts
dependencies {
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    api("com.benasher44:uuid:0.8.2")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

// core-utils/build.gradle.kts
dependencies {
    api(project(":core:core-domain"))

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
    implementation("org.flywaydb:flyway-core:9.22.3")
    implementation("com.orbitz.consul:consul-client:1.5.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.testcontainers:postgresql:1.19.1")
}
```

## Best Practices

### 1. Shared Kernel Prinzipien

- **Minimale Oberfläche**: Nur wirklich gemeinsame Konzepte
- **Stabile APIs**: Änderungen beeinflussen alle Module
- **Versionierung**: Sorgfältige Versionierung bei Breaking Changes
- **Dokumentation**: Umfassende Dokumentation für alle Komponenten

### 2. Fehlerbehandlung

- **Result-Type verwenden**: Statt Exceptions für erwartete Fehler
- **Validierung**: Frühe Validierung mit ValidationResult
- **Logging**: Strukturiertes Logging für Debugging

### 3. Serialisierung

- **Custom Serializers**: Für spezielle Kotlin-Typen
- **Versionierung**: Schema-Evolution berücksichtigen
- **Performance**: Effiziente Serialisierung für häufige Operationen

## Zukünftige Erweiterungen

1. **Event Sourcing Enhancements** - Erweiterte Event Store Features
2. **Distributed Tracing** - OpenTelemetry Integration
3. **Metrics Collection** - Micrometer Integration
4. **Configuration Management** - Externalized Configuration
5. **Security Utilities** - Encryption/Decryption Utilities
6. **Caching Abstractions** - Cache-Provider Abstractions
7. **Async Utilities** - Coroutines Utilities
8. **Testing Utilities** - Test-Helpers und Fixtures

---

**Letzte Aktualisierung**: 25. Juli 2025

Für weitere Informationen zur Gesamtarchitektur siehe [README.md](../README.md).
