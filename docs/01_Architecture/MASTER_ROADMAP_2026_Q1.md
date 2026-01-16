# MASTER ROADMAP Q1 2026: "Operation Tracer Bullet"

**Status:** ACTIVE / SINGLE SOURCE OF TRUTH
**Owner:** Lead Software Architect
**Letztes Update:** 15.01.2026

---

## 1. Strategisches Ziel
Wir validieren die gesamte Architektur-Kette (Frontend -> Gateway -> Service -> DB) anhand des **Ping-Service**. Dieser Service dient als **technischer Blueprint** (Vorlage) für alle kommenden Fach-Services. Er muss "Production Ready" gehärtet sein, bevor wir Fachlichkeit implementieren.

**Aktueller technischer Stand:**
*   Build System: ✅ Grün (Gradle, Kotlin 2.3, Spring Boot 3.5.9, Spring Cloud 2025.0.1).
*   Code-Basis: ✅ `ping-service` existiert rudimentär.
*   Infrastruktur: ⚠️ Muss gehärtet werden.

---

## 2. Arbeitsaufträge an die AGENTS (Phasenplan)

### PHASE 1: Backend Hardening & Infrastructure (Woche 2)
*Ziel: Der Ping-Service läuft sicher, stabil und beobachtbar in der Docker-Umgebung.*

#### 👷 Agent: Senior Backend Developer
Deine Aufgabe ist es, den `ping-service` von einem "Hello World" zu einem Enterprise-Microservice zu machen.

*   [ ] **Security Implementation (Prio 1):**
    *   Konfiguriere Spring Security als OAuth2 Resource Server.
    *   Implementiere RBAC (Role Based Access Control) für `/ping/secure`.
    *   Stelle sicher, dass Tokens vom Keycloak (Docker) korrekt validiert werden.
*   [ ] **Resilience (Prio 2):**
    *   Aktiviere Resilience4j CircuitBreaker für Datenbank-Zugriffe.
    *   Implementiere Fallbacks (z.B. "Degraded Mode" wenn DB weg ist).
*   [ ] **Observability (Prio 3):**
    *   Aktiviere Spring Boot Actuator (Health, Info, Prometheus).
    *   Stelle sicher, dass Tracing-IDs (Micrometer/Zipkin) durchgereicht werden.
*   [ ] **Persistence Härtung:**
    *   Integriere **Flyway** für Datenbank-Migrationen (kein `ddl-auto` in Prod!).
    *   Implementiere einen "Deep Health Check" (`/actuator/health`), der DB und Cache aktiv prüft.

#### 🏗️ Agent: Infrastructure & DevOps
Deine Aufgabe ist die Stabilität der Laufzeitumgebung.

*   [ ] **Docker Environment:**
    *   Stabilisiere `docker-compose.yaml`. Alle Services (Consul, Keycloak, Postgres, Zipkin) müssen zuverlässig starten.
    *   Prüfe Migration von Redis zu **Valkey** (Open Source Härtung), wie im Hardening-Dokument vorgeschlagen.
*   [ ] **Gateway Config:**
    *   Konfiguriere Routen und CircuitBreaker im Spring Cloud Gateway für den `ping-service`.

---

### PHASE 2: Frontend Integration (Woche 3)
*Ziel: Das Frontend kann authentifiziert mit dem Backend sprechen.*

#### 🎨 Agent: KMP Frontend Expert
Deine Aufgabe ist die Anbindung des gehärteten Backends.

*   [ ] **HTTP Client Core:**
    *   Konfiguriere Ktor Client mit `AuthInterceptor` (Bearer Token Injection).
    *   Implementiere Global Error Handling (Umgang mit 401, 403, 503).
*   [ ] **Authentication Flow:**
    *   Implementiere den OIDC Login Flow (Keycloak) für Desktop und Web.
    *   Speichere Tokens sicher im Memory (AuthState).
*   [ ] **UI Implementation:**
    *   Baue einen Debug-Screen, der die Endpunkte `/ping/simple` und `/ping/secure` visualisiert.

---

### PHASE 3: Offline & Sync (Woche 4)
*Ziel: Datenkonsistenz auch bei Netzwerk-Verlust.*

#### 🤝 Joint Task Force (Backend & Frontend)
*   [ ] **Sync-Protokoll:**
    *   Implementierung des Delta-Syncs basierend auf `PingEvent` (UUIDv7 + Timestamp).
    *   Frontend: Speicherung in SQLDelight (lokal).
    *   Backend: Bereitstellung des Sync-Endpunkts.

---

## 3. Definition of Done (für Phase 1 & 2)
1.  `docker compose up` startet den kompletten Stack fehlerfrei.
2.  Frontend-User kann sich einloggen (Keycloak).
3.  Frontend zeigt Daten vom `ping-service` an.
4.  Beim Abschalten der DB zeigt der Service einen sauberen Fallback (CircuitBreaker offen).
5.  In Zipkin ist der komplette Request-Trace (Frontend -> Gateway -> Service -> DB) sichtbar.
