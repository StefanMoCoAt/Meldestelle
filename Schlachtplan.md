### Schlachtplan für das 'infrastructure'-Modul

Basierend auf der Analyse des aktuellen Zustands (Stand: 11. Oktober 2025) habe ich einen strukturierten Aktionsplan erstellt. Die letzte größere Aktualisierung war im Juli 2025, seitdem gab es signifikante Änderungen am Gateway-Modul.

---

### 🔴 Phase 1: SOFORT (Diese Woche)

#### 1.1 Gateway-Tests reparieren (Höchste Priorität)

**Problem:** Tests sind komplett defekt - nur ~47% funktionieren noch (25/53 Tests).

**Aktionen:**

- ❌ **Löschen:** `JwtAuthenticationTests.kt` - testet nicht-existierende Custom-Filter
- ✅ **Behalten:** `FallbackControllerTests.kt`, `GatewayApplicationTests.kt`
- ✏️ **Überarbeiten:** `GatewayRoutingTests.kt`, `GatewaySecurityTests.kt`, `GatewayFiltersTests.kt`
  - Option A: Tests mit MockJWT-Tokens ausstatten (siehe `TestSecurityConfig.kt`)
  - Option B: Tests auf Public Paths verlegen (`/actuator/**`, `/fallback/**`)
  - Option C: Security in Tests deaktivieren

**Warum jetzt:** Tests geben keine Sicherheit mehr – blockiert Entwicklung.

**Zeitaufwand:** 4–6 Stunden

---

#### 1.2 Gateway-Build-Datei bereinigen

**Problem:** Duplizierte Dependency in `gateway/build.gradle.kts` (Zeile 33-34).

**Aktion:**

```kotlin
// ENTFERNEN: Zeile 34
implementation(project(":infrastructure:event-store:redis-event-store"))  // ← Duplikat!
```

**Zeitaufwand:** 5 Minuten

---

### 🟡 Phase 2: KURZFRISTIG (Nächste 2 Wochen)

#### 2.1 Dependency-Versionen aktualisieren

**Problem:** Versionen von Juli 2025 – teilweise veraltet.

**Zu prüfen und aktualisieren:**

| Dependency        | Aktuell  | Latest (Okt 2025) | Priorität |
|-------------------|----------|-------------------|-----------|
| Spring Boot       | 3.5.5    | 3.5.x             | Mittel    |
| Spring Cloud      | 2025.0.0 | 2025.0.x          | Mittel    |
| Kotlin            | 2.2.20   | 2.2.x             | Niedrig   |
| Keycloak          | 26.0.7   | 26.x.x            | Hoch      |
| Testcontainers    | 1.21.3   | 1.21.x            | Niedrig   |
| PostgresQL Driver | 42.7.7   | 42.7.x            | Niedrig   |

**Aktion:**

1. `gradle/libs.versions.toml` aktualisieren
2. Tests nach jedem Update ausführen
3. Breaking Changes dokumentieren

**Zeitaufwand:** 1–2 Tage (mit Testing)

---

#### 2.2 Docker-Images aktualisieren

**Problem:** Einige Docker-Images sind möglicherweise veraltet.

**Zu prüfen:**

```yaml
# docker-compose.yml
postgres: 16-alpine           # ✅ Aktuell (neueste: 16.x)
redis: 7-alpine               # ✅ Aktuell
keycloak: 26.4.0              # ⚠️ Prüfen auf 26.x updates
consul: 1.15                  # ⚠️ Prüfen (neueste: 1.20+)
kafka: 7.4.0                  # ⚠️ Prüfen (neueste: 7.8+)
prometheus: v2.54.1           # ⚠️ Prüfen
grafana: 11.3.0               # ✅ Wahrscheinlich aktuell
```

**Aktion:**

1. Versions-Check durchführen
2. Schrittweise aktualisieren (einzeln testen!)
3. `.env`-Datei mit Versions-Variablen anlegen

**Zeitaufwand:** 3–4 Stunden

---

#### 2.3 Monitoring-Modul vervollständigen

