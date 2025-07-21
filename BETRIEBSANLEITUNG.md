# Betriebsanleitung für das Meldestelle-Projekt

Diese Betriebsanleitung beschreibt, wie Sie das Meldestelle-Projekt einrichten und ausführen können.

## Inhaltsverzeichnis

1. [Projektübersicht](#projektübersicht)
2. [Voraussetzungen](#voraussetzungen)
3. [Installation](#installation)
4. [Konfiguration](#konfiguration)
5. [Ausführung](#ausführung)
6. [Zugriff auf die Anwendung](#zugriff-auf-die-anwendung)
7. [Monitoring und Wartung](#monitoring-und-wartung)
8. [Fehlerbehebung](#fehlerbehebung)

## Projektübersicht

Das Meldestelle-Projekt ist ein Kotlin JVM Backend-Projekt, das eine Self-Contained Systems (SCS) Architektur für ein Pferdesport-Managementsystem implementiert. Es folgt den Prinzipien des Domain-Driven Design (DDD) mit klar getrennten Bounded Contexts.

### Module

- **shared-kernel**: Gemeinsame Domänentypen, Enums, Serialisierer, Validierungsdienstprogramme und Basis-DTOs
- **master-data**: Stammdatenverwaltung (Länder, Regionen, Altersklassen, Veranstaltungsorte)
- **member-management**: Personen- und Vereins-/Verbandsverwaltung
- **horse-registry**: Pferderegistrierung und -verwaltung
- **event-management**: Veranstaltungs- und Turnierverwaltung
- **api-gateway**: Zentrales API-Gateway, das alle Dienste aggregiert
- **composeApp**: Frontend-Modul

### Technologie-Stack

- **Kotlin JVM**: Primäre Programmiersprache
- **Ktor**: Web-Framework für REST-APIs
- **Exposed**: Datenbank-ORM
- **PostgreSQL**: Datenbank
- **Consul**: Service-Discovery und -Registry
- **Kotlinx Serialization**: JSON-Serialisierung
- **Gradle**: Build-System
- **Docker**: Containerisierung

## Voraussetzungen

Um das Projekt auszuführen, benötigen Sie:

### Für die lokale Entwicklung

- JDK 21 oder höher
- Gradle 8.14 oder höher
- PostgreSQL 16
- Docker und Docker Compose (für containerisierte Ausführung)
- Git (für den Quellcode-Zugriff)

### Für die containerisierte Ausführung

- Docker Engine 24.0 oder höher
- Docker Compose V2 oder höher

## Installation

### Quellcode herunterladen

```bash
git clone <repository-url>
cd meldestelle
```

### Umgebungsvariablen einrichten

Erstellen Sie eine `.env`-Datei im Stammverzeichnis des Projekts mit den folgenden Umgebungsvariablen:

```
# Postgres-Konfiguration
POSTGRES_USER=meldestelle_user
POSTGRES_PASSWORD=secure_password_change_me
POSTGRES_DB=meldestelle_db
POSTGRES_SHARED_BUFFERS=256MB
POSTGRES_EFFECTIVE_CACHE_SIZE=768MB
POSTGRES_WORK_MEM=16MB
POSTGRES_MAINTENANCE_WORK_MEM=64MB
POSTGRES_MAX_CONNECTIONS=100

# PgAdmin-Konfiguration
PGADMIN_DEFAULT_EMAIL=admin@example.com
PGADMIN_DEFAULT_PASSWORD=admin_password_change_me
PGADMIN_PORT=127.0.0.1:5050

# Grafana-Konfiguration
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
```

**Wichtig**: Ändern Sie die Passwörter für eine Produktionsumgebung!

## Konfiguration

Das Projekt verwendet einen mehrschichtigen Konfigurationsansatz, um verschiedene Umgebungen zu unterstützen:

1. Umgebungsvariablen (höchste Priorität)
2. Umgebungsspezifische Konfigurationsdateien (.properties)
3. Basis-Konfigurationsdatei (application.properties)
4. Standardwerte im Code (niedrigste Priorität)

### Umgebungen

Das Projekt unterstützt folgende Umgebungen:

| Umgebung     | Beschreibung                | Typische Verwendung                       |
|--------------|-----------------------------|--------------------------------------------|
| DEVELOPMENT  | Lokale Entwicklungsumgebung | Lokale Entwicklung, Debug-Modus aktiv      |
| TEST         | Testumgebung                | Automatisierte Tests, Integrationstests    |
| STAGING      | Vorabproduktionsumgebung    | Manuelle Tests, UAT, Demos                 |
| PRODUCTION   | Produktionsumgebung         | Live-System                                |

Die aktuelle Umgebung wird über die Umgebungsvariable `APP_ENV` festgelegt. Wenn diese Variable nicht gesetzt ist, wird standardmäßig `DEVELOPMENT` verwendet.

### Konfigurationsdateien

Die Konfigurationsdateien befinden sich im `/config`-Verzeichnis:

- `application.properties`: Basiseinstellungen für alle Umgebungen
- `application-dev.properties`: Entwicklungsumgebung
- `application-test.properties`: Testumgebung
- `application-staging.properties`: Staging-Umgebung
- `application-prod.properties`: Produktionsumgebung

## Ausführung

### Methode 1: Mit Docker Compose (empfohlen)

Diese Methode startet alle erforderlichen Dienste in Containern.

1. Stellen Sie sicher, dass Docker und Docker Compose installiert sind
2. Stellen Sie sicher, dass die `.env`-Datei konfiguriert ist
3. Führen Sie den folgenden Befehl aus:

```bash
docker compose up -d
```

Um die Logs zu überwachen:

```bash
docker compose logs -f
```

Um die Dienste zu stoppen:

```bash
docker compose down
```

Um die Dienste zu stoppen und alle Daten zu löschen:

```bash
docker compose down -v
```

### Methode 2: Lokale Entwicklung mit Gradle

Diese Methode ist für die Entwicklung gedacht und erfordert eine lokale PostgreSQL-Datenbank.

1. Stellen Sie sicher, dass JDK 21 oder höher installiert ist
2. Stellen Sie sicher, dass PostgreSQL installiert und konfiguriert ist
3. Konfigurieren Sie die Datenbankverbindung in `config/application-dev.properties`
4. Bauen Sie das Projekt:

```bash
./gradlew build
```

5. Starten Sie das API-Gateway:

```bash
./gradlew :api-gateway:jvmRun
```

## Zugriff auf die Anwendung

Nach dem Start der Anwendung können Sie auf folgende Dienste zugreifen:

- **API-Gateway**: http://localhost:8080
- **API-Dokumentation**: http://localhost:8080/docs
- **Swagger UI**: http://localhost:8080/swagger
- **OpenAPI-Spezifikation**: http://localhost:8080/openapi
- **PgAdmin**: http://localhost:5050
- **Consul UI**: http://localhost:8500
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000
- **Kibana**: http://localhost:5601

### API-Dokumentation

Die API-Dokumentation umfasst alle Bounded Contexts:
- Authentication API
- Master Data API
- Member Management API
- Horse Registry API
- Event Management API

## Monitoring und Wartung

### Monitoring-Stack

Das Projekt enthält einen vollständigen Monitoring-Stack:

- **Prometheus**: Metriken-Sammlung und -Speicherung
- **Grafana**: Visualisierung von Metriken und Dashboards
- **Alertmanager**: Benachrichtigungen bei Problemen
- **ELK-Stack**: Elasticsearch, Logstash und Kibana für Logging

### Service Discovery

Das Projekt verwendet Consul für Service Discovery, wodurch Dienste sich dynamisch entdecken und miteinander kommunizieren können, ohne fest codierte Endpunkte zu verwenden. Dies macht das System widerstandsfähiger und skalierbarer.

- **Consul UI**: Zugriff auf die Consul-UI unter http://localhost:8500

## Fehlerbehebung

### Häufige Probleme

1. **Dienste starten nicht**
   - Überprüfen Sie die Docker-Logs: `docker-compose logs -f <service-name>`
   - Stellen Sie sicher, dass alle erforderlichen Ports verfügbar sind
   - Überprüfen Sie die Umgebungsvariablen in der `.env`-Datei

2. **Fehler bei Docker Compose Abhängigkeiten**
   - Wenn Sie eine Fehlermeldung wie `service "X" depends on undefined service "Y"` erhalten, überprüfen Sie die `depends_on`-Einträge in der docker-compose.yml
   - Stellen Sie sicher, dass alle referenzierten Dienste korrekt definiert sind
   - Für Dienste, die über Hostnamen kommunizieren, können Sie Netzwerk-Aliase verwenden:
     ```yaml
     services:
       api-gateway:
         networks:
           meldestelle-net:
             aliases:
               - server
     ```
   - Stellen Sie sicher, dass alle verwendeten Volumes in der `volumes`-Sektion definiert sind

3. **Datenbankverbindungsprobleme**
   - Überprüfen Sie, ob die PostgreSQL-Datenbank läuft: `docker-compose ps db`
   - Überprüfen Sie die Datenbankverbindungseinstellungen
   - Überprüfen Sie die Datenbank-Logs: `docker-compose logs -f db`

4. **API-Gateway ist nicht erreichbar**
   - Überprüfen Sie, ob der API-Gateway-Dienst läuft: `docker-compose ps api-gateway`
   - Überprüfen Sie die API-Gateway-Logs: `docker-compose logs -f api-gateway`
   - Stellen Sie sicher, dass Port 8080 nicht von einem anderen Dienst verwendet wird

5. **PostgreSQL SSL-Konfigurationsprobleme**
   - Wenn die Datenbank mit der Fehlermeldung `FATAL: could not load server certificate file "server.crt": No such file or directory` nicht startet, ist SSL aktiviert, aber die erforderlichen Zertifikatsdateien fehlen
   - Lösungen:
     - Option 1: Deaktivieren Sie SSL in der PostgreSQL-Konfiguration (`config/postgres/postgresql.conf`), indem Sie `ssl = off` setzen
     - Option 2: Stellen Sie die erforderlichen SSL-Zertifikatsdateien (server.crt, server.key) bereit und mounten Sie sie im Container
   - Für Entwicklungsumgebungen ist Option 1 (SSL deaktivieren) in der Regel ausreichend
   - Für Produktionsumgebungen sollten Sie Option 2 (SSL-Zertifikate bereitstellen) in Betracht ziehen, um die Datenbankverbindungen zu sichern

### Support

Bei weiteren Problemen wenden Sie sich bitte an das Entwicklungsteam oder erstellen Sie ein Issue im Repository.

---

Letzte Aktualisierung: 2025-07-21
