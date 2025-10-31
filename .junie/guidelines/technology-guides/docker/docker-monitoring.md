# Docker-Monitoring und Observability

---

guideline_type: "technology"
scope: "docker-monitoring"
audience: ["developers", "devops", "ai-assistants"]
last_updated: "2025-09-15"
dependencies: ["docker-overview.md", "docker-architecture.md"]
related_files: ["docker-compose.yml", "config/monitoring/*", "config/grafana/*", "config/prometheus/*"]
ai_context: "Monitoring-Setup, Prometheus-Metriken, Grafana-Dashboards, Health-Checks und Log-Aggregation"

---

## 📊 Monitoring und Observability

### Prometheus Metrics

Alle Services exposieren standardisierte Metrics:

```yaml
# Service-Labels für Prometheus Autodiscovery
labels:
  - "prometheus.scrape=true"
  - "prometheus.port=8080"
  - "prometheus.path=/actuator/prometheus"
  - "prometheus.service=${SERVICE_NAME}"
```

> **🤖 AI-Assistant Hinweis:**
> Monitoring-Stack Zugriff:
> - **Grafana:** http://localhost:3000 (admin/admin)
> - **Prometheus:** http://localhost:9090
> - **Metrics-Endpoints:** `/actuator/prometheus` für Spring-Services
> - **Health-Checks:** `/actuator/health` für Readiness-Probes

### Grafana Dashboards

**Vorgefertigte Dashboards:**

- **Infrastructure Overview**: CPU, Memory, Disk, Network
- **Spring Boot Services**: JVM Metrics, HTTP Requests, Circuit Breaker
- **Database Performance**: PostgreSQL Connections, Query Performance
- **Message Queue**: Kafka Consumer Lag, Throughput
- **Business Metrics**: Application-spezifische KPIs

### Health Check Matrix

| Service | Endpoint | Erwartung | Timeout |
|---------|----------|-----------|---------|
| API Gateway | `/actuator/health` | `{"status":"UP"}` | 15s |
| Ping Service | `/actuator/health/readiness` | HTTP 200 | 3s |
| PostgreSQL | `pg_isready` | Connection OK | 5s |
| Redis | `redis-cli ping` | PONG | 5s |
| Keycloak | `/health/ready` | HTTP 200 | 5s |

### Log Aggregation

```bash
# Centralized logging mit ELK Stack (optional)
docker-compose -f docker-compose.yml -f docker-compose.logging.yml up -d

# Log-Parsing für strukturierte Logs
docker-compose logs --follow --tail=100 api-gateway | jq -r '.message'
```

## 🎯 AI-Assistenten: Monitoring-Schnellreferenz

### Monitoring-URLs
- **Grafana Dashboard:** http://localhost:3000 (admin/admin)
- **Prometheus Targets:** http://localhost:9090/targets
- **Prometheus Metrics:** http://localhost:9090/metrics
- **Service Health:** http://localhost:<port>/actuator/health

### Wichtige Metrics

| Metric-Typ | Beispiel | Beschreibung |
|------------|----------|--------------|
| JVM Memory | `jvm_memory_used_bytes` | Speicherverbrauch Java-Services |
| HTTP Requests | `http_requests_total` | API-Request-Zähler |
| Database Connections | `hikaricp_connections` | Pool-Verbindungen |
| Kafka Lag | `kafka_consumer_lag` | Consumer-Verzögerung |
| Custom Business | `meldestelle_registrations_total` | Fachliche KPIs |

### Health-Check Befehle

```bash
# Alle Services prüfen
docker-compose ps

# Service-spezifische Health-Checks
curl -s http://localhost:8082/actuator/health | jq '.status'
curl -s http://localhost:8081/actuator/health | jq '.status'

# Infrastructure Health-Checks
docker-compose exec postgres pg_isready -U meldestelle -d meldestelle
docker-compose exec redis redis-cli ping
curl -s http://localhost:8180/health/ready
```

### Log-Analyse

```bash
# Service-Logs in Echtzeit
docker-compose logs -f <service-name>

# Error-Logs filtern
docker-compose logs <service-name> | grep ERROR

# JSON-Logs strukturiert anzeigen
docker-compose logs api-gateway | jq -r '. | select(.level=="ERROR") | .message'

# Performance-Logs analysieren
docker-compose logs api-gateway | grep -i "took\|duration\|time"
```

### Dashboard-Setup

#### Infrastructure-Dashboard

```json
{
  "dashboard": {
    "title": "Meldestelle Infrastructure",
    "panels": [
      {
        "title": "CPU Usage",
        "targets": [
          {
            "expr": "rate(container_cpu_usage_seconds_total[5m]) * 100"
          }
        ]
      },
      {
        "title": "Memory Usage",
        "targets": [
          {
            "expr": "container_memory_usage_bytes / container_spec_memory_limit_bytes * 100"
          }
        ]
      }
    ]
  }
}
```

#### Application-Dashboard

```json
{
  "dashboard": {
    "title": "Meldestelle Services",
    "panels": [
      {
        "title": "HTTP Requests/sec",
        "targets": [
          {
            "expr": "rate(http_requests_total[1m])"
          }
        ]
      },
      {
        "title": "Response Time",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))"
          }
        ]
      }
    ]
  }
}
```

### Alerting-Regeln

```yaml
# prometheus/alerts.yml
groups:
  - name: meldestelle.rules
    rules:
    - alert: ServiceDown
      expr: up == 0
      for: 1m
      labels:
        severity: critical
      annotations:
        summary: "Service {{ $labels.instance }} is down"

    - alert: HighMemoryUsage
      expr: (container_memory_usage_bytes / container_spec_memory_limit_bytes) > 0.8
      for: 5m
      labels:
        severity: warning
      annotations:
        summary: "High memory usage on {{ $labels.instance }}"

    - alert: DatabaseConnectionsFull
      expr: hikaricp_connections_active >= hikaricp_connections_max * 0.8
      for: 2m
      labels:
        severity: warning
      annotations:
        summary: "Database connection pool nearly exhausted"
```

### Monitoring-Wartung

```bash
# Prometheus-Konfiguration neu laden
curl -X POST http://localhost:9090/-/reload

# Grafana-Dashboards exportieren
curl -s -H "Authorization: Bearer <token>" \
  http://localhost:3000/api/dashboards/uid/<dashboard-uid> > dashboard_backup.json

# Monitoring-Data bereinigen
docker-compose exec prometheus rm -rf /prometheus/data
docker-compose restart prometheus

# Log-Rotation für Monitoring-Services
docker-compose exec grafana find /var/log -name "*.log" -exec truncate -s 0 {} \;
```

### Performance-Tuning

```yaml
# prometheus.yml - Optimierte Konfiguration
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "/etc/prometheus/alerts.yml"

scrape_configs:
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['api-gateway:8080', 'ping-service:8082']
    scrape_interval: 10s

  - job_name: 'infrastructure'
    static_configs:
      - targets: ['postgres:5432', 'redis:6379']
    scrape_interval: 30s
```

---

**Navigation:**
- [docker-overview](./docker-overview.md) - Grundlagen und Philosophie
- [docker-architecture](./docker-architecture.md) - Container-Services und Struktur
- [docker-development](./docker-development.md) - Entwicklungsworkflow
- [docker-production](./docker-production.md) - Production-Deployment
- [docker-troubleshooting](./docker-troubleshooting.md) - Problemlösung
