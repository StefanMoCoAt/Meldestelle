---
type: Reference
status: ACTIVE
owner: Backend Developer
tags: [backend, service, reference, ping]
---

# 🎯 Ping Service Reference

Der `ping-service` ist der **"Tracer Bullet"** (Leuchtspurgeschoss) der Meldestelle-Architektur. Er ist kein Wegwerf-Prototyp, sondern die **Referenzimplementierung** für alle zukünftigen Microservices.

## 1. Mission & Verantwortung

*   **Technischer Durchstich:** Beweist, dass die Kette *Frontend -> Gateway -> Service -> DB* funktioniert.
*   **Blueprint:** Definiert Standards für Architektur (DDD/Hexagonal), Testing, Security und Build-Prozesse.
*   **Infrastruktur-Validierung:** Testet die Integration mit Consul, Keycloak, Postgres, Redis und Zipkin.
*   **Offline-First Lab:** Hier wird die Delta-Sync-Logik (`/sync`) entwickelt und validiert, bevor sie in fachliche Services einzieht.

---

## 2. Technologie-Stack

*   **Framework:** Spring Boot 3.5.x (Spring MVC, Tomcat).
*   **Sprache:** Kotlin 2.x (Coroutines für asynchrone Abläufe).
*   **Datenbank:** PostgreSQL (via Spring Data JPA).
*   **Migration:** Flyway.
*   **Security:** OAuth2 Resource Server (JWT via Keycloak).
*   **Resilience:** Resilience4j (Circuit Breaker).
*   **API Contract:** KMP-Modul `:contracts:ping-api` (Shared Code mit Frontend).

---

## 3. Architektur (Hexagonal)

Der Service folgt strikt der **Ports & Adapters** (Hexagonal) Architektur:

1.  **Domain (`at.mocode.ping.domain`):**
    *   Der Kern. Enthält Entities (`Ping`) und Business-Regeln.
    *   Frei von Frameworks (kein Spring, kein JPA).
    *   Definiert Interfaces für Ports (`PingRepository`).

2.  **Application (`at.mocode.ping.application`):**
    *   Orchestriert die Use Cases (`PingUseCase`).
    *   Steuert Transaktionen (`@Transactional`).
    *   Verbindet Domain und Infrastructure.

3.  **Infrastructure (`at.mocode.ping.infrastructure`):**
    *   **Web:** `PingController` (REST API).
    *   **Persistence:** `PingRepositoryAdapter` (JPA Implementierung).
    *   **Security:** Global Config für JWT-Validierung.

---

## 4. API Endpunkte

| Methode | Pfad | Auth | Beschreibung |
| :--- | :--- | :--- | :--- |
| `GET` | `/ping/simple` | 🔓 Public | Erstellt einen Ping in der DB. Testet Schreibzugriff. |
| `GET` | `/ping/enhanced` | 🔓 Public | Testet Circuit Breaker. Parameter `simulate=true` löst Fehler aus. |
| `GET` | `/ping/health` | 🔓 Public | Gibt Status "UP" zurück. |
| `GET` | `/ping/public` | 🔓 Public | Expliziter Public-Test. |
| `GET` | `/ping/secure` | 🔒 **Secure** | Erfordert Token mit Rolle `MELD_USER`. Testet Auth-Flow. |
| `GET` | `/ping/sync` | 🔒 **Secure** | **Delta-Sync**. Liefert Änderungen seit `lastSyncTimestamp`. |

---

## 5. Getting Started

### A. Voraussetzungen
*   Java 25 (oder kompatibel).
*   Docker & Docker Compose (für Infrastruktur).

### B. Infrastruktur starten
Bevor der Service laufen kann, braucht er Datenbank und Keycloak.
```bash
# Im Root-Verzeichnis
docker compose --profile infra up -d
```

### C. Starten via Gradle (Lokal)
Ideal für Entwicklung und Debugging.
```bash
# Startet den Service im Profil "local"
./gradlew :backend:services:ping:ping-service:bootRun
```
*   **URL:** `http://localhost:8082` (Direktzugriff)
*   **Debug Port:** 5006

### D. Starten via Docker (Integration)
Testet den Service im Container-Verbund (hinter dem Gateway).
```bash
# Baut das Image und startet es zusammen mit dem Gateway
docker compose --profile backend up -d --build
```
*   **URL (via Gateway):** `http://localhost:8081/api/ping/...`

---

## 6. Konfiguration

Die Konfiguration erfolgt primär über `application.yml` und Environment-Variables (12-Factor App).

| Variable | Default (Docker) | Beschreibung |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8082` | Port des Services. |
| `POSTGRES_DB_URL` | `jdbc:postgresql://postgres:5432/...` | JDBC URL. |
| `SSEC_ISSUER_URI` | `http://keycloak:8080/...` | URL des Identity Providers (für Token-Check). |
| `CONSUL_HOST` | `consul` | Host für Service Discovery. |

### Profile
*   `local`: Für lokale Entwicklung (nutzt `localhost` Adressen).
*   `docker`: Für Betrieb im Docker-Netzwerk (nutzt Service-Namen wie `postgres`).
*   `test`: Für Unit/Integration-Tests (nutzt H2 oder Testcontainers).

---

## 7. Testing

*   **Unit Tests:** `./gradlew :backend:services:ping:ping-service:test`
*   **Manuelle Tests:** Siehe [Testing with Postman](../Guides/Testing_with_Postman.md).
