---
type: Roadmap
status: ARCHIVED
owner: Lead Architect
last_update: 2026-01-15
---

# Roadmap: System Hardening & Stability

**Hinweis:** Dieses Dokument ist veraltet. Die Inhalte wurden in die `MASTER_ROADMAP_2026_Q1.md` integriert.

**Status:** Draft
**Priorität:** Hoch (Blocker für Feature-Entwicklung)

## 1. Backend & Build System (Architect / Backend Dev)

### 1.1 Dependency Management
- [ ] **Spring Cloud 2025.0.1 Downgrade:**
    - `libs.versions.toml`: Spring Cloud Version auf `2025.0.1` setzen.
    - `platform/build.gradle.kts`: BOM Import prüfen.
    - Ziel: Behebung der `ClassNotFoundException` im Gateway (CircuitBreaker).
- [ ] **Micrometer 1.16.1:**
    - Explizites Upgrade in `libs.versions.toml` für Java 25 Kompatibilität.
- [ ] **KMP Database Cleanup:**
    - Entscheidung: SQLDelight für KMP Client.
    - Entfernen von Room Dependencies (falls nicht zwingend benötigt).
    - Exposed Version im Backend prüfen (`0.5x` vs `1.0.0-rc`).

### 1.2 Modul-Struktur
- [ ] **`core-utils` Refactoring:**
    - Verschieben von `DatabaseUtils` (JVM-Code) aus `core-utils` nach `:backend:infrastructure:persistence`.
    - Sicherstellen, dass `core-utils` rein `commonMain` kompatibel ist.

---

## 2. Infrastructure & DevOps (DevOps Engineer)

### 2.1 Docker Environment
- [ ] **Redis -> Valkey Migration:**
    - Prüfen, ob wir Redis durch Valkey (Open Source Fork) ersetzen, um Lizenzprobleme zu vermeiden.
    - Update `docker-compose.yaml`.
- [ ] **Keycloak Härtung:**
    - Export der Realm-Config (`meldestelle-realm.json`) und Mounten im Container (statt manueller Config).
    - Sicherstellen, dass `frontend-client` korrekte Redirect-URIs für Desktop & Web hat.

### 2.2 Observability
- [ ] **Zipkin Integration:**
    - Prüfen, ob Traces vom Gateway bis zur DB durchgereicht werden.
    - Ggf. `micrometer-tracing-bridge-brave` konfigurieren.

---

## 3. Frontend (Frontend Expert)

### 3.1 Build Fixes
- [ ] **Wasm Worker Fix:**
    - Behebung der `Unresolved reference: Worker` Fehler im `composeApp:wasmJsBrowserDistribution` Task.
    - Prüfen der `kotlinx-browser` Version.

### 3.2 Auth Integration
- [ ] **OIDC Client:**
    - Implementierung des Login-Flows mit `ktor-client-auth` und Keycloak.
