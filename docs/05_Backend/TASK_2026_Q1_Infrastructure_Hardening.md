---
type: Task
status: OPEN
owner: Senior Backend Developer
created: 2026-01-15
priority: HIGH
context: Operation Tracer Bullet (Phase 1)
---

# Arbeitsanweisung: Infrastructure Hardening & Security Implementation

**Ziel:** Finalisierung der Backend-Infrastruktur-Module und Härtung des `ping-service` gemäß [ADR 001](../01_Architecture/adr/001-backend-infrastructure-decisions.md).

---

## 1. Kontext & Architektur-Entscheidungen

Wir befinden uns in **Phase 1** ("Tracer Bullet"). Das Ziel ist ein stabiler, sicherer Durchstich vom Frontend bis zur Datenbank.
Die Architektur wurde wie folgt geschärft (siehe ADR 001):
*   **Persistence:** Hybrid-Ansatz (JPA für Writes/Entities, Exposed für komplexe Reads).
*   **Security:** Zentralisiertes Modul (`backend/infrastructure/security`).
*   **Messaging:** Kafka ist für Phase 1 **out of scope**. Fokus auf REST.
*   **Migration:** Flyway Skripte liegen direkt im Service (`db/migration`).

---

## 2. Deine Aufgaben (Checkliste)

### A. Security Module (`backend/infrastructure/security`)
Dieses Modul wurde neu angelegt. Fülle es mit Leben.

*   [ ] **Security Configuration:**
    *   Erstelle eine `SecurityConfig`-Klasse, die `SecurityFilterChain` konfiguriert.
    *   Implementiere OAuth2 Resource Server Support (JWT Validierung).
    *   Definiere globale CORS-Regeln (Frontend darf zugreifen).
*   [ ] **Role Converter:**
    *   Implementiere einen `KeycloakRoleConverter`, der die Rollen aus dem JWT (Realm/Resource Access) in Spring Security `GrantedAuthority` mappt.
*   **Wichtig:** Achte auf Kompatibilität. Das Gateway nutzt WebFlux (Reactive), die Services nutzen WebMVC (Servlet). Falls nötig, trenne die Konfigurationen oder nutze `ConditionalOnWebApplication`.

### B. Persistence Layer (`backend/infrastructure/persistence`)
Das Modul ist bereits konfiguriert.

*   [ ] **Verwendung im Service:**
    *   Stelle sicher, dass der `ping-service` dieses Modul nutzt.
    *   Implementiere `PingEntity` als JPA Entity.
    *   Nutze `JpaRepository` für Standard-CRUD-Operationen.

### C. Ping Service Hardening (`backend/services/ping/ping-service`)
Mache den Service "Production Ready".

*   [ ] **Flyway:**
    *   Erstelle `src/main/resources/db/migration/V1__init_ping.sql`.
    *   Definiere das Schema für die `ping` Tabelle.
*   [ ] **API Implementation:**
    *   Implementiere `/ping/public` (offen) und `/ping/secure` (benötigt Auth).
    *   Nutze `@PreAuthorize("hasRole('MELD_USER')")` o.ä. zum Testen der Rollen.
*   [ ] **Resilience:**
    *   Konfiguriere Resilience4j (CircuitBreaker) für die DB-Verbindung (via `application.yml`).

### D. Gateway Integration (`backend/infrastructure/gateway`)
*   [ ] **Routing:**
    *   Prüfe die `application.yml` im Gateway.
    *   Stelle sicher, dass Routen zum `ping-service` korrekt konfiguriert sind (via Service Discovery "ping-service").

---

## 3. Definition of Done

1.  Das Projekt kompiliert fehlerfrei (`./gradlew build`).
2.  `docker compose up` startet Gateway, Ping-Service, Keycloak und Postgres ohne Fehler.
3.  Ein Request auf `http://localhost:8080/ping/public` (via Gateway) liefert 200 OK.
4.  Ein Request auf `http://localhost:8080/ping/secure` ohne Token liefert 401 Unauthorized.
5.  Die Datenbank-Tabelle `ping` wurde durch Flyway automatisch erstellt.

---

**Referenzen:**
*   [ADR 001: Backend Infrastructure Decisions](../01_Architecture/adr/001-backend-infrastructure-decisions.md)
*   [Master Roadmap Q1 2026](../01_Architecture/MASTER_ROADMAP_2026_Q1.md)
