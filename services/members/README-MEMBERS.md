# Members Module

## Überblick

Das Members-Modul ist eine umfassende Lösung zur Verwaltung von Mitgliedern für Pferdesportorganisationen. Es implementiert eine saubere Architektur mit Domain-Driven Design und bietet vollständige CRUD-Operationen sowie erweiterte Geschäftslogik für die Mitgliederverwaltung.

## Funktionalität

### Verwaltete Entität

#### Mitglied (Member)

- **Persönliche Informationen**: Vor- und Nachname, E-Mail, Telefon, Geburtsdatum
- **Mitgliedschaftsinformationen**: Mitgliedsnummer, Start-/Enddatum, Aktivitätsstatus
- **Zusätzliche Informationen**: Adresse, Notfallkontakt
- **Audit-Felder**: Erstellungs- und Aktualisierungszeitstempel
- **Geschäftslogik**: Validierung, Mitgliedschaftsgültigkeit, Vollständiger Name

### Geschäftsoperationen

Das Modul bietet 18+ spezialisierte Repository-Operationen:

#### Basis-CRUD-Operationen

- `findById(id)` - Mitglied nach UUID suchen
- `save(member)` - Mitglied speichern (erstellen/aktualisieren)
- `delete(id)` - Mitglied löschen

#### Such-Operationen

- `findByMembershipNumber(number)` - Nach Mitgliedsnummer suchen
- `findByEmail(email)` - Nach E-Mail-Adresse suchen
- `findByName(searchTerm, limit)` - Nach Namen suchen (Teilübereinstimmung)
- `findAllActive(limit, offset)` - Alle aktiven Mitglieder
- `findAll(limit, offset)` - Alle Mitglieder (aktiv und inaktiv)

#### Datumsbasierte Abfragen

- `findByMembershipStartDateRange(start, end)` - Mitglieder nach Startdatum-Bereich
- `findByMembershipEndDateRange(start, end)` - Mitglieder nach Enddatum-Bereich
- `findMembersWithExpiringMembership(daysAhead)` - Mitglieder mit ablaufender Mitgliedschaft

#### Validierungs-Operationen

- `existsByMembershipNumber(number, excludeId)` - Prüfung auf doppelte Mitgliedsnummer
- `existsByEmail(email, excludeId)` - Prüfung auf doppelte E-Mail-Adresse

#### Zähl-Operationen

- `countActive()` - Anzahl aktiver Mitglieder
- `countAll()` - Gesamtanzahl aller Mitglieder

## Architektur

Das Modul folgt der Clean Architecture mit klarer Trennung der Verantwortlichkeiten:

```
members/
├── members-domain/              # Domain Layer
│   ├── model/                   # Domain Models
│   │   └── Member.kt           # Mitglied-Entität mit Geschäftslogik
│   ├── repository/             # Repository Interfaces
│   │   └── MemberRepository.kt # 18+ Geschäftsoperationen
│   └── events/                 # Domain Events
│       └── MemberEvents.kt     # Mitgliedschafts-Events
├── members-application/         # Application Layer
│   └── usecase/                # Use Cases
│       └── FindExpiringMembershipsUseCase.kt
├── members-infrastructure/      # Infrastructure Layer
│   ├── persistence/            # Database Implementation
│   │   ├── MemberRepositoryImpl.kt
│   │   └── MemberTable.kt
│   └── repository/             # Alternative Implementations
│       └── InMemoryMemberRepository.kt
├── members-api/                # API Layer
│   └── rest/                   # REST Controllers
│       └── MemberController.kt
└── members-service/            # Service Layer
    ├── MembersServiceApplication.kt
    └── test/                   # Integration Tests
        └── MemberServiceIntegrationTest.kt
```

### Domain Layer

- **1 Domain Model** mit reichhaltiger Geschäftslogik
- **1 Repository Interface** mit 18+ Geschäftsoperationen
- **Domain Events** für Mitgliedschaftsänderungen
- **Keine Abhängigkeiten** zu anderen Layern

### Application Layer

- **Use Cases** für komplexe Geschäftsoperationen
- **Orchestrierung** von Domain-Services
- **Anwendungslogik** ohne UI-Abhängigkeiten

### Infrastructure Layer

- **Datenbankzugriff** mit Exposed ORM
- **Repository-Implementierung** mit PostgreSQL
- **In-Memory-Repository** für Tests
- **Datenbankschema** und Migrationen

### API Layer

- **REST-Controller** für HTTP-Endpunkte
- **DTO-Mapping** zwischen Domain und API
- **Validierung** und Fehlerbehandlung

### Service Layer

- **Spring Boot Anwendung**
- **Dependency Injection** Konfiguration
- **Integrationstests**

## Domain Model Details

### Member-Entität

```kotlin
data class Member(
    val memberId: Uuid,

    // Persönliche Informationen
    var firstName: String,
    var lastName: String,
    var email: String,
    var phone: String? = null,
    var dateOfBirth: LocalDate? = null,

    // Mitgliedschaftsinformationen
    var membershipNumber: String,
    var membershipStartDate: LocalDate,
    var membershipEndDate: LocalDate? = null,
    var isActive: Boolean = true,

    // Zusätzliche Informationen
    var address: String? = null,
    var emergencyContact: String? = null,

    // Audit-Felder
    val createdAt: Instant,
    var updatedAt: Instant
)
```

