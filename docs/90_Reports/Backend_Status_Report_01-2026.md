# Architecture & Build Status Report
**Datum:** 09.01.2026
**Von:** Senior Backend Developer
**An:** Lead Software Architect

## 1. Executive Summary
Wir haben eine umfassende Stabilisierung der Projekt-Architektur durchgeführt. Kritische Versionskonflikte im Backend (Spring Boot vs. Spring Cloud) wurden behoben. Die Trennung zwischen Frontend (KMP) und Backend (JVM) wurde durch Refactoring des `core`-Bereichs strikt durchgesetzt, um "Pollution" durch JVM-Code im Frontend zu verhindern.

Der Build-Prozess ist derzeit noch durch spezifische **Kotlin/Wasm Kompilierungsfehler** blockiert, die aus der strikten Typisierung und dem JS-Interop von WebAssembly resultieren.

---

## 2. Durchgeführte Maßnahmen

### 2.1 Backend Architecture Alignment
*   **Spring Cloud Konflikt gelöst:** Downgrade von `2025.1.0` (Oakwood, inkompatibel mit Boot 3.5) auf **`2025.0.1` (Northfields)**. Dies verhindert garantierte Laufzeitfehler (`NoSuchMethodError`).
*   **Java 25 Optimierung:** Upgrade von Micrometer auf `1.16.1` für besseren Virtual Thread Support.
*   **Exposed Versionierung:** Bestätigung der Nutzung von `1.0.0-rc-4` (statt der veralteten 0.61.0).

### 2.2 Modul-Hygiene & KMP Trennung
*   **Refactoring `core:core-utils`:**
    *   Das Modul enthielt JVM-spezifischen Code (`DatabaseUtils.kt` mit Exposed-Abhängigkeiten), der den Frontend-Build (JS/Wasm) brach.
    *   **Lösung:** Erstellung eines neuen Moduls **`:backend:infrastructure:persistence`**. Der DB-Code wurde dorthin verschoben. `core:core-utils` ist nun ein reines KMP-Modul.
*   **Zirkuläre Abhängigkeiten aufgelöst:**
    *   Das Modul `frontend:shared` hatte Abhängigkeiten zu Feature-Modulen und dem Design-System, was zu Zyklen führte.
    *   **Lösung:** `frontend:shared` wurde bereinigt und dient nun rein als Basis-Layer (Config, Utils).

### 2.3 Build-System (Gradle & KMP)
*   **Wasm-Target Konsolidierung:**
    *   Um Inkonsistenzen bei der Dependency Resolution zu beheben, wurde das Target `wasmJs` **projektweit** in allen relevanten KMP-Modulen (`core`, `frontend`) aktiviert.
    *   Dies löste die `Unresolved platforms: [wasmJs]` Fehler.

---

## 3. Aktuelle Blocker (Wasm Compiler)

Obwohl die Dependency-Struktur nun sauber ist, scheitert der Compiler im `wasmJs` Target an spezifischen Interop-Problemen:

1.  **Fehlende Referenzen (`Unresolved reference`):**
    *   `org.w3c.dom.Worker` und `kotlinx.browser.window` werden im Wasm-Kontext nicht gefunden.
    *   *Ursache:* Kotlin/Wasm benötigt möglicherweise explizite Imports oder externe Deklarationen für bestimmte DOM-APIs, die in Kotlin/JS implizit waren, oder die Standard-Bibliothek wird nicht korrekt eingebunden.
2.  **JS-Interop Einschränkungen:**
    *   Fehler: `Type 'ERROR CLASS: Symbol not found for Worker' cannot be used as return type`.
    *   Kotlin/Wasm erlaubt keine komplexen `js("...")` Blöcke innerhalb von Funktionen und hat keinen `dynamic` Typ. Unsere ersten Fixes (Helper-Funktionen) waren ein Schritt in die richtige Richtung, aber die Typen (wie `Worker`) müssen dem Compiler bekannt gemacht werden.

---

## 4. Nächste Schritte (Plan)

1.  **Wasm-Build reparieren:**
    *   Prüfen, ob wir eine explizite Dependency (z.B. `kotlinx-browser` oder `kotlin-stdlib-wasm-js`) benötigen.
    *   Falls `Worker` in der Wasm-Stdlib fehlt: Definition einer `external class Worker` für Wasm erstellen, um dem Compiler den Typ bekannt zu machen.
2.  **Backend-Verifikation ("Bauplan"):**
    *   Sobald der Build durchläuft (oder wir das Frontend temporär exkludieren), werde ich den **`ping-service`** starten.
    *   Ziel: Nachweis, dass Spring Context, Datenbank-Verbindung (JPA) und die neue Modul-Struktur (`backend:infrastructure:persistence`) zur Laufzeit funktionieren.
3.  **Sync-Strategie:**
    *   Anschließend widmen wir uns der im Frontend-Report erwähnten "Offline-Sync"-Logik (basierend auf UUIDv7).

**Empfehlung:** Wir sollten den Wasm-Build-Fix priorisieren, da er aktuell das gesamte Projekt blockiert ("Fail Fast").
