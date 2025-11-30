# Events Module

## Überblick

Das Events-Modul ist eine umfassende Lösung zur Verwaltung von Pferdesportveranstaltungen. Es implementiert eine saubere Architektur mit Domain-Driven Design und bietet vollständige CRUD-Operationen sowie erweiterte Geschäftslogik für die Veranstaltungsplanung und -verwaltung.

## Funktionalität

### Verwaltete Entität

#### Veranstaltung (Event)

- **Grundinformationen**: Name, Beschreibung
- **Terminverwaltung**: Startdatum, Enddatum, Anmeldeschluss
- **Ort und Organisation**: Veranstaltungsort, Veranstalter-Verein-ID
- **Veranstaltungsdetails**: Sparten, Aktivitätsstatus, Öffentlichkeit, maximale Teilnehmerzahl
- **Audit-Felder**: Erstellungs- und Aktualisierungszeitstempel
- **Geschäftslogik**: Validierung, Anmeldestatus, Dauernberechnung

### Geschäftsoperationen

Das Modul bietet 10+ spezialisierte Repository-Operationen:

#### Basis-CRUD-Operationen

- `findById(id)` - Veranstaltung nach UUID suchen
- `save(veranstaltung)` - Veranstaltung speichern (erstellen/aktualisieren)
- `delete(id)` - Veranstaltung löschen

#### Such-Operationen

- `findByName(searchTerm, limit)` - Nach Namen suchen (Teilübereinstimmung)
- `findByVeranstalterVereinId(vereinId, activeOnly)` - Veranstaltungen eines Vereins
- `findAllActive(limit, offset)` - Alle aktiven Veranstaltungen
- `findPublicEvents(activeOnly)` - Öffentliche Veranstaltungen

#### Datumsbasierte Abfragen

- `findByDateRange(startDate, endDate, activeOnly)` - Veranstaltungen in Datumsbereich
- `findByStartDate(date, activeOnly)` - Veranstaltungen nach Startdatum

#### Zähl-Operationen

- `countActive()` - Anzahl aktiver Veranstaltungen
- `countByVeranstalterVereinId(vereinId, activeOnly)` - Anzahl Veranstaltungen pro Verein

## Architektur

Das Modul folgt der Clean Architecture mit klarer Trennung der Verantwortlichkeiten:

```
events/
├── events-domain/               # Domain Layer
│   ├── model/                   # Domain Models
│   │   └── Veranstaltung.kt    # Veranstaltungs-Entität mit Geschäftslogik
│   ├── repository/             # Repository Interfaces
│   │   └── VeranstaltungRepository.kt # 10+ Geschäftsoperationen
│   └── EventManagement.kt      # Domain Service/Facade
├── events-application/          # Application Layer
│   └── usecase/                # Use Cases
│       ├── CreateVeranstaltungUseCase.kt
│       ├── GetVeranstaltungUseCase.kt
│       ├── UpdateVeranstaltungUseCase.kt
│       └── DeleteVeranstaltungUseCase.kt
├── events-infrastructure/       # Infrastructure Layer
│   └── persistence/            # Database Implementation
│       ├── VeranstaltungRepositoryImpl.kt
│       └── VeranstaltungTable.kt
├── events-api/                 # API Layer
│   └── rest/                   # REST Controllers
│       └── VeranstaltungController.kt
└── events-service/             # Service Layer
    └── EventsServiceApplication.kt
```

### Domain Layer

- **1 Domain Model** mit reichhaltiger Geschäftslogik
- **1 Repository Interface** mit 10+ Geschäftsoperationen
- **Domain Service** für komplexe Veranstaltungslogik
- **Keine Abhängigkeiten** zu anderen Layern

### Application Layer

- **Use Cases** für CRUD-Operationen
- **Orchestrierung** von Domain-Services
- **Anwendungslogik** ohne UI-Abhängigkeiten

### Infrastructure Layer

- **Datenbankzugriff** mit Exposed ORM
- **Repository-Implementierung** mit PostgreSQL
- **Datenbankschema** und Migrationen

### API Layer

- **REST-Controller** für HTTP-Endpunkte
- **DTO-Mapping** zwischen Domain und API
- **Validierung** und Fehlerbehandlung

### Service Layer

- **Spring Boot Anwendung**
- **Dependency Injection** Konfiguration
- **Service-Konfiguration**

## Domain Model Details

### Veranstaltung-Entität

```kotlin
data class Veranstaltung(
    val veranstaltungId: Uuid,

    // Grundinformationen
    var name: String,
    var beschreibung: String? = null,

    // Termine
    var startDatum: LocalDate,
    var endDatum: LocalDate,

    // Ort und Organisation
    var ort: String,
    var veranstalterVereinId: Uuid,

    // Veranstaltungsdetails
    var sparten: List<SparteE> = emptyList(),
    var istAktiv: Boolean = true,
    var istOeffentlich: Boolean = true,
    var maxTeilnehmer: Int? = null,
    var anmeldeschluss: LocalDate? = null,

    // Audit-Felder
    val createdAt: Instant,
    var updatedAt: Instant
)
```

