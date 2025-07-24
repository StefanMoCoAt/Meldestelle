# Umgebungsvariablen für die Entwicklung

## Übersicht

Dieses Dokument beschreibt alle erforderlichen Umgebungsvariablen für die lokale Entwicklung der Meldestelle-Anwendung. Die Variablen sind in der `.env`-Datei im Projektverzeichnis definiert und werden automatisch von Docker Compose geladen.

## Setup

1. **Kopieren Sie die .env-Datei:**
   ```bash
   # Die .env-Datei ist bereits im Projektverzeichnis vorhanden
   # Passen Sie die Werte nach Bedarf für Ihre lokale Umgebung an
   ```

2. **Starten Sie die Services:**
   ```bash
   docker-compose up -d
   ```

3. **Überprüfen Sie die Konfiguration:**
   ```bash
   # Überprüfen Sie, ob alle Services laufen
   docker-compose ps

   # Überprüfen Sie die Logs
   docker-compose logs -f
   ```

## Umgebungsvariablen-Kategorien

### 1. Anwendungskonfiguration

| Variable | Beschreibung | Standardwert | Erforderlich |
|----------|--------------|--------------|--------------|
| `API_HOST` | Host-Adresse für den API-Server | `0.0.0.0` | Ja |
| `API_PORT` | Port für den API-Server | `8081` | Ja |
| `APP_NAME` | Name der Anwendung | `Meldestelle` | Nein |
| `APP_VERSION` | Version der Anwendung | `1.0.0` | Nein |
| `APP_DESCRIPTION` | Beschreibung der Anwendung | `Pferdesport Meldestelle System` | Nein |
| `APP_ENVIRONMENT` | Aktuelle Umgebung | `development` | Ja |

### 2. Datenbank-Konfiguration (PostgreSQL)

| Variable | Beschreibung | Standardwert | Erforderlich |
|----------|--------------|--------------|--------------|
| `DB_HOST` | PostgreSQL Host | `localhost` | Ja |
| `DB_PORT` | PostgreSQL Port | `5432` | Ja |
| `DB_NAME` | Datenbankname | `meldestelle` | Ja |
| `DB_USER` | Datenbankbenutzer | `meldestelle` | Ja |
| `DB_PASSWORD` | Datenbankpasswort | `meldestelle` | Ja |
| `DB_MAX_POOL_SIZE` | Maximale Anzahl Verbindungen im Pool | `10` | Nein |
| `DB_MIN_POOL_SIZE` | Minimale Anzahl Verbindungen im Pool | `5` | Nein |
| `DB_AUTO_MIGRATE` | Automatische Datenbankmigrationen | `true` | Nein |

**Docker-spezifische Variablen:**
- `POSTGRES_USER`: PostgreSQL-Container Benutzer
- `POSTGRES_PASSWORD`: PostgreSQL-Container Passwort
- `POSTGRES_DB`: PostgreSQL-Container Datenbankname

### 3. Redis-Konfiguration

#### Event Store
| Variable | Beschreibung | Standardwert | Erforderlich |
|----------|--------------|--------------|--------------|
| `REDIS_EVENT_STORE_HOST` | Redis Host für Event Store | `localhost` | Ja |
| `REDIS_EVENT_STORE_PORT` | Redis Port für Event Store | `6379` | Ja |
| `REDIS_EVENT_STORE_PASSWORD` | Redis Passwort | *(leer)* | Nein |
| `REDIS_EVENT_STORE_DATABASE` | Redis Datenbank-Index | `0` | Nein |
| `REDIS_EVENT_STORE_CONNECTION_TIMEOUT` | Verbindungs-Timeout (ms) | `2000` | Nein |
| `REDIS_EVENT_STORE_READ_TIMEOUT` | Lese-Timeout (ms) | `2000` | Nein |
| `REDIS_EVENT_STORE_USE_POOLING` | Verbindungs-Pooling aktivieren | `true` | Nein |
| `REDIS_EVENT_STORE_MAX_POOL_SIZE` | Maximale Pool-Größe | `8` | Nein |
| `REDIS_EVENT_STORE_MIN_POOL_SIZE` | Minimale Pool-Größe | `2` | Nein |

