# Meldestelle Project Guidelines

**Version:** 2.1.0
**Last Updated:** 2025-09-15
**Status:** Reorganized & AI-Optimized

---

## 📋 Overview

This directory contains the comprehensive development guidelines for the Meldestelle project. The guidelines have been restructured into a hierarchical, AI-assistant-optimized format for better navigation, maintainability, and usability.

> **🤖 AI-Assistant Note:**
> All guidelines now include structured metadata headers and AI-specific hints for optimal assistant interaction:
> - **Metadata:** Each file has guideline_type, scope, audience, and dependencies
> - **AI Context:** Specific context information for better AI understanding
> - **Cross-References:** Consistent navigation links between related guidelines
> - **Quick Reference:** AI-optimized tables and checklists

## 🗂️ Guidelines Structure

### 📊 Master Guideline

- **[Master-Guideline](master-guideline.md)** - Central project guidelines and architectural foundations

### 🏗️ Project Standards

Core development standards and quality requirements:

| Guideline                                                                 | Scope                                       | AI Context                                      |
|---------------------------------------------------------------------------|---------------------------------------------|-------------------------------------------------|
| [Coding Standards](./project-standards/coding-standards.md)               | Code quality, naming conventions, patterns  | Kotlin standards, Result pattern, value classes |
| [Testing Standards](./project-standards/testing-standards.md)             | Test strategies, tools, coverage            | Test pyramid, Testcontainers, debugging         |
| [Documentation Standards](./project-standards/documentation-standards.md) | Documentation language, structure, API docs | German language rules, README templates         |
| [Architecture Principles](./project-standards/architecture-principles.md) | Microservices, DDD, EDA, KMP                | Clean Architecture, bounded contexts, MVVM      |

### 🔧 Technology Guides

Technology-specific implementation guidelines:

#### Web Applications

- **[Web App Guideline](./technology-guides/web-app-guideline.md)** - Compose Multiplatform development for desktop and web clients

#### Docker & Infrastructure

| Docker Module                                                                  | Focus Area                      | AI Context                                  |
|--------------------------------------------------------------------------------|---------------------------------|---------------------------------------------|
| [Docker Overview](./technology-guides/docker/docker-overview.md)               | Philosophy and principles       | Container strategy, security-first approach |
| [Docker Architecture](./technology-guides/docker/docker-architecture.md)       | Services and version management | Service categories, centralized versions    |
| [Docker Development](./technology-guides/docker/docker-development.md)         | Development workflow            | Makefile commands, debugging, hot-reload    |
| [Docker Production](./technology-guides/docker/docker-production.md)           | Production deployment           | Security hardening, SSL/TLS, monitoring     |
| [Docker Monitoring](./technology-guides/docker/docker-monitoring.md)           | Observability setup             | Prometheus, Grafana, health checks          |
| [Docker Troubleshooting](./technology-guides/docker/docker-troubleshooting.md) | Problem resolution              | Common issues, best practices, workflows    |

### 🔄 Process Guides
Development process and workflow guidelines:

- **[Trace Bullet Guideline](./process-guides/trace-bullet-guideline.md)** - End-to-end architecture validation cycle

## 🎯 Quick Navigation for AI Assistants

### Common Development Tasks

| Task                         | Primary Guidelines                        | Supporting Guidelines                     |
|------------------------------|-------------------------------------------|-------------------------------------------|
| **New Feature Development**  | Architecture Principles, Coding Standards | Testing Standards, Docker Development     |
| **Frontend Development**     | Web App Guideline                         | Architecture Principles, Coding Standards |
| **Backend Service Creation** | Architecture Principles, Coding Standards | Docker Development, Testing Standards     |
| **Infrastructure Setup**     | Docker Architecture, Docker Development   | Docker Overview, Docker Monitoring        |
| **Production Deployment**    | Docker Production                         | Docker Architecture, Docker Monitoring    |
| **Testing Implementation**   | Testing Standards                         | Coding Standards, Docker Development      |
| **Documentation Writing**    | Documentation Standards                   | All related technical guidelines          |
| **Troubleshooting Issues**   | Docker Troubleshooting                    | Docker Development, Docker Monitoring     |

### Key Architectural Decisions

