# Docker-Compose Fehler Behebung - Vollständige Lösung

## Problemübersicht
Die folgenden Fehler wurden beim Ausführen der docker-compose Befehle identifiziert und behoben:

1. **Network-Konfigurationsfehler**: `meldestelle-network declared as external, but could not be found`
2. **ContainerConfig KeyError**: Fehler beim Inspizieren bestehender Container
3. **API Gateway Service-Fehler**: `Service api-gateway has neither an image nor a build context specified`
4. **Deploy Resource Warnings**: Unsupported `reservations.cpus` sub-keys

## 🔧 Angewendete Lösungen

### 1. Network-Konfiguration korrigiert ✓
**Problem**: Inkonsistente Network-Definitionen zwischen compose-Dateien
- `docker-compose.yml`: `driver: bridge`
- `docker-compose.services.yml` und `docker-compose.clients.yml`: `external: true`

**Lösung**:
- Entfernung von `external: true` aus allen compose-Dateien
- Einheitliche Verwendung von `driver: bridge`

### 2. ContainerConfig KeyError behoben ✓
**Problem**: Korrupte Container-Metadaten von vorherigen Runs
**Lösung**:
- Bereinigung aller bestehenden Container
- Befehl: `docker rm $(docker ps -a -q --filter "name=meldestelle")`

### 3. API Gateway Service-Konfiguration ✓
**Problem**: `docker-compose.override.yml` referenziert Services, die nicht in der Basis-Konfiguration definiert sind
**Lösung**:
- Korrekte Verwendung der compose-Datei-Kombinationen
- `docker-compose.override.yml` nur zusammen mit `docker-compose.services.yml` verwenden

### 4. Deploy Resource Warnings eliminiert ✓
**Problem**: Docker Compose 1.29.2 unterstützt keine `reservations` unter `deploy.resources`
**Lösung**:
- Entfernung aller `reservations` Sektionen aus `docker-compose.services.yml`
- Beibehaltung der `limits` Konfigurationen

## ✅ Korrekte Docker-Compose Befehle

### Vorbereitung (einmalig nach Fehlern)
```bash
# Zum richtigen Verzeichnis wechseln
cd /home/stefan-mo/WsMeldestelle/Meldestelle

# Bestehende Container bereinigen (falls ContainerConfig Fehler auftreten)
docker rm $(docker ps -a -q --filter "name=meldestelle") 2>/dev/null || true

# Verwaiste Images bereinigen (optional)
docker image prune -f
```

### 1. Alle Services einschließlich Clients
```bash
docker-compose \
  -f docker-compose.yml \
  -f docker-compose.services.yml \
  -f docker-compose.clients.yml \
  up -d
```

### 2. Nur Infrastructure für Backend-Entwicklung
```bash
docker-compose -f docker-compose.yml up -d postgres redis kafka consul zipkin
```

### 3. Mit Debug-Unterstützung für Service-Entwicklung
```bash
DEBUG=true SPRING_PROFILES_ACTIVE=docker \
docker-compose -f docker-compose.yml -f docker-compose.services.yml up -d
```

### 4. Mit Live-Reload für Frontend-Entwicklung
```bash
# WICHTIG: Nur verwenden wenn docker-compose.services.yml ebenfalls geladen wird
docker-compose \
  -f docker-compose.yml \
  -f docker-compose.services.yml \
  -f docker-compose.override.yml \
  up -d
```

## 🚨 Wichtige Hinweise

### Override-Datei Verwendung
- `docker-compose.override.yml` darf **NICHT** allein mit `docker-compose.yml` verwendet werden
- Grund: Override definiert nur Konfigurationsüberschreibungen, keine vollständigen Services
- **Richtig**: `-f docker-compose.yml -f docker-compose.services.yml -f docker-compose.override.yml`
- **Falsch**: `-f docker-compose.yml -f docker-compose.override.yml`

### Network-Konsistenz
- Alle compose-Dateien verwenden jetzt `driver: bridge` für `meldestelle-network`
- Keine `external: true` Deklarationen mehr vorhanden
- Network wird automatisch von Docker Compose erstellt

### Resource-Limits
- Nur `limits` werden verwendet (memory, cpus)
- `reservations` wurden entfernt (nicht unterstützt in Docker Compose 1.29.2)
- Services starten ohne Warnings

## 🔍 Fehlerbehebung

### Bei "ContainerConfig" Fehlern:
```bash
docker rm $(docker ps -a -q --filter "name=meldestelle") 2>/dev/null || true
docker-compose down --volumes --remove-orphans 2>/dev/null || true
```

### Bei Network-Fehlern:
```bash
docker network ls | grep meldestelle
docker network rm meldestelle-network 2>/dev/null || true
```

### Bei Build-Fehlern:
```bash
docker-compose build --no-cache --pull
```

## 🧪 Verifikation

### Status prüfen:
```bash
docker-compose ps
docker network ls | grep meldestelle
```

### Logs überwachen:
```bash
docker-compose logs -f [service-name]
```

### Services stoppen:
```bash
docker-compose down
# Mit Volumes entfernen:
docker-compose down -v
```

## ✅ Zusammenfassung
- ✅ Network-Konfiguration vereinheitlicht
- ✅ ContainerConfig-Fehler durch Container-Cleanup behoben
- ✅ API Gateway Service-Konfiguration korrigiert
- ✅ Deploy Resource Warnings eliminiert
- ✅ Korrekte Verwendung der compose-Datei-Kombinationen dokumentiert

Alle ursprünglichen Fehler wurden behoben. Die docker-compose Befehle sollten nun ohne Fehler oder Warnings ausgeführt werden können.
