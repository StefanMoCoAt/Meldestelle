Zusammengefasst ergibt sich daraus folgender, konkreter Fahrplan:

1.  **Schritt 0: Aufräumen (ca. 1-2 Stunden)**
    *   [ ] Entfernen Sie den auskommentierten Ktor-Code aus der `infrastructure:gateway:build.gradle.kts`.
    *   [ ] Refaktorieren Sie die Test-Route in `GatewayApplicationTests.kt` auf die Kotlin DSL von Spring Cloud Gateway.
    *   [ ] **(Optional)** Führen Sie `value class`es für stark typisierte IDs oder Konfigurationsparameter im `core`-Modul ein.

2.  **Schritt 1: Phase 2 - Den "Ping-Service" bauen**
    *   [ ] Erstellen Sie ein neues Gradle-Modul `:temp:ping-service`.
    *   [ ] Implementieren Sie eine simple Spring Boot Anwendung darin.
    *   [ ] Fügen Sie die Abhängigkeiten zu `spring-boot-starter-web`, `spring-cloud-starter-consul-discovery` und Ihrem `platform:platform-dependencies` hinzu.
    *   [ ] Erstellen Sie einen `RestController` mit einem `GET /ping` Endpunkt, der `mapOf("status" to "pong")` zurückgibt.
    *   [ ] Konfigurieren Sie die `application.yml` des Services, damit er sich bei Consul registriert und einen eindeutigen Namen (`spring.application.name=ping-service`) hat.

3.  **Schritt 2: Phase 3 - Gateway-Route konfigurieren**
    *   [ ] Fügen Sie in der `application.yml` Ihres Gateways eine Route hinzu, die Anfragen von `/api/ping` an den `ping-service` weiterleitet (Load Balanced via `lb://ping-service`).

4.  **Schritt 3: Phase 4 - Gesamtsystem testen**
    *   [ ] Starten Sie Consul, den Gateway und den Ping-Service.
    *   [ ] Rufen Sie die Gateway-URL (z.B. `http://localhost:8080/api/ping`) auf und verifizieren Sie, dass Sie die `{"status": "pong"}`-Antwort erhalten.
    *   [ ] Erstellen Sie den minimalen "Ping"-Button in Ihrer Client-Anwendung und testen Sie den gesamten Weg.

Wenn Sie diesen Plan abarbeiten, haben Sie nicht nur Ihre Architektur validiert, sondern auch einige Stellen modernisiert und aufgeräumt. Sie sind auf einem exzellenten Weg
