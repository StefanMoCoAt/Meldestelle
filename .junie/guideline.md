# Meldestelle Development Guidelines

**Version:** 1.0
**Date:** 2025-08-15
**Status:** Active

This document outlines the development guidelines for the Meldestelle project, covering coding conventions, code organization, and testing approaches.

---

## 1. Coding Conventions

### 1.1 Language Standards

- **Primary Language:** Kotlin (JVM/Multiplatform)
- **Java Compatibility:** Target Java 21+
- **Kotlin Version:** Latest stable version
- **Code Style:** Official Kotlin coding conventions

### 1.2 Naming Conventions

#### Classes and Interfaces
```kotlin
// Use PascalCase for classes and interfaces
class MemberService
interface EventRepository
data class MemberRegistration
sealed class AuthResult

// Use descriptive names that reflect domain concepts
class HorseRegistrationService  // Good
class HRS                       // Avoid abbreviations
```

#### Functions and Variables
```kotlin
// Use camelCase for functions and variables
fun authenticateUser(): AuthResult
val memberRepository: MemberRepository
suspend fun findByEmail(email: EmailAddress): Result<Member?, RepositoryError>

// Use descriptive test method names with "should" statements
@Test
fun `authenticate should return Success for valid credentials`()
```

#### Constants and Enums
```kotlin
// Use SCREAMING_SNAKE_CASE for constants
const val MAX_RETRY_ATTEMPTS = 3
const val DEFAULT_TIMEOUT_MS = 5000L

// Use PascalCase for enum values
enum class MemberStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
```

### 1.3 Code Structure Principles

#### Result Pattern Usage
```kotlin
// Always use Result pattern for operations that can fail
interface MemberRepository {
    suspend fun findById(id: MemberId): Result<Member?, RepositoryError>
    suspend fun save(member: Member): Result<Unit, RepositoryError>
}

// Result extensions for error handling
inline fun <T, E, R> Result<T, E>.mapError(transform: (E) -> R): Result<T, R> =
    when (this) {
        is Result.Success -> Result.Success(value)
        is Result.Failure -> Result.Failure(transform(error))
    }
```

#### Coroutines and Async Programming
```kotlin
// Use suspend functions for async operations
suspend fun processEventBatch(events: List<DomainEvent>): Result<Unit, ProcessingError>

// Prefer structured concurrency
class EventProcessor {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun processEvents() = withContext(scope.coroutineContext) {
        // Implementation
    }
}
```

#### Documentation Standards
```kotlin
/**
 * Authenticates a user with the given credentials.
 *
 * @param credentials The user credentials containing username and password
 * @return AuthResult.Success with user data if authentication succeeds,
 *         AuthResult.Failure with error details if it fails
 */
suspend fun authenticate(credentials: UserCredentials): AuthResult
```

---

## 2. Code Organization and Package Structure

### 2.1 Overall Architecture

The project follows a **microservices architecture** with **Domain-Driven Design (DDD)** principles and **Clean Architecture** patterns.

#### High-Level Structure
```
Meldestelle/
├── core/                           # Shared kernel - fundamental building blocks
│   ├── core-domain/               # Common domain types and interfaces
│   └── core-utils/                # Shared utilities and extensions
├── infrastructure/                 # Cross-cutting infrastructure services
│   ├── auth/                      # Authentication & authorization
│   ├── messaging/                 # Event messaging (Kafka)
│   ├── cache/                     # Distributed caching (Redis)
│   ├── gateway/                   # API Gateway
│   └── monitoring/                # Observability and monitoring
├── [domain-services]/             # Domain-specific microservices
│   ├── members/                   # Member management
│   ├── events/                    # Event management
│   ├── horses/                    # Horse registry
│   └── masterdata/                # Master data management
├── client/                        # Client applications
│   ├── common-ui/                 # Shared UI components (KMP)
│   ├── desktop-app/               # Desktop application
│   └── web-app/                   # Web application
└── platform/                     # Build and dependency management
```

### 2.2 Microservice Structure (Clean Architecture)

Each domain service follows a **4-layer architecture**:

```
domain-service/
├── domain-api/                    # REST controllers, DTOs, API contracts
├── domain-application/            # Use cases, application logic, orchestration
├── domain-domain/                 # Domain models, business rules, interfaces
└── domain-infrastructure/         # Technical implementations (DB, external APIs)
```

