
# Meldestelle

## Überblick

Meldestelle ist ein modulares System zur Verwaltung von Pferdesportveranstaltungen. Das System ermöglicht die Registrierung von Pferden, Mitgliedern und Veranstaltungen sowie die Verwaltung von Stammdaten.

Das Projekt wurde kürzlich auf eine modulare Architektur migriert, um die Wartbarkeit und Erweiterbarkeit zu verbessern.

## Systemanforderungen

- Java 21
- Kotlin 2.2.10
- Gradle 9.0.0 (automatischer Download über Gradle Wrapper)
- Docker und Docker Compose (v2.0+)

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

- **masterdata**: Umfassende Verwaltung von Stammdaten für Pferdesportveranstaltungen
  - **Funktionalität**: Länder (ISO-Codes, EU/EWR-Mitgliedschaft), Bundesländer (OEPS/ISO-Codes), Altersklassen (Teilnahmeberechtigung), Turnierplätze (Typ, Abmessungen, Boden)
  - **API-Endpunkte**: 37 REST-Endpunkte mit vollständiger CRUD-Funktionalität
  - **Geschäftslogik**: Validierung, Duplikatsprüfung, Berechtigung, Eignung für Disziplinen
  - masterdata-api: REST-Controller und DTO-Definitionen
  - masterdata-application: Use Cases und Geschäftslogik
  - masterdata-domain: Domänenmodelle und Repository-Interfaces
  - masterdata-infrastructure: Datenbankzugriff und Persistierung
  - masterdata-service: Spring Boot Service-Implementierung

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

### Docker-Infrastruktur

Das System bietet verschiedene Docker-Konfigurationen für unterschiedliche Umgebungen:

#### Entwicklungsumgebung (Schnellstart)

```bash
# Infrastruktur starten
docker compose up -d

# Status überprüfen
docker compose ps

# Logs anzeigen
docker compose logs -f
```

Dies startet alle erforderlichen Dienste wie PostgreSQL, Redis, Keycloak, Kafka, Zipkin und optional Prometheus und Grafana.

#### Produktionsumgebung

Für die Produktionsumgebung siehe **[README-PRODUCTION.md](Tagebuch/README-PRODUCTION.md)** - enthält:
- Umfassende Sicherheitskonfiguration
- SSL/TLS-Setup
- Detaillierte Troubleshooting-Anleitung
- Backup- und Wiederherstellungsverfahren

#### Umgebungsvariablen

Für die Konfiguration von Umgebungsvariablen siehe **[README-ENV.md](Tagebuch/README-ENV.md)** - enthält:
- Vollständige Umgebungsvariablen-Dokumentation
- Validierungsskripte
- Konfigurationsbeispiele

### Validierung und Troubleshooting

```bash
# Umgebungsvariablen validieren
./validate-env.sh

# Docker-Compose Konfiguration validieren
./validate-docker-compose.sh

# Service-Status überprüfen
docker-compose ps

# Service-Logs anzeigen
docker-compose logs [service-name]
```

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

Die Client-Anwendungen sind als ein gemeinsames Kotlin Multiplatform (KMP) Modul `:client` organisiert und liefern:
- Desktop (JVM) über Compose Desktop
- Web (Kotlin/JS im Browser) über Compose Multiplatform
- Optional: WASM mit Flag -PenableWasm=true

```bash
# Desktop (JVM) starten
./gradlew :client:run

# Web (WASM) – Development-Server mit Live-Reload
./gradlew :client:wasmJsBrowserDevelopmentRun

# Web (WASM) – Production-Build (mit optionaler Bundle-Analyse)
ANALYZE_BUNDLE=true ./gradlew :client:wasmJsBrowserProductionWebpack
```

Ausgabeorte (Build-Artefakte):
- Desktop-Distributionen: client/build/compose/binaries
- WASM Production Build: client/build/dist/wasmJs/productionExecutable

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

