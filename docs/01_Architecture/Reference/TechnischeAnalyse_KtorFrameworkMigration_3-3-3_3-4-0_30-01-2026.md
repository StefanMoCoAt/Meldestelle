# Technische Analyse: Ktor Framework Migration (3.3.3 -> 3.4.0)

### Einleitung und Management-Zusammenfassung

Dieser Bericht beleuchtet die technischen Implikationen des Updates von Ktor 3.3.3 (November 2025) auf
Ktor 3.4.0 (Januar 2026). Während Version 3.3.3 primär ein Stabilisierungs-Release war, führt Version 3.4.0 signifikante
Architekturänderungen ein, insbesondere in den Bereichen **OpenAPI-Generierung,
Ressourcenmanagement (Structured Concurrency) und Multiplatform-Build-Targets.**

Für dein Projekt, das sowohl `ktor-client` (Frontend) als auch `ktor-server` (Backend) nutzt, ist dieses Update
kritisch.
Es erfordert manuelle Eingriffe in die Gradle-Konfiguration (insbesondere für JS/Wasm) und das Hinzufügen neuer
Abhängigkeiten für OpenAPI, um Laufzeitfehler zu vermeiden.

---

## 1. Frontend: Änderungen im Ktor Client

Deine Konfiguration umfasst `ktor-client-core`, `auth`, `logging`, sowie die Engines `cio` (JVM) und `js` (Web).
Hier sind die wichtigsten Punkte:

### 1.1 Kritische Änderung: Umbenennung des JS-Targets (`ktor-client-js`)

Die wohl wichtigste Änderung für dein Build-Skript betrifft das Kotlin Multiplatform Target für
JavaScript und WebAssembly.

- **Vorher (3.3.3):** Das geteilte Target hieß `jsAndWasmShared`.

- **Neu (3.4.0):** Das Target wurde in `web` umbenannt.

**Handlungsbedarf:** Du musst deine `build.gradle.kts` anpassen. Wenn du `sourceSets` definiert hast, die
auf `jsAndWasmShared` verweisen (z.B. `val jsAndWasmSharedMain by getting`), musst du diese Referenzen auf `webMain`
ändern.
Ohne diese Änderung wird der Build fehlschlagen.

### 1.2 Authentifizierung & Token-Cache (`ktor-client-auth`)

Das Verhalten des `BearerAuthProvider` wurde flexibler gestaltet.

- **Cache-Kontrolle:** In Version 3.3.3 wurden Tokens aggressiv gecacht. Version 3.4.0 führt die Methode `.clearToken()`
  ein sowie die Konfigurationsoption `cacheTokens = false`. Dies ist besonders wichtig für Single-Page-Applications (
  SPAs), bei denen ein `User-Logout` sofort wirksam sein muss, ohne den Client neu zu starten.

- **Sicherheit:** Die Erkennung von "maskierten" Bearer-Schemata wurde verbessert, um Sicherheitslücken bei der
  Header-Validierung zu schließen.

### 1.3 Logging Fix für Multipart (`ktor-client-logging`)

Ein kritischer Bug wurde behoben: In 3.3.3 konnte das Logging des Request-Bodys bei `multipart/form-data` (z.B.
Datei-Uploads) dazu führen, dass der Thread blockierte ("Hanging").

- **Vorteil:** Du kannst nun sicher `LogLevel.ALL` oder `LogLevel.BODY` auch für Upload-Endpunkte aktivieren, ohne
  Riskio eines
  Deadlocks.

### 1.4 Konsistenz der CIO Engine (`ktor-client-cio`)

Es gab Unterschiede zwischen der `CIO-Engine` (die du für JVM nutzt) und nativen Engines beim Parsen von URLs ohne
Schema
oder mit speziellen Zeichen. Diese Inkonsistenzen wurden behoben, was das Verhalten zwischen deinen JVM- und
JS-Clients vereinheitlicht.

---

## 2. Backend: Änderungen im Ktor Server

Deine Server-Konfiguration (`netty`, `auth-jwt`, `cors`, `openapi`, `serialization`) ist von umfangreichen Neuerungen
betroffen.

### 2.1 OpenAPI: Von Build-Time zu Runtime (Breaking Change!)

Dies ist die größte architektonische Änderung.
Die Module `ktor-server-openapi` und `ktor-server-swagger` generieren die API-Dokumentation nun dynamisch zur Laufzeit,
statt statische Dateien beim Build zu erzeugen.

