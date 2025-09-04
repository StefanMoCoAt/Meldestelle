# Infrastructure/Gateway Module - Comprehensive Documentation

## Überblick

Das API-Gateway ist der zentrale und einzige öffentliche Einstiegspunkt für alle Anfragen von externen Clients (z.B. Web-Anwendung, Desktop-Anwendung, mobile Apps) an das Meldestelle-System. Es fungiert als "Pförtner" für die gesamte Microservice-Landschaft und wurde zu einem vollwertigen, produktionstauglichen API Gateway mit modernen Best Practices erweitert.

**Wichtiger Grundsatz**: Kein externer Client sollte jemals direkt mit einem internen Microservice kommunizieren. Alle Anfragen laufen über das Gateway.

## Architektur und Technologie

Das Gateway ist als eigenständiger Spring Boot Service implementiert und nutzt Spring Cloud Gateway als technologische Grundlage. Spring Cloud Gateway ist ein reaktives, nicht-blockierendes Framework, das sich nahtlos in das Spring-Ökosystem integriert.

### Technologie-Stack
- **Spring Boot 3.x** - Moderne Spring Boot Anwendung
- **Spring Cloud Gateway** - Reaktives Gateway Framework
- **Spring WebFlux** - Reaktive Web-Programmierung mit Netty
- **Resilience4j** - Circuit Breaker Pattern Implementation
- **Consul** - Service Discovery und Health Checks
- **Micrometer + Prometheus** - Umfassende Metriken und Monitoring
- **JWT** - Token-basierte Authentifizierung
- **Reactive Streams** - Non-blocking I/O für optimale Performance

## Hauptverantwortlichkeiten

Das Gateway handhabt alle Cross-Cutting Concerns (übergreifende Belange), die für mehrere oder alle Microservices gelten und entlastet damit die Fach-Services von technischen Aufgaben.

### 1. Dynamisches Routing
- **Service Discovery Integration**: Vollständige Consul Integration für automatische Service-Erkennung
- **Load Balancing**: Intelligente Lastverteilung zwischen Service-Instanzen
- **Health-basiertes Routing**: Weiterleitung nur an gesunde Service-Instanzen

**Verfügbare Routen**:
- `/api/members/**` → members-service
- `/api/horses/**` → horses-service
- `/api/events/**` → events-service
- `/api/masterdata/**` → masterdata-service
- `/api/auth/**` → auth-service
- `/api/ping/**` → ping-service

### 2. Sicherheit und Authentifizierung
- **JWT Security Enforcement**: Validierung von Bearer Tokens für alle geschützten Endpunkte
- **Public Path Exemptions**: Konfigurierbare öffentliche Pfade (`/`, `/health`, `/actuator/**`, `/api/auth/login`)
- **User Context Injection**: Automatische Weiterleitung von User-ID und Rolle an Backend Services
- **Standardisierte Fehlerbehandlung**: Strukturierte 401 Unauthorized Responses

### 3. Rate Limiting
- **Intelligentes Rate Limiting** basierend auf User-Typ:
  - **Anonymous Users**: 50 Anfragen pro Minute
  - **Authenticated Users**: 200 Anfragen pro Minute
  - **Admin Users**: 500 Anfragen pro Minute
- **IP-basierte Limits**: Schutz vor DDoS-Attacken
- **Custom Headers**: X-RateLimit-* Header für Client-Information

### 4. Circuit Breaker und Resilienz
- **Service-spezifische Circuit Breaker**: Resilience4j Integration für jeden Backend Service
- **Fallback Mechanismen**: Benutzerfreundliche Fehlermeldungen bei Service-Ausfällen
- **Retry Logic**: Automatische Wiederholungen bei transienten Fehlern
- **Graceful Degradation**: Systembetrieb auch bei partiellen Service-Ausfällen

### 5. Monitoring und Observability
Das Gateway implementiert umfassende Observability durch eine vollständig integrierte Micrometer-basierte Metriken-Architektur.

#### Automatische Metriken-Erfassung (GatewayMetricsConfig)
- **Request Duration Tracking**: Automatische Messung aller Request-Response Zyklen
  - Metric: `gateway_request_duration` (Timer)
  - Tags: method, path, status, status_series
  - Percentile-basierte Auswertung (P50, P90, P95, P99)
- **Error Rate Monitoring**: Detailliertes Error-Tracking für 4xx/5xx Responses
  - Metric: `gateway_errors_total` (Counter)
  - Tags: method, path, status, status_series, error_type
  - Unterscheidung zwischen client_error und server_error
