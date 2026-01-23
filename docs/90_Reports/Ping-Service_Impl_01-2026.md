---
type: Report
status: ARCHIVED
owner: Lead Architect
date: 2026-01-10
tags: [backend, ping-service, task]
---

# Arbeitsauftrag: Implementierung des `ping-service` (Tracer Bullet)

**ARCHIVED:** This is the original task description. The implementation has evolved. Please refer to `docs/05_Backend/Services/PingService_Reference.md`.

---

**An:** Senior Backend Developer
**Von:** Lead Software Architect
**Betreff:** Arbeitsauftrag: Implementierung des `ping-service` (Tracer Bullet)

Guten Tag,

deine nächste Aufgabe ist die Implementierung unseres ersten Microservices, des `ping-service`. Dieser Service ist von
strategischer Bedeutung, da er als **"Tracer Bullet"** und **Blaupause** für alle zukünftigen fachlichen Services dient.

Mit dieser Implementierung validieren wir die gesamte Kette: von der Service-Registrierung bei Consul über das
Gateway-Routing und die Security mit Keycloak bis hin zur Observability mit Zipkin.

Deine Expertise in Spring Boot, DDD und sauberer Architektur ist hier entscheidend, um eine qualitativ hochwertige und
wiederverwendbare Vorlage zu schaffen.

## Deine Aufgaben im Detail:

1. Modulstruktur anlegen

Bitte lege die folgende Modulstruktur an. Beachte die neue, klarere Benennung des
Implementierungsmoduls:

- `:contracts:ping-api`: Enthält die KMP-kompatiblen DTOs.
- `:backend:services:ping:ping-service`: Enthält die Spring Boot Anwendung, Controller und Konfiguration.

Stelle sicher, dass die Module in der `settings.gradle.kts` registriert sind.

  ```kotlin 
  include(
  ":platform:platform-bom",
  ":platform:platform-testing",
  ":contracts:ping-api",
  ":backend:services:ping:ping-service",
  ":backend:infrastructure:gateway",
  // ":backend:services:registry:registry-api",
  // ":backend:services:registry:registry-domain",
  ```

2. API-Definition in `:ping-api`

Definiere in `ping-api/src/commonMain/kotlin` ein einfaches, serialisierbares DTO. Dieses Modul darf **keine
JVM-spezifischen Abhängigkeiten** enthalten, um die KMP-Kompatibilität für das Frontend zu gewährleisten.

  ```kotlin
  PingResponse.kt
  ```
  ```kotlin
  package de.meldestelle.api.ping
  
  import kotlinx . serialization . Serializable

  @Serializable
  data class PingResponse(
    val message: String,
    val principal: String? = null
  )
  ```

3. Service-Implementierung in :ping-service
   
Implementiere die Spring Boot Anwendung.

- **`PingController.kt`:**
 - **`GET /api/ping`:** Ein öffentlicher Endpunkt, der eine `PingResponse("Pong", "anonymous")` zurückgibt.
 - **`GET /api/ping/secure`:** Ein durch Spring Security geschützter Endpunkt. Er soll den `preferred_username` aus dem `Jwt` Principal extrahieren und in der `PingResponse` zurückgeben.

Hier ist ein Implementierungsvorschlag für den Controller:

  ```kotlin
  // in backend/services/ping/ping-service/src/main/kotlin/.../PingController.kt
  
  @RestController
  @RequestMapping("/api/ping")
  class PingController {
  
      @GetMapping
      fun pingPublic(): PingResponse {
          return PingResponse(message = "Pong", principal = "anonymous")
      }
  
      @GetMapping("/secure")
      fun pingSecure(principal: Jwt): PingResponse {
          val username = principal.getClaimAsString("preferred_username")
          return PingResponse(message = "Pong (Secure)", principal = username)
      }
  }
  ```

4. **Konfiguration**

Erstelle die `application.yml` für den Service. Sie muss die Anwendung für unsere Infrastruktur korrekt konfigurieren:

- **Service Name:** ping-service
- **Service Discovery:** Registrierung bei Consul.
- **Security:** Konfiguration als Resource Server, der JWTs vom `issuer-uri` unseres Keycloak-Containers validiert.
- **Observability:** Actuator-Endpunkte (`health`, `info`, `prometheus`) freigeben und Tracing aktivieren.

  ```yaml
  # in backend/services/ping/ping-service/src/main/resources/application.yml
  
  server:
    port: 8081 # Eindeutiger Port für den Service
  
  spring:
    application:
      name: ping-service
  
    # --- Consul Discovery ---
    cloud:
      consul:
        host: consul
        port: 8500
        discovery:
          instance-id: \${spring.application.name}:\${random.value}
          health-check-path: /actuator/health
          health-check-interval: 10s
  
    # --- Security (Keycloak) ---
    security:
      oauth2:
        resourceserver:
          jwt:
            issuer-uri: http://keycloak:8080/realms/meldestelle
  
  # --- Observability ---
  management:
    endpoints:
      web:
        exposure:
          include: "health,info,prometheus"
    tracing:
      sampling:
        probability: 1.0 # Trace every request
  
  logging:
    pattern:
      level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
  ```
  
5. **Build-Konfiguration(`build.gradle.kts`)

Achte auf die korrekte und saubere Definition der Abhängigkeiten.

- `ping-api/build.gradle.kts`
  ```kotlin
      plugins {
        alias(libs.plugins.kotlin.multiplatform)
        alias(libs.plugins.kotlin.serialization)
    }

    kotlin {
        jvm() // Für die Nutzung im Backend
        js(IR) { browser() } // Für die Nutzung im Frontend
        sourceSets {
            commonMain.dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
  ```
  
- `ping-service/build.gradle.kts`
  ```kotlin
      plugins {
        alias(libs.plugins.spring.boot)
        alias(libs.plugins.kotlin.jvm)
        alias(libs.plugins.kotlin.spring)
    }

    dependencies {
        // API-Modul einbinden
        implementation(project(":contracts:ping-api"))

        // Unsere zentrale BOM für konsistente Versionen
        implementation(platform(project(":platform:platform-bom")))

        // Spring Boot Starter
        implementation(libs.spring.boot.starter.web)
        implementation(libs.spring.boot.starter.actuator)
        implementation(libs.spring.boot.starter.security)
        implementation(libs.spring.boot.starter.oauth2.resource.server)

        // Spring Cloud (Consul, OpenFeign etc.)
        implementation(libs.spring.cloud.starter.consul.discovery)

        // Test-Abhängigkeiten
        testImplementation(platform(project(":platform:platform-testing")))
        testImplementation(libs.bundles.test.spring)
    }
  ```
  
## Definition of Done:

Der Auftrag gilt als erledigt, wenn:
1. Die Anwendung erfolgreich startet und sich im Consul UI als `UP` registriert.
2. Ein `GET`-Request auf `http//localhost:8081/api/ping` (über das Gateway) den Status `200 OK` und die `{"message":"Pong", "principal":"anonymous"}` zurückgibt.
3. Ein `GET`-Request auf `http//localhost:8081/api/ping/secure` ohne Token den Status `401 Unauthorized` zurückgibt.
4. Ein `GET`-Request auf `http//localhost:8081/api/ping/secure` mit einem gültigen Keycloak-Token deb Status `200 OK` und eine Antwort mit dem korrekten Benutzernamen zurückgibt.
5. Die Requests in der Zipkin UI als Trace sichtbar sind.

Bei Fragen zur Konfiguration oder zur Architektur stehe ich dir zur Verfügung.
