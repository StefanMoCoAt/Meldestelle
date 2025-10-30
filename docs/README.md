# Meldestelle - Dokumentation

## 📚 Single Source of Truth: YouTrack

Die Hauptdokumentation befindet sich in der **YouTrack Wissensdatenbank**:

👉 **[Meldestelle Command Center](https://meldestelle-pro.youtrack.cloud/knowledge-bases)**

### Was du in YouTrack findest:

- 🏗️ **Bounded Context Dokumentation** (Members, Horses, Events, Masterdata)
- 📡 **API-Referenz** (automatisch aus KDoc generiert)
- 🚀 **Deployment-Guides** (Proxmox, Cloudflare, Nginx)
- 🔐 **Sicherheits-Konfigurationen** (Keycloak, GitHub Secrets)
- 💡 **Roadmap & Visionen**
- 📊 **Architektur-Diagramme** (interaktiv)

---

## 📂 Was im Repository bleibt

### 1. Architecture Decision Records (ADRs)

Architekturentscheidungen sind Teil der Code-Historie und werden im Repository versioniert:

- [ADR Übersicht](architecture/adr/..)
- [ADR-0001: Modulare Architektur](architecture/adr/0001-modular-architecture-de.md)
- [ADR-0002: Domain-Driven Design](architecture/adr/0002-domain-driven-design-de.md)
- [ADR-0003: Microservices](architecture/adr/0003-microservices-architecture-de.md)
- [ADR-0004: Event-Driven Communication](architecture/adr/0004-event-driven-communication-de.md)
- [ADR-0005: Polyglot Persistence](architecture/adr/0005-polyglot-persistence-de.md)
- [ADR-0006: Authentication & Authorization (Keycloak)](architecture/adr/0006-authentication-authorization-keycloak-de.md)
- [ADR-0007: API Gateway Pattern](architecture/adr/0007-api-gateway-pattern-de.md)
- [ADR-0008: Multiplatform Client Applications](architecture/adr/0008-multiplatform-client-applications-de.md)

### 2. C4-Diagramme (PlantUML-Quellen)

Versionierte Diagramm-Quellen für Architekturdokumentation:

- [C4 Context](architecture/c4/01-context-de.puml)
- [C4 Container](architecture/c4/02-container-de.puml)
- [C4 Component - Events Service](architecture/c4/03-component-events-service-de.puml)

### 3. Developer Guides

Minimale Anleitungen für lokale Entwicklung:

- **[Lokales Setup](how-to/start-local.md)** - Projekt in 5 Minuten starten
- **[KDoc Style Guide](how-to/kdoc-style.md)** - Dokumentations-Konventionen im Code
- **[Branch-Schutz & PR-Workflow](how-to/branchschutz-und-pr-workflow.md)** - Git-Workflow

---

## 🔄 Automatische Synchronisation

Das Projekt nutzt automatisierte Workflows für Konsistenz:

- **KDoc → YouTrack**: [docs-kdoc-sync.yml](../.github/workflows/docs-kdoc-sync.yml) - Synchronisiert API-Dokumentation
  aus Code-Kommentaren nach YouTrack
- **Docker SSoT**: [ssot-guard.yml](../.github/workflows/ssot-guard.yml) - Validiert Docker-Versionskonsistenz
- **CI Pipeline**: [ci-main.yml](../.github/workflows/ci-main.yml) - Hauptpipeline für Build, Tests, Validierung

---

## 📋 Dokumentations-Workflow

### Für Code-Änderungen:

1. KDoc im Code schreiben
2. PR erstellen → CI validiert
3. Nach Merge → KDoc-Sync pusht automatisch nach YouTrack

### Für Architektur-Entscheidungen:

1. ADR in `docs/architecture/adr/` erstellen
2. PR mit ADR-Review
3. Nach Merge → Zusammenfassung in YouTrack verlinken

### Für Infrastruktur/Konfiguration:

1. Dokumentation direkt in YouTrack erstellen
2. Bei Code-relevanten Änderungen → im Commit-Message auf YouTrack-Artikel verweisen

---

## ❓ Fragen & Support

- **Technische Fragen**: [GitHub Discussions](https://github.com/your-org/meldestelle/discussions)
- **Bugs**: [GitHub Issues](https://github.com/your-org/meldestelle/issues)
- **Architektur-Diskussionen**: [YouTrack](https://meldestelle-pro.youtrack.cloud)
- **Projekt-Dokumentation**: [YouTrack Wissensdatenbank](https://meldestelle-pro.youtrack.cloud/knowledge-bases)

---

**Hinweis**: Diese README wurde am 30. Oktober 2025 aktualisiert im Rahmen der Dokumentations-Migration nach YouTrack (
siehe ADR-0009 - folgt).
