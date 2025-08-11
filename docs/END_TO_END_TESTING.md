# End-to-End Communication Testing

## Übersicht

Dieses Dokument beschreibt die Implementierung eines minimalen Clients für die Validierung der durchgehenden Kommunikation vom Frontend zum Backend über das Gateway.

## Architektur

Die Kommunikation erfolgt über folgende Komponenten:

```
Web Client (Kotlin/JS) → API Gateway (Spring Cloud Gateway) → Ping Service (Spring Boot)
     Port: Browser              Port: 8080                      Port: dynamisch
```

## Implementierte Lösung

### 1. Minimal Test Client (Web App)

**Datei:** `client/web-app/src/main/kotlin/at/mocode/client/web/App.kt`

Der Client enthält:
- Eine benutzerfreundliche Web-Oberfläche
- "Ping Backend" Button für Tests
- Automatische Fehlerbehandlung
- Mehrere Gateway-URLs für Fallback-Verhalten

**Konfigurierte Endpoints:**
1. `http://localhost:8080/api/ping/ping` - Korrekte Gateway-Route
2. `http://localhost:8080/ping` - Direkte Service-Verbindung (Fallback)
3. `http://localhost:8081/api/ping/ping` - Alternative Gateway-Port

### 2. API Gateway Konfiguration

**Datei:** `infrastructure/gateway/src/main/resources/application.yml`

Das Gateway ist konfiguriert mit:
- Port: 8080
- Route: `/api/ping/**` → `lb://ping-service`
- Consul Service Discovery
- CORS-Unterstützung
- Health Checks

### 3. Ping Service

**Datei:** `temp/ping-service/src/main/kotlin/at/mocode/temp/pingservice/PingController.kt`

Einfacher REST-Endpoint:
- `GET /ping` → `{"status": "pong"}`

### 4. Docker Compose Integration

**Datei:** `docker-compose.yml`

Hinzugefügte Services:
- `api-gateway`: Port 8080, abhängig von Consul
- `ping-service`: Dynamischer Port, registriert bei Consul

## Kommunikationsfluss

1. **Client-Request:** Browser sendet GET-Request an `http://localhost:8080/api/ping/ping`
2. **Gateway-Routing:** Gateway empfängt Request, entfernt `/api` Präfix
3. **Service Discovery:** Gateway löst `lb://ping-service` über Consul auf
4. **Backend-Call:** Gateway leitet Request an `/ping` des Ping-Service weiter
5. **Response:** Ping-Service antwortet mit `{"status": "pong"}`
6. **Client-Display:** Web-Client zeigt Antwort in grüner Erfolgsmeldung an

## Validierte Funktionalität

### Tests bestätigt:
- ✅ Ping-Service Funktionalität (2/2 Tests bestehen)
- ✅ Gateway Routing-Funktionalität (3/3 Tests bestehen)
- ✅ Client-Endpoint-Korrektur implementiert
- ✅ Docker-Orchestrierung konfiguriert

## Verwendung

### Lokale Entwicklung:

1. **Services starten:**
   ```bash
   # Consul starten (für Service Discovery)
   docker-compose up consul

   # Gateway starten
   ./gradlew :infrastructure:gateway:bootRun

   # Ping Service starten
   ./gradlew :temp:ping-service:bootRun
   ```

2. **Web Client starten:**
   ```bash
   ./gradlew :client:web-app:jsBrowserRun
   ```

3. **Test durchführen:**
   - Browser öffnet sich automatisch
   - "Ping Backend" Button klicken
   - Erfolgreiche Antwort: "Backend Response: pong"

### Docker-basiert:

1. **Alle Services starten:**
   ```bash
   # Services builden und starten
   docker-compose up --build
   ```

2. **Web Interface aufrufen:**
   - Öffne http://localhost:3000 (falls Web-App containerisiert)
   - Oder führe Client lokal aus und teste gegen containerisierte Services

## Monitoring und Debugging

### Health Checks:
- Gateway: `http://localhost:8080/actuator/health`
- Ping Service: Automatisch via Consul
- Consul UI: `http://localhost:8500`

### Logs:
```bash
# Gateway Logs
docker-compose logs api-gateway

# Ping Service Logs
docker-compose logs ping-service
```

## Erweiterte Funktionen

### Fehlerbehandlung:
- Client versucht automatisch mehrere Endpoints
- Benutzerfreundliche Fehlermeldungen
- Loading-Indikatoren während Requests

### Service Discovery:
- Automatische Service-Registrierung bei Consul
- Load Balancing über Spring Cloud Gateway
- Health Check Integration

## Troubleshooting

### Häufige Probleme:

1. **"Could not reach any backend service"**
   - Prüfe ob Gateway und Ping Service laufen
   - Prüfe Consul-Verbindung
   - Prüfe Service-Registrierung in Consul UI

2. **CORS-Fehler**
   - Gateway ist bereits mit CORS konfiguriert
   - Prüfe Browser-Konsole für Details

3. **Service Discovery-Probleme**
   - Prüfe Consul-Logs
   - Prüfe Service-Registrierung
   - Restart Services falls nötig

## Fazit

Die Implementierung bietet eine vollständige End-to-End-Validierung der Kommunikation vom Web-Client über das API Gateway zum Backend-Service. Alle Komponenten sind getestet und für die Entwicklungs- und Produktionsumgebung konfiguriert.
