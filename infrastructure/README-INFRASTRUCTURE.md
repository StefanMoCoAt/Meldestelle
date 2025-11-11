# Infrastructure Module

## Überblick

Das Infrastructure-Modul stellt die technische Grundlage für das gesamte Meldestelle-System bereit. Es implementiert alle querschnittlichen Infrastrukturkomponenten, die von den Geschäftsmodulen (members, horses, events, masterdata) benötigt werden. Das Modul folgt dem Prinzip der Separation of Concerns und bietet wiederverwendbare, skalierbare Infrastrukturdienste.

## Architektur

Das Infrastructure-Modul ist in 6 Hauptkomponenten unterteilt:

```
infrastructure/
├── auth/                        # Authentifizierung und Autorisierung
│   ├── auth-client/            # Client-seitige Auth-Komponenten
│   └── auth-server/            # Server-seitige Auth-Services
├── cache/                      # Caching-Infrastruktur
│   ├── cache-api/              # Cache-Abstraktionen
│   └── redis-cache/            # Redis-basierte Cache-Implementierung
├── event-store/                # Event Sourcing
│   ├── event-store-api/        # Event Store Abstraktionen
│   └── redis-event-store/      # Redis-basierte Event Store Implementierung
├── gateway/                    # API Gateway
│   ├── src/                    # Gateway-Implementierung
│   ├── docs/                   # Gateway-Dokumentation
│   └── build/                  # Build-Artefakte
├── messaging/                  # Messaging-System
│   ├── messaging-client/       # Messaging-Client
│   └── messaging-config/       # Messaging-Konfiguration
└── monitoring/                 # Monitoring und Observability
    ├── monitoring-client/      # Monitoring-Client
    └── monitoring-server/      # Monitoring-Server
```

## Komponenten-Übersicht

### 1. Authentication & Authorization (auth/)

Zentrale Authentifizierungs- und Autorisierungskomponente basierend auf OAuth 2.0 und JWT.

#### Features

- **JWT Token Management** - Erstellung, Validierung und Refresh von JWT-Tokens
- **OAuth 2.0 Integration** - Unterstützung für OAuth 2.0 Flows
- **Role-Based Access Control (RBAC)** - Rollenbasierte Zugriffskontrolle
- **Keycloak Integration** - Integration mit Keycloak Identity Provider
- **Session Management** - Sichere Session-Verwaltung

#### Komponenten

- **auth-client**: Client-seitige Authentifizierungslogik
- **auth-server**: Server-seitige Authentifizierungsdienste

#### Verwendung

```kotlin
// JWT Token validieren
val tokenValidator = JwtTokenValidator()
val claims = tokenValidator.validate(token)

// Benutzer authentifizieren
val authService = AuthenticationService()
val user = authService.authenticate(credentials)
```

### 2. Caching (cache/)

Hochperformante Caching-Lösung für verbesserte Anwendungsleistung.

#### Features

- **Redis Integration** - Redis als primärer Cache-Store
- **Multi-Level Caching** - L1 (In-Memory) und L2 (Redis) Cache
- **Cache Invalidation** - Intelligente Cache-Invalidierungsstrategien
- **TTL Management** - Flexible Time-To-Live Konfiguration
- **Cache Statistics** - Monitoring und Metriken

#### Komponenten

- **cache-api**: Cache-Abstraktionen und Interfaces
- **redis-cache**: Redis-basierte Cache-Implementierung

#### Verwendung

```kotlin
// Cache-Service verwenden
val cacheService = RedisCacheService()
cacheService.put("key", value, Duration.ofMinutes(30))
val cachedValue = cacheService.get<String>("key")

// Cache invalidieren
cacheService.invalidate("pattern:*")
```

### 3. Event Store (event-store/)

Event Sourcing Infrastruktur für Domain Events und CQRS-Pattern.

#### Features

- **Event Sourcing** - Persistierung von Domain Events
- **Event Replay** - Wiederherstellung von Aggregaten aus Events
- **Snapshots** - Performance-Optimierung durch Snapshots
- **Event Versioning** - Versionierung von Event-Schemas
- **Stream Processing** - Event-Stream-Verarbeitung

#### Komponenten

- **event-store-api**: Event Store Abstraktionen
- **redis-event-store**: Redis-basierte Event Store Implementierung

#### Verwendung

```kotlin
// Events speichern
val eventStore = RedisEventStore()
eventStore.saveEvents(aggregateId, events, expectedVersion)

// Events laden
val events = eventStore.getEventsForAggregate(aggregateId)

// Event-Stream abonnieren
eventStore.subscribeToStream("member-events") { event ->
    // Event verarbeiten
}
```

### 4. API Gateway (gateway/)

Zentraler Eingangspoint für alle API-Anfragen mit Routing, Load Balancing und Sicherheit.

#### Features

- **Request Routing** - Intelligentes Routing zu Microservices
- **Load Balancing** - Lastverteilung zwischen Service-Instanzen
- **Rate Limiting** - Schutz vor Überlastung
- **API Versioning** - Unterstützung für API-Versionierung
- **Request/Response Transformation** - Datenformat-Transformationen
- **Security** - Authentifizierung und Autorisierung
- **Monitoring** - Request-Tracking und Metriken

