Repository-Architektur (MP-22)

Dieses Dokument beschreibt die Zielstruktur und das Mapping vom bisherigen Stand (Ist) zur neuen Struktur (Soll). Es begleitet Epic 2 (MP-22).

Zielstruktur (Top-Level)

backend/   Gateway, Discovery (optional), Services
  gateway
  discovery
  services
frontend/  KMP Frontend
  shells    Ausführbare Apps (Assembler)
  features  Vertical Slices (kein Feature→Feature)
  core      Shared Foundation (Design-System, Network, Local-DB, Auth, Domain)
docker/    Docker Compose, .env.example, Monitoring-/Core-Konfiguration
docs/      Architektur, ADRs, C4-Modelle, Guides

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

Nächste Schritte (MP-22 Folgetasks)

1. Physisches Verschieben der Frontend-Module gemäß Mapping und Anpassung von settings.gradle.kts.
2. Physisches Verschieben der Backend-Komponenten in backend/* inkl. evtl. Package-Pfade, sofern notwendig.
3. Ergänzung von docker-compose.services.yml und docker-compose.clients.yml mit echten Overlays.
4. Erstellen der ersten ADRs unter docs/adr (Koin, SQLDelight, Optimistic Locking, Freshness UI, Core Domain).
