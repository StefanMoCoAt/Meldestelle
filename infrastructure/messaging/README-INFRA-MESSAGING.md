# Infrastructure/Messaging Module

## Überblick

Das **Messaging-Modul** stellt die Infrastruktur für die asynchrone, reaktive Kommunikation zwischen den Microservices bereit. Es nutzt **Apache Kafka** als hochperformanten, verteilten Message-Broker und ist entscheidend für die Entkopplung von Services und die Implementierung einer skalierbaren, ereignisgesteuerten Architektur.

Das Modul implementiert moderne **Domain-Driven Design (DDD)** Prinzipien mit expliziter Fehlerbehandlung über das **Result Pattern** und bietet sowohl suspending Coroutine-APIs als auch reaktive Stream-APIs für maximale Flexibilität.

## Architektur

Das Modul ist in zwei spezialisierte Komponenten aufgeteilt, um Konfiguration von der Client-Logik zu trennen:


infrastructure/messaging/
├── messaging-config/   # Stellt die zentrale Kafka-Konfiguration bereit
└── messaging-client/   # Stellt wiederverwendbare, reaktive Clients bereit


### `messaging-config`

Dieses Modul zentralisiert die grundlegende Kafka-Konfiguration für das gesamte Projekt.

* **Zweck:** Definiert Spring-Beans für die `ProducerFactory` (Basis für Producer) und eine `Map` mit Standard-Konfigurationen für Consumer (z.B. `bootstrap-servers`, `group-id`, Serializer).
* **Vorteil:** Stellt Konsistenz sicher und vereinfacht die Einrichtung neuer Producer oder Consumer in den Services.

### `messaging-client`

Dieses Modul baut auf der Konfiguration auf und stellt wiederverwendbare High-Level-Komponenten für die Interaktion mit Kafka bereit.

* **Zweck:**
    * **`EventPublisher` Interface**: Definiert moderne APIs für das Publizieren von Domain Events mit expliziter Fehlerbehandlung über das Result Pattern.
    * **`KafkaEventPublisher`**: Implementierung des EventPublisher mit sowohl modernen suspending Coroutine-APIs als auch Legacy-reaktiven APIs. Nutzt den `ReactiveKafkaProducerTemplate` von Spring.
    * **`KafkaEventConsumer`**: Ein reaktiver Service zum Empfangen von Nachrichten. Er kapselt die Komplexität von `reactor-kafka` und gibt einen kontinuierlichen `Flux`-Stream von Events zurück.
    * **`MessagingError`**: Domain-spezifische Fehlertypen für typsichere Fehlerbehandlung (SerializationError, ConnectionError, TimeoutError, AuthenticationError, etc.).
* **Vorteil:**
    * Moderne **Result Pattern** APIs für typsichere Fehlerbehandlung ohne Exceptions
    * Sowohl **Coroutine-basierte** als auch **reaktive** APIs verfügbar
    * Kapselt die Komplexität der Kafka-API mit domain-spezifischen Abstraktionen
    * Umfassendes Retry-Management mit intelligenter Retry-Logik

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

## Testing-Strategie

Die Zuverlässigkeit des Moduls wird durch eine mehrstufige Teststrategie sichergestellt, die sowohl Unit- als auch Integrationstests umfasst:

### Integrationstests (Goldstandard)
* **Testcontainers**: Der `KafkaIntegrationTest` startet einen echten Apache Kafka Docker-Container, um die Funktionalität unter realen Bedingungen zu validieren
* **Reaktives Testen**: Nutzt Project Reactor's `StepVerifier` für deterministische Tests der reaktiven Streams ohne unzuverlässige Thread.sleep-Aufrufe
* **Lifecycle Management**: Saubere Ressourcenverwaltung über @BeforeEach und @AfterEach für korrekte Freigabe von Producer-Threads
* **End-to-End Validierung**: Vollständige Publish-Subscribe-Zyklen mit echtem Kafka-Cluster

