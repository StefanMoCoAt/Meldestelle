# Infrastructure/Messaging Module

## Überblick

Das **Messaging-Modul** stellt die Infrastruktur für die asynchrone, reaktive Kommunikation zwischen den Microservices bereit. Es nutzt **Apache Kafka** als hochperformanten, verteilten Message-Broker und ist entscheidend für die Entkopplung von Services und die Implementierung einer skalierbaren, ereignisgesteuerten Architektur.

Das Modul implementiert moderne **Domain-Driven Design (DDD)** Prinzipien mit expliziter Fehlerbehandlung über das **Result Pattern** und bietet sowohl suspending Coroutine-APIs als auch reaktive Stream-APIs für maximale Flexibilität.

### Kernfeatures

- **🎯 Result Pattern APIs**: Typsichere Fehlerbehandlung ohne Exceptions
- **⚡ Reactive Streams**: Hochperformante, nicht-blockierende I/O-Operationen
- **🔄 Intelligent Retry Logic**: Differenzierte Retry-Strategien basierend auf Fehlertypen
- **📊 Batch Processing**: Optimierte Verarbeitung mehrerer Events mit kontrollierbarer Parallelität
- **🔒 Security Features**: Sichere Deserialisierung mit Trusted-Package-Validierung
- **📈 Observability**: Umfassendes Logging und Monitoring für Production-Ready-Deployment
- **🧪 Comprehensive Testing**: Integration Tests mit Testcontainers und fokussierte Unit Tests

## Architektur

Das Modul ist in zwei spezialisierte Komponenten aufgeteilt, um Konfiguration von der Client-Logik zu trennen:

infrastructure/messaging/
├── messaging-config/   # Stellt die zentrale Kafka-Konfiguration bereit
└── messaging-client/   # Stellt wiederverwendbare, reaktive Clients bereit

### `messaging-config`

Dieses Modul zentralisiert die grundlegende Kafka-Konfiguration für das gesamte Projekt.

- **Zweck:** Definiert Spring-Beans für die `ProducerFactory` (Basis für Producer) und eine `Map` mit Standard-Konfigurationen für Consumer (z.B. `bootstrap-servers`, `group-id`, Serializer).
- **Vorteil:** Stellt Konsistenz sicher und vereinfacht die Einrichtung neuer Producer oder Consumer in den Services.

### `messaging-client`

Dieses Modul baut auf der Konfiguration auf und stellt wiederverwendbare High-Level-Komponenten für die Interaktion mit Kafka bereit.

#### Kern-Komponenten

- **`EventPublisher` Interface**: Definiert moderne APIs für das Publizieren von Domain Events
  - **Moderne APIs**: `publishEvent()` und `publishEvents()` mit Result Pattern
  - **Legacy APIs**: `publishEventReactive()` und `publishEventsReactive()` (deprecated)

- **`EventConsumer` Interface**: Definiert APIs für das Empfangen von Domain Events
  - **Moderne APIs**: `receiveEventsWithResult()` mit Flow<Result<T>> für typsichere Fehlerbehandlung
  - **Legacy APIs**: `receiveEvents()` mit Flux<T> (deprecated)

- **`KafkaEventPublisher`**: Implementierung des EventPublisher mit umfassendem Feature-Set
  - Reaktive, nicht-blockierende Kafka-Integration mit `ReactiveKafkaProducerTemplate`
  - Intelligente Retry-Logic mit exponential backoff
  - Optimierte Batch-Verarbeitung mit kontrollierbarer Parallelität (10 concurrent operations)
  - Comprehensive Logging und Progress-Tracking

- **`KafkaEventConsumer`**: Implementierung des EventConsumer mit erweiterten Funktionen
  - Connection-Pooling zur Wiederverwendung von KafkaReceiver-Instanzen
  - Sichere Deserialisierung mit Trusted-Package-Validierung
  - Manual Acknowledgment Control für bessere Kontrolle über Commit-Verhalten
  - Consumer-Cache-Management für Ressourcenoptimierung

- **`MessagingError` Hierarchie**: Domain-spezifische Fehlertypen für strukturierte Fehlerbehandlung
  - `SerializationError`, `DeserializationError`: Serialization-/Deserialization-Probleme
  - `ConnectionError`: Netzwerk- und Verbindungsfehler
  - `TimeoutError`: Zeitüberschreitungen
  - `AuthenticationError`: Authentifizierungs-/Autorisierungsfehler
  - `TopicConfigurationError`: Topic-Konfigurationsprobleme
  - `UnexpectedError`: Allgemeine unerwartete Fehler

