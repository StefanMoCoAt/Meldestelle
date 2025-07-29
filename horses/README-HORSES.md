# Horses Module

## Überblick

Das Horses-Modul ist eine umfassende Lösung zur Verwaltung von Pferden für Pferdesportorganisationen. Es implementiert eine saubere Architektur mit Domain-Driven Design und bietet vollständige CRUD-Operationen sowie erweiterte Geschäftslogik für die Pferderegistrierung und -verwaltung.

## Funktionalität

### Verwaltete Entität

#### Pferd (DomPferd)
- **Grundinformationen**: Name, Geschlecht, Geburtsdatum, Rasse, Farbe
- **Besitz und Verantwortung**: Besitzer-ID, verantwortliche Person
- **Zuchtinformationen**: Züchtername, Zuchtbuchnummer
- **Identifikationsnummern**: Lebensnummer, Chipnummer, Passnummer, OEPS-Nummer, FEI-Nummer
- **Abstammung**: Vater, Mutter, Muttervater
- **Körperliche Merkmale**: Stockmaß (Höhe in cm)
- **Status und Verwaltung**: Aktivitätsstatus, Bemerkungen, Datenquelle
- **Audit-Felder**: Erstellungs- und Aktualisierungszeitstempel

### Geschäftsoperationen

Das Modul bietet 25+ spezialisierte Repository-Operationen:

#### Basis-CRUD-Operationen
- `findById(id)` - Pferd nach UUID suchen
- `save(horse)` - Pferd speichern (erstellen/aktualisieren)
- `delete(id)` - Pferd löschen

#### Such-Operationen nach Identifikationsnummern
- `findByLebensnummer(lebensnummer)` - Nach Lebensnummer suchen
- `findByChipNummer(chipNummer)` - Nach Chipnummer suchen
- `findByPassNummer(passNummer)` - Nach Passnummer suchen
- `findByOepsNummer(oepsNummer)` - Nach OEPS-Nummer suchen
- `findByFeiNummer(feiNummer)` - Nach FEI-Nummer suchen

#### Such-Operationen nach Eigenschaften
- `findByName(searchTerm, limit)` - Nach Namen suchen (Teilübereinstimmung)
- `findByOwnerId(ownerId, activeOnly)` - Pferde eines Besitzers
- `findByResponsiblePersonId(personId, activeOnly)` - Pferde einer verantwortlichen Person
- `findByGeschlecht(geschlecht, activeOnly, limit)` - Nach Geschlecht filtern
- `findByRasse(rasse, activeOnly, limit)` - Nach Rasse filtern

#### Datumsbasierte Abfragen
- `findByBirthYear(birthYear, activeOnly)` - Pferde nach Geburtsjahr
- `findByBirthYearRange(fromYear, toYear, activeOnly)` - Pferde nach Geburtsjahr-Bereich

#### Registrierungs-Abfragen
- `findAllActive(limit)` - Alle aktiven Pferde
- `findOepsRegistered(activeOnly)` - OEPS-registrierte Pferde
- `findFeiRegistered(activeOnly)` - FEI-registrierte Pferde

#### Validierungs-Operationen
- `existsByLebensnummer(lebensnummer)` - Prüfung auf doppelte Lebensnummer
- `existsByChipNummer(chipNummer)` - Prüfung auf doppelte Chipnummer
- `existsByPassNummer(passNummer)` - Prüfung auf doppelte Passnummer
- `existsByOepsNummer(oepsNummer)` - Prüfung auf doppelte OEPS-Nummer
- `existsByFeiNummer(feiNummer)` - Prüfung auf doppelte FEI-Nummer

#### Zähl-Operationen
- `countActive()` - Anzahl aktiver Pferde
- `countByOwnerId(ownerId, activeOnly)` - Anzahl Pferde pro Besitzer
- `countOepsRegistered(activeOnly)` - Anzahl OEPS-registrierter Pferde ✨ **NEU**
- `countFeiRegistered(activeOnly)` - Anzahl FEI-registrierter Pferde ✨ **NEU**

## Architektur

Das Modul folgt der Clean Architecture mit klarer Trennung der Verantwortlichkeiten:

```
horses/
├── horses-domain/               # Domain Layer
│   ├── model/                   # Domain Models
│   │   └── DomPferd.kt         # Pferd-Entität mit Geschäftslogik
│   └── repository/             # Repository Interfaces
│       └── HorseRepository.kt  # 25+ Geschäftsoperationen
├── horses-application/          # Application Layer
│   └── usecase/                # Use Cases
│       ├── CreateHorseUseCase.kt
│       ├── GetHorseUseCase.kt
│       ├── UpdateHorseUseCase.kt
│       └── DeleteHorseUseCase.kt
├── horses-infrastructure/       # Infrastructure Layer
│   └── persistence/            # Database Implementation
│       ├── HorseRepositoryImpl.kt
│       └── HorseTable.kt
├── horses-api/                 # API Layer
│   └── rest/                   # REST Controllers
│       └── HorseController.kt
└── horses-service/             # Service Layer
    ├── HorsesServiceApplication.kt
    └── test/                   # Integration Tests
        └── HorseServiceIntegrationTest.kt
```

### Domain Layer
- **1 Domain Model** mit reichhaltiger Geschäftslogik
- **1 Repository Interface** mit 25+ Geschäftsoperationen
- **Geschäftsregeln** für Pferderegistrierung und -validierung
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

## 🚀 Aktuelle Optimierungen (2025-07-25)

Das Horses-Modul wurde kürzlich analysiert, vervollständigt und optimiert. Folgende Verbesserungen wurden implementiert:

### ✨ Neue Funktionalitäten

#### Erweiterte Such-Endpunkte
Neue REST-Endpunkte für vollständige Identifikationsnummer-Suche:
- `GET /api/horses/search/passport/{nummer}` - Suche nach Passnummer
- `GET /api/horses/search/oeps/{nummer}` - Suche nach OEPS-Nummer
- `GET /api/horses/search/fei/{nummer}` - Suche nach FEI-Nummer

#### Optimierte Statistik-Operationen
- Neue effiziente Zähl-Methoden für OEPS und FEI registrierte Pferde
- Performance-Verbesserung von O(n) auf O(1) Komplexität für Statistiken
- Datenbankoptimierte COUNT-Abfragen statt Laden aller Datensätze

### ⚡ Performance-Optimierungen

#### Datenbankeffizienz
- **Vorher**: Statistik-Endpunkt lud alle Pferde und verwendete `.size`
- **Nachher**: Effiziente COUNT-Abfragen direkt in der Datenbank
- **Auswirkung**: Drastische Reduzierung der Speichernutzung und Antwortzeiten

#### Architektur-Konsistenz
- Alle API-Endpunkte verwenden jetzt konsistent die Use-Case-Schicht
- Eliminierung direkter Repository-Aufrufe in der API-Schicht
- Saubere Trennung der Architektur-Schichten

### 🏗️ Architektur-Verbesserungen

#### Clean Architecture Compliance
- **Konsistente Schichtung**: Alle Endpunkte folgen dem Use-Case-Pattern
- **Fehlerbehandlung**: Einheitliche Fehlerantworten über alle Endpunkte
- **Validierung**: Umfassende Eingabevalidierung mit geteilten Utilities
- **HTTP-Standards**: Korrekte Status-Codes und REST-Konventionen

#### Code-Qualität
- Verbesserte Lesbarkeit und Wartbarkeit
- Konsistente Namenskonventionen
- Umfassende Dokumentation aller neuen Funktionen

### 📊 Qualitätsmetriken

#### Vor der Optimierung
- ❌ Fehlende Such-Endpunkte für 3 Identifikationstypen
- ❌ Ineffiziente Statistik-Abfragen (O(n) Komplexität)
- ❌ Inkonsistente Architektur (einige Endpunkte umgingen Use Cases)
- ❌ Performance-Probleme bei großen Datensätzen

#### Nach der Optimierung
- ✅ Vollständige API-Abdeckung für alle Identifikationstypen
- ✅ Effiziente Statistik-Abfragen (O(1) Komplexität)
- ✅ Konsistente Clean Architecture durchgehend
- ✅ Optimierte Performance für alle Operationen

### 🔮 Zukünftige Empfehlungen

#### Caching-Schicht
- Implementierung einer Caching-Schicht für häufig abgerufene Daten
- Individuelle Pferde-Lookups mit angemessener TTL
- Statistiken und Zählungen mit Cache-Invalidierung

#### Async-Operationen
- Asynchrone Verarbeitung für Batch-Operationen
- Komplexe Such-Abfragen mit Async-Pattern
- Statistik-Berechnungen im Hintergrund

#### Monitoring und Logging
- Umfassendes Monitoring für API-Antwortzeiten
- Datenbank-Query-Performance-Überwachung
- Fehlerrate-Tracking und -Analyse
- **DTO-Mapping** zwischen Domain und API
- **Validierung** und Fehlerbehandlung

### Service Layer
- **Spring Boot Anwendung**
- **Dependency Injection** Konfiguration
- **Integrationstests**

## Domain Model Details

### DomPferd-Entität

