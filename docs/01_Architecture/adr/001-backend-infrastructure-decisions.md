# ADR 001: Backend Infrastructure & Architecture Decisions

**Status:** ACCEPTED
**Date:** 2026-01-15
**Author:** Lead Architect
**Context:** "Operation Tracer Bullet" (Phase 1) - Hardening of the `ping-service`.

---

## 1. Persistence Strategy: JPA vs. Exposed

**Decision:** **Hybrid Approach (Command/Query Separation)**
*   **Primary (Command/Write):** We use **JPA (Hibernate)** for the standard "Write Model" in our microservices.
    *   *Reason:* Best integration with Spring Data, transaction management, and validation. Standard for Enterprise Spring Boot.
*   **Secondary (Query/Read/Batch):** We allow **Exposed** for complex read queries or bulk operations where JPA overhead is too high.
    *   *Reason:* Kotlin-native, type-safe SQL generation, better performance for read-heavy operations.

**Action:**
*   The `backend/infrastructure/persistence` module will support **both**.
*   `ping-service` will primarily use **JPA** for its entities (`PingEntity`).
*   We will NOT remove JPA from `ping-service`.
*   We will NOT remove Exposed from `infrastructure/persistence`.

## 2. Security Shared Module

**Decision:** **Extract `backend/infrastructure/security`**
*   **Reason:** We strictly follow DRY (Don't Repeat Yourself). Security configuration (OAuth2 Resource Server, JWT Converter, CORS, Global Method Security) is identical for all microservices.
*   **Scope:**
    *   `SecurityConfig`: Standard `SecurityFilterChain`.
    *   `KeycloakRoleConverter`: Extracting roles from JWT.
    *   `CorsConfig`: Centralized CORS policy.

**Action:**
*   Create `backend/infrastructure/security`.
*   Move security logic from `ping-service` (if any) to this module.

## 3. Messaging vs. Sync Protocol

**Decision:** **REST-based Pull (Phase 1) -> Kafka (Phase 3)**
*   **Phase 1 (Tracer Bullet):** We do **NOT** use Kafka for the simple `ping-service` yet.
    *   *Reason:* Keep the "Tracer Bullet" simple. We want to validate the HTTP/Auth chain first.
*   **Phase 3 (Offline Sync):** We will introduce Kafka for the "Outbox Pattern" later.
    *   *Reason:* Reliable event delivery for offline clients requires a durable log.

**Action:**
*   Remove `reactor-kafka` dependency from `ping-service` for now to reduce noise.
*   Focus on `PingEntity` (JPA) and REST endpoints.

## 4. Database Migration (Flyway)

**Decision:** **Database per Service (Option A)**
*   **Reason:** Microservices autonomy. Each service owns its schema.
*   **Location:** `src/main/resources/db/migration` inside each service module.
*   **Naming:** `V{Version}__{Description}.sql` (e.g., `V1__init_ping_schema.sql`).

**Action:**
*   `ping-service` must contain `V1__init.sql`.
*   `spring.flyway.enabled=true` in `application.yml`.

---

## Summary of Tasks for Senior Backend Developer

1.  **Persistence:** Use JPA for `PingEntity`.
2.  **Security:** Wait for `infrastructure/security` module (Architect will create skeleton) OR start implementing in `ping-service` and refactor later (preferred: Architect creates module now).
3.  **Messaging:** Ignore Kafka for now.
4.  **Flyway:** Create `V1__init.sql` in `ping-service`.
