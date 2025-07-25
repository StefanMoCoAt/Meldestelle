# Migrationsplan für die Meldestelle-Projekt-Restrukturierung

Dieses Dokument beschreibt den Plan zur Migration von Code aus der alten Modulstruktur in die neue Modulstruktur, wie in den Projekt-Restrukturierungsanforderungen beschrieben.

## 1. Shared-Kernel zu Core-Modulen

### Core-Domain
- `shared-kernel/src/commonMain/kotlin/at/mocode/dto/base/BaseDto.kt` → `core/core-domain/src/main/kotlin/at/mocode/core/domain/model/`
- `shared-kernel/src/commonMain/kotlin/at/mocode/enums/Enums.kt` → `core/core-domain/src/main/kotlin/at/mocode/core/domain/model/`

### Core-Utils
- `shared-kernel/src/commonMain/kotlin/at/mocode/serializers/Serialization.kt` → `core/core-utils/src/main/kotlin/at/mocode/core/utils/serialization/`
- `shared-kernel/src/commonMain/kotlin/at/mocode/validation/ApiValidationUtils.kt` → `core/core-utils/src/main/kotlin/at/mocode/core/utils/validation/`
- `shared-kernel/src/commonMain/kotlin/at/mocode/validation/ValidationResult.kt` → `core/core-utils/src/main/kotlin/at/mocode/core/utils/validation/`
- `shared-kernel/src/commonMain/kotlin/at/mocode/validation/ValidationUtils.kt` → `core/core-utils/src/main/kotlin/at/mocode/core/utils/validation/`
- `shared-kernel/src/jvmMain/kotlin/at/mocode/shared/config/AppConfig.kt` → `core/core-utils/src/main/kotlin/at/mocode/core/utils/config/`
- `shared-kernel/src/jvmMain/kotlin/at/mocode/shared/config/AppEnvironment.kt` → `core/core-utils/src/main/kotlin/at/mocode/core/utils/config/`
- `shared-kernel/src/jvmMain/kotlin/at/mocode/shared/database/DatabaseConfig.kt` → `core/core-utils/src/main/kotlin/at/mocode/core/utils/database/`
- `shared-kernel/src/jvmMain/kotlin/at/mocode/shared/database/DatabaseFactory.kt` → `core/core-utils/src/main/kotlin/at/mocode/core/utils/database/`
- `shared-kernel/src/jvmMain/kotlin/at/mocode/shared/database/DatabaseMigrator.kt` → `core/core-utils/src/main/kotlin/at/mocode/core/utils/database/`
- `shared-kernel/src/jvmMain/kotlin/at/mocode/shared/discovery/ServiceRegistration.kt` → `core/core-utils/src/main/kotlin/at/mocode/core/utils/discovery/`

### Tests
- `shared-kernel/src/jvmTest/kotlin/at/mocode/shared/database/test/SimpleDatabaseTest.kt` → `core/core-utils/src/test/kotlin/at/mocode/core/utils/database/`
- `shared-kernel/src/jvmTest/kotlin/at/mocode/validation/test/ValidationTest.kt` → `core/core-utils/src/test/kotlin/at/mocode/core/utils/validation/`

## 2. Master-Data zu Masterdata-Modulen

### Masterdata-Domain
- `master-data/src/commonMain/kotlin/at/mocode/masterdata/domain/model/AltersklasseDefinition.kt` → `masterdata/masterdata-domain/src/main/kotlin/at/mocode/masterdata/domain/model/`
- `master-data/src/commonMain/kotlin/at/mocode/masterdata/domain/model/BundeslandDefinition.kt` → `masterdata/masterdata-domain/src/main/kotlin/at/mocode/masterdata/domain/model/`
- `master-data/src/commonMain/kotlin/at/mocode/masterdata/domain/model/LandDefinition.kt` → `masterdata/masterdata-domain/src/main/kotlin/at/mocode/masterdata/domain/model/`
- `master-data/src/commonMain/kotlin/at/mocode/masterdata/domain/model/Platz.kt` → `masterdata/masterdata-domain/src/main/kotlin/at/mocode/masterdata/domain/model/`
- `master-data/src/commonMain/kotlin/at/mocode/masterdata/domain/repository/LandRepository.kt` → `masterdata/masterdata-domain/src/main/kotlin/at/mocode/masterdata/domain/repository/`

### Masterdata-Application
- `master-data/src/commonMain/kotlin/at/mocode/masterdata/application/usecase/CreateCountryUseCase.kt` → `masterdata/masterdata-application/src/main/kotlin/at/mocode/masterdata/application/usecase/`
- `master-data/src/commonMain/kotlin/at/mocode/masterdata/application/usecase/GetCountryUseCase.kt` → `masterdata/masterdata-application/src/main/kotlin/at/mocode/masterdata/application/usecase/`

