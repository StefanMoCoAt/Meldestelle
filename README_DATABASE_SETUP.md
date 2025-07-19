# Datenbank-Setup

Dieses Dokument beschreibt, wie die Datenbank für das Meldestelle-Projekt eingerichtet und verwaltet wird.

## Überblick

Das Projekt verwendet PostgreSQL als Datenbank und Exposed als ORM-Framework. Die Datenbankmigrationen werden mit einem eigenem, auf Exposed basierenden Migrationssystem verwaltet.

## Konfiguration

Die Datenbankkonfiguration erfolgt über Umgebungsvariablen. Diese können entweder direkt im Betriebssystem gesetzt oder über eine `.env`-Datei bei Verwendung von Docker Compose bereitgestellt werden.

### Erforderliche Umgebungsvariablen

- `DB_HOST`: Hostname der Datenbank (Standard: `localhost`)
- `DB_PORT`: Port der Datenbank (Standard: `5432`)
- `DB_NAME`: Name der Datenbank (Standard: `meldestelle_db`)
- `DB_USER`: Benutzername für die Datenbank (Standard: `meldestelle_user`)
- `DB_PASSWORD`: Passwort für den Datenbankbenutzer

### .env-Datei

Für die lokale Entwicklung und Docker Compose wird eine `.env`-Datei im Projektwurzelverzeichnis verwendet. Ein Beispiel:

```
# Datenbank-Konfiguration
POSTGRES_USER=meldestelle_user
POSTGRES_PASSWORD=secure_password_change_me
POSTGRES_DB=meldestelle_db

# API Gateway Konfiguration
API_PORT=8081
```

## Datenbankmigrationen

Das Projekt verwendet ein eigenes, auf Exposed basierendes Migrationssystem. Jede Migration ist eine Klasse, die von `Migration` erbt und eine eindeutige Versionsnummer und Beschreibung hat.

### Migrations-Struktur

Migrationen werden in den entsprechenden Modulen definiert und im API-Gateway zentral registriert und ausgeführt.

### Hinzufügen einer neuen Migration

1. Erstellen Sie eine neue Klasse, die von `Migration` erbt
2. Implementieren Sie die `up()`-Methode, um die nötigen Änderungen vorzunehmen
3. Registrieren Sie die Migration in `MigrationSetup.kt`

Beispiel:

```kotlin
class MyNewMigration : Migration(5, "Add new feature tables") {
    override fun up() {
        SchemaUtils.create(MyNewTable)
    }
}
```

### Ausführen von Migrationen

Migrationen werden automatisch beim Start der Anwendung ausgeführt. Es werden nur Migrationen ausgeführt, die noch nicht in der Datenbank registriert sind.

## Datenbankstruktur

Die Datenbankstruktur ist in verschiedene Bereiche unterteilt, die den Modulen des Projekts entsprechen:

1. **Master Data** - Stammdaten wie Länder, Bundesländer, Sportarten
2. **Member Management** - Personen, Vereine, Mitgliedschaften
3. **Horse Registry** - Pferde und deren Besitzer
4. **Event Management** - Veranstaltungen und zugehörige Daten

## Entwicklungsumgebung einrichten

### Mit Docker Compose

1. Erstellen Sie eine `.env`-Datei mit den erforderlichen Umgebungsvariablen
2. Führen Sie `docker-compose up -d db` aus, um nur die Datenbank zu starten
3. Alternativ `docker-compose up -d` für das gesamte System

### Manuell

1. Installieren Sie PostgreSQL auf Ihrem System
2. Erstellen Sie eine Datenbank und einen Benutzer
3. Setzen Sie die Umgebungsvariablen oder passen Sie die Standardwerte in `DatabaseConfig.kt` an
4. Starten Sie die Anwendung

## Fehlerbehebung

### Verbindungsprobleme

- Überprüfen Sie, ob die PostgreSQL-Instanz läuft
- Überprüfen Sie die Verbindungsparameter in den Umgebungsvariablen
- Überprüfen Sie Firewalls und Netzwerkeinstellungen

### Migrationsfehler

- Prüfen Sie die Logs auf detaillierte Fehlermeldungen
- Migrationen werden nur einmal ausgeführt - Änderungen an bestehenden Migrationen haben keine Auswirkung
- Bei schwerwiegenden Problemen kann die `_migrations`-Tabelle manuell bearbeitet werden (nur für Fortgeschrittene)
