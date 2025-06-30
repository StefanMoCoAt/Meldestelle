# Meldestelle RESTful API Documentation

## Overview
This document describes the RESTful API for the Meldestelle (Austrian Equestrian Event Management System). The API provides endpoints for managing persons, clubs (Vereine), articles (Artikel), horses (Pferde), and tournaments (Turniere).

## Base URL
```
http://localhost:8080
```

## Authentication
Currently, the API does not implement authentication. This should be added in production.

## Content Type
All requests and responses use `application/json` content type.

## Error Handling
All endpoints return consistent error responses:
```json
{
  "error": "Error message description"
}
```

## HTTP Status Codes
- `200 OK` - Successful GET/PUT requests
- `201 Created` - Successful POST requests
- `204 No Content` - Successful DELETE requests
- `400 Bad Request` - Invalid request parameters or body
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## Health Check

### GET /health
Returns server health status.

**Response:**
```
OK
```

---

## Persons API

### GET /api/persons
Get all persons.

**Response:**
```json
[
  {
    "id": "uuid",
    "oepsSatzNr": "string",
    "nachname": "string",
    "vorname": "string",
    "titel": "string",
    "geburtsdatum": "2023-01-01",
    "geschlechtE": "MAENNLICH|WEIBLICH|DIVERS",
    "nationalitaet": "AUT",
    "email": "string",
    "telefon": "string",
    "adresse": "string",
    "plz": "string",
    "ort": "string",
    "stammVereinId": "uuid",
    "mitgliedsNummerIntern": "string",
    "letzteZahlungJahr": 2023,
    "feiId": "string",
    "istGesperrt": false,
    "sperrGrund": "string",
    "rollen": ["REITER", "RICHTER"],
    "lizenzen": [],
    "qualifikationenRichter": ["string"],
    "qualifikationenParcoursbauer": ["string"],
    "istAktiv": true,
    "createdAt": "2023-01-01T00:00:00Z",
    "updatedAt": "2023-01-01T00:00:00Z"
  }
]
```

### GET /api/persons/{id}
Get person by ID.

**Parameters:**
- `id` (path) - UUID of the person

### GET /api/persons/oeps/{oepsSatzNr}
Get person by OEPS registration number.

**Parameters:**
- `oepsSatzNr` (path) - OEPS registration number

### GET /api/persons/search?q={query}
Search persons by name or email.

**Parameters:**
- `q` (query) - Search query string

### GET /api/persons/verein/{vereinId}
Get all persons belonging to a specific club.

**Parameters:**
- `vereinId` (path) - UUID of the club

### POST /api/persons
Create a new person.

**Request Body:**
```json
{
  "oepsSatzNr": "string",
  "nachname": "string",
  "vorname": "string",
  "titel": "string",
  "geburtsdatum": "2023-01-01",
  "geschlechtE": "MAENNLICH",
  "nationalitaet": "AUT",
  "email": "string",
  "telefon": "string",
  "adresse": "string",
  "plz": "string",
  "ort": "string",
  "stammVereinId": "uuid",
  "istAktiv": true
}
```

### PUT /api/persons/{id}
Update an existing person.

**Parameters:**
- `id` (path) - UUID of the person

**Request Body:** Same as POST

### DELETE /api/persons/{id}
Delete a person.

**Parameters:**
- `id` (path) - UUID of the person

---

## Clubs (Vereine) API

### GET /api/vereine
Get all clubs.

**Response:**
```json
[
  {
    "id": "uuid",
    "oepsVereinsNr": "string",
    "name": "string",
    "kuerzel": "string",
    "bundesland": "string",
    "adresse": "string",
    "plz": "string",
    "ort": "string",
    "email": "string",
    "telefon": "string",
    "webseite": "string",
    "istAktiv": true,
    "createdAt": "2023-01-01T00:00:00Z",
    "updatedAt": "2023-01-01T00:00:00Z"
  }
]
```

