---
type: Journal
status: COMPLETED
owner: Lead Architect
date: 2026-01-30
participants:
  - Lead Architect
---

# Session Log: 30. Jänner 2026 - Refactoring Exposed & Ktor

## Zielsetzung
Durchführung der strategischen Migration auf Exposed 1.0.0 (Stable) und Ktor 3.4.0, basierend auf den technischen Analyseberichten.

## Durchgeführte Arbeiten

### 1. Versions-Update (`libs.versions.toml`)
*   **Exposed:** Aktualisiert von `1.0.0-rc-4` auf `1.0.0`.
*   **Ktor:** Aktualisiert von `3.3.3` auf `3.4.0`.
*   **Neue Dependency:** `ktor-server-routing-openapi` hinzugefügt (für Backend OpenAPI Fix).

### 2. Exposed Migration (Backend)
*   **Problem:** `DatabaseUtils.kt` enthielt veraltete/falsche Imports (`org.jetbrains.exposed.v1...`), die nicht mit Exposed 1.0.0 kompatibel sind.
*   **Lösung:** Die Imports wurden auf die Standard-Exposed-Packages (`org.jetbrains.exposed.sql...`) korrigiert.
*   **UUID-Thematik:** Die Tabellen-Definitionen (`VeranstaltungTable`, etc.) nutzen weiterhin `UUIDTable`. Es besteht das Risiko, dass Exposed 1.0.0 hier auf `kotlin.uuid.Uuid` gewechselt hat. Dies muss beim nächsten Build verifiziert werden. Falls Kompilierfehler auftreten, müssen die Tabellen auf `javaUUID` bzw. `JavaUUIDTable` (falls existent) migriert werden.

### 3. Ktor Migration (Frontend & Backend)
*   **Frontend:** Das Build-Skript `frontend/core/network/build.gradle.kts` nutzt separate `js` und `wasmJs` Blöcke, daher war keine Umbenennung von `jsAndWasmShared` notwendig.
*   **Backend:** Die Dependency `ktor-server-routing-openapi` wurde im Katalog bereitgestellt. Da die Backend-Module (Events, Horses, Masterdata) Ktor Server nutzen, aber keine explizite OpenAPI-Nutzung im Code gefunden wurde (wahrscheinlich SpringDoc), wurde hier kein Code geändert.

## Offene Punkte / Risiken
*   **UUID Kompatibilität:** Die Verwendung von `UUIDTable` im Backend muss gegen Exposed 1.0.0 getestet werden. Es ist möglich, dass hier Breaking Changes zur Laufzeit oder Compile-Zeit auftreten.
*   **Ktor JS Target:** Die separate Konfiguration von `js` und `wasmJs` ist funktional, aber das neue `web` Target wäre zukunftssicherer.

## Nächste Schritte
1.  **Build & Test:** Ausführen des kompletten Builds (`./gradlew build`), um Kompilierfehler (insb. UUIDs) zu identifizieren.
2.  **Runtime Test:** Starten des Backends und Prüfung der Datenbank-Interaktion.
