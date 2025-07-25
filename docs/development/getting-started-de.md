# Entwicklungsanleitung - Erste Schritte

## Überblick

Diese Anleitung hilft neuen Entwicklern beim Einstieg in das Meldestelle-Projekt. Sie deckt alle notwendigen Schritte ab, von der initialen Einrichtung bis zur ersten erfolgreichen Entwicklungsumgebung.

## Voraussetzungen

### System-Anforderungen

- **Betriebssystem**: Windows 10+, macOS 10.15+, oder Linux (Ubuntu 20.04+ empfohlen)
- **RAM**: Mindestens 8GB (16GB empfohlen)
- **Speicher**: Mindestens 10GB freier Speicherplatz
- **Netzwerk**: Stabile Internetverbindung für Downloads

### Erforderliche Software

#### 1. Java Development Kit (JDK)
```bash
# Java 21 installieren (empfohlen: Eclipse Temurin)
# Windows (mit Chocolatey)
choco install temurin21

# macOS (mit Homebrew)
brew install --cask temurin21

# Linux (Ubuntu/Debian)
sudo apt update
sudo apt install openjdk-21-jdk

# Verifizierung
java -version
javac -version
```

#### 2. Docker und Docker Compose
```bash
# Docker Desktop installieren (Windows/macOS)
# Herunterladen von: https://www.docker.com/products/docker-desktop

# Linux (Ubuntu)
sudo apt update
sudo apt install docker.io docker-compose
sudo usermod -aG docker $USER

# Verifizierung
docker --version
docker-compose --version
```

#### 3. Git
```bash
# Windows (mit Chocolatey)
choco install git

# macOS (mit Homebrew)
brew install git

# Linux (Ubuntu)
sudo apt install git

# Verifizierung
git --version
```

#### 4. IDE (Empfohlen: IntelliJ IDEA)
```bash
# IntelliJ IDEA Community Edition
# Herunterladen von: https://www.jetbrains.com/idea/download/

# Oder mit Package Manager
# Windows (Chocolatey)
choco install intellijidea-community

# macOS (Homebrew)
brew install --cask intellij-idea-ce

# Linux (Snap)
sudo snap install intellij-idea-community --classic
```

## Projekt-Setup

### 1. Repository klonen

```bash
# Repository klonen
git clone <repository-url>
cd Meldestelle

# Branch-Status prüfen
git status
git branch -a
```

### 2. Umgebungsvariablen konfigurieren

```bash
# .env-Datei erstellen (falls nicht vorhanden)
cp .env.example .env

# .env-Datei bearbeiten
nano .env  # oder mit bevorzugtem Editor
```

#### Wichtige Umgebungsvariablen

```bash
# Anwendungskonfiguration
APP_ENVIRONMENT=development
APP_NAME=meldestelle
APP_VERSION=1.0.0

# Datenbank-Konfiguration
DATABASE_URL=jdbc:postgresql://localhost:5432/meldestelle
DATABASE_USERNAME=meldestelle
DATABASE_PASSWORD=password

# Redis-Konfiguration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Keycloak-Konfiguration
KEYCLOAK_URL=http://localhost:8080
KEYCLOAK_REALM=meldestelle
KEYCLOAK_CLIENT_ID=meldestelle-client

# Kafka-Konfiguration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_GROUP_ID=meldestelle-group
```

### 3. Docker-Infrastruktur starten

```bash
# Alle Services starten
docker-compose up -d

# Status überprüfen
docker-compose ps

# Logs anzeigen (optional)
docker-compose logs -f

# Einzelne Services starten (falls gewünscht)
docker-compose up -d postgres redis keycloak
```

#### Service-Übersicht

