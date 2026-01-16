# PENDING DECISIONS: Backend Infrastructure & Architecture

**Status:** OPEN
**Date:** 2026-01-15
**Requester:** Senior Backend Developer
**Target:** Lead Architect

---

## Context
During the analysis of the `backend/infrastructure` modules for the "Operation Tracer Bullet" (Phase 1), several inconsistencies were found that block the clean implementation of the `ping-service`.

## Questions & Decisions Required

### 1. Persistence Strategy: JPA vs. Exposed (BLOCKER)
**Observation:**
*   `ping-service` imports `spring-boot-starter-data-jpa` (Hibernate).
*   `backend/infrastructure/persistence` provides `DatabaseUtils` exclusively based on **Exposed** (Kotlin SQL Framework).
*   Result: Two competing ORM approaches in the classpath.

**Question:**
What is the standard technology for our microservices?
*   **Option A (JPA/Hibernate):** The standard Spring way. `infrastructure/persistence` needs to be refactored.
*   **Option B (Exposed):** The "Kotlin-Native" way. JPA must be removed from `ping-service`.

*Recommendation:* Exposed fits better with DDD/Kotlin (less magic), but JPA has better Spring integration.

### 2. Security Shared Module
**Observation:**
Roadmap requires OAuth2/RBAC. Currently, there is no `backend/infrastructure/security` module. Implementing security directly in `ping-service` violates DRY.

**Question:**
Should a `backend/infrastructure/security` module be extracted in Phase 1 to encapsulate standard config (Resource Server, CORS, Role Mapping)?

### 3. Messaging vs. Sync Protocol
**Observation:**
Roadmap specifies "Delta-Sync APIs" for Offline-First.
`backend/infrastructure/messaging` contains heavy **Kafka** (Reactor Kafka) dependencies.

**Question:**
Does `ping-service` need Kafka for the "Tracer Bullet"?
*   If Sync is REST-based (Pull), Kafka might be over-engineering for Phase 1.
*   Should `PingEvents` be stored in DB (Outbox) or pushed to Kafka?

### 4. Database Migration (Flyway)
**Observation:**
Flyway is required. No convention exists for script location.

**Question:**
Where should Flyway scripts (`V1__init.sql`) be located?
*   **Option A:** Inside the service (`ping-service/src/main/resources/db/migration`) -> "Database per Service".
*   **Option B:** Centralized.

*Recommendation:* Option A.

---

## Next Steps
Please resolve these questions (especially #1) so development can proceed.
