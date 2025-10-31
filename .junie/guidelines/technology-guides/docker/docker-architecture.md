# Docker-Architektur und Services

---

guideline_type: "technology"
scope: "docker-architecture"
audience: ["developers", "ai-assistants", "devops"]
last_updated: "2025-09-15"
dependencies: ["docker-overview.md", "master-guideline.md"]
related_files: ["docker-compose.yml", "docker/versions.toml", "scripts/docker-versions-update.sh"]
ai_context: "Docker-Container-Architektur, Service-Definitionen und zentrale Versionsverwaltung"

---

## 🏗️ Architektur-Überblick

> **📖 Hinweis:** Für einen allgemeinen Überblick über die Docker-Infrastruktur siehe [docker-overview](docker-overview.md).

### Container-Kategorien

```plantuml
graph TB
    subgraph "Infrastructure Services"
        PG[PostgresQL]
        RD[Redis]
        KC[Keycloak]
        KF[Kafka+Zookeeper]
        CS[Consul]
    end

    subgraph "Application Services"
        GW[API Gateway]
        AS[Auth Server]
        MS[Monitoring Server]
        PS[Ping Service]
    end

    subgraph "Client Applications"
        WA[Web App]
        DA[Desktop App - Native]
    end

    subgraph "Monitoring Stack"
        PR[Prometheus]
        GR[Grafana]
        ZK[Zipkin]
        NX[Nginx - Prod]
    end

    Infrastructure --> Application
    Application --> Client
    Monitoring --> Infrastructure
    Monitoring --> Application
```

### Service-Ports Matrix

| Service           | Development | Production   | Health Check                                          | Debug Port | Version     |
|-------------------|-------------|--------------|-------------------------------------------------------|------------|-------------|
| PostgreSQL        | 5432        | Internal     | pg_isready -U meldestelle -d meldestelle              | -          | 16-alpine   |
| Redis             | 6379        | Internal     | redis-cli ping                                        | -          | 7-alpine    |
| Keycloak          | 8180        | 8443 (HTTPS) | /health/ready                                         | -          | 26.0.7      |
| Kafka             | 9092        | Internal     | kafka-topics --bootstrap-server localhost:9092 --list | -          | 7.4.0       |
| Zookeeper         | 2181        | Internal     | nc -z localhost 2181                                  | -          | 7.4.0       |
| Consul            | 8500        | Internal     | /v1/status/leader                                     | -          | 1.15        |
| Auth Server       | 8081        | Internal     | /actuator/health/readiness                            | 5005       | 1.0.0       |
| Ping Service      | 8082        | Internal     | /actuator/health/readiness                            | 5005       | 1.0.0       |
| Monitoring Server | 8083        | Internal     | /actuator/health/readiness                            | 5005       | 1.0.0       |
| Prometheus        | 9090        | Internal     | /-/healthy                                            | -          | v2.54.1     |
| Grafana           | 3000        | 3443 (HTTPS) | /api/health                                           | -          | 11.3.0      |
| Nginx             | -           | 80/443       | /health                                               | -          | 1.25-alpine |

## 🎯 Zentrale Docker-Versionsverwaltung

> **🤖 AI-Assistant Hinweis:**
> Das Versionssystem folgt dem Single Source of Truth Prinzip:
> - **Zentrale Datei:** `docker/versions.toml` definiert alle Versionen
> - **Build-Args:** Automatisch generierte `.env`-Dateien in `docker/build-args/`
> - **Updates:** Via `./scripts/docker-versions-update.sh`

### Überblick und Motivation

**Version 3.0.0** führt eine revolutionäre Änderung in der Docker-Versionsverwaltung ein: die **zentrale Verwaltung aller Build-Argumente** analog zum bewährten `gradle/libs.versions.toml` System.

#### Das Problem vor Version 3.0.0

```dockerfile
# BEFORE: Redundante Hardcodierung in 12+ Dockerfiles
ARG GRADLE_VERSION=9.0.0
ARG GRADLE_VERSION=9.0.0
ARG GRADLE_VERSION=9.0.0
# ... 9 weitere Male identisch wiederholt!
```

#### Die Lösung: Single Source of Truth

```toml
# docker/versions.toml - SINGLE SOURCE OF TRUTH
[versions]
gradle = "9.1.0"
java = "21"
node = "22.21.0"
nginx = "1.28.0-alpine"
prometheus = "v2.54.1"
grafana = "11.3.0"
keycloak = "26.4.2"
```

### 🏗️ Architektur der zentralen Versionsverwaltung

