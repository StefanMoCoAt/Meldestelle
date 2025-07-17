# Module Structure Design für Self-Contained Systems

## Neue Projektstruktur

```
Meldestelle/
├── shared-kernel/                    # Gemeinsame Basis-Komponenten
│   ├── src/commonMain/kotlin/at/mocode/
│   │   ├── enums/                   # Gemeinsame Enums
│   │   ├── serializers/             # Gemeinsame Serializer
│   │   ├── validation/              # Basis-Validatoren
│   │   └── dto/base/                # Basis-DTOs
│   └── build.gradle.kts
│
├── member-management/               # Bounded Context 1
│   ├── src/
│   │   ├── commonMain/kotlin/at/mocode/members/
│   │   │   ├── domain/
│   │   │   │   ├── model/           # DomPerson, DomVerein
│   │   │   │   ├── repository/      # Repository Interfaces
│   │   │   │   └── service/         # Domain Services
│   │   │   ├── application/
│   │   │   │   ├── dto/             # Member-spezifische DTOs
│   │   │   │   └── usecase/         # Use Cases
│   │   │   └── infrastructure/
│   │   │       ├── repository/      # Repository Implementierungen
│   │   │       └── api/             # REST Controllers
│   │   └── test/
│   └── build.gradle.kts
│
├── horse-registry/                  # Bounded Context 2
│   ├── src/
│   │   ├── commonMain/kotlin/at/mocode/horses/
│   │   │   ├── domain/
│   │   │   │   ├── model/           # DomPferd
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   └── usecase/
│   │   │   └── infrastructure/
│   │   │       ├── repository/
│   │   │       └── api/
│   │   └── test/
│   └── build.gradle.kts
│
├── license-management/              # Bounded Context 3
│   ├── src/
│   │   ├── commonMain/kotlin/at/mocode/licenses/
│   │   │   ├── domain/
│   │   │   │   ├── model/           # DomLizenz, DomQualifikation
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   └── usecase/
│   │   │   └── infrastructure/
│   │   │       ├── repository/
│   │   │       └── api/
│   │   └── test/
│   └── build.gradle.kts
│
├── event-management/                # Bounded Context 4
│   ├── src/
│   │   ├── commonMain/kotlin/at/mocode/events/
│   │   │   ├── domain/
│   │   │   │   ├── model/           # Turnier, Veranstaltung
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   └── usecase/
│   │   │   └── infrastructure/
│   │   │       ├── repository/
│   │   │       └── api/
│   │   └── test/
│   └── build.gradle.kts
│
├── master-data/                     # Bounded Context 5
│   ├── src/
│   │   ├── commonMain/kotlin/at/mocode/masterdata/
│   │   │   ├── domain/
│   │   │   │   ├── model/           # LandDefinition, BundeslandDefinition
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   └── usecase/
│   │   │   └── infrastructure/
│   │   │       ├── repository/
│   │   │       └── api/
│   │   └── test/
│   └── build.gradle.kts
│
├── data-integration/                # Bounded Context 6
│   ├── src/
│   │   ├── commonMain/kotlin/at/mocode/integration/
│   │   │   ├── domain/
│   │   │   │   ├── model/           # ZNS_Staging Models
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   └── usecase/
│   │   │   └── infrastructure/
│   │   │       ├── repository/
│   │   │       └── api/
│   │   └── test/
│   └── build.gradle.kts
│
├── competition-management/          # Bounded Context 7
│   ├── src/
│   │   ├── commonMain/kotlin/at/mocode/competitions/
│   │   │   ├── domain/
│   │   │   │   ├── model/           # Bewerb, Abteilung, Spezifika
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   └── usecase/
│   │   │   └── infrastructure/
│   │   │       ├── repository/
│   │   │       └── api/
│   │   └── test/
│   └── build.gradle.kts
│
├── api-gateway/                     # API Gateway für einheitliche Schnittstelle
│   ├── src/main/kotlin/at/mocode/gateway/
│   │   ├── config/                  # Gateway-Konfiguration
│   │   ├── routing/                 # Route-Aggregation
│   │   └── security/                # Authentifizierung/Autorisierung
│   └── build.gradle.kts
│
├── composeApp/                      # Frontend (unverändert)
└── settings.gradle.kts              # Aktualisiert für neue Module
```

