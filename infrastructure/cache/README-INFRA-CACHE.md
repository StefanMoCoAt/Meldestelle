# Infrastructure/Cache – Modulbeschreibung und Implementierungsleitfaden

Letzte Aktualisierung: 03. September 2025

## Zweck und Aufgaben des Moduls

Das Infrastructure/Cache-Modul stellt eine einheitliche, technologie‑neutrale Cache‑Schnittstelle für alle Services bereit und liefert mit einer Redis‑basierten Adapter‑Implementierung die produktionsreife Ausführung. Ziele:

- Antwortzeiten reduzieren und Primärdatenbanken entlasten.
- Einheitliche API für Lesen/Schreiben, Batch‑Operationen und TTLs.
- Resilienz bei Redis‑Ausfällen durch lokalen Fallback.
- Operative Transparenz durch einfache Metriken, Health‑Informationen und periodische Wartungsaufgaben.

## Architektur (Port‑Adapter)

- cache‑api: enthält die öffentlichen Verträge und Basistypen
  - DistributedCache: zentrale Port‑Schnittstelle für Cache‑Operationen
  - CacheEntry, CacheConfiguration, CacheSerializer
  - ConnectionStatusTracker/ConnectionStateListener zur Verbindungsüberwachung
- redis‑cache: Adapter, der die Port‑Schnittstelle mit Spring Data Redis umsetzt
  - RedisDistributedCache: konkrete Implementierung inkl. Offline‑Fallback, Dirty‑Sync, Batchs, Key‑Prefixing, TTL‑Handling und einfachen Metriken
  - JacksonCacheSerializer: serialisiert Werte und CacheEntry per Jackson

## Öffentliche API (Auszug)

DistributedCache

- get(key, clazz)/set(key, value, ttl?)
- delete(key), exists(key)
- multiGet(keys, clazz), multiSet(map, ttl?)
- multiDelete(keys)
- synchronize(keys?), markDirty(key), getDirtyKeys(), clear()

Idiomatic Kotlin Extensions

- cache.get<T>(key)
- cache.multiGet<T>(keys)

CacheConfiguration (DefaultCacheConfiguration vorhanden)

- defaultTtl?, localCacheMaxSize?, offlineModeEnabled, synchronizationInterval, offlineEntryMaxAge?, keyPrefix, compressionEnabled, compressionThreshold

Hinweis: Die Kompression wird aktuell durch den Serializer bereitgestellt; Schwellwerte/Flags sind für zukünftiges Tuning vorgesehen.

## Implementierungsdetails (RedisDistributedCache)

- Lokaler Fallback: ConcurrentHashMap als lokaler Cache speichert CacheEntry inkl. expiresAt. Bei Redis‑Ausfall werden Schreibvorgänge lokal gehalten und als „dirty“ markiert.
- Dirty‑Synchronisation: Sobald die Verbindung wieder ONLINE ist, werden geänderte Schlüssel zu Redis synchronisiert (synchronize()).
- Key‑Prefixing: Alle externen Keys werden mittels keyPrefix gekapselt, um Mandanten/Services zu isolieren.
- TTL/Expiration: TTL wird einheitlich über kotlin.time.Duration angegeben und für Redis in java.time.Duration konvertiert. Lokale Einträge enthalten expiresAt und werden periodisch bereinigt.
- Batch‑Operationen: multiGet/multiSet/multiDelete nutzen Redis‑Batching/Pipelining, lokal wird konsistent gespiegelt.
- Größenbegrenzung Local Cache (neu): Wenn localCacheMaxSize gesetzt ist, werden bei Überschreitung die am längsten nicht mehr modifizierten Einträge aus dem lokalen Cache entfernt (LRM – least recently modified). Dadurch bleibt der lokale Fallback speichereffizient.
- Periodische Aufgaben (@Scheduled):
  - Verbindungsprüfung: fixedDelayString = "${redis.connection-check-interval:10000}"
  - Lokale Bereinigung: fixedDelayString = "${redis.local-cache-cleanup-interval:60000}"
  - Dirty‑Sync: fixedDelayString = "${redis.sync-interval:300000}"
  - Metriken‑Log: fixedDelayString = "${redis.metrics-log-interval:300000}"