#### Layer Responsibilities

**`:domain-api` Layer:**
```kotlin
// REST Controllers
@RestController
@RequestMapping("/api/v1/members")
class MemberController(private val memberService: MemberService)

// DTOs for external communication
data class MemberRegistrationRequest(
    val firstName: String,
    val lastName: String,
    val email: String
)
```

**`:domain-application` Layer:**
```kotlin
// Use cases and application services
class MemberApplicationService(
    private val memberRepository: MemberRepository,
    private val eventPublisher: EventPublisher
) {
    suspend fun registerMember(command: RegisterMemberCommand): Result<MemberId, MemberError>
}
```

**`:domain-domain` Layer:**
```kotlin
// Domain models and business logic
data class Member(
    val id: MemberId,
    val personalInfo: PersonalInfo,
    val membershipStatus: MembershipStatus
) {
    fun activate(): Member = copy(membershipStatus = MembershipStatus.ACTIVE)
}

// Repository interfaces (implemented in infrastructure)
interface MemberRepository {
    suspend fun findById(id: MemberId): Result<Member?, RepositoryError>
    suspend fun save(member: Member): Result<Unit, RepositoryError>
}
```

**`:domain-infrastructure` Layer:**
```kotlin
// Technical implementations
class ExposedMemberRepository(
    private val database: Database
) : MemberRepository {
    override suspend fun findById(id: MemberId): Result<Member?, RepositoryError> {
        // Database implementation using Exposed ORM
    }
}
```

### 2.3 Package Naming Conventions

```kotlin
// Base package structure
at.mocode.[layer].[domain].[component]

// Examples
at.mocode.members.domain.model      // Domain models
at.mocode.members.application.service // Application services
at.mocode.members.infrastructure.persistence // Persistence layer
at.mocode.infrastructure.messaging.kafka // Infrastructure components
at.mocode.core.utils.result         // Core utilities
```

### 2.4 Dependency Rules

- **Core modules** must not depend on any other modules
- **Domain layer** must not depend on infrastructure or application layers
- **Application layer** can depend on domain layer only
- **Infrastructure layer** can depend on domain and application layers
- **API layer** orchestrates calls between application and infrastructure

---

## 3. Unit and Integration Testing Approaches

### 3.1 Testing Strategy Overview

The project follows a **comprehensive testing strategy** with multiple testing levels:

1. **Unit Tests** - Fast, isolated tests for individual components
2. **Integration Tests** - Tests for component interactions
3. **Performance Tests** - Load and throughput testing
4. **End-to-End Tests** - Full system workflow testing

### 3.2 Testing Stack

#### Core Testing Libraries
```kotlin
// Unit testing
testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

// Integration testing
testImplementation("org.testcontainers:junit-jupiter:1.19.1")
testImplementation("org.testcontainers:kafka:1.19.1")
testImplementation("org.testcontainers:postgresql:1.19.1")

// Performance testing
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
```

### 3.3 Unit Testing Conventions

#### Test Structure and Naming
```kotlin
class AuthenticationServiceTest {

    @BeforeEach
    fun setUp() {
        // Test setup
    }

    @Test
    fun `authenticate should return Success for valid credentials`() = runTest {
        // Given
        val credentials = UserCredentials("user@example.com", "validPassword")
        coEvery { userRepository.findByEmail(any()) } returns Result.Success(testUser)

        // When
        val result = authenticationService.authenticate(credentials)

        // Then
        assertTrue(result is AuthResult.Success)
        assertEquals(testUser.id, result.user.id)
    }

    @Test
    fun `authenticate should return Failure for invalid credentials`() = runTest {
        // Given - When - Then pattern
    }
}
```

#### Mocking Best Practices
```kotlin
class MemberServiceTest {
    private val memberRepository = mockk<MemberRepository>()
    private val eventPublisher = mockk<EventPublisher>()
    private val memberService = MemberService(memberRepository, eventPublisher)

    @Test
    fun `should publish event when member is registered`() = runTest {
        // Mock repository responses
        coEvery { memberRepository.save(any()) } returns Result.Success(Unit)
        coEvery { eventPublisher.publish(any()) } returns Result.Success(Unit)

        // Test implementation
        val result = memberService.registerMember(validCommand)

        // Verify interactions
        coVerify { eventPublisher.publish(any<MemberRegisteredEvent>()) }
    }
}
```

