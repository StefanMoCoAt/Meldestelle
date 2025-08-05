# Core Module

## Überblick

Das Core-Modul bildet das Fundament des gesamten Meldestelle-Systems und implementiert den **Shared Kernel** nach Domain-Driven Design Prinzipien. Es stellt gemeinsame, domänen-agnostische Konzepte, Utilities und Infrastrukturkomponenten bereit, die von allen anderen Modulen verwendet werden.

## Architektur

Das Modul ist nach den Prinzipien der Clean Architecture in zwei Hauptkomponenten unterteilt:

* **`:core-domain`**: Der "reine" Teil des Kernels. Enthält nur Datenstrukturen und Interfaces ohne externe Abhängigkeiten.
* **`:core-utils`**: Stellt technische Hilfsfunktionen und konkrete Implementierungen bereit, die auf dem `core-domain` aufbauen.

## Core-Domain Komponenten

Dieses Modul hat eine **minimale Oberfläche**, um eine maximale Entkopplung der Fach-Services zu gewährleisten.

* **`BaseDto.kt`**: Definiert standardisierte DTOs (Data Transfer Objects) wie `ApiResponse<T>` und `PagedResponse<T>`, um eine konsistente API-Struktur im gesamten System sicherzustellen.
* **`DomainEvent.kt`**: Stellt die Basis-Infrastruktur für Domänen-Events (`DomainEvent`, `BaseDomainEvent`) bereit, die für eine asynchrone, ereignisgesteuerte Kommunikation unerlässlich ist.
* **`Enums.kt`**: Enthält ausschließlich fundamental querschnittliche Enums. Nach einem Refactoring verbleibt hier nur noch `DatenQuelleE`, da es die Herkunft von Daten beschreibt – ein Konzept, das für alle Domänen relevant ist. Domänenspezifische Enums (z.B. für Pferderassen oder Disziplinen) wurden bewusst entfernt.
* **`Serializers.kt`**: Bietet benutzerdefinierte Serializer für `kotlinx.serialization`, um Typen wie `Uuid` und `Instant` systemweit konsistent in JSON umzuwandeln.

## Core-Utils Komponenten

* **Konfiguration (`config/`)**:
    * **`ConfigLoader.kt`**: Implementiert ein sauberes Muster zur Entkopplung der Konfigurations-Ladelogik. Er liest `.properties`-Dateien und Umgebungsvariablen.
    * **`AppConfig.kt`**: Dient als reine, unveränderliche Datenklasse, die die vom `ConfigLoader` geladenen Werte enthält. Dieses Muster verbessert die Testbarkeit erheblich.
* **Datenbank (`database/`)**:
    * **`DatabaseFactory.kt`**: Eine robuste Factory zur Verwaltung von Datenbankverbindungen mit einem hoch-performanten Connection Pool (HikariCP) und automatischer Datenbank-Migration durch den Industriestandard **Flyway**.
* **Fehlerbehandlung (`error/`)**:
    * **`Result.kt`**: Eine typsichere, versiegelte Klasse (`sealed class`) für funktionales Error-Handling, die den übermäßigen Einsatz von Exceptions für erwartete Geschäftsfehler vermeidet.
* **Validierung (`validation/`)**:
    * **`ValidationResult.kt`**: Eine vereinheitlichte, serialisierbare Datenstruktur (`ValidationResult`, `ValidationError`) zur systemweiten, konsistenten Kommunikation von Validierungsfehlschlägen über API-Grenzen hinweg.

## Testing-Strategie

Das `core`-Modul ist durch eine umfassende Suite von Unit- und Integrationstests abgesichert, die einen hohen Qualitätsstandard setzen.

* **Unit-Tests**: Kritische Komponenten wie der `ConfigLoader`, die Serializer und die `ApiResponse`-Logik sind durch Unit-Tests abgedeckt.
* **Datenbank-Tests (Goldstandard)**: Die Datenbanklogik wird nicht gegen eine ungenaue In-Memory-Datenbank (wie H2) getestet. Stattdessen wird **Testcontainers** verwendet, um für jeden Testlauf eine echte **PostgreSQL-Datenbank** in einem Docker-Container zu starten. Dies garantiert 100%ige Kompatibilität zwischen Test- und Produktionsumgebung.

---
