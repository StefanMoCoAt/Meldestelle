# Redis Integration

Dieses Dokument beschreibt die Redis-Integration, die für die Meldestelle-Anwendung implementiert wurde, einschließlich einer verteilten Cache-Lösung mit Offline-Fähigkeit und Redis Streams für Event Sourcing.

## Verteilte Cache-Lösung

### Überblick

Die verteilte Cache-Lösung bietet eine Möglichkeit, Daten über mehrere Instanzen der Anwendung hinweg zu cachen, mit Unterstützung für Offline-Betrieb. Wenn die Anwendung offline ist, kann sie weiterhin aus dem lokalen Cache lesen und in ihn schreiben und mit Redis synchronisieren, wenn die Verbindung wiederhergestellt wird.

### Komponenten

1. **Cache API** (`infrastructure/cache/cache-api`)
   - `DistributedCache`: Schnittstelle für den verteilten Cache
   - `CacheConfiguration`: Schnittstelle für die Cache-Konfiguration
   - `CacheEntry`: Klasse, die einen Cache-Eintrag mit Metadaten für Offline-Fähigkeit darstellt
   - `CacheSerializer`: Schnittstelle für die Serialisierung und Deserialisierung von Cache-Einträgen
   - `ConnectionStatus`: Schnittstellen für die Verfolgung des Verbindungsstatus

2. **Redis Cache Implementation** (`infrastructure/cache/redis-cache`)
   - `RedisDistributedCache`: Redis-Implementierung des verteilten Caches
   - `JacksonCacheSerializer`: Jackson-basierte Implementierung des Cache-Serialisierers
   - `RedisConfiguration`: Spring-Konfiguration für Redis

### Funktionen

- **Grundlegende Cache-Operationen**: get, set, delete, exists
- **Batch-Operationen**: multiGet, multiSet, multiDelete
- **TTL-Unterstützung**: Time-to-live für Cache-Einträge
- **Offline-Fähigkeit**: Weiterarbeiten, wenn Redis nicht verfügbar ist
- **Automatische Synchronisierung**: Synchronisierung mit Redis, wenn die Verbindung wiederhergestellt wird
- **Verbindungsstatus-Verfolgung**: Verfolgung des Verbindungsstatus und Benachrichtigung von Listenern

### Konfiguration

Der Cache kann mit den folgenden Eigenschaften in `application.yml` konfiguriert werden:

```yaml
redis:
  host: localhost
  port: 6379
  password: # Leer lassen für kein Passwort
  database: 0
  connection-timeout: 2000
  read-timeout: 2000
  use-pooling: true
  max-pool-size: 8
  min-pool-size: 2
  connection-check-interval: 10000 # 10 Sekunden
  local-cache-cleanup-interval: 60000 # 1 Minute
  sync-interval: 300000 # 5 Minuten
```

## Redis Streams für Event Sourcing

### Überblick

Redis Streams werden für Event Sourcing verwendet und bieten eine Möglichkeit, Domain-Events zu speichern und abzurufen. Die Implementierung unterstützt das Anhängen von Events an Streams, das Lesen von Events aus Streams und das Abonnieren von Events.

### Komponenten

1. **Event Store API** (`infrastructure/event-store/event-store-api`)
   - `EventStore`: Schnittstelle für den Event Store
   - `EventSerializer`: Schnittstelle für die Serialisierung und Deserialisierung von Events
   - `Subscription`: Schnittstelle für Abonnements von Event-Streams

2. **Redis Event Store Implementation** (`infrastructure/event-store/redis-event-store`)
   - `RedisEventStore`: Redis Streams-Implementierung des Event Stores
   - `JacksonEventSerializer`: Jackson-basierte Implementierung des Event-Serialisierers
   - `RedisEventConsumer`: Consumer für Redis Streams, der Events mit Consumer-Gruppen verarbeitet
   - `RedisEventStoreConfiguration`: Spring-Konfiguration für Redis Event Store

### Funktionen

- **Event-Anhängen**: Anhängen von Events an Streams mit optimistischer Nebenläufigkeitskontrolle
- **Event-Lesen**: Lesen von Events aus Streams
- **Event-Abonnement**: Abonnieren von Events aus bestimmten Streams oder allen Streams
- **Consumer-Gruppen**: Verarbeitung von Events mit Consumer-Gruppen
- **Nebenläufigkeitskontrolle**: Optimistische Nebenläufigkeitskontrolle für das Anhängen von Events

### Konfiguration

Der Event Store kann mit den folgenden Eigenschaften in `application.yml` konfiguriert werden:

```yaml
redis:
  event-store:
    host: localhost
    port: 6379
    password: # Leer lassen für kein Passwort
    database: 1 # Verwenden Sie eine andere Datenbank für den Event Store
    connection-timeout: 2000
    read-timeout: 2000
    use-pooling: true
    max-pool-size: 8
    min-pool-size: 2
    consumer-group: event-processors
    consumer-name: "${spring.application.name}-${random.uuid}"
    stream-prefix: "event-stream:"
    all-events-stream: "all-events"
    claim-idle-timeout: 60000 # 1 Minute
    poll-timeout: 100 # 100 Millisekunden
    poll-interval: 100 # 100 Millisekunden
    max-batch-size: 100
    create-consumer-group-if-not-exists: true
```

## Integrationstests

Integrationstests für Redis-Komponenten werden mit Testcontainers implementiert, das automatisch einen Redis-Container für Tests startet. Dies stellt sicher, dass die Tests in einer isolierten Umgebung laufen und nicht von externen Redis-Instanzen abhängen.

### Ausführen von Integrationstests

Um die Integrationstests auszuführen, verwenden Sie den folgenden Gradle-Befehl:

```bash
./gradlew integrationTest
```

Dies führt alle Tests mit "Integration" in ihrem Namen aus, einschließlich der Redis-Integrationstests.

> **Hinweis:** Aufgrund der im Abschnitt "Bekannte Probleme und Einschränkungen" erwähnten Kompilierungsprobleme können die Integrationstests möglicherweise nicht lokal ausgeführt werden, bis diese Probleme behoben sind. Der CI/CD-Workflow ist korrekt konfiguriert, um die Tests in Zukunft auszuführen, sobald diese Probleme behoben sind.

### CI/CD-Integration

Das Projekt enthält einen GitHub Actions-Workflow für die Ausführung von Integrationstests, der in `.github/workflows/integration-tests.yml` definiert ist. Dieser Workflow:

1. Richtet einen Redis-Service-Container für Integrationstests ein
2. Führt die Integrationstests mit dem `integrationTest` Gradle-Task aus
3. Lädt Testberichte als Artefakte für einfachen Zugriff hoch

Der Workflow wird bei Push auf main- und develop-Branches sowie bei Pull-Requests auf diese Branches ausgelöst.

### Schreiben von Redis-Integrationstests

Beim Schreiben von Integrationstests für Redis-Komponenten:

1. Verwenden Sie die `@Testcontainers`-Annotation, um die Testcontainers-Unterstützung zu aktivieren
2. Definieren Sie einen Redis-Container mit `GenericContainer` und dem Redis-Image
3. Konfigurieren Sie die Redis-Verbindung mit dem Host und dem gemappten Port des Containers
4. Verwenden Sie die `@Container`-Annotation, um sicherzustellen, dass der Container automatisch gestartet und gestoppt wird

Beispiel:

```kotlin
@Testcontainers
class RedisIntegrationTest {

    companion object {
        @Container
        val redisContainer = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
    }

    @BeforeEach
    fun setUp() {
        val redisPort = redisContainer.getMappedPort(6379)
        val redisHost = redisContainer.host

        // Konfigurieren Sie die Redis-Verbindung mit redisHost und redisPort
    }

    // Testmethoden
}
```

## Bekannte Probleme und Einschränkungen

1. **IDE-Auflösungsprobleme**: Die IDE kann für einige Klassen nicht aufgelöste Referenzen anzeigen, aber der Code sollte korrekt kompilieren und ausgeführt werden. Dies liegt daran, dass die Abhängigkeiten in den build.gradle.kts-Dateien enthalten sind, aber möglicherweise nicht korrekt von der IDE aufgelöst werden.

2. **Test-Abhängigkeiten**: Die Tests erfordern, dass Docker installiert und ausgeführt wird, damit Testcontainers ordnungsgemäß funktionieren.

3. **Abhängigkeitsauflösung**: Wenn Sie bei der Ausführung der Integrationstests auf Probleme mit der Abhängigkeitsauflösung stoßen, stellen Sie sicher, dass das platform-bom-Modul explizite Versionseinschränkungen für alle erforderlichen Abhängigkeiten enthält. Die folgenden Abhängigkeiten sind besonders wichtig für Redis-Integrationstests:
   - `org.springframework.boot:spring-boot-starter-data-redis`
   - `io.lettuce:lettuce-core`
   - `com.fasterxml.jackson.module:jackson-module-kotlin`
   - `com.fasterxml.jackson.datatype:jackson-datatype-jsr310`
   - `org.testcontainers:testcontainers`
   - `org.testcontainers:junit-jupiter`
   - `javax.annotation:javax.annotation-api`

   Stand Juli 2025 wurde die Abhängigkeit `javax.annotation:javax.annotation-api` mit Version 1.3.2 zum platform-bom-Modul hinzugefügt.

