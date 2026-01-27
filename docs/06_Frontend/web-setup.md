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

### Datenbank-Initialisierung (Fix 27.01.2026)

Um Probleme mit `table already exists` beim Neustart zu vermeiden, wurde die `DatabaseDriverFactory.js.kt` angepasst:
*   Prüfung der `PRAGMA user_version`.
*   Schema-Erstellung nur bei Version 0.
*   Migration bei Version < Schema-Version.
*   Workaround für fehlende `QueryResult.map` Funktion im JS-Treiber durch explizite Typisierung und Cursor-Capture.

## Authentifizierung & CORS

### Keycloak Konfiguration

Für die Web-App (`web-app` Client) gelten folgende Einstellungen:
*   **Access Type:** `public` (Kein Client Secret senden!)
*   **Direct Access Grants:** `Enabled` (für Login via Username/Passwort API)
*   **Web Origins:** `*` (oder spezifische URL) für CORS.

### Webpack Proxy

Der Webpack Dev Server leitet API-Anfragen (`/api/...`) an das API Gateway (`http://localhost:8081`) weiter.
*   **Wichtig:** `pathRewrite` wurde entfernt, damit `/api/ping` korrekt als `/api/ping` beim Gateway ankommt (und nicht als `/ping`).
*   **Base URL:** Die `PlatformConfig.js.kt` setzt die `baseUrl` für den `apiClient` auf `window.location.origin + "/api"` (z.B. `http://localhost:8080/api`), damit der Proxy greift.

## Build-Konfiguration

*   **`build.gradle.kts`:** Im `build.gradle.kts` des `meldestelle-portal`-Moduls wird das `wasmJs` oder `js` Target entsprechend konfiguriert, um die Web-Anwendung zu bauen.
*   **Webpack-Integration:** Die Gradle-Plugins für KMP/JS und Compose for Web kümmern sich um die Integration mit Webpack und die Erstellung des finalen JavaScript-Bundles.

## Aktueller Status (27.01.2026)

*   **Login:** Funktioniert (CORS & Client Config gefixt).
*   **Ping-Services:** Funktionieren (Routing & Auth gefixt).
*   **Sync:** `404 Not Found` Problem identifiziert (URL-Pfad-Auflösung). Fix implementiert (relativer Pfad im `LoginViewModel`), Verifikation ausstehend.
