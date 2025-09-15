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
