# Docker-Overview und Philosophie

---
guideline_type: "technology"
scope: "docker-overview"
audience: ["developers", "ai-assistants"]
last_updated: "2025-09-13"
dependencies: ["master-guideline.md"]
related_files: ["docker-compose.yml", "docker/versions.toml"]
ai_context: "Docker philosophy and general principles for Meldestelle project"
---

## 🚀 Überblick und Philosophie

Das Meldestelle-Projekt implementiert eine **moderne, sicherheitsorientierte Containerisierungsstrategie** basierend auf bewährten DevOps-Praktiken und Production-Ready-Standards. Unsere Docker-Architektur ist darauf ausgelegt:

- **Sicherheit first**: Alle Container laufen als Non-Root-User
- **Optimale Performance**: Multi-stage Builds mit Layer-Caching
- **Observability**: Umfassendes Monitoring und Health-Checks
- **Skalierbarkeit**: Microservices-ready mit Service Discovery
- **Wartbarkeit**: Standardisierte Templates und klare Konventionen

## 🎯 Für AI-Assistenten: Wichtige Konzepte

> **🤖 AI-Assistant Hinweis:**
> Diese Sektion enthält die Grundphilosophie des Docker-Setups.
> - Alle Versionsinformationen sind in `docker/versions.toml` zentralisiert
> - Services sind in `docker-compose.yml` definiert
> - Monitoring läuft unter `http://localhost:3001` (Grafana)

### Zentrale Dateien für AI-Referenz
- `docker/versions.toml` - Single Source of Truth für alle Versionen
- `docker-compose.yml` - Haupt-Service-Orchestrierung
- `scripts/docker-versions-update.sh` - Automatische Version-Updates
- `scripts/validate-docker-consistency.sh` - Konsistenz-Validierung

## 📋 Docker-Guidelines Navigation

Für spezifische Docker-Themen siehe:
- [Docker-Architektur](./docker-architecture.md) - Container-Services und Struktur
- [Docker-Development](./docker-development.md) - Entwicklungsworkflow
- [Docker-Production](./docker-production.md) - Production-Deployment
- [Docker-Monitoring](./docker-monitoring.md) - Observability und Überwachung
- [Docker-Troubleshooting](./docker-troubleshooting.md) - Problemlösung

## Grundprinzipien

### Sicherheitsaspekte
- **Non-Root-Container**: Alle Container laufen mit dediziertem User
- **Minimale Base-Images**: Verwendung schlanker Images (Alpine, Distroless)
- **Security-Scans**: Regelmäßige Vulnerability-Checks
- **Network-Segmentierung**: Isolierte Docker-Networks

### Performance-Optimierung
- **Multi-Stage-Builds**: Schlanke Production-Images
- **Layer-Caching**: Optimale Build-Performance
- **Resource-Limits**: Definierte CPU/Memory-Constraints
- **Health-Checks**: Proaktive Service-Überwachung

### Wartbarkeit
- **Standardisierte Templates**: Konsistente Dockerfile-Struktur
- **Zentrale Konfiguration**: Environment-basierte Konfiguration
- **Dokumentation**: Umfassende README-Dateien pro Service
- **Versionierung**: Semantische Versionierung aller Images

---

> **Basis-Prinzipien:** Diese Guidelines erweitern die [Master-Guideline](../../master-guideline.md) um Docker-spezifische Aspekte und folgen den allgemeinen Projektstandards.