### GET /api/vereine/{id}
Get club by ID.

**Parameters:**
- `id` (path) - UUID of the club

### GET /api/vereine/oeps/{oepsVereinsNr}
Get club by OEPS club number.

**Parameters:**
- `oepsVereinsNr` (path) - OEPS club number

### GET /api/vereine/search?q={query}
Search clubs by name, abbreviation, or location.

**Parameters:**
- `q` (query) - Search query string

### GET /api/vereine/bundesland/{bundesland}
Get clubs by federal state.

**Parameters:**
- `bundesland` (path) - Federal state code

### POST /api/vereine
Create a new club.

**Request Body:**
```json
{
  "oepsVereinsNr": "string",
  "name": "string",
  "kuerzel": "string",
  "bundesland": "string",
  "adresse": "string",
  "plz": "string",
  "ort": "string",
  "email": "string",
  "telefon": "string",
  "webseite": "string",
  "istAktiv": true
}
```

### PUT /api/vereine/{id}
Update an existing club.

**Parameters:**
- `id` (path) - UUID of the club

**Request Body:** Same as POST

### DELETE /api/vereine/{id}
Delete a club.

**Parameters:**
- `id` (path) - UUID of the club

---

## Articles (Artikel) API

### GET /api/artikel
Get all articles.

**Response:**
```json
[
  {
    "id": "uuid",
    "bezeichnung": "string",
    "preis": "10.50",
    "einheit": "string",
    "istVerbandsabgabe": false,
    "createdAt": "2023-01-01T00:00:00Z",
    "updatedAt": "2023-01-01T00:00:00Z"
  }
]
```

### GET /api/artikel/{id}
Get article by ID.

**Parameters:**
- `id` (path) - UUID of the article

### GET /api/artikel/search?q={query}
Search articles by name or unit.

**Parameters:**
- `q` (query) - Search query string

### GET /api/artikel/verbandsabgabe/{istVerbandsabgabe}
Get articles by association fee status.

**Parameters:**
- `istVerbandsabgabe` (path) - Boolean value (true/false)

### POST /api/artikel
Create a new article.

**Request Body:**
```json
{
  "bezeichnung": "string",
  "preis": "10.50",
  "einheit": "string",
  "istVerbandsabgabe": false
}
```

### PUT /api/artikel/{id}
Update an existing article.

**Parameters:**
- `id` (path) - UUID of the article

**Request Body:** Same as POST

### DELETE /api/artikel/{id}
Delete an article.

**Parameters:**
- `id` (path) - UUID of the article

---

## Horses (Pferde) API

### GET /api/horses
Get all horses.

**Response:**
```json
[
  {
    "pferdId": "uuid",
    "oepsSatzNrPferd": "string",
    "oepsKopfNr": "string",
    "name": "string",
    "lebensnummer": "string",
    "feiPassNr": "string",
    "geburtsjahr": 2015,
    "geschlecht": "WALLACH|STUTE|HENGST",
    "farbe": "string",
    "rasse": "string",
    "abstammungVaterName": "string",
    "abstammungMutterName": "string",
    "abstammungMutterVaterName": "string",
    "abstammungZusatzInfo": "string",
    "besitzerPersonId": "uuid",
    "verantwortlichePersonId": "uuid",
    "heimatVereinId": "uuid",
    "letzteZahlungPferdegebuehrJahrOeps": 2023,
    "stockmassCm": 165,
    "datenQuelle": "MANUELL|ZNS_IMPORT",
    "istAktiv": true,
    "notizenIntern": "string",
    "createdAt": "2023-01-01T00:00:00Z",
    "updatedAt": "2023-01-01T00:00:00Z"
  }
]
```

### GET /api/horses/{id}
Get horse by ID.

**Parameters:**
- `id` (path) - UUID of the horse

### GET /api/horses/oeps/{oepsSatzNr}
Get horse by OEPS registration number.

**Parameters:**
- `oepsSatzNr` (path) - OEPS registration number

