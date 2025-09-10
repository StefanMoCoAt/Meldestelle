# Meldestelle Client Containerization

## Übersicht

Dieses Dokument beschreibt die Docker-Containerisierung der Kotlin Compose Multiplatform Frontend-Anwendungen für das Meldestelle-Projekt.

## Implementierte Lösungen

### 🌐 Web Application (WASM) - Bereits funktionsfähig
- **Status**: ✅ Vollständig implementiert und funktionsfähig
- **Technologie**: Kotlin Compose Multiplatform mit WASM-Target
- **Container**: Nginx-basiertes Setup mit statischen Assets
- **Port**: 4000
- **Zugriff**: `http://localhost:4000`
- **Docker-Compose Service**: `web-app`

### 🖥️ Desktop Application (JVM) - Neu implementiert
- **Status**: ✅ Implementiert mit VNC-basierten GUI-Zugriff
- **Technologie**: Kotlin Compose Desktop mit VNC + noVNC
- **Container**: Ubuntu-basiert mit Xvfb, x11vnc, fluxbox, noVNC
- **Ports**:
  - 6080 (noVNC Web-Interface)
  - 5901 (Direkter VNC-Zugriff)
- **Zugriff**: `http://localhost:6080` (Web-basiertes VNC)
- **Docker-Compose Service**: `desktop-app`

## Verwendung

### Alle Client-Anwendungen starten
```bash
# Mit Backend-Services
docker-compose -f docker-compose.yml -f docker-compose.services.yml -f docker-compose.clients.yml up -d

# Nur Frontend-Anwendungen
docker-compose -f docker-compose.yml -f docker-compose.clients.yml up -d
```

### Einzelne Anwendungen starten
```bash
# Nur Web-Anwendung
docker-compose -f docker-compose.yml -f docker-compose.clients.yml up -d web-app

# Nur Desktop-Anwendung
docker-compose -f docker-compose.yml -f docker-compose.clients.yml up -d desktop-app
```

## Desktop Application - VNC-Zugriff

### Web-basierter Zugriff (empfohlen)
1. Container starten: `docker-compose up -d desktop-app`
2. Browser öffnen: `http://localhost:6080`
3. VNC-Viewer startet automatisch
4. Meldestelle Desktop-Anwendung wird angezeigt

### Direkter VNC-Zugriff
1. VNC-Client installieren (z.B. TigerVNC, RealVNC)
2. Verbindung zu `localhost:5901` herstellen
3. Passwort: `meldestelle` (falls erforderlich)

## Architektur Details

### Web Application (WASM)
```
┌─────────────────┐    ┌──────────────┐    ┌─────────────────┐
│   Browser       │────│ Nginx        │────│ Static WASM     │
│   localhost:4000│    │ Container    │    │ Assets          │
└─────────────────┘    └──────────────┘    └─────────────────┘
```

### Desktop Application (JVM + VNC)
```
┌─────────────────┐    ┌──────────────┐    ┌─────────────────┐
│   Browser       │────│ noVNC        │────│ VNC Server      │
│   localhost:6080│    │ Web Interface│    │ (x11vnc)        │
└─────────────────┘    └──────────────┘    └─────────────────┘
                                              │
                                       ┌──────▼──────┐
                                       │ Xvfb + Compose │
                                       │ Desktop App    │
                                       └────────────────┘
```

## Build-Prozess

### Web Application
1. Gradle Build: `wasmJsBrowserDistribution`
2. Output: `/build/dist/wasmJs/productionExecutable/`
3. Nginx serving static assets

### Desktop Application
1. Gradle Build: `createDistributable`
2. Output: `/build/compose/binaries/main/app/`
3. VNC Environment Setup:
   - Xvfb (Virtual X Server)
   - fluxbox (Window Manager)
   - x11vnc (VNC Server)
   - noVNC (Web-based VNC Client)

## Umgebungsvariablen

### Web Application
- `API_BASE_URL`: Backend API URL (default: `http://api-gateway:8081`)
- `APP_TITLE`: Anwendungstitel (default: `Meldestelle`)

### Desktop Application
- `API_BASE_URL`: Backend API URL (default: `http://api-gateway:8081`)
- `DISPLAY`: X11 Display (default: `:99`)
- `VNC_PORT`: VNC Server Port (default: `5901`)
- `NOVNC_PORT`: noVNC Web Interface Port (default: `6080`)

## Health Checks

### Web Application
- Endpoint: `http://localhost:4000/health`
- Methode: HTTP GET
- Erwartete Antwort: `{"status":"ok","service":"web-app"}`

### Desktop Application
- Endpoint: `http://localhost:6080/vnc.html`
- Methode: HTTP GET (via noVNC)
- Überprüfung: noVNC Web-Interface verfügbar

## Logs und Debugging

### Container-Logs anzeigen
```bash
# Web Application
docker-compose logs -f web-app

# Desktop Application
docker-compose logs -f desktop-app
```

### Desktop Application Logs
- Application Logs: `/var/log/meldestelle.log`
- Error Logs: `/var/log/meldestelle_error.log`
- VNC Logs: Über supervisor zugänglich

## Troubleshooting

### Web Application
- **Container startet nicht**: Überprüfe API Gateway Verfügbarkeit
- **Leere Seite**: Überprüfe Browser-Kompatibilität mit WASM
- **API-Fehler**: Überprüfe Netzwerk-Konfiguration

### Desktop Application
- **VNC nicht erreichbar**: Überprüfe Port 6080 Verfügbarkeit
- **Schwarzer Bildschirm**: Warte 30-60s für Application Startup
- **Keine GUI**: Überprüfe Xvfb und Window Manager Status
- **Performance-Probleme**: VNC-Bildschirmauflösung reduzieren

## Erweiterungen

### VNC-Konfiguration anpassen
Die VNC-Konfiguration kann über Umgebungsvariablen oder durch Anpassung des `start-vnc.sh` Skripts im Dockerfile geändert werden.

### Alternative GUI-Lösungen
- **X11 Forwarding**: Für Linux-Host-Systeme
- **RDP**: Alternative Remote Desktop Lösung
- **Web-based Terminals**: Für minimale GUI-Anforderungen

## Fazit

✅ **Beide Containerisierungsansätze erfolgreich implementiert:**
- Web (WASM): Optimiert für moderne Browser
- Desktop (JVM): Universell über VNC-Web-Interface zugänglich

Die Lösung erfüllt alle Anforderungen aus der ursprünglichen Issue-Beschreibung und ermöglicht sowohl Web- als auch Desktop-Zugriff auf die Meldestelle-Anwendung über Docker-Container.