| Service | Port | Beschreibung | URL |
|---------|------|--------------|-----|
| PostgreSQL | 5432 | Hauptdatenbank | localhost:5432 |
| Redis | 6379 | Cache & Event Store | localhost:6379 |
| Keycloak | 8080 | Authentifizierung | http://localhost:8080 |
| Kafka | 9092 | Messaging | localhost:9092 |
| Zookeeper | 2181 | Kafka Koordination | localhost:2181 |
| Zipkin | 9411 | Distributed Tracing | http://localhost:9411 |
| Prometheus | 9090 | Metriken | http://localhost:9090 |
| Grafana | 3000 | Dashboards | http://localhost:3000 |

### 4. Umgebung validieren

```bash
# Validierungsskript ausführen
./validate-env.sh

# Oder manuell prüfen
docker-compose ps
curl http://localhost:8080/auth/realms/meldestelle
```

## IDE-Konfiguration

### IntelliJ IDEA Setup

#### 1. Projekt öffnen
1. IntelliJ IDEA starten
2. "Open" wählen
3. Meldestelle-Projektverzeichnis auswählen
4. "Open as Project" bestätigen

#### 2. Kotlin-Plugin aktivieren
1. File → Settings (Ctrl+Alt+S)
2. Plugins → Marketplace
3. "Kotlin" suchen und installieren
4. IDE neu starten

#### 3. Gradle-Konfiguration
1. File → Settings → Build → Gradle
2. "Use Gradle from" → "gradle-wrapper.properties file"
3. "Gradle JVM" → Java 21 auswählen
4. "Apply" und "OK"

#### 4. Code-Style konfigurieren
1. File → Settings → Editor → Code Style
2. Scheme → "Project" auswählen
3. Kotlin → Tabs and Indents:
   - Tab size: 4
   - Indent: 4
   - Continuation indent: 8

#### 5. Nützliche Plugins installieren
- **Docker**: Docker-Integration
- **Database Tools**: Datenbankzugriff
- **GitToolBox**: Erweiterte Git-Features
- **Rainbow Brackets**: Bessere Klammer-Visualisierung
- **String Manipulation**: Text-Utilities

### VS Code Setup (Alternative)

#### 1. Erforderliche Extensions
```bash
# Extension Pack for Java
code --install-extension vscjava.vscode-java-pack

# Kotlin Language
code --install-extension fwcd.kotlin

# Docker
code --install-extension ms-azuretools.vscode-docker

# GitLens
code --install-extension eamodio.gitlens
```

#### 2. Workspace-Konfiguration
```json
{
    "java.home": "/path/to/java-21",
    "java.configuration.updateBuildConfiguration": "automatic",
    "kotlin.languageServer.enabled": true,
    "docker.showStartPage": false
}
```

Erstellen Sie diese Datei als `.vscode/settings.json` im Projektverzeichnis.

## Projekt-Architektur verstehen

### Modulare Struktur

```
Meldestelle/
├── core/                    # Shared Kernel
│   ├── core-domain/        # Gemeinsame Domain-Modelle
│   └── core-utils/         # Utilities und Konfiguration
├── members/                 # Mitgliederverwaltung
│   ├── members-domain/     # Domain Layer
│   ├── members-application/ # Application Layer
│   ├── members-infrastructure/ # Infrastructure Layer
│   ├── members-api/        # API Layer
│   └── members-service/    # Service Layer
├── horses/                  # Pferderegistrierung
├── events/                  # Veranstaltungsverwaltung
├── masterdata/             # Stammdatenverwaltung
├── infrastructure/         # Infrastruktur-Services
├── client/                 # Client-Anwendungen
└── docs/                   # Dokumentation
```

### Clean Architecture Prinzipien

1. **Domain Layer**: Geschäftslogik und Entitäten
2. **Application Layer**: Use Cases und Orchestrierung
3. **Infrastructure Layer**: Datenbankzugriff und externe Services
4. **API Layer**: REST-Controller und DTOs
5. **Service Layer**: Spring Boot Anwendungen

### Technologie-Stack

