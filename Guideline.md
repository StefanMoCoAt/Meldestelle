# Meldestelle_Pro: Entwicklungs-Guideline

**Status:** Finalisiert & Verbindlich
**Version:** 2.0
**Stand:** August 2025

## 1. Vision & Architektonische Grundpfeiler

Dieses Dokument definiert die verbindlichen technischen Richtlinien und Qualitätsstandards für das Projekt "Meldestelle_Pro". Ziel ist die Schaffung einer modernen, skalierbaren und wartbaren Plattform für den Pferdesport.

Unsere Architektur basiert auf **vier Säulen**:

1. **Modularität & Skalierbarkeit** durch eine **Microservices-Architektur**
2. **Fachlichkeit im Code** durch **Domain-Driven Design (DDD)**
3. **Entkopplung & Resilienz** durch eine **ereignisgesteuerte Architektur (EDA)**
4. **Effizienz & Konsistenz** durch eine **Multiplattform-Client-Strategie (KMP)**

> **Grundsatz:** Jede Code-Änderung muss diese vier Grundprinzipien respektieren.

---

## 2. Backend-Entwicklungsrichtlinien

#### 2.1. Microservice-Struktur (Clean Architecture)

**Jeder fachliche Microservice (z.B. :members, :events) muss der etablierten 4-Layer-Struktur folgen:**

* **`:*-api`**: Definiert die öffentliche Schnittstelle des Service (REST-Controller, DTOs).
* **`:*-application`**: Enthält die Anwendungslogik und Use Cases. Hier werden die Repositories orchestriert.
* **`:*-domain`**: Das Herz des Service. Enthält die reinen, von Frameworks unabhängigen Domänenmodelle, Geschäftsregeln
  und Repository-Interfaces.
* **`:*-infrastructure`**: Die technische Implementierung der Interfaces aus der Domänenschicht (z.B. Datenbankzugriff
  mit Exposed).

#### 2.2. Domain-Driven Design (DDD) in der Praxis

* **Shared Kernel (`:core`-Modul):** Das `:core`-Modul ist heilig. Es darf **ausschließlich** fundamentalen,
  domänen-agnostischen Code enthalten. Fachspezifische Konzepte gehören in ihre jeweilige Domäne.
* **Repository-Pattern mit `Result`:** Jede Repository-Methode muss das `Result`-Pattern verwenden, um Erfolgs- und
  Fehlerfälle explizit und typsicher zu behandeln.

    ```kotlin
    // Repository mit Result-Pattern
    interface MemberRepository {
        suspend fun findById(id: MemberId): Result<Member?, RepositoryError>
        suspend fun save(member: Member): Result<Unit, RepositoryError>
        suspend fun findByEmail(email: EmailAddress): Result<List<Member>, RepositoryError>
    }
    ```

#### 2.3. Core-Modul Spezifikation

Das `:core`-Modul definiert die fundamentalen Bausteine der gesamten Anwendung:

* **Result Extensions:** Utility-Funktionen für typsichere Fehlerbehandlung
* **Common Types:** Basistypen für alle Domänen
* **Shared Utilities:** Plattformunabhängige Hilfsfunktionen

    ```kotlin
    // Result Extensions im core-utils Modul
    inline fun <T, E, R> Result<T, E>.mapError(transform: (E) -> R): Result<T, R> =
        when (this) {
            is Result.Success -> Result.Success(value)
            is Result.Failure -> Result.Failure(transform(error))
        }

    inline fun <T, E> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E> =
        also { if (it is Result.Failure) action(it.error) }

    // Common Domain Types
    @JvmInline
    value class CorrelationId(val value: UUID) {
        companion object {
            fun generate(): CorrelationId = CorrelationId(UUID.randomUUID())
            fun of(value: String): Result<CorrelationId, ValidationError> =
                runCatching { UUID.fromString(value) }
                    .map { CorrelationId(it) }
                    .mapError { ValidationError.InvalidUUID("Invalid correlation ID: $value") }
        }
    }

    // Konkrete Error-Implementierungen
    sealed class ValidationError(code: String, message: String) : DomainError(code, message) {
        data class InvalidUUID(override val message: String) :
            ValidationError("INVALID_UUID", message)
        data class InvalidEmail(override val message: String) :
            ValidationError("INVALID_EMAIL", message)
        data class InvalidLength(val field: String, val min: Int, val max: Int) :
            ValidationError("INVALID_LENGTH", "Field $field must be between $min and $max characters")
    }
    ```