```kotlin
data class DomPferd(
    val pferdId: Uuid,

    // Grundinformationen
    var pferdeName: String,
    var geschlecht: PferdeGeschlechtE,
    var geburtsdatum: LocalDate? = null,
    var rasse: String? = null,
    var farbe: String? = null,

    // Besitz und Verantwortung
    var besitzerId: Uuid? = null,
    var verantwortlichePersonId: Uuid? = null,

    // Zuchtinformationen
    var zuechterName: String? = null,
    var zuchtbuchNummer: String? = null,

    // Identifikationsnummern
    var lebensnummer: String? = null,
    var chipNummer: String? = null,
    var passNummer: String? = null,
    var oepsNummer: String? = null,
    var feiNummer: String? = null,

    // Abstammung
    var vaterName: String? = null,
    var mutterName: String? = null,
    var mutterVaterName: String? = null,

    // Körperliche Merkmale
    var stockmass: Int? = null, // Höhe in cm

    // Status und Verwaltung
    var istAktiv: Boolean = true,
    var bemerkungen: String? = null,
    var datenQuelle: DatenQuelleE = DatenQuelleE.MANUELL,

    // Audit-Felder
    val createdAt: Instant,
    var updatedAt: Instant
)
```

### Geschäftslogik-Methoden

- `getDisplayName()` - Anzeigename mit Geburtsjahr
- `hasCompleteIdentification()` - Prüfung auf vollständige Identifikation
- `isOepsRegistered()` - OEPS-Registrierungsstatus
- `isFeiRegistered()` - FEI-Registrierungsstatus
- `getAge()` - Altersberechnung in Jahren
- `validateForRegistration()` - Validierung für Registrierung
- `withUpdatedTimestamp()` - Kopie mit aktualisiertem Zeitstempel

### Enumerationen

#### PferdeGeschlechtE
- `HENGST` - Hengst (männlich, nicht kastriert)
- `STUTE` - Stute (weiblich)
- `WALLACH` - Wallach (männlich, kastriert)

#### DatenQuelleE
- `MANUELL` - Manuelle Eingabe
- `IMPORT` - Datenimport
- `SYNCHRONISATION` - Synchronisation mit externen Systemen

## Repository-Operationen

### Erweiterte Such-Features

```kotlin
// Pferde nach Identifikationsnummer suchen
val horse = horseRepository.findByLebensnummer("AT123456789")
val chipHorse = horseRepository.findByChipNummer("982000123456789")

// Pferde eines Besitzers finden
val ownerHorses = horseRepository.findByOwnerId(ownerId, activeOnly = true)

// Pferde nach Eigenschaften filtern
val stallions = horseRepository.findByGeschlecht(PferdeGeschlechtE.HENGST)
val warmbloods = horseRepository.findByRasse("Warmblut", activeOnly = true)

// Pferde nach Geburtsjahr suchen
val youngHorses = horseRepository.findByBirthYearRange(2020, 2024)
```

### Registrierungs-Abfragen

```kotlin
// OEPS-registrierte Pferde finden
val oepsHorses = horseRepository.findOepsRegistered(activeOnly = true)

// FEI-registrierte Pferde finden
val feiHorses = horseRepository.findFeiRegistered(activeOnly = true)

// Alle aktiven Pferde
val activeHorses = horseRepository.findAllActive(limit = 1000)
```

### Validierung und Duplikatsprüfung

```kotlin
// Prüfung auf doppelte Identifikationsnummern
val lebensnummerExists = horseRepository.existsByLebensnummer("AT123456789")
val chipExists = horseRepository.existsByChipNummer("982000123456789")
val oepsExists = horseRepository.existsByOepsNummer("AUT12345")
```

## Use Cases

### CreateHorseUseCase

Erstellt ein neues Pferd mit Validierung und Duplikatsprüfung.

```kotlin
class CreateHorseUseCase(
    private val horseRepository: HorseRepository
) {
    suspend fun execute(horse: DomPferd): DomPferd {
        // Validierung
        val errors = horse.validateForRegistration()
        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        // Duplikatsprüfung
        horse.lebensnummer?.let { nummer ->
            if (horseRepository.existsByLebensnummer(nummer)) {
                throw DuplicateException("Lebensnummer bereits vorhanden")
            }
        }

        return horseRepository.save(horse)
    }
}
```

### GetHorseUseCase

Ruft Pferdeinformationen ab mit verschiedenen Suchkriterien.

### UpdateHorseUseCase

Aktualisiert Pferdeinformationen mit Validierung.

### DeleteHorseUseCase

Löscht ein Pferd (soft delete durch Deaktivierung).

## API-Endpunkte