- **Backend**: Kotlin + Spring Boot
- **Datenbank**: PostgreSQL + Exposed ORM
- **Caching**: Redis
- **Messaging**: Apache Kafka
- **Authentifizierung**: Keycloak + JWT
- **Monitoring**: Prometheus + Grafana
- **Tracing**: Zipkin
- **Frontend**: Jetpack Compose (Desktop/Web)
- **Build**: Gradle mit Kotlin DSL

## Erste Entwicklungsschritte

### 1. Projekt kompilieren

```bash
# Vollständigen Build ausführen
./gradlew build

# Nur kompilieren (ohne Tests)
./gradlew compileKotlin

# Spezifisches Modul kompilieren
./gradlew :members:members-service:build
```

### 2. Tests ausführen

```bash
# Alle Tests
./gradlew test

# Modul-spezifische Tests
./gradlew :members:test

# Integration Tests
./gradlew integrationTest

# Test-Reports anzeigen
open build/reports/tests/test/index.html
```

### 3. Services starten

```bash
# Einzelnen Service starten
./gradlew :members:members-service:bootRun

# Mit spezifischem Profil
./gradlew :members:members-service:bootRun --args='--spring.profiles.active=dev'

# API Gateway starten
./gradlew :infrastructure:gateway:bootRun
```

### 4. Datenbank-Migrationen

```bash
# Flyway-Migrationen ausführen
./gradlew flywayMigrate

# Migration-Status prüfen
./gradlew flywayInfo

# Datenbank zurücksetzen (Vorsicht!)
./gradlew flywayClean
```

## Entwicklungsworkflows

### Feature-Entwicklung

#### 1. Feature Branch erstellen
```bash
# Neuen Feature Branch erstellen
git checkout -b feature/neue-funktion

# Branch auf Remote pushen
git push -u origin feature/neue-funktion
```

#### 2. Code-Änderungen
```bash
# Änderungen committen
git add .
git commit -m "feat: neue Funktion implementiert"

# Code-Style prüfen
./gradlew ktlintCheck

# Code formatieren
./gradlew ktlintFormat
```

#### 3. Tests und Qualitätssicherung
```bash
# Tests ausführen
./gradlew test

# Code-Coverage prüfen
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html

# Statische Code-Analyse
./gradlew detekt
```

#### 4. Pull Request erstellen
1. Änderungen auf Remote Branch pushen
2. Pull Request im Repository erstellen
3. Code Review abwarten
4. Nach Approval mergen

### Debugging

#### 1. Service-Debugging
```bash
# Service mit Debug-Port starten
./gradlew :members:members-service:bootRun --debug-jvm

# Oder mit spezifischem Port
./gradlew :members:members-service:bootRun -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

#### 2. IntelliJ Remote Debugging
1. Run → Edit Configurations
2. "+" → Remote JVM Debug
3. Port: 5005 (oder gewählter Port)
4. Debug-Session starten

#### 3. Logs analysieren
```bash
# Service-Logs anzeigen
docker-compose logs -f members-service

# Alle Logs
docker-compose logs -f

# Spezifische Log-Level
export LOGGING_LEVEL_ROOT=DEBUG
./gradlew :members:members-service:bootRun
```

### API-Testing

#### 1. Swagger UI verwenden
```bash
# Service starten
./gradlew :members:members-service:bootRun

# Swagger UI öffnen
open http://localhost:8082/swagger-ui.html
```

#### 2. cURL-Beispiele
```bash
# Alle Mitglieder abrufen
curl -H "Authorization: Bearer <token>" \
     http://localhost:8082/api/members

# Neues Mitglied erstellen
curl -X POST \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d '{"firstName":"Max","lastName":"Mustermann","email":"max@example.com","membershipNumber":"M2024001","membershipStartDate":"2024-01-01"}' \
     http://localhost:8082/api/members