#### Vorteile

- **Typsichere Fehlerbehandlung**: Result Pattern eliminiert unerwartete Exceptions
- **Flexible APIs**: Sowohl moderne Coroutine-basierte als auch Legacy reaktive APIs
- **Production-Ready**: Umfassendes Retry-Management, Observability und Ressourcenoptimierung
- **Domain-Driven Design**: Explizite Fehlertypen und saubere Abstraktionen

## Verwendung

Ein Microservice, der Nachrichten senden oder empfangen möchte, deklariert eine Abhängigkeit zu `:infrastructure:messaging:messaging-client` und injiziert die entsprechenden Interfaces.

### Moderne API (Result Pattern + Coroutines) - **Empfohlen**

**Beispiel für das Senden einer Nachricht mit typsicherer Fehlerbehandlung:**

```kotlin
@Service
class EventNotificationService(
    private val eventPublisher: EventPublisher
) {
    suspend fun notifyNewEvent(eventDetails: EventDetails): Result<Unit> {
        val topic = "new-events-topic"
        return eventPublisher.publishEvent(topic, eventDetails.id, eventDetails)
            .onFailure { error ->
                when (error) {
                    is MessagingError.SerializationError -> logger.error("Serialization failed for event", error)
                    is MessagingError.ConnectionError -> logger.warn("Connection issue, will retry later", error)
                    is MessagingError.TimeoutError -> logger.warn("Timeout publishing event", error)
                    else -> logger.error("Unexpected error publishing event", error)
                }
            }
    }

    suspend fun notifyMultipleEvents(events: List<Pair<String, EventDetails>>): Result<List<Unit>> {
        val topic = "batch-events-topic"
        return eventPublisher.publishEvents(topic, events)
            .onSuccess { results ->
                logger.info("Successfully published {} events", results.size)
            }
            .onFailure { error ->
                logger.error("Failed to publish batch events: {}", error.message)
            }
    }
}
```

**Beispiel für das Empfangen von Nachrichten mit typsicherer Fehlerbehandlung:**

```kotlin
@Component
class ModernEventListener(
    private val eventConsumer: EventConsumer
) {
    private val logger = LoggerFactory.getLogger(ModernEventListener::class.java)

    @PostConstruct
    fun startListening() {
        val topic = "new-events-topic"

        // Moderne Result-basierte API mit Flow<Result<T>>
        eventConsumer.receiveEventsWithResult(topic, EventDetails::class.java)
            .asFlow()
            .collect { result ->
                result
                    .onSuccess { event ->
                        logger.info("Successfully received event with ID: {}", event.id)
                        processEvent(event)
                    }
                    .onFailure { error ->
                        when (error) {
                            is MessagingError.DeserializationError -> {
                                logger.error("Failed to deserialize event from topic '{}': {}", topic, error.message)
                                // Deserialization-Fehler sind meist permanent - keine weiteren Versuche
                                handlePoisonMessage(topic, error)
                            }
                            is MessagingError.ConnectionError -> {
                                logger.warn("Connection issue while consuming from topic '{}': {}", topic, error.message)
                                // Connection-Fehler sind oft temporär - Consumer wird automatisch retries
                            }
                            is MessagingError.TimeoutError -> {
                                logger.warn("Timeout while consuming from topic '{}': {}", topic, error.message)
                                // Timeout-Fehler können retries bekommen
                            }
                            else -> {
                                logger.error("Unexpected error consuming from topic '{}': {}", topic, error.message, error)
                                handleUnexpectedError(topic, error)
                            }
                        }
                    }
            }
    }

    private suspend fun processEvent(event: EventDetails) {
        // Geschäftslogik zur Verarbeitung des Events
        logger.debug("Processing event: {}", event)
    }

    private suspend fun handlePoisonMessage(topic: String, error: MessagingError.DeserializationError) {
        // Poison Messages in separates Topic oder Dead Letter Queue verschieben
        logger.warn("Moving poison message from topic '{}' to dead letter queue", topic)
    }

    private suspend fun handleUnexpectedError(topic: String, error: MessagingError) {
        // Monitoring/Alerting für unerwartete Fehler
        logger.error("Alerting monitoring system for unexpected error in topic '{}'", topic)
    }
}
```

