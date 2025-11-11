# Docker-Development Workflow

---

guideline_type: "technology"
scope: "docker-development"
audience: ["developers", "ai-assistants"]
last_updated: "2025-11-11"
dependencies: ["docker-overview.md", "docker-architecture.md"]
related_files: ["docker-compose.yml", "docker-compose.services.yml", "docker-compose.clients.yml", "Makefile"]
ai_context: "Entwicklungs-Workflow, Debugging und lokale Entwicklungsumgebung mit Docker"

---

## 🛠️ Development-Workflow

> **📖 Hinweis:** Für einen allgemeinen Überblick über die Docker-Infrastruktur 
> siehe [docker-overview](docker-overview.md). 
> Für Details zur Container-Architektur siehe [docker-architecture](docker-architecture.md).
> Für Production-Deployment siehe [docker-production](docker-production.md).

## 🚀 Schnellstart

### Komplette Hilfe anzeigen

```bash
make help  # Zeigt alle verfügbaren Befehle mit Beschreibungen
```

### Wichtigste Befehle für den Einstieg

```bash
# Komplettes System starten
make full-up          # Infrastruktur + Services + Clients

# Nur Backend starten
make services-up      # Infrastruktur + Microservices

# Nur Entwicklungsumgebung
make dev-up           # Infrastruktur only

# System-Health prüfen
make health-check     # Prüft alle Infrastruktur-Services

# Logs anzeigen
make full-logs        # Alle Logs in Echtzeit
```

## 📚 Vollständige Makefile-Referenz

### Development Workflow Commands

Befehle für die lokale Entwicklungsumgebung:

```bash
make dev-up          # Startet Entwicklungsumgebung (docker-compose.yml)
make dev-down        # Stoppt Entwicklungsumgebung
make dev-restart     # Neustart Entwicklungsumgebung
make dev-logs        # Zeigt alle Development-Logs
make dev-info        # Zeigt Entwicklungsumgebungs-Informationen
```

**Verwendung:**

```bash
# Infrastruktur starten (Postgres, Redis, Keycloak, Consul)
make dev-up

# Nach dem Start werden angezeigt:
# - Consul UI: http://localhost:8500
# - Keycloak: http://localhost:8180 (admin/admin)
# - PostgreSQL: localhost:5432
# - Redis: localhost:6379
```

### Layer-spezifische Commands

Das System ist in drei Schichten organisiert:

#### 1. Infrastructure Layer

```bash
make infrastructure-up      # Startet nur Infrastruktur-Services
make infrastructure-down    # Stoppt Infrastruktur-Services
make infrastructure-logs    # Zeigt Infrastruktur-Logs
```

**Services:** PostgreSQL, Redis, Keycloak, Consul, Kafka, Zookeeper, Prometheus, Grafana

#### 2. Services Layer (Backend)

```bash
make services-up       # Startet Infrastruktur + Microservices
make services-down     # Stoppt Services
make services-restart  # Neustart Services
make services-logs     # Zeigt Service-Logs
```

**Services:** API Gateway + alle Microservices (Ping, Members, Horses, Events, Masterdata, Auth, Monitoring)

**URLs nach Start:**
- Gateway: http://localhost:8081
- Ping Service: http://localhost:8082
- Members Service: http://localhost:8083
- Horses Service: http://localhost:8084
- Events Service: http://localhost:8085
- Master Service: http://localhost:8086

#### 3. Clients Layer (Frontend)

```bash
make clients-up       # Startet Infrastruktur + Client-Apps
make clients-down     # Stoppt Clients
make clients-restart  # Neustart Clients
make clients-logs     # Zeigt Client-Logs
```

**Clients:** Web-App, Auth-Server, Monitoring-Server

**URLs nach Start:**
- Web App: http://localhost:4000
- Auth Server: http://localhost:8087
- Monitoring: http://localhost:8088

### Full System Commands

Befehle für das komplette System (alle Layer):

```bash
make full-up       # Startet ALLES (Infrastructure + Services + Clients)
make full-down     # Stoppt komplettes System
make full-restart  # Neustart komplettes System
make full-logs     # Zeigt alle System-Logs
```

