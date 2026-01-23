---
type: Journal
status: COMPLETED
owner: Curator
date: 2026-01-23
participants:
  - DevOps Engineer
  - Frontend Expert
  - Curator
---

# Session Log: Auth & Build Fixes

**Datum:** 23. Jänner 2026
**Ziel:** Behebung von Build-Fehlern im Docker-Stack und Stabilisierung des Auth-Flows (Login/Logout) im Frontend.

## Durchgeführte Arbeiten

### 1. Docker Build Fix (DevOps)
*   **Problem:** Der Build von `api-gateway` und `ping-service` schlug fehl, weil Gradle das Modul `:frontend:core:auth` konfigurieren wollte, dessen Pfad im Docker-Container fehlte.
*   **Lösung:** Die `Dockerfile`s beider Services wurden aktualisiert, um die neue Frontend-Struktur (`frontend/core/auth` statt `frontend/features/auth-feature`) widerzuspiegeln.

### 2. Frontend Auth Stabilisierung (Frontend Expert)
*   **Problem 1 (Login 401):** Der `AuthApiClient` nutzte den globalen `apiClient`, der für das Gateway konfiguriert war (`DefaultRequest` mit Base URL). Dies führte zu Konflikten bei Requests gegen Keycloak.
    *   **Lösung:** Einführung eines `baseHttpClient` (named) im `NetworkModule`, der "nackt" ist. Der `AuthApiClient` nutzt nun diesen Client.
*   **Problem 2 (Logout ineffektiv):** Das Ktor `Auth` Plugin cachete den Token intern, sodass ein Logout (`AuthTokenManager.clearToken()`) erst beim Neustart wirksam wurde.
    *   **Lösung:** Entfernung des `Auth` Plugins aus dem `apiClient`. Stattdessen wird der `Authorization` Header nun via `install(DefaultRequest)` dynamisch bei jedem Request aus dem `TokenProvider` gelesen.

### 3. Keycloak Konfiguration
*   **Problem:** Der Admin-User musste beim ersten Login das Passwort ändern (`temporary: true`), was den API-Login blockierte.
*   **Lösung:** In `meldestelle-realm.json` wurde `temporary: false` gesetzt.

## Ergebnisse
*   Der Docker-Build läuft fehlerfrei durch.
*   Login funktioniert stabil.
*   Logout funktioniert sofort (nachfolgende Requests liefern korrekt 401).
*   Die Architektur im Frontend ist durch die Trennung der HttpClients sauberer.

**Status:** 🟢 **System Fully Operational**