#### Cache
| Variable | Beschreibung | Standardwert | Erforderlich |
|----------|--------------|--------------|--------------|
| `REDIS_CACHE_HOST` | Redis Host für Cache | `localhost` | Ja |
| `REDIS_CACHE_PORT` | Redis Port für Cache | `6379` | Ja |
| `REDIS_CACHE_PASSWORD` | Redis Passwort für Cache | *(leer)* | Nein |
| `REDIS_CACHE_DATABASE` | Redis Datenbank-Index für Cache | `1` | Nein |

### 4. Sicherheitskonfiguration

| Variable | Beschreibung | Standardwert | Erforderlich |
|----------|--------------|--------------|--------------|
| `JWT_SECRET` | JWT-Signatur-Schlüssel | `meldestelle-jwt-secret-key-for-development-change-in-production` | Ja |
| `JWT_ISSUER` | JWT-Aussteller | `meldestelle-api` | Ja |
| `JWT_AUDIENCE` | JWT-Zielgruppe | `meldestelle-clients` | Ja |
| `JWT_REALM` | JWT-Realm | `meldestelle` | Ja |
| `API_KEY` | API-Schlüssel für interne Services | `meldestelle-api-key-for-development` | Ja |

### 5. Keycloak-Konfiguration

| Variable | Beschreibung | Standardwert | Erforderlich |
|----------|--------------|--------------|--------------|
| `KEYCLOAK_ADMIN` | Keycloak Admin-Benutzer | `admin` | Ja |
| `KEYCLOAK_ADMIN_PASSWORD` | Keycloak Admin-Passwort | `admin` | Ja |
| `KC_DB` | Keycloak Datenbanktyp | `postgres` | Ja |
| `KC_DB_URL` | Keycloak Datenbank-URL | `jdbc:postgresql://postgres:5432/keycloak` | Ja |
| `KC_DB_USERNAME` | Keycloak Datenbankbenutzer | `meldestelle` | Ja |
| `KC_DB_PASSWORD` | Keycloak Datenbankpasswort | `meldestelle` | Ja |

### 6. Service Discovery (Consul)

| Variable | Beschreibung | Standardwert | Erforderlich |
|----------|--------------|--------------|--------------|
| `CONSUL_HOST` | Consul Host | `consul` | Ja |
| `CONSUL_PORT` | Consul Port | `8500` | Ja |
| `SERVICE_DISCOVERY_ENABLED` | Service Discovery aktivieren | `true` | Nein |
| `SERVICE_DISCOVERY_REGISTER_SERVICES` | Services registrieren | `true` | Nein |
| `SERVICE_DISCOVERY_HEALTH_CHECK_PATH` | Health Check Pfad | `/health` | Nein |
| `SERVICE_DISCOVERY_HEALTH_CHECK_INTERVAL` | Health Check Intervall (s) | `10` | Nein |

### 7. Messaging (Kafka)

| Variable | Beschreibung | Standardwert | Erforderlich |
|----------|--------------|--------------|--------------|
| `ZOOKEEPER_CLIENT_PORT` | Zookeeper Client Port | `2181` | Ja |
| `KAFKA_BROKER_ID` | Kafka Broker ID | `1` | Ja |
| `KAFKA_ZOOKEEPER_CONNECT` | Zookeeper Verbindung | `zookeeper:2181` | Ja |
| `KAFKA_ADVERTISED_LISTENERS` | Kafka Listener | `PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092` | Ja |
| `KAFKA_LISTENER_SECURITY_PROTOCOL_MAP` | Security Protocol Map | `PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT` | Ja |
| `KAFKA_INTER_BROKER_LISTENER_NAME` | Inter-Broker Listener | `PLAINTEXT` | Ja |
| `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR` | Replikationsfaktor | `1` | Ja |

### 8. Monitoring

| Variable | Beschreibung | Standardwert | Erforderlich |
|----------|--------------|--------------|--------------|
| `GF_SECURITY_ADMIN_USER` | Grafana Admin-Benutzer | `admin` | Ja |
| `GF_SECURITY_ADMIN_PASSWORD` | Grafana Admin-Passwort | `admin` | Ja |
| `GF_USERS_ALLOW_SIGN_UP` | Grafana Benutzerregistrierung | `false` | Nein |
| `METRICS_AUTH_USERNAME` | Metrics-Endpunkt Benutzer | `admin` | Ja |
| `METRICS_AUTH_PASSWORD` | Metrics-Endpunkt Passwort | `metrics` | Ja |

### 9. Logging-Konfiguration