### Masterdata-Infrastructure
- `master-data/src/jvmMain/kotlin/at/mocode/masterdata/infrastructure/repository/LandRepositoryImpl.kt` → `masterdata/masterdata-infrastructure/src/main/kotlin/at/mocode/masterdata/infrastructure/persistence/`
- `master-data/src/jvmMain/kotlin/at/mocode/masterdata/infrastructure/repository/LandTable.kt` → `masterdata/masterdata-infrastructure/src/main/kotlin/at/mocode/masterdata/infrastructure/persistence/`
- `master-data/src/jvmMain/kotlin/at/mocode/masterdata/infrastructure/table/LandTable.kt` → `masterdata/masterdata-infrastructure/src/main/kotlin/at/mocode/masterdata/infrastructure/persistence/`

### Masterdata-API
- `master-data/src/jvmMain/kotlin/at/mocode/masterdata/infrastructure/api/CountryController.kt` → `masterdata/masterdata-api/src/main/kotlin/at/mocode/masterdata/api/rest/`

### Client UI
- `master-data/src/jsMain/kotlin/at/mocode/masterdata/ui/components/StammdatenListe.kt` → `client/common-ui/src/main/kotlin/at/mocode/client/common/components/masterdata/`

## 3. Member-Management zu Members-Modulen

### Members-Domain
- `member-management/src/commonMain/kotlin/at/mocode/members/domain/model/*.kt` → `members/members-domain/src/main/kotlin/at/mocode/members/domain/model/`
- `member-management/src/commonMain/kotlin/at/mocode/members/domain/repository/*.kt` → `members/members-domain/src/main/kotlin/at/mocode/members/domain/repository/`
- `member-management/src/commonMain/kotlin/at/mocode/members/domain/service/*.kt` → `members/members-domain/src/main/kotlin/at/mocode/members/domain/service/`
- `member-management/src/jvmMain/kotlin/at/mocode/members/domain/service/*.kt` → `members/members-domain/src/main/kotlin/at/mocode/members/domain/service/`

### Members-Application
- `member-management/src/commonMain/kotlin/at/mocode/members/application/usecase/*.kt` → `members/members-application/src/main/kotlin/at/mocode/members/application/usecase/`

### Members-Infrastructure
- `member-management/src/jvmMain/kotlin/at/mocode/members/infrastructure/repository/*.kt` → `members/members-infrastructure/src/main/kotlin/at/mocode/members/infrastructure/persistence/`
- `member-management/src/jvmMain/kotlin/at/mocode/members/infrastructure/table/*.kt` → `members/members-infrastructure/src/main/kotlin/at/mocode/members/infrastructure/persistence/`

### Client UI
- `member-management/src/jsMain/kotlin/at/mocode/members/ui/components/*.kt` → `client/common-ui/src/main/kotlin/at/mocode/client/common/components/members/`

## 4. Horse-Registry zu Horses-Modulen

### Horses-Domain
- `horse-registry/src/commonMain/kotlin/at/mocode/horses/domain/model/DomPferd.kt` → `horses/horses-domain/src/main/kotlin/at/mocode/horses/domain/model/`
- `horse-registry/src/commonMain/kotlin/at/mocode/horses/domain/repository/HorseRepository.kt` → `horses/horses-domain/src/main/kotlin/at/mocode/horses/domain/repository/`

### Horses-Application
- `horse-registry/src/commonMain/kotlin/at/mocode/horses/application/usecase/*.kt` → `horses/horses-application/src/main/kotlin/at/mocode/horses/application/usecase/`

### Horses-Infrastructure
- `horse-registry/src/jvmMain/kotlin/at/mocode/horses/infrastructure/repository/HorseRepositoryImpl.kt` → `horses/horses-infrastructure/src/main/kotlin/at/mocode/horses/infrastructure/persistence/`
- `horse-registry/src/jvmMain/kotlin/at/mocode/horses/infrastructure/repository/HorseTable.kt` → `horses/horses-infrastructure/src/main/kotlin/at/mocode/horses/infrastructure/persistence/`

### Horses-API
- `horse-registry/src/jvmMain/kotlin/at/mocode/horses/infrastructure/api/HorseController.kt` → `horses/horses-api/src/main/kotlin/at/mocode/horses/api/rest/`

### Client UI
- `horse-registry/src/jsMain/kotlin/at/mocode/horses/ui/components/PferdeListe.kt` → `client/common-ui/src/main/kotlin/at/mocode/client/common/components/horses/`

## 5. Event-Management zu Events-Modulen

### Events-Domain
- `event-management/src/commonMain/kotlin/at/mocode/events/domain/model/Veranstaltung.kt` → `events/events-domain/src/main/kotlin/at/mocode/events/domain/model/`
- `event-management/src/commonMain/kotlin/at/mocode/events/domain/repository/VeranstaltungRepository.kt` → `events/events-domain/src/main/kotlin/at/mocode/events/domain/repository/`
- `event-management/src/commonMain/kotlin/at/mocode/events/EventManagement.kt` → `events/events-domain/src/main/kotlin/at/mocode/events/`

