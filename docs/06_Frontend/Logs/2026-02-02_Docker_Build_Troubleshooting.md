# 🧹 Troubleshooting Log: Frontend Docker Build & Runtime Config

**Datum:** 02.02.2026
**Status:** ⚠️ BLOCKED (Build Failure)
**Thema:** Dockerisierung des KMP Frontends (JS/IR) mit Caddy und Runtime-Konfiguration.

## 1. Zielsetzung
Stabilisierung des Frontend-Deployments via Docker Compose.
*   **Architektur:** Single Page Application (SPA) served by Caddy.
*   **Anforderung:** "Build Once, Deploy Anywhere" -> Konfiguration (API URL) muss zur Laufzeit (Runtime) injiziert werden, nicht zur Build-Zeit.
*   **Tech Stack:** Kotlin 2.3.0, Gradle 9.2.1, Compose Multiplatform 1.10.0.

## 2. Implementierte Lösung (Code-Ebene)
Die Architektur für die Runtime-Konfiguration wurde erfolgreich implementiert:

1.  **Kotlin (`Config.kt`, `main.kt`):**
    *   Die App lädt vor dem Start der UI eine `config.json` via `window.fetch`.
    *   `AppConfig` wird in Koin registriert.
2.  **Caddy (`Caddyfile`, `config.json`):**
    *   Caddy Webserver ersetzt Nginx.
    *   Nutzt das `templates` Modul, um Environment-Variablen (`API_BASE_URL`) in die `config.json` zu rendern.
3.  **Dockerfile:**
    *   Multi-Stage Build (Gradle -> Caddy).
    *   Optimiertes Caching für Gradle 9.x.

## 3. Das Problem: Gradle Build Fehler
Der Build schlägt im Docker-Container (und teilweise lokal) fehl mit:
`PluginApplicationException: Failed to apply plugin class 'org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin'.`
`The Kotlin Gradle plugin was loaded multiple times in different subprojects...`

### Ursache
In einem Multi-Modul KMP-Projekt (Shell + Core Libraries) versuchen mehrere Module, die JavaScript-Umgebung (Node.js/Browser) zu konfigurieren.
*   **Shell (`meldestelle-portal`):** Benötigt `browser()` für Webpack/Distribution.
*   **Libraries (`core/*`):** Benötigen JS-Target für Kompilierung, nutzen teilweise `npm()` Abhängigkeiten (z.B. `local-db` für SQLite).
*   **Konflikt:** Gradle 9.x und das Kotlin-Plugin geraten in einen Race-Condition-Zustand, wenn das `NodeJsRootPlugin` transitiv mehrfach initialisiert wird.

## 4. Durchgeführte Versuche

| Versuch | Änderung | Ergebnis | Analyse |
| :--- | :--- | :--- | :--- |
| **1. Basis** | `alias(libs.plugins...)` in allen Modulen. `browser {}` in allen Modulen. | ❌ FAILED | "Plugin loaded multiple times". |
| **2. Library Mode** | Entfernen von `browser {}` aus allen Core-Modulen. Nur `binaries.library()`. | ⚠️ SUCCESS (Lokal) / ❌ FAILED (Docker) | Lokal: Warnung "JS Environment Not Selected". Docker: Trotzdem Fehler, vermutlich wegen `npm()` Dependency in `local-db`. |
| **3. Explicit Browser** | Hinzufügen von minimalem `browser { testTask { enabled = false } }` in Libraries. | ❌ FAILED | Sofortiger "Plugin loaded multiple times" Fehler. |
| **4. Plugin ID** | Nutzung von `id("org.jetbrains.kotlin.multiplatform")` statt `alias`. | ❌ FAILED | "Plugin not found" (Version Resolution via Catalog schlägt fehl). |
| **5. Revert** | Zurück zu "Library Mode" (Versuch 2). | ❌ FAILED | Der Fehler bleibt hartnäckig im Docker-Build bestehen. |

## 5. Nächste Schritte (Planung)
Das Problem liegt in der Gradle-Konfiguration der JS-Targets im Monorepo.

1.  **Root-Level Node.js Konfiguration:** Das `NodeJsRootPlugin` muss zwingend **einmalig** im Root-Projekt konfiguriert werden, um den Konflikt in den Submodulen zu lösen.
2.  **Convention Plugin:** Erstellung eines `buildSrc` oder `conventions` Plugins, das die JS-Konfiguration zentralisiert (`apply(plugin = "kotlin-multiplatform")`).
3.  **Workaround:** Explizites `rootProject.plugins.apply(...)` für das NodeJs-Plugin in der Root `build.gradle.kts`.

---
*Dokumentiert durch Curator Agent.*