### Unit Tests
* **`KafkaEventPublisherErrorTest`**: Fokussierte Tests für Fehlerbehandlung mit MockK für isolierte Testszenarien
* **Fehlerszenarien**: Systematische Tests für Serialization-, Authentication-, Connection- und Timeout-Fehler
* **Batch-Verarbeitung**: Validierung von Batch-Operationen und Empty-Batch-Handling
* **Retry-Logic**: Tests für intelligente Retry-Mechanismen und Retry-Exhaustion

### Sicherheits- und Konfigurationstests
* **`KafkaSecurityTest`**: Validierung der Sicherheitskonfigurationen und Trusted-Package-Verwaltung
* **`KafkaEventConsumerCacheTest`**: Tests für Consumer-Caching und Ressourcenoptimierung
* **Konfigurationsvalidierung**: Automatische Validierung aller Konfigurationsparameter

## Neue Features und Optimierungen (2025)

### Domain-Driven Design (DDD) Integration
* **Result Pattern APIs**: Neue suspending Coroutine-basierte APIs mit typsicherer Fehlerbehandlung über das Result Pattern
* **Domain-spezifische Fehlertypen**: Umfassende `MessagingError` Hierarchie (SerializationError, ConnectionError, TimeoutError, AuthenticationError, etc.)
* **Explizite Fehlerbehandlung**: Eliminiert unerwartete Exceptions durch strukturierte Fehler-Typen
* **Backward Compatibility**: Legacy-reactive APIs bleiben verfügbar, sind aber als deprecated markiert

### Erweiterte Konfigurationsvalidierung
* **Automatische Validierung**: Alle Konfigurationsparameter werden automatisch bei der Zuweisung validiert
* **Bootstrap-Server-Format**: Unterstützt sowohl einfache (`host:port`) als auch protokoll-präfixierte Formate (`PLAINTEXT://host:port`)
* **Sicherheitsfeatures**: Konfigurierbare Sicherheitsfunktionen für Produktionsumgebungen
* **Connection-Pool-Management**: Konfigurierbare Verbindungspool-Größe für bessere Ressourcenverwaltung

### Verbesserte Observability
* **Strukturierte Logs**: Erweiterte Logging-Informationen mit GroupID, Timestamps und Event-Kontext
* **Fehlerkontext**: Detaillierte Fehlerinformationen mit Retry-Status und Event-Type-Details
* **Performance-Tracking**: Bessere Nachvollziehbarkeit von Batch-Operationen und Retry-Versuchen
* **Batch-Progress-Logging**: Automatisches Progress-Logging bei großen Batch-Operationen (alle 100 Events)

### Robustheit-Verbesserungen
* **Intelligente Retry-Logik**: Differenzierte Retry-Strategien basierend auf Fehlertypen (keine Retries für Serialization/Auth-Fehler)
* **Exponential Backoff**: Konfigurierbare Retry-Delays mit exponential backoff (1s initial, max 10s backoff)
* **Controlled Batch Concurrency**: Optimierte Batch-Verarbeitung mit konfigurierbarer Parallelität (Standard: 10 concurrent operations)
* **Testcontainer-Kompatibilität**: Vollständige Kompatibilität mit Docker-basierten Tests
* **Enhanced Error Handling**: Verbesserte Fehlerbehandlung mit strukturierten Kontext-Informationen

### Test-Suite Optimierung
* **Fokussierte Unit Tests**: Bereinigte Test-Suite mit Fokus auf essentielle Funktionalität
* **MockK Integration**: Moderne Mocking-Frameworks für isolierte Unit Tests
* **StepVerifier Korrekturen**: Korrigierte reaktive Test-Assertions für `Mono<Unit>` Rückgabetypen
* **Reduced Test Complexity**: Entfernung unnötiger Performance- und Logging-Tests zugunsten fokussierter Funktionstests

---

**Letzte Aktualisierung**: 15. August 2025
