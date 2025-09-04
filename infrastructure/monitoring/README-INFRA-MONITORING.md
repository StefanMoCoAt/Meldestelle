# Infrastructure/Monitoring Modul – Aktuelle Dokumentation (Stand: September 2025)

## Überblick

Das Monitoring-Modul stellt die zentrale Observability-Infrastruktur für alle Services bereit. Es deckt zwei der drei Observability-Säulen ab: Metriken und Distributed Tracing. Ziel ist es, Betriebsdaten konsistent zu erfassen, zu visualisieren und Probleme schnell zu erkennen.

- Metriken: Quantitative Leistungsdaten wie Antwortzeiten, Fehlerraten, JVM- und Systemmetriken.
- Distributed Tracing: Ende-zu-Ende-Verfolgung einer Anfrage über Service-Grenzen hinweg (Trace/Span).

## Aufgabe des Moduls

- Bereitstellung einer einheitlichen Monitoring-Client-Bibliothek für alle Microservices.
- Zentrale Bereitstellung eines Zipkin-Servers zur Sammlung und Visualisierung von Traces.
- Sicherstellung eines konsistenten Prometheus-/Micrometer-Setups über alle Services hinweg.
- Bereitstellung konservativer Default-Properties, die pro Service überschrieben werden können.

## Architektur

Das Modul ist in eine wiederverwendbare Client-Bibliothek und einen zentralen Server aufgeteilt:

```
infrastructure/monitoring/
├── monitoring-client/      # Bibliothek, die jeder Service einbindet
└── monitoring-server/      # Eigenständiger Service, der den Zipkin-Server hostet
```

### monitoring-client

Dies ist eine wiederverwendbare Bibliothek, die von jedem Microservice (z. B. gateway, members, masterdata) eingebunden wird.

- Zweck: Automatische Instrumentierung der Anwendung, Aktivierung von Actuator-/Tracing-Funktionen und Export von Metriken.
- Technologien:
  - Spring Boot Actuator: Stellt u. a. den Endpunkt `/actuator/prometheus` bereit.
  - Micrometer (Core, Prometheus): Einheitliches Metrik-API, Export in Prometheus-Format.
  - Micrometer Tracing (Brave Bridge) + Zipkin Reporter: Erzeugt und sendet Traces (Spans) an Zipkin.
- Vorteile: Keine individuelle Konfiguration in jedem Service nötig; sinnvolle Defaults per AutoConfiguration.

#### AutoConfiguration und Defaults

Die Klasse MonitoringClientAutoConfiguration ist als `@AutoConfiguration` deklariert und aktiviert nur, wenn Actuator/Micrometer am Classpath sind (`@ConditionalOnClass`). Sie lädt `monitoring-defaults.properties` mit niedriger Priorität. Wichtige Defaults:

```
management.endpoints.web.exposure.include=health,info,prometheus
management.tracing.enabled=true
management.tracing.sampling.probability=${TRACING_SAMPLING_PROBABILITY:1.0}
management.observations.http.server.requests.enabled=true
management.info.env.enabled=true
management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans
```

Hinweise:
- Sampling-Rate: In Entwicklung 1.0 (100%). In Produktion sollte dies reduziert werden (z. B. 0.1).
- Endpunkte: Der Prometheus-Scrape-Pfad ist einheitlich `/actuator/prometheus`.
- Überschreibung: Jede Anwendung kann diese Werte über application.yml/-properties anpassen.

### monitoring-server

Eigenständiger Spring-Boot-Service, der den Zipkin-Server hostet. Durch die Zipkin-Server-Abhängigkeit erfolgt die Auto-Konfiguration; eine explizite `@EnableZipkinServer`-Annotation ist nicht erforderlich.

- Zweck: Empfang und UI-Visualisierung von Traces aus allen Services.
- Metriken: Der Server exportiert eigene Metriken (Prometheus-Registry eingebunden), sodass er ebenfalls von Prometheus gescraped werden kann.

## Zusammenspiel im System

1. Jeder Microservice bindet `:infrastructure:monitoring:monitoring-client` ein und exponiert `/actuator/prometheus`; Traces werden an Zipkin gesendet.
2. Der `:infrastructure:monitoring:monitoring-server` empfängt Traces und stellt die Zipkin UI bereit.
3. Prometheus (docker-compose) scraped periodisch alle `/actuator/prometheus`-Endpunkte und speichert Metriken.
4. Grafana (docker-compose) visualisiert Metriken/Dashboards.

Diese Kombination aus Micrometer, Prometheus, Zipkin und Grafana bildet einen gängigen Production-Stack.

## Verwendung in Services

- Abhängigkeit hinzufügen: `implementation(projects.infrastructure.monitoring.monitoringClient)` (über build.gradle.kts der Services).
- Optional: Eigene Tags setzen (z. B. `management.metrics.tags.application`, `environment`).
- Optional: Sampling-Rate via Umgebungsvariable `TRACING_SAMPLING_PROBABILITY` anpassen.

Beispiel (application.yml):

```yaml
management:
  metrics:
    tags:
      application: ${spring.application.name}
      environment: ${SPRING_PROFILES_ACTIVE:dev}
  tracing:
    sampling:
      probability: 0.2
```

## Testing-Strategie (Tracer-Bullet-Zyklus)

- Monitoring-Server: Ein grundlegender Smoke-Test prüft erfolgreichen Start der Anwendung.
- Monitoring-Client: Keine dedizierten Unit-Tests; Validierung erfolgt End-to-End durch integrierte Services (Prometheus scrape, Zipkin-Empfang).

## Aktualitäts-Check (Repo-Stand September 2025)

- monitoring-client: AutoConfiguration (Deutsch kommentiert) und Defaults vorhanden; Endpunkt `management.zipkin.tracing.endpoint` verweist auf `zipkin:9411` und entspricht docker-compose.
- monitoring-server: Kotlin-Hauptklasse startet Zipkin-Server; Build-Datei bindet Actuator, Zipkin-Server und Prometheus-Registry ein.
- Gateway application.yml exponiert Actuator-Endpunkte inkl. prometheus; passt zum Monitoring-Ansatz.

## Optimierungen (September 2025)

- Dokumentation präzisiert und vollständig auf Deutsch gebracht; Zweck und Umsetzung klar herausgestellt.
- Kleine Konsistenz-Anpassung: Kommentare im Wurzel-Build-Skript des Monitoring-Moduls ins Deutsche übertragen.
- Empfehlung (optional, nicht zwingend umgesetzt):
  - Standard-Tags global setzen (application, environment, instance) via `management.metrics.tags.*`.
  - Falls Services eigene MeterRegistry konfigurieren: `@ConditionalOnMissingBean` beachten, um Kollisionen zu vermeiden.

---
Letzte Aktualisierung: 4. September 2025