**Problem:** Nur 3 Kotlin-Files – deutlich unter implementiert im Vergleich zur Dokumentation.

**Dokumentiert, aber fehlt:**

- Distributed Tracing (Zipkin) - Docker-Container fehlt!
- Custom Metrics Implementation
- Health Check Aggregation
- Alerting Rules Implementation

**Aktion:**

1. Zipkin zu `docker-compose.yml` hinzufügen
2. Tracing-Integration in Gateway testen
3. Custom Metrics-Library erstellen
4. Prometheus Alerting Rules konfigurieren

**Zeitaufwand:** 2–3 Tage

---

### 🟢 Phase 3: MITTELFRISTIG (Nächste 4–6 Wochen)

#### 3.1 Dokumentation aktualisieren

**Problem:** README von Juli 2025 – nicht mehr aktuell.

**Zu aktualisieren:**

**`README-INFRASTRUCTURE.md`:**

- Zeile 552: "Letzte Aktualisierung: 25. Juli 2025" → Oktober 2025
- Security-Sektion: OAuth2 Resource Server statt Custom JWT Filter
- Keycloak Version: 23.0 → 26.4.0
- Kafka Version: 7.5.0 → 7.4.0 (Downgrade dokumentieren!)
- Monitoring: Zipkin-Konfiguration ergänzen

**Neue Sections hinzufügen:**

- #### Bekannte Limitierungen

- #### Migration Notes (Juli → Oktober 2025)

- #### Troubleshooting erweitern

**Zeitaufwand:** 1 Tag

---

#### 3.2 Auth-Module überarbeiten

**Problem:** Vermutlich veraltet - Custom JWT vs. OAuth2 Resource Server Diskrepanz.

**Zu klären:**

- Werden `auth-client` und `auth-server` noch verwendet?
- Redundanz mit Gateway's OAuth2 Resource Server?
- Keycloak-Integration vereinheitlichen

**Aktion:**

1. Abhängigkeiten zu auth-Modulen analysieren
2. Entscheiden: Refactoring oder Deprecation
3. Wenn deprecated: Migration Path dokumentieren

**Zeitaufwand:** 3–5 Tage

---

#### 3.3 Cache-Module modernisieren

**Problem:** Redis 7 ist aktuell, aber Implementation-Patterns könnten veraltet sein.

**Zu prüfen:**

- Multi-Level Caching tatsächlich implementiert?
- Cache Statistics vorhanden?
- TTL Management korrekt?
- Integration mit Spring Cache Abstraction?

**Aktion:**

1. Cache-Tests erweitern
2. Performance-Metriken hinzufügen
3. Cache-Warming Strategy implementieren

**Zeitaufwand:** 2–3 Tage

---

#### 3.4 Event-Store Performance-Optimierung

**Problem:** Redis-basiert - für Production ggf. nicht optimal.

**Zu evaluieren:**

- Ist Redis der richtige Event Store für Production?
- Alternative: PostgresQL mit Event Store Pattern?
- Snapshot-Strategie tatsächlich implementiert?

**Aktion:**

1. Performance-Tests durchführen
2. Event Store Benchmark (Redis vs. PostgresQL)
3. Dokumentation aktualisieren mit Pros/Cons

**Zeitaufwand:** 1 Woche

---

### 🔵 Phase 4: LANGFRISTIG (Nächste 2–3 Monate)

#### 4.1 Service Mesh evaluieren

**Dokumentiert in "Zukünftige Erweiterungen"** – noch nicht implementiert.

**Optionen:**

- Istio (komplex, feature-reich)
- Linkerd (leichtgewichtig)
- Consul Connect (bereits Consul vorhanden!)

**Empfehlung:** Start mit Consul Connect - minimaler Overhead.

**Zeitaufwand:** 2–3 Wochen

---

#### 4.2 OpenTelemetry statt Zipkin

**Problem:** Zipkin ist veraltet – OpenTelemetry ist der moderne Standard.

**Migration Path:**