## Architektur-Prinzipien

### 1. Hexagonal Architecture pro Context
Jeder Bounded Context folgt der Hexagonal Architecture:
- **Domain**: Geschäftslogik und Entitäten
- **Application**: Use Cases und DTOs
- **Infrastructure**: Technische Implementierung

### 2. Dependency Inversion
- Domain Layer hat keine Abhängigkeiten zu anderen Layern
- Infrastructure implementiert Domain Interfaces
- Application orchestriert Domain Services

### 3. Clean Boundaries
- Contexts kommunizieren nur über definierte APIs
- Keine direkten Abhängigkeiten zwischen Domain Models
- DTOs für Context-übergreifende Kommunikation

## Inter-Context Communication

### 1. Synchrone Kommunikation
```kotlin
// Beispiel: Member Management ruft Master Data auf
interface CountryService {
    suspend fun getCountryById(id: Uuid): CountryDto?
}

// Implementation im API Gateway
class CountryServiceImpl : CountryService {
    override suspend fun getCountryById(id: Uuid): CountryDto? {
        return masterDataClient.getCountry(id)
    }
}
```

### 2. Asynchrone Kommunikation
```kotlin
// Domain Events für lose Kopplung
sealed class DomainEvent {
    data class PersonCreated(val personId: Uuid, val data: PersonDto) : DomainEvent()
    data class HorseRegistered(val horseId: Uuid, val ownerId: Uuid) : DomainEvent()
    data class LicenseExpired(val licenseId: Uuid, val personId: Uuid) : DomainEvent()
}

// Event Bus für Context-übergreifende Events
interface EventBus {
    suspend fun publish(event: DomainEvent)
    fun subscribe(handler: (DomainEvent) -> Unit)
}
```

### 3. Shared Kernel
```
shared-kernel/src/commonMain/kotlin/at/mocode/
├── enums/
│   ├── DatenQuelleE.kt
│   ├── GeschlechtE.kt
│   └── PferdeGeschlechtE.kt
├── dto/base/
│   ├── BaseDto.kt
│   └── ErrorDto.kt
├── serializers/
│   ├── UuidSerializer.kt
│   └── KotlinInstantSerializer.kt
└── validation/
    ├── ValidationResult.kt
    └── BaseValidator.kt
```

## Migration Strategy

### Phase 1: Shared Kernel Setup
1. Erstelle `shared-kernel` Modul
2. Verschiebe gemeinsame Enums und Serializer
3. Definiere Basis-DTOs und Validatoren

### Phase 2: Master Data Context
1. Erstelle `master-data` Modul (keine Abhängigkeiten)
2. Verschiebe Stammdaten-Models
3. Implementiere Repository und API

### Phase 3: Core Contexts
1. `member-management` (abhängig von master-data)
2. `horse-registry` (abhängig von member-management)
3. `license-management` (abhängig von member-management)

### Phase 4: Business Contexts
1. `event-management`
2. `competition-management`
3. `data-integration`

### Phase 5: API Gateway
1. Implementiere Gateway für einheitliche API
2. Konfiguriere Routing zu Contexts
3. Implementiere Authentifizierung

## Deployment Options

### Option 1: Monolithic Deployment
- Alle Contexts in einer Anwendung
- Einfache Entwicklung und Deployment
- Shared Database

### Option 2: Modular Monolith
- Separate JARs pro Context
- Gemeinsame Runtime
- Context-spezifische Schemas

### Option 3: Microservices
- Separate Services pro Context
- Unabhängige Deployment
- Separate Datenbanken

## Vorteile der neuen Struktur

1. **Klare Verantwortlichkeiten**: Jeder Context hat einen definierten Zweck
2. **Lose Kopplung**: Contexts sind nur über APIs verbunden
3. **Hohe Kohäsion**: Verwandte Funktionalität ist zusammengefasst
4. **Testbarkeit**: Jeder Context kann isoliert getestet werden
5. **Skalierbarkeit**: Contexts können unabhängig skaliert werden
6. **Team-Autonomie**: Teams können an verschiedenen Contexts arbeiten
