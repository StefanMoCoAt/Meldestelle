# Konfigurationsmanagement

Dieses Dokument beschreibt, wie die Konfiguration des Meldestelle-Projekts verwaltet wird.

## Übersicht

Das Projekt verwendet einen mehrschichtigen Konfigurationsansatz, um verschiedene Umgebungen (Entwicklung, Test, Staging, Produktion) zu unterstützen. Die Konfiguration kann über folgende Quellen bereitgestellt werden:

1. Umgebungsvariablen (höchste Priorität)
2. Umgebungsspezifische Konfigurationsdateien (.properties)
3. Basis-Konfigurationsdatei (application.properties)
4. Standardwerte im Code (niedrigste Priorität)

## Konfigurationsquellen

### Umgebungsvariablen

Umgebungsvariablen haben die höchste Priorität und überschreiben alle anderen Konfigurationen. Sie werden typischerweise verwendet, um sensible Informationen wie Passwörter oder umgebungsspezifische Werte zu setzen.

Beispiel:
```bash
# Umgebung festlegen
export APP_ENV=PRODUCTION

# Datenbank-Konfiguration
export DB_HOST=db.example.com
export DB_PORT=5432
export DB_NAME=meldestelle_db
export DB_USER=db_user
export DB_PASSWORD=secret_password

# Server-Konfiguration
export API_PORT=8081
```

### Konfigurationsdateien

Das Projekt verwendet .properties-Dateien im `/config`-Verzeichnis. Die folgenden Dateien werden geladen (in dieser Reihenfolge):

1. `application.properties` - Basiseinstellungen für alle Umgebungen
2. Umgebungsspezifische Datei - abhängig von `APP_ENV`:
   - `application-dev.properties` - Entwicklungsumgebung (Standard)
   - `application-test.properties` - Testumgebung
   - `application-staging.properties` - Staging-Umgebung
   - `application-prod.properties` - Produktionsumgebung

## Umgebungen

Das Projekt unterstützt folgende Umgebungen:

| Umgebung | Beschreibung | Typische Verwendung |
|----------|-------------|--------------------|
| DEVELOPMENT | Lokale Entwicklungsumgebung | Lokale Entwicklung, Debug-Modus aktiv |
| TEST | Testumgebung | Automatisierte Tests, Integrationstests |
| STAGING | Vorabproduktionsumgebung | Manuelle Tests, UAT, Demos |
| PRODUCTION | Produktionsumgebung | Live-System |

Die aktuelle Umgebung wird über die Umgebungsvariable `APP_ENV` festgelegt. Wenn diese Variable nicht gesetzt ist, wird standardmäßig `DEVELOPMENT` verwendet.

## Konfigurationsstruktur

Die Konfiguration ist in mehrere Kategorien unterteilt:

### AppInfo

Allgemeine Anwendungsinformationen:

```properties
app.name=Meldestelle
app.version=1.0.0
app.description=Pferdesport Meldestelle System
```

### Server

Server-Konfiguration:

```properties
server.port=8081
server.host=0.0.0.0
server.workers=4
server.cors.enabled=true
server.cors.allowedOrigins=*
```

### Datenbank

Datenbank-Konfiguration:

```properties
database.host=localhost
database.port=5432
database.name=meldestelle_db
database.username=meldestelle_user
database.password=secure_password_change_me
database.maxPoolSize=10
database.autoMigrate=true
```

### Sicherheit

Sicherheitseinstellungen (JWT, etc.):

```properties
security.jwt.secret=your-secret-key
security.jwt.issuer=meldestelle-api
security.jwt.audience=meldestelle-clients
security.jwt.realm=meldestelle
security.jwt.expirationInMinutes=1440
```

### Logging

Logging-Konfiguration:

```properties
logging.level=INFO
logging.requests=true
logging.responses=false
```

## Verwendung im Code

Die Konfiguration wird über die zentrale `AppConfig`-Klasse bereitgestellt:

```kotlin
import at.mocode.shared.config.AppConfig

// Verwendung der Konfiguration
fun example() {
    // Umgebung prüfen
    if (AppConfig.environment.isDevelopment()) {
        println("Debug-Modus aktiv")
    }

    // Server-Port abrufen
    val port = AppConfig.server.port

    // Datenbank-Konfiguration
    val dbConfig = AppConfig.database

    // JWT-Secret
    val jwtSecret = AppConfig.security.jwt.secret
}
```

## Konfiguration für Docker

Bei Verwendung von Docker werden Umgebungsvariablen in der `.env`-Datei und im `docker-compose.yml` definiert:

```yaml
services:
  server:
    environment:
      - APP_ENV=PRODUCTION
      - DB_HOST=db
      - DB_PORT=5432
      - DB_NAME=${POSTGRES_DB}
      - DB_USER=${POSTGRES_USER}
      - DB_PASSWORD=${POSTGRES_PASSWORD}
```

## Beste Praktiken

1. **Sensible Daten**: Speichern Sie niemals sensible Daten wie Passwörter oder API-Schlüssel direkt in Konfigurationsdateien, die in die Versionskontrolle eingecheckt werden. Verwenden Sie stattdessen Umgebungsvariablen.

2. **Umgebungsspezifische Konfiguration**: Verwenden Sie umgebungsspezifische Konfigurationsdateien nur für Werte, die sich zwischen den Umgebungen unterscheiden.

3. **Standardwerte**: Geben Sie für alle Konfigurationsparameter sinnvolle Standardwerte an, damit die Anwendung auch funktioniert, wenn nicht alle Konfigurationen explizit gesetzt sind.

4. **Validierung**: Validieren Sie kritische Konfigurationen beim Anwendungsstart, um Fehler frühzeitig zu erkennen.

5. **Dokumentation**: Halten Sie die Dokumentation der Konfigurationsparameter aktuell, damit neue Teammitglieder die Anwendung leicht konfigurieren können.
