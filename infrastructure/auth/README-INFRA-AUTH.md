# Infrastructure/Auth Module

## Überblick

Das **Auth-Modul** ist die zentrale Komponente für die gesamte Authentifizierung und Autorisierung innerhalb der Meldestelle-Systemlandschaft. Es ist verantwortlich für die Absicherung von APIs, die Validierung von Benutzeridentitäten und die Verwaltung von Berechtigungen.

Als Identity Provider wird **Keycloak** verwendet. Dieses Modul kapselt die gesamte Interaktion mit Keycloak und stellt dem Rest des Systems eine einheitliche und vereinfachte Sicherheitsschicht zur Verfügung.

## Architektur

Das Auth-Modul ist in zwei spezialisierte Komponenten aufgeteilt, um eine klare Trennung der Verantwortlichkeiten zu gewährleisten:

infrastructure/auth/
├── auth-client/ # Wiederverwendbare Bibliothek für die JWT-Validierung
└── auth-server/ # Eigenständiger Service für Benutzerverwaltung & Token-Austausch


### `auth-client`

Dieses Modul ist eine **wiederverwendbare Bibliothek** und kein eigenständiger Service. Es enthält die gesamte Logik, die andere Microservices (wie `masterdata-service`, `members-service` etc.) benötigen, um ihre Endpunkte abzusichern.

**Hauptaufgaben:**
* **JWT-Management:** Stellt einen `JwtService` zur Erstellung und Validierung von JSON Web Tokens bereit.
* **Modell-Definition:** Definiert die **Quelle der Wahrheit** für sicherheitsrelevante Konzepte wie `RolleE` und `BerechtigungE` als typsichere Kotlin-Enums. Dies stellt sicher, dass alle Services dieselbe "Sprache" für Berechtigungen sprechen.
* **Schnittstellen:** Bietet saubere Schnittstellen wie `AuthenticationService` an, die von der konkreten Implementierung (z.B. Keycloak) abstrahieren.

Jeder Microservice, der geschützte Endpunkte anbietet, bindet dieses Modul als Abhängigkeit ein.

### `auth-server`

Dies ist ein **eigenständiger Spring Boot Microservice**, der als Brücke zwischen dem Meldestelle-System und Keycloak agiert.

**Hauptaufgaben:**
* **Benutzer-API:** Stellt eine REST-API zur Verfügung, um Benutzer zu verwalten (z.B. Registrierung). Diese API kommuniziert im Hintergrund über den `keycloak-admin-client` mit Keycloak.
* **Token-Endpunkte:** Ist verantwortlich für das Ausstellen von Tokens nach einer erfolgreichen Authentifizierung.
* **Implementierung der `AuthenticationService`-Schnittstelle:** Enthält die konkrete Logik, die gegen Keycloak prüft, ob ein Benutzername und ein Passwort korrekt sind.

## Zusammenspiel im System

1.  Ein **Benutzer** meldet sich über eine Client-Anwendung am **`auth-server`** an.
2.  Der **`auth-server`** validiert die Anmeldedaten gegen **Keycloak**.
3.  Bei Erfolg erstellt der `auth-server` mit dem `JwtService` aus dem `auth-client` ein JWT, das die Berechtigungen des Benutzers enthält, und sendet es an den Client zurück.
4.  Der **Client** sendet eine Anfrage an einen anderen Microservice (z.B. `members-service`) und fügt das JWT als Bearer-Token in den Header ein.
5.  Der **`members-service`**, der ebenfalls den `auth-client` als Abhängigkeit hat, nutzt den `JwtService`, um das Token zu validieren und die Berechtigungen typsicher auszulesen.

Diese Architektur entkoppelt die Fach-Services von der Komplexität der Identitätsverwaltung und schafft eine robuste, zentrale Sicherheitsinfrastruktur.
---
**Letzte Aktualisierung**: 9. August 2025
