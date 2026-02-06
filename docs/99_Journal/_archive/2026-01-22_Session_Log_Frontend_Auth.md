---
type: Journal
status: COMPLETED
owner: Frontend Expert
date: 2026-01-22
participants:
  - Frontend Expert
  - User
---

# Session Log: Frontend Auth & Refactoring

**Datum:** 22. Jänner 2026
**Ziel:** Implementierung des Login-Flows im Frontend und Refactoring der Architektur.

## Durchgeführte Arbeiten

### 1. Architektur-Refactoring
*   **Auth-Feature:** Das Modul `frontend/features/auth-feature` wurde nach `frontend/core/auth` verschoben, da es sich um eine Basisfunktionalität (Infrastruktur) handelt.
*   **Design-System:** Das Package `at.mocode.clients.shared.commonui` wurde zu `at.mocode.frontend.core.designsystem` refactored.
*   **Cleanup:** Alte, redundante Dateien und Module wurden bereinigt.

### 2. Authentifizierung (Login)
*   **Client:** Umstellung auf `postman-client` (Confidential Client) für den Desktop-Login, da `web-app` (Public Client) keine Direct Access Grants (Password Flow) unterstützte.
*   **Secret:** Das Client Secret (`postman-secret-123`) wurde temporär in `AppConstants` hinterlegt (DEV-Only).
*   **AuthApiClient:** Implementierung von Basic Auth Header für den Token-Request.
*   **LoginViewModel:** Fix des State-Managements beim Logout (automatischer Reset von `isAuthenticated`).

### 3. UI & Navigation
*   **MainApp:** Einführung von `AppScaffold` und Scroll-Support für Landing/Welcome Screens.
*   **Navigation:** Hinzufügen von "Zurück"-Buttons in `LoginScreen` und `PingScreen`.
*   **Usability:** Entfernung verwirrender Browser-Login-Buttons.

### 4. Backend-Integration
*   **Secure Ping:** Erfolgreich getestet (200 OK mit Token).
*   **Sync:** Erfolgreich getestet (200 OK mit Token). URL-Fix (`/api/pings/sync` -> `/api/ping/sync`).

## Ergebnisse
*   Die Desktop-App ist nun voll funktionsfähig: Login, Logout, Secure API Calls und Sync funktionieren.
*   Die Code-Struktur ist sauberer und folgt der Trennung zwischen Core (Infra) und Features (Domain).

## Offene Punkte
*   **Browser-Login:** PKCE Flow für Web-Target muss noch sauber implementiert werden.
*   **User-Info:** Das Profil zeigt noch "unbekannt", da der Username nicht korrekt aus dem Token geparst wird.
*   **Secret Management:** Das Client Secret darf nicht im Code bleiben (für Prod).

---
**Status:** ✅ Erfolgreich abgeschlossen.