**Nach `make full-up` verfügbare Services:**

```
🌐 Frontend & APIs:
   Web App:         http://localhost:4000
   API Gateway:     http://localhost:8081

🔧 Infrastructure:
   PostgresQL:      localhost:5432
   Redis:           localhost:6379
   Keycloak:        http://localhost:8180
   Consul:          http://localhost:8500
   Prometheus:      http://localhost:9090
   Grafana:         http://localhost:3000

⚙️  Microservices:
   Ping Service:    http://localhost:8082
   Members Service: http://localhost:8083
   Horses Service:  http://localhost:8084
   Events Service:  http://localhost:8085
   Master Service:  http://localhost:8086
   Auth Server:     http://localhost:8087
   Monitoring:      http://localhost:8088
```

### Build Commands

Befehle zum Bauen von Docker-Images:

```bash
make build                              # Baut alle Custom-Images
make build-service SERVICE=ping-service # Baut einzelnen Service
make build-client CLIENT=web-app        # Baut einzelnen Client
```

**Beispiele:**

```bash
# Einzelnen Service neu bauen (ohne Cache)
make build-service SERVICE=api-gateway

# Web-App neu bauen
make build-client CLIENT=web-app

# Alle Images neu bauen
make build
```

### Test Commands

Befehle für Testing:

```bash
make test       # Führt Integration-Tests aus
make test-e2e   # Führt End-to-End-Tests aus
```

**Details:**

```bash
# Integration-Tests
# - Startet automatisch Infrastruktur
# - Führt Gradle-Tests aus
# - Stoppt Infrastruktur nach Tests
make test

# E2E-Tests
# - Startet komplette Entwicklungsumgebung
# - Führt Client-Tests aus
# - Stoppt Umgebung nach Tests
make test-e2e
```

### Environment Management Commands

Befehle für Environment-Konfiguration:

```bash
make env-setup       # Zeigt Environment-Setup-Anweisungen
make env-dev         # Wechselt zu Development-Environment
make env-prod        # Wechselt zu Production-Environment
make env-staging     # Wechselt zu Staging-Environment
make env-test        # Wechselt zu Test-Environment
make validate        # Validiert Docker Compose Konfiguration
make env-template    # Erstellt .env Template-Datei
```

**Verwendung:**

```bash
# Entwicklungsumgebung aktivieren
make env-dev

# Validierung durchführen
make validate

# Neue .env-Template erstellen
make env-template
```

### SSoT (Single Source of Truth) Commands

Befehle für Docker-Versionsverwaltung:

```bash
make versions-show                      # Zeigt zentrale Versionen (docker/versions.toml)
make versions-update key=gradle value=9.1.0  # Aktualisiert eine Version
make docker-sync                        # Synchronisiert versions.toml -> build-args/*.env
make docker-compose-gen ENV=development # Generiert Docker Compose Files
make docker-validate                    # Validiert Docker SSoT Konsistenz
make hooks-install                      # Installiert Pre-Commit SSoT Guard Hook
```

**Workflow für Versions-Updates:**

```bash
# 1. Version in versions.toml aktualisieren
make versions-update key=gradle value=9.1.0

# 2. Build-Args synchronisieren
make docker-sync

# 3. Compose-Files neu generieren
make docker-compose-gen ENV=development

# 4. Konsistenz validieren
make docker-validate
```

**Verfügbare Versions-Keys:**
- `gradle` - Gradle-Version
- `java` - Java-Version
- `node` - Node.js-Version
- `nginx` - Nginx-Version
- `alpine` - Alpine Linux-Version
- `prometheus` - Prometheus-Version
- `grafana` - Grafana-Version
- `keycloak` - Keycloak-Version
- `app-version` - Anwendungsversion
- `spring-profiles-default` - Spring Default-Profile
- `spring-profiles-docker` - Spring Docker-Profile

### Production Commands

Befehle für Production-Deployment:

```bash
make prod-up       # Startet Production-Environment
make prod-down     # Stoppt Production-Environment
make prod-restart  # Neustart Production-Environment
make prod-logs     # Zeigt Production-Logs
```

**⚠️ Hinweis:** Stelle sicher, dass `.env` korrekt konfiguriert ist mit `make env-prod`

