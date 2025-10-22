---
modul: members-api
status: active
last_reviewed: 2025-10-22
review_cycle: 180d
summary: Dokumentation der Members‑API (Endpunkte, Parameter, Beispiele).
yt_epic: MP-1
yt_issues: []
tags: [api, members]
---

# Members‑API – Dokumentation

## Überblick

Die Members API bietet umfassende Funktionalität zur Verwaltung von Vereinsmitgliedern und deren Mitgliedschaftsdaten. Sie unterstützt vollständige CRUD-Operationen sowie spezialisierte Funktionen für Mitgliedschaftsverwaltung, Validierung und Statistiken.

## Basis-Informationen

- **Basis-URL**: `/api/members`
- **Controller**: `MemberController`
- **Authentifizierung**: JWT Bearer Token erforderlich
- **Content-Type**: `application/json`
- **Zeichenkodierung**: UTF-8

## Endpunkte Übersicht

| Methode | Endpunkt | Beschreibung |
|---------|----------|--------------|
| GET | `/api/members` | Alle Mitglieder abrufen |
| GET | `/api/members/{id}` | Mitglied nach ID abrufen |
| GET | `/api/members/by-membership-number/{membershipNumber}` | Mitglied nach Mitgliedsnummer |
| GET | `/api/members/by-email/{email}` | Mitglied nach E-Mail |
| GET | `/api/members/stats` | Mitgliederstatistiken |
| POST | `/api/members` | Neues Mitglied erstellen |
| PUT | `/api/members/{id}` | Mitglied aktualisieren |
| DELETE | `/api/members/{id}` | Mitglied löschen |
| GET | `/api/members/expiring-memberships` | Ablaufende Mitgliedschaften |
| GET | `/api/members/by-date-range` | Mitglieder nach Datumsbereich |
| GET | `/api/members/validate/email/{email}` | E-Mail-Eindeutigkeit prüfen |
| GET | `/api/members/validate/membership-number/{membershipNumber}` | Mitgliedsnummer-Eindeutigkeit prüfen |

## Detaillierte Endpunkt-Dokumentation

### 1. Alle Mitglieder abrufen

```http
GET /api/members
```

Ruft eine Liste aller Mitglieder ab mit optionaler Filterung und Suche.

#### Query-Parameter

| Parameter | Typ | Standard | Beschreibung |
|-----------|-----|----------|--------------|
| `activeOnly` | boolean | `true` | Nur aktive Mitglieder anzeigen |
| `limit` | integer | `100` | Maximale Anzahl Ergebnisse |
| `offset` | integer | `0` | Anzahl zu überspringende Ergebnisse |
| `search` | string | - | Suchbegriff für Mitgliedernamen |

#### Beispiel-Anfrage

```http
GET /api/members?activeOnly=true&limit=50&search=Schmidt
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Erfolgreiche Antwort (200 OK)

```json
{
  "data": [
    {
      "memberId": "123e4567-e89b-12d3-a456-426614174000",
      "firstName": "Max",
      "lastName": "Schmidt",
      "email": "max.schmidt@example.com",
      "phone": "+43 1 234 5678",
      "dateOfBirth": "1985-03-15",
      "membershipNumber": "M2024001",
      "membershipStartDate": "2024-01-01",
      "membershipEndDate": "2024-12-31",
      "isActive": true,
      "address": "Musterstraße 123, 1010 Wien",
      "emergencyContact": "Anna Schmidt, +43 1 234 5679",
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-07-25T12:37:00Z"
    }
  ],
  "success": true,
  "message": null,
  "errors": [],
  "timestamp": "2025-07-25T12:37:00Z"
}
```

#### Fehler-Antworten

- **500 Internal Server Error**: Serverfehler beim Abrufen der Mitglieder

### 2. Mitglied nach ID abrufen

```http
GET /api/members/{id}
```

Ruft ein spezifisches Mitglied anhand seiner eindeutigen ID ab.

#### Pfad-Parameter

| Parameter | Typ | Beschreibung |
|-----------|-----|--------------|
| `id` | UUID | Eindeutige Mitglieder-ID |

#### Beispiel-Anfrage

```http
GET /api/members/123e4567-e89b-12d3-a456-426614174000
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Erfolgreiche Antwort (200 OK)

