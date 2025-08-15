# Meldestelle_Pro: Entwicklungs-Guideline

## Status: Finalisiert & Verbindlich

### 1. Vision & Architektonische Grundpfeiler

Dieses Dokument definiert die verbindlichen technischen Richtlinien und Qualitätsstandards für das Projekt "
Meldestelle_Pro". Ziel ist die Schaffung einer modernen, skalierbaren und wartbaren Plattform für den Pferdesport.
Unsere Architektur basiert auf vier Säulen:

1. **Modularität & Skalierbarkeit** durch eine **Microservices-Architektur.**

2. **Fachlichkeit im Code** durch **Domain-Driven Design (DDD).**

3. **Entkopplung & Resilienz** durch eine **ereignisgesteuerte Architektur (EDA).**

4. **Effizienz & Konsistenz** durch eine **Multiplattform-Client-Strategie (KMP).**

Jede Code-Änderung muss diese vier Grundprinien respektieren.

---

### 2. Backend-Entwicklungsrichtlinien

#### 2.1. Microservice-Struktur (Clean Architecture)

**Jeder fachliche Microservice (z.B. :members, :events) muss der etablierten 4-Layer-Struktur folgen:**

* **`:*-api`: Definiert die öffentliche Schnittstelle des Service (REST-Controller, DTOs).**

* **`:*-application`: Enthält die Anwendungslogik und Use Cases. Hier werden die Repositories orchestriert.**

* **`:*-domain`: Das Herz des Service. Enthält die reinen, von Frameworks unabhängigen Domänenmodelle, Geschäftsregeln
  und Repository-Interfaces.**

* **`:*-infrastructure`: Die technische Implementierung der Interfaces aus der Domänenschicht (z.B. Datenbankzugriff mit
  Exposed).**

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

#### 2.3. Messaging & Event-Naming

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

### 3. Frontend-Entwicklungsrichtlinien

#### 3.1. Architekturmuster: MVVM & KMP

Das Frontend folgt konsequent dem **Model-View-ViewModel (MVVM)**-Muster und der **Kotlin Multiplatform (KMP)**
-Strategie:

* **Model & ViewModel:** Die gesamte Geschäftslogik, der Zustand und die API-Aufrufe leben im `:client:common-ui`-Modul
  und sind plattformunabhängig.

* **View:** Die Benutzeroberfläche wird mit **Compose Multiplatform* im `:client:common-ui`-Modul implementiert.

#### 3.2. Vertikale Schnitte (Features)

Der UI-Code wird nach **fachlichen Features** strukturiert. Ein Feature (z. B. "Nennungsabwicklung") hat sein eigenes
Verzeichnis und enthält alle zugehörigen Views, ViewModels und Models.

---

### 4. Allgemeine Qualitätsstandards

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

### 6. Monitoring & Observability

#### 6.1. Structured Logging

Logs müssen als strukturierte Daten (z.B. JSON) ausgegeben werden und immer eine Korrelations-ID enthalten, um Anfragen
über Service-Grenzen hinweg verfolgen zu können.

```Kotlin
// Ergänzung zur Guideline
@Component
class MemberService {
    private val logger = KotlinLogging.logger {}

    suspend fun createMember(command: CreateMemberCommand) {
        logger.info {
            "Creating member" with mapOf(
                "memberId" to command.memberId.value,
                "operation" to "create_member",
                "correlationId" to MDC.get("correlationId")
            )
        }
    }
}
```

#### 6.2. Metrics

Es müssen sowohl technische als auch fachliche Metriken erfasst werden.

```Kotlin
// Spezifische Business-Metriken definieren
@Component
class BusinessMetrics(meterRegistry: MeterRegistry) {
    private val memberRegistrations = Counter.builder("business.member.registrations.total")
        .description("Total number of member registrations")
        .tag("service", "members")
        .register(meterRegistry)
}
```

---

### 7. Zusätzliche Richtlinien

#### 7.1. Security

Die Autorisierung muss auf Methodenebene mit Spring Security Annotations (`@PreAuthorize`) durchgesetzt werden, um eine
feingranulare Zugriffskontrolle zu gewährleisten.

#### 7.2. Performance

Cache-Strategien (`@Cacheable`, `@CacheEvict`) **müssen gezielt eingesetzt werden**, um die Latenz bei häufigen
Lesezugriffen zu minimieren.

#### 7.3. Dokumentation

Alle öffentlichen REST-Endpunkte müssen mit OpenAPI-Annotationen (`@Operation`, `@ApiResponse`) dokumentiert werden, um
eine klare und interaktive API-Dokumentation zu generieren.
