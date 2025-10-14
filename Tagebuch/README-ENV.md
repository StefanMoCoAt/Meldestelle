# Umgebungsvariablen Setup - Zusammenfassung

## Was wurde implementiert

Das Meldestelle-Projekt verfügt über eine vollständig zentralisierte Umgebungsvariablen-Konfiguration im `config/` Verzeichnis.

### 1. Zentrale Konfigurationsstruktur

- **`config/.env.template`** - Master-Vorlage mit allen verfügbaren Umgebungsvariablen
- **`config/.env.dev`** - Entwicklungsumgebung-Konfiguration
- **`config/.env.prod`** - Produktionsumgebung-Konfiguration
- **`config/.env.staging`** - Staging-Umgebung-Konfiguration
- **`config/.env.test`** - Testumgebung-Konfiguration
- **`config/README.md`** - Umfassende Dokumentation der Konfigurationsverwaltung

### 2. Aktualisierte Dateien

- **`docker-compose.yml`** - Alle Services verwenden Umgebungsvariablen mit Fallback-Werten
- **Symlink `.env`** - Verweist auf die aktuelle Umgebungskonfiguration

### 3. Konfigurierte Services

Die folgenden Services sind vollständig konfiguriert:

- **PostgreSQL** - Datenbank mit konfigurierbaren Zugangsdaten
- **Redis** - Event Store und Cache mit separaten Konfigurationen
- **Keycloak** - Authentifizierung mit konfigurierbaren Admin-Zugangsdaten
- **Kafka/Zookeeper** - Messaging-System mit konfigurierbaren Parametern
- **Grafana** - Monitoring mit konfigurierbaren Admin-Zugangsdaten
- **Prometheus** - Metriken-Sammlung
- **Zipkin** - Distributed Tracing

### 4. Umgebungsvariablen-Kategorien

- **Anwendungskonfiguration** (API_HOST, API_PORT, etc.)
- **Datenbank-Konfiguration** (DB_HOST, DB_PORT, DB_USER, etc.)
- **Redis-Konfiguration** (Event Store und Cache)
- **Sicherheitskonfiguration** (JWT_SECRET, API_KEY, etc.)
- **Keycloak-Konfiguration** (Admin-Zugangsdaten, DB-Verbindung)
- **Service Discovery** (Consul-Konfiguration)
- **Messaging** (Kafka/Zookeeper-Konfiguration)
- **Monitoring** (Grafana, Prometheus-Konfiguration)
- **Logging-Konfiguration** (Log-Level, Request/Response-Logging)
- **CORS und Rate Limiting**

## Verwendung

### Schnellstart

1. **Umgebung wählen:**
   ```bash
   # Für Entwicklung
   ln -sf config/.env.dev .env

   # Für Produktion
   ln -sf config/.env.prod .env

   # Für Tests
   ln -sf config/.env.test .env
   ```

2. **Services starten:**
   ```bash
   docker compose up -d
   ```

3. **Services überprüfen:**
   ```bash
   docker compose ps
   ```

### Anpassungen

- Kopieren und bearbeiten Sie die gewünschte `.env.*` Datei aus dem `config/` Verzeichnis
- Verwenden Sie verschiedene Ports für mehrere Entwickler (siehe `.env.test` für Beispiel)
- Ändern Sie alle `CHANGE_ME` Werte in Produktionsumgebungen

### Dokumentation

Vollständige Dokumentation finden Sie in:
- `config/README.md` - Zentrale Konfigurationsdokumentation

## Sicherheitshinweise

⚠️ **Wichtig:**
- Niemals Produktionsgeheimnisse in die Versionskontrolle einbinden
- JWT_SECRET in der Produktion ändern
- Starke Passwörter für Produktionsumgebungen verwenden
- API-Schlüssel regelmäßig rotieren

## Fehlerbehebung

Bei Problemen:
1. Überprüfen Sie die aktive Umgebungskonfiguration: `ls -la .env`
2. Validieren Sie die Docker-Compose-Konfiguration: `docker compose config`
3. Überprüfen Sie die Service-Logs: `docker compose logs -f`
4. Konsultieren Sie `config/README.md` für detaillierte Konfigurationsrichtlinien

### Keycloak startet neu (Restart-Loop) oder beendet sich mit Code 1
Das Problem tritt häufig auf, wenn das Keycloak-DB-Schema fehlt oder nicht zur aktuell gesetzten `KC_DB_SCHEMA` passt.

So gehen Sie vor:

- Logs erfassen (bitte im Fehlerfall mitschicken):
  - Keycloak: `docker compose logs -f keycloak`
  - Postgres: `docker compose logs -f postgres`

