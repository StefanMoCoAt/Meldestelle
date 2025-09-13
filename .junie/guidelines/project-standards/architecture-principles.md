# Architecture Principles und Grundsätze

---
guideline_type: "project-standards"
scope: "architecture-principles"
audience: ["developers", "architects", "ai-assistants"]
last_updated: "2025-09-13"
dependencies: ["master-guideline.md"]
related_files: ["build.gradle.kts", "settings.gradle.kts", "docker-compose.yml"]
ai_context: "Architectural foundations, microservices patterns, DDD principles, event-driven architecture, and multiplatform strategy"
---

## 🏗️ Vision & Architektonische Grundpfeiler

Dieses Dokument definiert die verbindlichen technischen Richtlinien und Qualitätsstandards für das Projekt "Meldestelle_Pro". Ziel ist die Schaffung einer modernen, skalierbaren und wartbaren Plattform für den Pferdesport.

> **🤖 AI-Assistant Hinweis:**
> Die Architektur basiert auf vier Kernsäulen:
> - **Microservices:** Modularität & Skalierbarkeit
> - **DDD:** Fachlichkeit im Code
> - **EDA:** Ereignisgesteuerte Entkopplung
> - **KMP:** Kotlin Multiplatform für Effizienz

### Die vier Säulen der Architektur

1. **Modularität & Skalierbarkeit** durch eine **Microservices-Architektur**
2. **Fachlichkeit im Code** durch **Domain-Driven Design (DDD)**
3. **Entkopplung & Resilienz** durch eine **ereignisgesteuerte Architektur (EDA)**
4. **Effizienz & Konsistenz** durch eine **Multiplattform-Client-Strategie (KMP)**

> **Grundsatz:** Jede Code-Änderung muss diese vier Grundprinzipien respektieren.

## 🎯 AI-Assistenten: Architektur-Schnellreferenz

### Architektur-Säulen im Detail

| Säule | Technologie | Zweck | Umsetzung |
|-------|------------|-------|-----------|
| Microservices | Spring Boot, Docker | Modularität & Skalierbarkeit | Service-per-Domain-Pattern |
| DDD | Kotlin, Clean Architecture | Fachlichkeit im Code | Bounded Contexts, Domain Events |
| EDA | Kafka, Events | Entkopplung & Resilienz | Asynchrone Kommunikation |
| KMP | Kotlin Multiplatform | Effizienz & Konsistenz | Shared Business Logic |

## 🔧 Backend-Entwicklungsrichtlinien

### Microservice-Struktur (Clean Architecture)

Jeder fachliche Microservice folgt der 4-Layer-Struktur (`api`, `application`, `domain`, `infrastructure`).

```
service-name/
├── service-name-api/          # REST-Endpoints, DTOs
├── service-name-application/  # Use Cases, Commands, Queries
├── service-name-domain/       # Domain Models, Events, Services
└── service-name-infrastructure/ # Repositories, External Services
```

#### Layer-Verantwortlichkeiten

**API Layer (`-api`):**
```kotlin
@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    private val memberService: MemberService
) {
    @PostMapping
    fun createMember(@RequestBody request: CreateMemberRequest): ResponseEntity<MemberResponse> {
        val command = CreateMemberCommand(
            name = request.name,
            email = request.email,
            licenseNumber = request.licenseNumber
        )

        return when (val result = memberService.createMember(command)) {
            is Result.Success -> ResponseEntity.ok(result.value.toResponse())
            is Result.Failure -> ResponseEntity.badRequest().body(result.error.toErrorResponse())
        }
    }
}
```

**Application Layer (`-application`):**
```kotlin
@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val eventPublisher: EventPublisher
) {
    suspend fun createMember(command: CreateMemberCommand): Result<Member, BusinessError> {
        // Validation
        val validationResult = validateCreateMemberCommand(command)
        if (validationResult is Result.Failure) {
            return validationResult
        }

        // Business Logic
        val member = Member.create(
            name = command.name,
            email = command.email,
            licenseNumber = command.licenseNumber
        )

        // Persistence
        return memberRepository.save(member).map {
            // Event Publishing
            eventPublisher.publish(MemberCreatedEvent(member))
            member
        }
    }
}
```

**Domain Layer (`-domain`):**
```kotlin
@JvmInline
value class MemberId(val value: UUID) {
    companion object {
        fun generate(): MemberId = MemberId(UUID.randomUUID())
    }
}

data class Member private constructor(
    val id: MemberId,
    val name: String,
    val email: Email,
    val licenseNumber: LicenseNumber,
    val status: MemberStatus = MemberStatus.PENDING
) {
    companion object {
        fun create(
            name: String,
            email: String,
            licenseNumber: String
        ): Result<Member, ValidationError> {
            return Result.Success(
                Member(
                    id = MemberId.generate(),
                    name = name,
                    email = Email.of(email).getOrThrow(),
                    licenseNumber = LicenseNumber.of(licenseNumber).getOrThrow()
                )
            )
        }
    }

    fun activate(): Member = copy(status = MemberStatus.ACTIVE)
    fun suspend(): Member = copy(status = MemberStatus.SUSPENDED)
}
```