#### Konfiguration

```yaml
# gateway-config.yml
routes:
  - id: members-service
    uri: http://members-service:8082
    predicates:
      - Path=/api/members/**
    filters:
      - StripPrefix=2
      - RateLimit=100,1m
```

### 5. Messaging (messaging/)

Asynchrone Kommunikation zwischen Services über Message Queues.

#### Features

- **Apache Kafka Integration** - Kafka als Message Broker
- **Event-Driven Architecture** - Unterstützung für Event-driven Patterns
- **Message Serialization** - JSON und Avro Serialisierung
- **Dead Letter Queues** - Fehlerbehandlung für nicht verarbeitbare Nachrichten
- **Consumer Groups** - Skalierbare Message-Verarbeitung

#### Komponenten

- **messaging-client**: Kafka-Client-Bibliothek
- **messaging-config**: Messaging-Konfiguration

#### Verwendung

```kotlin
// Message Producer
val producer = KafkaMessageProducer()
producer.send("member-events", memberCreatedEvent)

// Message Consumer
val consumer = KafkaMessageConsumer()
consumer.subscribe("member-events") { message ->
    // Message verarbeiten
}
```

### 6. Monitoring (monitoring/)

Umfassende Monitoring- und Observability-Lösung.

#### Features

- **Metrics Collection** - Sammlung von Anwendungsmetriken
- **Distributed Tracing** - Zipkin-Integration für Request-Tracing
- **Health Checks** - Service-Gesundheitsprüfungen
- **Alerting** - Automatische Benachrichtigungen bei Problemen
- **Dashboards** - Grafana-Integration für Visualisierung

#### Komponenten

- **monitoring-client**: Client-seitige Monitoring-Bibliothek
- **monitoring-server**: Monitoring-Server und Aggregation

#### Metriken

```kotlin
// Custom Metrics
val meterRegistry = PrometheusMeterRegistry()
val counter = Counter.builder("member.created")
    .register(meterRegistry)

counter.increment()

// Timing
Timer.Sample.start(meterRegistry)
    .stop(Timer.builder("member.creation.time")
        .register(meterRegistry))
```

## Technologie-Stack

### Datenbanken und Speicher

- **Redis 7.0** - Caching und Event Store
- **PostgreSQL 16** - Relationale Datenbank (über Domain-Module)

### Message Broker

- **Apache Kafka 7.5.0** - Event Streaming und Messaging

### Monitoring und Observability

- **Prometheus** - Metriken-Sammlung
- **Grafana** - Dashboards und Visualisierung
- **Zipkin** - Distributed Tracing

### Security

- **Keycloak 26.4.2** - Identity und Access Management
- **JWT** - Token-basierte Authentifizierung

### API Gateway

- **Spring Cloud Gateway** - API Gateway Implementierung
- **Nginx** - Reverse Proxy und Load Balancer

## Konfiguration

### Docker Compose

```yaml
# docker-compose.yml (Auszug)
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092

  keycloak:
    image: quay.io/keycloak/keycloak:26.4.2
    environment:
      KC_BOOTSTRAP_ADMIN_USERNAME: admin
      KC_BOOTSTRAP_ADMIN_PASSWORD: admin
    ports:
      - "8080:8080"
```

### Umgebungsvariablen

```bash
# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_GROUP_ID=meldestelle-group

# Keycloak Configuration
KEYCLOAK_URL=http://localhost:8080
KEYCLOAK_REALM=meldestelle
KEYCLOAK_CLIENT_ID=meldestelle-client

# Monitoring Configuration
PROMETHEUS_URL=http://localhost:9090
ZIPKIN_URL=http://localhost:9411
```

## Service Discovery

### Consul Integration

```kotlin
// Service Registration
val consulClient = ConsulClient()
val service = NewService().apply {
    id = "members-service-1"
    name = "members-service"
    address = "localhost"
    port = 8082
    check = NewService.Check().apply {
        http = "http://localhost:8082/actuator/health"
        interval = "10s"
    }
}
consulClient.agentServiceRegister(service)
```

## Sicherheit

### JWT Token Struktur

```json
{
  "sub": "user123",
  "iss": "meldestelle-auth",
  "aud": "meldestelle-api",
  "exp": 1640995200,
  "iat": 1640991600,
  "roles": ["MEMBER", "TRAINER"],
  "permissions": ["READ_HORSES", "WRITE_EVENTS"]
}
```

### RBAC Rollen

- **ADMIN** - Vollzugriff auf alle Ressourcen
- **TRAINER** - Zugriff auf Pferde und Veranstaltungen
- **MEMBER** - Zugriff auf eigene Daten
- **GUEST** - Nur Lesezugriff auf öffentliche Daten

## Performance und Skalierung

### Caching-Strategien

