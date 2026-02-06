---
type: Journal
status: COMPLETED
owner: Curator
date: 2026-01-27
participants:
  - Lead Architect
  - Frontend Expert
  - Curator
---

# Session Log: 27. Jänner 2026

## Zielsetzung
Stabilisierung der Web-Applikation (JS/Wasm), Behebung von Build-Fehlern und Inbetriebnahme des Delta-Syncs.

## Durchgeführte Arbeiten

### 1. Web-App Build & Runtime Fixes
*   **Problem:** Webpack-Build schlug fehl wegen `sqlite3.wasm` Handling.
*   **Lösung:** Revertierung komplexer Webpack-Hacks. Der Build funktioniert nun standardmäßig, da die Abhängigkeiten korrekt konfiguriert sind.
*   **Problem:** Login schlug fehl mit 404 auf `/members/sync`.
*   **Lösung:** Veralteten Aufruf im `LoginViewModel` entfernt (Members-Modul existiert nicht mehr).

### 2. SQLDelight Async Driver Issues (JS/Wasm)
*   **Problem:** Laufzeitfehler `The driver used with SQLDelight is asynchronous, so SQLDelight should be configured for asynchronous usage` beim Aufruf von `getLatestSince` (Select).
*   **Analyse:** Trotz `generateAsync = true` in `build.gradle.kts` scheint der generierte Code für `executeAsOneOrNull()` oder `executeAsList()` im Browser-Kontext Probleme zu machen, wenn er synchron aufgerufen wird (was bei `suspend` eigentlich nicht passieren sollte, aber evtl. durch fehlende Coroutine-Extensions im Classpath verursacht wurde).
*   **Versuche:**
    *   Transaktion entfernt/hinzugefügt -> Kein Effekt.
    *   `executeAsList()` statt `executeAsOneOrNull()` -> Kein Effekt.
    *   Explizites `await()` -> Kompilierfehler (da `upsert` bereits `suspend Unit` ist).
    *   Hinzufügen von `libs.sqldelight.coroutines` zu `ping-feature` -> Kein Effekt auf den Laufzeitfehler.
*   **Lösung (Workaround):** Bypass in `PingEventRepositoryImpl.getLatestSince()`. Die Methode gibt nun immer `null` zurück, was einen **Full-Sync** erzwingt.
*   **Ergebnis:** Der Sync (`upsert`) läuft nun erfolgreich durch! Das Schreiben in die DB funktioniert asynchron und transaktional.

### 3. UI/UX
*   Das Ping-Service Dashboard zeigt nun im Event-Log erfolgreich "Sync completed successfully" an.

## Offene Punkte & Nächste Schritte

1.  **SQLDelight Async Select Fix:**
    *   Tiefere Analyse, warum `select` Queries im JS-Target den Fehler werfen, während `insert` Queries funktionieren. Eventuell ein Bug in SQLDelight 2.0.2 in Kombination mit Kotlin 2.1.0 oder WebWorkerDriver Konfiguration.
    *   Langfristig sollte der Bypass entfernt werden, um echten Delta-Sync zu ermöglichen.

2.  **Daten-Visualisierung:**
    *   Erweiterung des Dashboards um eine Ansicht der lokal gespeicherten Ping-Events, um den Sync auch visuell zu verifizieren (nicht nur via Logs).

## Technische Erkenntnisse
*   **SQLDelight & JS:** Die Kombination aus `generateAsync=true`, `WebWorkerDriver` und Multiplatform-Modulen ist fragil. Schreiboperationen (`suspend Unit`) scheinen robuster zu sein als Leseoperationen (`ExecutableQuery`), bei denen die asynchrone Ausführung explizit sichergestellt werden muss.
*   **Tracer Bullet:** Der Ansatz, erst die Infrastruktur (Ping Service) komplett durchzustechen, hat sich bewährt. Wir haben fundamentale Probleme im Frontend-Stack (Wasm/DB) identifiziert und gelöst (bzw. mitigiert), bevor wir komplexe Fachlichkeit implementieren.

**Status:** 🟢 **Web-App Running** / 🟡 **Sync (Full-Sync Workaround)**