1. OpenTelemetry Collector aufsetzen
2. Spring Boot Auto-Instrumentation aktivieren
3. Zipkin als Backend behalten (kompatibel!)
4. Schrittweise migrieren

**Zeitaufwand:** 1–2 Wochen

---

#### 4.3 Security Hardening

**Aktuelle Gaps:**

- JWT Token Rotation nicht implementiert
- Rate Limiting nur dokumentiert, nicht konfiguriert
- Audit Logging fehlt
- HTTPS/TLS noch nicht erzwungen

**Aktion:**

1. Rate Limiting im Gateway aktivieren
2. Audit Log Framework implementieren
3. TLS für Service-zu-Service-Kommunikation
4. Security Scan mit OWASP Dependency Check

**Zeitaufwand:** 2–3 Wochen

---

#### 4.4 Infrastructure as Code (IaC)

**Problem:** Nur Docker Compose – für Production nicht ausreichend.

**Zu erstellen:**

- Kubernetes Manifests (aktualisieren - Zeile 393+)
- Helm Charts (aktualisieren – Zeile 420+)
- Terraform für Cloud-Ressourcen
- CI/CD Pipelines

**Zeitaufwand:** 4–6 Wochen

---

### 📊 Priorisierung-Matrix

| Phase | Aufgabe       | Dringlichkeit | Aufwand | Impact  |
|-------|---------------|---------------|---------|---------|
| 1     | Gateway-Tests | 🔴 Sehr hoch  | 4-6h    | Hoch    |
| 1     | Build-Datei   | 🔴 Sehr hoch  | 5min    | Niedrig |
| 2     | Dependencies  | 🟡 Hoch       | 1-2d    | Mittel  |
| 2     | Docker-Images | 🟡 Hoch       | 3-4h    | Mittel  |
| 2     | Monitoring    | 🟡 Mittel     | 2-3d    | Hoch    |
| 3     | Dokumentation | 🟢 Mittel     | 1d      | Mittel  |
| 3     | Auth-Module   | 🟢 Mittel     | 3-5d    | Hoch    |
| 3     | Cache         | 🟢 Niedrig    | 2-3d    | Mittel  |
| 3     | Event-Store   | 🟢 Niedrig    | 1w      | Mittel  |
| 4     | Service Mesh  | 🔵 Niedrig    | 2-3w    | Hoch    |
| 4     | OpenTelemetry | 🔵 Niedrig    | 1-2w    | Mittel  |
| 4     | Security      | 🔵 Mittel     | 2-3w    | Hoch    |
| 4     | IaC           | 🔵 Niedrig    | 4-6w    | Hoch    |

---

### 🎯 Empfohlene Reihenfolge

#### Woche 1-2

1. Gateway-Tests reparieren
2. Build-Datei bereinigen
3. Dependencies aktualisieren

#### Woche 3-4

4. Docker-Images aktualisieren
5. Monitoring vervollständigen
6. Dokumentation aktualisieren

#### Woche 5-8

7. Auth-Module evaluieren/refactored
8. Cache-Module modernisieren
9. Event-Store Performance-Tests

#### Monat 3-4

10. Security Hardening
11. OpenTelemetry Migration
12. Service Mesh Evaluation

#### Monat 5-6

13. Infrastructure as Code
14. Production Readiness Assessment

---

### 🛠️ Tooling-Empfehlungen

**Für Dependency-Management:**

- Renovate Bot oder Dependabot für automatische Updates
- `./gradlew dependencyUpdates` Plugin verwenden

**Für Security:**

- OWASP Dependency Check
- Trivy für Container-Scanning
- SonarQube für Code-Qualität

**Für Monitoring:**

- Grafana Dashboards aus Community importieren
- Prometheus Alertmanager konfigurieren

---

### 📝 Nächste Schritte

1. **Jetzt sofort:** Gateway-Tests fixen (blockiert alles andere)
2. **Diese Woche:** Dependencies updaten und testen
3. **Nächste Woche:** Sprint Planning für Phase 2
4. **Monatlich:** Review des Fortschritts und Repriorisierung