1. **Application-Level Caching** - In-Memory Cache für häufig verwendete Daten
2. **Database Query Caching** - Redis-Cache für Datenbankabfragen
3. **HTTP Response Caching** - Gateway-Level Caching für API-Responses
4. **CDN Caching** - Content Delivery Network für statische Inhalte

### Load Balancing

```nginx
# nginx.conf
upstream members-service {
    server members-service-1:8082;
    server members-service-2:8082;
    server members-service-3:8082;
}

location /api/members/ {
    proxy_pass http://members-service;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```

## Monitoring und Alerting

### Prometheus Metriken

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'meldestelle-services'
    static_configs:
      - targets:
        - 'members-service:8082'
        - 'horses-service:8083'
        - 'events-service:8084'
        - 'gateway:8081'
```

### Grafana Dashboards

- **System Overview** - Gesamtsystem-Metriken
- **Service Health** - Service-spezifische Gesundheitsindikatoren
- **API Performance** - Request-Zeiten und Durchsatz
- **Error Rates** - Fehlerquoten und -trends
- **Infrastructure** - Redis, Kafka, Database Metriken

### Alerting Rules

```yaml
# alerting-rules.yml
groups:
  - name: meldestelle-alerts
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
        for: 5m
        annotations:
          summary: "High error rate detected"

      - alert: ServiceDown
        expr: up == 0
        for: 1m
        annotations:
          summary: "Service is down"
```

## Deployment

### Kubernetes

```yaml
# infrastructure-deployment.yml
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
        image: meldestelle/api-gateway:latest
        ports:
        - containerPort: 8081
        env:
        - name: REDIS_HOST
          value: "redis-service"
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
```

### Helm Charts

```yaml
# values.yml
redis:
  enabled: true
  auth:
    enabled: false
  master:
    persistence:
      enabled: true
      size: 8Gi

kafka:
  enabled: true
  replicaCount: 3
  persistence:
    enabled: true
    size: 10Gi

monitoring:
  prometheus:
    enabled: true
  grafana:
    enabled: true
    adminPassword: "admin"
```

## Entwicklung

### Lokale Entwicklung

```bash
# Infrastructure Services starten
docker-compose up -d redis kafka keycloak prometheus grafana zipkin

# Gateway starten
./gradlew :infrastructure:gateway:bootRun

# Tests ausführen
./gradlew :infrastructure:test
```

### Integration Tests

```kotlin
@SpringBootTest
@Testcontainers
class InfrastructureIntegrationTest {

    @Container
    val redis = GenericContainer<Nothing>("redis:7-alpine")
        .withExposedPorts(6379)

    @Container
    val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))

    @Test
    fun `should cache data in Redis`() {
        // Test Redis Caching
    }

    @Test
    fun `should send and receive Kafka messages`() {
        // Test Kafka Messaging
    }
}
```

## Troubleshooting

### Häufige Probleme

#### Redis Connection Issues

```bash
# Redis Verbindung testen
redis-cli -h localhost -p 6379 ping

# Redis Logs prüfen
docker logs redis-container
```

#### Kafka Connection Issues

```bash
# Kafka Topics auflisten
kafka-topics --bootstrap-server localhost:9092 --list

# Consumer Group Status
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group meldestelle-group
```

#### Gateway Routing Issues

```bash
# Gateway Health Check
curl http://localhost:8081/actuator/health

# Route Configuration prüfen
curl http://localhost:8081/actuator/gateway/routes
```

## Best Practices

### Caching

1. **Cache Warming** - Wichtige Daten beim Start vorwärmen
2. **Cache Invalidation** - Konsistente Invalidierungsstrategien
3. **TTL Configuration** - Angemessene Time-To-Live Werte
4. **Cache Monitoring** - Hit/Miss Ratios überwachen

### Messaging

1. **Idempotenz** - Message-Handler idempotent implementieren
2. **Error Handling** - Retry-Mechanismen und Dead Letter Queues
3. **Schema Evolution** - Backward-kompatible Schema-Änderungen
4. **Monitoring** - Message-Durchsatz und Latenz überwachen

### Security

1. **Token Rotation** - Regelmäßige JWT-Token-Rotation
2. **HTTPS Only** - Ausschließlich verschlüsselte Verbindungen
3. **Rate Limiting** - Schutz vor Brute-Force-Angriffen
4. **Audit Logging** - Vollständige Audit-Trails

## Zukünftige Erweiterungen

1. **Service Mesh** - Istio/Linkerd Integration
2. **Advanced Monitoring** - OpenTelemetry Integration
3. **Multi-Region Deployment** - Geografische Verteilung
4. **Chaos Engineering** - Resilience Testing
5. **GraphQL Gateway** - GraphQL API-Unterstützung
6. **Event Sourcing Enhancements** - Advanced Event Store Features
7. **AI/ML Integration** - Machine Learning Pipeline Integration
8. **Blockchain Integration** - Distributed Ledger für Audit-Trails

---

**Letzte Aktualisierung**: 25. Juli 2025

Für weitere Informationen zur Gesamtarchitektur siehe [README.md](../README.md).
