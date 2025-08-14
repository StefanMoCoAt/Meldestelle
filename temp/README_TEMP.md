# Ping Service - Circuit Breaker Demo

## ⚠️ Wichtiger Hinweis

Dieses Modul (`:temp:ping-service`) ist ein **temporärer Service** ausschließlich für Testzwecke. Seine Aufgabe ist die Validierung der technischen Infrastruktur im Rahmen des **"Tracer Bullet"-Szenarios** und die Demonstration von **Circuit Breaker Patterns**.

Nachdem der End-to-End-Test erfolgreich war, sollte dieses Modul in der `settings.gradle.kts` wieder deaktiviert oder vollständig entfernt werden.

## 📋 Inhaltsverzeichnis

- [Überblick](#überblick)
- [Architektur & Features](#architektur--features)
- [API Endpoints](#api-endpoints)
- [Konfiguration](#konfiguration)
- [Lokale Entwicklung](#lokale-entwicklung)
- [Docker Deployment](#docker-deployment)
- [Testing](#testing)
- [Monitoring & Health Checks](#monitoring--health-checks)
- [Troubleshooting](#troubleshooting)

## 🎯 Überblick

Der `ping-service` ist ein Spring Boot Microservice, der die grundlegende Service-Architektur und moderne Resilience Patterns demonstriert:

- **Circuit Breaker Pattern** mit Resilience4j
- **Service Discovery** mit Spring Cloud Consul
- **Health Checks** und **Monitoring** mit Spring Boot Actuator
- **Containerisierte Deployment** mit optimiertem Docker Setup
- **Comprehensive Testing** mit Integration- und Unit-Tests

## 🏗️ Architektur & Features

### Technology Stack
- **Spring Boot 3.2.5** - Modern Java/Kotlin web framework
- **Kotlin** - Primary programming language
- **Resilience4j** - Circuit breaker and fault tolerance
- **Spring Cloud Consul** - Service discovery and configuration
- **Micrometer + Prometheus** - Metrics collection
- **Docker** - Containerization with multi-stage builds

### Circuit Breaker Configuration
Der Service verwendet Resilience4j Circuit Breaker mit folgenden Einstellungen:
- **Failure Rate Threshold**: 60% (Circuit öffnet bei 60% Fehlern)
- **Minimum Calls**: 4 (Mindestanzahl Calls für Berechnung)
- **Wait Duration**: 5s (Wartezeit im OPEN Status)
- **Half-Open Calls**: 3 (Anzahl Calls im HALF_OPEN Status)

## 🚀 API Endpoints

### 1. Standard Ping Endpoint
```http
GET /ping
```
**Beschreibung**: Einfacher Ping ohne Circuit Breaker
**Response**:
```json
{
  "status": "pong"
}
```

### 2. Enhanced Ping mit Circuit Breaker
```http
GET /ping/enhanced?simulate=false
```
**Beschreibung**: Ping mit Circuit Breaker Schutz
**Query Parameter**:
- `simulate` (optional): `true` für Failure-Simulation

**Success Response**:
```json
{
  "status": "pong",
  "timestamp": "2025-08-14 12:26:30",
  "service": "ping-service",
  "circuitBreaker": "CLOSED"
}
```

**Fallback Response** (Circuit Breaker OPEN):
```json
{
  "status": "fallback",
  "message": "Service temporarily unavailable",
  "timestamp": "2025-08-14 12:26:30",
  "service": "ping-service-fallback",
  "circuitBreaker": "OPEN",
  "error": "Simulated service failure"
}
```

### 3. Health Check Endpoint
```http
GET /ping/health
```
**Beschreibung**: Health Check mit Circuit Breaker Status

**Response**:
```json
{
  "status": "UP",
  "timestamp": "2025-08-14 12:26:30",
  "circuitBreaker": "CLOSED"
}
```

### 4. Test Failure Endpoint
```http
GET /ping/test-failure
```
**Beschreibung**: Endpoint zum Testen der Circuit Breaker Funktionalität (60% Failure Rate)

## ⚙️ Konfiguration

### Application Configuration (`application.yml`)
```yaml
spring:
  application:
    name: ping-service
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        register: true
        health-check-path: /actuator/health
        health-check-interval: 10s

server:
  port: 8082

management:
  endpoints:
    web:
      exposure:
        include: health,info,circuitbreakers
  endpoint:
    health:
      show-details: always

resilience4j:
  circuitbreaker:
    instances:
      pingCircuitBreaker:
        failure-rate-threshold: 60
        minimum-number-of-calls: 4
        wait-duration-in-open-state: 5s
        permitted-number-of-calls-in-half-open-state: 3
```

### Environment Variables
- `SPRING_PROFILES_ACTIVE`: Aktives Spring Profil (default: `default`)
- `DEBUG`: Enable Debug-Modus (`true`/`false`, Debug Port: 5005)
- `SERVER_PORT`: Server Port (default: `8082`)

## 💻 Lokale Entwicklung

### Prerequisites
- Java 21+
- Docker (optional)
- Consul (für Service Discovery)

### Service starten
```bash
# Standard Start
./gradlew :temp:ping-service:bootRun

# Mit spezifischem Profil
./gradlew :temp:ping-service:bootRun -Pspring.profiles.active=dev

# Build JAR
./gradlew :temp:ping-service:bootJar
```

### Service testen
```bash
# Standard Ping
curl http://localhost:8082/ping

# Enhanced Ping
curl http://localhost:8082/ping/enhanced

# Health Check
curl http://localhost:8082/ping/health

# Circuit Breaker mit Simulation
curl "http://localhost:8082/ping/enhanced?simulate=true"

# Failure Test
curl http://localhost:8082/ping/test-failure
```

## 🐳 Docker Deployment

### Build Docker Image
```bash
# Von der Projekt-Root ausführen
docker build -t ping-service:latest -f temp/ping-service/Dockerfile .
```

### Run Container
```bash
# Standard Mode
docker run -p 8082:8082 ping-service:latest

# Debug Mode
docker run -p 8082:8082 -p 5005:5005 -e DEBUG=true ping-service:latest

# Mit Environment Variables
docker run -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e LOGGING_LEVEL_ROOT=WARN \
  ping-service:latest
```

### Docker Features
- **Multi-stage Build** für optimale Image-Größe
- **Non-root User** für bessere Sicherheit
- **Health Checks** integriert
- **JVM Optimierungen** für Container-Umgebung
- **Debug Support** über Environment Variables

## 🧪 Testing

### Unit Tests ausführen
```bash
./gradlew :temp:ping-service:test
```

### Integration Tests
```bash
./gradlew :temp:ping-service:integrationTest
```

### Test Coverage
Der Service enthält umfassende Tests für:
- **Controller Tests**: API Endpoint Validierung
- **Circuit Breaker Tests**: Resilience4j Integration
- **Integration Tests**: End-to-End Scenarios
- **Health Check Tests**: Actuator Endpoint Validation

### Test Klassen
- `PingControllerTest`: Controller Unit Tests
- `PingControllerIntegrationTest`: Full Spring Context Tests
- `PingServiceCircuitBreakerTest`: Circuit Breaker Logic Tests

## 📊 Monitoring & Health Checks

### Actuator Endpoints
- **Health**: `GET /actuator/health`
- **Health Readiness**: `GET /actuator/health/readiness`
- **Health Liveness**: `GET /actuator/health/liveness`
- **Info**: `GET /actuator/info`
- **Circuit Breakers**: `GET /actuator/circuitbreakers`
- **Metrics**: `GET /actuator/metrics`
- **Prometheus**: `GET /actuator/prometheus`

### Circuit Breaker Monitoring
```bash
# Circuit Breaker Status
curl http://localhost:8082/actuator/circuitbreakers

# Metrics
curl http://localhost:8082/actuator/metrics/resilience4j.circuitbreaker.calls

# Prometheus Format
curl http://localhost:8082/actuator/prometheus | grep circuit
```

### Service Discovery
Bei aktiviertem Consul wird der Service automatisch registriert:
- **Service Name**: `ping-service`
- **Health Check**: `/actuator/health`
- **Check Interval**: 10 Sekunden

## 🔧 Troubleshooting

### Häufige Probleme

#### 1. Service startet nicht (Port bereits belegt)
```bash
# Port prüfen
netstat -tlnp | grep 8082

# Alternativen Port verwenden
SERVER_PORT=8083 ./gradlew :temp:ping-service:bootRun
```

#### 2. Circuit Breaker öffnet nicht
- Mindestens 4 Calls erforderlich (siehe `minimum-number-of-calls`)
- 60% Failure Rate erforderlich
- Verwende `/ping/test-failure` für Tests

#### 3. Consul Connection Failed
```bash
# Consul Status prüfen
consul agent -dev

# Oder Service ohne Consul starten
spring.cloud.consul.discovery.enabled=false
```

#### 4. Docker Build Fails
```bash
# Build Context prüfen - muss von Projekt-Root ausgeführt werden
docker build -t ping-service:test -f temp/ping-service/Dockerfile .

# Nicht von temp/ping-service/ ausführen!
```

### Debug Mode
```bash
# Debug Mode aktivieren
DEBUG=true ./gradlew :temp:ping-service:bootRun

# Debug Port: 5005
```

### Logs
```bash
# Alle Logs
docker logs <container-id>

# Circuit Breaker Logs
docker logs <container-id> 2>&1 | grep -i circuit

# Health Check Logs
docker logs <container-id> 2>&1 | grep -i health
```

## 📝 Entwicklungsnotizen

### Warum Circuit Breaker?
Der Circuit Breaker Pattern verhindert:
- **Cascade Failures**: Verhindert Ausfall-Kaskaden
- **Resource Exhaustion**: Schont Ressourcen bei Service-Problemen
- **Fast Failure**: Schnelle Fehlerrückmeldung statt lange Timeouts

### Fallback Strategy
Bei OPEN Circuit Breaker:
- Sofortige Fallback-Response (keine Latenz)
- Informative Fehlermeldungen
- Status-Informationen für Debugging

### Production Readiness
- Health Checks für Kubernetes/Docker
- Prometheus Metriken für Monitoring
- Non-root Container für Sicherheit
- Optimierte JVM Settings für Container

---

**Letzte Aktualisierung**: 2025-08-14
**Version**: 1.0.0
**Maintainer**: Meldestelle Development Team
