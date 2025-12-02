# Gateway Configuration Documentation

## Überblick

Dieses Dokument beschreibt alle zentralen Konfigurationseigenschaften für das API Gateway. Die Konfiguration erfolgt über die `application.yml` Datei und kann durch Umgebungsvariablen überschrieben werden.

## Table of Contents

- [Server Configuration](#server-configuration)
- [Spring Application](#spring-application)
- [Consul Service Discovery](#consul-service-discovery)
- [Spring Cloud Gateway](#spring-cloud-gateway)
- [Circuit Breaker (Resilience4j)](#circuit-breaker-resilience4j)
- [Management & Monitoring](#management--monitoring)
- [Security](#security)
- [Logging](#logging)

---

## Server Configuration

### server.port

- **Typ**: Integer
- **Default**: 8081
- **Environment Variable**: `GATEWAY_PORT`
- **Beschreibung**: Port, auf dem das Gateway läuft

### server.netty.connection-timeout

- **Typ**: Duration
- **Default**: 5s
- **Beschreibung**: Timeout für initiale TCP-Verbindungen

### server.netty.idle-timeout

- **Typ**: Duration
- **Default**: 15s
- **Beschreibung**: Timeout für inaktive Verbindungen

**Beispiel:**

```yaml
server:
  port: 8081
  netty:
    connection-timeout: 5s
    idle-timeout: 15s
```

---

## Spring Application

### spring.application.name

- **Typ**: String
- **Default**: api-gateway
- **Beschreibung**: Name der Anwendung, wird in Consul und Logs verwendet

### spring.profiles.active

- **Typ**: String
- **Default**: dev
- **Environment Variable**: `SPRING_PROFILES_ACTIVE`
- **Beschreibung**: Aktives Spring-Profil (dev, test, prod)
- **Mögliche Werte**: dev, test, staging, prod

### spring.security.user.name / password

- **Typ**: String
- **Default**: admin / admin
- **Environment Variables**: `GATEWAY_ADMIN_USER`, `GATEWAY_ADMIN_PASSWORD`
- **Beschreibung**: Basic Auth für administrative Endpunkte
- **⚠️ Wichtig**: In Produktion durch sichere Werte ersetzen!

**Beispiel:**

```yaml
spring:
  application:
    name: api-gateway
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  security:
    user:
      name: ${GATEWAY_ADMIN_USER:admin}
      password: ${GATEWAY_ADMIN_PASSWORD:admin}
```

---

## Consul Service Discovery

### spring.cloud.consul.host

- **Typ**: String
- **Default**: localhost
- **Environment Variable**: `CONSUL_HOST`
- **Beschreibung**: Hostname des Consul-Servers

### spring.cloud.consul.port

- **Typ**: Integer
- **Default**: 8500
- **Environment Variable**: `CONSUL_PORT`
- **Beschreibung**: Port des Consul-Servers

### spring.cloud.consul.enabled

- **Typ**: Boolean
- **Default**: true
- **Environment Variable**: `CONSUL_ENABLED`
- **Beschreibung**: Aktiviert/Deaktiviert Consul Integration

### spring.cloud.consul.discovery.enabled

- **Typ**: Boolean
- **Default**: true
- **Environment Variable**: `CONSUL_ENABLED`
- **Beschreibung**: Aktiviert Service Discovery

### spring.cloud.consul.discovery.register

- **Typ**: Boolean
- **Default**: true
- **Environment Variable**: `CONSUL_ENABLED`
- **Beschreibung**: Registriert das Gateway in Consul

### spring.cloud.consul.discovery.health-check-path

- **Typ**: String
- **Default**: /actuator/health
- **Beschreibung**: Pfad für Consul Health Checks

### spring.cloud.consul.discovery.health-check-interval

- **Typ**: Duration
- **Default**: 10s
- **Beschreibung**: Intervall für Health Checks

### spring.cloud.consul.discovery.instance-id

- **Typ**: String
- **Default**: ${spring.application.name}-${server.port}-${random.uuid}
- **Beschreibung**: Eindeutige Instanz-ID für Service Discovery

**Beispiel:**

```yaml
spring:
  cloud:
    consul:
      host: ${CONSUL_HOST:localhost}
      port: ${CONSUL_PORT:8500}
      enabled: ${CONSUL_ENABLED:true}
      discovery:
        enabled: ${CONSUL_ENABLED:true}
        register: ${CONSUL_ENABLED:true}
        health-check-path: /actuator/health
        health-check-interval: 10s
        instance-id: ${spring.application.name}-${server.port}-${random.uuid}
```

---

## Spring Cloud Gateway

### Verbindungskonfiguration

#### spring.cloud.gateway.server.webflux.httpclient.connect-timeout

- **Typ**: Integer (Millisekunden)
- **Default**: 5000
- **Beschreibung**: Timeout für Backend-Verbindungen

#### spring.cloud.gateway.server.webflux.httpclient.response-timeout

- **Typ**: Duration
- **Default**: 30s
- **Beschreibung**: Timeout für Backend-Responses

#### spring.cloud.gateway.server.webflux.httpclient.pool.max-idle-time

- **Typ**: Duration
- **Default**: 15s
- **Beschreibung**: Max. Idle-Zeit für Verbindungen im Pool

#### spring.cloud.gateway.server.webflux.httpclient.pool.max-life-time

- **Typ**: Duration
- **Default**: 60s
- **Beschreibung**: Max. Lebensdauer einer Verbindung

**Beispiel:**

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          httpclient:
            connect-timeout: 5000
            response-timeout: 30s
            pool:
              max-idle-time: 15s
              max-life-time: 60s
```

### Default Filters

Diese Filter werden auf **alle** Routen angewendet:

1. **DedupeResponseHeader**: Entfernt doppelte CORS-Header
2. **CircuitBreaker**: Default Circuit Breaker mit Fallback
3. **Retry**: Automatische Wiederholung bei Fehlern
4. **Security Headers**: X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, etc.
5. **Cache-Control**: No-cache Header für alle Responses

**Beispiel:**

```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin
        - name: CircuitBreaker
          args:
            name: defaultCircuitBreaker
            fallbackUri: forward:/fallback
        - name: Retry
          args:
            retries: 3
            statuses: BAD_GATEWAY,GATEWAY_TIMEOUT
            methods: GET,POST,PUT,DELETE
            backoff:
              firstBackoff: 50ms
              maxBackoff: 500ms
              factor: 2
```

### Routes

Das Gateway definiert folgende Service-Routen:

#### 1. Members Service Route

- **Path**: `/api/members/**`
- **Service**: members-service (via Consul)
- **Circuit Breaker**: membersCircuitBreaker
- **Fallback**: /fallback/members

#### 2. Horses Service Route

- **Path**: `/api/horses/**`
- **Service**: horses-service (via Consul)
- **Circuit Breaker**: horsesCircuitBreaker
- **Fallback**: /fallback/horses

#### 3. Events Service Route

- **Path**: `/api/events/**`
- **Service**: events-service (via Consul)
- **Circuit Breaker**: eventsCircuitBreaker
- **Fallback**: /fallback/events

#### 4. Masterdata Service Route

- **Path**: `/api/masterdata/**`
- **Service**: masterdata-service (via Consul)
- **Circuit Breaker**: masterdataCircuitBreaker
- **Fallback**: /fallback/masterdata

#### 5. Auth Service Route

- **Path**: `/api/auth/**`
- **Service**: auth-service (via Consul)
- **Circuit Breaker**: authCircuitBreaker
- **Fallback**: /fallback/auth

#### 6. Ping Service Route

- **Path**: `/api/ping/**`
- **Service**: ping-service (via Consul)
- **No Circuit Breaker**: Optional service

**Beispiel einer Route:**

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: members-service-route
          uri: lb://members-service  # lb = Load Balanced via Consul
          predicates:
            - Path=/api/members/**
          filters:
            - StripPrefix=1  # Entfernt /api vom Pfad
            - name: CircuitBreaker
              args:
                name: membersCircuitBreaker
                fallbackUri: forward:/fallback/members
```

---

## Circuit Breaker (Resilience4j)

### Default Konfiguration

#### resilience4j.circuitbreaker.configs.default.registerHealthIndicator

- **Typ**: Boolean
- **Default**: true
- **Beschreibung**: Registriert Circuit Breaker im Health Endpoint

#### resilience4j.circuitbreaker.configs.default.slidingWindowSize

- **Typ**: Integer
- **Default**: 100
- **Beschreibung**: Größe des Sliding Window für Fehlerrate-Berechnung

#### resilience4j.circuitbreaker.configs.default.minimumNumberOfCalls

- **Typ**: Integer
- **Default**: 20
- **Beschreibung**: Mindestanzahl an Calls bevor Circuit Breaker aktiviert wird

#### resilience4j.circuitbreaker.configs.default.permittedNumberOfCallsInHalfOpenState

- **Typ**: Integer
- **Default**: 3
- **Beschreibung**: Anzahl Test-Calls im Half-Open State

#### resilience4j.circuitbreaker.configs.default.waitDurationInOpenState

- **Typ**: Duration
- **Default**: 5s
- **Beschreibung**: Wartezeit bevor von Open zu Half-Open gewechselt wird

#### resilience4j.circuitbreaker.configs.default.failureRateThreshold

- **Typ**: Integer (Prozent)
- **Default**: 50
- **Beschreibung**: Fehlerrate-Schwelle für Circuit Breaker Aktivierung

### Service-spezifische Circuit Breaker

Jeder Service hat einen eigenen Circuit Breaker mit angepasster Konfiguration:

| Service | Sliding Window | Failure Threshold | Besonderheit |
|---------|---------------|-------------------|--------------|
| members-service | 50 | 50% | Standard |
| horses-service | 50 | 50% | Standard |
| events-service | 75 | 50% | Größeres Window |
| masterdata-service | 30 | 50% | Kleineres Window |
| auth-service | 20 | 30% | Sensitiverer Threshold |

**Beispiel:**

```yaml
resilience4j:
  circuitbreaker:
    instances:
      authCircuitBreaker:
        baseConfig: default
        slidingWindowSize: 20
        failureRateThreshold: 30  # Auth ist kritisch -> niedrigerer Threshold
```

---

## Management & Monitoring

### Exposed Endpoints

#### management.endpoints.web.exposure.include

- **Typ**: Comma-separated String
- **Default**: health,info,metrics,prometheus,gateway,circuitbreakers
- **Beschreibung**: Öffentlich verfügbare Actuator Endpoints

**Verfügbare Endpoints:**

- `/actuator/health` - Service Health Status
- `/actuator/info` - Service Informationen
- `/actuator/metrics` - Micrometer Metriken
- `/actuator/prometheus` - Prometheus Scrape Endpoint
- `/actuator/gateway` - Gateway Routes & Filters
- `/actuator/circuitbreakers` - Circuit Breaker Status

### Health Endpoint

#### management.endpoint.health.show-details

- **Typ**: String
- **Default**: always
- **Mögliche Werte**: never, when-authorized, always
- **Beschreibung**: Zeigt detaillierte Health-Informationen

#### management.endpoint.health.show-components

- **Typ**: Boolean
- **Default**: always
- **Beschreibung**: Zeigt Health-Komponenten

#### management.endpoint.health.probes.enabled

- **Typ**: Boolean
- **Default**: true
- **Beschreibung**: Aktiviert Kubernetes Liveness/Readiness Probes

### Metrics

#### management.metrics.tags

- **Beschreibung**: Globale Tags für alle Metriken
- **Standard Tags**:
  - application: ${spring.application.name}
  - environment: ${spring.profiles.active}
  - instance: ${spring.cloud.consul.discovery.instance-id}
  - service: gateway
  - component: infrastructure
  - gateway: api-gateway

#### management.metrics.distribution.percentiles-histogram.http.server.requests

- **Typ**: Boolean
- **Default**: true
- **Beschreibung**: Aktiviert Histogram für Request-Zeiten

#### management.metrics.distribution.percentiles.http.server.requests

- **Typ**: Array[Double]
- **Default**: [0.5, 0.90, 0.95, 0.99]
- **Beschreibung**: Percentile-Werte für Request-Zeiten

### Tracing

#### management.tracing.enabled

- **Typ**: Boolean
- **Default**: false
- **Environment Variable**: `TRACING_ENABLED`
- **Beschreibung**: Aktiviert Distributed Tracing

#### management.tracing.sampling.probability

- **Typ**: Double (0.0 - 1.0)
- **Default**: 1.0
- **Environment Variable**: `TRACING_SAMPLING_PROBABILITY`
- **Beschreibung**: Sampling-Rate für Traces (1.0 = 100%)

#### management.zipkin.tracing.endpoint

- **Typ**: URL
- **Default**: <http://localhost:9411/api/v2/spans>
- **Environment Variable**: `ZIPKIN_TRACING_ENDPOINT`
- **Beschreibung**: Zipkin Server URL

**Beispiel:**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,gateway,circuitbreakers
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  tracing:
    enabled: ${TRACING_ENABLED:false}
    sampling:
      probability: ${TRACING_SAMPLING_PROBABILITY:1.0}
  zipkin:
    tracing:
      endpoint: ${ZIPKIN_TRACING_ENDPOINT:http://localhost:9411/api/v2/spans}
```

---

## Security

Die Security-Konfiguration erfolgt über Custom Properties unter `gateway.security`:

### gateway.security.publicPaths

- **Typ**: Array[String]
- **Default**: ["/", "/fallback/**", "/actuator/**", "/webjars/**", "/v3/api-docs/**", "/api/auth/**"]
- **Beschreibung**: Pfade, die ohne Authentifizierung zugänglich sind

### gateway.security.cors.allowedOriginPatterns

- **Typ**: Array[String]
- **Default**: ["http://localhost:[*]", "https://*.meldestelle.at"]
- **Beschreibung**: Erlaubte Origin-Patterns für CORS

### gateway.security.cors.allowedMethods

- **Typ**: Array[String]
- **Default**: ["GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"]
- **Beschreibung**: Erlaubte HTTP-Methoden

### gateway.security.cors.allowedHeaders

- **Typ**: Array[String]
- **Default**: ["*"]
- **Beschreibung**: Erlaubte Request-Headers

### gateway.security.cors.exposedHeaders

- **Typ**: Array[String]
- **Default**: ["X-Correlation-ID", "X-RateLimit-Limit", "X-RateLimit-Remaining"]
- **Beschreibung**: Headers die an Client exponiert werden

### gateway.security.cors.allowCredentials

- **Typ**: Boolean
- **Default**: true
- **Beschreibung**: Erlaubt Credentials (Cookies, Auth-Header)

### gateway.security.cors.maxAge

- **Typ**: Duration
- **Default**: 1h
- **Beschreibung**: Cache-Zeit für CORS Preflight-Requests

**Beispiel:**

```yaml
gateway:
  security:
    publicPaths:
      - "/"
      - "/actuator/**"
      - "/api/auth/**"
    cors:
      allowedOriginPatterns:
        - "http://localhost:[*]"
        - "https://*.meldestelle.at"
      allowedMethods:
        - GET
        - POST
        - PUT
        - DELETE
      allowCredentials: true
      maxAge: 1h
```

### JWT Configuration

#### spring.security.oauth2.resourceserver.jwt.jwk-set-uri

- **Typ**: URL
- **Environment Variable**: `KEYCLOAK_JWK_SET_URI`
- **Beschreibung**: Keycloak JWK Set URI für JWT-Validierung
- **Beispiel**: <http://localhost:8180/realms/meldestelle/protocol/openid-connect/certs>

---

## Logging

### logging.level

- **Beschreibung**: Log-Level für verschiedene Pakete

**Standard Log-Levels:**

- `org.springframework.cloud.gateway`: INFO
- `org.springframework.cloud.loadbalancer`: DEBUG
- `org.springframework.cloud.consul`: INFO
- `at.mocode.infrastructure.gateway`: DEBUG
- `io.github.resilience4j`: INFO
- `reactor.netty.http.client`: INFO
- `org.springframework.security`: WARN
- `org.springframework.web`: INFO

### logging.pattern.console

- **Beschreibung**: Console-Log-Pattern mit Farben und Correlation-ID

### logging.pattern.file

- **Beschreibung**: File-Log-Pattern ohne Farben

### logging.file.name

- **Typ**: String
- **Default**: infrastructure/gateway/logs/gateway.log
- **Beschreibung**: Log-Datei Pfad

### logging.logback.rollingpolicy

- **clean-history-on-start**: true
- **max-file-size**: 100MB
- **total-size-cap**: 1GB
- **max-history**: 30 (Tage)

**Beispiel:**

```yaml
logging:
  level:
    at.mocode.infrastructure.gateway: DEBUG
    org.springframework.cloud.gateway: INFO
  file:
    name: infrastructure/gateway/logs/gateway.log
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
```

---

## Umgebungsvariablen Übersicht

### Kritische Variablen für Produktion

| Variable | Beschreibung | Default |
|----------|--------------|---------|
| `GATEWAY_PORT` | Gateway Port | 8081 |
| `CONSUL_HOST` | Consul Server | localhost |
| `CONSUL_PORT` | Consul Port | 8500 |
| `CONSUL_ENABLED` | Consul Aktivieren | true |
| `GATEWAY_ADMIN_USER` | Admin Username | admin |
| `GATEWAY_ADMIN_PASSWORD` | Admin Password | admin |
| `KEYCLOAK_JWK_SET_URI` | Keycloak JWK URI | <http://localhost:8180/>... |
| `TRACING_ENABLED` | Tracing aktivieren | false |
| `ZIPKIN_TRACING_ENDPOINT` | Zipkin Server | <http://localhost:9411/>... |
| `SPRING_PROFILES_ACTIVE` | Spring Profil | dev |

---

## Profile-spezifische Konfiguration

Das Gateway unterstützt verschiedene Spring Profile:

### dev (Development)

- Detailliertes Logging
- Alle Monitoring-Endpunkte verfügbar
- Tracing optional

### test

- Reduziertes Logging
- Test-spezifische Timeouts
- In-Memory Services optional

### prod (Production)

- Production-ready Logging
- Sichere Credentials erforderlich
- Tracing empfohlen
- Rate Limiting aktiviert

**Beispiel für profile-spezifische Datei:**

```yaml
# application-prod.yml
spring:
  security:
    user:
      name: ${GATEWAY_ADMIN_USER}  # Muss gesetzt sein!
      password: ${GATEWAY_ADMIN_PASSWORD}  # Muss gesetzt sein!

management:
  tracing:
    enabled: true
    sampling:
      probability: 0.1  # 10% Sampling in Production

logging:
  level:
    at.mocode.infrastructure.gateway: INFO  # Weniger Logs
```

---

## Best Practices

1. **Umgebungsvariablen verwenden**: Nie Credentials in application.yml hardcoden
2. **Profile nutzen**: Separate Konfigurationen für dev/test/prod
3. **Health Checks aktivieren**: Für Consul und Kubernetes
4. **Tracing in Production**: Mindestens 10% Sampling
5. **Monitoring exportieren**: Prometheus-Endpunkt für Grafana
6. **Circuit Breaker tunen**: An Service-Charakteristiken anpassen
7. **CORS restriktiv**: Nur benötigte Origins erlauben
8. **Log Rotation**: Verhindert volle Festplatten

---

## Troubleshooting

### Gateway startet nicht

- ✅ Prüfen: Consul erreichbar?
- ✅ Prüfen: Port 8081 frei?
- ✅ Prüfen: Keycloak erreichbar? (Optional)

### Service nicht erreichbar

- ✅ Prüfen: Service in Consul registriert?
- ✅ Prüfen: Circuit Breaker offen?
- ✅ Prüfen: Health Check erfolgreich?

### CORS-Fehler

- ✅ Prüfen: Origin in allowedOriginPatterns?
- ✅ Prüfen: Methode in allowedMethods?
- ✅ Prüfen: allowCredentials korrekt?

### Hohe Latenz

- ✅ Prüfen: response-timeout zu hoch?
- ✅ Prüfen: Backend-Services langsam?
- ✅ Prüfen: Connection Pool ausgeschöpft?

---

## Weitere Ressourcen

- [Gateway README](README-INFRA-GATEWAY.md)
- [Spring Cloud Gateway Dokumentation](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Resilience4j Dokumentation](https://resilience4j.readme.io/)
- [Consul Dokumentation](https://www.consul.io/docs)