```plaintext
docker/
├── versions.toml                    # 🎯 Single Source of Truth
├── build-args/                     # Auto-generierte Environment Files
│   ├── global.env                  # Globale Build-Argumente
│   ├── services.env                # dockerfiles/services/*
│   ├── clients.env                 # dockerfiles/clients/*
│   └── infrastructure.env          # dockerfiles/infrastructure/*
└── README.md                       # Dokumentation
```

### 📊 Hierarchische Versionsverwaltung

#### 1. **Globale Versionen** (`docker/build-args/global.env`)

Verwendet von **allen** Dockerfiles:
```bash
# --- Build Tools ---
GRADLE_VERSION=9.1.0
JAVA_VERSION=21

# --- Build Metadata ---
BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
VERSION=1.0.0

# --- Common Base Images ---
ALPINE_VERSION=3.19
ECLIPSE_TEMURIN_JDK_VERSION=21-jdk-alpine
ECLIPSE_TEMURIN_JRE_VERSION=21-jre-alpine

# --- Monitoring & Infrastructure Services ---
DOCKER_PROMETHEUS_VERSION=v2.54.1
DOCKER_GRAFANA_VERSION=11.3.0
DOCKER_KEYCLOAK_VERSION=26.4.2
```

#### 2. **Kategorie-spezifische Versionen**

**Services** (`docker/build-args/services.env`):
```bash
SPRING_PROFILES_ACTIVE=docker
SERVICE_PORT=8080
PING_SERVICE_PORT=8082
MEMBERS_SERVICE_PORT=8083
```

**Clients** (`docker/build-args/clients.env`):
```bash
NODE_VERSION=22.21.0
NGINX_VERSION=1.28.0-alpine
WEB_APP_PORT=4000
DESKTOP_APP_VNC_PORT=5901
```

**Infrastructure** (`docker/build-args/infrastructure.env`):
```bash
SPRING_PROFILES_ACTIVE=default
GATEWAY_PORT=8081
AUTH_SERVER_PORT=8087
```

### 🛠️ Verwendung der zentralen Versionsverwaltung

#### Automatisierte Builds mit `scripts/docker-build.sh`

```bash
# Alle Services mit zentralen Versionen bauen
./scripts/docker-build.sh services

# Client-Anwendungen bauen
./scripts/docker-build.sh clients

# Komplettes System bauen
./scripts/docker-build.sh all

# Aktuelle Versionen anzeigen
./scripts/docker-build.sh --versions
```

#### Versionen aktualisieren mit `scripts/docker-versions-update.sh`

```bash
# Aktuelle Versionen anzeigen
./scripts/docker-versions-update.sh show

# Java auf Version 22 upgraden
./scripts/docker-versions-update.sh update java 22

# Gradle auf 9.1.0 upgraden
./scripts/docker-versions-update.sh update gradle 9.1.0

# Prometheus auf neueste Version upgraden
./scripts/docker-versions-update.sh update prometheus v2.54.1

# Grafana auf neueste Version upgraden
./scripts/docker-versions-update.sh update grafana 11.3.0

# Keycloak auf neueste Version upgraden
./scripts/docker-versions-update.sh update keycloak 26.4.2

# Alle Environment-Dateien synchronisieren
./scripts/docker-versions-update.sh sync
```

## 🎯 Für AI-Assistenten: Architektur-Schnellreferenz

### Service-Kategorien
- **Infrastructure:** PostgresQL, Redis, Keycloak, Kafka, Zookeeper, Consul
- **Application:** API Gateway, Auth Server, Monitoring Server, Ping Service
- **Clients:** Web App (Port 3000), Desktop App
- **Monitoring:** Prometheus (9090), Grafana (3000), Zipkin, Nginx

### Wichtige Befehle

```bash
# Service-Status prüfen
docker-compose ps

# Logs eines Services anzeigen
docker-compose logs <service-name>

# Versionen aktualisieren
./scripts/docker-versions-update.sh show
./scripts/docker-versions-update.sh update <component> <version>

# Services neu starten
docker-compose restart <service-name>
```

### Zentrale Konfigurationsdateien

- `docker/versions.toml` - Alle Versionen
- `docker-compose.yml` - Haupt-Services
- `docker-compose.clients.yml` - Client-Anwendungen
- `docker/build-args/*.env` - Generierte Build-Argumente

---

**Navigation:**
- [master-guideline](../../master-guideline.md) – Hauptrichtlinie für das Projekt
- [Docker-Overview](./docker-overview.md) - Grundlagen und Philosophie
- [Docker-Development](./docker-development.md) - Entwicklungsworkflow
- [Docker-Production](./docker-production.md) - Production-Deployment
- [Docker-Monitoring](./docker-monitoring.md) - Observability
- [Docker-Troubleshooting](./docker-troubleshooting.md) - Problemlösung