#### 2.4. Messaging & Event-Naming

* **Asynchrone Kommunikation:** Die bevorzugte Kommunikationsmethode ist asynchron über Kafka.
* **Event-Naming Convention:** Domänen-Events folgen dem Muster `{Domain}{Entity}{Action}Event`.

    ```kotlin
    // Event-Naming Convention
    sealed class DomainEvent(
        val aggregateId: String,
        val version: Long,
        val timestamp: Instant = Instant.now()
    ) {
        // Pattern: {Domain}{Entity}{Action}Event
        data class MemberPersonalDataUpdatedEvent(
            val memberId: MemberId,
            val personalData: PersonalData
        ) : DomainEvent(memberId.value, version)
    }
    ```

---

## 3. Frontend-Entwicklungsrichtlinien

#### 3.1. Architekturmuster: MVVM & KMP

Das Frontend folgt konsequent dem **Model-View-ViewModel (MVVM)**-Muster und der **Kotlin Multiplatform (KMP)**-Strategie:

* **Model & ViewModel:** Die gesamte Geschäftslogik, der Zustand und die API-Aufrufe leben im `:client:common-ui`-Modul und sind plattformunabhängig.
* **View:** Die Benutzeroberfläche wird mit **Compose Multiplatform** im `:client:common-ui`-Modul implementiert.

#### 3.2. State Management

**Unidirectional Data Flow mit MVI-Pattern:**

```kotlin
// State Management Pattern
@Stable
data class MemberListUiState(
    val members: List<Member> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

sealed class MemberListIntent {
    object LoadMembers : MemberListIntent()
    data class SearchMembers(val query: String) : MemberListIntent()
    data class DeleteMember(val memberId: MemberId) : MemberListIntent()
}

class MemberListViewModel(
    private val memberRepository: MemberRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MemberListUiState())
    val uiState: StateFlow<MemberListUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: MemberListIntent) {
        when (intent) {
            is MemberListIntent.LoadMembers -> loadMembers()
            is MemberListIntent.SearchMembers -> searchMembers(intent.query)
            is MemberListIntent.DeleteMember -> deleteMember(intent.memberId)
        }
    }
}
```

#### 3.3. Navigation Architecture

**Compose Navigation mit typsicheren Routes:**

```kotlin
// Navigation Definition
@Serializable
sealed class Screen {
    @Serializable
    object MemberList : Screen()

    @Serializable
    data class MemberDetail(val memberId: String) : Screen()

    @Serializable
    data class EventRegistration(val eventId: String, val memberId: String) : Screen()
}

// Navigation Router
class NavigationRouter {
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    fun navigateTo(screen: Screen) {
        _navigationEvents.tryEmit(NavigationEvent.NavigateTo(screen))
    }

    fun navigateBack() {
        _navigationEvents.tryEmit(NavigationEvent.NavigateBack)
    }
}
```

#### 3.4. Vertikale Schnitte (Features)

Der UI-Code wird nach **fachlichen Features** strukturiert. Ein Feature (z.B. "Nennungsabwicklung") hat sein eigenes Verzeichnis und enthält alle zugehörigen Views, ViewModels und Models:

```
client/common-ui/src/commonMain/kotlin/
├── features/
│   ├── members/
│   │   ├── presentation/
│   │   │   ├── MemberListViewModel.kt
│   │   │   ├── MemberDetailViewModel.kt
│   │   │   └── MemberUiState.kt
│   │   ├── ui/
│   │   │   ├── MemberListScreen.kt
│   │   │   ├── MemberDetailScreen.kt
│   │   │   └── components/
│   │   └── domain/
│   │       └── MemberUseCases.kt
│   └── events/
│       ├── presentation/
│       ├── ui/
│       └── domain/
```

#### 3.5. Platform-spezifische Implementierungen

**Desktop-spezifische Features:**

