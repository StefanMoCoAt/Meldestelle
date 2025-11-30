# Meldestelle Clients – Architekturübersicht (aktualisiert)

## Ziele
- Öffentliche Willkommensseite mit Links zu Ping-Service, Keycloak Login/Registrierung
- Einfache Auth-Status-Seite („Du bist als … angemeldet“)
- Bereinigung der Legacy-UI (altes Präsentations-Layer entfernt)
- Bereitstellung als Docker-Container: `web-app` (Kotlin/JS) und `desktop-app` (VNC/noVNC)

## Module und Struktur
- `clients/app`: Einstieg der Anwendung (Compose Multiplatform)
  - `MainApp.kt`: Start-Routing, Composables `WelcomeScreen`, `LoginScreen`, `AuthStatusScreen`
  - Verwendet: `ping-feature` (PingScreen), `auth-feature` (Login via `AuthApiClient`)
- `clients/shared`: Gemeinsame Domain/Data/DI + `AppConfig`
  - `AppConfig`: Basis-URLs und Keycloak-Client-Konfiguration
- `clients/shared/common-ui`: generische UI-Bausteine (Legacy-Teile neutralisiert)
  - `layout/MainLayout.kt`, `components/NotificationCard.kt`, `screens/DashboardScreen.kt` → bewusst geleert
- `clients/auth-feature`: Login-API gegen Keycloak (Password Grant)
- `clients/ping-feature`: Ping-Screen und ViewModel, greift auf Ping-Service zu

## Navigation (vereinfacht)
- Start: `AppScreen.Home` → `WelcomeScreen`
- `AppScreen.Ping` → `PingScreen`
- `AppScreen.Login` → `LoginScreen`
- `AppScreen.Profile` → `AuthStatusScreen`

## Keycloak-Konfiguration
Quelle: `docker/core/keycloak/meldestelle-realm.json` und `docs/reference/ports-and-urls.md`
- Realm: `meldestelle`
- Öffentlicher Client: `web-app` (PKCE; für Browser)
- Keycloak URL (lokal): `http://localhost:8180`
- AppConfig nutzt: `KEYCLOAK_URL=http://localhost:8180`, `KEYCLOAK_REALM=meldestelle`, `KEYCLOAK_CLIENT_ID=web-app`

### Authentifizierungs-Flow (PKCE)
- Flow: Authorization Code Flow mit PKCE (S256)
- Redirect-URI (lokal): `http://localhost:4000/` (Root, Query-Parameter werden vom Client ausgewertet)
- Ablauf:
  1. Button „Login“ → PKCE-Start (Code Verifier/Challenge werden generiert) und Redirect zu Keycloak `/auth`
  2. Keycloak leitet zurück auf `http://localhost:4000/?code=...&state=...`
  3. Client tauscht `code` + `code_verifier` am `/token`-Endpoint gegen Tokens
  4. `AuthTokenManager` speichert Access-Token (in-memory), UI zeigt Status „Du bist als … angemeldet“

Hinweis: Password-Grant wird nicht mehr genutzt; für Desktop (JVM) bleibt bei Bedarf der lokale Login-Screen als Fallback vorhanden.

## Ports und URLs (lokal)
Quelle: `docs/reference/ports-and-urls.md`
- API Gateway: `http://localhost:8081`
- Keycloak: `http://localhost:8180`
- Ping Service: `http://localhost:8082`
- Web App: `http://localhost:4000`
- Desktop App: VNC `5901`, noVNC `http://localhost:6080`

## Docker
### Web-App (Kotlin/JS, kein WASM)
- Dockerfile: `dockerfiles/clients/web-app/Dockerfile`
- Build: Gradle `:clients:app:jsBrowserDistribution` → statische Dateien via Nginx
- Compose:
  - Hardcoded: Service `web-app` mit Port `4000:4000` in `compose.hardcoded.yaml`
  - Variablen: Service `web-app` mit `${WEB_APP_PORT}` in `compose.yaml` (Wert in `.env`)

Downloads (Desktop-Installer, Platzhalter):
- Verzeichnis: `dockerfiles/clients/web-app/downloads/` → wird nach `/usr/share/nginx/html/downloads/` kopiert
- URL: `http://localhost:4000/downloads/`
- Alternativ: per Compose ein Host-Verzeichnis auf `/usr/share/nginx/html/downloads` mounten

### Desktop-App (VNC/noVNC)
- Dockerfile: `dockerfiles/clients/desktop-app/Dockerfile`
- Build: Gradle `:clients:app:createDistributable` → Desktop Runtime
- Runtime: Ubuntu + `xvfb` + `x11vnc` + `noVNC` + `supervisord`
- Compose:
  - Hardcoded: Service `desktop-app` 5901/6080 in `compose.hardcoded.yaml`
  - Variablen: Service `desktop-app` mit `${DESKTOP_APP_VNC_PORT}` und `${DESKTOP_APP_NOVNC_PORT}` in `compose.yaml` (Werte in `.env`)

## Bereinigung (Altlasten)
- Entfernt/neutralisiert: Altes Präsentations-Layer (abhängig von `presentation.state`/`actions`)
  - `clients/shared/common-ui/components/NotificationCard.kt` → geleert
  - `clients/shared/common-ui/layout/MainLayout.kt` → geleert
  - `clients/shared/common-ui/screens/DashboardScreen.kt` → geleert

## Konfigurationsquelle
- Einheitliche Werte/Ports: `docker/versions.toml` (Clients: `web-app=4000`, `desktop-app-vnc=5901`, `desktop-app-novnc=6080`)
- Compose-Variablen: `.env` und `.env.template`

## Nächste Schritte
- Optional: Umstellung Login auf Authorization Code Flow (PKCE) für Browser
- Optional: Willkommensseite visuell ausbauen (Branding)
- Optional: Bereitstellung der Desktop-Installer über `web-app` Download-Link
