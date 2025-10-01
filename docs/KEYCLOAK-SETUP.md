# Keycloak Integration - Setup und Konfiguration

## Übersicht

Dieses Dokument beschreibt die vollständige Keycloak-Integration für das Meldestelle-System, einschließlich Authentifizierung, Konfiguration und Best Practices.

## Architektur

### Authentifizierungsansatz

Das System verwendet **Spring Security OAuth2 Resource Server** für die JWT-Validierung:

- ✅ **Empfohlener Ansatz**: Spring Security `oauth2ResourceServer`
  - Kryptographisch sichere JWT-Signaturvalidierung
  - Automatische JWK-Set-Aktualisierung
  - Standardkonform (RFC 7519, RFC 7517)
  - Integriert mit Spring Security Authorization

- ❌ **NICHT verwendet**: Custom JWT Filter
  - Frühere Implementierungen wurden entfernt
  - Hatten Sicherheitslücken (fehlende Signaturvalidierung)

### Komponenten

1. **Keycloak Server** (Port 8180 extern, 8080 intern)
   - OAuth2/OpenID Connect Provider
   - PostgreSQL Backend
   - Realm: `meldestelle`

2. **API Gateway**
   - OAuth2 Resource Server
   - JWT-Validierung via JWK-Set
   - Rollenbasierte Autorisierung

3. **PostgreSQL Database**
   - Keycloak-Schema: `keycloak`
   - Automatische Schema-Initialisierung

## Konfiguration

### Docker Compose

#### Keycloak Service

```yaml
keycloak:
  image: quay.io/keycloak/keycloak:26.0.7
  environment:
    # Admin-Zugangsdaten (IN PRODUKTION ÄNDERN!)
    KEYCLOAK_ADMIN: admin
    KEYCLOAK_ADMIN_PASSWORD: admin

    # Datenbank
    KC_DB: postgres
    KC_DB_URL: jdbc:postgresql://postgres:5432/meldestelle
    KC_DB_SCHEMA: keycloak

    # Connection Pool Optimierung
    KC_DB_POOL_INITIAL_SIZE: 5
    KC_DB_POOL_MIN_SIZE: 5
    KC_DB_POOL_MAX_SIZE: 20

    # JVM Optimierung
    JAVA_OPTS_APPEND: >-
      -XX:MaxRAMPercentage=75.0
      -XX:+UseG1GC
      -XX:+UseStringDeduplication
```

#### Produktionsmodus

Der Service läuft im **Produktionsmodus** (`start --optimized`):
- Schnellerer Start durch Pre-Build
- Optimierte Performance
- Geeignet für Produktionsumgebungen

**Wichtig**: Für Entwicklung kann auf `start-dev` umgestellt werden.

### Realm-Konfiguration

**Datei**: `docker/services/keycloak/meldestelle-realm.json`

#### Realm: `meldestelle`

- **Display Name**: Meldestelle Authentication
- **Sprachen**: Deutsch (Standard), Englisch
- **SSL**: External (hinter Reverse Proxy)

#### Sicherheitseinstellungen

- **Brute Force Protection**: Aktiviert
  - Max. 5 Fehlversuche
  - 15 Minuten Sperrzeit

- **Password Policy**:
  - Mindestens 8 Zeichen
  - Mind. 1 Ziffer, 1 Kleinbuchstabe, 1 Großbuchstabe, 1 Sonderzeichen
  - Nicht identisch mit Username

#### Token-Einstellungen

- **Access Token Lifespan**: 5 Minuten (300 Sek.)
- **SSO Session Idle**: 30 Minuten (1800 Sek.)
- **SSO Session Max**: 10 Stunden (36000 Sek.)
- **Refresh Token**: Einmalige Verwendung

### Clients

#### 1. api-gateway (Confidential Client)

```json
{
  "clientId": "api-gateway",
  "protocol": "openid-connect",
  "publicClient": false,
  "bearerOnly": false,
  "standardFlowEnabled": true,
  "directAccessGrantsEnabled": true,
  "serviceAccountsEnabled": true
}
```

**Verwendung**: Backend-Service-to-Service Kommunikation

**Secret**: Muss in Keycloak UI generiert und konfiguriert werden

#### 2. web-app (Public Client)