### Geschäftslogik-Methoden

- `getFullName()` - Vollständiger Name des Mitglieds
- `isMembershipValid()` - Prüfung der Mitgliedschaftsgültigkeit
- `validate()` - Datenvalidierung mit Fehlerliste
- `withUpdatedTimestamp()` - Kopie mit aktualisiertem Zeitstempel

## Repository-Operationen

### Erweiterte Such-Features

```kotlin
// Mitglieder mit ablaufender Mitgliedschaft finden
val expiringMembers = memberRepository.findMembersWithExpiringMembership(30)

// Mitglieder nach Datumsbereich suchen
val newMembers = memberRepository.findByMembershipStartDateRange(
    startDate = LocalDate(2024, 1, 1),
    endDate = LocalDate(2024, 12, 31)
)

// Namenssuche mit Teilübereinstimmung
val searchResults = memberRepository.findByName("Schmidt", limit = 10)
```

### Validierung und Duplikatsprüfung

```kotlin
// Prüfung auf doppelte Mitgliedsnummer
val numberExists = memberRepository.existsByMembershipNumber("M2024001")

// Prüfung auf doppelte E-Mail (mit Ausschluss für Updates)
val emailExists = memberRepository.existsByEmail(
    email = "max@example.com",
    excludeMemberId = existingMember.memberId
)
```

## Use Cases

### FindExpiringMembershipsUseCase

Findet Mitglieder mit ablaufenden Mitgliedschaften und kann automatische Benachrichtigungen auslösen.

```kotlin
class FindExpiringMembershipsUseCase(
    private val memberRepository: MemberRepository
) {
    suspend fun execute(daysAhead: Int = 30): List<Member> {
        return memberRepository.findMembersWithExpiringMembership(daysAhead)
    }
}
```

## API-Endpunkte

Das Members-Modul stellt REST-Endpunkte über den MemberController bereit:

- `GET /api/members` - Alle aktiven Mitglieder abrufen
- `GET /api/members/{id}` - Mitglied nach ID abrufen
- `GET /api/members/search?name={name}` - Mitglieder nach Namen suchen
- `GET /api/members/expiring?days={days}` - Mitglieder mit ablaufender Mitgliedschaft
- `POST /api/members` - Neues Mitglied erstellen
- `PUT /api/members/{id}` - Mitglied aktualisieren
- `DELETE /api/members/{id}` - Mitglied löschen

## Konfiguration

### Datenbankschema

Das Modul verwendet eine `members`-Tabelle mit folgenden Spalten:

- `member_id` (UUID, Primary Key)
- `first_name`, `last_name`, `email` (Required)
- `phone`, `date_of_birth` (Optional)
- `membership_number` (Unique)
- `membership_start_date`, `membership_end_date`
- `is_active` (Boolean)
- `address`, `emergency_contact` (Optional)
- `created_at`, `updated_at` (Timestamps)

### Service-Konfiguration

```yaml
# application.yml
members:
  service:
    name: members-service
    port: 8082
  database:
    url: jdbc:postgresql://localhost:5432/meldestelle
    table: members
```

## Tests

### Integration Tests

Das Modul enthält umfassende Integrationstests:

```kotlin
@Test
fun `should find members with expiring membership`() {
    // Test-Implementierung für ablaufende Mitgliedschaften
}

@Test
fun `should validate unique membership number`() {
    // Test für Eindeutigkeit der Mitgliedsnummer
}
```

### Test-Datenbank

Verwendet H2 In-Memory-Datenbank für Tests mit automatischem Schema-Setup.

## Deployment

### Docker

```dockerfile
FROM openjdk:21-jre-slim
COPY members-service.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: members-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: members-service
  template:
    spec:
      containers:
      - name: members-service
        image: meldestelle/members-service:latest
        ports:
        - containerPort: 8082
```

## Monitoring

### Metriken

- Anzahl aktiver Mitglieder
- Anzahl ablaufender Mitgliedschaften
- API-Response-Zeiten
- Datenbankverbindungs-Pool

### Health Checks

- Datenbankverbindung
- Service-Verfügbarkeit
- Speicherverbrauch

## Entwicklung

### Lokale Entwicklung

```bash
# Service starten
./gradlew :members:members-service:bootRun

# Tests ausführen
./gradlew :members:test

# Integration Tests
./gradlew :members:members-service:test
```

### Code-Qualität

- **Kotlin Coding Standards**
- **100% Test Coverage** für Domain Layer
- **Integration Tests** für alle Use Cases
- **API-Dokumentation** mit OpenAPI

## Zukünftige Erweiterungen

1. **Mitgliedschaftstypen** - Verschiedene Mitgliedschaftskategorien
2. **Beitragsverwaltung** - Integration mit Zahlungssystem
3. **Mitgliedschaftshistorie** - Tracking von Änderungen
4. **Bulk-Operationen** - Massenimport/-export
5. **Benachrichtigungen** - Automatische E-Mail-Benachrichtigungen
6. **Reporting** - Mitgliedschaftsstatistiken und Reports

---

**Letzte Aktualisierung**: 25. Juli 2025

Für weitere Informationen zur Gesamtarchitektur siehe [README.md](../../README.md).