### 3.4 Integration Testing Approaches

#### Database Integration Tests
```kotlin
@Testcontainers
class MemberRepositoryIntegrationTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine")
    }

    @Test
    fun `should persist and retrieve member correctly`() = runTest {
        // Test with real database using Testcontainers
        val member = createTestMember()

        val saveResult = memberRepository.save(member)
        assertTrue(saveResult.isSuccess())

        val retrievedResult = memberRepository.findById(member.id)
        assertTrue(retrievedResult.isSuccess())
        assertEquals(member, retrievedResult.getOrNull())
    }
}
```

#### Messaging Integration Tests
```kotlin
@Testcontainers
class KafkaEventPublisherIntegrationTest {

    companion object {
        @Container
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
    }

    @Test
    fun `should publish and consume events correctly`() = runTest {
        val event = MemberRegisteredEvent(memberId = MemberId.generate())

        val publishResult = eventPublisher.publish(event)
        assertTrue(publishResult.isSuccess())

        // Verify event was consumed
        val consumedEvents = eventConsumer.consumeEvents(timeout = 5.seconds)
        assertTrue(consumedEvents.any { it.memberId == event.memberId })
    }
}
```

### 3.5 Performance Testing

#### Batch Processing Performance Tests
```kotlin
class KafkaBatchPerformanceTest {

    @Test
    fun `should process large batches within acceptable time limits`() = runTest {
        val batchSize = 1000
        val events = generateTestEvents(batchSize)
        val startTime = System.currentTimeMillis()

        val results = eventProcessor.processBatch(events)
        val processingTime = System.currentTimeMillis() - startTime

        assertTrue(results.all { it.isSuccess() })
        assertTrue(processingTime < 5000) // Should complete within 5 seconds

        println("[DEBUG_LOG] Processed $batchSize events in ${processingTime}ms")
    }
}
```

### 3.6 Test Organization

#### Directory Structure
```
src/
├── main/kotlin/                   # Production code
└── test/kotlin/                   # Test code
    ├── unit/                      # Unit tests (optional sub-organization)
    ├── integration/               # Integration tests
    └── performance/               # Performance tests
```

#### Test Categories and Execution
```kotlin
// Use JUnit 5 tags for test categorization
@Tag("unit")
class MemberServiceTest

@Tag("integration")
class MemberRepositoryIntegrationTest

@Tag("performance")
class KafkaBatchPerformanceTest
```

### 3.7 Testing Guidelines

#### Best Practices
1. **Test Method Naming:** Use descriptive names with "should" statements
2. **AAA Pattern:** Arrange, Act, Assert structure
3. **One Assertion Per Test:** Focus on single behavior
4. **Test Data Builders:** Use factory methods for test data creation
5. **Coroutine Testing:** Use `runTest` for suspend functions
6. **Mock Verification:** Verify important interactions, not implementation details

#### Coverage Goals
- **Unit Tests:** 80%+ code coverage for domain and application layers
- **Integration Tests:** Cover all repository implementations and external integrations
- **Performance Tests:** Cover critical batch operations and high-load scenarios

#### Debugging Support
```kotlin
// Always prefix debug messages with [DEBUG_LOG]
@Test
fun `should handle concurrent requests`() = runTest {
    println("[DEBUG_LOG] Starting concurrent request test with ${requestCount} requests")

    // Test implementation

    println("[DEBUG_LOG] Completed test. Success rate: ${successCount}/${requestCount}")
}
```

---

## 4. Additional Development Standards

### 4.1 Error Handling
- Use `Result` pattern consistently for operations that can fail
- Define domain-specific error types
- Avoid throwing exceptions in domain logic

### 4.2 Logging and Monitoring
- Use structured logging with appropriate log levels
- Include correlation IDs for request tracing
- Monitor key business metrics and technical performance

### 4.3 Security Considerations
- Validate all external inputs
- Use JWT tokens for authentication
- Implement proper authorization checks
- Secure sensitive configuration data

---

This guideline is a living document and should be updated as the project evolves and new patterns emerge.
