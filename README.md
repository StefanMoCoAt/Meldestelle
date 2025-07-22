# Meldestelle

## Überblick

Meldestelle ist ein modulares System zur Verwaltung von Pferdesportveranstaltungen. Das System ermöglicht die Registrierung von Pferden, Mitgliedern und Veranstaltungen sowie die Verwaltung von Stammdaten.

Das Projekt wurde kürzlich auf eine modulare Architektur migriert, um die Wartbarkeit und Erweiterbarkeit zu verbessern.

## Systemanforderungen

- Java 21
- Kotlin 2.1.20
- Gradle 8.14
- Docker und Docker Compose

## Infrastruktur

Das System nutzt folgende Dienste:

- **PostgreSQL 16**: Primäre Datenbank
- **Redis 7**: Caching
- **Keycloak 23.0**: Authentifizierung und Autorisierung
- **Kafka 7.5.0**: Messaging und Event-Streaming
- **Zipkin**: Distributed Tracing
- **Prometheus & Grafana**: Monitoring (optional)

## Projektstruktur

Das Projekt ist in folgende Hauptmodule unterteilt:

- **core**: Gemeinsame Kernkomponenten
  - core-domain: Domänenmodelle und Geschäftslogik
  - core-utils: Allgemeine Hilfsfunktionen

- **masterdata**: Verwaltung von Stammdaten
  - masterdata-api: API-Definitionen
  - masterdata-application: Anwendungslogik
  - masterdata-domain: Domänenmodelle
  - masterdata-infrastructure: Infrastrukturkomponenten
  - masterdata-service: Service-Implementierung

- **members**: Mitgliederverwaltung
  - members-api: API-Definitionen
  - members-application: Anwendungslogik
  - members-domain: Domänenmodelle
  - members-infrastructure: Infrastrukturkomponenten
  - members-service: Service-Implementierung

- **horses**: Pferderegistrierung
  - horses-api: API-Definitionen
  - horses-application: Anwendungslogik
  - horses-domain: Domänenmodelle
  - horses-infrastructure: Infrastrukturkomponenten
  - horses-service: Service-Implementierung

- **events**: Veranstaltungsverwaltung
  - events-api: API-Definitionen
  - events-application: Anwendungslogik
  - events-domain: Domänenmodelle
  - events-infrastructure: Infrastrukturkomponenten
  - events-service: Service-Implementierung

- **infrastructure**: Gemeinsame Infrastrukturkomponenten
  - auth: Authentifizierung
  - cache: Caching
  - event-store: Event-Speicher
  - gateway: API-Gateway
  - messaging: Messaging-Infrastruktur
  - monitoring: Monitoring-Komponenten

- **client**: Client-Anwendungen
  - common-ui: Gemeinsame UI-Komponenten
  - desktop-app: Desktop-Anwendung
  - web-app: Web-Anwendung

## Installation und Setup

### Voraussetzungen

Stellen Sie sicher, dass Java 21, Docker und Docker Compose installiert sind.

### Infrastruktur starten

```bash
docker-compose up -d
```

Dies startet alle erforderlichen Dienste wie PostgreSQL, Redis, Keycloak, Kafka, Zipkin und optional Prometheus und Grafana.

### Projekt bauen

```bash
./gradlew build
```

### Dienste starten

```bash
# Gateway starten
./gradlew :infrastructure:gateway:bootRun

# Masterdata-Service starten
./gradlew :masterdata:masterdata-service:bootRun

# Members-Service starten
./gradlew :members:members-service:bootRun

# Horses-Service starten
./gradlew :horses:horses-service:bootRun

# Events-Service starten
./gradlew :events:events-service:bootRun
```

### Client-Anwendungen starten

```bash
# Desktop-Anwendung starten
./gradlew :client:desktop-app:run

# Web-Anwendung bauen
./gradlew :client:web-app:build
```

## Entwicklung

### Aktuelle Migrationshinweise

Das Projekt wurde kürzlich von einer monolithischen Struktur zu einer modularen Architektur migriert. Die Migration umfasste:

- Umzug von `:shared-kernel` zu `core`-Modulen
- Umzug von `:master-data` zu `masterdata`-Modulen
- Umzug von `:member-management` zu `members`-Modulen
- Umzug von `:horse-registry` zu `horses`-Modulen
- Umzug von `:event-management` zu `events`-Modulen
- Umzug von `:api-gateway` zu `infrastructure/gateway`
- Umzug von `:composeApp` zu `client`-Modulen

Es gibt noch einige offene Probleme, insbesondere bei den Client-Modulen, die Kotlin Multiplatform und Compose Multiplatform verwenden.

### Entwicklungsrichtlinien

- Verwenden Sie die in der Projektstruktur definierten Module
- Folgen Sie den Architekturentscheidungen (ADRs) im Verzeichnis `docs/architecture/adr`
- Verwenden Sie die Datenmodelle aus `docs/architecture/data-model`

### Tests ausführen

```bash
./gradlew test
```

## Dokumentation

Weitere Dokumentation finden Sie im `docs`-Verzeichnis:

- API-Dokumentation: `docs/api`
- Architektur: `docs/architecture`
- Entwicklungsrichtlinien: `docs/development`
- Diagramme: `docs/diagrams`
- Betriebsanleitung: `docs/operations`
- Postman-Sammlungen: `docs/postman`

## Lizenz

Siehe [LICENSE](LICENSE) Datei.

## Stand

Letzte Aktualisierung: 22. Juli 2025