```json
{
  "clientId": "web-app",
  "publicClient": true,
  "standardFlowEnabled": true,
  "attributes": {
    "pkce.code.challenge.method": "S256"
  }
}
```

**Verwendung**: Frontend Single-Page Application (mit PKCE)

### Rollen

| Rolle | Beschreibung | Verwendung |
|-------|--------------|------------|
| `ADMIN` | Vollzugriff | Systemadministration |
| `USER` | Standardbenutzer | Normale Anwendungsfunktionen |
| `MONITORING` | Überwachung | Metriken und Health Checks |
| `GUEST` | Gast | Minimaler Zugriff |

### Standard-Benutzer

**Username**: `admin`
**Passwort**: `Change_Me_In_Production!` (temporär, muss beim ersten Login geändert werden)
**Rollen**: ADMIN, USER

## Spring Security Konfiguration

### Gateway SecurityConfig

**Datei**: `infrastructure/gateway/src/main/kotlin/.../SecurityConfig.kt`

```kotlin
@Bean
fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    return http
        .oauth2ResourceServer { oauth2 ->
            oauth2.jwt { jwt ->
                jwt.jwtDecoder(jwtDecoder())
            }
        }
        .authorizeExchange { exchanges ->
            exchanges
                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                .pathMatchers("/api/monitoring/**").hasAnyRole("ADMIN", "MONITORING")
                .anyExchange().authenticated()
        }
        .build()
}
```

### Application Properties

**Datei**: `application-keycloak.yml`

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/realms/meldestelle
          jwk-set-uri: http://keycloak:8080/realms/meldestelle/protocol/openid-connect/certs

gateway:
  security:
    keycloak:
      enabled: false  # Custom filter deaktiviert - oauth2ResourceServer wird verwendet
```

## Datenbank-Setup

### PostgreSQL Schema

**Datei**: `docker/services/postgres/01-init-keycloak-schema.sql`

Das Schema wird automatisch beim ersten Start von PostgreSQL erstellt:

```sql
CREATE SCHEMA IF NOT EXISTS keycloak;
GRANT ALL PRIVILEGES ON SCHEMA keycloak TO meldestelle;
```

Keycloak erstellt seine Tabellen automatisch im `keycloak` Schema.

## Produktion Deployment

### Dockerfile

**Optional**: `dockerfiles/infrastructure/keycloak/Dockerfile`

Pre-built optimiertes Image für schnelleren Start:

```dockerfile
FROM quay.io/keycloak/keycloak:26.0.7 AS builder
RUN /opt/keycloak/bin/kc.sh build --db=postgres
```

**Verwendung in docker-compose.yml**:

```yaml
keycloak:
  build:
    context: .
    dockerfile: dockerfiles/infrastructure/keycloak/Dockerfile
```

### Umgebungsvariablen für Produktion

Erstellen Sie eine `.env` Datei:

```env
# Keycloak Admin (ÄNDERN!)
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=<starkes_passwort>

# Datenbank
POSTGRES_USER=meldestelle
POSTGRES_PASSWORD=<db_passwort>
POSTGRES_DB=meldestelle

# Keycloak Konfiguration
KC_HOSTNAME_STRICT=true
KC_HOSTNAME_STRICT_HTTPS=true
KC_HTTP_ENABLED=false
KC_PROXY=edge

# JVM Memory (optional)
KC_DB_POOL_MAX_SIZE=50
```

### SSL/TLS

Für HTTPS in Produktion:

1. **Empfohlen**: Reverse Proxy (nginx, Traefik)
   ```yaml
   KC_PROXY: edge
   KC_HTTP_ENABLED: true
   KC_HOSTNAME_STRICT_HTTPS: false
   ```

2. **Direkt mit Keycloak**:
   ```yaml
   KC_HTTPS_CERTIFICATE_FILE: /path/to/cert.pem
   KC_HTTPS_CERTIFICATE_KEY_FILE: /path/to/key.pem
   KC_HTTP_ENABLED: false
   ```

## Betrieb

### Start

```bash
# Alle Services starten
docker-compose up -d

