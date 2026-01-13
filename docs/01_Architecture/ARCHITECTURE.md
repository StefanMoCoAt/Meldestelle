Repository-Architektur (MP-22)

Dieses Dokument beschreibt die Zielstruktur und das Mapping vom bisherigen Stand (Ist) zur neuen Struktur (Soll). Es begleitet Epic 2 (MP-22).

Zielstruktur (Top-Level)

backend/   Gateway, Discovery (optional), Services
  gateway   Spring Cloud Gateway
  discovery
  services
frontend/  KMP Frontend
  shells    Ausführbare Apps (Assembler)
  features  Vertical Slices (kein Feature→Feature)
  core      Shared Foundation (Design-System, Network, Local-DB, Auth, Domain)
    design-system
    domain
    network
    local-db
    navigation
docker/    Docker Compose, .env.example, Monitoring-/Core-Konfiguration
docs/      Architektur, ADRs, C4-Modelle, Guides
  adr      Architecture Decision Records (ADRs)
  c4       C4-Diagramme (PlantUML Quellen)

Ist → Soll Mapping (erste Tranche)

- Frontend
  - clients/app → frontend/shells/meldestelle-portal (verschieben in Folge-Commit)
  - clients/shared/common-ui → frontend/core/design-system (verschieben in Folge-Commit)
  - clients/shared/navigation → frontend/core/navigation (verschieben in Folge-Commit)

- Backend
  - infrastructure/gateway → backend/gateway (verschieben in Folge-Commit)
  - services/* → backend/services/* (verschieben in Folge-Commit)
  - Discovery (falls genutzt) → backend/discovery

- Docker
  - compose.yaml → docker/docker-compose.yml (neu angelegt, Makefile angepasst)
  - .env Handling → docker/.env.example (neu, als Template)

Build/Gradle

- settings.gradle.kts bleibt vorerst unverändert. Modul-Verschiebungen folgen in einem separaten Schritt mit angepassten include-Pfaden.
- Version Catalog (gradle/libs.versions.toml) bleibt die einzige Quelle der Versionswahrheit.

Richtlinien (Kurzfassung)

- Features kommunizieren ausschließlich über Routen (Navigation) und Shared-Modelle in frontend/core/domain.
- Kein manueller Authorization-Header – nur der DI-verwaltete apiClient aus frontend/core/network (Koin Named Binding).
- SQLDelight als Offline-SSoT: Schema/Migrationen zentral versionieren, UI liest stets lokal und synchronisiert im Hintergrund.

DI-Policy & Architecture Guards (MP-23)

- DI-Policy (Frontend)
  - Http‑Requests erfolgen ausschließlich über den via Koin bereitgestellten `apiClient` (named Binding) aus `:frontend:core:network`.
  - Manuelles Setzen des `Authorization`‑Headers ist verboten. Token‑Handling wird zentral im `apiClient` konfiguriert (Auth‑Plugin/Interceptor).
  - Basis‑URL wird plattformspezifisch aufgelöst:
    - JVM/Desktop: Env `API_BASE_URL` (Fallback `http://localhost:8081`).
    - Web/JS: `globalThis.API_BASE_URL` (z. B. per `index.html` oder Proxy), sonst `window.location.origin`, Fallback `http://localhost:8081`.

- Architecture Guards (Frontend‑Scope)
  - Root‑Task `archGuards` bricht den Build ab, wenn verbotene Muster gefunden werden (manuelle `Authorization`‑Header). Tests sind ausgenommen; Backend ist ausgenommen.
  - Statische Analyse verfügbar über `detekt` und `ktlintCheck`; Aggregator `staticAnalysis` führt alles zusammen.

- Hinweise für Features
  - Features importieren keine anderen Features (Kommunikation über Navigation + Shared‑Domain‑Modelle). Eine explizite Detekt‑Regel folgt.
  - Netzwerkzugriffe in Features nutzen Koin über die App‑Shell (DI‑Bootstrap). Für schrittweise Migration kann eine Factory den `apiClient` optional beziehen.

Nächste Schritte (MP-22 Folgetasks)

1. Physisches Verschieben der Frontend-Module gemäß Mapping und Anpassung von settings.gradle.kts.
2. Physisches Verschieben der Backend-Komponenten in backend/* inkl. evtl. Package-Pfade, sofern notwendig.
3. Ergänzung von docker-compose.services.yml und docker-compose.clients.yml mit echten Overlays.
4. Erstellen der ersten ADRs unter `docs/01_Architecture/adr/` (Koin, SQLDelight, Optimistic Locking, Freshness UI, Core Domain).

Hinweis: ADRs liegen ab sofort zentral unter `docs/01_Architecture/adr/`. C4-Diagramme liegen unter `docs/01_Architecture/c4/`.
