# Offline-First-Architektur

Dieses Dokument beschreibt die **Zielarchitektur** für die Offline-First-Strategie im KMP-Frontend.

---

## Status Quo (Stand: 13.01.2026)

**WICHTIG:** Dieses Dokument beschreibt die **geplante Zielarchitektur**. Die aktuelle Implementierung ist ein erster Schritt in diese Richtung.

*   **SQLDelight-Modul (`:frontend:core:local-db`):** Das Modul ist konfiguriert und nutzt `generateAsync = true` für eine asynchrone API.
*   **Datenbank-Schema:** Aktuell ist nur eine einfache Tabelle `LocalSettings` implementiert, die zum Speichern von lokalen Schlüssel-Wert-Paaren dient.
    *   Siehe: `frontend/core/local-db/src/commonMain/sqldelight/at/mocode/frontend/core/localdb/MeldestelleDb.sq`
*   **Komplexe Entitäten & Sync:** Die im Folgenden beschriebenen Repositories für Fachdaten, die `Flow`-basierte Datenflüsse und die Delta-Sync-Logik sind **noch nicht implementiert**.

---

## Zielarchitektur

### Grundprinzip

Die Anwendung soll auch ohne eine aktive Netzwerkverbindung voll funktionsfähig sein. Alle Daten, die der Benutzer sieht und bearbeitet, werden in einer lokalen Datenbank auf dem Gerät gespeichert. Die Synchronisation mit dem Backend erfolgt im Hintergrund, sobald eine Netzwerkverbindung verfügbar ist.

**Single Source of Truth (für die UI):** Die lokale SQLDelight-Datenbank ist die alleinige Quelle der Wahrheit für die Benutzeroberfläche. Die UI liest niemals direkt aus dem Netzwerk.

### Komponenten

1.  **Lokale Datenbank (SQLDelight):**
    *   Hält eine Kopie der relevanten Backend-Daten.
    *   Verwendet plattformspezifische Treiber:
        *   **Web (JS/Wasm):** `WebWorkerDriver` mit OPFS (Origin Private File System) für persistente Speicherung im Browser.
        *   **Desktop (JVM):** `JdbcSqliteDriver` für die Speicherung in einer lokalen SQLite-Datei.
    *   Bietet eine asynchrone API (`suspend`-Funktionen), wie bereits durch `generateAsync` sichergestellt.

2.  **Repositories:**
    *   Kapseln den Zugriff auf die lokale Datenbank.
    *   Bieten `Flow`-basierte APIs, um die UI über Datenänderungen zu informieren.
    *   Beispiel: `fun getHorses(): Flow<List<Horse>>`

3.  **ViewModels:**
    *   Sammeln die Daten-Flows von den Repositories und transformieren sie in einen UI-Zustand (`StateFlow`).
    *   Leiten Benutzerinteraktionen an die entsprechenden Use Cases oder Repositories weiter.

4.  **Sync-Logik:**
    *   Ein separater Mechanismus, der für den Datenabgleich zwischen der lokalen Datenbank und dem Backend verantwortlich ist.
    *   **Delta-Sync:** Um die Netzwerklast zu minimieren, werden nur die Änderungen seit der letzten Synchronisation übertragen. Dies wird durch die Verwendung von Zeitstempeln oder Versionsnummern (z.B. UUIDv7) in den Datenmodellen ermöglicht.
    *   **Konfliktlösung:** Bei gleichzeitigen Änderungen auf dem Client und im Backend muss eine Konfliktlösungsstrategie implementiert werden (z.B. "Last-Write-Wins" oder eine manuelle Auflösung durch den Benutzer).

### Datenfluss (Lesen)

1.  Die UI (Composable) beobachtet einen `StateFlow` im ViewModel.
2.  Das ViewModel sammelt einen `Flow` aus einem Repository.
3.  Das Repository fragt die lokale SQLDelight-Datenbank ab und gibt einen `Flow` zurück.
4.  Jede Änderung in der lokalen Datenbank wird automatisch an die UI weitergegeben.

### Datenfluss (Schreiben)

1.  Der Benutzer löst eine Aktion in der UI aus (z.B. Klick auf "Speichern").
2.  Die UI ruft eine Funktion im ViewModel auf.
3.  Das ViewModel ruft eine `suspend`-Funktion in einem Repository auf.
4.  Das Repository schreibt die Änderung in die lokale SQLDelight-Datenbank.
5.  Die Änderung wird (ggf. in einer separaten "outbox"-Tabelle) für die nächste Synchronisation vorgemerkt.
6.  Die UI wird durch den Lese-Datenfluss automatisch aktualisiert.

### Synchronisations-Prozess

1.  Ein Hintergrund-Job (z.B. ein Coroutine-Worker) wird periodisch oder bei Netzwerkverfügbarkeit gestartet.
2.  Der Sync-Worker sendet die vorgemerkten lokalen Änderungen an das Backend.
3.  Der Sync-Worker fragt das Backend nach neuen Änderungen seit der letzten Synchronisation ab.
4.  Die neuen Daten vom Backend werden in die lokale Datenbank geschrieben.
5.  Die UI wird durch den Lese-Datenfluss automatisch aktualisiert.