Wichtige Robustheitsdetails

- Alle Redis‑Operationen fangen RedisConnectionFailureException ab und schalten den ConnectionState auf DISCONNECTED. Beim nächsten erfolgreichen Zugriff wird CONNECTED gesetzt und eine Synchronisation der dirty keys ausgelöst.
- multiSet setzt TTLs bei Bedarf per Pipeline nach (pExpire); einzelne set‑Operationen nutzen expire via Duration.

## Verwendung (Beispiele)

Einbinden: Projekte hängen gegen :infrastructure:cache:redis-cache und injizieren DistributedCache.

Lesen/Schreiben mit TTL

```kotlin
val user = cache.get<User>("user:42")
if (user == null) {
    val loaded = userRepository.findById("42") ?: return null
    cache.set("user:42", loaded, ttl = 1.hours)
}
```

Batch‑Lesezugriff

```kotlin
val ids = listOf("user:1", "user:2", "user:3")
val map = cache.multiGet<User>(ids)
```

Bulk‑Schreiben

```kotlin
cache.multiSet(mapOf(
    "cfg:app" to appConfig,
    "cfg:features" to features
), ttl = 30.minutes)
```

Verbindungsstatus überwachen

```kotlin
cache.registerConnectionListener(object : ConnectionStateListener {
    override fun onConnectionStateChanged(newState: ConnectionState, timestamp: Instant) {
        logger.info("Cache connection state: $newState at $timestamp")
    }
})
```

## Konfiguration

DefaultCacheConfiguration bietet sinnvolle Defaults. Relevante Properties (optional via Spring @Scheduled Platzhalter):

- redis.connection-check-interval: ms für Verbindungsprüfung (Default 10000)
- redis.local-cache-cleanup-interval: ms für lokale Bereinigung (Default 60000)
- redis.sync-interval: ms für Synchronisationsläufe (Default 300000)
- redis.metrics-log-interval: ms für periodisches Metriken‑Logging (Default 300000)

Hinweise

- keyPrefix sollte pro Service gesetzt werden (z. B. "masterdata"), um Kollisionen zu vermeiden.
- localCacheMaxSize begrenzt die Größe des lokalen Fallback‑Caches. Bei null ist die Größe unbegrenzt.

## Betrieb & Monitoring

- Health‑Infos: getHealthStatus() liefert eine einfache Einschätzung basierend auf ConnectionState und Erfolgsrate der Operationen.
- Metriken: getPerformanceMetrics() liefert einfache Kennzahlen (Operations, Success‑Rate, Größe lokaler Cache, Anzahl dirty Keys). Periodisches Logging per @Scheduled möglich.
- Cache Warming: warmCache(keys, loader) und warmCacheBulk(map) helfen, Hot‑Keys/gefragte Konfigurationen beim Start vorzuwärmen.

## Grenzen & bekannte Punkte

- Kompression ist im Serializer implementiert; die konfigurierbaren Flags/Schwellenwerte sind derzeit nicht dynamisch an/aus‑geschaltet.
- Offline‑Modus: Die Konfiguration offlineModeEnabled ist vorhanden; die Implementierung betreibt den lokalen Fallback standardmäßig bei Verbindungsproblemen. Eine harte Deaktivierung dieses Verhaltens ist aktuell nicht verdrahtet.

## Changelog (Kurz)

- 2025‑09‑03: Fehlerbehebungen für @Scheduled‑Platzhalter, korrektes Logging im Cache‑Warming, lokale Cache‑Größenbegrenzung (LRM‑Eviction) hinzugefügt. Dokumentation aktualisiert (diese Datei).

## Fazit

Das Cache‑Modul bietet eine klare, wiederverwendbare Cache‑Schnittstelle mit einer robusten Redis‑Implementierung. Es unterstützt TTLs, Batch‑Operationen, lokalen Fallback bei Ausfällen und liefert einfache, praxistaugliche Betriebsinformationen. Mit keyPrefix und lokalen Limits ist der Einsatz in Multi‑Service‑Umgebungen unkompliziert und stabil.
