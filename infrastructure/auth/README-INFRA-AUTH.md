# Infrastructure/Auth Module

## Überblick

Das **Auth-Modul** ist die zentrale Komponente für die gesamte Authentifizierung und Autorisierung innerhalb der Meldestelle-Systemlandschaft. Es ist verantwortlich für die Absicherung von APIs, die Validierung von Benutzeridentitäten und die Verwaltung von Berechtigungen.

Als Identity Provider wird **Keycloak** verwendet. Dieses Modul kapselt die gesamte Interaktion mit Keycloak und stellt dem Rest des Systems eine einheitliche und vereinfachte Sicherheitsschicht zur Verfügung.

## Architektur

Das Auth-Modul ist in zwei spezialisierte Komponenten aufgeteilt, um eine klare Trennung der Verantwortlichkeiten zu gewährleisten:


infrastructure/auth/
├── auth-client/      # Wiederverwendbare Bibliothek für die JWT-Validierung
└── auth-server/      # Eigenständiger Service für Benutzerverwaltung & Token-Austausch


### `auth-client`

Dieses Modul ist eine **wiederverwendbare Bibliothek** und kein eigenständiger Service. Es enthält die gesamte Logik, die andere Microservices (wie `masterdata-service`, `members-service` etc.) benötigen, um ihre Endpunkte abzusichern.

**Hauptaufgaben:**
* **JWT-Validierung:** Stellt Spring Security Konfigurationen bereit, um eingehende JWTs (ausgestellt von Keycloak) zu validieren. Es prüft die Signatur, den Aussteller (Issuer) und die Gültigkeitsdauer des Tokens.
* **Rechte-Extraktion:** Extrahiert die Rollen und Berechtigungen des Benutzers aus dem validierten Token.
* **Security Context:** Befüllt den `SecurityContextHolder` von Spring, sodass in den Controllern einfach auf den authentifizierten Benutzer zugegriffen werden kann (z.B. mit `@AuthenticationPrincipal`).

Jeder Microservice, der geschützte Endpunkte anbietet, bindet dieses Modul als Abhängigkeit ein.

### `auth-server`

Dies ist ein **eigenständiger Spring Boot Microservice**, der als Brücke zwischen dem Meldestelle-System und Keycloak agiert.

**Hauptaufgaben:**
* **Benutzer-API:** Stellt eine REST-API zur Verfügung, um Benutzer zu verwalten (z.B. Registrierung, Profil-Updates). Diese API kommuniziert im Hintergrund über den `keycloak-admin-client` mit Keycloak.
* **Token-Endpunkte:** Kann Endpunkte für den Austausch oder die Erneuerung von Tokens bereitstellen (Token Exchange).
* **Zentraler Login-Punkt (Optional):** Kann als zentraler Punkt für Login-Weiterleitungen im OAuth2/OIDC-Flow dienen.

Durch die Kapselung der Keycloak-spezifischen Logik im `auth-server` müssen die anderen Fach-Services nichts über die Interna der Benutzerverwaltung wissen. Sie interagieren nur mit der sauberen API des `auth-server`.

## Zusammenspiel im System

1.  Ein **Benutzer** meldet sich über eine Client-Anwendung an und erhält ein JWT von **Keycloak**.
2.  Die **Client-Anwendung** sendet eine Anfrage an einen Microservice (z.B. `masterdata-service`) und fügt das JWT als Bearer-Token in den `Authorization`-Header ein.
3.  Der **Microservice**, der `auth-client` als Abhängigkeit hat, validiert das Token automatisch.
4.  Wenn der Microservice Benutzerdaten ändern muss, ruft er nicht direkt Keycloak auf, sondern die sichere REST-API des **`auth-server`**.

Diese Architektur entkoppelt die Fach-Services von der Komplexität der Identitätsverwaltung und schafft eine robuste, zentrale Sicherheitsinfrastruktur.

---
**Letzte Aktualisierung**: 31. Juli 2025