### Geschäftslogik-Methoden

- `isRegistrationOpen()` - Prüfung ob Anmeldung noch möglich ist
- `getDurationInDays()` - Berechnung der Veranstaltungsdauer in Tagen
- `isMultiDay()` - Prüfung ob mehrtägige Veranstaltung
- `validate()` - Datenvalidierung mit Fehlerliste
- `withUpdatedTimestamp()` - Kopie mit aktualisiertem Zeitstempel

### Enumerationen

#### SparteE (Sportsparten)

- `DRESSUR` - Dressurreiten
- `SPRINGEN` - Springreiten
- `VIELSEITIGKEIT` - Vielseitigkeitsreiten
- `FAHREN` - Fahrsport
- `VOLTIGIEREN` - Voltigieren
- `WESTERN` - Westernreiten
- `DISTANZ` - Distanzreiten

## Repository-Operationen

### Erweiterte Such-Features

```kotlin
// Veranstaltungen nach Namen suchen
val events = veranstaltungRepository.findByName("Turnier", limit = 10)

// Veranstaltungen eines Vereins finden
val clubEvents = veranstaltungRepository.findByVeranstalterVereinId(
    vereinId = clubId,
    activeOnly = true
)

// Veranstaltungen in Datumsbereich suchen
val summerEvents = veranstaltungRepository.findByDateRange(
    startDate = LocalDate(2024, 6, 1),
    endDate = LocalDate(2024, 8, 31),
    activeOnly = true
)

// Öffentliche Veranstaltungen finden
val publicEvents = veranstaltungRepository.findPublicEvents(activeOnly = true)
```

### Datumsbasierte Abfragen

```kotlin
// Veranstaltungen an einem bestimmten Tag
val todayEvents = veranstaltungRepository.findByStartDate(
    date = LocalDate.now(),
    activeOnly = true
)

// Alle aktiven Veranstaltungen
val activeEvents = veranstaltungRepository.findAllActive(limit = 100)
```

### Statistiken und Zählungen

```kotlin
// Anzahl aktiver Veranstaltungen
val totalActive = veranstaltungRepository.countActive()

// Anzahl Veranstaltungen pro Verein
val clubEventCount = veranstaltungRepository.countByVeranstalterVereinId(
    vereinId = clubId,
    activeOnly = true
)
```

## Use Cases

### CreateVeranstaltungUseCase

Erstellt eine neue Veranstaltung mit Validierung und Geschäftsregeln.

```kotlin
class CreateVeranstaltungUseCase(
    private val veranstaltungRepository: VeranstaltungRepository
) {
    suspend fun execute(veranstaltung: Veranstaltung): Veranstaltung {
        // Validierung
        val errors = veranstaltung.validate()
        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        // Geschäftsregeln prüfen
        if (veranstaltung.anmeldeschluss != null &&
            veranstaltung.anmeldeschluss!! > veranstaltung.startDatum) {
            throw BusinessRuleException("Anmeldeschluss muss vor Veranstaltungsbeginn liegen")
        }

        return veranstaltungRepository.save(veranstaltung)
    }
}
```

### GetVeranstaltungUseCase

Ruft Veranstaltungsinformationen ab mit verschiedenen Suchkriterien.

### UpdateVeranstaltungUseCase

Aktualisiert Veranstaltungsinformationen mit Validierung.

### DeleteVeranstaltungUseCase

Löscht eine Veranstaltung (soft delete durch Deaktivierung).

## API-Endpunkte

Das Events-Modul stellt REST-Endpunkte über den VeranstaltungController bereit:

- `GET /api/events` - Alle aktiven Veranstaltungen abrufen
- `GET /api/events/{id}` - Veranstaltung nach ID abrufen
- `GET /api/events/search?name={name}` - Veranstaltungen nach Namen suchen
- `GET /api/events/club/{clubId}` - Veranstaltungen eines Vereins
- `GET /api/events/public` - Öffentliche Veranstaltungen
- `GET /api/events/date-range?start={start}&end={end}` - Veranstaltungen in Datumsbereich
- `GET /api/events/date/{date}` - Veranstaltungen an einem bestimmten Tag
- `POST /api/events` - Neue Veranstaltung erstellen
- `PUT /api/events/{id}` - Veranstaltung aktualisieren
- `DELETE /api/events/{id}` - Veranstaltung löschen

## Konfiguration

### Datenbankschema

Das Modul verwendet eine `events`-Tabelle mit folgenden Spalten:

- `veranstaltung_id` (UUID, Primary Key)
- `name` (Required)
- `beschreibung` (Text, Optional)
- `start_datum`, `end_datum` (Date, Required)
- `ort` (Required)
- `veranstalter_verein_id` (UUID, Foreign Key)
- `sparten` (JSON Array)
- `ist_aktiv`, `ist_oeffentlich` (Boolean)
- `max_teilnehmer` (Integer, Optional)
- `anmeldeschluss` (Date, Optional)
- `created_at`, `updated_at` (Timestamps)

### Service-Konfiguration

```yaml
# application.yml
events:
  service:
    name: events-service
    port: 8084
  database:
    url: jdbc:postgresql://localhost:5432/meldestelle
    table: events
  business-rules:
    max-duration-days: 30
    min-registration-period-days: 7
    allow-past-events: false
```

## Tests

### Integration Tests

Das Modul enthält umfassende Integrationstests:

```kotlin
@Test
fun `should create event with valid data`() {
    // Test für Veranstaltungserstellung
}

@Test
fun `should find events by date range`() {
    // Test für datumsbasierte Suche
}

@Test
fun `should validate registration deadline`() {
    // Test für Anmeldeschluss-Validierung
}

@Test
fun `should find public events only`() {
    // Test für öffentliche Veranstaltungen
}
```

### Test-Datenbank

Verwendet H2 In-Memory-Datenbank für Tests mit automatischem Schema-Setup.

## Deployment

### Docker

```dockerfile
FROM openjdk:21-jre-slim
COPY events-service.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: events-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: events-service
  template:
    spec:
      containers:
      - name: events-service
        image: meldestelle/events-service:latest
        ports:
        - containerPort: 8084
```

## Monitoring

### Metriken

- Anzahl aktiver Veranstaltungen
- Anzahl öffentlicher Veranstaltungen
- Durchschnittliche Veranstaltungsdauer
- API-Response-Zeiten
- Datenbankverbindungs-Pool
- Validierungsfehler-Rate

### Health Checks

- Datenbankverbindung
- Service-Verfügbarkeit
- Speicherverbrauch
- Externe System-Verbindungen

## Entwicklung

### Lokale Entwicklung

```bash
# Service starten
./gradlew :events:events-service:bootRun

# Tests ausführen
./gradlew :events:test

# Integration Tests
./gradlew :events:events-service:test
```

### Code-Qualität

- **Kotlin Coding Standards**
- **100% Test Coverage** für Domain Layer
- **Integration Tests** für alle Use Cases
- **API-Dokumentation** mit OpenAPI

## Geschäftsregeln

### Veranstaltungsplanung

1. **Datumsvalidierung**: Enddatum muss nach oder gleich Startdatum sein
2. **Anmeldeschluss**: Muss vor Veranstaltungsbeginn liegen
3. **Teilnehmerbegrenzung**: Maximale Teilnehmerzahl muss positiv sein
4. **Öffentlichkeit**: Private Veranstaltungen nur für Vereinsmitglieder

### Sparten-Management

- Unterstützung für alle österreichischen Pferdesport-Sparten
- Mehrfachauswahl möglich für kombinierte Veranstaltungen
- Sparten-spezifische Validierungsregeln

### Vereins-Integration

- Verknüpfung mit Vereinsverwaltung
- Berechtigung zur Veranstaltungserstellung
- Vereins-spezifische Konfigurationen

## Integration

### Externe Systeme

#### OEPS-Integration

- Synchronisation mit OEPS-Veranstaltungskalender
- Automatische Meldung bei OEPS-relevanten Veranstaltungen
- Import von OEPS-Veranstaltungsdaten

#### FEI-Integration

- Unterstützung für internationale Veranstaltungen
- FEI-Regularien und -Standards
- Automatische Klassifizierung

### Interne Module

#### Members-Modul

- Teilnehmerverwaltung
- Anmeldestatus-Tracking
- Mitgliedschaftsvalidierung

#### Horses-Modul

- Pferdeanmeldungen
- Eignung für Sparten
- Registrierungsstatus

## Zukünftige Erweiterungen

1. **Anmeldungssystem** - Vollständiges Teilnehmeranmeldungssystem
2. **Zeitplanung** - Detaillierte Zeitpläne und Startlisten
3. **Ergebniserfassung** - Integration mit Bewertungssystemen
4. **Livestreaming** - Integration mit Streaming-Plattformen
5. **Mobile App** - Mobile Anwendung für Teilnehmer
6. **Zahlungsintegration** - Startgebühren und Zahlungsabwicklung
7. **Wetterintegration** - Wettervorhersage und -warnungen
8. **Kapazitätsmanagement** - Stallplätze und Parkplätze
9. **Catering-Management** - Verpflegung und Bewirtung
10. **Sponsoring** - Sponsoren-Management und -präsentation

---

**Letzte Aktualisierung**: 25. Juli 2025

Für weitere Informationen zur Gesamtarchitektur siehe [README.md](../../README.md).