```kotlin
// Desktop-specific implementations
actual class PlatformFileManager {
    actual suspend fun selectFile(): Result<File?, FileError> {
        return withContext(Dispatchers.IO) {
            try {
                val fileChooser = JFileChooser()
                val result = fileChooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    Result.Success(fileChooser.selectedFile)
                } else {
                    Result.Success(null)
                }
            } catch (e: Exception) {
                Result.Failure(FileError.SelectionFailed(e.message))
            }
        }
    }
}

// Web-specific implementations
actual class PlatformFileManager {
    actual suspend fun selectFile(): Result<File?, FileError> {
        return try {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.click()
            // Implementation für Web File API
            Result.Success(null) // Simplified
        } catch (e: Exception) {
            Result.Failure(FileError.SelectionFailed(e.message))
        }
    }
}
```

---

## 4. API-Versioning & Kompatibilität

#### 4.1. Versioning-Strategie

**Header-basierte Versionierung (Empfohlen):**

```kotlin
// API Version Header
@RestController
@RequestMapping("/api/members")
class MemberController {

    @GetMapping
    fun getMembers(
        @RequestHeader(value = "API-Version", defaultValue = "1.0") version: String,
        @RequestParam query: String?
    ): ResponseEntity<List<MemberDto>> {
        return when (version) {
            "1.0" -> memberService.getMembersV1(query)
            "2.0" -> memberService.getMembersV2(query)
            else -> ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build()
        }
    }
}

// Client-seitige Versionierung
class ApiClient {
    companion object {
        const val CURRENT_API_VERSION = "2.0"
        const val MIN_SUPPORTED_VERSION = "1.0"
    }

    private val defaultHeaders = mapOf(
        "API-Version" to CURRENT_API_VERSION,
        "Accept" to "application/json"
    )
}
```

#### 4.2. Backward Compatibility Rules

* **Breaking Changes:** Erfordern eine neue Major-Version (1.x → 2.x)
* **Additive Changes:** Können in Minor-Versionen erfolgen (1.0 → 1.1)
* **Bug Fixes:** Patch-Versionen (1.0.0 → 1.0.1)

```kotlin
// Compatibility Matrix
object ApiCompatibility {
    val supportedVersions = mapOf(
        "2.0" to ApiVersionConfig(
            deprecated = false,
            sunsetDate = null,
            features = setOf("advanced-search", "bulk-operations")
        ),
        "1.0" to ApiVersionConfig(
            deprecated = true,
            sunsetDate = LocalDate.of(2025, 12, 31),
            features = setOf("basic-search")
        )
    )
}
```

#### 4.3. Versioning Lifecycle Management

* **Deprecation Notice:** Mindestens 6 Monate vor Entfernung
* **Documentation:** Alle Versionen müssen in OpenAPI dokumentiert sein
* **Migration Guide:** Für jede Major-Version erforderlich

---

## 5. Allgemeine Qualitätsstandards

#### 4.1. Code-Qualität & Kotlin-Konventionen

* **Value Classes für Typsicherheit:** Primitive Typen (UUID, String, Long) für IDs oder spezifische Werte müssen in
  typsichere `value class`-Wrapper gekapselt werden, um Fehler zu vermeiden.

    ```kotlin
    // Ergänzung für Value Objects
    @JvmInline
    value class MemberId(val value: UUID) {
        companion object {
            fun of(value: String): Result<MemberId, ValidationError> =
                runCatching { UUID.fromString(value) }
                    .map { MemberId(it) }
                    .mapError { ValidationError.INVALID_UUID }
        }
    }
    ```

#### 4.2. Error-Handling

* **`Result`-Pattern statt Exceptions:** Für erwartbare Geschäftsfehler ist das `Result`-Pattern zu verwenden.
* **Spezifische Fehler-Hierarchie:** Wir verwenden eine `sealed class`-Hierarchie, um Fehlerarten klar zu
  kategorisieren.

    ```kotlin
    // Spezifische Error-Hierarchie definieren
    sealed class DomainError(val code: String, val message: String)
    sealed class ValidationError(code: String, message: String) : DomainError(code, message)
    sealed class BusinessError(code: String, message: String) : DomainError(code, message)
    sealed class TechnicalError(code: String, message: String) : DomainError(code, message)
    ```

#### 4.3. Testing

* **Testcontainers als Goldstandard:** Jede Interaktion mit externer Infrastruktur (DB, Cache, Broker) **muss** mit *
  *Testcontainers** getestet werden.
