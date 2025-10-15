---
owner: project-maintainers
status: active
review_cycle: 180d
last_reviewed: 2025-10-15
summary: Konsolidierte Übersicht aller relevanten lokalen Ports/URLs sowie der produktiven Domains (hinter Nginx).
---

# Referenz: Ports & URLs

## Lokal (Standard-Setup)
- Web App: http://localhost:4000
- API Gateway: http://localhost:8081
  - Health: http://localhost:8081/actuator/health
- Services (nur lokal):
  - Ping Service: http://localhost:8082
  - Members Service: http://localhost:8083
  - Horses Service: http://localhost:8084
  - Events Service: http://localhost:8085
  - Masterdata Service: http://localhost:8086
- Keycloak (Auth): http://localhost:8180
- Consul (Service Discovery): http://localhost:8500
- Postgres: localhost:5432
- Redis: localhost:6379
- noVNC (Desktop): http://localhost:6080

Hinweis: In Produktion sind die einzelnen Services (8082–8086) nicht öffentlich erreichbar. Alle API-Aufrufe laufen über das Gateway.

## Produktion (hinter Nginx)
- Web App: http://meldestelle.yourdomain.com
- API Gateway: http://api.meldestelle.yourdomain.com
  - Health: http://api.meldestelle.yourdomain.com/actuator/health
- VNC (optional): http://vnc.meldestelle.yourdomain.com

Optional HTTPS: gleiche Hosts mit https://, sobald Zertifikate aktiv sind.
