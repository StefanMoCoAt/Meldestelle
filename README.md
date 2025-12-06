# Meldestelle

> Modulares System für Pferdesportveranstaltungen mit Domain-Driven Design

[![CI Pipeline](https://github.com/StefanMoCoAt/meldestelle/workflows/CI%20-%20Main%20Pipeline/badge.svg)](https://github.com/StefanMoCoAt/meldestelle/actions)
[![Docker SSoT](https://github.com/StefanMoCoAt/meldestelle/workflows/Docker%20SSoT%20Guard/badge.svg)](https://github.com/StefanMoCoAt/meldestelle/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## 🚀 Quick Start

```bash
# 1) Repository klonen
git clone https://github.com/StefanMoCoAt/meldestelle.git
cd meldestelle

# 2) Runtime-Environment vorbereiten (Single Source of Truth)
#   Kopiere die Vorlage und passe sie bei Bedarf an.
cp -n .env.template config/env/.env 2>/dev/null || true
#   Optionale lokale Geheimnisse/Overrides (gitignored):
#   echo "POSTGRES_PASSWORD=meinlokalespasswort" >> config/env/.env.local

# 3) (Optional) Compose-Files generieren
#    (nur falls du die Generator-Pipeline nutzt)
# DOCKER_SSOT_MODE=envless bash scripts/generate-compose-files.sh all development

# 4) Infrastruktur starten
docker compose -f docker-compose.yaml up -d

# 5) Services starten (Beispiel)
./gradlew :members:members-service:bootRun
# oder – falls zentral gewollt und unterstützt
# ./gradlew bootRun
```

**Vollständige Anleitung**: [docs/how-to/start-local.md](docs/how-to/start-local.md)

---

## 📚 Dokumentation

### Single Source of Truth: YouTrack

Die Hauptdokumentation befindet sich in der **YouTrack Wissensdatenbank**:

👉 **[Meldestelle Command Center](https://meldestelle-pro.youtrack.cloud/articles/MP-A-24)**

#### In YouTrack

- 🏗️ **Bounded Context Dokumentation** (Members, Horses, Events, Masterdata)
- 📡 **API-Referenz** (automatisch aus KDoc generiert)
- 🚀 **Deployment-Guides** (Proxmox, Cloudflare, Nginx)
- 🔐 **Infrastruktur-Konfigurationen** (Netzwerk, Datenbanken, Keycloak)
- 💡 **Roadmap & Visionen**

#### Im Repository

- [📖 docs/README.md](docs/README.md) - Übersicht aller Repository-Dokumentation
- [🏛️ Architecture Decision Records](docs/architecture/adr)
- [📐 C4-Diagramme](docs/architecture/c4)
- [🛠️ Developer Guides](docs/how-to)
- [📑 Projekt-Guidelines (Master)](.junie/guidelines/master-guideline.md)

Zusätzliche zentrale Guidelines:

- [Coding Standards](.junie/guidelines/project-standards/coding-standards.md)
- [Testing Standards](.junie/guidelines/project-standards/testing-standards.md)
- [Documentation Standards](.junie/guidelines/project-standards/documentation-standards.md)
- [Architecture Principles](.junie/guidelines/project-standards/architecture-principles.md)
- [Web App Guideline](.junie/guidelines/technology-guides/web-app-guideline.md)
- Docker Guides:
  - [Docker Overview](.junie/guidelines/technology-guides/docker/docker-overview.md)
  - [Docker Architecture](.junie/guidelines/technology-guides/docker/docker-architecture.md)
  - [Docker Development](.junie/guidelines/technology-guides/docker/docker-development.md)
  - [Docker Production](.junie/guidelines/technology-guides/docker/docker-production.md)
  - [Docker Monitoring](.junie/guidelines/technology-guides/docker/docker-monitoring.md)
  - [Docker Troubleshooting](.junie/guidelines/technology-guides/docker/docker-troubleshooting.md)
- Process Guide: [Trace Bullet](.junie/guidelines/process-guides/trace-bullet-guideline.md)

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

## ⚙️ Konfigurationsstruktur (Build vs. Runtime)

Laufzeit (Runtime) – Single Source of Truth:

- config/env/.env – globale Runtime-Werte (Ports, Hosts, Feature-Flags, Pfade, Profile)
- config/env/.env.local – lokale, geheime Overrides (gitignored)
- Optionale DDD-Slice-Overrides (nur wenn nötig):
  - config/env/services/<service>.env (z. B. ping-service.env)
  - config/env/infrastructure/<component>.env (z. B. api-gateway.env)
  - config/env/clients/<client>.env (z. B. web-app.env)

Build-Zeit (nur Versionen/Tags/Pfade):

- docker/versions.toml – zentrale Versionsquelle (SSoT)
- docker/build-args/global.env – aus versions.toml abgeleitet (kann via scripts/generate-build-env.sh erzeugt werden)
- docker/build-args/{clients,infrastructure,services}.env – nur Build-relevante Pfade/Namen; keine Runtime-Variablen

Compose-Anbindung:

- Alle docker-compose*.yml laden config/env/.env und optional die per-Slice-Overrides via env_file
- Laufzeitwerte werden nicht via build.args eingeschleust

Deprecations / Umbenennungen:

- `DOCKER_*_VERSION` → `*_IMAGE_TAG` (nur Build-Zeit)
- `APP_VERSION` wurde vereinheitlicht als `VERSION`

Schnelltest / Smoke (lokal):

- docker compose -f docker-compose.yml up -d
- docker compose -f docker-compose.services.yml up -d
- docker compose -f docker-compose.clients.yml up -d
- Healthchecks prüfen: <http://localhost:3000> (Grafana), <http://localhost:9090> (Prometheus), <http://localhost:8180> (Keycloak), <http://localhost:8081> (Gateway), <http://localhost:4000> (Web)

Sicherheits-Hinweise:

- Keine echten Secrets im Repo; verwende config/env/.env.local für lokale Entwicklung
- Die optimierten Compose-Dateien (`*.optimized`) nutzen Docker-Secrets im Profil "prod"

---

## 🛠️ Tech Stack

| Komponente     | Technologie                   | Version |
|----------------|-------------------------------|---------|
| **Backend**    | Kotlin + Spring Boot          | 3.x     |
| **JVM**        | Java                          | 21      |
| **Build**      | Gradle                        | 9.1.0   |
| **Datenbank**  | PostgreSQL                    | 16      |
| **Cache**      | Redis                         | 7       |
| **Messaging**  | Apache Kafka                  | 7.4.0   |
| **Auth**       | Keycloak                      | 26.4.2  |
| **Monitoring** | Prometheus + Grafana + Zipkin | -       |
| **Container**  | Docker + Docker Compose       | v2.0+   |

---

### 📦 Projektstruktur

```plaintext
Meldestelle/
├── 🗂️ client/                 # Client-Anwendungen
│   ├── desktop-app/
│   └── web-app/
├── 🗂️ core/                   # Gemeinsame Kern-Komponenten
│   ├── core-domain/
│   └── core-utils/
├── 🗂️ docs/                   # Minimale Entwickler-Dokumentation
│   ├── architecture/
│   └── how-to/
├── 🗂️ events/                 # Bounded Context: Veranstaltungsverwaltung
│   └── (analog zu members)
├── 🗂️ horses/                 # Bounded Context: Pferderegistrierung
│   └── (analog zu members)
├── 🗂️ infrastructure/         # Technische Infrastruktur
│   ├── auth/                  # Authentifizierung
│   ├── cache/                 # Caching (Redis)
│   ├── gateway/               # API Gateway (Spring Cloud Gateway)
│   ├── messaging/             # Kafka-Integration
│   └── monitoring/            # Observability
├── 🗂️ masterdata/             # Bounded Context: Stammdaten
│   └── (analog zu members)
└── 🗂️ members/                # Bounded Context: Mitgliederverwaltung
    ├── members-api/
    ├── members-application/
    ├── members-domain/
    ├── members-infrastructure/
    └── members-service/
```

---

## 🔒 Docker Single Source of Truth (SSoT)

Alle Versionen zentral in **`docker/versions.toml`**:

### SSoT – Schnellstart (präzisiert)

```bash
# Versionen anzeigen
bash scripts/docker-build.sh --versions

# Compose-Files generieren (Kompatibilitätsmodus)
bash scripts/generate-compose-files.sh all development

# Konsistenz validieren (Kompatibilitätsmodus)
bash scripts/validate-docker-consistency.sh all
```

### SSoT – Zwei Betriebsmodi (konsistent)

```bash
# 1) Kompatibilitätsmodus (compat)
bash scripts/docker-versions-update.sh sync
bash scripts/generate-compose-files.sh all development
bash scripts/validate-docker-consistency.sh all

# 2) Env-less Modus (empfohlen)
DOCKER_SSOT_MODE=envless bash scripts/docker-build.sh --versions
DOCKER_SSOT_MODE=envless bash scripts/generate-compose-files.sh all development
DOCKER_SSOT_MODE=envless bash scripts/validate-docker-consistency.sh all
```

Alternative (persistente Shell-Variante):

```bash
export DOCKER_SSOT_MODE=envless
bash scripts/docker-build.sh --versions
bash scripts/generate-compose-files.sh all development
bash scripts/validate-docker-consistency.sh all
```

#### CI-Schutz – lokal reproduzieren (getrennte/verkettete Befehle)

```bash
# Compat
bash scripts/docker-versions-update.sh sync && \
  bash scripts/generate-compose-files.sh all development && \
  bash scripts/validate-docker-consistency.sh all && \
  git diff --name-only  # sollte leer sein

# Env-less (Variante A: prefix)
DOCKER_SSOT_MODE=envless bash scripts/generate-compose-files.sh all development && \
  DOCKER_SSOT_MODE=envless bash scripts/validate-docker-consistency.sh all && \
  git diff --name-only  # sollte leer sein

# Env-less (Variante B: export)
export DOCKER_SSOT_MODE=envless
bash scripts/generate-compose-files.sh all development && \
  bash scripts/validate-docker-consistency.sh all && \
  git diff --name-only  # sollte leer sein
```

### Deployment (klarstellen, falls SSoT vorausgeht)

```bash
# Nur Infrastruktur
# Wenn eine handgeschriebene docker-compose.yaml existiert:
docker compose -f docker-compose.yaml up -d
# Falls Compose-Files generiert werden:
docker compose -f docker-compose.services.yaml up -d

# Services via Gradle
a) Einzeldienst
./gradlew :members:members-service:bootRun
b) Falls unterstützt: alle (oder Aggregator)
./gradlew bootRun
```

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
 docker compose -f docker-compose.yaml up -d
```

#### Services über Gradle

```bash
 ./gradlew bootRun
```

---

## Docker Single Source of Truth (SSoT)—Details

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

### Makefile-Befehle

Das Projekt verwendet ein umfassendes Makefile mit ~50 Befehlen für alle Development-Workflows:

```bash
make help  # Zeigt alle verfügbaren Befehle
```

**Wichtigste Befehle:**

```bash
make full-up         # Startet komplettes System (Infra + Services + Clients)
make services-up     # Startet Backend (Infra + Microservices)
make dev-up          # Startet Development-Environment
make test            # Führt Integration-Tests aus
make health-check    # Prüft System-Health
```

**SSoT-Befehle:**

```bash
make docker-sync         # Synchronisiert versions.toml -> build-args/*.env
make docker-compose-gen  # Generiert Docker Compose Files
make docker-validate     # Validiert Docker SSoT Konsistenz
```

**Vollständige Referenz:** [Docker Development Guide](.junie/guidelines/technology-guides/docker/docker-development.md#-vollständige-makefile-referenz)

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
bash scripts/docker-versions-update.sh sync && \
  bash scripts/generate-compose-files.sh all development && \
  bash scripts/validate-docker-consistency.sh all && \
  git diff --name-only  # sollte leer sein
```

#### Env-less

```bash
DOCKER_SSOT_MODE=envless bash scripts/generate-compose-files.sh all development && \
  DOCKER_SSOT_MODE=envless bash scripts/validate-docker-consistency.sh all && \
  git diff --name-only  # sollte leer sein
```

---

## 🔄 Automatisierte Workflows

| Workflow                                                         | Zweck                                | Trigger           |
|------------------------------------------------------------------|--------------------------------------|-------------------|
| [ci-main.yml](.github/workflows/ci-main.yml)                     | Build, Test, OpenAPI-Lint, Docs-Lint | Push/PR           |
| [ssot-guard.yml](.github/workflows/ssot-guard.yml)               | Docker SSoT Validierung              | Push/PR           |
| [docs-kdoc-sync.yml](.github/workflows/docs-kdoc-sync.yml)       | KDoc → YouTrack Sync                 | workflow_dispatch |
| [integration-tests.yml](.github/workflows/integration-tests.yml) | Integration Tests                    | Push/PR           |
| [deploy-proxmox.yml](.github/workflows/deploy-proxmox.yml)       | Deployment zu Proxmox                | workflow_dispatch |

---

## 📜 Lizenz

[MIT License](LICENSE)

---

## 🤝 Contributing

Bitte lies [docs/how-to/branchschutz-und-pr-workflow.md](docs/how-to/branchschutz-und-pr-workflow.md) für den
PR-Workflow.

---

## 📞 Support & Kontakt

- **Bugs**: [GitHub Issues](https://github.com/StefanMoCoAt/meldestelle/issues)
- **Discussions**: [GitHub Discussions](https://github.com/StefanMoCoAt/meldestelle/discussions)
- **Dokumentation**: [YouTrack Wissensdatenbank](https://meldestelle-pro.youtrack.cloud/articles/MP-A-24)

---

**Version**: 2.0.0 (nach Dokumentations-Refactoring)  
**letzte Aktualisierung**: 31. Oktober 2025
