# Docker-Development Workflow

---
guideline_type: "technology"
scope: "docker-development"
audience: ["developers", "ai-assistants"]
last_updated: "2025-09-15"
dependencies: ["docker-overview.md", "docker-architecture.md"]
related_files: ["docker-compose.yml", "docker-compose.override.yml", "Makefile"]
ai_context: "Entwicklungs-Workflow, Debugging und lokale Entwicklungsumgebung mit Docker"
---

## 🛠️ Development-Workflow

> **📖 Hinweis:** Für einen allgemeinen Überblick über die Docker-Infrastruktur siehe [docker-overview](docker-overview.md).

### Schnellstart-Befehle

```bash
# 🚀 Komplettes Development-Setup
make dev-up           # Startet alle Development-Services
make dev-down         # Stoppt alle Services
make dev-logs         # Zeigt Logs aller Services
make dev-restart      # Neustart aller Services

# 🔧 Service-spezifische Befehle
make service-build SERVICE=ping-service    # Service neu bauen
make service-logs SERVICE=ping-service     # Service-Logs anzeigen
make service-restart SERVICE=ping-service  # Service neustarten
```

> **🤖 AI-Assistant Hinweis:**
> Für Development verwende die Makefile-Befehle oder direkt docker-compose:
> - **Alles starten:** `make dev-up` oder `docker-compose up -d`
> - **Logs ansehen:** `make dev-logs` oder `docker-compose logs -f`
> - **Service debuggen:** `docker-compose exec <service> sh`

**Makefile-Beispiel:**

```makefile
# Development commands
.PHONY: dev-up dev-down dev-logs dev-restart

dev-up:
	docker-compose -f docker-compose.yml -f docker-compose.services.yml up -d
	@echo "🚀 Development environment started"
	@echo "📊 Grafana: http://localhost:3000 (admin/admin)"
	@echo "🔍 Prometheus: http://localhost:9090"
	@echo "🚪 API Gateway: http://localhost:8080"

dev-down:
	docker-compose -f docker-compose.yml -f docker-compose.services.yml down

dev-logs:
	docker-compose -f docker-compose.yml -f docker-compose.services.yml logs -f

dev-restart:
	$(MAKE) dev-down
	$(MAKE) dev-up

# Service-specific commands
service-build:
	@test -n "$(SERVICE)" || (echo "❌ SERVICE parameter required"; exit 1)
	docker-compose -f docker-compose.yml -f docker-compose.services.yml build $(SERVICE)

service-logs:
	@test -n "$(SERVICE)" || (echo "❌ SERVICE parameter required"; exit 1)
	docker-compose logs -f $(SERVICE)

service-restart:
	@test -n "$(SERVICE)" || (echo "❌ SERVICE parameter required"; exit 1)
	docker-compose -f docker-compose.yml -f docker-compose.services.yml restart $(SERVICE)
```

### Hot-Reload Development

**docker-compose.override.yml** für optimierte Entwicklung:

```yaml
# Development overrides für Hot-Reload
version: '3.8'

services:
  web-client:
    volumes:
      - ./client/web-app/src:/app/src:ro
      - ./client/common-ui/src:/app/common-ui/src:ro
    environment:
      - NODE_ENV=development
    command: npm run dev

  ping-service:
    environment:
      - DEBUG=true
      - SPRING_DEVTOOLS_RESTART_ENABLED=true
    ports:
      - "5005:5005"  # Debug-Port
    volumes:
      - ./temp/ping-service/src:/workspace/src:ro
```

### Debugging von Services

```bash
# Service im Debug-Modus starten
docker-compose -f docker-compose.yml up -d ping-service
docker-compose exec ping-service sh

# Logs in Echtzeit verfolgen
docker-compose logs -f ping-service api-gateway

# Health-Check Status prüfen
curl -s http://localhost:8082/actuator/health | jq
curl -s http://localhost:8080/actuator/health | jq
```

## 🎯 AI-Assistenten: Development-Schnellreferenz

### Häufige Entwicklungsaufgaben

| Aufgabe | Befehl | Beschreibung |
|---------|---------|--------------|
| Umgebung starten | `make dev-up` | Alle Services für Development |
| Service debuggen | `docker-compose exec <service> sh` | Shell im Container |
| Logs verfolgen | `docker-compose logs -f <service>` | Live-Logs anzeigen |
| Service neu bauen | `make service-build SERVICE=<name>` | Einzelnen Service rebuilden |
| Health-Check | `curl localhost:<port>/actuator/health` | Service-Status prüfen |

### Development-URLs
- **Grafana:** http://localhost:3000 (admin/admin)
- **Prometheus:** http://localhost:9090
- **API Gateway:** http://localhost:8080
- **Consul:** http://localhost:8500
- **Keycloak:** http://localhost:8180

### Debug-Ports
- **Spring-Services:** 5005 (Standard Java Debug)
- **Web-App:** Hot-Reload über Volume-Mapping
- **Client-Apps:** Port 4000 (Web), 5901 (Desktop VNC)

### Troubleshooting Development

#### Container startet nicht
```bash
# Container-Status prüfen
docker-compose ps

# Container-Logs anzeigen
docker-compose logs <service-name>

# Container neu starten
docker-compose restart <service-name>

# Image neu bauen
docker-compose build --no-cache <service-name>
```

#### Port-Konflikte
```bash
# Ports prüfen
netstat -tulpn | grep :<port>

# Service mit anderem Port starten
docker-compose -f docker-compose.yml -f docker-compose.override.yml up -d
```

#### Volume-Probleme
```bash
# Volumes prüfen
docker volume ls

# Volume-Inhalt anzeigen
docker-compose exec <service> ls -la /path/to/volume
```

---

**Navigation:**
- [Docker-Overview](./docker-overview.md) - Grundlagen und Philosophie
- [Docker-Architecture](./docker-architecture.md) - Container-Services und Struktur
- [Docker-Production](./docker-production.md) - Production-Deployment
- [Docker-Monitoring](./docker-monitoring.md) - Observability
- [Docker-Troubleshooting](./docker-troubleshooting.md) - Problemlösung
