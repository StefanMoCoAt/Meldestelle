# Testing Standards und Qualitätssicherung

---
guideline_type: "project-standards"
scope: "testing-standards"
audience: ["developers", "ai-assistants"]
last_updated: "2025-09-13"
dependencies: ["master-guideline.md", "coding-standards.md"]
related_files: ["build.gradle.kts", "src/test/**", "testcontainers.properties"]
ai_context: "Testing strategies, test pyramid, tools, coverage requirements, and debugging practices"
---

## 🧪 Testing Standards

Tests sind ein integraler Bestandteil jedes Features und müssen einen hohen Standard erfüllen.

> **🤖 AI-Assistant Hinweis:**
> Testing-Prinzipien für das Meldestelle-Projekt:
> - **Test-Pyramide:** 80%+ Unit-Tests, Integrationstests für externe Systeme
> - **Testcontainers:** Goldstandard für Infrastruktur-Tests
> - **Debug-Logs:** Präfix `[DEBUG_LOG]` für Test-Ausgaben
> - **Result-Pattern:** Tests müssen auch Error-Handling validieren

### Test-Pyramide & Werkzeuge

#### Unit-Tests (80 %+ Abdeckung)

Für Domänen- und Anwendungslogik (JUnit 5, MockK).

```kotlin
class MemberServiceTest {
    private val memberRepository = mockk<MemberRepository>()
    private val eventPublisher = mockk<EventPublisher>()
    private val memberService = MemberService(memberRepository, eventPublisher)

    @Test
    fun `should return Success when member is created successfully`() {
        // Given
        val command = CreateMemberCommand(
            memberId = MemberId.generate(),
            name = "Max Mustermann",
            email = "max@example.com"
        )

        every { memberRepository.save(any()) } returns Result.Success(Unit)
        every { eventPublisher.publish(any()) } returns Result.Success(Unit)

        // When
        val result = memberService.createMember(command)

        // Then
        assertThat(result).isInstanceOf<Result.Success<Unit>>()
        verify { memberRepository.save(any()) }
        verify { eventPublisher.publish(ofType<MemberCreatedEvent>()) }
    }

    @Test
    fun `should return Failure when repository save fails`() {
        // Given
        val command = CreateMemberCommand(
            memberId = MemberId.generate(),
            name = "Max Mustermann",
            email = "max@example.com"
        )

        every { memberRepository.save(any()) } returns Result.Failure(RepositoryError.DATABASE_ERROR)

        // When
        val result = memberService.createMember(command)

        // Then
        assertThat(result).isInstanceOf<Result.Failure<RepositoryError>>()
        verify { memberRepository.save(any()) }
        verify(exactly = 0) { eventPublisher.publish(any()) }
    }
}
```

#### Integrationstests

Decken alle Repository-Implementierungen und externen Integrationen ab.

```kotlin
@Testcontainers
class MemberRepositoryIntegrationTest {

    @Container
    private val postgresContainer = PostgreSQLContainer("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")

    private lateinit var memberRepository: MemberRepository

    @BeforeEach
    fun setup() {
        val dataSource = HikariDataSource().apply {
            jdbcUrl = postgresContainer.jdbcUrl
            username = postgresContainer.username
            password = postgresContainer.password
        }

        // Run migrations
        Flyway.configure()
            .dataSource(dataSource)
            .locations("db/migration")
            .load()
            .migrate()

        memberRepository = PostgresMemberRepository(dataSource)
    }

    @Test
    fun `should save and retrieve member successfully`() {
        // Given
        val member = Member(
            id = MemberId.generate(),
            name = "Integration Test Member",
            email = "integration@test.com"
        )

        // When
        val saveResult = runBlocking { memberRepository.save(member) }
        val findResult = runBlocking { memberRepository.findById(member.id) }

        // Then
        assertThat(saveResult).isInstanceOf<Result.Success<Unit>>()
        assertThat(findResult).isInstanceOf<Result.Success<Member?>>()

        val retrievedMember = (findResult as Result.Success).value
        assertThat(retrievedMember?.id).isEqualTo(member.id)
        assertThat(retrievedMember?.name).isEqualTo(member.name)
        assertThat(retrievedMember?.email).isEqualTo(member.email)
    }
}
```

#### Testcontainers als Goldstandard

Jede Interaktion mit externer Infrastruktur (DB, Cache, Broker) **muss** mit **Testcontainers** getestet werden.

```kotlin
@Testcontainers
class EventStoreIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        private val redisContainer = GenericContainer<Nothing>("redis:7-alpine")
            .withExposedPorts(6379)

        @Container
        @JvmStatic
        private val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
    }

    @Test
    fun `should store and retrieve events from Redis`() {
        println("[DEBUG_LOG] Testing Redis event storage")

        // Given
        val eventStore = RedisEventStore(
            redisHost = redisContainer.host,
            redisPort = redisContainer.getMappedPort(6379)
        )

        val event = MemberCreatedEvent(
            memberId = MemberId.generate(),
            name = "Test Member",
            timestamp = Instant.now()
        )

        // When
        val storeResult = runBlocking { eventStore.store(event) }
        val retrieveResult = runBlocking { eventStore.getEvents(event.memberId) }

        // Then
        assertThat(storeResult).isInstanceOf<Result.Success<Unit>>()
        assertThat(retrieveResult).isInstanceOf<Result.Success<List<DomainEvent>>>()

        val events = (retrieveResult as Result.Success).value
        assertThat(events).hasSize(1)
        assertThat(events.first()).isInstanceOf<MemberCreatedEvent>()

        println("[DEBUG_LOG] Successfully stored and retrieved ${events.size} events")
    }
}
```