```json
{
  "data": {
    "memberId": "123e4567-e89b-12d3-a456-426614174000",
    "firstName": "Max",
    "lastName": "Schmidt",
    "email": "max.schmidt@example.com",
    "phone": "+43 1 234 5678",
    "dateOfBirth": "1985-03-15",
    "membershipNumber": "M2024001",
    "membershipStartDate": "2024-01-01",
    "membershipEndDate": "2024-12-31",
    "isActive": true,
    "address": "Musterstraße 123, 1010 Wien",
    "emergencyContact": "Anna Schmidt, +43 1 234 5679",
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-07-25T12:37:00Z"
  },
  "success": true,
  "message": null,
  "errors": [],
  "timestamp": "2025-07-25T12:37:00Z"
}
```

#### Fehler-Antworten

- **400 Bad Request**: Ungültiges UUID-Format
- **404 Not Found**: Mitglied nicht gefunden
- **500 Internal Server Error**: Serverfehler

### 3. Mitglied nach Mitgliedsnummer abrufen

```http
GET /api/members/by-membership-number/{membershipNumber}
```

#### Pfad-Parameter

| Parameter | Typ | Beschreibung |
|-----------|-----|--------------|
| `membershipNumber` | string | Mitgliedsnummer |

#### Beispiel-Anfrage

```http
GET /api/members/by-membership-number/M2024001
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 4. Mitglied nach E-Mail abrufen

```http
GET /api/members/by-email/{email}
```

#### Pfad-Parameter

| Parameter | Typ | Beschreibung |
|-----------|-----|--------------|
| `email` | string | E-Mail-Adresse |

#### Beispiel-Anfrage

```http
GET /api/members/by-email/max.schmidt@example.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 5. Mitgliederstatistiken abrufen

```http
GET /api/members/stats
```

Ruft Statistiken über die Mitgliederdatenbank ab.

#### Erfolgreiche Antwort (200 OK)

```json
{
  "data": {
    "totalActive": 1250,
    "totalMembers": 1380
  },
  "success": true,
  "message": null,
  "errors": [],
  "timestamp": "2025-07-25T12:37:00Z"
}
```

### 6. Neues Mitglied erstellen

```http
POST /api/members
```

Erstellt ein neues Mitglied mit den bereitgestellten Daten.

#### Request Body

```json
{
  "firstName": "Max",
  "lastName": "Mustermann",
  "email": "max.mustermann@example.com",
  "phone": "+43 1 234 5678",
  "dateOfBirth": "1985-03-15",
  "membershipNumber": "M2024002",
  "membershipStartDate": "2024-08-01",
  "membershipEndDate": "2024-12-31",
  "isActive": true,
  "address": "Beispielstraße 456, 1020 Wien",
  "emergencyContact": "Anna Mustermann, +43 1 234 5679"
}
```

#### Erfolgreiche Antwort (201 Created)

```json
{
  "data": {
    "memberId": "456e7890-e89b-12d3-a456-426614174001",
    "firstName": "Max",
    "lastName": "Mustermann",
    "email": "max.mustermann@example.com",
    "phone": "+43 1 234 5678",
    "dateOfBirth": "1985-03-15",
    "membershipNumber": "M2024002",
    "membershipStartDate": "2024-08-01",
    "membershipEndDate": "2024-12-31",
    "isActive": true,
    "address": "Beispielstraße 456, 1020 Wien",
    "emergencyContact": "Anna Mustermann, +43 1 234 5679",
    "createdAt": "2025-07-25T12:37:00Z",
    "updatedAt": "2025-07-25T12:37:00Z"
  },
  "success": true,
  "message": null,
  "errors": [],
  "timestamp": "2025-07-25T12:37:00Z"
}
```

#### Fehler-Antworten

- **400 Bad Request**: Ungültige Anfragedaten
- **409 Conflict**: E-Mail oder Mitgliedsnummer bereits vorhanden
- **422 Unprocessable Entity**: Validierungsfehler

### 7. Mitglied aktualisieren

```http
PUT /api/members/{id}
```

Aktualisiert ein bestehendes Mitglied.

#### Pfad-Parameter

| Parameter | Typ | Beschreibung |
|-----------|-----|--------------|
| `id` | UUID | Eindeutige Mitglieder-ID |

#### Request Body

```json
{
  "firstName": "Max",
  "lastName": "Mustermann",
  "email": "max.mustermann.updated@example.com",
  "phone": "+43 1 234 5678",
  "dateOfBirth": "1985-03-15",
  "membershipNumber": "M2024002",
  "membershipStartDate": "2024-08-01",
  "membershipEndDate": "2025-07-31",
  "isActive": true,
  "address": "Neue Straße 789, 1030 Wien",
  "emergencyContact": "Anna Mustermann, +43 1 234 5679"
}
```

