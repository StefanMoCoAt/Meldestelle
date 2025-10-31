---
owner: project-maintainers
status: active
review_cycle: 180d
last_reviewed: 2025-10-31
summary: "Übersicht der wichtigsten lokalen URLs und Ports. Quelle: docker/versions.toml"
---

# Referenz: Wichtige URLs und Ports (lokal)

Quelle der Wahrheit für Ports: [docker/versions.toml](../../docker/versions.toml) → [service-ports]

## Infrastruktur
- API Gateway: http://localhost:8081
- Keycloak (Auth): http://localhost:8180
- Consul (Service Discovery): http://localhost:8500
- PostgreSQL: localhost:5432
- Redis: localhost:6379

## Services
- Ping Service: http://localhost:8082
- Members Service: http://localhost:8083
- Horses Service: http://localhost:8084
- Events Service: http://localhost:8085
- Masterdata Service: http://localhost:8086

## Monitoring
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

## Clients
- Web App: http://localhost:4000
- Desktop App (VNC): localhost:5901
- Desktop App (noVNC): http://localhost:6080

## Hinweise
- Die oben genannten Ports sind in `docker/versions.toml` unter `[service-ports]` zentral gepflegt und werden für Docker Compose-Generierung und Validierung verwendet.
- Bei Port-Konflikten können die Werte in `docker/versions.toml` angepasst und anschließend die Compose-Dateien mit `bash scripts/generate-compose-files.sh all development` neu generiert werden.