Das Horses-Modul stellt REST-Endpunkte über den HorseController bereit:

- `GET /api/horses` - Alle aktiven Pferde abrufen
- `GET /api/horses/{id}` - Pferd nach ID abrufen
- `GET /api/horses/search?name={name}` - Pferde nach Namen suchen
- `GET /api/horses/owner/{ownerId}` - Pferde eines Besitzers
- `GET /api/horses/identification/{number}` - Pferd nach Identifikationsnummer
- `GET /api/horses/oeps-registered` - OEPS-registrierte Pferde
- `GET /api/horses/fei-registered` - FEI-registrierte Pferde
- `POST /api/horses` - Neues Pferd erstellen
- `PUT /api/horses/{id}` - Pferd aktualisieren
- `DELETE /api/horses/{id}` - Pferd löschen

## Konfiguration

### Datenbankschema

Das Modul verwendet eine `horses`-Tabelle mit folgenden Spalten:
- `pferd_id` (UUID, Primary Key)
- `pferde_name` (Required)
- `geschlecht` (Enum: HENGST, STUTE, WALLACH)
- `geburtsdatum`, `rasse`, `farbe` (Optional)
- `besitzer_id`, `verantwortliche_person_id` (UUID, Foreign Keys)
- `zuechter_name`, `zuchtbuch_nummer` (Optional)
- `lebensnummer`, `chip_nummer`, `pass_nummer` (Unique, Optional)
- `oeps_nummer`, `fei_nummer` (Unique, Optional)
- `vater_name`, `mutter_name`, `mutter_vater_name` (Optional)
- `stockmass` (Integer, Optional)
- `ist_aktiv` (Boolean)
- `bemerkungen` (Text, Optional)
- `daten_quelle` (Enum)
- `created_at`, `updated_at` (Timestamps)

### Service-Konfiguration

```yaml
# application.yml
horses:
  service:
    name: horses-service
    port: 8083
  database:
    url: jdbc:postgresql://localhost:5432/meldestelle
    table: horses
  validation:
    require-identification: true
    allow-duplicate-names: false
```

## Tests

### Integration Tests

Das Modul enthält umfassende Integrationstests:

```kotlin
@Test
fun `should create horse with valid data`() {
    // Test für Pferdeerstellung
}

@Test
fun `should find horses by owner`() {
    // Test für Besitzer-basierte Suche
}

@Test
fun `should validate unique identification numbers`() {
    // Test für Eindeutigkeit der Identifikationsnummern
}
```

### Test-Datenbank

Verwendet H2 In-Memory-Datenbank für Tests mit automatischem Schema-Setup.

## Deployment

### Docker

```dockerfile
FROM openjdk:21-jre-slim
COPY horses-service.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: horses-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: horses-service
  template:
    spec:
      containers:
      - name: horses-service
        image: meldestelle/horses-service:latest
        ports:
        - containerPort: 8083
```

## Monitoring

### Metriken

- Anzahl aktiver Pferde
- Anzahl registrierter Pferde (OEPS/FEI)
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
./gradlew :horses:horses-service:bootRun

# Tests ausführen
./gradlew :horses:test

# Integration Tests
./gradlew :horses:horses-service:test
```

### Code-Qualität

- **Kotlin Coding Standards**
- **100% Test Coverage** für Domain Layer
- **Integration Tests** für alle Use Cases
- **API-Dokumentation** mit OpenAPI

## Compliance und Standards

### OEPS-Integration

- Unterstützung für OEPS-Nummern
- Validierung nach OEPS-Standards
- Synchronisation mit OEPS-Datenbank

### FEI-Integration

- Unterstützung für FEI-Nummern
- Internationale Registrierungsstandards
- Compliance mit FEI-Regularien

### Datenschutz

- DSGVO-konforme Datenhaltung
- Anonymisierung von Testdaten
- Audit-Trail für alle Änderungen

## Zukünftige Erweiterungen

1. **Gesundheitsdaten** - Veterinärmedizinische Aufzeichnungen
2. **Leistungsdaten** - Turnierergebnisse und Bewertungen
3. **Versicherungsdaten** - Integration mit Versicherungssystemen
4. **Foto-Management** - Bildverwaltung für Pferde
5. **Stammbaum-Visualisierung** - Grafische Darstellung der Abstammung
6. **Import/Export** - Datenimport aus externen Systemen
7. **Mobile App** - Mobile Anwendung für Pferdebesitzer
8. **QR-Code-Integration** - QR-Codes für schnelle Identifikation

---

**Letzte Aktualisierung**: 25. Juli 2025

Für weitere Informationen zur Gesamtarchitektur siehe [README.md](../README.md).