### GET /api/horses/lebensnummer/{lebensnummer}
Get horse by life number (UELN).

**Parameters:**
- `lebensnummer` (path) - Horse life number

### GET /api/horses/search?q={query}
Search horses by name or other attributes.

**Parameters:**
- `q` (query) - Search query string

### GET /api/horses/name/{name}
Get horses by name.

**Parameters:**
- `name` (path) - Horse name

### GET /api/horses/owner/{ownerId}
Get horses by owner ID.

**Parameters:**
- `ownerId` (path) - UUID of the owner person

### GET /api/horses/responsible/{personId}
Get horses by responsible person ID.

**Parameters:**
- `personId` (path) - UUID of the responsible person

### GET /api/horses/club/{clubId}
Get horses by home club ID.

**Parameters:**
- `clubId` (path) - UUID of the home club

### GET /api/horses/breed/{breed}
Get horses by breed.

**Parameters:**
- `breed` (path) - Horse breed

### GET /api/horses/birth-year/{year}
Get horses by birth year.

**Parameters:**
- `year` (path) - Birth year (integer)

### GET /api/horses/active
Get only active horses.

### POST /api/horses
Create a new horse.

**Request Body:**
```json
{
  "oepsSatzNrPferd": "string",
  "oepsKopfNr": "string",
  "name": "string",
  "lebensnummer": "string",
  "feiPassNr": "string",
  "geburtsjahr": 2015,
  "geschlecht": "WALLACH",
  "farbe": "string",
  "rasse": "string",
  "abstammungVaterName": "string",
  "abstammungMutterName": "string",
  "abstammungMutterVaterName": "string",
  "abstammungZusatzInfo": "string",
  "besitzerPersonId": "uuid",
  "verantwortlichePersonId": "uuid",
  "heimatVereinId": "uuid",
  "letzteZahlungPferdegebuehrJahrOeps": 2023,
  "stockmassCm": 165,
  "datenQuelle": "MANUELL",
  "istAktiv": true,
  "notizenIntern": "string"
}
```

### PUT /api/horses/{id}
Update an existing horse.

**Parameters:**
- `id` (path) - UUID of the horse

**Request Body:** Same as POST

### DELETE /api/horses/{id}
Delete a horse.

**Parameters:**
- `id` (path) - UUID of the horse

---

## Data Models

### Person
Represents a person in the system (rider, judge, official, etc.).

### Verein (Club)
Represents an equestrian club or association.

### Artikel (Article)
Represents items/products that can be sold at events.

### Pferd (Horse)
Represents a horse with breeding information and ownership details.

### Turnier (Tournament)
Represents an equestrian tournament/competition.

---

## Future Enhancements

1. **Authentication & Authorization** - Implement JWT-based authentication
2. **Pagination** - Add pagination support for list endpoints
3. **Filtering** - Add more advanced filtering options
4. **Validation** - Implement comprehensive input validation
5. **Rate Limiting** - Add rate limiting for API protection
6. **API Versioning** - Implement API versioning strategy
7. **Documentation** - Add OpenAPI/Swagger documentation
8. **Caching** - Implement caching for frequently accessed data
9. **Audit Logging** - Add audit trails for data changes
10. **Bulk Operations** - Support bulk create/update/delete operations

---

## Technical Details

- **Framework:** Ktor (Kotlin)
- **Database:** PostgreSQL with Exposed ORM
- **Serialization:** Kotlinx Serialization
- **UUID:** Multiplatform UUID library
- **Date/Time:** Kotlinx DateTime

## Database Schema

The API is built on top of the following main database tables:
- `personen` - Person data
- `vereine` - Club data
- `artikel` - Article data
- `pferde` - Horse data
- `turniere` - Tournament data
- `veranstaltungen` - Event data
- `plaetze` - Venue data
- `lizenzen` - License data

Each table includes standard audit fields (`created_at`, `updated_at`) and uses UUIDs as primary keys.