**Beispiel für Consumer mit Coroutines und strukturierter Parallelität:**

```kotlin
@Service
class BatchEventProcessor(
    private val eventConsumer: EventConsumer
) {
    private val logger = LoggerFactory.getLogger(BatchEventProcessor::class.java)

    suspend fun processBatchEvents(topic: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var processedCount = 0
            var errorCount = 0

            eventConsumer.receiveEventsWithResult(topic, EventDetails::class.java)
                .asFlow()
                .take(100) // Verarbeite maximal 100 Events pro Batch
                .collect { result ->
                    result
                        .onSuccess { event ->
                            processedCount++
                            logger.debug("Processed event {}/{}", processedCount, 100)
                        }
                        .onFailure { error ->
                            errorCount++
                            logger.warn("Error processing event: {}", error.message)
                        }
                }

            logger.info("Batch processing completed: {} processed, {} errors", processedCount, errorCount)
            Result.success(processedCount)
        } catch (exception: Exception) {
            logger.error("Batch processing failed", exception)
            Result.failure(MessagingError.UnexpectedError("Batch processing failed: ${exception.message}", exception))
        }
    }
}
```

### Legacy Reactive API - **Wird depreciert**

**Beispiel für das Senden einer Nachricht (reaktiv, nicht-blockierend):**

```kotlin
@Service
class LegacyEventNotificationService(
    private val eventPublisher: EventPublisher
) {
    @Deprecated("Use suspending publishEvent with Result instead")
    fun notifyNewEventReactive(eventDetails: EventDetails) {
        val topic = "new-events-topic"
        eventPublisher.publishEventReactive(topic, eventDetails.id, eventDetails)
            .subscribe(
                { /* onNext: Unit received */ },
                { error -> logger.error("Failed to send message to topic '{}'", topic, error) },
                { /* onComplete: Nichts zu tun */ }
            )
        // Die Methode kehrt sofort zurück, ohne auf die Bestätigung von Kafka zu warten.
    }
}
```

**Beispiel für das Empfangen von Nachrichten (reaktiv):**

```kotlin
@Component
class EventListener(
    private val eventConsumer: EventConsumer
) {
    @PostConstruct
    fun listenForEvents() {
        val topic = "new-events-topic"
        eventConsumer.receiveEvents<EventDetails>(topic)
            .subscribe { event ->
                logger.info("Received new event with ID: {}", event.id)
                // Geschäftslogik zur Verarbeitung des Events...
            }
    }
}
```

## Konfiguration

Das Messaging-Modul bietet umfassende Konfigurationsmöglichkeiten über die `KafkaConfig`-Klasse mit automatischer Validierung und optimierten Standardwerten für Production-Ready-Deployments.

### Basis-Konfiguration

```kotlin
@Configuration
class MessagingConfiguration {

    @Bean
    fun kafkaConfig(): KafkaConfig {
        return KafkaConfig().apply {
            // Kafka-Cluster-Verbindung
            bootstrapServers = "kafka-cluster:9092"  // oder "localhost:9092" für lokale Entwicklung

            // Consumer-Gruppierung
            defaultGroupIdPrefix = "myapp-messaging"

            // Sicherheitseinstellungen
            trustedPackages = "com.mycompany.*,at.mocode.*"
            enableSecurityFeatures = true

            // Performance-Tuning
            connectionPoolSize = 20  // Für hochfrequente Anwendungen
        }
    }
}
```

### Konfigurationsoptionen

| Parameter | Typ | Standard | Beschreibung |
|-----------|-----|----------|--------------|
| `bootstrapServers` | String | "localhost:9092" | Kafka-Cluster-Endpunkte. Unterstützt `host:port` und `PROTOCOL://host:port` Formate |
| `defaultGroupIdPrefix` | String | "messaging-client" | Präfix für automatisch generierte Consumer-Gruppen |
| `trustedPackages` | String | "at.mocode.*" | Comma-separated List von Packages für sichere JSON-Deserialisierung |
| `enableSecurityFeatures` | Boolean | true | Aktiviert erweiterte Sicherheitsfeatures für Production |
| `connectionPoolSize` | Int | 10 | Anzahl der gleichzeitigen Kafka-Verbindungen im Pool |

### Production-Konfiguration

Für Production-Umgebungen empfohlene Konfiguration:

```kotlin
@Configuration
@Profile("production")
class ProductionMessagingConfiguration {

    @Bean
    fun kafkaConfig(): KafkaConfig {
        return KafkaConfig().apply {
            // Hochverfügbares Kafka-Cluster
            bootstrapServers = "kafka-01.prod:9092,kafka-02.prod:9092,kafka-03.prod:9092"

            // Environment-spezifische Gruppierung
            defaultGroupIdPrefix = "${System.getenv("APP_NAME")}-${System.getenv("ENVIRONMENT")}"

            // Restriktive Sicherheitseinstellungen
            trustedPackages = "com.mycompany.events.*,com.mycompany.domain.*"
            enableSecurityFeatures = true

            // Optimiert für hohe Parallelität
            connectionPoolSize = 50
        }
    }
}
```

### Umgebungsvariablen

Das Modul unterstützt Konfiguration über Umgebungsvariablen für Container-Deployments:

```bash
# Docker/Kubernetes Environment Variables
KAFKA_BOOTSTRAP_SERVERS=kafka-cluster:9092
KAFKA_GROUP_ID_PREFIX=myapp-prod
KAFKA_TRUSTED_PACKAGES=com.mycompany.*
KAFKA_CONNECTION_POOL_SIZE=25
KAFKA_ENABLE_SECURITY=true
```

### Erweiterte Producer-Konfiguration

Die `KafkaConfig` stellt optimierte Producer-Eigenschaften bereit:

```kotlin
// Automatisch konfigurierte Producer-Eigenschaften:
// - Batch-Verarbeitung (32KB Batches, 5ms Linger)
// - Snappy-Komprimierung für bessere Performance
// - Idempotenz für Exactly-Once-Semantics
// - Intelligente Retry-Logik (3 Versuche, 1s Backoff)
// - 30s Delivery-Timeout mit 10s Request-Timeout
```

### Erweiterte Consumer-Konfiguration

Consumer werden automatisch mit optimierten Einstellungen konfiguriert:

```kotlin
// Automatisch konfigurierte Consumer-Eigenschaften:
// - Manual Commit für bessere Kontrolle
// - Optimierte Fetch-Größen (1KB min, 1MB max)
// - 500ms Max-Wait für Fetch-Operationen
// - Session-Timeout: 30s, Heartbeat: 3s
// - Automatic Offset Reset: earliest
// - Max 500 Records pro Poll
```

### Monitoring und Observability

```kotlin
@Component
class MessagingHealthIndicator(
    private val kafkaConfig: KafkaConfig
) : HealthIndicator {

    override fun health(): Health {
        return try {
            // Kafka-Cluster-Konnektivität prüfen
            val adminClient = AdminClient.create(kafkaConfig.producerConfigs())
            val clusterMetadata = adminClient.describeCluster()
            val nodeCount = clusterMetadata.nodes().get(5, TimeUnit.SECONDS).size

            Health.up()
                .withDetail("kafka.cluster.nodes", nodeCount)
                .withDetail("kafka.bootstrap.servers", kafkaConfig.bootstrapServers)
                .withDetail("kafka.connection.pool.size", kafkaConfig.connectionPoolSize)
                .build()
        } catch (exception: Exception) {
            Health.down()
                .withDetail("kafka.error", exception.message)
                .withException(exception)
                .build()
        }
    }
}
```

## Dependency Management

### Gradle-Konfiguration

Das Messaging-Modul nutzt eine saubere Modularisierung über Gradle Composite Builds:

```kotlin
// In einem Service-Modul
dependencies {
    // Hauptabhängigkeit für messaging functionality
    implementation(projects.infrastructure.messaging.messagingClient)

    // Die messaging-config wird transitiv eingebunden
    // Alle benötigten Kafka-, Spring- und Reactive-Dependencies sind enthalten
}
```

### Verfügbare Module

| Modul | Zweck | Transitive Dependencies |
|--------|--------|------------------------|
| `messaging-config` | Zentrale Kafka-Konfiguration | Spring Kafka, Jackson, Kafka Clients |
| `messaging-client` | High-Level Publisher/Consumer APIs | Reactor Kafka, Kotlinx Coroutines, messaging-config |

### Version-Management

