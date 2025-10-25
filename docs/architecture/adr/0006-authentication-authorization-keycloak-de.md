# ADR-0006: Authentifizierung und Autorisierung mit Keycloak

## Status

Akzeptiert

## Kontext

Als Teil unserer Microservices-Architektur ([ADR-0003](0003-microservices-architecture-de.md)) benötigten wir eine robuste und zentralisierte Lösung für Authentifizierung und Autorisierung. Zu den wichtigsten Anforderungen gehörten:

1. Single Sign-On (SSO) über alle Dienste und Anwendungen hinweg
2. Unterstützung für mehrere Authentifizierungsmethoden (Benutzername/Passwort, OAuth, SAML)
3. Feingranulare Autorisierung mit rollenbasierter Zugriffssteuerung (RBAC)
4. Benutzerverwaltungsfunktionen einschließlich Selbstregistrierung und Profilmanagement
5. Integration mit externen Identitätsanbietern
6. Sicherheits-Best-Practices einschließlich Passwortrichtlinien und Kontosperrung
7. Token-basierte Authentifizierung für die Kommunikation zwischen Diensten

Die Implementierung dieser Funktionen von Grund auf wäre zeitaufwändig und fehleranfällig und würde Ressourcen von unserer Kerngeschäftsfunktionalität abziehen.

## Entscheidung

Wir haben uns entschieden, Keycloak (Version 26.4.2) als unsere Identitäts- und Zugriffsverwaltungslösung zu verwenden. Keycloak ist eine Open-Source-Identitäts- und Zugriffsverwaltungslösung, die Folgendes bietet:

1. **Benutzerauthentifizierung**: Mehrere Authentifizierungsmethoden und -abläufe
2. **Benutzerföderation**: Integration mit LDAP, Active Directory und anderen Benutzerspeichern
3. **Identitätsvermittlung**: Integration mit externen Identitätsanbietern (Google, Facebook usw.)
4. **Single Sign-On**: Über alle Anwendungen und Dienste hinweg
5. **Feingranulare Autorisierung**: Rollen- und attributbasierte Zugriffssteuerung
6. **Benutzerverwaltung**: Selbstregistrierung, Profilmanagement, Passwortrichtlinien
7. **Token-basierte Authentifizierung**: JWT-Tokens für die Kommunikation zwischen Diensten

Unsere Implementierung umfasst:
- Keycloak-Server, der als containerisierter Dienst bereitgestellt wird
- Integration mit unserem API-Gateway für die Token-Validierung
- Client-Adapter für unsere Dienste und Anwendungen
- Benutzerdefinierte Themes und E-Mail-Vorlagen
- Rollen- und Gruppendefinitionen, die auf unser Domänenmodell abgestimmt sind

## Konsequenzen

### Positive

- **Umfassende Lösung**: Keycloak bietet eine vollständige Identitäts- und Zugriffsverwaltungslösung
- **Standards-Konformität**: Keycloak implementiert Industriestandards (OAuth 2.0, OpenID Connect, SAML)
- **Reduzierter Entwicklungsaufwand**: Wir müssen Authentifizierung und Autorisierung nicht von Grund auf implementieren
- **Sicherheit**: Keycloak folgt Sicherheits-Best-Practices und wird aktiv gewartet
- **Flexibilität**: Keycloak unterstützt mehrere Authentifizierungsmethoden und Identitätsanbieter

### Negative

- **Betriebliche Komplexität**: Keycloak fügt einen weiteren Dienst hinzu, der bereitgestellt und gewartet werden muss
- **Lernkurve**: Teams müssen Keycloak-Konzepte und APIs erlernen
- **Leistungsüberlegungen**: Die Token-Validierung fügt den Anfragen einen gewissen Overhead hinzu
- **Abhängigkeit**: Wir sind für Authentifizierung und Autorisierung von Keycloak abhängig

### Neutral

- **Konfigurationsbedarf**: Keycloak erfordert sorgfältige Konfiguration, um mit unseren Sicherheitsanforderungen übereinzustimmen
- **Upgrade-Management**: Keycloak-Upgrades müssen sorgfältig verwaltet werden

## Betrachtete Alternativen

### Eigener Authentifizierungsdienst

Wir haben in Betracht gezogen, unseren eigenen Authentifizierungsdienst zu entwickeln. Dies hätte uns vollständige Kontrolle über die Implementierung gegeben, hätte aber erheblichen Entwicklungsaufwand und laufende Wartung erfordert.

### Auth0

Wir haben die Verwendung von Auth0, einer kommerziellen Identity-as-a-Service (IDaaS)-Lösung, in Betracht gezogen. Auth0 hätte ähnliche Funktionen wie Keycloak mit weniger betrieblichem Overhead geboten, hätte aber laufende Kosten und potenzielle Anbieterabhängigkeit mit sich gebracht.

### Spring Security mit JWT

Wir haben die Verwendung von Spring Security mit JWT-Tokens für Authentifizierung und Autorisierung in Betracht gezogen. Dies hätte sich gut in unsere Spring-basierten Dienste integriert, hätte aber mehr Entwicklungsaufwand erfordert und hätte nicht die umfassenden Identitätsverwaltungsfunktionen von Keycloak geboten.

## Referenzen

- [Keycloak Dokumentation](https://www.keycloak.org/documentation)
- [OAuth 2.0 und OpenID Connect](https://oauth.net/2/)
- [JWT (JSON Web Tokens)](https://jwt.io/)
- [Absicherung von Microservices mit Keycloak](https://www.keycloak.org/docs/latest/securing_apps/)
