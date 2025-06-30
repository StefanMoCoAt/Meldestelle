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
- `GET /health` - Gesundheitsprüfung des Services
- `GET /api` - API-Informationen

### Person Management (`/api/persons`)
- `GET /api/persons` - Alle Personen abrufen
- `POST /api/persons` - Neue Person erstellen
- `GET /api/persons/{id}` - Person nach UUID abrufen
- `PUT /api/persons/{id}` - Person aktualisieren
- `DELETE /api/persons/{id}` - Person löschen
- `GET /api/persons/oeps/{oepsSatzNr}` - Person nach OEPS-Nummer abrufen
- `GET /api/persons/search?q={query}` - Personen suchen
- `GET /api/persons/verein/{vereinId}` - Personen nach Verein-ID abrufen

## Schema-Definitionen

### Person
```yaml
Person:
  type: object
  properties:
    id:
      type: string
      format: uuid
    vorname:
      type: string
    nachname:
      type: string
    geburtsdatum:
      type: string
      format: date
    oepsSatzNr:
      type: string
    vereinId:
      type: string
      format: uuid
    email:
      type: string
      format: email
    telefon:
      type: string
  required:
    - vorname
    - nachname
```

### Error
```yaml
Error:
  type: object
  properties:
    error:
      type: string
  required:
    - error
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
`server/src/main/resources/openapi.yaml`

### Beispiel für neuen Endpunkt:
```yaml
/api/vereine:
  get:
    summary: Get all clubs
    description: Retrieve a list of all clubs
    tags:
      - Clubs
    responses:
      '200':
        description: List of clubs
        content:
          application/json:
            schema:
              type: array
              items:
                $ref: '#/components/schemas/Verein'
```

## Technische Details

### Dependencies
- `io.ktor:ktor-server-openapi:3.1.2`
- `io.ktor:ktor-server-swagger:3.1.2`

### Konfiguration
Die Swagger/OpenAPI-Konfiguration befindet sich in:
- `server/src/main/kotlin/at/mocode/plugins/Routing.kt`
- `server/src/main/resources/openapi.yaml`

### Tests
Automatisierte Tests für die Swagger-Funktionalität:
- `server/src/test/kotlin/at/mocode/SwaggerTest.kt`

## Nächste Schritte

1. **Erweitern Sie die Dokumentation** für weitere API-Endpunkte (Vereine, Turniere, etc.)
2. **Fügen Sie Authentifizierung hinzu** zur OpenAPI-Spezifikation wenn implementiert
3. **Konfigurieren Sie Produktions-URLs** in der OpenAPI-Spezifikation
4. **Implementieren Sie API-Versionierung** in der Dokumentation

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
