# Start Local (Lokales Setup)

Kurzanleitung, um das Projekt lokal in wenigen Minuten zu starten.

**Wichtiger Hinweis (Januar 2026):** Der Build ist derzeit aufgrund eines Kotlin/Wasm-Compiler-Problems blockiert. Die Infrastruktur und die Backend-Services können jedoch unabhängig davon gestartet werden.

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
docker compose -f docker-compose.yaml up -d

# 4) Backend-Service starten (Beispiel: Results Service)
./gradlew :backend:services:results:results-service:bootRun
```

Sobald die Infrastruktur läuft, erreichst du unter anderem:
- Gateway: http://localhost:8081
- Keycloak: http://localhost:8180
- Grafana: http://localhost:3000
- Prometheus: http://localhost:9090

## Tests ausführen
```bash
# Führt alle Tests aus (Frontend-Tests könnten fehlschlagen)
./gradlew test

# Spezifisches Backend-Modul testen
./gradlew :backend:services:results:results-service:test
```

## Troubleshooting
- Dienste starten nicht? Ports belegt oder Logs prüfen:
  ```bash
  docker ps
  docker logs <container-name>
  ```
- Infrastruktur neu starten:
  ```bash
  docker compose -f docker-compose.yaml down -v
  docker compose -f docker-compose.yaml up -d
  ```
- Environment-Variablen: werden aus der `.env`-Datei im Root-Verzeichnis geladen.

## Weiterführende Hinweise
- Architektur: `docs/01_Architecture/ARCHITECTURE.md` (veraltet, siehe Reports)
- ADRs: `docs/01_Architecture/adr/`
- Aktuelle Reports: `docs/90_Reports/`

Stand: Januar 2026 (teilweise aktualisiert)
