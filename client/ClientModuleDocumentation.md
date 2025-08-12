# 🖥️ Client-Modul

Dieses Modul liefert die **grafische Benutzeroberfläche** für das Projekt
– einmal als **Desktop-App (JVM)** und einmal als **Web-App (JavaScript)**.
Dank **Kotlin Multiplatform + Compose Multiplatform** teilt sich beides eine
gemeinsame Code-Basis.

---

## 1. Voraussetzungen

| Tool            | Empfohlene Version | Bemerkung                                       |
|-----------------|--------------------|-------------------------------------------------|
| JDK             | 21 (Temurin)       | Für Desktop‐Build und Gradle                    |
| Node.js & npm   | ≥ 20               | Nur für den JS-/Browser-Build                   |
| Gradle Wrapper  | Wird mitgeliefert  | `./gradlew` ruft immer die projektinterne Version auf |

---

## 2. Build & Run

### 2.1 Desktop-App starten

```bash
# Im Projekt-Root
./gradlew :client:runJvm
```
Die App startet als eigenständiges JVM-Fenster auf Ihrem Desktop.

### 2.2 Web-App starten

```bash
./gradlew :client:jsBrowserDevelopmentRun
```

1. Gradle kompiliert das JS-Artefakt.
2. Anschließend öffnet sich ein lokaler Dev-Server (Standard: <http://localhost:3000>).

Hot-Reload wird vom Compose-/Ktor-Dev-Server automatisch gehandhabt.

---

## 3. Packaging

| Ziel            | Task (Gradle)                          | Ergebnis                               |
|-----------------|----------------------------------------|----------------------------------------|
| **Desktop**     | `:client:packageJvm`                   | Self-contained Verzeichnis mit Start-Skript |
| **Web (prod)**  | `:client:jsBrowserProductionWebpack`   | Optimiertes Bundle in `build/dist`     |

---

## 4. Architekturüberblick

```client
commonMain ├─ UI: Compose Runtime/Foundation/Material³ ├─ Netzwerk: Ktor Client (+ JSON Serialisierung) └─ Geschäftslogik & Models
jvmMain └─ Ktor CIO Engine (Desktop)
jsMain └─ Ktor JS Engine (Browser)
```

Gemeinsame Logik (UI-State, Repository-Klassen etc.) lebt in
`commonMain`. Plattform-spezifisch ist im Wesentlichen nur der
gewählte **Ktor-Engine**.

---

## 5. API-Kommunikation

Alle Aufrufe an das Backend erfolgen **asynchron** via `Ktor Client`.
Das JSON-Serialisieren übernimmt `kotlinx.serialization`.

Beispiel (vereinfacht):

kotlin val client = HttpClient(CIO) { install(ContentNegotiation) { json() } }
suspend fun ping(): PingResponse = client.get("$BASE_URL/ping").body()

---

## 6. Konfiguration

| Schlüssel                    | Zweck                       | Standardwert                |
|------------------------------|-----------------------------|-----------------------------|
| `BASE_URL` (env / props)     | Root-URL des Gateways       | `http://localhost:8080`     |
| `LOG_LEVEL` (env / props)    | Logging (DEBUG/INFO/…)      | `INFO`                      |

Konfiguration kann via JVM-Args (`-D`) oder Umgebungsvariablen
überschrieben werden.

---

## 7. Tests

Noch keine UI-Tests enthalten.
Empfohlen: **Compose UI Testing** (Desktop) und **Kotlin/Wrappers
Testing** (JS).

---

## 8. Häufige Gradle-Tasks

| Zweck                              | Task                                  |
|------------------------------------|---------------------------------------|
| Desktop-App starten (Dev)          | `./gradlew :client:runJvm`            |
| Web-App starten (Dev)              | `./gradlew :client:jsBrowserDevelopmentRun` |
| Desktop-Artefakt packen            | `./gradlew :client:packageJvm`        |
| Web-Artefakt für Prod erstellen    | `./gradlew :client:jsBrowserProductionWebpack` |
| Alle Tests ausführen               | `./gradlew :client:test`              |
| Abhängigkeits-Updates anzeigen     | `./gradlew :client:dependencyUpdates` |

---

## 9. Troubleshooting

| Problem                         | Lösungsvorschlag |
|---------------------------------|------------------|
| Weißer Bildschirm im Browser    | Dev-Konsole öffnen (`F12`) → Netzwerk-Fehler? CORS-Header prüfen |
| `java.lang.UnsatisfiedLinkError`| Prüfen, ob das korrekte JDK (21) verwendet wird |
| Gradle-Timeout beim NPM-Install | Proxy-/Firewall-Settings überprüfen; ggf. `--network=host` |

---

## 10. Lizenz

`TODO: <Lizenzname>` – bitte anpassen.
