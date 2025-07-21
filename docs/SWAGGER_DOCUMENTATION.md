# Swagger/OpenAPI Documentation

## Übersicht

Die Meldestelle API verfügt jetzt über eine vollständige Swagger/OpenAPI-Dokumentation, die eine interaktive Benutzeroberfläche zur Erkundung und Testung der API-Endpunkte bietet.

## Zugriff auf die Dokumentation

### Swagger UI
- **URL**: `http://localhost:8080/swagger`
- **Beschreibung**: Interaktive Benutzeroberfläche zur Erkundung der API
- **Features**:
  - Vollständige API-Dokumentation
  - Interaktive Testmöglichkeiten
  - Beispiel-Requests und -Responses
  - Schema-Definitionen

### OpenAPI Specification
- **URL**: `http://localhost:8080/openapi`
- **Beschreibung**: Raw OpenAPI 3.0.3 Spezifikation im YAML-Format
- **Verwendung**: Kann für Code-Generierung oder Import in andere Tools verwendet werden

## Dokumentierte Endpunkte

### Basis-Endpunkte
- `GET /` - API Gateway Information
- `GET /health` - Gesundheitsprüfung des Services
- `GET /docs` - Zentrale API-Dokumentationsseite
- `GET /api` - Weiterleitung zur zentralen API-Dokumentationsseite
- `GET /api/json` - API-Informationen im JSON-Format

### Authentication Context (`/auth/*`)
- `POST /auth/login` - Benutzeranmeldung
- `POST /auth/register` - Benutzerregistrierung
- `GET /auth/profile` - Benutzerprofil abrufen

### Master Data Context (`/api/masterdata/*`)
- `GET /api/masterdata/countries` - Alle Länder abrufen
- `POST /api/masterdata/countries` - Neues Land erstellen
- `GET /api/masterdata/countries/{id}` - Land nach ID abrufen
- `PUT /api/masterdata/countries/{id}` - Land aktualisieren
- `DELETE /api/masterdata/countries/{id}` - Land löschen

### Horse Registry Context (`/api/horses/*`)
- `GET /api/horses` - Alle Pferde abrufen
- `GET /api/horses/fei-registered` - FEI-registrierte Pferde abrufen
- `GET /api/horses/stats` - Pferdestatistiken abrufen
- `POST /api/horses/stats` - Neues Pferd registrieren
- `GET /api/horses/{id}` - Pferd nach ID abrufen

### Event Management Context (`/api/events/*`)
- `GET /api/events` - Alle Veranstaltungen abrufen
- `GET /api/events/stats` - Veranstaltungsstatistiken abrufen
- `POST /api/events/stats` - Neue Veranstaltung erstellen

## Schema-Definitionen

### LoginRequest
```yaml
LoginRequest:
  type: object
  properties:
    email:
      type: string
      format: email
    password:
      type: string
      format: password
  required:
    - email
    - password
```

### UserProfileResponse
```yaml
UserProfileResponse:
  type: object
  properties:
    id:
      type: string
      format: uuid
    email:
      type: string
      format: email
    firstName:
      type: string
    lastName:
      type: string
    phoneNumber:
      type: string
    roles:
      type: array
      items:
        type: string
    createdAt:
      type: string
      format: date-time
    updatedAt:
      type: string
      format: date-time
```

### CountryResponse
```yaml
CountryResponse:
  type: object
  properties:
    id:
      type: string
      format: uuid
    name:
      type: string
    isoCode:
      type: string
    active:
      type: boolean
```

### HorseResponse
```yaml
HorseResponse:
  type: object
  properties:
    id:
      type: string
      format: uuid
    name:
      type: string
    birthYear:
      type: integer
    breed:
      type: string
    color:
      type: string
    gender:
      type: string
      enum: [STALLION, MARE, GELDING]
    feiRegistered:
      type: boolean
    ownerId:
      type: string
      format: uuid
    active:
      type: boolean
```