1. **Microservices Architecture** - See [Architecture Principles](./project-standards/architecture-principles.md)
2. **Domain-Driven Design** - See [Architecture Principles](./project-standards/architecture-principles.md)
3. **Event-Driven Architecture** - See [Architecture Principles](./project-standards/architecture-principles.md)
4. **Kotlin Multiplatform** - See [Web App Guideline](./technology-guides/web-app-guideline.md)
5. **Docker-First Infrastructure** - See [Docker Overview](./technology-guides/docker/docker-overview.md)

### Technology Stack Quick Reference

| Layer              | Technologies                                | Guidelines                                |
|--------------------|---------------------------------------------|-------------------------------------------|
| **Frontend**       | Kotlin Multiplatform, Compose Multiplatform | Web App Guideline                         |
| **Backend**        | Spring Boot, Kotlin, Clean Architecture     | Architecture Principles, Coding Standards |
| **Infrastructure** | Docker, PostgreSQL, Redis, Kafka, Consul    | Docker Guides                             |
| **Monitoring**     | Prometheus, Grafana, Zipkin                 | Docker Monitoring                         |
| **Testing**        | JUnit 5, MockK, Testcontainers              | Testing Standards                         |

## 🚀 Getting Started

### For Developers

1. Start with [Master-Guideline](./master-guideline.md) for project overview
2. Review [Architecture Principles](./project-standards/architecture-principles.md) for architectural foundations
3. Follow [Coding Standards](./project-standards/coding-standards.md) for development practices
4. Use [Docker Development](./technology-guides/docker/docker-development.md) for local setup

### For AI Assistants

1. Each guideline includes structured metadata and AI context
2. Use the `ai_context` field for understanding guideline scope
3. Cross-reference related guidelines through navigation sections
4. Leverage quick reference tables for rapid information access

### For Project Managers

1. [Trace Bullet Guideline](./process-guides/trace-bullet-guideline.md) for current development cycle
2. [Master-Guideline](./master-guideline.md) for project standards overview
3. Individual guidelines for specific team coordination

## 🤖 Automatisierung und Validierung

Das Guidelines-System verfügt über umfassende Automatisierungsfeatures:

### 🔗 Automatische Validierung

- **Link-Validierung:** `.junie/scripts/validate-links.sh` - Cross-Referenzen und YAML-Konsistenz
- **Template-System:** `.junie/scripts/create-guideline.sh` - Automatische Guideline-Erstellung
- **Pre-commit Hook:** `.junie/scripts/pre-commit-guidelines.sh` - Lokale Validierung vor Commits
- **CI/CD-Integration (optional):** `.github/workflows/ci-main.yml` (Job: `validate-docs`) - Markdown-Lint und Link-Check für kritische Docs

### 📋 Verfügbare Templates

- **Project-Standards:** `project-standard-template.md`
- **Technology-Guides:** `technology-guideline-template.md`
- **Process-Guides:** `process-guide-template.md`

**Detaillierte Dokumentation:** [AUTOMATION-FEATURES.md](../AUTOMATION-FEATURES.md)

## 📝 Guideline Metadata Format

All guidelines follow this metadata structure for AI optimization:

```yaml
---
guideline_type: "project-standards"  # oder "technology" oder "process-guide"
scope: "specific-area-identifier"
audience: ["developers", "ai-assistants", "architects", "devops", "project-managers"]
last_updated: "YYYY-MM-DD"
dependencies: ["list-of-related-guidelines"]
related_files: ["relevant-project-files"]
ai_context: "Brief description for AI understanding"
---
```

## 🔍 Guidelines Maintenance

### Update Process

1. **Content Changes** → Update specific guideline file
2. **Structural Changes** → Update README.md navigation
3. **New Guidelines** → Add to appropriate category and update index
4. **Deprecated Guidelines** → Archive and update references

### Quality Assurance

- All guidelines include AI-optimized metadata
- Cross-references are maintained and validated
- Navigation links are consistent across guidelines
- Content follows documentation standards

---

**Last Restructuring:** 2025-09-13 - Complete hierarchical reorganization with AI optimization
**Next Review:** As needed based on project evolution

**Questions or suggestions?** Update this README.md or reach out to the development team.
