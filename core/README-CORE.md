# Core Module

## Überblick

Das Core-Modul bildet das Fundament des gesamten Meldestelle-Systems und implementiert den **Shared Kernel** nach Domain-Driven Design Prinzipien. Es stellt gemeinsame Domänenkonzepte, Utilities und Infrastrukturkomponenten bereit, die von allen anderen Modulen (members, horses, events, masterdata, infrastructure) verwendet werden.

## Architektur

Das Core-Modul ist nach den Prinzipien der Clean Architecture in zwei Hauptkomponenten unterteilt:


```
core/
├── core-domain/                 # Shared Domain Layer
│   ├── model/                   # Gemeinsame Domain Models
│   │   ├── BaseDto.kt          # Basis-DTO-Klassen
│   │   └── Enums.kt            # Gemeinsame Enumerationen
│   ├── serialization/          # Serialisierung
│   │   └── Serializers.kt      # Custom Serializers
│   └── event/                  # Domain Events
│       └── DomainEvent.kt      # Event-Infrastruktur
└── core-utils/                 # Shared Utilities
    ├── config/                 # Konfiguration
    │   ├── AppConfig.kt        # Anwendungskonfiguration
    │   └── AppEnvironment.kt   # Umgebungskonfiguration
    ├── database/               # Datenbank-Utilities
    │   ├── DatabaseConfig.kt   # Datenbank-Konfiguration
    │   └── DatabaseFactory.kt  # Datenbank-Factory
    ├── discovery/              # Service Discovery
    │   └── ServiceRegistration.kt # Service-Registrierung
    ├── error/                  # Fehlerbehandlung
    │   └── Result.kt           # Result-Type für Fehlerbehandlung
    ├── serialization/          # Serialisierung
    │   └── Serialization.kt    # Serialisierungs-Utilities
    └── validation/             # Validierung
        ├── ValidationResult.kt  # Validierungsergebnisse
        ├── ValidationUtils.kt   # Validierungs-Utilities
        └── ApiValidationUtils.kt # API-Validierung
```

## Core-Domain Komponenten

### 1. Gemeinsame Enumerationen (`Enums.kt`)
Zentrale Enumerationen, die modulübergreifend verwendet werden, um eine konsistente "Ubiquitäre Sprache" zu etablieren. Dazu gehören `SparteE`, `PferdeGeschlechtE`, `RolleE` und `BerechtigungE`.

### 2. Basis-DTOs (`BaseDto.kt`)
Gemeinsame Basisklassen für Data Transfer Objects, die eine konsistente API-Struktur im gesamten System sicherstellen, wie `ApiResponse<T>` für Standard-Antworten und `PagedResponse<T>` für paginierte Listen.

### 3. Domain Events (`DomainEvent.kt`)
Die Event-Sourcing Infrastruktur für Domain-Driven Design. Definiert die Basis-Interfaces (`DomainEvent`, `DomainEventPublisher`, `DomainEventHandler`) für die asynchrone Kommunikation zwischen den Services.

### 4. Custom Serializers (`Serializers.kt`)
Spezialisierte Serializer für Kotlin-Typen wie `Uuid`, `Instant` und `LocalDate`, um eine einheitliche JSON-Serialisierung über alle Services hinweg zu garantieren.

## Core-Utils Komponenten

### 1. Fehlerbehandlung (`Result.kt`)
Eine funktionale und typsichere `Result<T, E>`-Klasse zur Behandlung von vorhersehbaren Geschäftsfehlern ohne den Einsatz von Exceptions. Dies führt zu robusterem und besser lesbarem Code in den Anwendungs-Services.

### 2. Konfiguration (`AppConfig.kt`, `AppEnvironment.kt`)
Eine zentrale und flexible Konfigurationsverwaltung, die Einstellungen aus verschiedenen Quellen (Umgebungsvariablen, Property-Dateien) für unterschiedliche Umgebungen (`DEVELOPMENT`, `PRODUCTION` etc.) laden kann.

### 3. Datenbank-Utilities (`DatabaseFactory.kt`, `DatabaseConfig.kt`)
Stellt die zentrale Logik für Datenbankverbindungen bereit. Die `DatabaseFactory` konfiguriert einen hoch-performanten Connection Pool (HikariCP) und integriert das Industrie-Standard-Tool **Flyway** für die automatische Ausführung von versionierten SQL-Datenbankmigrationen beim Start eines Service.

### 4. Validierung (`ValidationUtils.kt`, `ApiValidationUtils.kt`)
Eine umfassende Sammlung von wiederverwendbaren Hilfsfunktionen zur Validierung von Daten, von einfachen Längenprüfungen bis hin zu komplexen API-Parameter-Validierungen.

### 5. Service Discovery (`ServiceRegistration.kt`)
Eine Implementierung zur Registrierung von Microservices bei einem Consul-Server, um eine dynamische Service-Landschaft zu ermöglichen.

## Verwendung in anderen Modulen

Andere Module deklarieren eine Abhängigkeit zum `core`-Modul, um auf dessen Bausteine zugreifen zu können.

### API Responses
```kotlin
// In API Controllers
@RestController
class MemberController {

    @GetMapping("/api/members/{id}")
    fun getMember(@PathVariable id: String): ApiResponse<Member> {
        // ...
    }
}
```

### Result-Type Usage

```kotlin
// In Use Cases
class CreateMemberUseCase {
    suspend fun execute(member: Member): Result<Member, ValidationError> {
        // ...
    }
}
```

## Best Practices

### 1. Shared Kernel Prinzipien

- **Minimale Oberfläche**: Nur wirklich gemeinsame Konzepte
- **Stabile APIs**: Änderungen beeinflussen alle Module
- **Versionierung**: Sorgfältige Versionierung bei Breaking Changes
- **Dokumentation**: Umfassende Dokumentation für alle Komponenten

### 2. Fehlerbehandlung

- **Result-Type verwenden**: Statt Exceptions für erwartete Geschäftsfehler, um die Geschäftslogik klar und explizit zu halten.
- **Validierung**: Frühe Validierung mit ValidationResult

### 3. Serialisierung

- **Custom Serializers**: Für spezielle Datentypen werden die bereitgestellten Serializer verwendet, um Konsistenz zu gewährleisten.
- **Schema-Evolution**: Bei der Weiterentwicklung von DTOs und Events muss die Abwärtskompatibilität berücksichtigt werden.

## Zukünftige Erweiterungen

1. **Event Sourcing Enhancements** - Erweiterte Event Store Features
2. **Distributed Tracing** - OpenTelemetry Integration
3. **Metrics Collection** - Micrometer Integration
4. **Configuration Management** - Externalized Configuration
5. **Security Utilities** - Encryption/Decryption Utilities
6. **Caching Abstractions** - Cache-Provider Abstractions
7. **Async Utilities** - Coroutines Utilities
8. **Testing Utilities** - Test-Helpers und Fixtures

---

**Letzte Aktualisierung**: 28. Juli 2025

Für weitere Informationen zur Gesamtarchitektur siehe [README.md](../README.md).
