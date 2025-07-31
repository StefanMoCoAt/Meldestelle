# Infrastructure/Cache Module

## Überblick

Das **Cache-Modul** stellt eine zentrale und wiederverwendbare Caching-Infrastruktur für alle Microservices des Meldestelle-Systems bereit. Caching ist eine entscheidende Technik zur Verbesserung der Anwendungsleistung, zur Reduzierung der Latenz und zur Entlastung von Backend-Systemen wie der primären PostgreSQL-Datenbank.

## Architektur: Port-Adapter-Muster

Das Modul folgt streng dem **Port-Adapter-Muster** (auch als Hexagonale Architektur bekannt), um eine saubere Trennung zwischen der Caching-Schnittstelle (dem "Port") und der konkreten Implementierung (dem "Adapter") zu gewährleisten.


infrastructure/cache/
├── cache-api/      # Der "Port": Definiert die Caching-Schnittstelle
└── redis-cache/    # Der "Adapter": Implementiert die Schnittstelle mit Redis


### `cache-api`

Dieses Modul ist der **abstrakte Teil** der Architektur. Es definiert den "Vertrag" für das Caching, ohne sich um die zugrunde liegende Technologie zu kümmern.

* **Zweck:** Definiert ein oder mehrere Interfaces, z.B. `CacheService`, mit generischen Methoden wie `get(key)`, `set(key, value, ttl)` und `evict(key)`.
* **Vorteil:** Jeder Service im System programmiert nur gegen dieses Interface. Die Geschäftslogik ist vollständig von der Caching-Technologie entkoppelt. Ein Austausch des Caching-Providers (z.B. von Redis zu Caffeine) würde keine Änderungen in den Fach-Services erfordern.

### `redis-cache`

Dieses Modul ist die **konkrete Implementierung** der in `cache-api` definierten Schnittstellen.

* **Zweck:** Stellt eine Spring-basierte Konfiguration und eine Implementierung des `CacheService`-Interfaces bereit, die **Redis** als Datenspeicher verwendet. Es nutzt Spring Data Redis und den Lettuce-Client für die Kommunikation.
* **Technologie:** Verwendet Jackson für die Serialisierung der zu cachenden Objekte in das JSON-Format, bevor sie in Redis gespeichert werden.
* **Vorteil:** Kapselt die gesamte Redis-spezifische Logik an einem einzigen Ort.

## Verwendung in anderen Modulen

Ein Microservice, der Caching nutzen möchte, geht wie folgt vor:

1.  **Abhängigkeit deklarieren:** Das Service-Modul (z.B. `masterdata-service`) fügt eine `implementation`-Abhängigkeit zu `:infrastructure:cache:redis-cache` in seiner `build.gradle.kts` hinzu.

    ```kotlin
    // In masterdata-service/build.gradle.kts
    dependencies {
        implementation(projects.infrastructure.cache.redisCache)
    }
    ```

2.  **Interface injizieren:** Im Service-Code wird nur das Interface aus `cache-api` per Dependency Injection angefordert, nicht die konkrete Redis-Klasse.

    ```kotlin
    // In einem Use Case oder Service
    @Service
    class MasterdataService(
        private val cache: CacheService // Nur das Interface wird verwendet!
    ) {
        fun findCountryById(id: String): Country? {
            val cacheKey = "country:$id"
            // 1. Versuche, aus dem Cache zu lesen
            val cachedCountry = cache.get<Country>(cacheKey)
            if (cachedCountry != null) {
                return cachedCountry
            }

            // 2. Wenn nicht im Cache, aus der DB lesen
            val dbCountry = countryRepository.findById(id)

            // 3. Ergebnis in den Cache schreiben für zukünftige Anfragen
            if (dbCountry != null) {
                cache.set(cacheKey, dbCountry, ttl = 3600) // Cache für 1 Stunde
            }
            return dbCountry
        }
    }
    ```

Diese Architektur stellt sicher, dass die Geschäftslogik sauber und von Infrastrukturdetails unberührt bleibt.

---
**Letzte Aktualisierung**: 31. Juli 2025
