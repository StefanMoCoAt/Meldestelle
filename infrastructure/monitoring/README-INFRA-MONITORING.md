# Infrastructure/Monitoring Module

## Überblick

Das **Monitoring-Modul** ist die Grundlage für die **Observability** (Beobachtbarkeit) der gesamten Meldestelle-Systemlandschaft. In einer verteilten Microservice-Architektur ist es unerlässlich, Einblicke in das Verhalten, die Leistung und die Gesundheit der einzelnen Dienste zu haben. Dieses Modul stellt die Werkzeuge für zwei der drei Säulen der Observability bereit: **Metriken** und **Distributed Tracing**.

* **Metriken:** Quantitative Daten über die Leistung von Services (z.B. CPU-Auslastung, Antwortzeiten, Fehlerraten).
* **Distributed Tracing:** Verfolgung einer einzelnen Anfrage über mehrere Service-Grenzen hinweg, um Engpässe und Fehlerquellen zu identifizieren.

## Architektur

Das Modul ist in eine wiederverwendbare Client-Bibliothek und einen zentralen Server aufgeteilt:


infrastructure/monitoring/
├── monitoring-client/      # Bibliothek, die jeder Service einbindet
└── monitoring-server/      # Eigenständiger Service, der den Zipkin-Server hostet


### `monitoring-client`

Dies ist eine **wiederverwendbare Bibliothek**, die von **jedem einzelnen Microservice** (z.B. `masterdata-service`, `gateway`) als Abhängigkeit eingebunden werden muss.

* **Zweck:** Instrumentiert den Service automatisch, um Metriken und Traces zu generieren.
* **Technologien:**
    * **Spring Boot Actuator:** Stellt einen `/actuator/prometheus`-Endpunkt bereit, an dem Metriken im Prometheus-Format abgerufen werden können.
    * **Micrometer:** Eine Fassade für Metriken, die es ermöglicht, Anwendungsmetriken zu sammeln (z.B. HTTP-Request-Zeiten, JVM-Statistiken).
    * **Brave & Zipkin Reporter:** Instrumentiert den Code für Distributed Tracing und sendet die gesammelten Spans (Teile eines Traces) an den Zipkin-Server.
* **Vorteil:** Entwickler müssen sich nicht aktiv um die Implementierung von Monitoring kümmern. Durch das Einbinden dieser Bibliothek erhält jeder Service automatisch grundlegende Observability.

### `monitoring-server`

Dies ist ein **eigenständiger Spring Boot Service**, der eine zentrale Komponente des Monitoring-Stacks hostet.

* **Zweck:** Hostet den **Zipkin-Server** inklusive seiner grafischen Benutzeroberfläche. Alle `monitoring-client`-Instanzen senden ihre Tracing-Daten an diesen Server. Entwickler können dann in der Zipkin-UI die gesamten Anfrage-Flows visualisieren und analysieren.

## Zusammenspiel im Ökosystem

Das vollständige Monitoring-Setup besteht aus mehreren Teilen:

1.  Jeder **Microservice** bindet `:infrastructure:monitoring:monitoring-client` ein und stellt Metriken unter `/actuator/prometheus` bereit und sendet Traces an Zipkin.
2.  Der **`:infrastructure:monitoring:monitoring-server`** empfängt die Traces und stellt die Zipkin-UI zur Verfügung.
3.  Ein **Prometheus-Server** (definiert in `docker-compose.yml`) ist so konfiguriert, dass er periodisch die `/actuator/prometheus`-Endpunkte aller Microservices abfragt ("scraped") und die Metriken in seiner Zeitreihen-Datenbank speichert.
4.  Ein **Grafana-Server** (definiert in `docker-compose.yml`) visualisiert die in Prometheus gespeicherten Metriken in anpassbaren Dashboards.

Diese Kombination aus Micrometer, Prometheus, Zipkin und Grafana bildet einen leistungsstarken, branchenüblichen "Observability Stack".

## Neue Funktionen und Optimierungen

### Sicherheitsverbesserungen
* **Umgebungsvariablen für Credentials**: Alle hardcodierten Passwörter und API-Schlüssel wurden durch Umgebungsvariablen ersetzt
* **Alertmanager-Konfiguration**: SMTP- und Slack-Einstellungen nutzen jetzt sichere Umgebungsvariablen
* **Prometheus-Authentifizierung**: Metriken-Endpunkte sind durch Benutzername/Passwort geschützt

### Performance-Optimierungen
* **Konfigurierbare Tracing-Sampling-Rate**: Standard 100% für Entwicklung, über `TRACING_SAMPLING_PROBABILITY` anpassbar für Produktion
* **Optimierte Prometheus-Konfiguration**: Korrigierte Metriken-Pfade und eliminierte doppelte Jobs
* **Verbesserte Speicher-Retention**: Produktion nutzt 30 Tage Retention und WAL-Komprimierung

### Erweiterte Dashboards
* **Application Overview Dashboard**: Zentrale Anwendungsmetriken (Request Rate, Response Times, Error Rate, Status)
* **Infrastructure Components Dashboard**: Überwachung von PostgreSQL, Redis, Kafka, System-Metriken
* **JVM Dashboard**: Bestehende JVM-Metriken für Java-Anwendungen

### Konfigurationsverbesserungen
* **Einheitliche Endpunkt-Pfade**: Verwendung von `/actuator/prometheus` für alle Services
* **Umgebungsspezifische Konfiguration**: Getrennte Einstellungen für Entwicklung und Produktion
* **Erweiterte ELK-Integration**: Vollständige Logging-Pipeline mit Elasticsearch und Logstash

## Testing-Strategie (Tracer-Bullet Zyklus)

Im Rahmen des aktuellen "Tracer-Bullet"-Entwicklungszyklus wurde die Testing-Strategie auf das **Minimum für die Architektur-Validierung** reduziert:

### Monitoring-Server Test
* **Ein essentieller "Smoke-Test"**: Überprüft, ob der Zipkin-Server (monitoring-server) überhaupt starten kann
* **Zweck**: Validiert die korrekte Konfiguration des zentralen Monitoring-Servers
* **Warum essentiell**: Ohne einen funktionsfähigen Zipkin-Server können im finalen E2E-Test keine Tracing-Daten empfangen und ausgewertet werden

### Monitoring-Client
* **Keine separaten Tests**: Die monitoring-client Bibliothek wird implizit durch die Integration in andere Services (z.B. ping-service) getestet
* **Validierung erfolgt End-to-End**: Die Funktionalität wird durch den finalen "Tracer-Bullet"-Test bestätigt, wenn Services erfolgreich Tracing-Daten senden

Diese minimalistische Teststrategie stellt sicher, dass die Monitoring-Komponenten für den "Tracer-Bullet"-Test bereit sind, ohne Zeit in umfangreiche Testsuites zu investieren, die für die Architektur-Validierung nicht notwendig sind.

---
**Letzte Aktualisierung**: 16. August 2025
