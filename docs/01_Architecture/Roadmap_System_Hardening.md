# Roadmap: System-Instandsetzung & Härtung (via Ping-Service)

Diese Roadmap beschreibt den Plan, den **Ping-Service** als Speerspitze für die technische Instandsetzung und Härtung der gesamten Microservices-Infrastruktur zu nutzen.

**Status:** Entwurf (Bereit für Start am 15.01.2026)
**Lead:** Software Architect

---

## Phase 1: Infrastruktur-Diagnose (Instandsetzung)
*Ziel: Sicherstellen, dass alle Basis-Komponenten stabil miteinander sprechen.*

- [ ] **Deep-Health-Check im Ping-Service:**
    - Implementierung eines `/api/v1/ping/deep` Endpunkts.
    - Aktive Prüfung der PostgreSQL-Verbindung (via `Exposed`).
    - Aktive Prüfung der Cache-Verbindung (Valkey/Redis via `Lettuce`).
    - Validierung der Consul-Registrierung und Config-Abfrage.
- [ ] **Infrastruktur-Migration (Open Source Härtung):**
    - Umstellung von Redis auf **Valkey** im `docker-compose.yaml`.
    - Anpassung der Verbindungs-Parameter im Ping-Service.

## Phase 2: Resilience & Stabilität (Härtung)
*Ziel: Fehlertoleranz gegenüber Infrastruktur-Ausfällen.*

- [ ] **Resilience4j Integration:**
    - Konfiguration eines Circuit Breakers für DB-Zugriffe im Ping-Service.
    - Implementierung eines Fallback-Mechanismus (z.B. "Degraded Mode" Antwort).
- [ ] **Timeout-Härtung:**
    - Definition und Test von strikten Connect- und Read-Timeouts in der `application.yaml`.
- [ ] **Gateway-Integration:**
    - Test des Rate-Limitings am Gateway speziell für den Ping-Service.

## Phase 3: Security & Observability
*Ziel: Absicherung der Kommunikationswege und lückenlose Überwachung.*

- [ ] **Security-Chain Validierung:**
    - Aktivierung der JWT-Prüfung im Ping-Service.
    - Test der "Service-to-Service" Kommunikation mit Mock-Tokens.
- [ ] **Tracing-Check:**
    - Verifikation der Micrometer Tracing Integration (Trace-IDs in Logs und Zipkin).

## Phase 4: Standardisierung (Blueprint)
*Ziel: Übertragung der Erkenntnisse auf alle anderen Services.*

- [ ] **Refactoring Common-Module:**
    - Verschieben der bewährten Health- und Resilience-Konfigurationen in ein `core-backend` Modul.
- [ ] **Update der Dokumentation:**
    - Aktualisierung der Service-Templates basierend auf dem Ping-Service "Blueprint".

---

## Nächster Schritt (Morgen):
Start mit **Phase 1**: Implementierung des `Deep-Ping` Endpunkts und Vorbereitung der Valkey-Migration.
