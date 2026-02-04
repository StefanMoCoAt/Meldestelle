---
type: Journal
status: ACTIVE
owner: DevOps Engineer
last_update: 2026-02-04
---

# Session Log: Docker Hybrid Build & Build Optimization

**Datum:** 04.02.2026
**Teilnehmer:** User, DevOps Engineer
**Fokus:** Stabilisierung des Frontend-Builds im Docker-Container und Optimierung der Build-Performance.

## 🎯 Ziel
Den Docker-Build für den `web-app` Service reparieren, der aufgrund von Gradle/Kotlin-Plugin-Konflikten (`IsolatedKotlinClasspathClassCastException`) fehlschlug, und die Build-Zeiten optimieren.

## 📝 Protokoll

### 1. Problem: Gradle Plugin Konflikte im Docker
*   **Symptom:** `IsolatedKotlinClasspathClassCastException` beim Build im Docker-Container.
*   **Ursache:** Konflikt zwischen Gradle 9.x, Kotlin 2.3.0 und dem `NodeJsRootPlugin`, wenn das Root-Projekt versucht, die Node.js-Umgebung zu initialisieren, während Subprojekte dies ebenfalls tun.
*   **Lösung:**
    1.  **Root `build.gradle.kts` bereinigt:** KMP-Plugin nur noch mit `apply false` eingebunden. `kotlin { ... }` Block im Root entfernt. Root ist nun reiner Konfigurations-Container.
    2.  **Hybrid-Build Strategie:** Statt im Docker-Container zu bauen (was instabil war), bauen wir das Frontend lokal (`./gradlew ...jsBrowserDistribution`) und kopieren die fertigen Artefakte in den Container.

### 2. Problem: `.dockerignore` blockiert Artefakte
*   **Symptom:** `COPY failed: ... not found`.
*   **Ursache:** `.dockerignore` schloss `**/build/` pauschal aus.
*   **Lösung:** Explizite Ausnahme für den Pfad `!frontend/shells/meldestelle-portal/build/dist/js/productionExecutable/` (inklusive aller Eltern-Ordner) hinzugefügt.

### 3. Problem: Build-Performance (Webpack Timeout)
*   **Symptom:** Webpack-Task lief >12 Minuten oder hing.
*   **Ursache:** Zu wenig Speicher für den Gradle Daemon und Node.js Prozess bei großen Builds.
*   **Lösung:** `gradle.properties` angepasst:
    *   `org.gradle.jvmargs`: 4GB (vorher 3GB)
    *   `kotlin.daemon.jvmargs`: 4GB (vorher 3GB)

### 4. Problem: Caddy Config & Runtime
*   **Symptom:** 500er Fehler beim Abruf von `/config.json`.
*   **Ursache:** Syntax-Fehler im Caddy-Template (Escaping von Anführungszeichen).
*   **Lösung:** Template auf einfache Syntax zurückgesetzt: `{{env "API_BASE_URL" ...}}`.
*   **Playbook Update:** DevOps Engineer übernimmt explizit die Verantwortung für Caddy/Webserver-Konfiguration.

## ✅ Ergebnisse
1.  **Web-App läuft:** Der Container `meldestelle-web-app` startet erfolgreich und ist unter `http://localhost:4000` erreichbar.
2.  **Build-Prozess:** Stabilisiert durch Hybrid-Ansatz (Lokal bauen -> Docker kopieren).
3.  **Infrastruktur:** `build.gradle.kts` (Root) ist sauberer und performanter.

## ⏭️ Nächste Schritte (Open Points)
*   **Ping-Service Erreichbarkeit:** Das Frontend kann den Ping-Service (`http://localhost:8081`) noch nicht erreichen (CORS oder Netzwerk-Thema). -> Übergabe an Backend/Frontend.
*   **CI/CD:** Für die CI-Pipeline muss der Hybrid-Build berücksichtigt werden (Build-Step vor Docker-Build).

## 📂 Betroffene Dateien
*   `build.gradle.kts` (Root)
*   `gradle.properties`
*   `config/docker/caddy/web-app/Dockerfile`
*   `.dockerignore`
*   `config/docker/caddy/web-app/config.json`
*   `docs/04_Agents/Playbooks/DevOpsEngineer.md`
