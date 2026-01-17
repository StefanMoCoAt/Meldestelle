# Ping Service

Der `ping-service` ist der "Tracer Bullet" Service für die Meldestelle-Architektur. Er dient als Blueprint für alle weiteren Microservices.

## Verantwortlichkeit
*   Technischer Durchstich (Frontend -> Gateway -> Service -> DB).
*   Validierung der Infrastruktur (Security, Resilience, Observability).
*   Referenzimplementierung für DDD, Hexagonal Architecture und KMP-Integration.

## API Endpunkte

| Methode | Pfad | Beschreibung | Auth |
| :--- | :--- | :--- | :--- |
| GET | `/ping/simple` | Einfacher Ping, speichert in DB | Public |
| GET | `/ping/enhanced` | Ping mit Circuit Breaker Simulation | Public |
| GET | `/ping/public` | Expliziter Public Endpoint | Public |
| GET | `/ping/secure` | Geschützter Endpoint (benötigt Rolle) | **Secure** (MELD_USER) |
| GET | `/ping/health` | Health Check | Public |
| GET | `/ping/history` | Historie aller Pings | Public (Debug) |
| GET | `/ping/sync` | Delta-Sync für Offline-Clients | Public |

## Architektur
Der Service folgt der Hexagonalen Architektur (Ports & Adapters):
*   **Domain:** `at.mocode.ping.domain` (Pure Kotlin, keine Frameworks).
*   **Application:** `at.mocode.ping.application` (Use Cases, Spring Service).
*   **Infrastructure:** `at.mocode.ping.infrastructure` (Web, Persistence, Security).

## Security
*   Nutzt das zentrale Modul `backend:infrastructure:security`.
*   OAuth2 Resource Server (JWT Validation via Keycloak).
*   Rollen-Mapping: Keycloak Realm Roles -> Spring Security Authorities (`ROLE_...`).

## Persistence
*   Datenbank: PostgreSQL.
*   Migration: Flyway (`V1__init_ping.sql`).
*   ORM: Spring Data JPA (für Write Model).

## Resilience
*   Circuit Breaker: Resilience4j (für DB-Zugriffe und simulierte Fehler).

## Sync-Strategie (Phase 3)
*   Implementiert Delta-Sync via `/ping/sync`.
*   Parameter: `lastSyncTimestamp` (Long, Epoch Millis).
*   Response: Liste von `PingEvent` (ID, Message, LastModified).
*   Client kann basierend auf dem Timestamp nur neue/geänderte Daten abrufen.