### Debugging in Tests

Debug-Ausgaben im Test-Code müssen mit `[DEBUG_LOG]` beginnen, um sie leicht identifizieren und filtern zu können.

```kotlin
@Test
fun `should handle complex business scenario`() {
    println("[DEBUG_LOG] Starting complex business scenario test")

    // Test implementation

    println("[DEBUG_LOG] Member created with ID: ${member.id}")
    println("[DEBUG_LOG] Published ${events.size} domain events")
    println("[DEBUG_LOG] Test completed successfully")
}
```

## 🎯 AI-Assistenten: Testing-Schnellreferenz

### Test-Kategorien und Werkzeuge

| Test-Typ | Coverage-Ziel | Werkzeuge | Verwendung |
|----------|---------------|-----------|------------|
| Unit-Tests | 80%+ | JUnit 5, MockK, AssertJ | Domänen- & Anwendungslogik |
| Integrationstests | Alle Repositories | Testcontainers, JUnit 5 | Externe Integrationen |
| End-to-End Tests | Kritische User-Journeys | Testcontainers, REST Assured | Vollständige Workflows |

### Testcontainer-Konfiguration

#### PostgreSQL
```kotlin
@Container
private val postgresContainer = PostgreSQLContainer("postgres:16-alpine")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test")
    .withInitScript("test-data.sql")
```

#### Redis
```kotlin
@Container
private val redisContainer = GenericContainer<Nothing>("redis:7-alpine")
    .withExposedPorts(6379)
    .withCommand("redis-server", "--appendonly", "yes")
```

#### Kafka
```kotlin
@Container
private val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
    .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
```

#### Keycloak
```kotlin
@Container
private val keycloakContainer = KeycloakContainer("quay.io/keycloak/keycloak:26.0.7")
    .withRealmImportFile("test-realm.json")
    .withAdminUsername("admin")
    .withAdminPassword("admin")
```

### Test-Patterns für Result-Handling

```kotlin
// Success-Case testen
@Test
fun `should return Success when operation succeeds`() {
    // Given
    every { dependency.operation() } returns Result.Success(expectedValue)

    // When
    val result = serviceUnderTest.performOperation()

    // Then
    assertThat(result).isInstanceOf<Result.Success<ExpectedType>>()
    assertThat((result as Result.Success).value).isEqualTo(expectedValue)
}

// Failure-Case testen
@Test
fun `should return Failure when dependency fails`() {
    // Given
    every { dependency.operation() } returns Result.Failure(ExpectedError.SOME_ERROR)

    // When
    val result = serviceUnderTest.performOperation()

    // Then
    assertThat(result).isInstanceOf<Result.Failure<ExpectedError>>()
    assertThat((result as Result.Failure).error).isEqualTo(ExpectedError.SOME_ERROR)
}
```

### Mock-Setup für Services

```kotlin
class ServiceTest {
    private val repository = mockk<Repository>()
    private val eventPublisher = mockk<EventPublisher>()
    private val externalService = mockk<ExternalService>()

    private val serviceUnderTest = Service(repository, eventPublisher, externalService)

    @BeforeEach
    fun setup() {
        clearAllMocks()

        // Default mocks
        every { eventPublisher.publish(any()) } returns Result.Success(Unit)
    }

    @AfterEach
    fun cleanup() {
        confirmVerified(repository, eventPublisher, externalService)
    }
}
```

### Testdaten-Builder

```kotlin
class MemberTestDataBuilder {
    private var id: MemberId = MemberId.generate()
    private var name: String = "Test Member"
    private var email: String = "test@example.com"
    private var status: MemberStatus = MemberStatus.ACTIVE

    fun withId(id: MemberId) = apply { this.id = id }
    fun withName(name: String) = apply { this.name = name }
    fun withEmail(email: String) = apply { this.email = email }
    fun withStatus(status: MemberStatus) = apply { this.status = status }

    fun build() = Member(
        id = id,
        name = name,
        email = email,
        status = status
    )
}

// Verwendung in Tests
@Test
fun `should validate member data`() {
    val member = MemberTestDataBuilder()
        .withName("Max Mustermann")
        .withEmail("max@meldestelle.at")
        .withStatus(MemberStatus.PENDING)
        .build()

    // Test implementation
}
```

### Performance-Tests

```kotlin
@Test
fun `should handle high load efficiently`() {
    println("[DEBUG_LOG] Starting performance test with 1000 concurrent operations")

    val operations = (1..1000).map {
        async {
            serviceUnderTest.performOperation(
                TestCommand(id = MemberId.generate())
            )
        }
    }

    val results = runBlocking {
        operations.awaitAll()
    }

    val successCount = results.count { it is Result.Success }
    val failureCount = results.count { it is Result.Failure }

    println("[DEBUG_LOG] Performance test completed: $successCount successes, $failureCount failures")

    assertThat(successCount).isGreaterThan(950) // 95% success rate minimum
}
```

---

**Navigation:**
- [Master-Guideline](../master-guideline.md) - Übergeordnete Projektrichtlinien
- [Coding-Standards](./coding-standards.md) - Code-Qualitätsstandards
- [Documentation-Standards](./documentation-standards.md) - Dokumentationsrichtlinien
- [Architecture-Principles](./architecture-principles.md) - Architektur-Grundsätze
