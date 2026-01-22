---
type: Guide
status: ACTIVE
owner: DevOps Engineer
tags: [jwt, oidc, keycloak, docker, networking, security]
---

# Leitfaden: JWT-Validierung in der Docker-Umgebung

Dieser Leitfaden erklärt eine kritische Herausforderung und deren Lösung bei der Arbeit mit Keycloak (OIDC) und Microservices in einer Docker-Umgebung.

## Das Problem: Das "Split Horizon"-Dilemma

In unserer lokalen Docker-Umgebung existieren zwei "Sichtweisen" (Horizons) auf Keycloak:

1.  **Die externe Sicht (Browser/Postman):** Ein Client außerhalb von Docker (z.B. Postman) greift auf Keycloak über `http://localhost:8180` zu. Wenn dieser Client ein Token anfordert, stellt Keycloak dieses Token mit dem Issuer (`iss`) Claim `http://localhost:8180/realms/meldestelle` aus.

2.  **Die interne Sicht (Microservices):** Ein Service innerhalb des Docker-Netzwerks (z.B. `ping-service`) kann `localhost` nicht verwenden, um Keycloak zu erreichen. Er muss den Docker-internen Hostnamen `http://keycloak:8080` verwenden.

Wenn der Service nun das von außen kommende Token validieren will, passiert Folgendes:
*   Das Token sagt: "Ich wurde von `http://localhost:8180/realms/meldestelle` ausgestellt."
*   Die Standardkonfiguration des Services sagt: "Ich vertraue aber nur Token von `http://keycloak:8080/realms/meldestelle`."
*   **Ergebnis:** Die Validierung schlägt mit `iss claim is not valid` fehl.

## Die Lösung: Getrennte Konfiguration von Issuer und JWK-Pfad

Die Lösung besteht darin, Spring Security so zu konfigurieren, dass es zwischen der **Validierung des Issuers** und dem **technischen Abruf der Schlüssel** unterscheidet.

*   `spring.security.oauth2.resourceserver.jwt.issuer-uri`: **Muss exakt mit dem `iss`-Claim im Token übereinstimmen.** Hier verwenden wir die öffentliche URL.
*   `spring.security.oauth2.resourceserver.jwt.jwk-set-uri`: Der technische Endpunkt, unter dem der Service die Public Keys (JWKs) zur Signaturprüfung abruft. Hier verwenden wir die interne Docker-URL.

### Implementierung

Wir steuern dies zentral über unsere `.env`-Datei, um eine "Single Source of Truth" zu haben:

**`.env`**
```dotenv
# Public Issuer URI (must match the token issuer from browser/postman)
KC_ISSUER_URI=http://localhost:8180/realms/meldestelle

# Internal JWK Set URI (for service-to-service communication within Docker)
KC_JWK_SET_URI=http://keycloak:8080/realms/meldestelle/protocol/openid-connect/certs
```

Diese Variablen werden dann in der `dc-backend.yaml` an die Services durchgereicht:

**`dc-backend.yaml`**
```yaml
services:
  ping-service:
    environment:
      # ...
      # --- KEYCLOAK ---
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: "${KC_ISSUER_URI}"
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI: "${KC_JWK_SET_URI}"
```

Diese Konfiguration stellt sicher, dass die Validierung erfolgreich ist, während die Kommunikation innerhalb des Docker-Netzwerks korrekt funktioniert.

---

## Empfehlungen für die Entwickler

### Für den Senior Backend Developer

1.  **Konfigurations-Muster übernehmen:** Wenn du neue Microservices erstellst, kopiere exakt den `environment`-Block für Keycloak aus dem `ping-service` in der `dc-backend.yaml`. Das stellt sicher, dass die JWT-Validierung von Anfang an korrekt funktioniert.
2.  **Rollen-Synchronisation:** Bevor du einen Endpunkt mit `@PreAuthorize("hasRole('DEINE_ROLLE')")` sicherst, stelle sicher, dass die Rolle `DEINE_ROLLE` auch in der `config/docker/keycloak/meldestelle-realm.json` definiert ist.
3.  **Debugging-Tipp:** Bei einem `401 Unauthorized` auf einem geschützten Endpunkt, prüfe immer zuerst die Logs des Services. Die Fehlermeldungen von Spring Security sind sehr aussagekräftig (z.B. `iss claim not valid`, `Connection refused`, `An error occurred while attempting to decode the Jwt`).

### Für den KMP Frontend Expert

1.  **Stabiler Auth-Flow:** Die Authentifizierung ist jetzt stabil. Du kannst dich auf die Implementierung des Login-Prozesses konzentrieren.
2.  **Client-Konfiguration:** Der `web-app` Client in Keycloak ist für dich vorbereitet. Er ist ein **public client** und nutzt den sicheren **Authorization Code Flow mit PKCE**. Du musst eine OIDC-Client-Bibliothek verwenden, die diesen Flow unterstützt (z.B. `keycloak-js` oder eine moderne Alternative wie `oidc-client-ts`).
3.  **Benötigte Konfiguration:** Dein Frontend benötigt folgende Informationen, die du am besten in einer Environment-Datei ablegst:
    *   Keycloak URL: `http://localhost:8180`
    *   Realm: `meldestelle`
    *   Client ID: `web-app`
4.  **Keine Secrets im Frontend:** Der `web-app` Client hat absichtlich **kein** Secret. Versuche niemals, ein Secret im Frontend-Code zu speichern oder zu verwenden. PKCE sorgt hier für die nötige Sicherheit.
5.  **Token-Handling:** Nach dem Login erhältst du ein **Access Token**. Dieses muss bei jeder API-Anfrage an das Backend (`http://localhost:8081/api/...`) im `Authorization: Bearer <token>` HTTP-Header mitgesendet werden.