```kotlin
// platform/platform-bom/build.gradle.kts - Zentrale Versionsverwaltung
dependencies {
    constraints {
        api("org.springframework.kafka:spring-kafka:3.1.4")
        api("io.projectreactor.kafka:reactor-kafka:1.3.22")
        api("org.apache.kafka:kafka-clients:3.6.1")
    }
}
```

## Testing-Strategie

Die Zuverlässigkeit des Moduls wird durch eine mehrstufige Teststrategie sichergestellt, die sowohl Unit- als auch Integrationstests umfasst:

### Integrationstests (Goldstandard)
- **Testcontainers**: Der `KafkaIntegrationTest` startet einen echten Apache Kafka Docker-Container, um die Funktionalität unter realen Bedingungen zu validieren
- **Reaktives Testen**: Nutzt Project Reactor's `StepVerifier` für deterministische Tests der reaktiven Streams ohne unzuverlässige Thread.sleep-Aufrufe
- **Lifecycle Management**: Saubere Ressourcenverwaltung über @BeforeEach und @AfterEach für korrekte Freigabe von Producer-Threads
- **End-to-End Validierung**: Vollständige Publish-Subscribe-Zyklen mit echtem Kafka-Cluster

### Unit Tests
- **`KafkaEventPublisherErrorTest`**: Fokussierte Tests für Fehlerbehandlung mit MockK für isolierte Testszenarien
- **Fehlerszenarien**: Systematische Tests für Serialization-, Authentication-, Connection- und Timeout-Fehler
- **Batch-Verarbeitung**: Validierung von Batch-Operationen und Empty-Batch-Handling
- **Retry-Logic**: Tests für intelligente Retry-Mechanismen und Retry-Exhaustion

### Sicherheits- und Konfigurationstests
- **`KafkaSecurityTest`**: Validierung der Sicherheitskonfigurationen und Trusted-Package-Verwaltung
- **`KafkaEventConsumerCacheTest`**: Tests für Consumer-Caching und Ressourcenoptimierung
- **Konfigurationsvalidierung**: Automatische Validierung aller Konfigurationsparameter

## Neue Features und Optimierungen (2025)

### Domain-Driven Design (DDD) Integration
- **Result Pattern APIs**: Neue suspending Coroutine-basierte APIs mit typsicherer Fehlerbehandlung über das Result Pattern
- **Domain-spezifische Fehlertypen**: Umfassende `MessagingError` Hierarchie (SerializationError, ConnectionError, TimeoutError, AuthenticationError, etc.)
- **Explizite Fehlerbehandlung**: Eliminiert unerwartete Exceptions durch strukturierte Fehler-Typen
- **Backward Compatibility**: Legacy-reactive APIs bleiben verfügbar, sind aber als deprecated markiert

### Erweiterte Konfigurationsvalidierung
- **Automatische Validierung**: Alle Konfigurationsparameter werden automatisch bei der Zuweisung validiert
- **Bootstrap-Server-Format**: Unterstützt sowohl einfache (`host:port`) als auch protokoll-präfixierte Formate (`PLAINTEXT://host:port`)
- **Sicherheitsfeatures**: Konfigurierbare Sicherheitsfunktionen für Produktionsumgebungen
- **Connection-Pool-Management**: Konfigurierbare Verbindungspool-Größe für bessere Ressourcenverwaltung

### Verbesserte Observability
- **Strukturierte Logs**: Erweiterte Logging-Informationen mit GroupID, Timestamps und Event-Kontext
- **Fehlerkontext**: Detaillierte Fehlerinformationen mit Retry-Status und Event-Type-Details
- **Performance-Tracking**: Bessere Nachvollziehbarkeit von Batch-Operationen und Retry-Versuchen
- **Batch-Progress-Logging**: Automatisches Progress-Logging bei großen Batch-Operationen (alle 100 Events)

### Robustheit-Verbesserungen
- **Intelligente Retry-Logik**: Differenzierte Retry-Strategien basierend auf Fehlertypen (keine Retries für Serialization/Auth-Fehler)
- **Exponential Backoff**: Konfigurierbare Retry-Delays mit exponential backoff (1s initial, max 10s backoff)
- **Controlled Batch Concurrency**: Optimierte Batch-Verarbeitung mit konfigurierbarer Parallelität (Standard: 10 concurrent operations)
- **Testcontainer-Kompatibilität**: Vollständige Kompatibilität mit Docker-basierten Tests
- **Enhanced Error Handling**: Verbesserte Fehlerbehandlung mit strukturierten Kontext-Informationen

