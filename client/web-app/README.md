# Meldestelle Web-App

Eine moderne Web-Anwendung basierend auf **Compose for Web** (Compose Multiplatform) für das Meldestelle-Projekt.

## 📋 Überblick

Diese Web-Anwendung implementiert das Frontend für das Meldestelle-System unter Verwendung von Compose for Web. Sie folgt dem Architekturprinzip der maximalen Code-Wiederverwendung durch die Nutzung des `commonMain`-Source-Sets von Kotlin Multiplatform.

### Technologie-Stack

- **Frontend Framework**: Compose for Web (Compose Multiplatform 1.8.2)
- **Programmiersprache**: Kotlin/JS
- **Build-System**: Gradle 8.10
- **HTTP-Client**: Ktor Client
- **UI-Komponenten**: Compose Material 3 (aus commonMain)
- **Bundler**: Webpack (über Kotlin/JS Plugin)
- **Container**: Nginx (Production)

## 🏗️ Architektur

```
client/web-app/
├── src/
│   └── jsMain/
│       ├── kotlin/at/mocode/client/web/
│       │   └── main.kt                 # Entry Point
│       └── resources/
│           └── index.html              # HTML Template
├── build.gradle.kts                    # Build Konfiguration
└── build/                              # Build Artefakte
    └── dist/js/productionExecutable/   # Produktionsversion
```

### Design Prinzipien

1. **Code-Wiederverwendung**: Maximale Nutzung des `client:common-ui` Moduls
2. **Compose for Web**: Deklarative UI mit `@Composable` Funktionen
3. **State Management**: Zustandsverwaltung über ViewModels im `commonMain`
4. **Plattform-Trennung**: UI-Code in `jsMain`, Logik in `commonMain`

## 🚀 Schnellstart

### Voraussetzungen

- Java 21+
- Docker und Docker Compose
- Node.js 18+ (wird automatisch im Container installiert)

### Entwicklung starten

#### Mit Docker (Empfohlen)

```bash
# Web-App im Entwicklungsmodus starten
docker compose -f docker-compose.yml -f docker-compose.clients.yml up -d web-app

# Anwendung ist verfügbar unter:
# http://localhost:3000
```

#### Lokale Entwicklung

```bash
# Abhängigkeiten installieren und Entwicklungsserver starten
./gradlew :client:web-app:jsBrowserDevelopmentRun

# Anwendung läuft auf http://localhost:8080
```

### Produktionsbuild

```bash
# Optimierte JavaScript-Bundles erstellen
./gradlew :client:web-app:jsBrowserDistribution

# Artefakte befinden sich in:
# client/web-app/build/dist/js/productionExecutable/
```

## 🔧 Entwicklung

### Projekt-Struktur

```kotlin
// main.kt - Entry Point
fun main() {
    renderComposable(rootElementId = "root") {
        WebApp()
    }
}

@Composable
fun WebApp() {
    // Verwendet die gemeinsame App-Komponente aus commonMain
    App()
}
```

### Hot-Reload

Die Entwicklungsumgebung unterstützt Hot-Reload:
- Änderungen an Kotlin-Code werden automatisch neu kompiliert
- Browser wird automatisch aktualisiert
- Schnelle Entwicklungszyklen durch Webpack Dev Server

### Build-Konfiguration

Die `build.gradle.kts` konfiguriert:

```kotlin
kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "web-app.js"
                // Webpack optimization directory
                configDirectory = project.projectDir.resolve("webpack.config.d")
            }
            webpackTask {
                mainOutputFileName = "web-app.js"
            }
        }
        binaries.executable()
    }
}
```

### Webpack-Optimierungen

Das Projekt verwendet erweiterte Webpack-Optimierungen für bessere Performance:

#### Code Splitting
- **Separate Chunks**: Bundle wird in ~60 kleinere, cacheable Dateien aufgeteilt
- **Vendor Chunks**: Große Libraries (Kotlin stdlib, Compose runtime, Coroutines) werden separat geladen
- **Lazy Loading**: Verbessertes Caching durch getrennte Vendor- und App-Code-Chunks

#### Bundle-Größenoptimierung
- **Tree Shaking**: Entfernt ungenutzten Code
- **Minification**: Aggressive Komprimierung im Produktionsbuild
- **Scope Hoisting**: Optimiert JavaScript-Execution
- **Performance Budget**: Warnt bei zu großen Bundles (500KB pro Asset, 800KB Gesamt)

