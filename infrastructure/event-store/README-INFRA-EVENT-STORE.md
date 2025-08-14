# Infrastructure/Event-Store Module

## Überblick

Das **Event-Store-Modul** ist eine kritische Komponente der Infrastruktur, die für die Persistenz und Veröffentlichung von Domänen-Events zuständig ist. Es bildet die technische Grundlage für **Event Sourcing** und eine allgemeine **ereignisgesteuerte Architektur**. Anstatt nur den aktuellen Zustand einer Entität zu speichern, speichert der Event Store die gesamte Kette von Ereignissen, die zu diesem Zustand geführt haben.

Das Modul bietet eine vollständige, produktionsreife Event-Store-Implementierung mit garantierter Konsistenz, ausfallsicherer Event-Verarbeitung und optimaler Performance für moderne Microservice-Architekturen.

## Inhaltsverzeichnis

1. [Architektur](#architektur)
2. [Schlüsselfunktionen](#schlüsselfunktionen)
3. [Konfiguration](#konfiguration)
4. [API-Dokumentation](#api-dokumentation)
5. [Verwendung](#verwendung)
6. [Event Consumer](#event-consumer)
7. [Testing-Strategie](#testing-strategie)
8. [Performance & Monitoring](#performance--monitoring)
9. [Troubleshooting](#troubleshooting)
10. [Migration & Deployment](#migration--deployment)

## Architektur

### Port-Adapter-Muster

Das Modul folgt streng dem **Port-Adapter-Muster** (Hexagonal Architecture), um eine maximale Entkopplung von der konkreten Speichertechnologie zu erreichen:

```
┌─────────────────────────────────────────┐
│           Application Services          │
│  (members, horses, events, etc.)        │
└─────────────────┬───────────────────────┘
                  │ depends on
┌─────────────────▼───────────────────────┐
│        event-store-api (Port)           │
│  • EventStore interface                 │
│  • EventSerializer interface            │
│  • Subscription interface               │
│  • ConcurrencyException                 │
└─────────────────┬───────────────────────┘
                  │ implemented by
┌─────────────────▼───────────────────────┐
│     redis-event-store (Adapter)        │
│  • RedisEventStore                      │
│  • RedisEventConsumer                   │
│  • JacksonEventSerializer               │
│  • RedisEventStoreConfiguration         │
└─────────────────┬───────────────────────┘
                  │ uses
┌─────────────────▼───────────────────────┐
│            Redis Streams                │
│  • Aggregate streams (event-stream:*)   │
│  • Global stream (all-events)           │
│  • Consumer groups                      │
└─────────────────────────────────────────┘
```

### Module Structure

* **`:infrastructure:event-store:event-store-api`**: Definiert die provider-agnostischen Interfaces (`EventStore`, `EventSerializer`, `Subscription`) gegen die Fach-Services programmieren
* **`:infrastructure:event-store:redis-event-store`**: Konkrete Implementierung mit **Redis Streams** als hoch-performantes, persistentes Event-Log

## Schlüsselfunktionen

### 🔒 Garantierte Konsistenz
- **Atomare Transaktionen**: Schreibvorgänge in aggregatspezifische Streams und den globalen "all-events"-Stream werden innerhalb einer **Redis-Transaktion (`MULTI`/`EXEC`)** ausgeführt
- **Optimistische Concurrency Control**: Verhindert Race Conditions durch `expectedVersion`-Prüfung mit `ConcurrencyException` bei Konflikten
- **Eventual Consistency**: Garantiert, dass alle Events sowohl in aggregatspezifischen als auch globalen Streams verfügbar sind

### 🛡️ Resiliente Event-Verarbeitung
- **Redis Consumer Groups**: Skalierbare und ausfallsichere Event-Verarbeitung mit automatischer Last-Verteilung
- **Pending Message Recovery**: Robuste Logik zum "Claimen" von Nachrichten ausgefallener Consumer
- **Retry-Mechanismen**: Automatische Wiederholung bei temporären Fehlern
- **Graceful Degradation**: Kontinuierliche Funktion auch bei partiellen Ausfällen

### 📊 Intelligente Serialisierung
- **Metadata Separation**: Event-Metadaten und Nutzlast werden getrennt gespeichert für effiziente Stream-Analyse
- **Type Registry**: Dynamische Event-Type-Registrierung für polymorphe Deserialisierung
- **JSON-basiert**: Verwendung von Jackson für robuste, schema-flexible Serialisierung

### 🚀 Performance-Optimierung
- **Stream-basierte Speicherung**: Optimale Performance durch Redis Streams
- **Batch Operations**: Unterstützung für Batch-Event-Appending
- **Connection Pooling**: Konfigurierbare Verbindungspools für optimale Resource-Nutzung
- **Asynchrone Verarbeitung**: Non-blocking Event-Processing

## Konfiguration

### Basis-Konfiguration (application.yml)

```yaml
redis:
  event-store:
    # Redis Connection
    host: localhost                    # Redis Server Host
    port: 6379                        # Redis Server Port
    password: null                    # Redis Password (optional)
    database: 0                       # Redis Database Number

    # Connection Pool
    use-pooling: true                 # Enable connection pooling
    max-pool-size: 8                  # Maximum pool connections
    min-pool-size: 2                  # Minimum pool connections
    connection-timeout: 2000          # Connection timeout (ms)
    read-timeout: 2000                # Read timeout (ms)

    # Stream Configuration
    stream-prefix: "event-stream:"    # Prefix for aggregate streams
    all-events-stream: "all-events"   # Global events stream name

    # Consumer Configuration
    consumer-group: "event-processors" # Consumer group name
    consumer-name: "event-consumer"    # Consumer instance name
    create-consumer-group-if-not-exists: true

    # Processing Configuration
    claim-idle-timeout: PT1M          # Timeout for claiming idle messages
    poll-timeout: PT100MS             # Polling timeout
    max-batch-size: 100               # Maximum events per batch
```

### Production-Konfiguration

```yaml
redis:
  event-store:
    # Production Redis Setup
    host: redis-cluster.production.local
    port: 6379
    password: ${REDIS_PASSWORD}

    # Optimized Pool Settings
    use-pooling: true
    max-pool-size: 20
    min-pool-size: 5
    connection-timeout: 5000
    read-timeout: 5000

    # Production Consumer Settings
    consumer-group: "${app.name}-processors"
    consumer-name: "${app.instance-id}"
    claim-idle-timeout: PT2M
    poll-timeout: PT500MS
    max-batch-size: 50
```

### Umgebungsvariablen

```bash
# Redis Connection
REDIS_EVENT_STORE_HOST=redis.production.local
REDIS_EVENT_STORE_PORT=6379
REDIS_EVENT_STORE_PASSWORD=secret123
REDIS_EVENT_STORE_DATABASE=1

# Consumer Configuration
REDIS_EVENT_STORE_CONSUMER_GROUP=prod-processors
REDIS_EVENT_STORE_CONSUMER_NAME=instance-01
REDIS_EVENT_STORE_MAX_BATCH_SIZE=100
```

## API-Dokumentation

### EventStore Interface

```kotlin
interface EventStore {
    // Single Event Operations
    fun appendToStream(event: DomainEvent, streamId: UUID, expectedVersion: Long): Long
    fun readFromStream(streamId: UUID, fromVersion: Long = 0, toVersion: Long? = null): List<DomainEvent>
    fun getStreamVersion(streamId: UUID): Long

    // Batch Operations
    fun appendToStream(events: List<DomainEvent>, streamId: UUID, expectedVersion: Long): Long

    // Global Stream Operations
    fun readAllEvents(fromPosition: Long = 0, maxCount: Int? = null): List<DomainEvent>

    // Subscription Operations
    fun subscribeToStream(streamId: UUID, fromVersion: Long = 0, handler: (DomainEvent) -> Unit): Subscription
    fun subscribeToAll(fromPosition: Long = 0, handler: (DomainEvent) -> Unit): Subscription
}
```

### EventSerializer Interface

```kotlin
interface EventSerializer {
    // Serialization
    fun serialize(event: DomainEvent): Map<String, String>
    fun deserialize(data: Map<String, String>): DomainEvent

    // Type Management
    fun getEventType(event: DomainEvent): String
    fun getEventType(data: Map<String, String>): String
    fun registerEventType(eventClass: Class<out DomainEvent>, eventType: String)

    // Metadata Extraction
    fun getAggregateId(data: Map<String, String>): UUID
    fun getEventId(data: Map<String, String>): UUID
    fun getVersion(data: Map<String, String>): Long
}
```

## Verwendung

### 1. Dependency Setup

```kotlin
dependencies {
    implementation(projects.infrastructure.eventStore.redisEventStore)
}
```

### 2. Event Definition

```kotlin
@Serializable
data class MemberRegisteredEvent(
    @Transient override val aggregateId: AggregateId = AggregateId(UUID.randomUUID()),
    @Transient override val version: EventVersion = EventVersion(0),
    val memberId: UUID,
    val name: String,
    val email: String,
    val registeredAt: Instant
) : BaseDomainEvent(aggregateId, EventType("MemberRegistered"), version)
```

### 3. Service Implementation

```kotlin
@Service
class MemberApplicationService(
    private val eventStore: EventStore,
    private val eventSerializer: EventSerializer
) {
    @PostConstruct
    fun init() {
        // Register event types for serialization
        eventSerializer.registerEventType(MemberRegisteredEvent::class.java, "MemberRegistered")
        eventSerializer.registerEventType(MemberUpdatedEvent::class.java, "MemberUpdated")
    }

    fun registerNewMember(command: RegisterMemberCommand): UUID {
        val memberId = UUID.randomUUID()
        val event = MemberRegisteredEvent(
            aggregateId = AggregateId(memberId),
            version = EventVersion(1L),
            memberId = memberId,
            name = command.name,
            email = command.email,
            registeredAt = Instant.now()
        )

        try {
            // Append to stream with expected version 0 (new stream)
            val newVersion = eventStore.appendToStream(event, memberId, 0)
            logger.info("Member registered: {} at version {}", memberId, newVersion)
            return memberId
        } catch (ex: ConcurrencyException) {
            logger.warn("Concurrency conflict for member: {}", memberId)
            throw MemberAlreadyExistsException(memberId)
        }
    }

    fun updateMember(command: UpdateMemberCommand) {
        // 1. Load the current state from the event stream
        val events = eventStore.readFromStream(command.memberId)
        val currentVersion = eventStore.getStreamVersion(command.memberId)

        // 2. Validate business rules
        validateUpdateCommand(command, events)

        // 3. Create and append new event
        val event = MemberUpdatedEvent(
            aggregateId = AggregateId(command.memberId),
            version = EventVersion(currentVersion + 1),
            memberId = command.memberId,
            updatedFields = command.changes,
            updatedAt = Instant.now()
        )

        eventStore.appendToStream(event, command.memberId, currentVersion)
    }

    fun getMemberHistory(memberId: UUID): List<DomainEvent> {
        return eventStore.readFromStream(memberId)
    }

    fun getMemberHistoryRange(memberId: UUID, fromVersion: Long, toVersion: Long): List<DomainEvent> {
        return eventStore.readFromStream(memberId, fromVersion, toVersion)
    }
}
```

### 4. Batch Operations

```kotlin
@Service
class BulkMemberService(
    private val eventStore: EventStore
) {
    fun registerMultipleMembers(commands: List<RegisterMemberCommand>) {
        commands.forEach { command ->
            val events = listOf(
                MemberRegisteredEvent(/* ... */),
                MemberProfileCreatedEvent(/* ... */)
            )

            // Append multiple events atomically
            eventStore.appendToStream(events, command.memberId, 0)
        }
    }
}
```

## Event Consumer

### Consumer Setup

```kotlin
@Component
class MemberEventHandler(
    private val redisEventConsumer: RedisEventConsumer,
    private val memberProjectionService: MemberProjectionService
) {
    @PostConstruct
    fun init() {
        // Register handlers for specific event types
        redisEventConsumer.registerEventHandler("MemberRegistered") { event ->
            val memberEvent = event as MemberRegisteredEvent
            memberProjectionService.handleMemberRegistered(memberEvent)
        }

        redisEventConsumer.registerEventHandler("MemberUpdated") { event ->
            val memberEvent = event as MemberUpdatedEvent
            memberProjectionService.handleMemberUpdated(memberEvent)
        }

        // Register handler for all events (useful for auditing)
        redisEventConsumer.registerAllEventsHandler { event ->
            auditService.recordEvent(event)
        }
    }

    @PreDestroy
    fun cleanup() {
        // Consumers are automatically cleaned up, but manual cleanup is possible
        redisEventConsumer.unregisterEventHandler("MemberRegistered", memberHandler)
    }
}
```

### Consumer Configuration

```yaml
redis:
  event-store:
    # Consumer-specific settings
    consumer-group: "member-projections"
    consumer-name: "${spring.application.name}-${random.uuid}"

    # Processing optimization
    claim-idle-timeout: PT30S    # Claim messages idle for 30 seconds
    poll-timeout: PT1S           # Poll every second
    max-batch-size: 25           # Process 25 events per batch
```

## Testing-Strategie

### 1. Integrationstests mit Testcontainers

```kotlin
@Testcontainers
class RedisEventStoreIntegrationTest {
    companion object {
        @Container
        val redisContainer: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
    }

    @Test
    fun `should append and read events correctly`() {
        // Test implementation using a real Redis instance
        val events = listOf(testEvent1, testEvent2)
        val newVersion = eventStore.appendToStream(events, aggregateId, 0)

        val readEvents = eventStore.readFromStream(aggregateId)
        assertEquals(2, readEvents.size)
        assertEquals(2, newVersion)
    }
}
```

### 2. Unit-Tests für Business Logic

```kotlin
@ExtendWith(MockKExtension::class)
class MemberServiceTest {
    @MockK private lateinit var eventStore: EventStore

    @Test
    fun `should handle concurrency conflicts gracefully`() {
        // Given
        every { eventStore.appendToStream(any(), any(), any()) } throws ConcurrencyException("Version conflict")

        // When & Then
        assertThrows<MemberAlreadyExistsException> {
            memberService.registerMember(command)
        }
    }
}
```

### 3. Consumer Tests

```kotlin
@Test
fun `consumer should process events reliably`() {
    // Arrange
    val processedEvents = mutableListOf<DomainEvent>()
    redisEventConsumer.registerEventHandler("TestEvent") { event ->
        processedEvents.add(event)
    }

    // Act
    eventStore.appendToStream(testEvent, aggregateId, 0)
    redisEventConsumer.pollEvents() // Manually trigger polling for deterministic tests

    // Assert
    assertEquals(1, processedEvents.size)
    assertEquals(testEvent.eventId, processedEvents[0].eventId)
}
```

### Test-Features

- **Testcontainers Integration**: Echte Redis-Instanz für Integrationstests
- **Deterministische Tests**: Manueller Polling-Trigger statt Thread.sleep
- **Saubere Test-Daten**: @Transient-Annotation für Event-Klassen
- **Umfassende Szenarien**: Configuration, Error Handling, Stream, Resilience Tests

## Performance & Monitoring

### Performance-Charakteristiken

- **Durchsatz**: >10 000 Events/Sekunde bei optimaler Konfiguration
- **Latenz**: <10ms für Event-Appending, <50ms für Event-Reading
- **Skalierung**: Horizontal skalierbar durch Consumer Groups
- **Speicher**: Effiziente Stream-basierte Speicherung

### Monitoring-Metriken

```yaml
# Micrometer/Prometheus Metriken (automatisch aktiviert)
management:
  endpoints:
    web:
      exposure:
        include: metrics,health
  metrics:
    export:
      prometheus:
        enabled: true

# Custom Metriken
redis:
  event-store:
    metrics:
      events-appended: counter
      events-read: counter
      consumer-lag: gauge
      stream-length: gauge
```

### Health Checks

```kotlin
@Component
class EventStoreHealthIndicator(
    private val redisTemplate: StringRedisTemplate
) : HealthIndicator {
    override fun health(): Health {
        return try {
            redisTemplate.opsForValue().get("health-check")
            Health.up()
                .withDetail("redis", "connected")
                .build()
        } catch (ex: Exception) {
            Health.down(ex)
                .withDetail("redis", "disconnected")
                .build()
        }
    }
}
```

## Troubleshooting

### Häufige Probleme

#### 1. ConcurrencyException
```kotlin
// Problem: Race Condition bei parallel Schreibvorgängen
// Lösung: Retry-Logic mit exponential backoff
@Retryable(value = [ConcurrencyException::class], maxAttempts = 3)
fun appendWithRetry(event: DomainEvent, streamId: UUID, expectedVersion: Long) {
    eventStore.appendToStream(event, streamId, expectedVersion)
}
```

#### 2. Consumer Lag
```bash
# Redis CLI - Check consumer group info
XINFO GROUPS event-stream:aggregate-id

# Check pending messages
XPENDING event-stream:aggregate-id event-processors

# Claim stuck messages manually if needed
XCLAIM event-stream:aggregate-id event-processors consumer-name 60000 message-id
```

#### 3. Speicher-Issues
```yaml
# Redis Memory Optimization
redis:
  event-store:
    # Reduce batch size if memory constrained
    max-batch-size: 25

    # Shorter claim timeout to free memory faster
    claim-idle-timeout: PT30S
```

#### 4. Verbindungsprobleme
```yaml
# Connection troubleshooting
redis:
  event-store:
    connection-timeout: 10000  # Increase for slow networks
    read-timeout: 10000
    max-pool-size: 5          # Reduce if connection limits hit
```

### Debugging

```yaml
# Enable debug logging
logging:
  level:
    at.mocode.infrastructure.eventstore.redis: DEBUG
    org.springframework.data.redis: DEBUG
```

### Monitoring Commands

```bash
# Check Redis Stream info
redis-cli XINFO STREAM event-stream:aggregate-id

# Monitor real-time commands
redis-cli MONITOR

# Check memory usage
redis-cli INFO memory
```

## Migration & Deployment

### Deployment Checklist

- [ ] Redis Cluster verfügbar und erreichbar
- [ ] Konfiguration für Umgebung angepasst
- [ ] Consumer Groups erstellt (automatisch oder manuell)
- [ ] Monitoring und Alerting konfiguriert
- [ ] Health Checks implementiert
- [ ] Backup-Strategie definiert

### Migration zwischen Versionen

```kotlin
// Event Schema Evolution
@Serializable
data class MemberRegisteredEventV2(
    // Neue Felder optional machen für Backward Compatibility
    val additionalInfo: String? = null
) : BaseDomainEvent
```

### Backup & Recovery

```bash
# Redis Stream Backup (RDB)
redis-cli BGSAVE

# Stream-specific backup
redis-cli --rdb /backup/events.rdb

# Recovery
redis-server --dbfilename events.rdb --dir /backup/
```

---

**Letzte Aktualisierung**: 14. August 2025