### Test-Suite Optimierung
- **Fokussierte Unit Tests**: Bereinigte Test-Suite mit Fokus auf essentielle Funktionalität
- **MockK Integration**: Moderne Mocking-Frameworks für isolierte Unit Tests
- **StepVerifier Korrekturen**: Korrigierte reaktive Test-Assertions für `Mono<Unit>` Rückgabetypen
- **Reduced Test Complexity**: Entfernung unnötiger Performance- und Logging-Tests zugunsten fokussierter Funktionstests

## Troubleshooting

### Häufige Probleme und Lösungen

#### 1. Connection-Fehler zu Kafka

**Problem**: `MessagingError.ConnectionError` beim Senden oder Empfangen von Nachrichten

**Mögliche Ursachen und Lösungen**:

1. **Kafka-Cluster-Erreichbarkeit prüfen**:

```bash
# Teste Verbindung zu Kafka-Cluster
telnet kafka-cluster 9092

# Oder mit nc (netcat)
nc -zv kafka-cluster 9092
```

2. **Bootstrap-Server-Konfiguration validieren**:

```kotlin
// Multiple Broker für High Availability
kafkaConfig.bootstrapServers = "kafka-01:9092,kafka-02:9092,kafka-03:9092"
```

3. **Netzwerk-Timeouts erhöhen für langsame Verbindungen**:

```kotlin
// Producer-Konfiguration erweitern
override fun producerConfigs(): Map<String, Any> = super.producerConfigs() + mapOf(
    ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG to 30000,  // 30 Sekunden
    ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG to 60000  // 1 Minute
)
```

#### 2. Deserialization-Fehler

**Problem**: `MessagingError.DeserializationError` beim Empfangen von Nachrichten

**Lösungsansätze**:

```kotlin
// 1. Trusted Packages erweitern
kafkaConfig.trustedPackages = "at.mocode.*,com.mycompany.*,java.util.*"

// 2. Event-Schema-Kompatibilität prüfen
@JsonIgnoreProperties(ignoreUnknown = true)
data class EventDetails(
    val id: String,
    val version: Int = 1  // Schema-Versionierung
)

// 3. Dead Letter Queue für Poison Messages implementieren
private suspend fun handlePoisonMessage(topic: String, error: MessagingError.DeserializationError) {
    val dlqTopic = "${topic}.dlq"
    eventPublisher.publishEvent(dlqTopic, "error", error.message)
}
```

#### 3. Performance-Probleme

**Problem**: Langsame Message-Verarbeitung oder hohe Latenz

**Optimierungsstrategien**:

```kotlin
// 1. Connection Pool vergrößern
kafkaConfig.connectionPoolSize = 50

// 2. Batch-Verarbeitung nutzen
suspend fun processEventsBatch(events: List<EventDetails>) {
    val batchSize = 100
    events.chunked(batchSize).forEach { batch ->
        // Parallele Verarbeitung pro Batch
        batch.map { event ->
            async { processEvent(event) }
        }.awaitAll()
    }
}

// 3. Consumer-Parallelität erhöhen
// Mehrere Consumer-Instanzen mit unterschiedlichen Group-IDs
```

#### 4. Memory-Leaks bei Consumers

**Problem**: Speicherverbrauch steigt kontinuierlich

**Lösungen**:

```kotlin
// 1. Consumer-Cache korrekt verwalten
@PreDestroy
fun cleanup() {
    eventConsumer.cleanup()  // Cached receivers freigeben
}

// 2. Flow-Streams korrekt beenden
eventConsumer.receiveEventsWithResult(topic, EventDetails::class.java)
    .asFlow()
    .take(1000)  // Streams begrenzen
    .catch { exception ->
        logger.error("Stream error", exception)
    }
    .collect { /* process */ }

// 3. Subscription Management
val subscription = eventConsumer.receiveEvents<EventDetails>(topic)
    .take(Duration.ofMinutes(5))  // Auto-Timeout nach 5 Minuten
    .subscribe()
```

### Best Practices

#### 1. Error Handling