* **Mocking für Isolation:** Abhängigkeiten innerhalb von Tests werden mit Mocking-Frameworks (z.B. MockK) isoliert, um
  den Testfokus zu schärfen.

    ```kotlin
    // Testcontainers-Pattern für Infrastruktur-Tests
    @TestConfiguration
    class KafkaTestConfig {
        @Bean
        @Primary
        fun kafkaEventPublisher(): KafkaEventPublisher = mockk()
    }
    ```

---

### 5. Infrastruktur-Spezifikationen

#### 5.1. Kafka-Konfiguration

Die Konfiguration für Producer und Consumer muss produktionsreife Einstellungen für Zuverlässigkeit und Datenkonsistenz
verwenden.

   ```YAML
    # Ergänzung für application.yml
    kafka:
        producer:
            acks: all
            enable-idempotence: true
            max-in-flight-requests-per-connection: 1
        consumer:
            group-id-prefix: "meldestelle-${spring.application.name}"
            auto-offset-reset: earliest
            enable-auto-commit: false
   ```

#### 5.2. Datenbank-Migrationen mit Flyway

Migrations-Skripte müssen einer klaren Namenskonvention folgen.

* **Pattern:**`V{version}__{description}.sql` (z.B., `V001__Create_member_tables.sql`)

* **Repeatable:**`R__{description}.sql` (z.B., `R__Update_member_view.sql`)

---

## 6. Monitoring & Observability

#### 6.1. Structured Logging

Logs müssen als strukturierte Daten (z.B. JSON) ausgegeben werden und immer eine Korrelations-ID enthalten, um Anfragen über Service-Grenzen hinweg verfolgen zu können.

```kotlin
// Korrigierte Logging-Syntax
@Component
class MemberService {
    private val logger = KotlinLogging.logger {}

    suspend fun createMember(command: CreateMemberCommand) {
        logger.info {
            mapOf(
                "message" to "Creating member",
                "memberId" to command.memberId.value,
                "operation" to "create_member",
                "correlationId" to MDC.get("correlationId")
            ).toString()
        }
    }
}
```

#### 6.2. Service Level Indicators (SLIs) & Objectives (SLOs)

**Definierte SLIs für alle Services:**

```kotlin
// SLI/SLO Definitionen
object ServiceLevelIndicators {

    // Availability SLIs
    data class AvailabilitySLI(
        val serviceName: String,
        val targetUptime: Double = 0.995, // 99.5%
        val measurementWindow: Duration = Duration.ofDays(30)
    )

    // Latency SLIs
    data class LatencySLI(
        val serviceName: String,
        val percentile: Double = 0.95, // P95
        val targetLatency: Duration = Duration.ofMillis(500),
        val measurementWindow: Duration = Duration.ofMinutes(5)
    )

    // Error Rate SLIs
    data class ErrorRateSLI(
        val serviceName: String,
        val maxErrorRate: Double = 0.001, // 0.1%
        val measurementWindow: Duration = Duration.ofMinutes(5)
    )
}

// SLO Monitoring
@Component
class SLOMonitor(private val meterRegistry: MeterRegistry) {

    private val requestDuration = Timer.builder("http.request.duration")
        .description("HTTP request duration")
        .register(meterRegistry)

    private val errorRate = Counter.builder("http.request.errors")
        .description("HTTP request errors")
        .register(meterRegistry)

    fun recordRequest(duration: Duration, isError: Boolean) {
        requestDuration.record(duration)
        if (isError) errorRate.increment()
    }
}
```

#### 6.3. Business & Technical Metrics

**Umfassende Metriken-Strategie:**

```kotlin
// Business Metrics
@Component
class BusinessMetrics(meterRegistry: MeterRegistry) {

    // Fachliche Metriken
    private val memberRegistrations = Counter.builder("business.member.registrations.total")
        .description("Total number of member registrations")
        .tag("service", "members")
        .register(meterRegistry)

    private val eventParticipations = Counter.builder("business.event.participations.total")
        .description("Total event participations")
        .tag("service", "events")
        .register(meterRegistry)

    private val paymentTransactions = Timer.builder("business.payment.transaction.duration")
        .description("Payment transaction processing time")
        .tag("service", "payments")
        .register(meterRegistry)

    // Gauge für aktuelle Werte
    private val activeSessions = Gauge.builder("business.active.sessions")
        .description("Currently active user sessions")
        .register(meterRegistry) { getActiveSessionCount() }
}

// Technical Metrics
@Component
class TechnicalMetrics(meterRegistry: MeterRegistry) {

    // Database Metriken
    private val dbConnectionPool = Gauge.builder("database.connection.pool.active")
        .description("Active database connections")
        .register(meterRegistry) { getActiveConnections() }

    // Kafka Metriken
    private val kafkaLag = Gauge.builder("kafka.consumer.lag")
        .description("Kafka consumer lag")
        .register(meterRegistry) { getConsumerLag() }

    // Cache Metriken
    private val cacheHitRate = Gauge.builder("cache.hit.rate")
        .description("Cache hit rate percentage")
        .register(meterRegistry) { getCacheHitRate() }
}
```

