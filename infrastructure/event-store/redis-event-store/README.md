# Redis Event Store Module

## Überblick

Dieses Modul stellt eine konkrete Implementierung der `event-store-api` unter Verwendung von Redis Streams als Event-Store-Backend bereit.

## Architektur

Das Modul folgt dem Provider-Pattern:

- **event-store-api**: Provider-agnostische Interfaces (`EventStore`, `EventSerializer`)
- **redis-event-store**: Redis Streams-spezifische Implementierung

## Verwendung

### Dependency Hinzufügen

```kotlin
dependencies {
    implementation(projects.infrastructure.eventStore.redisEventStore)
}
```

### Konfiguration

Das Modul verwendet Spring Boot Auto-Configuration. Konfigurieren Sie Redis über `application.yml`:

```yaml
redis:
  event-store:
    host: localhost
    port: 6379
    password: null  # Optional
    database: 1     # Separate database for event store (default: 1)
    connectionTimeout: 2000
    readTimeout: 2000
    usePooling: true
    maxPoolSize: 8
    minPoolSize: 2
    consumerGroup: event-processors
    consumerName: event-consumer
    streamPrefix: "event-stream:"
    allEventsStream: all-events
    claimIdleTimeout: 60s
    pollTimeout: 100ms
    maxBatchSize: 100
    createConsumerGroupIfNotExists: true
```

### Code-Beispiel

```kotlin
@Service
class MyEventService(
    private val eventStore: EventStore,
    private val eventConsumer: RedisEventConsumer
) {

    // Event speichern
    suspend fun saveEvent(aggregateId: Uuid, event: DomainEvent) {
        eventStore.appendEvent(
            aggregateId = aggregateId,
            event = event,
            expectedVersion = EventVersion.ANY
        )
    }

    // Events abrufen
    suspend fun loadEvents(aggregateId: Uuid): List<DomainEvent> {
        return eventStore.loadEvents(aggregateId)
    }

    // Events konsumieren
    fun startConsuming() {
        eventConsumer.consumeEvents { event ->
            println("Received event: $event")
        }
    }
}
```

## Features

- ✅ Event Sourcing mit Redis Streams
- ✅ Optimistic Locking mit Event Versioning
- ✅ Consumer Groups für parallele Event-Verarbeitung
- ✅ Event Replay-Fähigkeit
- ✅ Pub/Sub für Event-Benachrichtigungen
- ✅ Jackson-basierte Serialisierung
- ✅ Connection Pooling mit Lettuce
- ✅ Kotlin Coroutines Support

## Redis Streams

Das Modul nutzt Redis Streams für Event Sourcing:

- **Stream pro Aggregate**: `event-stream:{aggregateId}`
- **All Events Stream**: `event-stream:all-events`
- **Consumer Groups**: Für parallele Verarbeitung
- **Message IDs**: Für Event-Ordering und Replay

## Beans

Das Modul registriert folgende Spring Beans:

- `eventStoreRedisConnectionFactory`: Separate Redis ConnectionFactory für Event Store
- `eventStoreRedisTemplate`: StringRedisTemplate für Event-Operationen
- `eventSerializer`: Jackson-basierter Event-Serializer
- `eventStore`: EventStore Implementierung
- `eventConsumer`: RedisEventConsumer für Event-Verarbeitung

## Gleichzeitige Verwendung mit redis-cache

⚠️ **WICHTIG**: Wenn Sie sowohl `redis-cache` als auch `redis-event-store` im selben Service verwenden:

### Unterschiedliche Databases

Die Module verwenden **separate Redis Databases**, um Konflikte zu vermeiden:

- **redis-cache**: Database 0 (Standard)
- **redis-event-store**: Database 1 (Standard, konfigurierbar)

### Konfigurationsbeispiel

```yaml
# Beide Module in einer application.yml
redis:
  # Cache Konfiguration
  host: localhost
  port: 6379
  database: 0  # Cache verwendet Database 0

  # Event Store Konfiguration (nested)
  event-store:
    host: localhost
    port: 6379
    database: 1  # Event Store verwendet Database 1
    consumerGroup: event-processors
```

### Bean-Namen

Die Module verwenden unterschiedliche Bean-Namen zur Vermeidung von Konflikten:

| Komponente | redis-cache | redis-event-store |
|------------|-------------|-------------------|
| ConnectionFactory | `redisConnectionFactory` | `eventStoreRedisConnectionFactory` |
| Template | `redisTemplate` | `eventStoreRedisTemplate` |
| Serializer | `cacheSerializer` | `eventSerializer` |

