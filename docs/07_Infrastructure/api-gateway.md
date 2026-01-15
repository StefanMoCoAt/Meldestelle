# Infrastruktur: API-Gateway

Dieses Dokument beschreibt die Konfiguration und die Aufgaben des API-Gateways im "Meldestelle"-Projekt.

## Zweck

Das API-Gateway (implementiert mit Spring Cloud Gateway) ist der zentrale, nach außen exponierte Einstiegspunkt für alle HTTP-Anfragen an das System.

Seine Hauptaufgaben sind:
*   **Routing:** Leitet Anfragen an den korrekten Microservice weiter (z.B. `/api/ping/**` -> `ping-service`).
*   **Security:** Erzwingt die Authentifizierung und Autorisierung für alle eingehenden Anfragen. Es validiert die von Keycloak ausgestellten JWTs.
*   **Cross-Cutting Concerns:** Implementiert übergreifende Funktionalitäten wie Rate Limiting, Logging und Circuit Breaking (mit Resilience4j).

## Konfiguration

Die Routen werden in der `application.yml` des Gateways definiert. Die Konfiguration für die Service Discovery erfolgt über Consul.

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: ping-service
          uri: lb://ping-service
          predicates:
            - Path=/api/ping/**
          filters:
            - StripPrefix=2
```
