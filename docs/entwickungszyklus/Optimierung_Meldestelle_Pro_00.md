# TODO-Liste: Optimierung & Performance-Verbesserung für Meldestelle_Pro

**Datum:** 26. Juli 2025

Dieses Dokument listet die geplanten Optimierungsmaßnahmen in den Bereichen Developer Experience, Betrieb & Performance sowie strategische Weiterentwicklung auf.

---

### 1. Developer Experience (DevEx) & Code-Qualität

*Ziel: Die Effizienz, Qualität und Wartbarkeit der Softwareentwicklung maximieren.*

- [ ] **Logging-Strategie implementieren:**
    - [ ] Ein zentrales Logging-Framework (z.B. SLF4J mit Logback) im `core`-Modul definieren.
    - [ ] Alle `println`-Anweisungen im gesamten Projekt durch strukturierte Logger-Aufrufe ersetzen.
    - [ ] Ein konsistentes Log-Format mit Korrelations-IDs für die Nachverfolgung von Anfragen über Service-Grenzen hinweg etablieren.

- [ ] **Contract Testing einführen:**
    - [ ] Ein Framework für Contract Testing (z.B. Pact) evaluieren und in den Build-Prozess integrieren.
    - [ ] Einen ersten Contract zwischen `nennungs-service` (Consumer) und `members-service` (Provider) als Pilotprojekt erstellen.
    - [ ] Den Contract-Test in die CI/CD-Pipeline integrieren, um inkompatible Änderungen automatisch zu verhindern.

- [ ] **Resilience Patterns implementieren:**
    - [ ] Eine Bibliothek für Resilience Patterns (z.B. Resilience4j) in die Service-Kommunikation integrieren.
    - [ ] "Retry"-Mechanismus für kritische, temporär fehlschlagende Service-Aufrufe (z.B. bei der Nennungsvalidierung) implementieren.
    - [ ] "Circuit Breaker"-Muster für Services implementieren, die bei längeren Ausfällen eines abhängigen Services nicht blockieren sollen.

---

### 2. Betrieb & Performance

*Ziel: Ein schnelles, sicheres und zuverlässiges System im Live-Betrieb gewährleisten.*

- [ ] **Advanced Caching-Strategien umsetzen:**
    - [ ] "Cache Warming" für die `Masterdata-Domäne` implementieren, um Regelwerke beim Service-Start vorzuladen.
    - [ ] Ein Monitoring-Dashboard in Grafana für Cache-Metriken (Hit/Miss Ratios, Cache-Größe) erstellen.
    - [ ] Die Cache-Konfiguration (z.B. TTLs) basierend auf den Monitoring-Daten feinjustieren.

- [ ] **Datenbank-Performance proaktiv optimieren:**
    - [ ] Regelmäßige Analyse von SQL-Abfragen mit `EXPLAIN ANALYZE` in den Entwicklungs-Workflow integrieren.
    - [ ] Fehlende oder ineffiziente Indizes für die Kern-Entitäten (`Nennung`, `DomPerson` etc.) identifizieren und hinzufügen.
    - [ ] Connection-Pool-Größen für jeden Service basierend auf erwarteter Last optimieren.

- [ ] **Deployment auf Kubernetes vorbereiten:**
    - [ ] Helm-Charts für jeden Microservice erstellen, um das Deployment zu standardisieren.
    - [ ] Eine Strategie für "Rolling Updates" definieren, um Aktualisierungen ohne Downtime zu ermöglichen.
    - [ ] "Liveness"- und "Readiness"-Probes für alle Services implementieren, damit Kubernetes den Zustand der Services überwachen kann.

---

### 3. Strategische & Zukünftige Optimierungen

*Ziel: Die technologische Basis für zukünftige Anforderungen und Skalierbarkeit schaffen.*

- [ ] **Echtzeit-Updates mit WebSockets implementieren:**
    - [ ] Eine WebSocket-Schnittstelle im `ergebnis-service` und in der `client-app` implementieren.
    - [ ] Das "Live-Turnier-Cockpit" so umbauen, dass es Status-Änderungen und Zwischenergebnisse per Push-Nachricht erhält statt durch Polling.

- [ ] **GraphQL als API-Alternative evaluieren:**
    - [ ] Einen Prototyp für eine GraphQL-Schnittstelle (z.B. für die `events-service`-API) erstellen.
    - [ ] Die Komplexität und den Nutzen im Vergleich zu REST für unsere Client-Anwendungen bewerten.
    - [ ] Entscheiden, ob zukünftige oder bestehende APIs zusätzlich mit GraphQL angeboten werden sollen.
