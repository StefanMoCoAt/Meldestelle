# Ping Service Testing Setup - Trace-Bullet Backend Testing

## Übersicht

Dieses Docker Compose Setup ermöglicht das isolierte Testen des **Ping Service Backends** im Rahmen der Trace-Bullet Implementierung. Es stellt eine minimale, aber vollständige Testumgebung bereit, die alle notwendigen Abhängigkeiten enthält, ohne die Hauptentwicklungsumgebung zu beeinträchtigen.

## Architektur

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Ping Service  │    │   Consul        │    │   PostgreSQL    │
│   Port: 8082    │◄──►│   Port: 8501    │    │   Port: 5433    │
│   (Test Target) │    │   (Discovery)   │    │   (Database)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                        │                        │
         ▼                        ▼                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Prometheus    │    │   Redis         │    │   Test Runner   │
│   Port: 9091    │    │   Port: 6380    │    │   (Automated)   │
│   (Monitoring)  │    │   (Cache)       │    │   (Tests)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Enthaltene Services

### 1. **Ping Service** (Hauptkomponente)
- **Port**: 8082
- **Debug Port**: 5005 (falls DEBUG=true)
- **Health Check**: `/actuator/health`
- **Metriken**: `/actuator/prometheus`
- **Circuit Breaker**: `/actuator/circuitbreakers`

### 2. **PostgreSQL Test-Datenbank**
- **Port**: 5433 (um Konflikte mit der Hauptdatenbank zu vermeiden)
- **Database**: `pingtest`
- **User**: `testuser`
- **Password**: `testpass`

### 3. **Redis Cache**
- **Port**: 6380 (um Konflikte zu vermeiden)
- **Verwendung**: Event Store und Caching

### 4. **Consul Service Discovery**
- **Port**: 8501 (um Konflikte zu vermeiden)
- **Web UI**: http://localhost:8501
- **Verwendung**: Service Registration und Discovery

### 5. **Prometheus Monitoring**
- **Port**: 9091 (um Konflikte zu vermeiden)
- **Web UI**: http://localhost:9091
- **Verwendung**: Metriken-Sammlung und Monitoring

### 6. **Test Runner** (Optional)
- Automatisierte Tests für alle Endpoints
- Läuft nur mit `--profile test`

## Schnellstart

### 1. Umgebung starten
```bash
# Basis-Setup starten (ohne automatische Tests)
docker-compose -f docker-compose-ping-test.yml up -d

# Mit automatischen Tests
docker-compose -f docker-compose-ping-test.yml --profile test up -d
```

### 2. Status prüfen
```bash
# Alle Container-Status anzeigen
docker-compose -f docker-compose-ping-test.yml ps

# Logs des Ping Service anzeigen
docker-compose -f docker-compose-ping-test.yml logs -f ping-service

# Health Checks aller Services
docker-compose -f docker-compose-ping-test.yml ps --format "table {{.Service}}\t{{.Status}}\t{{.Ports}}"
```

### 3. Manuelle Tests durchführen

#### Health Check
```bash
curl http://localhost:8082/actuator/health
```

#### Service Info
```bash
curl http://localhost:8082/actuator/info
```

#### Circuit Breaker Status
```bash
curl http://localhost:8082/actuator/circuitbreakers
```

#### Prometheus Metriken
```bash
curl http://localhost:8082/actuator/prometheus
```

### 4. Umgebung stoppen und aufräumen
```bash
# Services stoppen
docker-compose -f docker-compose-ping-test.yml down

# Services stoppen und Volumes löschen
docker-compose -f docker-compose-ping-test.yml down -v

# Zusätzlich Images löschen
docker-compose -f docker-compose-ping-test.yml down -v --rmi all
```

## Erweiterte Konfiguration

### Umgebungsvariablen

Erstellen Sie eine `.env.ping-test` Datei für benutzerdefinierte Konfiguration:

```bash
# Database Configuration
POSTGRES_USER=mytestuser
POSTGRES_PASSWORD=mytestpassword
POSTGRES_DB=mypingtest

# Debug Configuration
DEBUG=true

# JVM Configuration
JAVA_OPTS=-Xmx1g -XX:+UseG1GC -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
```

Laden Sie die Konfiguration:
```bash
docker-compose -f docker-compose-ping-test.yml --env-file .env.ping-test up -d
```

### Debug-Modus aktivieren

1. **Debug-Modus starten:**
```bash
DEBUG=true docker-compose -f docker-compose-ping-test.yml up -d ping-service
```

2. **IDE mit Remote Debug verbinden:**
   - Host: `localhost`
   - Port: `5005`
   - Typ: `Attach to remote JVM`

## Monitoring und Überwachung

### Prometheus Dashboard
- URL: http://localhost:9091
- Verfügbare Metriken:
  - `http_server_requests_seconds`
  - `jvm_memory_used_bytes`
  - `resilience4j_circuitbreaker_state`
  - Custom Application Metriken