- **Request Volume Tracking**: Vollständige Request-Volumen Überwachung
  - Metric: `gateway_requests_total` (Counter)
  - Tags: method, path für detaillierte Analyse
- **Circuit Breaker Events**: Monitoring von Resilience-Pattern Events
  - Metric: `gateway_circuit_breaker_events_total` (Counter)
  - Integration mit Resilience4j Circuit Breaker Status

#### Intelligente Pfad-Normalisierung
- **Kardinalitäts-Kontrolle**: Automatische Normalisierung von dynamischen Pfaden
  - `/api/horses/123` → `/api/horses/{id}`
  - UUID-Pattern → `/{uuid}`
  - Sehr lange Pfade werden gekürzt (100+ Zeichen)

#### Health Monitoring Integration
- **Downstream Service Health**: Umfassende Überwachung aller Backend Services
  - Kritische Services: Members, Horses, Events, Masterdata, Auth
  - Optionale Services: Ping Service
  - Circuit Breaker Status Integration
- **Distributed Tracing**: Korrelations-ID basiertes Request-Tracking
- **Strukturierte Logs**: JSON-Format für maschinelle Auswertung

#### Prometheus Export
- **Automatischer Export**: Alle Metriken werden automatisch an Prometheus exportiert
- **Common Tags**: Alle Metriken erhalten automatisch Service- und Component-Tags
- **Filter-Optimierung**: Rauschen-reduzierende Metrik-Filter für interne Spring/Netty Metriken

### 6. CORS-Management
- **Produktionstaugliche CORS-Konfiguration**:
  - Erlaubte Origins: `https://*.meldestelle.at`, `http://localhost:*`
  - Alle HTTP-Methoden (GET, POST, PUT, DELETE, PATCH, OPTIONS)
  - Credential-Support für authentifizierte Anfragen

## Implementierte Optimierungen

### Gateway-Konfiguration (application.yml)
✅ **Vollständige Service-Routen**: Routing für alle Business Services
✅ **Circuit Breaker Integration**: Service-spezifische Resilience4j Konfigurationen
✅ **Connection Pooling**: Optimierte HTTP-Client-Konfiguration
✅ **Security Headers**: Umfassende Sicherheits-Header (X-Content-Type-Options, X-Frame-Options, X-XSS-Protection)
✅ **Enhanced Logging**: Strukturierte Logs mit Korrelations-IDs und Performance-Daten

### Health Monitoring (GatewayHealthIndicator.kt)
✅ **Downstream Service Monitoring**: Überwachung aller kritischen Services
✅ **Service Discovery Integration**: Consul-basierte Service-Erkennung
✅ **Test-Environment Handling**: Graceful Degradation in Test-Umgebungen
✅ **Detailliertes Error Reporting**: Umfassende Statusinformationen

### Build-Optimierungen (build.gradle.kts)
✅ **SINGLE SOURCE OF TRUTH**: Alle Dependencies über libs.versions.toml
✅ **Build Info Generation**: Automatische Build-Metadaten
✅ **Modern Kotlin Compiler**: Optimierte Compiler-Einstellungen
✅ **Dependency Optimization**: Bereinigung redundanter Dependencies

### Docker-Optimierungen (Dockerfile)
✅ **Multi-Stage Build**: Spring Boot Layer-Extraktion für 90%+ besseres Caching
✅ **Security Hardening**: Non-root User, Security Updates
✅ **OCI Compliance**: Vollständige Container-Metadaten
✅ **Production-Ready**: Optimierte JVM-Settings für Container-Umgebung

### Metriken-Integration (GatewayMetricsConfig.kt)
✅ **Comprehensive Micrometer Integration**: Vollständige Metriken-Erfassung mit automatischem Prometheus Export
✅ **Request/Response Time Tracking**: Detaillierte Performance-Metriken mit Percentile-Auswertung
✅ **Error Rate Monitoring**: Intelligente Fehler-Klassifikation und -Tracking
✅ **Path Normalization**: Kardinalitäts-kontrolle für dynamische API-Pfade
✅ **Circuit Breaker Metrics**: Integration mit Resilience4j Event-Tracking
✅ **Custom Business Metrics**: Erweiterbare Metrik-Architektur für fachliche KPIs

### Dokumentation
✅ **OpenAPI 3.0.3 Spezifikation**: Vollständige API-Dokumentation mit Members Service
✅ **Interactive Swagger UI**: Modern dokumentierte API-Endpunkte
✅ **Static HTML Documentation**: Responsive, moderne Dokumentations-Website
✅ **Health Monitoring Integration**: Real-time Status-Informationen

