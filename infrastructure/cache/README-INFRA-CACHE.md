# Infrastructure/Cache Module

## Überblick

Das **Cache-Modul** stellt eine zentrale, hochverfügbare und wiederverwendbare Caching-Infrastruktur für alle Microservices bereit. Es dient der Verbesserung der Anwendungsperformance, der Reduzierung von Latenzen und der Entlastung der primären PostgreSQL-Datenbank.

## Architektur: Port-Adapter-Muster

Das Modul folgt streng dem **Port-Adapter-Muster** (Hexagonale Architektur), um eine saubere Trennung zwischen der Caching-Schnittstelle (dem "Port") und der konkreten Implementierung (dem "Adapter") zu gewährleisten.

* **`:infrastructure:cache:cache-api`**: Definiert den abstrakten "Vertrag" für das Caching (`DistributedCache`-Interface), ohne sich um die zugrunde liegende Technologie zu kümmern. Die Fach-Services programmieren ausschließlich gegen dieses Interface.
* **`:infrastructure:cache:redis-cache`**: Die konkrete Implementierung des Vertrags, die **Redis** als hochperformantes Caching-Backend verwendet. Kapselt die gesamte Redis-spezifische Logik.

## Schlüsselfunktionen

* **Offline-Fähigkeit & Resilienz:** Das Modul verfügt über einen In-Memory-Cache, der bei einem Ausfall der Redis-Verbindung als Fallback dient. Schreib-Operationen werden lokal als "dirty" markiert und automatisch mit Redis synchronisiert, sobald die Verbindung wiederhergestellt ist.
* **Idiomatische Kotlin-API:** Bietet neben der Standard-API auch ergonomische Erweiterungsfunktionen mit `reified`-Typen für eine saubere und typsichere Verwendung in Kotlin-Code (`cache.get<User>("key")`).
* **Projekweite Konsistenz:** Verwendet `kotlin.time.Duration` und `kotlin.time.Instant` für eine einheitliche Handhabung von Zeit- und Dauer-Angaben im gesamten Projekt.
* **Automatisierte Verbindungsüberwachung:** Überprüft periodisch den Zustand der Redis-Verbindung und informiert Listener über Statusänderungen (`CONNECTED`, `DISCONNECTED`).

## Verwendung

Ein Microservice bindet `:infrastructure:cache:redis-cache` als Abhängigkeit ein und lässt sich das `DistributedCache`-Interface per Dependency Injection geben.

**Beispiel mit der idiomatischen Kotlin-API:**
```kotlin
@Service
class MasterdataService(
    private val cache: DistributedCache // Nur das Interface wird verwendet!
) {
    fun findCountryById(id: String): Country? {
        val cacheKey = "country:$id"

        // 1. Versuche, aus dem Cache zu lesen (typsicher und sauber)
        val cachedCountry = cache.get<Country>(cacheKey)
        if (cachedCountry != null) {
            return cachedCountry
        }

        // 2. Wenn nicht im Cache, aus der DB lesen
        val dbCountry = countryRepository.findById(id)

        // 3. Ergebnis in den Cache schreiben für zukünftige Anfragen
        dbCountry?.let {
            cache.set(cacheKey, it, ttl = 1.hours) // Cache für 1 Stunde
        }
        return dbCountry
    }
}
```

## Testing-Strategie
Die Qualität des Moduls wird durch eine zweistufige Teststrategie sichergestellt:

* **Integrationstests mit Testcontainers: Die Kernfunktionalität wird gegen eine echte Redis-Datenbank getestet, die zur Laufzeit in einem Docker-Container gestartet wird. Dies garantiert 100%ige Kompatibilität.**

* **Unit-Tests mit MockK: Die komplexe Logik der Offline-Fähigkeit und Synchronisation wird durch das Mocking des RedisTemplate getestet. So können Verbindungsausfälle zuverlässig simuliert werden, ohne den Test-Lebenszyklus zu stören.**
