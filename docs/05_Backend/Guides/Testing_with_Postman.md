---
type: Guide
status: ACTIVE
owner: Backend Developer
tags: [testing, postman, backend, api]
---

# 🧪 Testanleitung: Ping-Service & Gateway mit Postman

Diese Anleitung beschreibt, wie die Backend-Services (Gateway, Ping-Service) und die Infrastruktur (Keycloak, DB) isoliert vom Frontend getestet werden können.

**Voraussetzungen:**
1.  **Infrastruktur läuft:** `docker compose --profile infra up -d` (Postgres, Keycloak, Redis, Consul).
2.  **Backend läuft:** `docker compose --profile backend up -d` (Gateway, Ping-Service).
3.  **Postman** ist installiert.

---

## 1. Vorbereitung: Keycloak User & Client

Damit wir testen können, brauchen wir einen User und einen Client in Keycloak, um uns ein Token zu holen.

*   **URL:** `http://localhost:8180` (oder Port aus `docker-compose logs keycloak`)
*   **Admin Login:** `kc-admin` / `kc-password`
*   **Realm:** Wähle oben links `meldestelle` aus (wurde beim Start importiert).

**Check:**
*   **User:** Gibt es einen User? (z.B. `testuser` / `password` mit Rolle `MELD_USER`)?
    *   *Falls nicht:* Lege schnell einen User an, setze Credentials (temporary: off) und weise ihm unter "Role Mapping" die Rolle `MELD_USER` zu.
*   **Client:** Gibt es einen Client? (z.B. `meldestelle-frontend` oder `postman`)?
    *   *Falls nicht:* Lege einen Client `postman` an.
    *   Access Type: `public` (oder `confidential` wenn du Client Secret nutzen willst, public reicht für Postman oft).
    *   Valid Redirect URIs: `*` (für Tests ok) oder `https://oauth.pstmn.io/v1/callback`.

---

## 2. Postman Collection einrichten

Erstelle eine neue Collection "Meldestelle Ping Test".

### A. Variablen (Environment)
Setze folgende Variablen in Postman (Environment "Local Docker"):
*   `gateway_url`: `http://localhost:8081`
*   `auth_url`: `http://localhost:8180/realms/meldestelle/protocol/openid-connect/token`
*   `client_id`: `meldestelle-frontend` (oder wie dein Client heißt)
*   `username`: `testuser` (dein User)
*   `password`: `password` (dein Passwort)

---

## 3. Test-Szenarien

Wir testen nun die verschiedenen Endpunkte und Sicherheitsstufen.

### Szenario 1: Public Endpoints (Ohne Login)
*Diese Requests müssen **ohne** Token funktionieren.*

**1.1 Simple Ping**
*   **Method:** `GET`
*   **URL:** `{{gateway_url}}/api/ping/simple`
*   **Auth:** No Auth
*   **Erwartetes Ergebnis:**
    *   Status: `200 OK`
    *   Body: `{"status": "pong", "service": "ping-service", ...}`
    *   *Bedeutung:* Gateway routet korrekt, Service ist erreichbar, DB-Schreibzugriff (Simple Ping speichert was) klappt.

**1.2 Health Check**
*   **Method:** `GET`
*   **URL:** `{{gateway_url}}/api/ping/health`
*   **Auth:** No Auth
*   **Erwartetes Ergebnis:**
    *   Status: `200 OK`
    *   Body: `{"status": "up", "healthy": true, ...}`

**1.3 Public Ping**
*   **Method:** `GET`
*   **URL:** `{{gateway_url}}/api/ping/public`
*   **Auth:** No Auth
*   **Erwartetes Ergebnis:** `200 OK`

---

### Szenario 2: Security Check (Negativ-Test)
*Wir versuchen, auf geschützte Bereiche zuzugreifen, ohne eingeloggt zu sein.*

**2.1 Secure Ping (Unauthenticated)**
*   **Method:** `GET`
*   **URL:** `{{gateway_url}}/api/ping/secure`
*   **Auth:** No Auth
*   **Erwartetes Ergebnis:**
    *   Status: `401 Unauthorized`
    *   *Bedeutung:* Gateway oder Service blockt den Request korrekt ab.

**2.2 Sync Ping (Unauthenticated)**
*   **Method:** `GET`
*   **URL:** `{{gateway_url}}/api/ping/sync`
*   **Auth:** No Auth
*   **Erwartetes Ergebnis:**
    *   Status: `401 Unauthorized` (sofern Sync auch geschützt ist).

---

### Szenario 3: Authenticated Endpoints (Mit Token)
*Jetzt holen wir uns ein Token und testen die geschützten Bereiche.*

**3.1 Token holen (Login)**
*   In Postman: Tab "Authorization" -> Type "OAuth 2.0".
*   Grant Type: `Password Credentials`
*   Access Token URL: `{{auth_url}}`
*   Client ID: `{{client_id}}`
*   Username: `{{username}}`
*   Password: `{{password}}`
*   Klick auf "Get New Access Token".
*   Klick auf "Use Token".

**3.2 Secure Ping (Authenticated)**
*   **Method:** `GET`
*   **URL:** `{{gateway_url}}/api/ping/secure`
*   **Auth:** Inherit from parent (oder OAuth 2.0 mit dem Token).
*   **Erwartetes Ergebnis:**
    *   Status: `200 OK`
    *   Body: `{"status": "secure-pong", ...}`
    *   *Bedeutung:* Token ist gültig, Signatur passt, Rolle `MELD_USER` wurde erkannt.

**3.3 Sync Ping (Authenticated)**
*   **Method:** `GET`
*   **URL:** `{{gateway_url}}/api/ping/sync?lastSyncTimestamp=0`
*   **Auth:** OAuth 2.0 (Token)
*   **Erwartetes Ergebnis:**
    *   Status: `200 OK`
    *   Body: `[ ... Liste von Events ... ]`
    *   *Bedeutung:* Delta-Sync funktioniert.

---

### Szenario 4: Resilience (Circuit Breaker)

**4.1 Enhanced Ping (Normal)**
*   **Method:** `GET`
*   **URL:** `{{gateway_url}}/api/ping/enhanced`
*   **Erwartetes Ergebnis:** `200 OK`

**4.2 Enhanced Ping (Simulation Failure)**
*   **Method:** `GET`
*   **URL:** `{{gateway_url}}/api/ping/enhanced?simulate=true`
*   **Wiederhole:** Sende den Request mehrmals schnell hintereinander.
*   **Erwartetes Ergebnis:**
    *   Manchmal `500 Error` (Simulation).
    *   Nach einigen Fehlern: `200 OK` aber mit `status: "fallback"` und `circuitBreakerState: "OPEN"`.
    *   *Bedeutung:* Resilience4j hat den Fehler erkannt und den Circuit Breaker geöffnet -> Fallback-Methode greift.

---

## Zusammenfassung der Checkliste

| Test | URL | Auth | Erwartet |
| :--- | :--- | :--- | :--- |
| **Connectivity** | `/api/ping/simple` | Nein | 200 OK |
| **Health** | `/api/ping/health` | Nein | 200 OK |
| **Security Block** | `/api/ping/secure` | Nein | 401 Unauthorized |
| **Auth Login** | (Keycloak Token) | - | Token erhalten |
| **Security Access** | `/api/ping/secure` | Ja (Token) | 200 OK |
| **Sync** | `/api/ping/sync` | Ja (Token) | 200 OK (Liste) |
| **Resilience** | `/api/ping/enhanced?simulate=true` | Nein | Fallback Response |