### Monitoring & Debugging Commands

Befehle für System-Monitoring und Debugging:

```bash
make status                     # Zeigt Container-Status
make health-check               # Prüft Health aller Infrastruktur-Services
make logs SERVICE=postgres      # Zeigt Logs eines spezifischen Services
make shell SERVICE=postgres     # Öffnet Shell in Container
```

**Beispiele:**

```bash
# Status aller Container
make status

# Health-Check durchführen
make health-check
# Output:
# PostgreSQL: ✅ Ready
# Redis: ✅ PONG
# Consul: ✅ Leader elected
# Keycloak: ✅ Ready

# Logs von PostgreSQL anzeigen
make logs SERVICE=postgres

# Shell im Postgres-Container öffnen
make shell SERVICE=postgres
```

### Cleanup Commands

Befehle zum Aufräumen:

```bash
make clean       # Aufräumen Docker-Ressourcen (Prune)
make clean-all   # Vollständiges Cleanup (inkl. Images und Volumes)
```

**⚠️ Warnung:** `make clean-all` löscht auch Volumes und Images!

### Development Tools Commands

```bash
make dev-tools-up     # Info: Dev-Tools wurden entfernt (verwende lokale Tools)
make dev-tools-down   # Info: Keine Dev-Tool-Container zum Stoppen
```

**Empfehlung:** Verwende lokale Tools wie pgAdmin, TablePlus, DBeaver, RedisInsight

## 🎯 AI-Assistenten: Development-Schnellreferenz

### Häufigste Workflows

#### 1. Lokale Entwicklung starten

```bash
# Variante A: Nur Infrastruktur
make dev-up
./gradlew :members:members-service:bootRun

# Variante B: Komplettes Backend
make services-up

# Variante C: Alles inkl. Frontend
make full-up
```

#### 2. Service neu bauen nach Code-Änderungen

```bash
# Service stoppen
docker compose down api-gateway

# Service neu bauen
make build-service SERVICE=api-gateway

# Service starten
make services-up
```

#### 3. Debugging eines Services

```bash
# Logs in Echtzeit
make logs SERVICE=ping-service

# Shell im Container öffnen
make shell SERVICE=ping-service

# Health-Status prüfen
curl -s http://localhost:8082/actuator/health | jq
```

#### 4. Docker-Versionen aktualisieren

```bash
# Gradle-Version ändern
make versions-update key=gradle value=9.1.0

# Änderungen synchronisieren
make docker-sync
make docker-compose-gen
make docker-validate

# Git-Status prüfen (sollte generierte Files zeigen)
git status
```

#### 5. Tests ausführen

```bash
# Integration-Tests (automatische Infrastruktur)
make test

# E2E-Tests (automatische Full-Environment)
make test-e2e

# Einzelner Test via Gradle
./gradlew :members:members-service:test
```

### Development-URLs Übersicht

