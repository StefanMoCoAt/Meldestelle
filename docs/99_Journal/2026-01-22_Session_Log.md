---
type: Journal
status: COMPLETED
owner: Curator
date: 2026-01-22
participants:
  - DevOps & Infrastructure Engineer
  - Owner (Stefan)
---

# Session Log: 22. Jänner 2026

## Zielsetzung
Analyse der Infrastruktur, Behebung von Authentifizierungs-Problemen (JWT/Keycloak) im Docker-Netzwerk und Validierung der "Tracer Bullet" Architektur (Ping-Service).

## Durchgeführte Arbeiten

### 1. Infrastruktur & IAM (Keycloak)
*   **Analyse:** Diskrepanz zwischen Dokumentation (`Testing_with_Postman.md`) und Konfiguration (`meldestelle-realm.json`) festgestellt.
*   **Fix (Realm):**
    *   Neue Rolle `MELD_USER` als technischer Platzhalter für verifizierte Benutzer eingeführt.
    *   Neuen Confidential Client `postman-client` erstellt, um saubere Backend-Tests via Password-Grant zu ermöglichen, ohne den Frontend-Client unsicher zu machen.
    *   Dem User `admin` die Rolle `MELD_USER` zugewiesen.
*   **Fix (Networking/JWT):**
    *   Das "Split Horizon"-Problem identifiziert (Token Issuer `localhost` vs. Docker-interner Keycloak-Host).
    *   `.env` Datei refactored: Trennung in `KC_ISSUER_URI` (Public) und `KC_JWK_SET_URI` (Internal).
    *   `dc-backend.yaml` aktualisiert: `api-gateway` und `ping-service` nutzen nun diese expliziten Variablen.

### 2. Dokumentation (Single Source of Truth)
*   **Update:** `docs/05_Backend/Guides/Testing_with_Postman.md` an den neuen `postman-client` angepasst.
*   **Neu:** `docs/07_Infrastructure/guides/jwt-in-docker.md` erstellt. Dieses Dokument erklärt das "Split Horizon"-Problem und dient als Referenz für Frontend- und Backend-Entwickler.

### 3. Testing
*   Erfolgreicher Durchlauf aller Postman-Tests (Connectivity, Health, Security Block, Auth Login, Security Access).
*   Bestätigung, dass der `ping-service` nun korrekt Token validiert, die von außen (Postman) kommen, aber intern (Docker) geprüft werden.

## Ergebnisse
*   Die lokale Entwicklungsumgebung ist nun **vollständig funktionsfähig** und **auth-ready**.
*   Die Infrastruktur-Konfiguration ist sauberer und expliziter (Trennung von Public/Internal URLs).
*   Eine solide Basis für die Frontend-Login-Implementierung ist geschaffen.

## Nächste Schritte
*   **Frontend:** Implementierung des Login-Flows (Authorization Code Flow mit PKCE) unter Nutzung des `web-app` Clients.
*   **Backend:** Beginn der Modellierung der **Events Domain** (Veranstaltungen).