#### Erfolgreiche Antwort (200 OK)

Gleiche Struktur wie bei der Erstellung, aber mit aktualisierten Daten und `updatedAt` Zeitstempel.

#### Fehler-Antworten

- **400 Bad Request**: Ungültige Anfragedaten oder UUID-Format
- **404 Not Found**: Mitglied nicht gefunden
- **409 Conflict**: E-Mail oder Mitgliedsnummer bereits vorhanden
- **500 Internal Server Error**: Serverfehler

### 8. Mitglied löschen

```http
DELETE /api/members/{id}
```

Löscht ein Mitglied aus dem System.

#### Pfad-Parameter

| Parameter | Typ | Beschreibung |
|-----------|-----|--------------|
| `id` | UUID | Eindeutige Mitglieder-ID |

#### Beispiel-Anfrage

```http
DELETE /api/members/123e4567-e89b-12d3-a456-426614174000
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Erfolgreiche Antwort (200 OK)

```json
{
  "data": "Member deleted successfully",
  "success": true,
  "message": null,
  "errors": [],
  "timestamp": "2025-07-25T12:37:00Z"
}
```

#### Fehler-Antworten

- **400 Bad Request**: Ungültiges UUID-Format
- **404 Not Found**: Mitglied nicht gefunden
- **500 Internal Server Error**: Serverfehler

### 9. Ablaufende Mitgliedschaften abrufen

```http
GET /api/members/expiring-memberships
```

Ruft Mitglieder ab, deren Mitgliedschaft in den nächsten Tagen abläuft.

#### Query-Parameter

| Parameter | Typ | Standard | Beschreibung |
|-----------|-----|----------|--------------|
| `daysAhead` | integer | `30` | Anzahl Tage im Voraus |

#### Beispiel-Anfrage

```http
GET /api/members/expiring-memberships?daysAhead=14
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Erfolgreiche Antwort (200 OK)

```json
{
  "data": [
    {
      "memberId": "123e4567-e89b-12d3-a456-426614174000",
      "firstName": "Max",
      "lastName": "Schmidt",
      "email": "max.schmidt@example.com",
      "membershipNumber": "M2024001",
      "membershipEndDate": "2025-08-10",
      "daysUntilExpiry": 14
    }
  ],
  "success": true,
  "message": null,
  "errors": [],
  "timestamp": "2025-07-25T12:37:00Z"
}
```

### 10. Mitglieder nach Datumsbereich abrufen

```http
GET /api/members/by-date-range
```

Ruft Mitglieder basierend auf einem Datumsbereich ab.

#### Query-Parameter

| Parameter | Typ | Erforderlich | Beschreibung |
|-----------|-----|--------------|--------------|
| `startDate` | string (YYYY-MM-DD) | Ja | Startdatum |
| `endDate` | string (YYYY-MM-DD) | Ja | Enddatum |
| `dateType` | string | Nein | `MEMBERSHIP_START_DATE` oder `MEMBERSHIP_END_DATE` |

#### Beispiel-Anfrage

```http
GET /api/members/by-date-range?startDate=2024-01-01&endDate=2024-12-31&dateType=MEMBERSHIP_START_DATE
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Fehler-Antworten

- **400 Bad Request**: Ungültiges Datumsformat oder Datumstyp

### 11. E-Mail-Eindeutigkeit validieren

```http
GET /api/members/validate/email/{email}
```

Prüft, ob eine E-Mail-Adresse bereits verwendet wird.

#### Pfad-Parameter

| Parameter | Typ | Beschreibung |
|-----------|-----|--------------|
| `email` | string | Zu prüfende E-Mail-Adresse |

#### Query-Parameter

| Parameter | Typ | Beschreibung |
|-----------|-----|--------------|
| `excludeMemberId` | UUID | Mitglieder-ID zum Ausschließen (für Updates) |

#### Beispiel-Anfrage

```http
GET /api/members/validate/email/test@example.com?excludeMemberId=123e4567-e89b-12d3-a456-426614174000
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Erfolgreiche Antwort (200 OK)

```json
{
  "data": {
    "isValid": true,
    "isUnique": false,
    "message": "Email address is already in use"
  },
  "success": true,
  "message": null,
  "errors": [],
  "timestamp": "2025-07-25T12:37:00Z"
}
```

### 12. Mitgliedsnummer-Eindeutigkeit validieren

```http
GET /api/members/validate/membership-number/{membershipNumber}
```

Prüft, ob eine Mitgliedsnummer bereits verwendet wird.

#### Pfad-Parameter