4. **Kompilierungsprobleme**: Es gibt bekannte Kompilierungsprobleme in den Redis-bezogenen Dateien, von denen einige behoben wurden, während andere noch behoben werden müssen:

   **In RedisEventConsumer.kt (behoben):**
   - Probleme mit der Behandlung von Nullable-Typen bei booleschen Ausdrücken (Zeilen 144 und 203) - BEHOBEN
   - Probleme mit der Typkonvertierung von Int zu Long (Zeile 187) - BEHOBEN
   - Probleme mit der Behandlung von Nullable-Sammlungen (Zeile 198) - BEHOBEN
   - Probleme mit den Parametern der pending-Methode (Zeile 220) - BEHOBEN
   - Probleme mit dem Spread-Operator bei Nullable-Typen (Zeile 230) - BEHOBEN

   **In RedisEventStore.kt (behoben):**
   - Probleme mit der Typkonvertierung von Int zu Long (Zeilen 122 und 152) - BEHOBEN
   - Probleme mit der Behandlung von Nullable-Sammlungen (Zeilen 128, 158, 188 und 193) - BEHOBEN

   **In RedisEventStoreConfiguration.kt (behoben):**
   - Typfehlanpassung mit RedisPassword (Zeile 59) - BEHOBEN

   Diese Probleme hängen hauptsächlich mit der API-Kompatibilität zwischen Spring Data Redis und Kotlins Typsystem zusammen. Sie müssen in einem zukünftigen Update behoben werden. Vorerst können Sie diese Probleme umgehen, indem Sie:

   a. **Die CI/CD-Pipeline für die Ausführung von Tests verwenden**, die über die richtige Umgebung verfügt

   b. **Problematische Abschnitte vorübergehend auskommentieren oder modifizieren**, wenn Sie Tests lokal ausführen:

   Zum Beispiel können Sie in RedisEventConsumer.kt die claimPendingMessages-Methode wie folgt modifizieren:

   ```kotlin
   private fun claimPendingMessages() {
       try {
           // Get all stream keys
           val streamKeys = redisTemplate.keys("${properties.streamPrefix}*") ?: return

           // Comment out the problematic sections for local testing
           // For each stream key, log that we're skipping pending message processing
           for (streamKey in streamKeys) {
               logger.debug("Skipping pending message processing for stream: $streamKey")
           }

           // Original implementation commented out for local testing
           /*
           for (streamKey in streamKeys) {
               // Get pending messages summary
               val pendingSummary = redisTemplate.opsForStream<String, String>()
                   .pending(streamKey, properties.consumerGroup)

               // Rest of the implementation...
           }
           */
       } catch (e: Exception) {
           logger.error("Error claiming pending messages: ${e.message}", e)
       }
   }
   ```

   c. **Testspezifische Implementierungen erstellen**, die die Verwendung der problematischen APIs vermeiden:

   ```kotlin
   // Test-specific implementation that avoids using problematic APIs
   class TestRedisEventConsumer(
       private val redisTemplate: StringRedisTemplate,
       private val serializer: EventSerializer,
       private val properties: RedisEventStoreProperties
   ) {
       // Simplified implementation for testing
       fun registerEventHandler(eventType: String, handler: (DomainEvent) -> Unit) {
           // Test implementation
       }

       // Other methods...
   }
   ```

   d. **Mock-Objekte für Tests verwenden** anstelle der tatsächlichen Redis-Implementierung:

   ```kotlin
   @Test
   fun testWithMocks() {
       // Mock the Redis template
       val redisTemplate = mock(StringRedisTemplate::class.java)
       val operations = mock(RedisStreamOperations::class.java)

       // Set up the mock to return expected values
       whenever(redisTemplate.opsForStream<String, String>()).thenReturn(operations)

       // Test with mocks instead of actual Redis implementation
   }
   ```

   e. **Sich auf Unit-Tests konzentrieren** anstatt auf Integrationstests, bis diese Probleme behoben sind

5. **API-Kompatibilität**: Die aktuelle Implementierung verwendet Spring Data Redis APIs, die sich in neueren Versionen geändert haben könnten. Stellen Sie bei der Behebung der Kompilierungsprobleme sicher, dass Sie die richtigen Methodensignaturen für die im platform-bom angegebene Version von Spring Data Redis verwenden.