**Infrastructure Layer (`-infrastructure`):**
```kotlin
@Repository
class PostgresMemberRepository(
    private val jdbcTemplate: JdbcTemplate
) : MemberRepository {

    override suspend fun save(member: Member): Result<Unit, RepositoryError> {
        return try {
            jdbcTemplate.update(
                "INSERT INTO members (id, name, email, license_number, status) VALUES (?, ?, ?, ?, ?)",
                member.id.value,
                member.name,
                member.email.value,
                member.licenseNumber.value,
                member.status.name
            )
            Result.Success(Unit)
        } catch (e: DataAccessException) {
            Result.Failure(RepositoryError.DATABASE_ERROR)
        }
    }

    override suspend fun findById(id: MemberId): Result<Member?, RepositoryError> {
        return try {
            val member = jdbcTemplate.queryForObject(
                "SELECT * FROM members WHERE id = ?",
                arrayOf(id.value)
            ) { rs, _ ->
                Member(
                    id = MemberId(UUID.fromString(rs.getString("id"))),
                    name = rs.getString("name"),
                    email = Email.of(rs.getString("email")).getOrThrow(),
                    licenseNumber = LicenseNumber.of(rs.getString("license_number")).getOrThrow(),
                    status = MemberStatus.valueOf(rs.getString("status"))
                )
            }
            Result.Success(member)
        } catch (e: EmptyResultDataAccessException) {
            Result.Success(null)
        } catch (e: DataAccessException) {
            Result.Failure(RepositoryError.DATABASE_ERROR)
        }
    }
}
```

### Repository-Pattern

Jede Repository-Methode muss das `Result`-Pattern verwenden.

```kotlin
interface MemberRepository {
    suspend fun findById(id: MemberId): Result<Member?, RepositoryError>
    suspend fun save(member: Member): Result<Unit, RepositoryError>
    suspend fun findByEmail(email: Email): Result<Member?, RepositoryError>
    suspend fun findByLicenseNumber(licenseNumber: LicenseNumber): Result<Member?, RepositoryError>
    suspend fun findAll(pageable: Pageable): Result<Page<Member>, RepositoryError>
}
```

### Messaging & Event-Naming

Event-Naming Convention: Domänen-Events folgen dem Muster `{Domain}{Entity}{Action}Event`.

```kotlin
data class MemberPersonalDataUpdatedEvent(
    val memberId: MemberId,
    val oldName: String,
    val newName: String,
    val oldEmail: Email,
    val newEmail: Email,
    val updatedAt: Instant = Instant.now(),
    val correlationId: String = MDC.get("correlationId") ?: UUID.randomUUID().toString()
) : DomainEvent {
    override val eventType: String = "member.personal-data.updated"
    override val aggregateId: String = memberId.value.toString()
    override val version: Int = 1
}
```

## 📱 Frontend-Entwicklungsrichtlinien

Das Frontend folgt konsequent dem **Model-View-ViewModel (MVVM)**-Muster und der **Kotlin Multiplatform (KMP)**-Strategie. Der UI-Code wird nach **fachlichen Features** (vertikale Schnitte) strukturiert.

### Multiplatform-Struktur

```
client/
├── src/commonMain/kotlin/         # Shared Business Logic
│   ├── domain/                    # Domain Models
│   ├── data/                      # Repositories, API-Clients
│   ├── presentation/              # ViewModels, UI-States
│   └── ui/                        # Shared UI-Components
├── src/jvmMain/kotlin/            # Desktop-spezifischer Code
│   └── ui/                        # Desktop UI-Adaptierungen
└── src/wasmJsMain/kotlin/         # Web-spezifischer Code
    └── ui/                        # Web UI-Adaptierungen
```

### MVVM-Implementation

**Shared ViewModel (commonMain):**
```kotlin
class MemberListViewModel(
    private val memberRepository: MemberRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemberListUiState())
    val uiState: StateFlow<MemberListUiState> = _uiState.asStateFlow()

    fun loadMembers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = memberRepository.getAllMembers()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        members = result.value,
                        error = null
                    )
                }
                is Result.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.error.message
                    )
                }
            }
        }
    }
}

data class MemberListUiState(
    val isLoading: Boolean = false,
    val members: List<Member> = emptyList(),
    val error: String? = null
)
```

**Shared UI-Component (commonMain):**
```kotlin
@Composable
fun MemberListScreen(
    viewModel: MemberListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMembers()
    }

    Column {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        LazyColumn {
            items(uiState.members) { member ->
                MemberCard(
                    member = member,
                    onMemberClick = { /* Handle click */ }
                )
            }
        }
    }
}
```

## 🎯 Domain-Driven Design (DDD) Patterns

### Bounded Contexts

