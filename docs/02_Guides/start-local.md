---
type: Guide
status: ACTIVE
owner: DevOps Engineer
tags: [setup, local, docker, gradle]
---

# Start Local (Lokales Setup)

Kurzanleitung, um das Projekt lokal in wenigen Minuten zu starten.

## Voraussetzungen
- Docker und Docker Compose (v2)
- Java 25 (JDK)
- Git

## Schnellstart

```bash
# 1) Repository klonen
git clone https://github.com/StefanMoCoAt/meldestelle.git
cd meldestelle

# 2) Runtime-Environment vorbereiten
#    Kopiere die Vorlage.
cp .env.example .env

# 3) Infrastruktur starten (Postgres, Redis, Keycloak, Monitoring, Gateway)
docker compose --profile infra up -d

# 4) Backend starten (Gateway + Ping Service)
docker compose --profile backend up -d
```

Sobald die Infrastruktur läuft, erreichst du unter anderem:
- Gateway: http://localhost:8081
- Keycloak: http://localhost:8180
- Grafana: http://localhost:3000
- Prometheus: http://localhost:9090

## Tests ausführen
```bash
# Führt alle Tests aus
./gradlew test

# Spezifisches Backend-Modul testen
./gradlew :backend:services:ping:ping-service:test
```

## Troubleshooting
- Dienste starten nicht? Ports belegt oder Logs prüfen:
  ```bash
  docker ps
  docker logs <container-name>
  ```
- Infrastruktur neu starten:
  ```bash
  docker compose down -v
  docker compose --profile infra up -d
  ```
- Environment-Variablen: werden aus der `.env`-Datei im Root-Verzeichnis geladen.

## Weiterführende Hinweise
- Architektur: `docs/01_Architecture/MASTER_ROADMAP_2026_Q1.md`
- ADRs: `docs/01_Architecture/adr/`
- Aktuelle Reports: `docs/90_Reports/`
