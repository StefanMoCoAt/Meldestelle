# Meldestelle - Docker Konfiguration

## Übersicht

Das Meldestelle-Projekt nutzt eine modulare Docker-Compose-Struktur für verschiedene Deployment-Szenarien:

- **`docker-compose.yml`** - Basis-Infrastruktur (PostgreSQL, Redis, Keycloak, Consul, Kafka, Monitoring, Gateway)
- **`docker-compose.services.yml`** - Microservices (Ping, Members, Horses, Events, Masterdata)
- **`docker-compose.clients.yml`** - Client-Anwendungen (Web-App, Auth-Server, Monitoring-Server)

## Architektur

### Infrastruktur-Services (docker-compose.yml)
- **PostgreSQL** (Port 5432) - Hauptdatenbank
- **Redis** (Port 6379) - Cache und Event Store
- **Keycloak** (Port 8180) - Authentifizierung und Autorisierung
- **Consul** (Port 8500) - Service Discovery
- **Kafka + Zookeeper** (Ports 9092, 2181) - Event Streaming
- **Prometheus** (Port 9090) - Metriken-Sammlung
- **Grafana** (Port 3000) - Monitoring-Dashboard
- **API Gateway** (Port 8081) - Zentraler Eingang

### Microservices (docker-compose.services.yml)
- **Ping Service** (Port 8082) - Health Check und Test Service
- **Members Service** (Port 8083) - Mitgliederverwaltung
- **Horses Service** (Port 8084) - Pferdedaten
- **Events Service** (Port 8085) - Veranstaltungen
- **Masterdata Service** (Port 8086) - Stammdaten

### Client-Anwendungen (docker-compose.clients.yml)
- **Web Application** (Port 3000) - Kotlin Multiplatform Frontend
- **Auth Server** (Port 8087) - Erweiterte Authentifizierung
- **Monitoring Server** (Port 8088) - Monitoring-Erweiterungen

## Verwendung

### Nur Infrastruktur starten
```bash
# Für Backend-Entwicklung
docker-compose up -d
```

### Vollständiges System
```bash
# Alle Services und Clients
docker-compose -f docker-compose.yml \
               -f docker-compose.services.yml \
               -f docker-compose.clients.yml up -d
```

### Nur Services ohne Clients
```bash
# Infrastruktur + Microservices
docker-compose -f docker-compose.yml \
               -f docker-compose.services.yml up -d
```

### Spezifische Services
```bash
# Nur bestimmte Services
docker-compose up -d postgres redis keycloak
```

## Umgebungsvariablen

Die Docker-Konfiguration nutzt das zentrale `.env`-System aus dem `config/` Verzeichnis:

```bash
# Für Entwicklung
ln -sf config/.env.dev .env

# Für Produktion
ln -sf config/.env.prod .env

# Für Tests
ln -sf config/.env.test .env
```

### Wichtige Variablen

| Variable | Standard | Beschreibung |
|----------|----------|--------------|
| `POSTGRES_USER` | meldestelle | PostgreSQL Benutzer |
| `POSTGRES_PASSWORD` | meldestelle | PostgreSQL Passwort |
| `POSTGRES_DB` | meldestelle | PostgreSQL Datenbankname |
| `REDIS_PASSWORD` | (leer) | Redis Passwort |
| `GATEWAY_PORT` | 8081 | API Gateway Port |
| `CONSUL_PORT` | 8500 | Consul Port |
| `KAFKA_PORT` | 9092 | Kafka Port |
| `PROMETHEUS_PORT` | 9090 | Prometheus Port |
| `GRAFANA_PORT` | 3000 | Grafana Port |

## Health Checks

Alle Services verfügen über Health Checks:

```bash
# Status aller Services prüfen
docker-compose ps

# Service-spezifische Logs
docker-compose logs -f [service-name]

# Health Check einzelner Services
docker-compose exec postgres pg_isready -U meldestelle
docker-compose exec redis redis-cli ping
curl http://localhost:8500/v1/status/leader  # Consul
curl http://localhost:8081/actuator/health   # API Gateway
```

## Entwicklung

### Hot Reload für Web-App
```bash
# Web-App im Development-Modus
docker-compose -f docker-compose.yml \
               -f docker-compose.clients.yml up -d web-app
```

