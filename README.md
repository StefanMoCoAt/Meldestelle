# Meldestelle

> Modulares System für Pferdesportveranstaltungen mit Domain-Driven Design

[![CI Pipeline](https://github.com/your-org/meldestelle/workflows/CI%20-%20Main%20Pipeline/badge.svg)](https://github.com/your-org/meldestelle/actions)
[![Docker SSoT](https://github.com/your-org/meldestelle/workflows/Docker%20SSoT%20Guard/badge.svg)](https://github.com/your-org/meldestelle/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## 🚀 Quick Start

1. Repository klonen

```bash
   git clone https://github.com/your-org/meldestelle.git cd meldestelle
```

2. Docker-Infrastruktur starten

```bash
   docker-compose up -d
```

3. Services starten

```bash
   ./gradlew bootRun
```

**Vollständige Anleitung**: [docs/how-to/start-local.md](docs/how-to/start-local.md)

---

## 📚 Dokumentation

### Single Source of Truth: YouTrack

Die Hauptdokumentation befindet sich in der **YouTrack Wissensdatenbank**:

👉 **[Meldestelle Command Center](https://meldestelle-pro.youtrack.cloud/knowledge-bases)**

#### In YouTrack:

- 🏗️ **Bounded Context Dokumentation** (Members, Horses, Events, Masterdata)
- 📡 **API-Referenz** (automatisch aus KDoc generiert)
- 🚀 **Deployment-Guides** (Proxmox, Cloudflare, Nginx)
- 🔐 **Infrastruktur-Konfigurationen** (Netzwerk, Datenbanken, Keycloak)
- 💡 **Roadmap & Visionen**

#### Im Repository:

- [📖 docs/README.md](docs/README.md) - Übersicht aller Repository-Dokumentation
- [🏛️ Architecture Decision Records](docs/architecture/adr/)
- [📐 C4-Diagramme](docs/architecture/c4/)
- [🛠️ Developer Guides](docs/how-to/)

---

## 🏗️ Architektur

### Bounded Contexts (DDD)

Das System ist in unabhängige Domänen aufgeteilt:

- **Members**: Mitgliederverwaltung
- **Horses**: Pferderegistrierung
- **Events**: Veranstaltungsverwaltung
- **Masterdata**: Stammdaten (Länder, Altersklassen, Turnierplätze)

### Technische Architektur

- **Microservices**: Unabhängige Services mit API Gateway
- **Event-Driven**: Apache Kafka für asynchrone Kommunikation
- **Polyglot Persistence**: PostgreSQL + Redis
- **Container-First**: Docker & Docker Compose

**Details**: [ADR-0002 Domain-Driven Design](docs/architecture/adr/0002-domain-driven-design-de.md)

---

## 🛠️ Tech Stack

| Komponente     | Technologie                   | Version |
|----------------|-------------------------------|---------|
| **Backend**    | Kotlin + Spring Boot          | 3.x     |
| **JVM**        | Java                          | 21      |
| **Build**      | Gradle                        | 9.0.0   |
| **Datenbank**  | PostgreSQL                    | 16      |
| **Cache**      | Redis                         | 7       |
| **Messaging**  | Apache Kafka                  | 7.5.0   |
| **Auth**       | Keycloak                      | 26.4.2  |
| **Monitoring** | Prometheus + Grafana + Zipkin | -       |
| **Container**  | Docker + Docker Compose       | v2.0+   |

---

## 📦 Projektstruktur

Meldestelle/
├── members/ # Bounded Context: Mitgliederverwaltung
│ ├── members-api/
│ ├── members-application/
│ ├── members-domain/
│ ├── members-infrastructure/
│ └── members-service/
│ ├── horses/ # Bounded Context: Pferderegistrierung
│ └── (analog zu members)
│ ├── events/ # Bounded Context: Veranstaltungsverwaltung
│ └── (analog zu members)
│ ├── masterdata/ # Bounded Context: Stammdaten
│ └── (analog zu members)
│ ├── infrastructure/ # Technische Infrastruktur
│ ├── gateway/ # API Gateway (Spring Cloud Gateway)
│ ├── auth/ # Authentifizierung
│ ├── cache/ # Caching (Redis)
│ ├── messaging/ # Kafka-Integration
│ └── monitoring/ # Observability
│ ├── core/ # Gemeinsame Kern-Komponenten
│ ├── core-domain/
│ └── core-utils/
│ ├── client/ # Client-Anwendungen
│ ├── web-app/
│ └── desktop-app/
│ └── docs/ # Minimale Entwickler-Dokumentation

---

## 🔒 Docker Single Source of Truth (SSoT)

Alle Versionen zentral in **`docker/versions.toml`**:

### Schnellstart

## Versionen anzeigen

```bash
 # DOCKER_SSOT_MODE=envless 
 bash scripts/docker-build.sh --versions
```

## Compose-Files generieren

```bash
bash scripts/generate-compose-files.sh all development
```

## Konsistenz validieren

```bash
bash scripts/validate-docker-consistency.sh all
```

### Zwei Betriebsmodi

#### 1. Kompatibilitätsmodus (compat)

```bash
 bash scripts/docker-versions-update.sh sync 
 # all development
 bash scripts/generate-compose-files.sh
 # all
 bash scripts/validate-docker-consistency.sh
```

#### 2. Env-less Modus (empfohlen)

DOCKER_SSOT_MODE=envless

# TODO

**Details**: Siehe Abschnitt "Docker Single Source of Truth (SSoT)" weiter unten

---

## 🧪 Testing

### Unit Tests

```bash
 ./gradlew test
```

### Integration Tests

```bash
 ./gradlew integrationTest
```

### Spezifisches Modul testen

```bash
 ./gradlew :members:members-service:test
```

---

## 🚢 Deployment

### Lokale Entwicklung

#### Nur Infrastruktur (Postgres, Redis, Kafka, Keycloak)

```bash
 docker-compose up -d
```

#### Services über Gradle

```bash
 ./gradlew bootRun
```

### Proxmox Produktion

Siehe: [docs/how-to/deploy-proxmox-nginx.md](docs/how-to/deploy-proxmox-nginx.md) (oder YouTrack für Details)

---

## Docker Single Source of Truth (SSoT) - Details

Dieser Abschnitt beschreibt den lokalen Workflow für die zentrale Docker-Versionsverwaltung.

### TL;DR – Zwei Betriebsmodi

- **Kompatibilitätsmodus (Standard)**: `build-args/*.env` werden aus `versions.toml` generiert
```bash
  bash scripts/docker-versions-update.sh sync
  bash scripts/generate-compose-files.sh all development
  bash scripts/validate-docker-consistency.sh all
```

- **Env-less Modus (Empfohlen)**: Keine `build-args/*.env` nötig – direkter Export aus `versions.toml`
```bash
  DOCKER_SSOT_MODE=envless bash scripts/docker-build.sh --versions
  DOCKER_SSOT_MODE=envless bash scripts/generate-compose-files.sh all development
  DOCKER_SSOT_MODE=envless bash scripts/validate-docker-consistency.sh all
```

### Makefile-Shortcuts

```bash
 make docker-sync # Kompatibilitätsmodus: Sync 
 make docker-compose-gen # Compose-Files generieren 
 make docker-validate # Validierung
```

### Was ist die Single Source of Truth?

- **`docker/versions.toml`** enthält alle Versionsangaben (Gradle, Java, Node, Nginx, Postgres, Redis, etc.)
- **Env-less**: `docker/build-args/*.env` sind optional; Variablen zur Laufzeit aus `versions.toml`
- **docker-compose*.yml** werden generiert und referenzieren nur zentrale `DOCKER_*`-Variablen
- **Dockerfiles** deklarieren ARGs ohne Default-Werte

### Versionen ändern

```bash
 bash scripts/docker-versions-update.sh update gradle 9.1.0 
 bash scripts/docker-versions-update.sh update node 22.21.0 
 bash scripts/docker-versions-update.sh update postgres 16-alpine
```

Danach: `generate` + `validate` ausführen!

### CI-Schutz

Die CI validiert Docker SSoT in beiden Modi (Matrix: compat + envless).

**Lokal reproduzieren**:
#### Compat
```bash
 bash scripts/docker-versions-update.sh sync 
 bash scripts/generate-compose-files.sh all development 
 bash scripts/validate-docker-consistency.sh all git diff --name-only # sollte leer sein
```

#### Env-less
DOCKER_SSOT_MODE=envless
```bash
 bash scripts/generate-compose-files.sh all development DOCKER_SSOT_MODE=envless 
 bash scripts/validate-docker-consistency.sh all git diff --name-only # sollte leer sein
```

---

## 🔄 Automatisierte Workflows

| Workflow | Zweck | Trigger |
|----------|-------|---------|
| [ci-main.yml](.github/workflows/ci-main.yml) | Build, Test, OpenAPI-Lint, Docs-Lint | Push/PR |
| [ssot-guard.yml](.github/workflows/ssot-guard.yml) | Docker SSoT Validierung | Push/PR |
| [docs-kdoc-sync.yml](.github/workflows/docs-kdoc-sync.yml) | KDoc → YouTrack Sync | workflow_dispatch |
| [integration-tests.yml](.github/workflows/integration-tests.yml) | Integration Tests | Push/PR |
| [deploy-proxmox.yml](.github/workflows/deploy-proxmox.yml) | Deployment zu Proxmox | workflow_dispatch |

---

## 📜 Lizenz

[MIT License](LICENSE)

---

## 🤝 Contributing

Bitte lies [docs/how-to/branchschutz-und-pr-workflow.md](docs/how-to/branchschutz-und-pr-workflow.md) für den PR-Workflow.

---

## 📞 Support & Kontakt

- **Bugs**: [GitHub Issues](https://github.com/your-org/meldestelle/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-org/meldestelle/discussions)
- **Dokumentation**: [YouTrack Wissensdatenbank](https://meldestelle-pro.youtrack.cloud/knowledge-bases)

---

**Version**: 2.0.0 (nach Dokumentations-Refactoring)  
**Letzte Aktualisierung**: 30. Oktober 2025