### Keine Konflikte

✅ Die Module sind so designed, dass sie **ohne Konflikte** gleichzeitig verwendet werden können:

- **Separate ConnectionFactories** mit `@Qualifier` Annotations
- **Separate Property-Prefixes**: `redis` vs `redis.event-store`
- **Unterschiedliche Database-Nummern**: 0 vs 1
- **Unterschiedliche Bean-Namen**: Explizite Qualifier verhindern Kollisionen

## Event Versioning

Das Modul unterstützt Optimistic Locking:

```kotlin
// Erwartete Version spezifizieren
eventStore.appendEvent(
    aggregateId = aggregateId,
    event = myEvent,
    expectedVersion = EventVersion.of(5)  // Erwartet Version 5
)

// Beliebige Version akzeptieren
eventStore.appendEvent(
    aggregateId = aggregateId,
    event = myEvent,
    expectedVersion = EventVersion.ANY
)
```

Bei Version-Konflikten wird eine `ConcurrencyException` geworfen.

## Consumer Groups

Das Modul unterstützt Consumer Groups für parallele Event-Verarbeitung:

```kotlin
// Consumer 1
eventConsumer.consumeEvents(
    consumerName = "consumer-1"
) { event ->
    // Verarbeite Event
    processEvent(event)
}

// Consumer 2 (in der gleichen Consumer Group)
eventConsumer.consumeEvents(
    consumerName = "consumer-2"
) { event ->
    // Verarbeite Event parallel
    processEvent(event)
}
```

Events werden automatisch auf verfügbare Consumer verteilt.

## Event Replay

Sie können Events von einem bestimmten Zeitpunkt oder Message-ID replaying:

```kotlin
// Replay alle Events eines Aggregates
val events = eventStore.loadEvents(aggregateId)

// Replay Events ab einer bestimmten Version
val eventsFromVersion = eventStore.loadEvents(
    aggregateId = aggregateId,
    fromVersion = EventVersion.of(10)
)
```

## Serialisierung

Das Modul verwendet Jackson für Event-Serialisierung:

- Automatische Kotlin-Modul Integration
- Polymorphe Serialisierung für verschiedene Event-Typen
- Custom Serializer können via `@Bean` überschrieben werden

## Performance

- **Connection Pooling**: Wiederverwendbare Verbindungen via Lettuce
- **Batch Processing**: Konfigurierbare Batch-Größe für Consumer
- **Non-blocking I/O**: Reaktive Operations mit Kotlin Coroutines
- **Stream-basiert**: Effiziente Event-Speicherung mit Redis Streams

## Troubleshooting

### Redis Verbindungsfehler

```
RedisConnectionFailureException: Unable to connect to Redis
```

**Lösung**: Überprüfen Sie Redis-Server und Netzwerk-Konfiguration. Stellen Sie sicher, dass Redis Streams unterstützt werden (Redis 5.0+).

### Concurrency Exception

```
ConcurrencyException: Expected version X but found Y
```

**Lösung**: Dies ist normales Verhalten bei Optimistic Locking. Implementieren Sie Retry-Logik oder verwenden Sie `EventVersion.ANY`.

### Consumer Group Fehler

```
Consumer group already exists
```

**Lösung**: Setzen Sie `createConsumerGroupIfNotExists: true` in der Konfiguration oder löschen Sie die Consumer Group manuell.

### Bean-Konflikte mit Cache

Wenn Sie Fehler wie "Multiple beans of type RedisConnectionFactory" erhalten:

**Lösung**: Die Module verwenden bereits unterschiedliche Bean-Namen mit `@Qualifier`. Stellen Sie sicher, dass Sie beide Module korrekt konfiguriert haben (siehe Abschnitt "Gleichzeitige Verwendung").

## Best Practices

1. **Separate Databases**: Verwenden Sie immer separate Redis Databases für Cache und Event Store
2. **Event Versioning**: Verwenden Sie Optimistic Locking für kritische Aggregates
3. **Consumer Groups**: Nutzen Sie Consumer Groups für horizontale Skalierung
4. **Error Handling**: Implementieren Sie Retry-Logik für transiente Fehler
5. **Monitoring**: Überwachen Sie Stream-Größen und Consumer Lag

## Weitere Informationen

- Siehe auch: [cache-api README](../../cache/cache-api/README.md)
- Siehe auch: [redis-cache README](../../cache/redis-cache/README.md)
- Redis Streams Dokumentation: <https://redis.io/docs/data-types/streams/>