#### 6.4. Alerting Strategy

**Alert-Definitionen basierend auf SLOs:**

```yaml
# Prometheus Alert Rules
groups:
  - name: slo.rules
    rules:
      - alert: HighErrorRate
        expr: rate(http_request_errors_total[5m]) > 0.001
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"

      - alert: HighLatency
        expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 0.5
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High latency detected"
```

---

## 7. Zusätzliche Richtlinien

#### 7.1. Security

Die Autorisierung muss auf Methodenebene mit Spring Security Annotations (`@PreAuthorize`) durchgesetzt werden, um eine feingranulare Zugriffskontrolle zu gewährleisten.

**JWT Implementation:**

```kotlin
// JWT Configuration
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        return JwtAuthenticationFilter()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/members/**").hasRole("USER")
                    .requestMatchers(HttpMethod.POST, "/api/members/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}

// Method-level Security
@RestController
@RequestMapping("/api/members")
class MemberController {

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or @memberService.isOwner(#id, authentication.name)")
    fun getMember(@PathVariable id: String): MemberDto {
        // Implementation
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#memberDto, 'CREATE')")
    fun createMember(@RequestBody memberDto: MemberDto): MemberDto {
        // Implementation
    }
}
```

**OAuth2 Integration:**

```kotlin
// OAuth2 Resource Server Configuration
@Configuration
class OAuth2Config {

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return NimbusJwtDecoder.withJwkSetUri("https://auth-provider/.well-known/jwks.json").build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val authorities = jwt.getClaimAsStringList("authorities") ?: emptyList()
            authorities.map { SimpleGrantedAuthority("ROLE_$it") }
        }
        return converter
    }
}

// Custom Permission Evaluator
@Component("memberService")
class MemberPermissionEvaluator {

    fun isOwner(memberId: String, username: String): Boolean {
        return memberRepository.findById(memberId)
            ?.let { it.email == username }
            ?: false
    }

    fun hasPermission(target: Any, permission: String): Boolean {
        // Custom permission logic
        return when (permission) {
            "CREATE" -> hasCreatePermission(target)
            "UPDATE" -> hasUpdatePermission(target)
            else -> false
        }
    }
}
```

**Rate Limiting:**

```kotlin
// Rate Limiting Configuration
@Configuration
class RateLimitConfig {

    @Bean
    fun rateLimitFilter(): RateLimitFilter {
        return RateLimitFilter(
            rateLimiters = mapOf(
                "/api/auth/login" to RateLimiter.create(5.0), // 5 requests per second
                "/api/members" to RateLimiter.create(100.0),   // 100 requests per second
                "/api/events" to RateLimiter.create(50.0)      // 50 requests per second
            )
        )
    }
}

// Custom Rate Limit Annotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimit(
    val requestsPerSecond: Double = 10.0,
    val burstCapacity: Int = 20
)

// Usage
@RestController
class AuthController {

    @PostMapping("/login")
    @RateLimit(requestsPerSecond = 5.0, burstCapacity = 10)
    fun login(@RequestBody loginRequest: LoginRequest): AuthResponse {
        // Implementation
    }
}
```

#### 7.2. Performance

Cache-Strategien (`@Cacheable`, `@CacheEvict`) **müssen gezielt eingesetzt werden**, um die Latenz bei häufigen Lesezugriffen zu minimieren.

#### 7.3. Dokumentation

Alle öffentlichen REST-Endpunkte müssen mit OpenAPI-Annotationen (`@Operation`, `@ApiResponse`) dokumentiert werden, um eine klare und interaktive API-Dokumentation zu generieren.