### Debug-Modus für Services
```bash
# Service mit Debug-Port (5005)
docker-compose -f docker-compose.yml \
               -f docker-compose.services.yml up -d
# Debug-Port ist automatisch verfügbar
```

### Logs verfolgen
```bash
# Alle Logs
docker-compose logs -f

# Spezifischer Service
docker-compose logs -f api-gateway

# Letzten 100 Zeilen
docker-compose logs --tail=100 -f
```

## Datenmanagement

### Volumes
- `postgres-data` - PostgreSQL Daten
- `redis-data` - Redis Persistierung
- `prometheus-data` - Prometheus Metriken
- `grafana-data` - Grafana Dashboards
- `monitoring-data` - Custom Monitoring Daten

### Backup
```bash
# PostgreSQL Backup
docker-compose exec -T postgres pg_dump -U meldestelle meldestelle > backup.sql

# Redis Backup
docker-compose exec redis redis-cli SAVE
docker cp $(docker-compose ps -q redis):/data/dump.rdb ./redis-backup.rdb
```

### Reset
```bash
# Alle Container und Volumes löschen
docker-compose down -v
docker-compose -f docker-compose.yml \
               -f docker-compose.services.yml \
               -f docker-compose.clients.yml down -v

# Images neu bauen
docker-compose build --no-cache
```

## Monitoring

### Prometheus Metriken
- URL: http://localhost:9090
- Sammelt Metriken von allen Services
- Konfiguration: `docker/monitoring/prometheus/prometheus.yml`

### Grafana Dashboards
- URL: http://localhost:3000
- Benutzer: admin / admin (Standard)
- Vorkonfigurierte Dashboards für alle Services

### Service Discovery
- Consul UI: http://localhost:8500
- Zeigt alle registrierten Services
- Health Status und Service-Informationen

## Troubleshooting

### Häufige Probleme

1. **Port-Konflikte**
   ```bash
   # Ports prüfen
   netstat -tulpn | grep :8081

   # Alternative Ports in .env setzen
   GATEWAY_PORT=8082
   ```

2. **Service startet nicht**
   ```bash
   # Dependencies prüfen
   docker-compose ps

   # Logs analysieren
   docker-compose logs [service-name]

   # Service neu starten
   docker-compose restart [service-name]
   ```

3. **Speicher-Probleme**
   ```bash
   # Speicher freigeben
   docker system prune -a

   # Volumes prüfen
   docker volume ls
   ```

4. **Netzwerk-Probleme**
   ```bash
   # Netzwerk neu erstellen
   docker-compose down
   docker network prune
   docker-compose up -d
   ```

### Konfiguration validieren
```bash
# Docker-Compose Syntax prüfen
docker-compose config

# Mit allen Files
docker-compose -f docker-compose.yml \
               -f docker-compose.services.yml \
               -f docker-compose.clients.yml config
```

## Produktion

### Sicherheitsaspekte
1. **Secrets**: Verwenden Sie starke Passwörter in `.env.prod`
2. **Netzwerk**: Externe Zugriffe über Load Balancer
3. **Volumes**: Backup-Strategie implementieren
4. **Updates**: Regelmäßige Image-Updates

### Performance-Optimierungen
1. **Resource Limits**: In Produktion definieren
2. **Monitoring**: Vollständige Observability
3. **Load Balancing**: Mehrere Instanzen für kritische Services
4. **Caching**: Redis optimal konfigurieren

## Build-Automatisierung

### Makefile-Integration
```bash
# Verfügbare Targets
make help

# System starten
make start

# System stoppen
make stop

# Logs anzeigen
make logs

# Services bauen
make build
```

### CI/CD Integration
```yaml
# GitHub Actions Beispiel
- name: Start Services
  run: |
    docker-compose -f docker-compose.yml \
                   -f docker-compose.services.yml up -d

- name: Run Tests
  run: |
    docker-compose exec -T api-gateway ./gradlew test
```

## Support

Bei Problemen:

1. Überprüfen Sie die Logs: `docker-compose logs -f`
2. Validieren Sie die Konfiguration: `docker-compose config`
3. Prüfen Sie die Umgebungsvariablen: `docker-compose config | grep environment`
4. Konsultieren Sie die Service-spezifischen READMEs im jeweiligen Verzeichnis

---

*Letzte Aktualisierung: 2025-01-06*