- **Neues Feature:** Du kannst Routen nun direkt im Code beschreiben

```kotlin
 get("/users") { ... }.describe { summary = "Getusers" response < List >(HttpStatusCode.OK) }
```

Dadurch sind Code und Dokumentation immer synchron.

- **⚠️ WICHTIG (Breaking Change):** Aufgrund eines Fehlers in der Transitivität der Abhängigkeiten in 3.4.0 musst du
  eine neue Abhängigkeit manuell hinzufügen, wenn du Swagger/OpenAPI nutzt:

```kotlin
implementation("io.ktor:ktor-server-routing-openapi:3.4.0")
```

Tust du das nicht, wird deine Anwendung beim Start mit einer `NoClassDefFoundError` abstürzen, sobald auf Swagger
zugegriffen wird.

### 2.2 Structured Concurrency: HttpRequestLifecycle
Ktor 3.4.0 führt das Plugin HttpRequestLifecycle ein.

Problem in 3.3.3: Wenn ein Client die Verbindung abbrach (z.B. Browser-Tab geschlossen), lief die Verarbeitung auf dem
Server oft weiter (Datenbankabfragen, Berechnungen), was Ressourcen verschwendete.

Lösung in 3.4.0: Mit diesem Plugin wird der Abbruch der TCP-Verbindung an den Kotlin-Coroutine-Scope weitergeleitet. Die
Verarbeitung wird sofort abgebrochen (cancelled).

Kotlin
install(HttpRequestLifecycle)
Dies ist für deine Netty-Engine voll unterstützt.

2.3 Authentifizierung & Sicherheit (ktor-server-auth-jwt, cors)
JWT Fail-Early: Wenn die JWT-Konfiguration fehlerhaft ist (z.B. fehlender Verifier), bricht der Server nun sofort beim
Start ab ("Fail-Fast"). In 3.3.3 trat der Fehler erst beim ersten Request auf, was das Debuggen in Staging/Prod
erschwerte.

CORS Fix: Ein Bug wurde behoben, bei dem OPTIONS Preflight-Requests mit 405 Method Not Allowed abgelehnt wurden, wenn
das CORS-Plugin innerhalb einer spezifischen route (statt global) installiert war.

2.4 Observability (call-logging, status-pages)
MDC & Trace IDs: Ein Fehler wurde korrigiert, bei dem MDC-Kontextinformationen (wie Trace-IDs) verloren gingen, wenn
innerhalb einer Route eine Exception geworfen wurde. Deine Logs sollten nun auch bei Fehlern (Stacktraces) die korrekten
Request-IDs enthalten.

StatusPages Header: Error-Handler in StatusPages haben nun Zugriff auf die Header des OutgoingContent, was wichtig ist,
um korrekte Authentifizierungs-Header (z.B. WWW-Authenticate) auch im Fehlerfall zu senden.

3. Checkliste für die Migration
   Hier ist ein konkreter Fahrplan für das Upgrade deiner Konfiguration:

Versionen anheben: Setze die ktor Version in deinem Version-Catalog oder gradle.properties auf 3.4.0.

JS-Target anpassen: Suche im build.gradle.kts (Frontend) nach jsAndWasmShared und ersetze es durch web. Benenne
entsprechende Source-Sets um (z.B. jsAndWasmSharedMain -> webMain).

OpenAPI fixen: Füge io.ktor:ktor-server-routing-openapi:3.4.0 explizit zu deinen Backend-Dependencies hinzu.

Lifecycle Plugin aktivieren: Installiere das HttpRequestLifecycle Plugin in deiner Server-Initialisierung für besseres
Ressourcenmanagement.

Zeit-Bibliothek prüfen: Falls du dich darauf verlassen hast, dass ktor-server-default-headers automatisch
kotlinx-datetime mitbringt: Dies wurde entfernt. Füge kotlinx-datetime ggf. explizit hinzu.

Fazit
Das Update auf Ktor 3.4.0 lohnt sich vor allem wegen der Runtime-OpenAPI-Generierung und dem besseren
Ressourcenmanagement durch das Lifecycle-Plugin. Die Hürden sind primär die Umbenennung des JS-Targets und die fehlende
transitive Abhängigkeit bei OpenAPI, die aber leicht manuell behoben werden kann.
