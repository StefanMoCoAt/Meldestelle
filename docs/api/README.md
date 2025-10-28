---
modul: api-overview
status: active
last_reviewed: 2025-10-22
review_cycle: 180d
summary: Überblick und Einstieg in die REST‑APIs der Meldestelle.
yt_epic: MP-1
yt_issues: [MP-7]
tags: [api, overview]
bc: infrastructure
doc_type: api
---

# Meldestelle – REST‑API Dokumentation

## Überblick

Die Meldestelle-Anwendung bietet eine umfassende REST API für die Verwaltung von Pferdesportveranstaltungen. Die API folgt RESTful-Prinzipien und ist in modulare Services unterteilt, die jeweils spezifische Domänen abdecken.

## API-Architektur

### Modulare Service-Struktur

Die API ist in folgende Hauptmodule unterteilt:

```
API Services
├── Members API          # Mitgliederverwaltung
├── Horses API           # Pferderegistrierung
├── Events API           # Veranstaltungsverwaltung
└── Masterdata API       # Stammdatenverwaltung
    ├── Countries        # Länderverwaltung
    ├── States           # Bundesländerverwaltung
    ├── Age Classes      # Altersklassenverwaltung
    └── Venues           # Plätze/Austragungsorte
```

### Technische Spezifikationen

- **Framework**: Spring Boot 3.x mit Spring Web MVC
- **Dokumentation**: OpenAPI 3.0 (Swagger)
- **Serialisierung**: JSON mit Jackson/Kotlinx Serialization
- **Authentifizierung**: JWT Bearer Token
- **Versionierung**: URL-basiert (/api/v1/)
- **Content-Type**: application/json
- **Zeichenkodierung**: UTF-8

## Basis-URL und Endpunkte

### Entwicklungsumgebung

```
Base URL: http://localhost:8081/api
```

### Produktionsumgebung

```
Base URL: https://api.meldestelle.yourdomain.com/api
```

## API-Module Übersicht

### 1. Members API

**Basis-Pfad**: `/api/members`

Verwaltung von Vereinsmitgliedern und deren Mitgliedschaftsdaten.

**Hauptfunktionen**:

- Mitgliederverwaltung (CRUD)
- Mitgliedschaftsstatus-Tracking
- Ablaufende Mitgliedschaften
- Validierung von E-Mail und Mitgliedsnummer

**Controller**: `MemberController`
**Endpunkte**: 12 REST-Endpunkte
**Dokumentation**: [Members API](members-api.md)

### 2. Horses API

**Basis-Pfad**: `/api/horses`

Registrierung und Verwaltung von Pferden mit umfassenden Identifikationsdaten.

**Hauptfunktionen**:

- Pferderegistrierung (CRUD)
- Identifikationsnummern-Verwaltung
- OEPS/FEI-Registrierung
- Besitzer- und Verantwortlichen-Zuordnung

**Controller**: `HorseController`
**Endpunkte**: 15+ REST-Endpunkte

### 3. Events API

**Basis-Pfad**: `/api/events`

Planung und Verwaltung von Pferdesportveranstaltungen.

**Hauptfunktionen**:

- Veranstaltungsplanung (CRUD)
- Terminverwaltung
- Teilnehmerverwaltung
- Öffentliche/Private Veranstaltungen

**Controller**: `VeranstaltungController`
**Endpunkte**: 10+ REST-Endpunkte

### 4. Masterdata API

**Basis-Pfad**: `/api/masterdata`

Verwaltung von Stammdaten für das gesamte System.

#### 4.1 Countries API

**Pfad**: `/api/masterdata/countries`

- Länderverwaltung mit ISO-Codes
- EU/EWR-Mitgliedschaft
- Mehrsprachige Ländernamen

#### 4.2 States API

**Pfad**: `/api/masterdata/states`

- Bundesländer/Kantone/Regionen
- OEPS-Codes für österreichische Bundesländer
- ISO 3166-2 Codes

#### 4.3 Age Classes API

**Pfad**: `/api/masterdata/age-classes`

- Altersklassen für verschiedene Sparten
- Teilnahmeberechtigung
- Geschlechts- und Spartenfilter

#### 4.4 Venues API

**Pfad**: `/api/masterdata/venues`

- Turnierplätze und Austragungsorte
- Platztypen und Abmessungen
- Bodenarten und Eignung

**Controller**: `CountryController`, `BundeslandController`, `AltersklasseController`, `PlatzController`
**Endpunkte**: 37+ REST-Endpunkte

## Gemeinsame API-Konventionen

### HTTP-Status-Codes

| Status Code | Bedeutung | Verwendung |
|-------------|-----------|------------|
| 200 | OK | Erfolgreiche GET/PUT-Anfragen |
| 201 | Created | Erfolgreiche POST-Anfragen |
| 204 | No Content | Erfolgreiche DELETE-Anfragen |
| 400 | Bad Request | Ungültige Anfragedaten |
| 401 | Unauthorized | Fehlende/ungültige Authentifizierung |
| 403 | Forbidden | Unzureichende Berechtigung |
| 404 | Not Found | Ressource nicht gefunden |
| 409 | Conflict | Duplikat oder Geschäftsregel-Verletzung |
| 422 | Unprocessable Entity | Validierungsfehler |
| 500 | Internal Server Error | Serverfehler |

### Standard-Response-Format

Alle API-Endpunkte verwenden ein einheitliches Response-Format:

```json
{
  "data": {},
  "success": true,
  "message": "Operation completed successfully",
  "errors": [],
  "timestamp": "2025-07-25T12:37:00Z"
}
```

#### Erfolgreiche Antwort

```json
{
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "firstName": "Max",
    "lastName": "Mustermann",
    "email": "max@example.com"
  },
  "success": true,
  "message": null,
  "errors": [],
  "timestamp": "2025-07-25T12:37:00Z"
}
```

#### Fehler-Antwort

```json
{
  "data": null,
  "success": false,
  "message": "Validation failed",
  "errors": [
    "Email address is required",
    "First name must not be empty"
  ],
  "timestamp": "2025-07-25T12:37:00Z"
}
```

### Paginierung

Für Listen-Endpunkte wird standardmäßig Paginierung unterstützt:

**Query-Parameter**:

- `limit`: Maximale Anzahl Ergebnisse (Standard: 100, Maximum: 1000)
- `offset`: Anzahl zu überspringende Ergebnisse (Standard: 0)

**Beispiel-Anfrage**:

```
GET /api/members?limit=50&offset=100
```

**Paginierte Antwort**:

```json
{
  "data": {
    "content": [],
    "page": 2,
    "size": 50,
    "totalElements": 1250,
    "totalPages": 25,
    "hasNext": true,
    "hasPrevious": true
  },
  "success": true,
  "timestamp": "2025-07-25T12:37:00Z"
}
```

### Suchfunktionalität

Viele Endpunkte unterstützen Suchfunktionalität:

**Query-Parameter**:

- `search`: Suchbegriff für Textfelder
- `name`: Suche nach Namen (Teilübereinstimmung)
- `active`: Filter für aktive/inaktive Einträge

**Beispiel**:

```
GET /api/members?search=Schmidt&active=true&limit=20
```

### Sortierung

Sortierung wird über Query-Parameter gesteuert:

**Query-Parameter**:

- `sort`: Sortierfeld (z.B. `name`, `createdAt`)
- `order`: Sortierrichtung (`asc` oder `desc`)

**Beispiel**:

```
GET /api/members?sort=lastName&order=asc
```

## Authentifizierung und Autorisierung

### JWT Bearer Token

Alle API-Endpunkte (außer öffentlichen) erfordern Authentifizierung über JWT Bearer Token:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Rollen und Berechtigungen

| Rolle | Beschreibung | Berechtigungen |
|-------|--------------|----------------|
| ADMIN | Systemadministrator | Vollzugriff auf alle Ressourcen |
| TRAINER | Trainer/Ausbilder | Zugriff auf Pferde und Veranstaltungen |
| MEMBER | Vereinsmitglied | Zugriff auf eigene Daten |
| GUEST | Gast | Nur Lesezugriff auf öffentliche Daten |

## Rate Limiting

Zum Schutz der API vor Überlastung gelten folgende Limits:

- **Authentifizierte Benutzer**: 1000 Anfragen/Stunde
- **Nicht authentifizierte Benutzer**: 100 Anfragen/Stunde
- **Burst-Limit**: 50 Anfragen/Minute

Bei Überschreitung wird HTTP 429 (Too Many Requests) zurückgegeben.

## Fehlerbehandlung

### Validierungsfehler (422)

```json
{
  "data": null,
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email address is invalid",
      "code": "INVALID_EMAIL"
    },
    {
      "field": "membershipNumber",
      "message": "Membership number already exists",
      "code": "DUPLICATE_MEMBERSHIP_NUMBER"
    }
  ],
  "timestamp": "2025-07-25T12:37:00Z"
}
```

### Geschäftsregel-Verletzungen (409)

```json
{
  "data": null,
  "success": false,
  "message": "Business rule violation",
  "errors": [
    "Membership end date cannot be before start date"
  ],
  "timestamp": "2025-07-25T12:37:00Z"
}
```

## API-Dokumentation

### Swagger/OpenAPI

Die vollständige API-Dokumentation ist über Swagger UI verfügbar:

- **Entwicklung**: <http://localhost:8080/swagger-ui.html>
- **Produktion**: <https://api.meldestelle.at/swagger-ui.html>

### Postman Collections

Postman Collections für alle API-Endpunkte sind verfügbar unter:

- [docs/postman/](../postman/)

## Versionierung

Die API verwendet URL-basierte Versionierung:

- **Aktuelle Version**: v1
- **Basis-URL**: `/api/v1/`
- **Deprecated Versionen**: Werden 12 Monate unterstützt

## Monitoring und Observability

### Health Checks

```
GET /actuator/health
```

### Metriken

```
GET /actuator/metrics
GET /actuator/prometheus
```

### API-Metriken

- Request-Anzahl pro Endpunkt
- Response-Zeiten
- Fehlerquoten
- Rate-Limiting-Statistiken

## Entwicklung und Testing

### Lokale Entwicklung

```bash
# API-Server starten
./gradlew bootRun

# Swagger UI öffnen
open http://localhost:8080/swagger-ui.html
```

### API-Tests

```bash
# Unit Tests
./gradlew test

# Integration Tests
./gradlew integrationTest

# API Tests mit Newman
newman run docs/postman/meldestelle-api.postman_collection.json
```

## Support und Kontakt

- **Dokumentation**: [docs/api/](.)
- **Issue Tracker**: GitHub Issues
- **API-Status**: <https://status.meldestelle.at>

---

**Letzte Aktualisierung**: 25. Juli 2025
**API-Version**: v1.0
**OpenAPI-Spezifikation**: 3.0.3
