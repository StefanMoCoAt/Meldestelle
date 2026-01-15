# Backend Roadmap - Meldestelle

Dieses Dokument beschreibt die geplanten Schritte zur Weiterentwicklung der Backend-Infrastruktur. Der aktuelle Fokus liegt auf der **Härtung der Infrastruktur** unter Nutzung des `ping-service` als technischem Blueprint.

## Status Quo
*   **Blueprint:** `ping-service` ist funktional, dient aber nun als Zielobjekt für Infrastruktur-Verbesserungen.
*   **Technologie:** Spring Boot 3.5.9, Kotlin Coroutines, PostgreSQL.

## Meilensteine (Priorisiert)

### Meilenstein 1: Infrastruktur-Härtung (Fokus: Ping-Service)
*   [ ] **Observability & Monitoring:**
    *   Vollständige Integration von Spring Boot Actuator.
    *   Konfiguration von Micrometer/Prometheus Metriken.
    *   Logging-Standardisierung (Structured Logging/JSON für ELK/Loki).
*   [ ] **Security Baseline:**
    *   Integration von Spring Security mit Keycloak (OAuth2/OIDC).
    *   Absicherung der Endpunkte im `ping-service` (RBAC).
    *   Validierung der JWT-Tokens am API-Gateway.
*   [ ] **Resilience & Stability:**
    *   Härtung des Resilience4j Setups (Circuit Breaker, Bulkhead, Rate Limiter).
    *   Optimierung der Datenbank-Connection-Pools (HikariCP).
    *   Einführung von Flyway oder Liquibase für kontrollierte DB-Migrationen.

### Meilenstein 2: Build-System & CI/CD Readiness
*   [ ] **Gradle Refactoring:** Umstellung auf Version Catalog (`libs.versions.toml`) für alle Backend-Module.
*   [ ] **Test-Härtung:** Standardisierung der Integrationstests mit Testcontainers (Postgres, Keycloak, Redis).
*   [ ] **Docker-Optimierung:** Multi-stage Builds und Distroless-Images für reduzierte Angriffsfläche.

### Meilenstein 3: Rollout auf Fach-Services
*   [ ] Übertragung der gehärteten Standards vom `ping-service` auf `horses`, `members`, etc.
*   [ ] Dokumentation der "Production Readiness Checklist" für neue Services.

## Nächste Schritte (Vorschlag)
1.  **Audit des aktuellen `ping-service`:** Identifikation von Lücken in Security und Monitoring.
2.  **Zentralisierung der Abhängigkeiten:** Einführung der `libs.versions.toml`, um eine konsistente Basis für die Härtung zu haben.