| Parameter | Typ | Beschreibung |
|-----------|-----|--------------|
| `membershipNumber` | string | Zu prüfende Mitgliedsnummer |

#### Query-Parameter

| Parameter | Typ | Beschreibung |
|-----------|-----|--------------|
| `excludeMemberId` | UUID | Mitglieder-ID zum Ausschließen (für Updates) |

## Datenmodelle

### Member (Mitglied)

```json
{
  "memberId": "UUID",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "phone": "string (optional)",
  "dateOfBirth": "string (YYYY-MM-DD, optional)",
  "membershipNumber": "string",
  "membershipStartDate": "string (YYYY-MM-DD)",
  "membershipEndDate": "string (YYYY-MM-DD, optional)",
  "isActive": "boolean",
  "address": "string (optional)",
  "emergencyContact": "string (optional)",
  "createdAt": "string (ISO 8601)",
  "updatedAt": "string (ISO 8601)"
}
```

### CreateMemberRequest

```json
{
  "firstName": "string (required)",
  "lastName": "string (required)",
  "email": "string (required)",
  "phone": "string (optional)",
  "dateOfBirth": "string (YYYY-MM-DD, optional)",
  "membershipNumber": "string (required)",
  "membershipStartDate": "string (YYYY-MM-DD, required)",
  "membershipEndDate": "string (YYYY-MM-DD, optional)",
  "isActive": "boolean (default: true)",
  "address": "string (optional)",
  "emergencyContact": "string (optional)"
}
```

### UpdateMemberRequest

Identisch mit `CreateMemberRequest`.

### MemberStats

```json
{
  "totalActive": "number",
  "totalMembers": "number"
}
```

## Validierungsregeln

### Pflichtfelder

- `firstName`: Nicht leer
- `lastName`: Nicht leer
- `email`: Gültige E-Mail-Adresse, eindeutig
- `membershipNumber`: Nicht leer, eindeutig
- `membershipStartDate`: Gültiges Datum

### Geschäftsregeln

- E-Mail-Adresse muss eindeutig sein
- Mitgliedsnummer muss eindeutig sein
- `membershipEndDate` muss nach `membershipStartDate` liegen (falls angegeben)
- Telefonnummer muss gültiges Format haben (falls angegeben)

## Fehlerbehandlung

### Validierungsfehler (422 Unprocessable Entity)

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

### Häufige Fehlercodes

| Code | Beschreibung |
|------|--------------|
| `MEMBER_NOT_FOUND` | Mitglied nicht gefunden |
| `INVALID_EMAIL` | Ungültige E-Mail-Adresse |
| `DUPLICATE_EMAIL` | E-Mail bereits vorhanden |
| `DUPLICATE_MEMBERSHIP_NUMBER` | Mitgliedsnummer bereits vorhanden |
| `INVALID_DATE_FORMAT` | Ungültiges Datumsformat |
| `INVALID_UUID_FORMAT` | Ungültiges UUID-Format |
| `MEMBERSHIP_DATE_CONFLICT` | Enddatum vor Startdatum |

## Beispiel-Workflows

### Neues Mitglied registrieren

1. **E-Mail validieren**: `GET /api/members/validate/email/{email}`
2. **Mitgliedsnummer validieren**: `GET /api/members/validate/membership-number/{membershipNumber}`
3. **Mitglied erstellen**: `POST /api/members`

### Mitglied aktualisieren

1. **Aktuelles Mitglied abrufen**: `GET /api/members/{id}`
2. **E-Mail validieren** (falls geändert): `GET /api/members/validate/email/{email}?excludeMemberId={id}`
3. **Mitglied aktualisieren**: `PUT /api/members/{id}`

### Ablaufende Mitgliedschaften verwalten

1. **Ablaufende Mitgliedschaften abrufen**: `GET /api/members/expiring-memberships?daysAhead=30`
2. **Für jedes Mitglied**: Benachrichtigung senden oder Verlängerung anbieten

## Rate Limiting

- **Authentifizierte Anfragen**: 1000 Anfragen/Stunde
- **Validierungs-Endpunkte**: 100 Anfragen/Minute (zusätzliches Limit)

## Caching

- **GET-Anfragen**: 5 Minuten Cache (außer Validierungs-Endpunkte)
- **Statistiken**: 15 Minuten Cache
- **Cache-Invalidierung**: Bei POST/PUT/DELETE-Operationen

---

**Letzte Aktualisierung**: 25. Juli 2025
**API-Version**: v1.0
**Controller-Version**: MemberController v1.0