# Nur Infrastruktur (inkl. Keycloak)
docker-compose up -d postgres redis keycloak consul
```

### Keycloak Admin Console

**URL**: http://localhost:8180
**Username**: admin
**Passwort**: admin (Standard)

### Health Checks

```bash
# Keycloak Readiness
curl http://localhost:8180/health/ready

# Keycloak Liveness
curl http://localhost:8180/health/live

# Metrics
curl http://localhost:8180/metrics
```

### Logs

```bash
# Keycloak Logs anzeigen
docker-compose logs -f keycloak

# Letzte 100 Zeilen
docker-compose logs --tail=100 keycloak
```

## Troubleshooting

### Problem: Keycloak startet nicht

**Symptom**: Container stoppt sofort nach Start

**Lösung**:
1. Prüfen Sie PostgreSQL Verbindung:
   ```bash
   docker-compose logs postgres
   ```

2. Schema-Berechtigungen prüfen:
   ```sql
   \dn+ keycloak
   ```

3. Keycloak Logs prüfen:
   ```bash
   docker-compose logs keycloak
   ```

### Problem: JWT Validierung schlägt fehl

**Symptom**: 401 Unauthorized trotz gültigem Token

**Lösung**:
1. Issuer URI prüfen:
   ```bash
   curl http://keycloak:8080/realms/meldestelle/.well-known/openid-configuration
   ```

2. JWK-Set prüfen:
   ```bash
   curl http://keycloak:8080/realms/meldestelle/protocol/openid-connect/certs
   ```

3. Gateway-Logs prüfen:
   ```bash
   docker-compose logs api-gateway | grep -i jwt
   ```

### Problem: Realm nicht importiert

**Symptom**: Realm `meldestelle` existiert nicht

**Lösung**:
1. Prüfen Sie Volume-Mount:
   ```bash
   docker-compose config | grep -A 5 "keycloak:" | grep volumes
   ```

2. Realm-Datei prüfen:
   ```bash
   ls -la docker/services/keycloak/
   ```

3. Container neu starten:
   ```bash
   docker-compose restart keycloak
   ```

## Migration von alter Implementierung

### Entfernte Komponenten

Die folgenden Dateien wurden entfernt (waren unsicher/redundant):

- ❌ `infrastructure/gateway/security/KeycloakJwtAuthenticationFilter.kt`
  - **Grund**: Keine kryptographische Signaturvalidierung
  - **Ersetzt durch**: Spring Security oauth2ResourceServer

- ❌ `infrastructure/gateway/filter/KeycloakJwtAuthenticationFilter.kt`
  - **Grund**: Redundant zu oauth2ResourceServer
  - **Ersetzt durch**: Spring Security oauth2ResourceServer

### Konfigurationsänderungen

**Alt**:
```yaml
gateway:
  security:
    keycloak:
      enabled: true  # Custom Filter
```

**Neu**:
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/realms/meldestelle

gateway:
  security:
    keycloak:
      enabled: false  # Spring Security oauth2ResourceServer verwenden
```

## Best Practices

### Sicherheit

1. ✅ **Verwenden Sie starke Admin-Passwörter**
2. ✅ **Aktivieren Sie HTTPS in Produktion**
3. ✅ **Regelmäßige Backups der Keycloak-Datenbank**
4. ✅ **Überwachen Sie Failed Login Attempts**
5. ✅ **Verwenden Sie Service Accounts für Backend-Services**

### Performance

1. ✅ **Database Connection Pool anpassen** (KC_DB_POOL_MAX_SIZE)
2. ✅ **JVM Memory optimieren** (JAVA_OPTS_APPEND)
3. ✅ **Pre-built Image verwenden** (Dockerfile)
4. ✅ **Cache aktivieren** (KC_CACHE=ispn)

### Monitoring

1. ✅ **Metrics überwachen** (/metrics Endpoint)
2. ✅ **Health Checks konfigurieren**
3. ✅ **Event Logging aktivieren**
4. ✅ **Admin Events protokollieren**

## Weitere Ressourcen

- [Keycloak Official Documentation](https://www.keycloak.org/documentation)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [OpenID Connect Specification](https://openid.net/specs/openid-connect-core-1_0.html)

## Support

Bei Problemen oder Fragen:

1. Prüfen Sie die Logs
2. Konsultieren Sie dieses Dokument
3. Kontaktieren Sie das Development Team
