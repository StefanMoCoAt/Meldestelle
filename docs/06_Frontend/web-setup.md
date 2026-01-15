# Web-Setup (Webpack & Worker)

Dieses Dokument beschreibt die spezifische Konfiguration für das Web-Target (JS/Wasm) des KMP-Frontends.

## OPFS (Origin Private File System)

Um eine performante und persistente Speicherung der SQLDelight-Datenbank im Browser zu ermöglichen, verwenden wir das **Origin Private File System (OPFS)**. Die Nutzung von OPFS erfordert, dass die Webseite in einem "cross-origin isolated"-Kontext läuft.

### HTTP-Header

Dazu müssen die folgenden HTTP-Header vom Webserver ausgeliefert werden:

*   `Cross-Origin-Opener-Policy: same-origin`
*   `Cross-Origin-Embedder-Policy: require-corp`

### Webpack Dev Server

Für die lokale Entwicklung wird der Webpack Dev Server verwendet. Um die erforderlichen Header zu setzen, wurde die `webpack.config.d/opfs-headers.js`-Datei erstellt, die die Header zur Konfiguration des Dev Servers hinzufügt.

## Web Worker für SQLDelight

Um die UI nicht zu blockieren, werden alle Datenbank-Operationen in einem separaten **Web Worker** ausgeführt.

*   **`sqlite.worker.js`:** Ein benutzerdefinierter Worker, der die SQLDelight-Datenbank initialisiert und die Anfragen vom Haupt-Thread entgegennimmt.
*   **`WebWorkerDriver`:** Der SQLDelight-Treiber, der die Kommunikation zwischen dem Haupt-Thread und dem Worker-Thread managed.

## Build-Konfiguration

*   **`build.gradle.kts`:** Im `build.gradle.kts` des `meldestelle-portal`-Moduls wird das `wasmJs` oder `js` Target entsprechend konfiguriert, um die Web-Anwendung zu bauen.
*   **Webpack-Integration:** Die Gradle-Plugins für KMP/JS und Compose for Web kümmern sich um die Integration mit Webpack und die Erstellung des finalen JavaScript-Bundles.

## Aktueller Blocker (Januar 2026)

Derzeit ist der Build für das `wasmJs`-Target aufgrund von Compiler-Fehlern im Zusammenhang mit dem JS-Interop und fehlenden DOM-API-Referenzen blockiert. Die Behebung dieses Problems hat höchste Priorität.