```
Meldestelle-Domain/
├── member-context/               # Mitgliederverwaltung
├── tournament-context/           # Turnierverwaltung
├── horse-context/               # Pferdeverwaltung
├── registration-context/        # Anmeldungen
└── payment-context/             # Zahlungsabwicklung
```

### Aggregate Design

```kotlin
class Tournament private constructor(
    val id: TournamentId,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val maxParticipants: Int,
    private val registrations: MutableList<TournamentRegistration> = mutableListOf()
) {
    companion object {
        fun create(
            name: String,
            startDate: LocalDate,
            endDate: LocalDate,
            maxParticipants: Int
        ): Result<Tournament, ValidationError> {
            // Business rules validation
            if (startDate.isAfter(endDate)) {
                return Result.Failure(ValidationError.INVALID_DATE_RANGE)
            }

            return Result.Success(
                Tournament(
                    id = TournamentId.generate(),
                    name = name,
                    startDate = startDate,
                    endDate = endDate,
                    maxParticipants = maxParticipants
                )
            )
        }
    }

    fun registerMember(memberId: MemberId): Result<TournamentRegistrationCreatedEvent, BusinessError> {
        // Business rules
        if (registrations.size >= maxParticipants) {
            return Result.Failure(BusinessError.TOURNAMENT_FULL)
        }

        if (registrations.any { it.memberId == memberId }) {
            return Result.Failure(BusinessError.ALREADY_REGISTERED)
        }

        val registration = TournamentRegistration(
            id = TournamentRegistrationId.generate(),
            tournamentId = id,
            memberId = memberId,
            registrationDate = LocalDateTime.now()
        )

        registrations.add(registration)

        return Result.Success(
            TournamentRegistrationCreatedEvent(
                tournamentId = id,
                memberId = memberId,
                registrationId = registration.id
            )
        )
    }
}
```

### Infrastructure & Betrieb

#### Kafka-Konfiguration

Die Konfiguration muss auf maximale Zuverlässigkeit ausgelegt sein:

```yaml
# application.yml
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

#### Datenbank-Migrationen (Flyway)

Migrations-Skripte müssen einer klaren Namenskonvention folgen:

* **Pattern:** `V{version}__{description}.sql` (z.B., `V001__Create_member_tables.sql`)
* **Repeatable:** `R__{description}.sql` (z.B., `R__Update_member_view.sql`)

#### API-Dokumentation (OpenAPI)

Alle öffentlichen REST-Endpunkte müssen mit OpenAPI-Annotationen (`@Operation`, `@ApiResponse`) dokumentiert werden, um eine klare und interaktive API-Dokumentation zu generieren.

```kotlin
@Operation(
    summary = "Neues Mitglied erstellen",
    description = "Erstellt ein neues Mitglied mit den angegebenen Daten"
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "201",
            description = "Mitglied erfolgreich erstellt"
        ),
        ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingabedaten"
        )
    ]
)
@PostMapping
fun createMember(@RequestBody request: CreateMemberRequest): ResponseEntity<MemberResponse>
```

## 🚀 Architektur-Entscheidungen (ADRs)

### ADR-001: Microservices mit Domain-Driven Design

**Status:** Akzeptiert

**Kontext:** Skalierbare und wartbare Architektur für Pferdesport-Plattform

**Entscheidung:** Microservices-Architektur mit DDD-Bounded-Contexts

**Konsequenzen:**
- ✅ Unabhängige Entwicklung und Deployment
- ✅ Fachliche Kapselung durch Bounded Contexts
- ❌ Komplexität bei Service-zu-Service-Kommunikation
- ❌ Eventual Consistency zwischen Services

### ADR-002: Event-Driven Architecture mit Kafka

**Status:** Akzeptiert

**Kontext:** Entkopplung und Resilienz zwischen Services

**Entscheidung:** Kafka als zentraler Event-Broker

**Konsequenzen:**
- ✅ Lose Kopplung zwischen Services
- ✅ Audit-Log durch Event-Store
- ❌ Komplexität bei Event-Schema-Evolution
- ❌ Eventually Consistent State

### ADR-003: Kotlin Multiplatform für Client

**Status:** Akzeptiert

**Kontext:** Code-Sharing zwischen Desktop und Web

**Entscheidung:** KMP mit Compose Multiplatform

**Konsequenzen:**
- ✅ Geteilte Business-Logic
- ✅ Einheitliche UI-Patterns
- ❌ Plattform-spezifische Optimierungen schwieriger
- ❌ Abhängigkeit von Kotlin/JetBrains-Ökosystem

---

**Navigation:**
- [Master-Guideline](../master-guideline.md) - Übergeordnete Projektrichtlinien
- [Coding-Standards](./coding-standards.md) - Code-Qualitätsstandards
- [Testing-Standards](./testing-standards.md) - Test-Qualitätssicherung
- [Documentation-Standards](./documentation-standards.md) - Dokumentationsrichtlinien
