---
type: ADR
status: ACTIVE
owner: Lead Architect
last_update: 2026-01-20
---

# ADR-0013: Stabilisierung des Tech-Stacks (Januar 2026)

## Status
Angenommen

## Kontext
Das Projekt "Meldestelle" setzt auf einen sehr modernen Technologie-Stack (Java 25, Kotlin 2.3.0, Spring Boot 3.5.9). Eine Analyse im Januar 2026 hat jedoch kritische Versionskonflikte aufgedeckt, die die Stabilität des Builds und der Laufzeitumgebung gefährden.

1.  **Spring Cloud Konflikt:** Der Release Train `2025.1.0` (Oakwood) ist für Spring Boot 4.0 konzipiert und inkompatibel mit Spring Boot 3.5.9 (führt zu `NoSuchMethodError`).
2.  **Compose Multiplatform:** Version `1.9.3` führt zu Compiler-Crashes in Verbindung mit Kotlin 2.3.0.
3.  **Exposed:** Version `0.61.0` ist veraltet und inkompatibel mit Kotlin 2.3.0.

## Entscheidung
Wir führen folgende Korrekturen am Tech-Stack durch, um eine stabile "Best Compatibility List" zu etablieren:

1.  **Spring Cloud Downgrade:** Wechsel auf Release Train `2025.0.1` (Northfields), der offiziell für Spring Boot 3.5.x freigegeben ist.
2.  **Compose Multiplatform Upgrade:** Wechsel auf `1.10.0-rc02` (oder stable), um volle Kotlin 2.3.0 Kompatibilität zu gewährleisten.
3.  **Exposed Upgrade:** Wechsel auf `1.0.0-rc-4` (oder neuer), um Bytecode-Inkompatibilitäten zu beheben.
4.  **Micrometer Upgrade:** Explizites Setzen von Version `1.16.1` für verbesserten Java 25 (Virtual Threads) Support.

## Konsequenzen

### Positiv
*   **Stabilität:** Der Build und die Application Context Initialisierung sind wieder stabil.
*   **Zukunftssicherheit:** Wir nutzen weiterhin die neuesten Features von Java 25 und Kotlin 2.3.0, aber in einer validierten Kombination.
*   **Wartbarkeit:** Die `libs.versions.toml` spiegelt nun eine getestete Konfiguration wider.

### Negativ
*   **Migrationsaufwand:** Einmaliger Aufwand zur Anpassung der `libs.versions.toml` und ggf. kleinerer API-Änderungen in Exposed/Compose.

## Compliance
Diese Entscheidung ist bindend für alle Module. Abweichungen müssen durch ein neues ADR genehmigt werden.