| Service         | URL                           | Credentials       |
|-----------------|-------------------------------|-------------------|
| Web App         | http://localhost:4000         | -                 |
| API Gateway     | http://localhost:8081         | -                 |
| Ping Service    | http://localhost:8082         | -                 |
| Members Service | http://localhost:8083         | -                 |
| Horses Service  | http://localhost:8084         | -                 |
| Events Service  | http://localhost:8085         | -                 |
| Master Service  | http://localhost:8086         | -                 |
| Auth Server     | http://localhost:8087         | -                 |
| Monitoring      | http://localhost:8088         | -                 |
| Keycloak Admin  | http://localhost:8180         | admin/admin       |
| Consul UI       | http://localhost:8500         | -                 |
| Prometheus      | http://localhost:9090         | -                 |
| Grafana         | http://localhost:3000         | admin/admin       |
| PostgreSQL      | localhost:5432                | meldestelle/***   |
| Redis           | localhost:6379                | -                 |

### Health-Check Endpoints

```bash
# API Gateway
curl -s http://localhost:8081/actuator/health | jq

# Ping Service
curl -s http://localhost:8082/actuator/health | jq

# Members Service
curl -s http://localhost:8083/actuator/health | jq

# Keycloak
curl -s http://localhost:8180/health/ready | jq

# Consul
curl -s http://localhost:8500/v1/status/leader
```

### Debug-Ports

**Spring Boot Services:**
- Debug-Port: 5005 (Standard Java Debug Protocol)
- Konfiguration in docker-compose.services.yml

**Client-Apps:**
- Web-App: Hot-Reload über Volume-Mapping
- Desktop-App: VNC Port 5901, VNC Web Port 6080

### Troubleshooting Development

#### Container startet nicht

```bash
# 1. Status prüfen
make status

# 2. Logs anzeigen
make logs SERVICE=<service-name>

# 3. Container neu starten
docker compose restart <service-name>

# 4. Image neu bauen (ohne Cache)
make build-service SERVICE=<service-name>
docker compose up -d <service-name>
```

#### Port-Konflikte

```bash
# Ports prüfen
lsof -i :<port>
# oder
netstat -tulpn | grep :<port>

# Konfigurierten Port in .env ändern
# z.B. API_GATEWAY_PORT=8081 -> API_GATEWAY_PORT=8091

# Services neu starten
make services-restart
```

#### Health-Check schlägt fehl

```bash
# 1. Service-Status prüfen
make status

# 2. Service-Logs prüfen
make logs SERVICE=<service-name>

# 3. Manueller Health-Check
curl -v http://localhost:<port>/actuator/health

# 4. Container-Netzwerk prüfen
docker network inspect meldestelle-network

# 5. Service neu starten
docker compose restart <service-name>
```

#### Volume-Probleme

```bash
# Volumes anzeigen
docker volume ls | grep meldestelle

# Volume-Inhalt prüfen
make shell SERVICE=postgres
ls -la /var/lib/postgresql/data

# Volume entfernen (⚠️ Daten gehen verloren!)
docker volume rm meldestelle_postgres-data

# Volumes neu erstellen
make full-down
make full-up
```

#### Datenbankverbindung fehlgeschlagen

```bash
# 1. PostgreSQL-Status prüfen
make health-check

# 2. PostgreSQL-Logs prüfen
make logs SERVICE=postgres

# 3. Verbindung testen (aus anderem Container)
docker compose exec api-gateway sh
apk add postgresql-client
psql -h postgres -U meldestelle -d meldestelle

# 4. Secrets prüfen (falls verwendet)
ls -la ./docker/secrets/
```

#### Gradle-Build schlägt fehl im Container

```bash
# 1. Gradle-Version in versions.toml prüfen
cat docker/versions.toml | grep gradle

# 2. Verfügbare Gradle-Images prüfen
docker search gradle | grep alpine

# 3. Build-Logs detailliert anzeigen
make logs SERVICE=<service-name>

# 4. Manuell im Container debuggen
make shell SERVICE=<service-name>
./gradlew --version
./gradlew dependencies
```

#### Service ist erreichbar, antwortet aber nicht

```bash
# 1. Service-Logs in Echtzeit
make logs SERVICE=<service-name>

# 2. JVM-Status prüfen (bei Java-Services)
make shell SERVICE=<service-name>
ps aux | grep java

# 3. Speicher/CPU prüfen
docker stats <container-name>

# 4. Netzwerk-Verbindung testen
docker compose exec <service> sh
apk add curl
curl -v http://api-gateway:8081/actuator/health
```

## 🤖 AI-Assistant Best Practices

### Für Code-Assistenten

1. **Verwende immer `make help`** um verfügbare Befehle zu sehen
2. **Befehlsnamen korrekt verwenden:**
   - ✅ `make build-service SERVICE=ping-service`
   - ❌ `make service-build SERVICE=ping-service` (veraltet)
3. **Port-Angaben beachten:**
   - API Gateway: Port 8081 (nicht 8080!)
   - Alle Ports in `.env` oder `docker-compose*.yml` definiert
4. **SSoT-Workflow einhalten:**
   - Versionen nur in `docker/versions.toml` ändern
   - `make docker-sync` nach Änderungen
   - `make docker-validate` vor Commits

### Für Entwickler-Support

1. **Troubleshooting-Reihenfolge:**
   - `make status` → Container-Status
   - `make health-check` → Service-Health
   - `make logs SERVICE=<name>` → Logs prüfen
   - `make shell SERVICE=<name>` → Interactive Debugging

2. **Häufige Fehlerquellen:**
   - Fehlende `.env` Datei → `make env-dev`
   - Port-Konflikte → `lsof -i :<port>`
   - Veraltete Images → `make build`
   - Volume-Berechtigungen → `make clean-all` (⚠️ Daten-Verlust!)

3. **Performance-Optimierung:**
   - Nur benötigte Layer starten (infrastructure/services/clients)
   - Docker BuildKit aktivieren: `export DOCKER_BUILDKIT=1`
   - Gradle-Cache nutzen (bereits in Dockerfiles konfiguriert)

## 📦 Docker Compose Dateien

Das Projekt verwendet mehrere Compose-Files:

- **docker-compose.yml** - Infrastruktur-Layer (Postgres, Redis, Keycloak, Consul, etc.)
- **docker-compose.services.yml** - Services-Layer (API Gateway, Microservices)
- **docker-compose.clients.yml** - Clients-Layer (Web-App, Desktop-App)

**Kombinationen:**

```bash
# Nur Infrastruktur
docker compose -f docker-compose.yml up -d

# Infrastruktur + Services
docker compose -f docker-compose.yml -f docker-compose.services.yml up -d

# Infrastruktur + Clients
docker compose -f docker-compose.yml -f docker-compose.clients.yml up -d

# Alles
docker compose -f docker-compose.yml -f docker-compose.services.yml -f docker-compose.clients.yml up -d

# ⚠️ Tipp: Verwende stattdessen die Makefile-Befehle!
```

## 🔄 Typische Entwicklungs-Workflows

### Workflow 1: Neues Feature entwickeln

```bash
# 1. Frische Umgebung starten
make full-down
make dev-up

# 2. Feature in IDE entwickeln
# (Service läuft lokal via Gradle, nicht in Docker)

# 3. Tests lokal ausführen
./gradlew :members:members-service:test

# 4. Service als Container testen
make build-service SERVICE=members-service
make services-up

# 5. Integration-Tests
make test

# 6. Aufräumen
make dev-down
```

### Workflow 2: Bug-Fixing

```bash
# 1. System starten
make full-up

# 2. Logs beobachten
make logs SERVICE=<betroffener-service>

# 3. Shell im Container öffnen (falls nötig)
make shell SERVICE=<betroffener-service>

# 4. Fix implementieren und Service neu bauen
make build-service SERVICE=<betroffener-service>
docker compose restart <betroffener-service>

# 5. Fix verifizieren
curl http://localhost:<port>/actuator/health
make test
```

### Workflow 3: Docker-Version aktualisieren

```bash
# 1. Aktuelle Versionen anzeigen
make versions-show

# 2. Version aktualisieren (z.B. Java)
make versions-update key=java value=22

# 3. Build-Args synchronisieren
make docker-sync

# 4. Compose-Files neu generieren
make docker-compose-gen ENV=development

# 5. Konsistenz validieren
make docker-validate

# 6. Testen
make clean-all  # ⚠️ Entfernt Volumes!
make full-up
make health-check
```

### Workflow 4: Kompletter System-Reset

```bash
# 1. Alles stoppen
make full-down

# 2. Alle Docker-Ressourcen entfernen
make clean-all  # ⚠️ Entfernt auch Volumes und Images!

# 3. Images neu bauen
make build

# 4. System neu starten
make full-up

# 5. Health-Check
make health-check

# 6. Logs prüfen
make full-logs
```

---

**Navigation:**
- [Docker-Overview](./docker-overview.md) - Grundlagen und Philosophie
- [Docker-Architecture](./docker-architecture.md) - Container-Services und Struktur
- [Docker-Production](./docker-production.md) - Production-Deployment
- [Docker-Monitoring](./docker-monitoring.md) - Observability
- [Docker-Troubleshooting](./docker-troubleshooting.md) - Problemlösung

---

**Letzte Aktualisierung:** 2025-11-11  
**Version:** 2.0.0 - Vollständige Makefile-Referenz mit allen 50+ Befehlen