- Schema-Status prüfen und ggf. manuell anlegen (nur wenn das Volume bereits existierte, als die Init-Skripte eingeführt wurden):
  1. In die Datenbank einloggen:
     ```bash
     docker exec -it meldestelle-postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"
     ```
  2. Folgende Befehle ausführen (ersetzen Sie den Benutzer bei Bedarf):
     ```sql
     CREATE SCHEMA IF NOT EXISTS keycloak;
     GRANT ALL PRIVILEGES ON SCHEMA keycloak TO "$POSTGRES_USER";
     GRANT USAGE ON SCHEMA keycloak TO "$POSTGRES_USER";
     ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak GRANT ALL ON TABLES TO "$POSTGRES_USER";
     ALTER DEFAULT PRIVILEGES IN SCHEMA keycloak GRANT ALL ON SEQUENCES TO "$POSTGRES_USER";
     ```

- Alternativ: Volumes zurücksetzen (Achtung: Datenverlust in Postgres und Keycloak-Volume!)
  ```bash
  docker compose down -v
  docker compose up -d postgres keycloak
  ```
  Hinweis: Bei frischen Volumes legt Postgres via `docker/services/postgres/01-init-keycloak-schema.sql` das Schema automatisch an. Die Datei `02-init-keycloak-schema.sql` ist absichtlich ein No-Op, um Doppel-Initialisierungen zu vermeiden.

- Konfiguration prüfen:
  - `KC_DB_SCHEMA` ist in `docker-compose.yml` parametrisiert und standardmäßig auf `keycloak` gesetzt. Sie können es in Ihrer `.env`-Datei überschreiben.
  - In Staging/Prod muss `KC_DB_URL`, `KC_DB_USERNAME`, `KC_DB_PASSWORD` auf die jeweilige DB/Benutzer zeigen (siehe `config/.env.staging`, `config/.env.prod`).

### Postgres Healthcheck schlägt fehl
Der Healthcheck ist jetzt vollständig über Umgebungsvariablen parametrisiert und passt sich Dev/Staging/Prod automatisch an:
```yaml
healthcheck:
  test: [ "CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-meldestelle} -d ${POSTGRES_DB:-meldestelle}" ]
```
Stellen Sie sicher, dass `POSTGRES_USER` und `POSTGRES_DB` korrekt gesetzt sind.

### Compose-Warnung "The BUILD_DATE variable is not set"
Die Warnung ist in `docker-compose.yml` behoben. Für Build-Argumente wird nun ein Fallback verwendet:
```yaml
BUILD_DATE: ${BUILD_DATE:-unknown}
```
Wenn Sie ein Datum setzen möchten, fügen Sie `BUILD_DATE=2025-10-05T16:55:00Z` Ihrer `.env` hinzu.

### Logging/Health Optimierungen (optional)
- Aktuell ist `KC_LOG_CONSOLE_FORMAT` auf `plain` gesetzt, um Standard-Logs auszugeben. Für strukturierte Logs können Sie `KC_LOG_CONSOLE_FORMAT=json` setzen.
- `KC_HEALTH_ENABLED=true` und ein großzügiges `start_period` (180s) sind aktiv, um Realm-Importe abwarten zu können.

## Nächste Schritte

- Die zentrale Konfiguration ist bereits vollständig implementiert
- Wählen Sie die gewünschte Umgebung mit den Symlink-Befehlen oben
- Passen Sie Konfigurationswerte in den `config/.env.*` Dateien nach Bedarf an
- Für neue Umgebungen verwenden Sie `config/.env.template` als Ausgangspunkt


---

## Smoke-Tests (Prometheus & Zipkin)

Nach dem Start der Infrastruktur können einfache Smoke-Tests ausgeführt werden:

```bash
# Zipkin: erzeugt einen Ping über das Gateway und prüft, ob Traces ankommen
bash scripts/smoke/zipkin_smoke.sh

# Prometheus: prüft, ob Gateway und Ping-Service Metriken exponieren
bash scripts/smoke/prometheus_smoke.sh
```

Variablen:
- GATEWAY_URL (Default: http://localhost:8081)
- ZIPKIN_URL (Default: http://localhost:9411)
- PING_SERVICE_URL (Default: http://localhost:8082)


## Keycloak Healthcheck

- Der Keycloak-Container verwendet nun einen robusten Healthcheck, der nicht von curl abhängt.
- Ablauf: Zuerst wird curl verwendet, falls vorhanden; alternativ wget; fehlt beides, wird ein Bash-/dev/tcp-Fallback genutzt. In diesem Fall wird eine klare Fehlermeldung in den Healthcheck-Logs ausgegeben.
- Zeitparameter: interval 15s, timeout 30s, retries 10, start_period 180s – ausreichend, um längere Realm-Imports (30+ Sekunden) abzuwarten.
- Beispiel (vereinfacht):
  - test: CMD-SHELL
  - if curl vorhanden → GET /health/ready prüfen; sonst wget; sonst Bash /dev/tcp mit HTTP-Status „200 OK“ prüfen.
