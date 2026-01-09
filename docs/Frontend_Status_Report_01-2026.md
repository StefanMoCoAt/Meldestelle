# Frontend Status Report & Architecture Update
**Datum:** 08.01.2026
**Von:** Senior Frontend Developer (KMP/Compose)
**An:** Lead Software Architect

## 1. Executive Summary
Das Frontend-System ("Meldestelle Portal") wurde erfolgreich auf eine moderne, zukunftssichere **Kotlin Multiplatform (KMP)** Architektur migriert, die echte Offline-Fähigkeit im Web (via Wasm/JS) und Desktop (JVM) unterstützt.

Kritische Blockaden im Build-System (Room-Inkompatibilität mit JS) wurden durch einen strategischen Wechsel zu **SQLDelight** gelöst. Das Dependency-Management wurde zentralisiert und bereinigt.

**Status:** Frontend-Build ist stabil (nach Fixes in `core-utils`). Backend-Anpassungen sind erforderlich.

---

## 2. Durchgeführte Maßnahmen

### 2.1 Architektur & Persistenz (Offline-First)
*   **Problem:** Die ursprünglich geplante Nutzung von **Room** erwies sich als Blocker für die Web-Targets (JS/Wasm), da Room derzeit nur JVM/Android/Native unterstützt.
*   **Lösung:** Migration zu **SQLDelight 2.2.1**.
    *   Ermöglicht echte Cross-Platform-Persistenz.
    *   **Web (JS/Wasm):** Nutzung von `WebWorkerDriver` in Kombination mit **OPFS (Origin Private File System)** für performante, persistente Speicherung im Browser.
    *   **Desktop (JVM):** Nutzung von `JdbcSqliteDriver` mit Java 25 Virtual Threads für nicht-blockierende I/O.
    *   **Async-First:** Die Datenbank-Schnittstellen wurden auf `suspend` (asynchron) umgestellt (`generateAsync = true`), um die strikten Anforderungen des Browsers zu erfüllen.

### 2.2 Build-System & Dependency Management
*   **Single Source of Truth:** Die `libs.versions.toml` wurde komplett refaktoriert.
    *   Klare Trennung zwischen **Frontend (KMP)** und **Backend (Spring/Infra)** Dependencies.
    *   Einführung von **Bundles** (`kmp-common`, `ktor-client-common`, `compose-common`) zur massiven Reduktion von Boilerplate in den `build.gradle.kts` Dateien.
*   **Wasm-Support:** Fehlende Plattform-Konfigurationen (`PlatformConfig.wasmJs.kt`) wurden ergänzt.
*   **Bereinigung:** Veraltete und nicht genutzte Dependencies wurden entfernt.

### 2.3 Infrastruktur & Web-Integration
*   **Webpack Konfiguration:** Hinzufügen von `opfs-headers.js`, um die notwendigen HTTP-Header (`Cross-Origin-Opener-Policy`, `Cross-Origin-Embedder-Policy`) für `SharedArrayBuffer` und OPFS im Dev-Server zu setzen.
*   **Web Worker:** Implementierung eines Custom Workers (`sqlite.worker.js`), der die Datenbank im Hintergrund-Thread verwaltet.

---

## 3. Auswirkungen auf das Backend & Core (Action Required)

Während der Frontend-Optimierung wurden Inkonsistenzen im Shared-Code (`core:core-utils`) aufgedeckt, die das Backend betreffen.

### 3.1 `core:core-utils` Kontamination
*   **Problem:** Das Modul `core:core-utils` ist als KMP-Modul konfiguriert, enthielt aber JVM-spezifischen Code für **Exposed** (JDBC-basiertes ORM), der im Frontend (JS/Wasm) nicht kompilierbar ist.
*   **Temporäre Lösung:** Die Datei `DatabaseUtils.kt` (Exposed-Helper) wurde **auskommentiert**, um den Frontend-Build zu retten.
*   **TODO Backend:**
    1.  Prüfen, ob `DatabaseUtils.kt` im Backend essenziell ist.
    2.  Falls ja: Verschieben in ein reines Backend-Modul (z.B. `:backend:common` oder `:backend:infrastructure:persistence`).
    3.  `core:core-utils` muss "rein" bleiben (nur KMP-kompatibler Code), wenn es vom Frontend konsumiert werden soll.

### 3.2 API-Verträge
*   Durch den Wechsel auf SQLDelight und die Async-First Architektur im Frontend ändert sich nichts an der REST-API, aber die Erwartungshaltung an die Synchronisation (Sync-Logik) wird wichtiger.

---

## 4. Nächste Schritte (Frontend Roadmap)

1.  **Navigation:** Implementierung einer robusten Navigationslösung (Type-Safe, Deep-Linking-fähig), die sowohl Desktop als auch Web (Browser-History) sauber bedient.
2.  **Sync-Logik:** Implementierung des Datenaustauschs zwischen lokaler SQLDelight-DB und Backend-APIs (Offline-Sync).
3.  **UI-Integration:** Anbindung der neuen asynchronen Datenbank-Repositories an die Compose-ViewModels (`Flow` -> `State`).

---

**Empfehlung an Lead Architect:**
Bitte weisen Sie den **Senior Backend Developer** an, das Modul `core:core-utils` zu bereinigen und die auskommentierte Datenbank-Logik in ein geeignetes Backend-Modul zu migrieren. Das Frontend ist nun stabil und bereit für die Feature-Entwicklung.
