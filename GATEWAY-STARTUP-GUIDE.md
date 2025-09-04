# Gateway Startup Guide - Korrigierte Befehle

Dieses Dokument erklärt die korrekten Befehle zum Starten des API Gateways sowohl mit Gradle als auch mit Docker.

## Wichtiger Hinweis: Arbeitsverzeichnis

**ALLE BEFEHLE MÜSSEN AUS DEM PROJEKT-ROOT-VERZEICHNIS AUSGEFÜHRT WERDEN:**

```bash
# Sicherstellen, dass Sie im richtigen Verzeichnis sind
cd /home/stefan/WsMeldestelle/Meldestelle

# Überprüfen des aktuellen Verzeichnisses
pwd
# Sollte ausgeben: /home/stefan/WsMeldestelle/Meldestelle

# Überprüfen, dass gradlew vorhanden ist
ls -la gradlew
```

## 1. Gateway mit Gradle starten

### Entwicklungsumgebung (Development)
```bash
# Aus dem Projekt-Root-Verzeichnis:
./gradlew :infrastructure:gateway:bootRun

# Mit spezifischem Profil:
./gradlew :infrastructure:gateway:bootRun --args='--spring.profiles.active=dev'
```

### Produktionsumgebung
```bash
# Gateway JAR bauen:
./gradlew :infrastructure:gateway:bootJar

# Gateway ausführen:
java -jar infrastructure/gateway/build/libs/gateway-*.jar
```

## 2. Gateway mit Docker starten

### Docker Image bauen
```bash
# Aus dem Projekt-Root-Verzeichnis:
docker build -t meldestelle/gateway:latest -f infrastructure/gateway/Dockerfile .

# Mit Build-Argumenten (optional):
docker build \
  --build-arg SPRING_PROFILES_ACTIVE=prod \
  -t meldestelle/gateway:latest \
  -f infrastructure/gateway/Dockerfile .
```

### Docker Container starten
```bash
# Einfacher Start:
docker run -p 8080:8080 meldestelle/gateway:latest

# Mit Umgebungsvariablen:
docker run \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e CONSUL_HOST=localhost \
  -e CONSUL_PORT=8500 \
  --name gateway \
  meldestelle/gateway:latest

# Im Hintergrund starten:
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --name gateway \
  meldestelle/gateway:latest
```

### Docker Container verwalten
```bash
# Container Status prüfen:
docker ps

# Logs anzeigen:
docker logs gateway

# Container stoppen:
docker stop gateway

# Container entfernen:
docker rm gateway

# Image entfernen:
docker rmi meldestelle/gateway:latest
```

## 3. Gateway mit Docker Compose

### docker-compose.yml verwenden
```bash
# Services starten (inkl. Gateway):
docker-compose up -d gateway

# Oder alle Services:
docker-compose up -d

# Logs verfolgen:
docker-compose logs -f gateway

# Services stoppen:
docker-compose down
```

## 4. Fehlerbehebung

### Häufige Fehler und Lösungen

#### "./gradlew: Datei oder Verzeichnis nicht gefunden"
**Problem:** Sie befinden sich nicht im Projekt-Root-Verzeichnis.
**Lösung:**
```bash
cd /home/stefan/WsMeldestelle/Meldestelle
ls -la gradlew  # Sollte die gradlew-Datei anzeigen
```

#### "lstat infrastructure: no such file or directory"
**Problem:** Docker build wird mit falschem Kontext ausgeführt.
**Lösung:**
```bash
# Sicherstellen, dass Sie im Projekt-Root sind:
cd /home/stefan/WsMeldestelle/Meldestelle

# Dockerfile-Pfad korrekt angeben:
docker build -t meldestelle/gateway:latest -f infrastructure/gateway/Dockerfile .
```

#### "Image nicht gefunden" beim docker run
**Problem:** Das Image wurde noch nicht gebaut.
**Lösung:**
```bash
# Zuerst das Image bauen:
docker build -t meldestelle/gateway:latest -f infrastructure/gateway/Dockerfile .

# Dann den Container starten:
docker run -p 8080:8080 meldestelle/gateway:latest
```

## 5. Gateway Health Check

Nach dem Start können Sie die Gateway-Gesundheit überprüfen:

```bash
# Health Endpoint:
curl http://localhost:8080/actuator/health

# Metriken:
curl http://localhost:8080/actuator/metrics

# Gateway-Routen:
curl http://localhost:8080/actuator/gateway/routes
```

## 6. Umgebungsvariablen

Wichtige Umgebungsvariablen für die Gateway-Konfiguration:

```bash
# Spring Profil
export SPRING_PROFILES_ACTIVE=dev|test|prod

# Consul Konfiguration
export CONSUL_HOST=localhost
export CONSUL_PORT=8500

# Gateway Admin Credentials
export GATEWAY_ADMIN_USER=admin
export GATEWAY_ADMIN_PASSWORD=secure-password

# Logging Level
export LOGGING_LEVEL_ROOT=INFO
export LOGGING_LEVEL_GATEWAY=DEBUG
```

## 7. Zusammenfassung der korrekten Befehle

```bash
# IMMER aus dem Projekt-Root-Verzeichnis:
cd /home/stefan/WsMeldestelle/Meldestelle

# Gateway mit Gradle starten:
./gradlew :infrastructure:gateway:bootRun

# Gateway Docker Image bauen:
docker build -t meldestelle/gateway:latest -f infrastructure/gateway/Dockerfile .

# Gateway Container starten:
docker run -p 8080:8080 meldestelle/gateway:latest
```

---

**Wichtiger Hinweis:** Alle Pfade sind relativ zum Projekt-Root-Verzeichnis (`/home/stefan/WsMeldestelle/Meldestelle`). Stellen Sie sicher, dass Sie sich immer in diesem Verzeichnis befinden, bevor Sie die Befehle ausführen.