6. **Serialisierung**: Die aktuelle Implementierung verwendet Jackson für die Serialisierung, was möglicherweise nicht für alle Anwendungsfälle am effizientesten ist. Erwägen Sie die Verwendung eines effizienteren Serialisierungsformats wie Protocol Buffers oder Avro für den Produktionseinsatz.

7. **Fehlerbehandlung**: Die aktuelle Implementierung enthält grundlegende Fehlerbehandlung, aber für den Produktionseinsatz könnte eine robustere Fehlerbehandlung erforderlich sein.

8. **Überwachung**: Die aktuelle Implementierung enthält keine Überwachung oder Metriken. Erwägen Sie, Überwachung und Metriken für den Produktionseinsatz hinzuzufügen.

## Fehlerbehebung

### Kompilierungsprobleme

Wenn Sie auf Kompilierungsprobleme mit dem Redis-bezogenen Code stoßen:

1. **Überprüfen Sie die spezifischen Fehlermeldungen**, um zu identifizieren, auf welche der bekannten Probleme Sie stoßen.
2. **Wenden Sie die entsprechende Umgehungslösung** aus dem Abschnitt "Bekannte Probleme und Einschränkungen" an.
3. **Überprüfen Sie die Abhängigkeitsversionen**, um sicherzustellen, dass sie mit den im platform-bom angegebenen übereinstimmen.
4. **Erwägen Sie die Verwendung einer anderen IDE**, wenn Sie IDE-spezifische Auflösungsprobleme haben.
5. **Melden Sie neue Probleme**, die nicht in der Dokumentation behandelt werden.

### Probleme mit der Abhängigkeitsauflösung

Wenn Sie bei der Ausführung der Integrationstests auf Probleme mit der Abhängigkeitsauflösung stoßen, versuchen Sie Folgendes:

1. Stellen Sie sicher, dass das platform-bom-Modul explizite Versionseinschränkungen für alle erforderlichen Abhängigkeiten enthält.
2. Überprüfen Sie, ob das redis-event-store-Modul alle notwendigen Abhängigkeiten enthält.
3. Führen Sie den Gradle-Build mit dem Flag `--refresh-dependencies` aus, um Gradle zu zwingen, Abhängigkeiten erneut herunterzuladen.
4. Löschen Sie den Gradle-Cache, indem Sie das Verzeichnis `.gradle` in Ihrem Home-Verzeichnis löschen.
5. Wenn Sie eine IDE verwenden, aktualisieren Sie das Gradle-Projekt, um sicherzustellen, dass die IDE die neuesten Abhängigkeiten kennt.

### Probleme mit der Konfiguration von Integrationstests

Wenn Sie Probleme mit der Konfiguration des integrationTest-Tasks haben, überprüfen Sie Folgendes:

1. Stellen Sie sicher, dass der integrationTest-Task in der build.gradle.kts-Datei korrekt konfiguriert ist.
2. Überprüfen Sie, ob die Verzeichnisse für Testklassen korrekt eingestellt sind.
3. Überprüfen Sie, ob die Test-Source-Sets korrekt konfiguriert sind.
4. Führen Sie den Gradle-Build mit dem Flag `--info` oder `--debug` aus, um detailliertere Informationen über das Problem zu erhalten.

## Zukünftige Verbesserungen

1. **Clustering-Unterstützung**: Unterstützung für Redis-Clustering für hohe Verfügbarkeit und Skalierbarkeit hinzufügen.

2. **Komprimierung**: Unterstützung für die Komprimierung von Cache-Einträgen hinzufügen, um den Speicherverbrauch zu reduzieren.

3. **Verschlüsselung**: Unterstützung für die Verschlüsselung sensibler Daten im Cache hinzufügen.

4. **Metriken**: Metriken für Cache- und Event-Store-Operationen hinzufügen.

5. **Circuit Breaker**: Circuit-Breaker-Muster für Redis-Operationen hinzufügen, um Kaskadenausfälle zu verhindern.

6. **Batch-Verarbeitung**: Batch-Verarbeitung für bessere Leistung verbessern.

7. **Anpassbare Serialisierung**: Anpassbare Serialisierungsformate ermöglichen.

8. **Verbesserte Fehlerbehandlung**: Robustere Fehlerbehandlungs- und Wiederherstellungsmechanismen hinzufügen.

9. **Dokumentation**: Detailliertere Dokumentation und Beispiele hinzufügen.

10. **Integrationstests**: Umfassendere Integrationstests hinzufügen.