#### Generierte Chunks (Beispiel)
```
web-app-runtime.js          1.67 KiB  (Runtime)
web-app.js                  482 bytes (Main App)
web-app-compose-runtime-*.js 274 KiB   (Compose Framework)
web-app-kotlin-stdlib.js    165 KiB   (Kotlin Standard Library)
web-app-coroutines.js       119 KiB   (Kotlinx Coroutines)
web-app-vendors-*.js        1.17 MiB  (Weitere Dependencies)
```
```

**Abhängigkeiten:**
- `compose.web.core` - Compose for Web Framework
- `compose.runtime` - Compose Runtime
- `project(":client:common-ui")` - Gemeinsame UI-Komponenten
- `kotlinx-coroutines-core-js` - Coroutines für Web

## 🌐 Deployment

### Docker Container

Die Anwendung wird als Docker-Container deployed:

```dockerfile
# Multi-stage Build
FROM gradle:8.10-jdk21 AS builder
# ... Build Phase

FROM nginx:1.25-alpine AS production
# ... Production Phase
```

**Features:**
- Multi-stage Build für optimale Image-Größe
- Nginx als Static File Server
- Health Checks
- Security Headers
- Gzip Kompression

### Konfiguration

Umgebungsvariablen:
- `NODE_ENV`: Entwicklungs-/Produktionsmodus
- `API_BASE_URL`: Backend API URL
- `APP_TITLE`: Anwendungstitel
- `APP_VERSION`: Versionsnummer

### Health Checks

```bash
# Container Health Check
curl --fail http://localhost:3000/health

# Antwort: {"status":"ok","service":"web-app"}
```

## 🔗 Integration

### Backend-Kommunikation

Die Web-App kommuniziert mit dem Backend über:
- **API Gateway**: `http://api-gateway:8081`
- **REST APIs**: Über Ktor Client
- **WebSocket**: Für Realtime-Updates (geplant)

### Gemeinsame Komponenten

Nutzt Komponenten aus `client:common-ui`:
- **ViewModels**: `PingViewModel` für Backend-Tests
- **UI-Komponenten**: `App`, `PingScreen`
- **Services**: `PingService` für HTTP-Aufrufe
- **Models**: Datenklassen und UI-States

### Beispiel Integration

```kotlin
@Composable
fun WebApp() {
    // Verwendet die gemeinsame App-Komponente
    // Diese enthält Material 3 Komponenten und ViewModels
    App(baseUrl = "http://localhost:8081")
}
```

## 📊 Build-Artefakte

Nach dem Build werden folgende Dateien generiert:

```
build/dist/js/productionExecutable/
├── web-app.js              # Hauptanwendung (minifiziert)
├── web-app.js.map          # Source Maps
├── 731.js                  # Code Splitting Chunk
├── index.html              # HTML Template
├── skiko.wasm              # Compose Runtime (WebAssembly)
└── skiko.js                # Compose JavaScript Runtime
```

## 🧪 Testing

### Entwicklungstests

```bash
# Backend-Konnektivität testen
# Öffne http://localhost:3000
# Klicke "Ping Backend" Button
```

### Build-Validierung

```bash
# Build ohne Ausführung testen
./gradlew :client:web-app:jsBrowserDevelopmentRun --dry-run

# Produktionsbuild testen
./gradlew :client:web-app:jsBrowserDistribution
```

## 📚 Weiterführende Dokumentation

- [Web-App Guideline](../../.junie/guidelines/web-app-guideline.md) - Architektur-Richtlinien
- [Docker README](../../README-DOCKER.md) - Container-Dokumentation
- [Compose for Web Docs](https://github.com/JetBrains/compose-multiplatform) - Offizielle Dokumentation

## 🔍 Troubleshooting

### Häufige Probleme

**Problem**: `Cannot connect to backend`
```bash
# Lösung: Backend Services starten
docker-compose -f docker-compose.yml -f docker-compose.services.yml up -d
```

**Problem**: `Module build failed`
```bash
# Lösung: Clean Build
./gradlew :client:web-app:clean :client:web-app:jsBrowserDevelopmentRun
```

**Problem**: `Port 3000 already in use`
```bash
# Lösung: Port in docker-compose.clients.yml ändern
ports:
  - "3001:3000"  # Externer Port ändern
```

### Logs und Debugging

```bash
# Container Logs anzeigen
docker logs meldestelle-web-app

# Build Logs mit Details
./gradlew :client:web-app:jsBrowserDevelopmentRun --info --stacktrace
```

---

**Letzte Aktualisierung**: 2025-09-10
**Version**: 1.0.0
