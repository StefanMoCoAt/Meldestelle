---
type: Report
status: DRAFT
owner: Lead Architect
date: 2026-01-31
tags: [e2e, smoke, docker, migration, ktor-3.4.0, exposed-1.0.0]
---

# E2E Smoke – Migration Exposed 1.0.0 & Ktor 3.4.0

## Setup
- Compose: docker compose --profile all up --build -d
- Services (Auszug):
  - api-gateway (8080/actuator, 8080/api via Proxy)
  - ping-service (8082/actuator, /api/ping via Gateway)
  - web-app (Nginx auf 4000)
  - desktop-app (noVNC auf 6080)
- Backend-Basis: Spring Boot 3.5.x, Spring Cloud 2025.0.1
- Versionen (Platform/Catalog): ktor=3.4.0, exposed=1.0.0

## Checks & Ergebnisse
- Gateway Health: 200 OK (readiness/live, Prometheus)
- Ping-Service Health/Prometheus: 200 OK stabil
- Web-App Health: 200 OK (Fallback-Assets aktiv, Favicon bereitgestellt)
- Desktop-App: Xvfb/XFCE/x11vnc/noVNC aktiv, Zugriff via http://localhost:6080/

## Observability
- Prometheus-Metriken erreichbar (Gateway/Ping)
- Logs ohne kritische Fehler im Happy Path

## Issues & Notes
- Frontend KMP/JS-Build schlägt in Builder aktuell fehl (fehlende JS-Implementierungen in Auth/Ping-Data). Nginx liefert Fallback-Assets aus; Favicon hinzugefügt, um 404 zu vermeiden.

## Entscheidung
- Empfehlung: Go für Phase 4 (FE „web“-Target Migration & Build-Fix; Dokumente finalisieren)