## Performance und Reliability

### Netty Server Optimierungen
- **Connection Timeouts**: 5 Sekunden für optimale Responsiveness
- **Idle Timeout**: 15 Sekunden für effiziente Resource-Nutzung
- **Elastic Connection Pool**: Automatische Skalierung basierend auf Load

### Circuit Breaker Konfiguration
- **Sliding Window**: 100 Anfragen für Default, service-spezifische Anpassungen
- **Failure Rate Threshold**: 50% für Standard-Services, 30% für Auth-Service
- **Half-Open State**: 3 Test-Anfragen für Service-Recovery

### JVM Optimierungen (Container)
```bash
JAVA_OPTS="-server -Xmx512m -Xms256m -XX:+UseG1GC
           -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
```

## API Gateway Request Flow

Ein typischer Anfrage-Flow:

1. **Client Request**: `https://api.meldestelle.at/api/members/123`
2. **Gateway Empfang**: Anfrage wird vom Spring Cloud Gateway empfangen
3. **Filter-Pipeline**:
   - **Security Filter**: JWT-Validierung
   - **Rate Limiting Filter**: Anfrage-Limits prüfen
   - **Correlation Filter**: Trace-ID generieren
   - **Logging Filter**: Request-Details erfassen
4. **Service Discovery**: Consul-Abfrage für verfügbare `members-service` Instanzen
5. **Load Balancing**: Intelligente Auswahl einer gesunden Instanz
6. **Circuit Breaker**: Überwachung der Service-Verfügbarkeit
7. **Request Forwarding**: Weiterleitung an Backend Service
8. **Response Processing**: Antwort-Verarbeitung und Header-Enrichment
9. **Client Response**: Strukturierte Antwort an Client

## Monitoring und Health Checks

### Actuator Endpunkte
- `/actuator/health` - Umfassender Health Status aller Services
- `/actuator/metrics` - Prometheus-kompatible Metriken
- `/actuator/info` - Anwendungs- und Build-Informationen
- `/actuator/gateway` - Gateway-spezifische Routing-Informationen
- `/actuator/circuitbreakers` - Circuit Breaker Status

### Key Performance Indicators (KPIs)

#### Automatisch erfasste Metriken
- **Request Throughput**: `gateway_requests_total` - Anfragen pro Sekunde nach Method/Path
- **Response Times**: `gateway_request_duration` - P50, P90, P95, P99 Percentile nach Status
- **Error Rates**: `gateway_errors_total` - 4xx/5xx Response Codes mit Error-Type Klassifikation
- **Circuit Breaker Events**: `gateway_circuit_breaker_events_total` - Resilience Pattern Aktivierungen
- **Service Availability**: Upstream Service Health via Health Indicators

#### Verfügbare Metric Tags für detaillierte Analyse
- **method**: HTTP-Method (GET, POST, PUT, DELETE, etc.)
- **path**: Normalisierter API-Pfad (z.B. `/api/horses/{id}`)
- **status**: HTTP-Status-Code (200, 404, 500, etc.)
- **status_series**: Status-Gruppe (2xx, 3xx, 4xx, 5xx)
- **error_type**: Fehler-Klassifikation (client_error, server_error)
- **service**: Automatisches "gateway" Tag
- **component**: Automatisches "infrastructure" Tag

#### Prometheus Query Beispiele
```promql
# Request Rate pro Endpunkt
rate(gateway_requests_total[5m])

# 95. Percentile Response Time
histogram_quantile(0.95, rate(gateway_request_duration_bucket[5m]))

# Error Rate nach Service
rate(gateway_errors_total[5m]) / rate(gateway_requests_total[5m])

# Circuit Breaker Aktivierungen
increase(gateway_circuit_breaker_events_total[1h])
```

## Security Features

### JWT Authentication
- **Bearer Token Validation**: Automatische JWT-Verifikation
- **Role Extraction**: User-Rolle für Backend Services verfügbar
- **Token Refresh**: Unterstützung für Token-Erneuerung
- **Public Endpoints**: Konfigurierbare Ausnahmen für öffentliche APIs

### Security Headers
```yaml
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
Cache-Control: no-cache, no-store, must-revalidate
```

## Development und Testing

### Local Development

**WICHTIG:** Alle Befehle müssen aus dem Projekt-Root-Verzeichnis (`/home/stefan/WsMeldestelle/Meldestelle`) ausgeführt werden.