---

### ⚠️ Risiken & Abhängigkeiten

**Kritische Pfade:**

- Gateway-Tests müssen ZUERST behoben werden
- Dependency-Updates können Breaking Changes haben
- Auth-Refactoring könnte alle Services betreffen

**Externe Abhängigkeiten:**

- Keycloak Breaking Changes bei Major Updates
- Spring Boot/Cloud Release Schedule beachten
- Kubernetes Cluster für IaC-Phase benötigt

---

**Geschätzter Gesamtaufwand:** 6–8 Wochen (bei 1 Vollzeit-Entwickler)

**Empfohlener Start:** Sofort mit Phase 1, dann iterativ durch die Phasen

---

### Documentations-Sprachbereinigung (2025-10-22)

Im Zuge der Vereinheitlichung auf ausschließlich deutschsprachige Dokumentation wurden folgende Dateien entfernt:

Gelöschte ADRs (englische Varianten):

- docs/architecture/adr/0000-adr-template.md
- docs/architecture/adr/0001-modular-architecture.md
- docs/architecture/adr/0002-domain-driven-design.md
- docs/architecture/adr/0003-microservices-architecture.md
- docs/architecture/adr/0004-event-driven-communication.md
- docs/architecture/adr/0005-polyglot-persistence.md
- docs/architecture/adr/0006-authentication-authorization-keycloak.md
- docs/architecture/adr/0007-api-gateway-pattern.md
- docs/architecture/adr/0008-multiplatform-client-applications.md

Gelöschte C4-Diagramme (englische Varianten):

- docs/architecture/c4/01-context.puml
- docs/architecture/c4/02-container.puml
- docs/architecture/c4/03-component-events-service.puml

Hinweis:

- Alle verbleibenden ADRs und C4-Diagramme sind in deutscher Sprache vorhanden (Suffix-de) und verlinkt.
- Weitere Doku-Dateien in docs/ sind deutsch (Front-Matter/Sprachindizien geprüft).

---

## CI‑Stabilisierung Keycloak (2025‑10‑25)

Hintergrund: In GitHub Actions startete Keycloak zeitweise nicht zuverlässig. Ziel: Integrationstests stabilisieren, ohne produktive Architektur zu ändern.

Änderungen:

- Integration‑Workflow (`.github/workflows/integration-tests.yml`) auf Matrixbetrieb umgestellt:
  - `keycloak_db=postgres` (produktnäher, mit externer Postgres‑DB)
  - `keycloak_db=dev-file` (Dateibackend, ohne Postgres; stabiler im CI)
- Robuste Startlogik:
  - Aktives Warten auf Postgres (nur in `postgres`‑Variante)
  - Keycloak‑Start per `docker run … start-dev` (26.4.2) mit `KC_HEALTH_ENABLED=true`
  - Health‑Checks gegen `/`, `/health`, `/q/health`, `/health/ready`, Admin‑Konsole
  - Ausführliche Log‑Ausgabe bei Fehlern (Keycloak & Postgres)
- Fail‑fast deaktiviert; beide Matrix‑Jobs laufen unabhängig.

Nutzung/Operative Hinweise:

- In PRs beide Matrix‑Runs beachten; bei Flakes in `postgres` sichert `dev-file` die Tests ab.
- Logs bei Fehlschlag: Step „Dump service logs (Keycloak, Postgres)“ am Jobende öffnen.
- Produktiv bleibt Postgres maßgeblich (siehe `docker-compose.yml`).

ADR‑Konsistenz:

- ADR‑0006 (Keycloak) bleibt gültig und unverändert; die `dev-file`‑Variante betrifft ausschließlich CI‑Tests.

Next Steps (optional):

- Falls `postgres` im CI dauerhaft flakey: Required Checks vorübergehend auf `dev-file` begrenzen.
- Langfristig: Ursachenanalyse für Postgres‑Variante (Runner‑Leistung/Timeouts/Schema‑Setup) und Re‑Enable als Required Check nach Stabilisierung.

---