### Consul Web UI
- URL: http://localhost:8501
- Zeigt registrierte Services
- Service Health Status
- Service Discovery Informationen

## Troubleshooting

### Häufige Probleme

#### 1. **Port-Konflikte**
```bash
# Prüfen Sie, welche Ports bereits verwendet werden
netstat -tlnp | grep -E ':(8082|5433|6380|8501|9091)'

# Oder mit ss
ss -tlnp | grep -E ':(8082|5433|6380|8501|9091)'
```

#### 2. **Service startet nicht**
```bash
# Detaillierte Logs anzeigen
docker-compose -f docker-compose-ping-test.yml logs ping-service

# Container Status prüfen
docker inspect ping-test-service
```

#### 3. **Consul Connection Probleme**
```bash
# Consul Logs prüfen
docker-compose -f docker-compose-ping-test.yml logs consul-test

# Consul Services anzeigen
curl http://localhost:8501/v1/agent/services
```

#### 4. **Database Connection Issues**
```bash
# PostgreSQL Logs
docker-compose -f docker-compose-ping-test.yml logs postgres-test

# Direkte Verbindung testen
docker exec -it ping-test-postgres psql -U testuser -d pingtest
```

### Performance-Optimierung

#### 1. **Speicher-Limits setzen**
```yaml
# In docker-compose-ping-test.yml ergänzen:
services:
  ping-service:
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M
```

#### 2. **Build-Cache nutzen**
```bash
# Build mit Cache
docker-compose -f docker-compose-ping-test.yml build --parallel

# Build ohne Cache (bei Problemen)
docker-compose -f docker-compose-ping-test.yml build --no-cache ping-service
```

## Test-Szenarien

### 1. **Circuit Breaker Tests**
```bash
# Circuit Breaker Status abrufen
curl http://localhost:8082/actuator/circuitbreakers

# Mehrfache Requests senden um Circuit Breaker zu testen
for i in {1..10}; do
  curl -w "Response time: %{time_total}s\n" http://localhost:8082/actuator/health
  sleep 1
done
```

### 2. **Load Testing**
```bash
# Mit Apache Bench (falls installiert)
ab -n 100 -c 10 http://localhost:8082/actuator/health

# Mit curl (einfacher Loop)
for i in {1..50}; do
  curl -s http://localhost:8082/actuator/health > /dev/null &
done
wait
```

### 3. **Service Discovery Tests**
```bash
# Service Registration prüfen
curl http://localhost:8501/v1/agent/services | jq .

# Health Checks in Consul
curl http://localhost:8501/v1/health/service/ping-service | jq .
```

## Continuous Integration

### GitHub Actions Beispiel
```yaml
name: Ping Service Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Start Test Environment
        run: |
          docker-compose -f docker-compose-ping-test.yml up -d

      - name: Wait for Services
        run: |
          timeout 300 bash -c 'until curl -f http://localhost:8082/actuator/health; do sleep 5; done'

      - name: Run Tests
        run: |
          docker-compose -f docker-compose-ping-test.yml --profile test up test-runner

      - name: Cleanup
        run: |
          docker-compose -f docker-compose-ping-test.yml down -v
```

## Best Practices

### 1. **Vor dem Testen**
- Stellen Sie sicher, dass keine anderen Services auf den Test-Ports laufen
- Überprüfen Sie verfügbaren Speicher und CPU-Ressourcen
- Löschen Sie alte Test-Volumes bei Bedarf

### 2. **Während des Testens**
- Nutzen Sie die Health Check Endpoints
- Überwachen Sie Logs in Echtzeit
- Prüfen Sie Metriken in Prometheus

### 3. **Nach dem Testen**
- Stoppen und entfernen Sie Test-Container
- Löschen Sie Test-Volumes um Speicher zu sparen
- Dokumentieren Sie gefundene Issues

## Erweiterungen

### Zusätzliche Services hinzufügen
Um weitere Services zu testen, erweitern Sie die `docker-compose-ping-test.yml`:

```yaml
services:
  another-service:
    build:
      context: .
      dockerfile: path/to/Dockerfile
    depends_on:
      - ping-service
    networks:
      - ping-test-network
```

### Grafana für erweiterte Visualisierung
```yaml
services:
  grafana-test:
    image: grafana/grafana:latest
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    networks:
      - ping-test-network
```

## Support

Bei Problemen oder Fragen:
1. Prüfen Sie die Logs: `docker-compose -f docker-compose-ping-test.yml logs`
2. Überprüfen Sie die Container-Status: `docker-compose -f docker-compose-ping-test.yml ps`
3. Konsultieren Sie die Hauptdokumentation in `README.md`
4. Überprüfen Sie die Service-spezifische Konfiguration in `temp/ping-service/src/main/resources/application.yml`

---

**Erstellt für**: Meldestelle Projekt - Ping Service Trace-Bullet Testing
**Version**: 1.0.0
**Datum**: 2025-09-08