```bash
# Sicherstellen, dass Sie im richtigen Verzeichnis sind
cd /home/stefan/WsMeldestelle/Meldestelle

# Gateway starten
./gradlew :infrastructure:gateway:bootRun

# Mit Docker
docker build -t meldestelle/gateway:latest -f infrastructure/gateway/Dockerfile .
docker run -p 8080:8080 meldestelle/gateway:latest
```

📖 **Detaillierte Startup-Anleitung:** Siehe `GATEWAY-STARTUP-GUIDE.md` im Projekt-Root für vollständige Befehle und Fehlerbehebung.

### Testing
```bash
# Unit Tests
./gradlew :infrastructure:gateway:test

# Integration Tests (mit Testcontainers)
./gradlew :infrastructure:gateway:integrationTest
```

## Konfiguration

### Environment Variables
```bash
SPRING_PROFILES_ACTIVE=prod
CONSUL_HOST=consul.meldestelle.at
CONSUL_PORT=8500
GATEWAY_ADMIN_USER=admin
GATEWAY_ADMIN_PASSWORD=secure-password
```

### Profile-spezifische Konfiguration
- **dev**: Entwicklungsumgebung mit Debug-Logging
- **test**: Test-Umgebung mit Mock Services
- **prod**: Produktionsumgebung mit allen Security Features

## Deployment

### Docker Deployment
```bash
# Multi-stage Build mit Layer Caching
docker build -t meldestelle/gateway:1.0.0 \
  -f infrastructure/gateway/Dockerfile .

# Container starten
docker run -d \
  --name gateway \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e CONSUL_HOST=consul \
  meldestelle/gateway:1.0.0
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    spec:
      containers:
      - name: gateway
        image: meldestelle/gateway:1.0.0
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 90
          periodSeconds: 30
```

## Troubleshooting

### Häufige Probleme

**Service Discovery Issues**
- Consul Connectivity prüfen
- Service Registration Status überprüfen
- DNS Resolution testen

**Circuit Breaker Activation**
- Service Health Status prüfen
- Failure Rate Threshold analysieren
- Manual Circuit Breaker Reset über Actuator

**Performance Issues**
- Connection Pool Metrics analysieren
- JVM Heap Usage monitoring
- Request Rate Limiting überprüfen

**Metriken und Monitoring Issues**
- Prometheus Scraping Endpunkt prüfen: `/actuator/prometheus`
- Metrics Registry Status überprüfen: `/actuator/metrics`
- GatewayMetricsWebFilter Aktivierung validieren
- Metric Tags auf Kardinalitäts-Explosion prüfen
- Path Normalization bei unerwarteten Metric-Namen

### Logging und Debugging
```bash
# Logs mit Korrelations-IDs
docker logs gateway | grep "correlationId"

# Circuit Breaker Status
curl http://localhost:8080/actuator/circuitbreakers

# Health Details
curl http://localhost:8080/actuator/health
```

## Zukünftige Erweiterungen

### Geplante Features
- **OAuth2/OIDC Integration**: Erweiterte Authentifizierung
- **GraphQL Gateway**: Unified GraphQL Interface
- **Caching Layer**: Redis-basiertes Response Caching
- **Request/Response Transformation**: Dynamic Content Modification

### Performance Optimierungen
- **HTTP/2 Support**: Moderne Protocol-Unterstützung
- **Connection Pooling Tuning**: Erweiterte Pool-Konfiguration
- **Reactive Streams Optimization**: Backpressure Handling

## Dokumentation und Ressourcen

### API Dokumentation
- **Swagger UI**: `/swagger` - Interactive API Documentation
- **OpenAPI Spec**: `/openapi` - Machine-readable API Specification
- **Static Documentation**: `/docs` - Comprehensive Documentation Hub

### Monitoring Dashboards
- **Health Status**: `/actuator/health` - Real-time Service Health
- **Metrics**: `/actuator/metrics` - Prometheus Metrics
- **Gateway Routes**: `/actuator/gateway/routes` - Active Route Information

---

**Letzte Aktualisierung**: 14. August 2025

**Version**: 1.1.0

**Maintainer**: Meldestelle Development Team

---

Diese Dokumentation wurde umfassend aktualisiert und um die neue Micrometer Metrics Integration (GatewayMetricsConfig.kt) erweitert. Sie dokumentiert alle implementierten Optimierungen einschließlich der vollständigen Observability-Architektur mit automatischer Request/Response Zeit Messung, Error Rate Tracking und Custom Business Metrics.
