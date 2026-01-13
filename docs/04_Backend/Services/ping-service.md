# Ping-Service

Diese Seite beschreibt den **aktuellen technischen Stand** des `Ping-Service`.
Sie ist der **stabile Einstiegspunkt** ("technische Wahrheit") für diesen Service.

Der `ping-service` dient als Health-Check-Endpunkt und als technische Blaupause für die Implementierung von Services nach den Prinzipien der Clean Architecture im "Meldestelle"-Projekt.

## Architektur & Implementierung

Der Service folgt einem Clean Architecture Ansatz:

*   **Driving Adapter:** Der `PingController` (`infrastructure/web`) nimmt HTTP-Anfragen entgegen.
*   **Application Port:** Das `PingUseCase` (`application`) definiert die Anwendungslogik.
*   **Driven Adapters:** Persistenz-Adapter (nicht im Detail gezeigt) interagieren mit der Datenbank.

Die Implementierung ist vollständig **asynchron** und nutzt **Kotlin Coroutines** und Spring WebFlux.

## API-Definition

Die API-Datenstrukturen (DTOs) und das API-Interface (`PingApi`) sind im KMP-Modul `:contracts:ping-api` definiert, um sie mit dem Frontend zu teilen.

### Wichtigste Endpunkte

Alle Endpunkte sind unter dem Basispfad `/` des Service-Ports (Standard: `8081`) erreichbar.

*   `GET /ping/simple`:
    *   Führt einen einfachen Ping aus, speichert das Ereignis in der Datenbank und gibt eine `PingResponse` zurück.
*   `GET /ping/enhanced`:
    *   Ein erweiterter Ping, der mit einem **Resilience4j Circuit Breaker** abgesichert ist.
    *   Kann einen Fehler simulieren (`?simulate=true`).
    *   Gibt eine `EnhancedPingResponse` mit Latenz und Circuit-Breaker-Status zurück.
*   `GET /ping/health`:
    *   Ein einfacher Health-Check, der den Status des Services zurückgibt (`HealthResponse`).
*   `GET /ping/history`:
    *   Gibt eine Liste der letzten Ping-Ereignisse aus der Datenbank zurück.

### Security

Die Endpunkte sind aktuell **nicht** durch Spring Security auf Service-Ebene geschützt. Die Absicherung erfolgt auf Ebene des API-Gateways.

## Historie

Der ursprüngliche Arbeitsauftrag zur Implementierung dieses Services ist unter `docs/90_Reports/Ping-Service_Impl_01-2026.md` zu finden, ist aber **veraltet** und spiegelt nicht mehr den aktuellen Stand wider.