### Events-Application
- `event-management/src/commonMain/kotlin/at/mocode/events/application/usecase/*.kt` → `events/events-application/src/main/kotlin/at/mocode/events/application/usecase/`

### Events-Infrastructure
- `event-management/src/jvmMain/kotlin/at/mocode/events/infrastructure/repository/VeranstaltungRepositoryImpl.kt` → `events/events-infrastructure/src/main/kotlin/at/mocode/events/infrastructure/persistence/`
- `event-management/src/jvmMain/kotlin/at/mocode/events/infrastructure/repository/VeranstaltungTable.kt` → `events/events-infrastructure/src/main/kotlin/at/mocode/events/infrastructure/persistence/`

### Events-API
- `event-management/src/jvmMain/kotlin/at/mocode/events/infrastructure/api/VeranstaltungController.kt` → `events/events-api/src/main/kotlin/at/mocode/events/api/rest/`

### Client UI
- `event-management/src/jsMain/kotlin/at/mocode/events/ui/components/VeranstaltungsListe.kt` → `client/common-ui/src/main/kotlin/at/mocode/client/common/components/events/`
- `event-management/src/jsMain/kotlin/at/mocode/events/ui/utils/EventComponent.kt` → `client/common-ui/src/main/kotlin/at/mocode/client/common/components/events/`

## 6. API-Gateway zu Infrastructure/Gateway

### Infrastructure/Gateway
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/Application.kt` → `infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/`
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/auth/*.kt` → `infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/auth/`
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/config/*.kt` → `infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/config/`
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/discovery/*.kt` → `infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/discovery/`
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/migrations/*.kt` → `infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/migrations/`
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/plugins/*.kt` → `infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/plugins/`
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/routing/*.kt` → `infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/routing/`
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/validation/*.kt` → `infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/validation/`
- `api-gateway/src/jvmMain/kotlin/at/mocode/gateway/module.kt` → `infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/`
- `api-gateway/src/jvmMain/resources/openapi/documentation.yaml` → `infrastructure/gateway/src/main/resources/openapi/`
- `api-gateway/src/jvmMain/resources/static/docs/*` → `infrastructure/gateway/src/main/resources/static/docs/`
- `api-gateway/src/test/kotlin/at/mocode/gateway/ApiIntegrationTest.kt` → `infrastructure/gateway/src/test/kotlin/at/mocode/infrastructure/gateway/`

## 7. ComposeApp zu Client-Modulen

### Client/Common-UI
- `composeApp/src/commonMain/kotlin/at/mocode/ui/theme/Theme.kt` → `client/common-ui/src/main/kotlin/at/mocode/client/common/theme/`
- `composeApp/src/commonMain/kotlin/at/mocode/di/AppDependencies.kt` → `client/common-ui/src/main/kotlin/at/mocode/client/common/di/`
- `composeApp/src/commonMain/kotlin/App.kt` → `client/common-ui/src/main/kotlin/at/mocode/client/common/`

### Client/Web-App
- `composeApp/src/commonMain/kotlin/at/mocode/ui/screens/*.kt` → `client/web-app/src/main/kotlin/at/mocode/client/web/screens/`
- `composeApp/src/commonMain/kotlin/at/mocode/ui/viewmodel/*.kt` → `client/web-app/src/main/kotlin/at/mocode/client/web/viewmodel/`
- `composeApp/src/jsMain/kotlin/main.kt` → `client/web-app/src/main/kotlin/at/mocode/client/web/`
- `composeApp/src/commonTest/kotlin/at/mocode/ui/viewmodel/*.kt` → `client/web-app/src/test/kotlin/at/mocode/client/web/viewmodel/`

### Client/Desktop-App
- `composeApp/src/desktopMain/kotlin/main.kt` → `client/desktop-app/src/main/kotlin/at/mocode/client/desktop/`

## Migrationsprozess

Für jede zu migrierende Datei:

1. Zielverzeichnis erstellen, falls es nicht existiert
2. Datei an den Zielort kopieren
3. Paket-Deklaration in der Datei entsprechend der neuen Paketstruktur aktualisieren
4. Imports entsprechend der neuen Paketstruktur aktualisieren
5. Alle Referenzen zu alten Modulnamen im Code aktualisieren

## Verifikation

Nach der Migration:

1. Build ausführen, um sicherzustellen, dass alle Module korrekt kompilieren
2. Tests ausführen, um die Funktionalität zu verifizieren
3. Verbleibende Migrationsaufgaben dokumentieren

---

**Letzte Aktualisierung**: 25. Juli 2025
