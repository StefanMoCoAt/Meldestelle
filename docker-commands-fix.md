# Docker-Compose Fehler Behebung

## Problem
Die docker-compose Befehle schlugen fehl mit dem Fehler:
```
ERROR: .FileNotFoundError: [Errno 2] No such file or directory: './docker-compose.yml'
```

## Ursache
Die Befehle wurden aus dem falschen Verzeichnis ausgeführt:
- **Falsch**: `/home/stefan-mo/WsMeldestelle/Meldestelle/.junie/guidelines/`
- **Richtig**: `/home/stefan-mo/WsMeldestelle/Meldestelle/` (Projekt-Root)

## Lösung
Alle docker-compose Befehle müssen aus dem Projekt-Root-Verzeichnis ausgeführt werden:

```bash
# Zuerst zum richtigen Verzeichnis wechseln
cd /home/stefan-mo/WsMeldestelle/Meldestelle

# Dann die Befehle ausführen:

# 1. Alle Services einschließlich Clients
docker-compose \
  -f docker-compose.yml \
  -f docker-compose.services.yml \
  -f docker-compose.clients.yml \
  up -d

# 2. Nur Infrastructure für Backend-Entwicklung
docker-compose -f docker-compose.yml up -d postgres redis kafka consul zipkin

# 3. Mit Debug-Unterstützung für Service-Entwicklung
DEBUG=true SPRING_PROFILES_ACTIVE=docker \
docker-compose -f docker-compose.yml -f docker-compose.services.yml up -d

# 4. Mit Live-Reload für Frontend-Entwicklung
docker-compose -f docker-compose.yml -f docker-compose.override.yml up -d
```

## Verifikation
Die folgenden Dateien existieren im Projekt-Root:
- ✅ `docker-compose.yml` (Infrastructure Services)
- ✅ `docker-compose.services.yml` (Application Services)
- ✅ `docker-compose.clients.yml` (Client Applications)
- ✅ `docker-compose.override.yml` (Development Overrides)

## Zusätzliche Befehle
```bash
# Services stoppen
docker-compose down

# Services mit Volumes entfernen
docker-compose down -v

# Logs anzeigen
docker-compose logs -f [service-name]

# Status prüfen
docker-compose ps
```