```

#### 3. Postman Collections
```bash
# Postman Collections importieren
# Dateien in docs/postman/ verwenden
```

## Häufige Probleme und Lösungen

### Docker-Probleme

#### Services starten nicht
```bash
# Ports prüfen
netstat -tulpn | grep :5432

# Docker-Logs prüfen
docker-compose logs postgres

# Services neu starten
docker-compose down
docker-compose up -d
```

#### Speicherplatz-Probleme
```bash
# Nicht verwendete Images entfernen
docker system prune -a

# Volumes aufräumen
docker volume prune
```

### Build-Probleme

#### Gradle-Cache-Probleme
```bash
# Gradle-Cache löschen
./gradlew clean
rm -rf ~/.gradle/caches

# Dependencies neu laden
./gradlew build --refresh-dependencies
```

#### Kotlin-Compiler-Probleme
```bash
# Kotlin-Daemon stoppen
./gradlew --stop

# Build-Verzeichnis löschen
./gradlew clean

# Neu kompilieren
./gradlew build
```

### Datenbank-Probleme

#### Verbindungsfehler
```bash
# PostgreSQL-Status prüfen
docker-compose ps postgres

# Datenbank-Logs prüfen
docker-compose logs postgres

# Verbindung testen
psql -h localhost -p 5432 -U meldestelle -d meldestelle
```

#### Migration-Fehler
```bash
# Migration-Status prüfen
./gradlew flywayInfo

# Fehlgeschlagene Migration reparieren
./gradlew flywayRepair

# Datenbank zurücksetzen (Entwicklung)
docker-compose down -v
docker-compose up -d postgres
./gradlew flywayMigrate
```

## Nützliche Befehle

### Gradle-Tasks
```bash
# Alle verfügbaren Tasks anzeigen
./gradlew tasks

# Abhängigkeiten anzeigen
./gradlew dependencies

# Projekt-Informationen
./gradlew projects

# Build-Scan erstellen
./gradlew build --scan
```

### Docker-Befehle
```bash
# Container-Status
docker-compose ps

# Logs verfolgen
docker-compose logs -f [service-name]

# Container neu starten
docker-compose restart [service-name]

# In Container einloggen
docker-compose exec postgres psql -U meldestelle
```

### Git-Workflows
```bash
# Aktuellen Status prüfen
git status

# Änderungen stagen
git add .

# Commit mit konventioneller Nachricht
git commit -m "feat(members): neue Validierung hinzugefügt"

# Branch wechseln
git checkout main
git pull origin main
```

## Weiterführende Ressourcen

### Dokumentation
- [API-Dokumentation](../api/README.md)
- [Architektur-Dokumentation](../architecture/)
- [Deployment-Anleitung](../README-PRODUCTION.md)

### Externe Ressourcen
- [Kotlin-Dokumentation](https://kotlinlang.org/docs/)
- [Spring Boot-Dokumentation](https://spring.io/projects/spring-boot)
- [Docker-Dokumentation](https://docs.docker.com/)
- [PostgreSQL-Dokumentation](https://www.postgresql.org/docs/)

### Community und Support
- **Issue Tracker**: GitHub Issues
- **Diskussionen**: GitHub Discussions
- **Code Reviews**: Pull Requests
- **Dokumentation**: Wiki

## Nächste Schritte

Nach erfolgreichem Setup:

1. **Code-Basis erkunden**: Beginnen Sie mit dem `members`-Modul
2. **Tests ausführen**: Verstehen Sie die Test-Struktur
3. **Erste Änderung**: Implementieren Sie eine kleine Verbesserung
4. **Code Review**: Erstellen Sie einen Pull Request
5. **Dokumentation**: Erweitern Sie die Dokumentation

---

**Letzte Aktualisierung**: 25. Juli 2025
**Version**: 1.0
**Zielgruppe**: Neue Entwickler

Bei Fragen oder Problemen erstellen Sie bitte ein Issue im Repository oder wenden Sie sich an das Entwicklungsteam.
