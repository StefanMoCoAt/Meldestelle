✅ TODO-Checkliste: Architektur-Validierung ("Tracer Bullet")
Phase 1: Backend-Infrastruktur vorbereiten
✅ Gradle-Setup bereinigen:

✅ In settings.gradle.kts sicherstellen, dass nur die platform-, core- und infrastructure-Module aktiv sind. Alle anderen (fachliche Services, Clients) müssen auskommentiert sein.

[ ] Konfiguration finalisieren:

[ ] Die AppConfig.kt mithilfe von Erweiterungsfunktionen (wie in PropertiesExtensions.kt) bereinigen, um Boilerplate-Code zu reduzieren.

[ ] Die Konfiguration um advertisedHost für den ServerConfig erweitern.

[ ] Logging-Infrastruktur implementieren:

[ ] Eine Logging-Bibliothek (z.B. SLF4J mit Logback) zu den platform-Abhängigkeiten hinzufügen.

[ ] Alle println-Aufrufe, insbesondere in ServiceRegistration.kt, durch strukturierte Logger-Aufrufe (logger.info, logger.error) ersetzen.

[ ] Gateway starten und testen:

[ ] Sicherstellen, dass der :infrastructure:gateway-Service gestartet werden kann.

Phase 2: "Ping-Service" als Test-Modul erstellen
[ ] Modul in Gradle anlegen:

[ ] In settings.gradle.kts eine neue Zeile hinzufügen: include(":temp:ping-service").

[ ] Ein build.gradle.kts für das neue Modul erstellen. Es benötigt Abhängigkeiten zu :core:core-utils und einem Web-Framework (z.B. Spring Boot, wie es im :masterdata-service verwendet wird).

[ ] Service-Implementierung:

[ ] Einen PingController mit einem GET /ping Endpunkt erstellen.

[ ] Die Endpunkt-Logik soll ein einfaches JSON zurückgeben, z.B. {"status": "pong", "timestamp": "..."}.

[ ] Einen logger.info("Ping endpoint called")-Aufruf in die Methode einfügen.

[ ] Service-Anwendung erstellen:

[ ] Eine main-Funktion für den Service erstellen, die:

[ ] Die AppConfig lädt.

[ ] Den ServiceRegistrar initialisiert und den Service bei Consul registriert.

[ ] Den eingebetteten Web-Server startet.

Phase 3: Minimalen Client für den Test anbinden
[ ] Client-Modul in Gradle aktivieren:

[ ] Den Kommentar für :client:web-app in settings.gradle.kts entfernen.

[ ] UI-Implementierung:

[ ] Eine einfache Seite in der Web-App erstellen.

[ ] Einen Button mit der Aufschrift "Ping Backend" hinzufügen.

[ ] Client-Logik:

[ ] Eine Funktion implementieren, die bei Klick auf den Button einen HTTP-Request an das Gateway sendet (z.B. http://localhost:GATEWAY_PORT/ping).

[ ] Die Antwort des Backends entgegennehmen und den status-Wert ("pong") auf der Seite anzeigen.

Phase 4: Gesamtsystem testen und aufräumen
[ ] Systemstart:

[ ] Die Docker-Infrastruktur starten: docker-compose up -d.

[ ] Den :infrastructure:gateway-Service starten.

[ ] Den :temp:ping-service starten.

[ ] Den :client:web-app-Service starten.

[ ] End-to-End-Test:

[ ] Die Web-App im Browser öffnen.

[ ] Auf den "Ping Backend"-Button klicken.

[ ] Erwartetes Ergebnis: Die Seite zeigt "pong" an.

[ ] Validierung:

[ ] Im Consul UI (üblicherweise http://localhost:8500) prüfen, ob der ping-service korrekt registriert ist.

[ ] Die Logs des Gateways und des ping-service auf die erwarteten Log-Meldungen überprüfen.

[ ] Aufräumen:

[ ] Wenn alles funktioniert, den aktuellen Stand in Git committen (z.B. "feat: Add stable infrastructure baseline").

[ ] Das :temp:ping-service-Modul und das :client:web-app-Modul in settings.gradle.kts wieder auskommentieren, um den Boden für den ersten echten Fach-Service vorzubereiten.


---

## Status-Update (September 2025)

Ergebnis: Der Trace-Bullet ist abgeschlossen. Folgende Punkte sind erledigt:
- [x] Gateway konfiguriert und startbar (inkl. Actuator/Prometheus, Tracing via monitoring-client)
- [x] Ping-Service implementiert, bei Consul registriert und via Gateway erreichbar
- [x] Circuit Breaker (Resilience4j) aktiv inkl. Fallbacks
- [x] Client (Desktop/Web) führt Ping über Gateway aus
- [x] Micrometer Tracing + Zipkin im Ping-Service und Gateway aktiv
- [x] CORS zentral im Gateway (globalcors) aktiv, service-lokales CORS entfernt
- [x] Einheitliches Logging-Pattern (traceId/spanId) über Logback
- [x] Prometheus-Scrapes für Gateway und Ping-Service

Zusätzlich eingeführt:
- Smoke-Skripte: `scripts/smoke/zipkin_smoke.sh` und `scripts/smoke/prometheus_smoke.sh`
- API-Härtung: `/ping` liefert nun status, timestamp, service
- Health Probes: Actuator-Probes für liveness/readiness aktiviert

Nächste Schritte (optional):
- [ ] Client-Auth (Keycloak) integrieren und End-to-End testen
- [ ] Weitere Services (members, horses, events) sukzessive ans Gateway hängen und observability prüfen
- [ ] Sampling-Rate für Produktion reduzieren (TRACING_SAMPLING_PROBABILITY=0.1)
- [ ] Optional: JSON-Logging für Containerbetrieb
