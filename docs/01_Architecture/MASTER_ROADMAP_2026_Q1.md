---
type: Roadmap
status: ACTIVE
owner: Lead Architect
last_update: 2026-02-01
---

# MASTER ROADMAP Q1 2026: "Operation Tracer Bullet"

**Strategisches Ziel:**
Wir validieren die gesamte Architektur-Kette (Frontend -> Gateway -> Service -> DB) anhand des **Ping-Service**. Dieser Service dient als **technischer Blueprint** (Vorlage) für alle kommenden Fach-Services. Er muss "Production Ready" gehärtet sein, bevor wir Fachlichkeit implementieren.

**Aktueller technischer Stand (01.02.2026):**
*   Build System: ✅ Grün (Gradle, Kotlin 2.3, Spring Boot 3.5.9, Spring Cloud 2025.0.1).
*   Code-Basis: ✅ `ping-service` existiert, Delta-Sync implementiert.
*   Infrastruktur: ✅ Docker Environment stabil (Valkey, Keycloak, Consul, Zipkin).
*   Frontend: ✅ Web-App & Desktop-App (KMP), Login funktioniert, Sync-Logik vorhanden.

---

## 2. Arbeitsaufträge an die AGENTS (Phasenplan)

### PHASE 1: Backend Hardening & Infrastructure (ABGESCHLOSSEN)
*Ziel: Der Ping-Service läuft sicher, stabil und beobachtbar in der Docker-Umgebung.*

#### 👷 Agent: Senior Backend Developer
*   [x] **Security Implementation:** OAuth2 Resource Server & RBAC implementiert.
*   [x] **Resilience:** CircuitBreaker für `/ping/enhanced` aktiv.
*   [x] **Observability:** Actuator, Micrometer & Zipkin Tracing aktiv.
*   [x] **Persistence:** Flyway & Postgres Integration stabil.

#### 🏗️ Agent: Infrastructure & DevOps
*   [x] **Docker Environment:** `dc-infra`, `dc-backend`, `dc-gui` stabil. Valkey als Redis-Ersatz integriert.
*   [x] **Gateway Config:** Routing `/api/ping/**` -> `ping-service` mit CircuitBreaker Fallback konfiguriert.

---

### PHASE 2: Frontend Integration (ABGESCHLOSSEN)
*Ziel: Das Frontend kann authentifiziert mit dem Backend sprechen.*

#### 🎨 Agent: KMP Frontend Expert
*   [x] **HTTP Client Core:** Ktor Client mit Auth & Error Handling.
*   [x] **Authentication Flow:** OIDC Login Flow (Keycloak) implementiert.
*   [x] **UI Implementation:** Debug-Screen für Pings vorhanden.

---

### PHASE 3: Offline & Sync (ABGESCHLOSSEN)
*Ziel: Datenkonsistenz auch bei Netzwerk-Verlust.*

#### 🤝 Joint Task Force (Backend & Frontend)
*   [x] **Sync-Protokoll:** `PingEvent` Contract definiert.
*   [x] **Sync-Fix (CRITICAL):** Typ-Mismatch behoben! Backend und Frontend nutzen nun konsistent `since: Long`.
*   [x] **Web-App Sync:** SQLDelight Integration vorbereitet.

---

## 3. Definition of Done (für Phase 1 & 2)
1.  [x] `docker compose up` startet den kompletten Stack fehlerfrei.
2.  [x] Frontend-User kann sich einloggen (Keycloak).
3.  [x] Frontend zeigt Daten vom `ping-service` an.
4.  [x] Beim Abschalten der DB zeigt der Service einen sauberen Fallback (CircuitBreaker offen).
5.  [x] In Zipkin ist der komplette Request-Trace (Frontend -> Gateway -> Service -> DB) sichtbar.

## 4. Next Steps (Q1/2026)
1.  **Entries Service:** Beginn der Implementierung des ersten echten Fach-Services ("Nennungen").
2.  **System Hardening:** Keycloak Production-Config (kein `start-dev`).