| Variable | Beschreibung | Standardwert | Erforderlich |
|----------|--------------|--------------|--------------|
| `LOGGING_LEVEL` | Log-Level | `DEBUG` | Nein |
| `LOGGING_REQUESTS` | Request-Logging aktivieren | `true` | Nein |
| `LOGGING_RESPONSES` | Response-Logging aktivieren | `true` | Nein |
| `LOGGING_REQUEST_HEADERS` | Request-Header loggen | `true` | Nein |
| `LOGGING_REQUEST_BODY` | Request-Body loggen | `true` | Nein |
| `LOGGING_RESPONSE_HEADERS` | Response-Header loggen | `true` | Nein |
| `LOGGING_RESPONSE_BODY` | Response-Body loggen | `true` | Nein |
| `LOGGING_STRUCTURED` | Strukturiertes Logging | `true` | Nein |
| `LOGGING_CORRELATION_ID` | Korrelations-ID einschließen | `true` | Nein |
| `LOGGING_REQUEST_ID_HEADER` | Request-ID Header Name | `X-Request-ID` | Nein |

### 10. CORS und Rate Limiting

| Variable | Beschreibung | Standardwert | Erforderlich |
|----------|--------------|--------------|--------------|
| `SERVER_CORS_ENABLED` | CORS aktivieren | `true` | Nein |
| `SERVER_CORS_ALLOWED_ORIGINS` | Erlaubte CORS-Origins | `*` | Nein |
| `RATELIMIT_ENABLED` | Rate Limiting aktivieren | `true` | Nein |
| `RATELIMIT_GLOBAL_LIMIT` | Globales Rate Limit | `100` | Nein |
| `RATELIMIT_GLOBAL_PERIOD_MINUTES` | Rate Limit Zeitraum (min) | `1` | Nein |
| `RATELIMIT_INCLUDE_HEADERS` | Rate Limit Header einschließen | `true` | Nein |

## Entwicklungsumgebung-spezifische Einstellungen

### Debug-Modus
```bash
DEBUG_MODE=true
DEV_HOT_RELOAD=true
```

### Verschiedene Ports für mehrere Entwickler
Wenn mehrere Entwickler gleichzeitig arbeiten, können Sie die Ports anpassen:

```bash
# Entwickler 1 (Standard)
API_PORT=8081
POSTGRES_EXTERNAL_PORT=5432
REDIS_EXTERNAL_PORT=6379

# Entwickler 2
API_PORT=8082
POSTGRES_EXTERNAL_PORT=5433
REDIS_EXTERNAL_PORT=6380
```

## Sicherheitshinweise

⚠️ **Wichtige Sicherheitshinweise:**

1. **Niemals Produktionsgeheimnisse in die Versionskontrolle einbinden**
2. **JWT_SECRET in der Produktion ändern**
3. **Starke Passwörter für Produktionsumgebungen verwenden**
4. **API-Schlüssel regelmäßig rotieren**
5. **Datenbankzugangsdaten sicher aufbewahren**

## Fehlerbehebung

### Häufige Probleme

1. **Verbindungsfehler zu PostgreSQL:**
   - Überprüfen Sie `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`
   - Stellen Sie sicher, dass der PostgreSQL-Container läuft

2. **Redis-Verbindungsfehler:**
   - Überprüfen Sie `REDIS_EVENT_STORE_HOST` und `REDIS_EVENT_STORE_PORT`
   - Stellen Sie sicher, dass der Redis-Container läuft

3. **JWT-Authentifizierungsfehler:**
   - Überprüfen Sie `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE`
   - Stellen Sie sicher, dass die Werte konsistent sind

4. **Port-Konflikte:**
   - Ändern Sie die Port-Variablen, wenn andere Services die gleichen Ports verwenden

### Logs überprüfen

```bash
# Alle Service-Logs anzeigen
docker-compose logs -f

# Spezifische Service-Logs
docker-compose logs -f postgres
docker-compose logs -f redis
docker-compose logs -f keycloak
```

### Konfiguration validieren

```bash
# Docker Compose Konfiguration validieren
docker-compose config

# Umgebungsvariablen anzeigen
docker-compose config --services
```

## Weitere Ressourcen

- [Docker Compose Dokumentation](https://docs.docker.com/compose/)
- [PostgreSQL Konfiguration](https://www.postgresql.org/docs/)
- [Redis Konfiguration](https://redis.io/documentation)
- [Keycloak Dokumentation](https://www.keycloak.org/documentation)
- [Kafka Dokumentation](https://kafka.apache.org/documentation/)
