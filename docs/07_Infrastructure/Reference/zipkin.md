# Zipkin Tracing

## Übersicht
Zipkin ist ein verteiltes Tracing-System, das hilft, Latenzprobleme in Microservice-Architekturen zu analysieren. Es sammelt Timing-Daten, die benötigt werden, um Latenzprobleme in Service-Architekturen zu beheben.

## Konfiguration in Docker Compose
Der Zipkin-Service ist in der `docker-compose.yaml` definiert:

```yaml
  zipkin:
    image: "${ZIPKIN_IMAGE:-openzipkin/zipkin:3}"
    container_name: "${PROJECT_NAME:-meldestelle}-zipkin"
    restart: no
    ports:
      - "${ZIPKIN_PORT:-9411:9411}"
    profiles: [ "ops", "all" ]
    networks:
      meldestelle-network:
        aliases:
          - "zipkin"
```

## Integration in Services
Die Services (`api-gateway`, `ping-service`, etc.) sind so konfiguriert, dass sie Tracing-Daten an Zipkin senden. Dies geschieht über Umgebungsvariablen in der `docker-compose.yaml`:

```yaml
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: "${ZIPKIN_ENDPOINT:-http://zipkin:9411/api/v2/spans}"
      MANAGEMENT_TRACING_SAMPLING_PROBABILITY: "${ZIPKIN_SAMPLING_PROBABILITY:-1.0}"
```

## Zugriff
Die Zipkin UI ist unter `http://localhost:9411` erreichbar.

## Troubleshooting
- **Keine Traces sichtbar:** Stelle sicher, dass die Services korrekt gestartet sind und die Umgebungsvariablen für Zipkin gesetzt sind. Prüfe die Logs der Services auf Verbindungsfehler zu Zipkin.
- **Zipkin nicht erreichbar:** Prüfe, ob der Container läuft (`docker ps`) und ob der Port 9411 nicht blockiert ist.