```kotlin
// Strukturierte Fehlerbehandlung mit spezifischen Aktionen
suspend fun handleMessagingError(error: MessagingError, topic: String) {
    when (error) {
        is MessagingError.SerializationError,
        is MessagingError.DeserializationError -> {
            // Keine Retries - permanente Fehler
            alertMonitoring("Schema compatibility issue", error)
        }
        is MessagingError.ConnectionError,
        is MessagingError.TimeoutError -> {
            // Retries möglich - temporäre Fehler
            scheduleRetry(error, topic)
        }
        is MessagingError.AuthenticationError -> {
            // Security-Issue - sofortige Attention erforderlich
            alertSecurity("Authentication failed", error)
        }
        else -> {
            // Unbekannte Fehler - Investigation erforderlich
            alertDevelopment("Unknown messaging error", error)
        }
    }
}
```

#### 2. Monitoring und Alerting

```kotlin
// Umfassendes Monitoring einrichten
@Component
class MessagingMetrics( private val meterRegistry: MeterRegistry ) {
    private val publishedEvents = Counter.builder("messaging.events.published")
        .register(meterRegistry)

    private val consumedEvents = Counter.builder("messaging.events.consumed")
        .register(meterRegistry)

    private val errorCounter = Counter.builder("messaging.errors")
        .tag("type", "unknown")
        .register(meterRegistry)

    fun recordPublishedEvent(topic: String) {
        publishedEvents.increment(Tags.of("topic", topic))
    }

    fun recordError(error: MessagingError, topic: String) {
        errorCounter.increment(
            Tags.of(
                "error.type", error.javaClass.simpleName,
                "topic", topic
            )
        )
    }
}
```

#### 3. Testing von Messaging-Code

```kotlin
// Integration Test mit Testcontainers
@TestMethodOrder(OrderAnnotation::class)
class MessagingIntegrationTest {

    companion object {
        @Container
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
    }

    @Test
    @Order(1)
    fun `should publish and consume events successfully`() = runTest {
        // Given
        val topic = "test-topic"
        val event = EventDetails("test-id", "test-data")

        // When
        val publishResult = eventPublisher.publishEvent(topic, event.id, event)
        val consumedEvents = mutableListOf<Result<EventDetails>>()

        eventConsumer.receiveEventsWithResult(topic, EventDetails::class.java)
            .asFlow()
            .take(1)
            .collect { result -> consumedEvents.add(result) }

        // Then
        publishResult.shouldBeSuccess()
        consumedEvents.shouldHaveSize(1)
        consumedEvents.first().getOrNull()?.id shouldBe event.id
    }
}
```

### Häufig gestellte Fragen (FAQ)

**Q: Wie unterscheidet sich die moderne API von der Legacy-API?**

A: Die moderne API nutzt das Result Pattern für explizite Fehlerbehandlung und Kotlin Coroutines für bessere Performance. Legacy APIs verwenden reaktive Streams mit Exception-basierter Fehlerbehandlung.

**Q: Wann sollte ich Batch-Verarbeitung verwenden?**

A: Batch-Verarbeitung ist empfohlen bei:

- Mehr als 10 Events pro Sekunde
- Hoher Netzwerk-Latenz zum Kafka-Cluster
- Events, die zusammen verarbeitet werden können

**Q: Wie handle ich Backpressure bei hohem Event-Durchsatz?**

A: Nutzen Sie die eingebauten Flow-Operatoren:

```kotlin
eventConsumer.receiveEventsWithResult(topic, EventType::class.java)
    .asFlow()
    .buffer(1000)  // Puffering für Backpressure-Handling
    .flowOn(Dispatchers.IO)  // Separater Dispatcher
    .collect { /* process */ }
```

---

**Letzte Aktualisierung**: 15. August 2025

## Aktualisierungen (September 2025)

- ReactiveKafkaConfig: Der Bean kafkaConfig() ist jetzt mit @ConditionalOnMissingBean annotiert. Dadurch wird kein zweiter KafkaConfig-Bean erzeugt, wenn bereits extern einer bereitgestellt wird. Dies verhindert Bean-Kollisionen und erleichtert Überschreibungen in Services/Tests.
- Legacy Consumer API: Die reifizierte Extension receiveEvents<T>(topic) wirft bei Fehlern nicht mehr, sondern filtert Fehl-Results heraus und protokolliert sie. Das hält den Flux lebendig und ist robuster. Die moderne, empfohlene Methode bleibt receiveEventsWithResult(topic): Flow<Result<T>>.
- Dokumentation: Diese Hinweise wurden ergänzt. Module bleiben ansonsten unverändert und production-ready.
