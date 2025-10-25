# Redis Cache Module

## Überblick

Dieses Modul stellt eine konkrete Implementierung der `cache-api` unter Verwendung von Redis als Caching-Backend bereit.

## Architektur

Das Modul folgt dem Provider-Pattern:

- **cache-api**: Provider-agnostische Interfaces (`CacheService`, `DistributedCache`)
- **redis-cache**: Redis-spezifische Implementierung

## Verwendung

### Dependency Hinzufügen

```kotlin
dependencies {
    implementation(projects.infrastructure.cache.redisCache)
}
```

### Konfiguration

Das Modul verwendet Spring Boot Auto-Configuration. Konfigurieren Sie Redis über `application.yml`:

```yaml
redis:
  host: localhost
  port: 6379
  password: null  # Optional
  database: 0     # Default cache database
  connectionTimeout: 2000
  readTimeout: 2000
  usePooling: true
  maxPoolSize: 8
  minPoolSize: 2
```

### Code-Beispiel

```kotlin
@Service
class MyService(private val cache: DistributedCache) {

    suspend fun getData(key: String): MyData? {
        return cache.get(key, MyData::class)
    }

    suspend fun saveData(key: String, data: MyData) {
        cache.put(key, data, ttl = 1.hours)
    }
}
```

## Features

- ✅ TTL-Unterstützung für Cache-Einträge
- ✅ Connection State Tracking
- ✅ Health Monitoring
- ✅ Jackson-basierte Serialisierung
- ✅ Connection Pooling mit Lettuce
- ✅ Kotlin Coroutines Support

## Beans

Das Modul registriert folgende Spring Beans:

- `redisConnectionFactory`: Standard Redis ConnectionFactory
- `redisTemplate`: RedisTemplate<String, ByteArray> für Cache-Operationen
- `cacheSerializer`: Jackson-basierter Serializer (kann überschrieben werden)
- `cacheConfiguration`: Standard Cache-Konfiguration (kann überschrieben werden)

## Gleichzeitige Verwendung mit redis-event-store

⚠️ **WICHTIG**: Wenn Sie sowohl `redis-cache` als auch `redis-event-store` im selben Service verwenden:

### Unterschiedliche Databases

Die Module verwenden **separate Redis Databases**, um Konflikte zu vermeiden:

- **redis-cache**: Database 0 (Standard)
- **redis-event-store**: Database 1 (konfigurierbar)

### Konfigurationsbeispiel

```yaml
# Redis Cache Konfiguration
redis:
  host: localhost
  port: 6379
  database: 0  # Cache verwendet Database 0
```

```yaml
# Redis Event Store Konfiguration
redis:
  event-store:
    host: localhost
    port: 6379
    database: 1  # Event Store verwendet Database 1
```

### Bean-Namen

Die Module verwenden unterschiedliche Bean-Namen:

| Komponente | redis-cache | redis-event-store |
|------------|-------------|-------------------|
| ConnectionFactory | `redisConnectionFactory` | `eventStoreRedisConnectionFactory` |
| Template | `redisTemplate` | `eventStoreRedisTemplate` |
| Serializer | `cacheSerializer` | `eventSerializer` |

### Keine Konflikte

✅ Die Module sind so designed, dass sie **ohne Konflikte** gleichzeitig verwendet werden können:

- Separate ConnectionFactories mit `@Qualifier`
- Separate Property-Prefixes (`redis` vs `redis.event-store`)
- Unterschiedliche Database-Nummern
- Unterschiedliche Bean-Namen

## Serialisierung

Das Modul verwendet Jackson für die Serialisierung:

- Automatische Kotlin-Modul Integration
- Java 8 Date/Time Support
- Custom Serializer können via `@Bean` überschrieben werden

## Health Checks

Das Modul tracked automatisch den Redis-Verbindungsstatus:

- Connection State (CONNECTED, DISCONNECTED, CONNECTING)
- Connection State Listeners für Benachrichtigungen
- Automatische Reconnect-Versuche

## Performance

- **Connection Pooling**: Wiederverwendbare Verbindungen via Lettuce
- **Non-blocking I/O**: Reaktive Operations mit Kotlin Coroutines
- **Optimierte Serialisierung**: Jackson-basiert mit Byte-Array-Caching

## Troubleshooting

### Redis Verbindungsfehler

```
RedisConnectionFailureException: Unable to connect to Redis
```

**Lösung**: Überprüfen Sie Redis-Server und Netzwerk-Konfiguration.

### Serialisierungsfehler

```
SerializationException: Could not serialize object
```

**Lösung**: Stellen Sie sicher, dass Ihre Datenklassen mit Jackson serialisierbar sind (data classes, keine private Konstruktoren).

### Bean-Konflikte mit Event Store

Wenn Sie Fehler wie "Multiple beans of type RedisConnectionFactory" erhalten:

**Lösung**: Verwenden Sie `@Qualifier` Annotations oder stellen Sie sicher, dass Sie die neueste Version beider Module verwenden (Bean-Namen-Konflikte sind bereits behoben).

## Weitere Informationen

- Siehe auch: [event-store-api README](../../event-store/event-store-api/README.md)
- Siehe auch: [redis-event-store README](../../event-store/redis-event-store/README.md)