### EventResponse
```yaml
EventResponse:
  type: object
  properties:
    id:
      type: string
      format: uuid
    name:
      type: string
    startDate:
      type: string
      format: date
    endDate:
      type: string
      format: date
    location:
      type: string
    organizerId:
      type: string
      format: uuid
    description:
      type: string
    status:
      type: string
      enum: [DRAFT, PUBLISHED, CANCELLED, COMPLETED]
```

### ErrorResponse
```yaml
ErrorResponse:
  type: object
  properties:
    success:
      type: boolean
    message:
      type: string
    errors:
      type: array
      items:
        type: object
        properties:
          field:
            type: string
          message:
            type: string
    timestamp:
      type: string
      format: date-time
```

## Verwendung

### 1. Server starten
```bash
./gradlew :server:run
```

### 2. Swagger UI öffnen
Navigieren Sie zu `http://localhost:8080/swagger` in Ihrem Browser.

### 3. API erkunden
- Klicken Sie auf die verschiedenen Endpunkte, um Details zu sehen
- Verwenden Sie "Try it out" um Requests direkt zu testen
- Sehen Sie sich die Beispiel-Responses an

### 4. OpenAPI Spec herunterladen
Besuchen Sie `http://localhost:8080/openapi` um die vollständige OpenAPI-Spezifikation zu erhalten.

## Erweiterung der Dokumentation

### Neue Endpunkte hinzufügen
Um neue API-Endpunkte zu dokumentieren, erweitern Sie die Datei:
`api-gateway/src/jvmMain/resources/openapi/documentation.yaml`

### Beispiel für neuen Endpunkt:
```yaml
/api/events/categories:
  get:
    tags:
      - Event Management
    summary: Get Event Categories
    description: Returns a list of all event categories
    operationId: getEventCategories
    responses:
      '200':
        description: Successful operation
        content:
          application/json:
            schema:
              type: array
              items:
                $ref: '#/components/schemas/EventCategoryResponse'
```

## Technische Details

### Dependencies
- `io.ktor:ktor-server-openapi:3.1.2`
- `io.ktor:ktor-server-swagger:3.1.2`

### Konfiguration
Die Swagger/OpenAPI-Konfiguration befindet sich in:
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/config/OpenApiConfig.kt` - Konfiguration der OpenAPI und Swagger UI Endpunkte
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/module.kt` - Integration der OpenAPI-Konfiguration in die Anwendung
- `api-gateway/src/jvmMain/resources/openapi/documentation.yaml` - OpenAPI-Spezifikation im YAML-Format

### Implementierte Funktionen
- Vollständige OpenAPI 3.0.3 Spezifikation
- Interaktive Swagger UI für API-Exploration
- Dokumentation aller API-Endpunkte aus allen Bounded Contexts
- Authentifizierung mit JWT-Token
- Beispiel-Requests und -Responses für alle Endpunkte
- Schema-Definitionen für alle Datenmodelle

## Aktueller Status

✅ **Implementiert**:
- OpenAPI-Spezifikation für alle Bounded Contexts
- Swagger UI für interaktive API-Exploration
- JWT-Authentifizierung in der OpenAPI-Spezifikation
- Produktions- und Entwicklungs-URLs in der Spezifikation
- Vollständige Dokumentation aller Endpunkte und Datenmodelle

## Troubleshooting

### Swagger UI lädt nicht
- Überprüfen Sie, ob der Server läuft
- Stellen Sie sicher, dass Port 8080 verfügbar ist
- Prüfen Sie die Logs auf Fehler

### OpenAPI Spec ist leer
- Überprüfen Sie, ob `openapi.yaml` im Classpath verfügbar ist
- Stellen Sie sicher, dass die Datei gültiges YAML enthält

### API-Endpunkte fehlen in der Dokumentation
- Erweitern Sie die `openapi.yaml` Datei
- Starten Sie den Server neu nach Änderungen