#### Status der Client-Module (nach Migration)
- Build-Status: :client baut erfolgreich für JVM, JS und WASM (Chrome/Karma-Tests sind bewusst deaktiviert, siehe unten)
- Desktop: Compose Desktop App startet über :client:run; API-Basisadresse via Umgebungsvariable API_BASE_URL (Default: http://localhost:8081)
- Web/WASM: Development-Server (:client:wasmJsBrowserDevelopmentRun) und Production-Build (:client:wasmJsBrowserProductionWebpack) funktionieren; API-Aufruf erfolgt same-origin über /api/ping (hinter dem Gateway)
- HTTP-Client: Minimaler Ktor-Client (ohne überflüssige Plugins) zur Reduzierung der Bundle-Größe
- UI: Platzhalter-/Demo-Features (Ping, Platform-Info, Conditional Panels) vorhanden; Domänenseiten für masterdata/members/horses/events noch ausständig

Bekannte Einschränkungen & offene Punkte:
- End-to-End-Navigation zu allen Domänen (masterdata, members, horses, events) fehlt noch
- Authentifizierung/Session-Handling im Client noch nicht integriert (Gateway/Keycloak folgt)
- Browser-basierte Unit-Tests (Karma/ChromeHeadless) sind abgeschaltet, um lokale Sandbox-Probleme zu vermeiden; JS-Tests laufen unter Node/Mocha

#### WASM-Bundle-Analyse & Optimierung
- Aktivieren über Umgebungsvariable ANALYZE_BUNDLE=true beim Production-WebBuild:

  ANALYZE_BUNDLE=true ./gradlew :client:wasmJsBrowserProductionWebpack

- Die Datei client/webpack.config.d/bundle-analyzer.js protokolliert die Asset-Größen und gibt Optimierungshinweise aus
- client/webpack.config.d/wasm-optimization.js aktiviert Tree-Shaking, Chunk-Splitting und Produktionsoptimierungen
- Weitere Tipps: Reduktion schwerer UI-Komponenten, Lazy Loading, Entfernen ungenutzter Abhängigkeiten

#### Integrationstests und E2E-Hinweise
- Vorhandene Modul-Integrationstests können per ./gradlew test ausgeführt werden
- Für manuelles E2E:
  1) docker compose up -d (Gateway + Services)
  2) Desktop-Client starten oder WASM-Dev-Server starten
  3) Ping im Client ausführen; Erwartung: Status OK vom Gateway-Endpunkt /api/ping

### Entwicklungsrichtlinien

- Verwenden Sie die in der Projektstruktur definierten Module
- Folgen Sie den Architekturentscheidungen (ADRs) im Verzeichnis `docs/architecture/adr` (verfügbar in Deutsch mit Dateiendung `-de.md`)
- Verwenden Sie die C4-Diagramme im Verzeichnis `docs/architecture/c4` für einen Überblick über die Systemarchitektur (verfügbar in Deutsch mit Dateiendung `-de.puml`)
- Verwenden Sie die Datenmodelle aus `docs/architecture/data-model`

### Tests ausführen

```bash
./gradlew test
```

## Docker Troubleshooting (Entwicklungsumgebung)

### Häufige Probleme und Lösungen

#### 1. Services starten nicht
```bash
# Alle Services stoppen und neu starten
docker compose down
docker compose up -d

# Einzelnen Service neu starten
docker compose restart [service-name]

# Service-Logs überprüfen
docker compose logs [service-name]
```

#### 2. Port bereits belegt
```bash
# Verwendete Ports prüfen
netstat -tulpn | grep :[port]
# oder
lsof -i :[port]

# Ports in .env anpassen
nano .env
# Beispiel: API_PORT=8081 statt 8080
```

#### 3. Datenbank-Verbindungsfehler
```bash
# PostgreSQL-Status prüfen
docker compose exec postgres pg_isready -U meldestelle

# Datenbank-Logs anzeigen
docker compose logs postgres

# Verbindung manuell testen
docker compose exec postgres psql -U meldestelle -d meldestelle
```

#### 4. Keycloak-Authentifizierung fehlgeschlagen
```bash
# Keycloak-Status prüfen
docker compose logs keycloak

# Keycloak Admin-Console öffnen
# http://localhost:8180/admin (admin/admin)

# Keycloak-Datenbank zurücksetzen
docker compose down
docker volume rm meldestelle_postgres-data
docker compose up -d
```

#### 5. Kafka-Verbindungsprobleme
```bash
# Kafka-Status prüfen
docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Zookeeper-Status prüfen
docker compose exec zookeeper nc -z localhost 2181

# Kafka-Logs anzeigen
docker compose logs kafka zookeeper
```

#### 6. Speicherplatz-Probleme
```bash
# Docker-Speicherverbrauch prüfen
docker system df

# Ungenutzte Ressourcen bereinigen
docker system prune -f

# Volumes bereinigen (ACHTUNG: Datenverlust!)
docker system prune -f --volumes
```

#### 7. Performance-Probleme
```bash
# Ressourcenverbrauch überwachen
docker stats

# Container-Limits anpassen (in docker-compose.yml)
# deploy:
#   resources:
#     limits:
#       memory: 1G
#       cpus: '0.5'
```

### Nützliche Docker-Befehle

```bash
# Alle Services mit Logs starten
docker compose up

# Services im Hintergrund starten
docker compose up -d

# Bestimmte Services starten
docker compose up postgres redis

# Services stoppen
docker compose stop

# Services stoppen und Container entfernen
docker compose down

# Services mit Volume-Bereinigung stoppen
docker compose down -v

# Container-Shell öffnen
docker compose exec [service-name] /bin/bash
# oder für Alpine-basierte Images:
docker compose exec [service-name] /bin/sh

# Konfiguration validieren
docker compose config

# Service-Status anzeigen
docker compose ps

# Logs aller Services anzeigen
docker compose logs

# Logs eines bestimmten Services verfolgen
docker compose logs -f [service-name]
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

Letzte Aktualisierung: 14. September 2025
