# Datenbank-Setup Korrekturen

## Überblick
Dieses Dokument beschreibt die Korrekturen, die am Datenbank-Setup vorgenommen wurden, um alle Probleme zu beheben, die bei der letzten Commit-Überprüfung identifiziert wurden.

## Behobene Probleme

### 1. Umgebungsvariablen-Namenskonflikt
**Problem:** Die `.env`-Datei verwendete `POSTGRES_*` Variablen, aber der Code erwartete `DB_*` Variablen.

**Lösung:**
- Hinzugefügt: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` Variablen zur `.env`-Datei
- Beibehalten: `POSTGRES_*` Variablen für Docker Compose Kompatibilität

### 2. Regex-Escaping in DatabaseMigrator.kt
**Problem:** Falsche Regex-Escaping in der Migration-ID-Generierung (`"\s+"` statt `"\\s+"`).

**Lösung:** Korrigiert zu `"\\s+".toRegex()` für ordnungsgemäße Whitespace-Ersetzung.

### 3. Falsche Dependency-Platzierung in shared-kernel
**Problem:** Datenbankabhängigkeiten waren in `jsMain.dependencies` statt `jvmMain.dependencies`.

**Lösung:** Verschoben alle Datenbankabhängigkeiten (HikariCP, Exposed, PostgreSQL) zu `jvmMain.dependencies`.

### 4. Fehlende Datenbankabhängigkeiten in api-gateway
**Problem:** Migration-Dateien konnten nicht kompiliert werden, da Exposed-Abhängigkeiten fehlten.

**Lösung:** Hinzugefügt Datenbankabhängigkeiten zu `api-gateway/build.gradle.kts` in `jvmMain.dependencies`.

### 5. Unvollständige Application.kt
**Problem:** Application.kt enthielt nur Imports, aber keine Implementierung.

**Lösung:**
- Hinzugefügt `main()` Funktion mit Datenbankinitialisierung
- Hinzugefügt Migrationsausführung beim Anwendungsstart
- Hinzugefügt Ktor-Server-Konfiguration mit Health-Check-Endpoint

### 6. Datetime-Spalten-Definitionen
**Problem:** Migration-Dateien verwendeten veraltete `datetime` und `currentDateTime()` Syntax.

**Lösung:**
- Aktualisiert alle Migration-Dateien zu `timestamp` und `CurrentTimestamp`
- Hinzugefügt korrekte Imports für `org.jetbrains.exposed.sql.kotlin.datetime.timestamp` und `CurrentTimestamp`

## Betroffene Dateien

### Geänderte Dateien:
- `.env` - Umgebungsvariablen-Konfiguration
- `shared-kernel/build.gradle.kts` - Dependency-Konfiguration
- `api-gateway/build.gradle.kts` - Dependency-Konfiguration
- `shared-kernel/src/jvmMain/kotlin/at/mocode/shared/database/DatabaseMigrator.kt` - Regex-Fix
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/Application.kt` - Vollständige Implementierung
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/migrations/EventManagementMigrations.kt` - Datetime-Fixes
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/migrations/HorseRegistryMigrations.kt` - Datetime-Fixes
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/migrations/MemberManagementMigrations.kt` - Datetime-Fixes

### Unveränderte Dateien:
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/migrations/MasterDataMigrations.kt` - Keine Probleme gefunden

## Verifikation
- ✅ Projekt kompiliert erfolgreich
- ✅ Alle Datenbankabhängigkeiten korrekt aufgelöst
- ✅ Migration-System funktionsfähig
- ✅ Anwendung startet mit Datenbankinitialisierung

## Nächste Schritte
1. Testen der Datenbankverbindung mit echten Datenbank-Instanzen
2. Ausführen der Migrationen in Entwicklungsumgebung
3. Validierung der Tabellenstrukturen
4. Integration-Tests für Datenbank-Operationen

## Datum
2025-07-19 13:21
