#!/bin/bash

# Migration script for Meldestelle Project
# This script implements the migration plan as described in docs/migration-plan.md

set -e  # Exit on error
echo "Starting migration process..."

# Function to create directory if it doesn't exist
create_dir() {
    if [ ! -d "$1" ]; then
        mkdir -p "$1"
        echo "Created directory: $1"
    fi
}

# Function to copy file and update package
copy_and_update() {
    local src="$1"
    local dest="$2"
    local old_pkg="$3"
    local new_pkg="$4"

    if [ ! -f "$src" ]; then
        echo "Warning: Source file not found: $src"
        return
    fi

    # Create destination directory
    create_dir "$(dirname "$dest")"

    # Copy file
    cp "$src" "$dest"
    echo "Copied: $src -> $dest"

    # Update package declaration if provided
    if [ -n "$old_pkg" ] && [ -n "$new_pkg" ]; then
        sed -i "s/package $old_pkg/package $new_pkg/" "$dest"
        echo "Updated package: $old_pkg -> $new_pkg in $dest"
    fi
}

echo "1. Migrating Shared-Kernel to Core Modules"

# Core-Domain
copy_and_update "shared-kernel/src/commonMain/kotlin/at/mocode/dto/base/BaseDto.kt" \
                "core/core-domain/src/main/kotlin/at/mocode/core/domain/model/BaseDto.kt" \
                "at.mocode.dto.base" \
                "at.mocode.core.domain.model"

copy_and_update "shared-kernel/src/commonMain/kotlin/at/mocode/enums/Enums.kt" \
                "core/core-domain/src/main/kotlin/at/mocode/core/domain/model/Enums.kt" \
                "at.mocode.enums" \
                "at.mocode.core.domain.model"

# Core-Utils
copy_and_update "shared-kernel/src/commonMain/kotlin/at/mocode/serializers/Serialization.kt" \
                "core/core-utils/src/main/kotlin/at/mocode/core/utils/serialization/Serialization.kt" \
                "at.mocode.serializers" \
                "at.mocode.core.utils.serialization"

copy_and_update "shared-kernel/src/commonMain/kotlin/at/mocode/validation/ApiValidationUtils.kt" \
                "core/core-utils/src/main/kotlin/at/mocode/core/utils/validation/ApiValidationUtils.kt" \
                "at.mocode.validation" \
                "at.mocode.core.utils.validation"

copy_and_update "shared-kernel/src/commonMain/kotlin/at/mocode/validation/ValidationResult.kt" \
                "core/core-utils/src/main/kotlin/at/mocode/core/utils/validation/ValidationResult.kt" \
                "at.mocode.validation" \
                "at.mocode.core.utils.validation"

copy_and_update "shared-kernel/src/commonMain/kotlin/at/mocode/validation/ValidationUtils.kt" \
                "core/core-utils/src/main/kotlin/at/mocode/core/utils/validation/ValidationUtils.kt" \
                "at.mocode.validation" \
                "at.mocode.core.utils.validation"

copy_and_update "shared-kernel/src/jvmMain/kotlin/at/mocode/shared/config/AppConfig.kt" \
                "core/core-utils/src/main/kotlin/at/mocode/core/utils/config/AppConfig.kt" \
                "at.mocode.shared.config" \
                "at.mocode.core.utils.config"

copy_and_update "shared-kernel/src/jvmMain/kotlin/at/mocode/shared/config/AppEnvironment.kt" \
                "core/core-utils/src/main/kotlin/at/mocode/core/utils/config/AppEnvironment.kt" \
                "at.mocode.shared.config" \
                "at.mocode.core.utils.config"

copy_and_update "shared-kernel/src/jvmMain/kotlin/at/mocode/shared/database/DatabaseConfig.kt" \
                "core/core-utils/src/main/kotlin/at/mocode/core/utils/database/DatabaseConfig.kt" \
                "at.mocode.shared.database" \
                "at.mocode.core.utils.database"

copy_and_update "shared-kernel/src/jvmMain/kotlin/at/mocode/shared/database/DatabaseFactory.kt" \
                "core/core-utils/src/main/kotlin/at/mocode/core/utils/database/DatabaseFactory.kt" \
                "at.mocode.shared.database" \
                "at.mocode.core.utils.database"

copy_and_update "shared-kernel/src/jvmMain/kotlin/at/mocode/shared/database/DatabaseMigrator.kt" \
                "core/core-utils/src/main/kotlin/at/mocode/core/utils/database/DatabaseMigrator.kt" \
                "at.mocode.shared.database" \
                "at.mocode.core.utils.database"

copy_and_update "shared-kernel/src/jvmMain/kotlin/at/mocode/shared/discovery/ServiceRegistration.kt" \
                "core/core-utils/src/main/kotlin/at/mocode/core/utils/discovery/ServiceRegistration.kt" \
                "at.mocode.shared.discovery" \
                "at.mocode.core.utils.discovery"

# Tests
copy_and_update "shared-kernel/src/jvmTest/kotlin/at/mocode/shared/database/test/SimpleDatabaseTest.kt" \
                "core/core-utils/src/test/kotlin/at/mocode/core/utils/database/SimpleDatabaseTest.kt" \
                "at.mocode.shared.database.test" \
                "at.mocode.core.utils.database"

copy_and_update "shared-kernel/src/jvmTest/kotlin/at/mocode/validation/test/ValidationTest.kt" \
                "core/core-utils/src/test/kotlin/at/mocode/core/utils/validation/ValidationTest.kt" \
                "at.mocode.validation.test" \
                "at.mocode.core.utils.validation"

echo "2. Migrating Master-Data to Masterdata Modules"

# Masterdata-Domain
copy_and_update "master-data/src/commonMain/kotlin/at/mocode/masterdata/domain/model/AltersklasseDefinition.kt" \
                "masterdata/masterdata-domain/src/main/kotlin/at/mocode/masterdata/domain/model/AltersklasseDefinition.kt" \
                "at.mocode.masterdata.domain.model" \
                "at.mocode.masterdata.domain.model"

copy_and_update "master-data/src/commonMain/kotlin/at/mocode/masterdata/domain/model/BundeslandDefinition.kt" \
                "masterdata/masterdata-domain/src/main/kotlin/at/mocode/masterdata/domain/model/BundeslandDefinition.kt" \
                "at.mocode.masterdata.domain.model" \
                "at.mocode.masterdata.domain.model"

copy_and_update "master-data/src/commonMain/kotlin/at/mocode/masterdata/domain/model/LandDefinition.kt" \
                "masterdata/masterdata-domain/src/main/kotlin/at/mocode/masterdata/domain/model/LandDefinition.kt" \
                "at.mocode.masterdata.domain.model" \
                "at.mocode.masterdata.domain.model"

copy_and_update "master-data/src/commonMain/kotlin/at/mocode/masterdata/domain/model/Platz.kt" \
                "masterdata/masterdata-domain/src/main/kotlin/at/mocode/masterdata/domain/model/Platz.kt" \
                "at.mocode.masterdata.domain.model" \
                "at.mocode.masterdata.domain.model"

copy_and_update "master-data/src/commonMain/kotlin/at/mocode/masterdata/domain/repository/LandRepository.kt" \
                "masterdata/masterdata-domain/src/main/kotlin/at/mocode/masterdata/domain/repository/LandRepository.kt" \
                "at.mocode.masterdata.domain.repository" \
                "at.mocode.masterdata.domain.repository"

# Masterdata-Application
copy_and_update "master-data/src/commonMain/kotlin/at/mocode/masterdata/application/usecase/CreateCountryUseCase.kt" \
                "masterdata/masterdata-application/src/main/kotlin/at/mocode/masterdata/application/usecase/CreateCountryUseCase.kt" \
                "at.mocode.masterdata.application.usecase" \
                "at.mocode.masterdata.application.usecase"

copy_and_update "master-data/src/commonMain/kotlin/at/mocode/masterdata/application/usecase/GetCountryUseCase.kt" \
                "masterdata/masterdata-application/src/main/kotlin/at/mocode/masterdata/application/usecase/GetCountryUseCase.kt" \
                "at.mocode.masterdata.application.usecase" \
                "at.mocode.masterdata.application.usecase"

# Masterdata-Infrastructure
copy_and_update "master-data/src/jvmMain/kotlin/at/mocode/masterdata/infrastructure/repository/LandRepositoryImpl.kt" \
                "masterdata/masterdata-infrastructure/src/main/kotlin/at/mocode/masterdata/infrastructure/persistence/LandRepositoryImpl.kt" \
                "at.mocode.masterdata.infrastructure.repository" \
                "at.mocode.masterdata.infrastructure.persistence"

copy_and_update "master-data/src/jvmMain/kotlin/at/mocode/masterdata/infrastructure/repository/LandTable.kt" \
                "masterdata/masterdata-infrastructure/src/main/kotlin/at/mocode/masterdata/infrastructure/persistence/LandTable.kt" \
                "at.mocode.masterdata.infrastructure.repository" \
                "at.mocode.masterdata.infrastructure.persistence"

copy_and_update "master-data/src/jvmMain/kotlin/at/mocode/masterdata/infrastructure/table/LandTable.kt" \
                "masterdata/masterdata-infrastructure/src/main/kotlin/at/mocode/masterdata/infrastructure/persistence/LandTable.kt" \
                "at.mocode.masterdata.infrastructure.table" \
                "at.mocode.masterdata.infrastructure.persistence"

# Masterdata-API
copy_and_update "master-data/src/jvmMain/kotlin/at/mocode/masterdata/infrastructure/api/CountryController.kt" \
                "masterdata/masterdata-api/src/main/kotlin/at/mocode/masterdata/api/rest/CountryController.kt" \
                "at.mocode.masterdata.infrastructure.api" \
                "at.mocode.masterdata.api.rest"

# Client UI
copy_and_update "master-data/src/jsMain/kotlin/at/mocode/masterdata/ui/components/StammdatenListe.kt" \
                "client/common-ui/src/main/kotlin/at/mocode/client/common/components/masterdata/StammdatenListe.kt" \
                "at.mocode.masterdata.ui.components" \
                "at.mocode.client.common.components.masterdata"

echo "3. Migrating Member-Management to Members Modules"

# Members-Domain (using wildcards for directories with multiple files)
for file in master-data/src/commonMain/kotlin/at/mocode/members/domain/model/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "members/members-domain/src/main/kotlin/at/mocode/members/domain/model/$filename" \
                        "at.mocode.members.domain.model" \
                        "at.mocode.members.domain.model"
    fi
done

for file in master-data/src/commonMain/kotlin/at/mocode/members/domain/repository/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "members/members-domain/src/main/kotlin/at/mocode/members/domain/repository/$filename" \
                        "at.mocode.members.domain.repository" \
                        "at.mocode.members.domain.repository"
    fi
done

for file in master-data/src/commonMain/kotlin/at/mocode/members/domain/service/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "members/members-domain/src/main/kotlin/at/mocode/members/domain/service/$filename" \
                        "at.mocode.members.domain.service" \
                        "at.mocode.members.domain.service"
    fi
done

for file in master-data/src/jvmMain/kotlin/at/mocode/members/domain/service/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "members/members-domain/src/main/kotlin/at/mocode/members/domain/service/$filename" \
                        "at.mocode.members.domain.service" \
                        "at.mocode.members.domain.service"
    fi
done

# Members-Application
for file in master-data/src/commonMain/kotlin/at/mocode/members/application/usecase/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "members/members-application/src/main/kotlin/at/mocode/members/application/usecase/$filename" \
                        "at.mocode.members.application.usecase" \
                        "at.mocode.members.application.usecase"
    fi
done

# Members-Infrastructure
for file in master-data/src/jvmMain/kotlin/at/mocode/members/infrastructure/repository/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "members/members-infrastructure/src/main/kotlin/at/mocode/members/infrastructure/persistence/$filename" \
                        "at.mocode.members.infrastructure.repository" \
                        "at.mocode.members.infrastructure.persistence"
    fi
done

for file in master-data/src/jvmMain/kotlin/at/mocode/members/infrastructure/table/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "members/members-infrastructure/src/main/kotlin/at/mocode/members/infrastructure/persistence/$filename" \
                        "at.mocode.members.infrastructure.table" \
                        "at.mocode.members.infrastructure.persistence"
    fi
done

# Client UI
for file in master-data/src/jsMain/kotlin/at/mocode/members/ui/components/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "client/common-ui/src/main/kotlin/at/mocode/client/common/components/members/$filename" \
                        "at.mocode.members.ui.components" \
                        "at.mocode.client.common.components.members"
    fi
done

echo "4. Migrating Horse-Registry to Horses Modules"

# Horses-Domain
copy_and_update "horse-registry/src/commonMain/kotlin/at/mocode/horses/domain/model/DomPferd.kt" \
                "horses/horses-domain/src/main/kotlin/at/mocode/horses/domain/model/DomPferd.kt" \
                "at.mocode.horses.domain.model" \
                "at.mocode.horses.domain.model"

copy_and_update "horse-registry/src/commonMain/kotlin/at/mocode/horses/domain/repository/HorseRepository.kt" \
                "horses/horses-domain/src/main/kotlin/at/mocode/horses/domain/repository/HorseRepository.kt" \
                "at.mocode.horses.domain.repository" \
                "at.mocode.horses.domain.repository"

# Horses-Application
for file in horse-registry/src/commonMain/kotlin/at/mocode/horses/application/usecase/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "horses/horses-application/src/main/kotlin/at/mocode/horses/application/usecase/$filename" \
                        "at.mocode.horses.application.usecase" \
                        "at.mocode.horses.application.usecase"
    fi
done

# Horses-Infrastructure
copy_and_update "horse-registry/src/jvmMain/kotlin/at/mocode/horses/infrastructure/repository/HorseRepositoryImpl.kt" \
                "horses/horses-infrastructure/src/main/kotlin/at/mocode/horses/infrastructure/persistence/HorseRepositoryImpl.kt" \
                "at.mocode.horses.infrastructure.repository" \
                "at.mocode.horses.infrastructure.persistence"

copy_and_update "horse-registry/src/jvmMain/kotlin/at/mocode/horses/infrastructure/repository/HorseTable.kt" \
                "horses/horses-infrastructure/src/main/kotlin/at/mocode/horses/infrastructure/persistence/HorseTable.kt" \
                "at.mocode.horses.infrastructure.repository" \
                "at.mocode.horses.infrastructure.persistence"

# Horses-API
copy_and_update "horse-registry/src/jvmMain/kotlin/at/mocode/horses/infrastructure/api/HorseController.kt" \
                "horses/horses-api/src/main/kotlin/at/mocode/horses/api/rest/HorseController.kt" \
                "at.mocode.horses.infrastructure.api" \
                "at.mocode.horses.api.rest"

# Client UI
copy_and_update "horse-registry/src/jsMain/kotlin/at/mocode/horses/ui/components/PferdeListe.kt" \
                "client/common-ui/src/main/kotlin/at/mocode/client/common/components/horses/PferdeListe.kt" \
                "at.mocode.horses.ui.components" \
                "at.mocode.client.common.components.horses"

echo "5. Migrating Event-Management to Events Modules"

# Events-Domain
copy_and_update "event-management/src/commonMain/kotlin/at/mocode/events/domain/model/Veranstaltung.kt" \
                "events/events-domain/src/main/kotlin/at/mocode/events/domain/model/Veranstaltung.kt" \
                "at.mocode.events.domain.model" \
                "at.mocode.events.domain.model"

copy_and_update "event-management/src/commonMain/kotlin/at/mocode/events/domain/repository/VeranstaltungRepository.kt" \
                "events/events-domain/src/main/kotlin/at/mocode/events/domain/repository/VeranstaltungRepository.kt" \
                "at.mocode.events.domain.repository" \
                "at.mocode.events.domain.repository"

copy_and_update "event-management/src/commonMain/kotlin/at/mocode/events/EventManagement.kt" \
                "events/events-domain/src/main/kotlin/at/mocode/events/EventManagement.kt" \
                "at.mocode.events" \
                "at.mocode.events"

# Events-Application
for file in event-management/src/commonMain/kotlin/at/mocode/events/application/usecase/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "events/events-application/src/main/kotlin/at/mocode/events/application/usecase/$filename" \
                        "at.mocode.events.application.usecase" \
                        "at.mocode.events.application.usecase"
    fi
done

# Events-Infrastructure
copy_and_update "event-management/src/jvmMain/kotlin/at/mocode/events/infrastructure/repository/VeranstaltungRepositoryImpl.kt" \
                "events/events-infrastructure/src/main/kotlin/at/mocode/events/infrastructure/persistence/VeranstaltungRepositoryImpl.kt" \
                "at.mocode.events.infrastructure.repository" \
                "at.mocode.events.infrastructure.persistence"

copy_and_update "event-management/src/jvmMain/kotlin/at/mocode/events/infrastructure/repository/VeranstaltungTable.kt" \
                "events/events-infrastructure/src/main/kotlin/at/mocode/events/infrastructure/persistence/VeranstaltungTable.kt" \
                "at.mocode.events.infrastructure.repository" \
                "at.mocode.events.infrastructure.persistence"

# Events-API
copy_and_update "event-management/src/jvmMain/kotlin/at/mocode/events/infrastructure/api/VeranstaltungController.kt" \
                "events/events-api/src/main/kotlin/at/mocode/events/api/rest/VeranstaltungController.kt" \
                "at.mocode.events.infrastructure.api" \
                "at.mocode.events.api.rest"

# Client UI
copy_and_update "event-management/src/jsMain/kotlin/at/mocode/events/ui/components/VeranstaltungsListe.kt" \
                "client/common-ui/src/main/kotlin/at/mocode/client/common/components/events/VeranstaltungsListe.kt" \
                "at.mocode.events.ui.components" \
                "at.mocode.client.common.components.events"

copy_and_update "event-management/src/jsMain/kotlin/at/mocode/events/ui/utils/EventComponent.kt" \
                "client/common-ui/src/main/kotlin/at/mocode/client/common/components/events/EventComponent.kt" \
                "at.mocode.events.ui.utils" \
                "at.mocode.client.common.components.events"

echo "6. Migrating API-Gateway to Infrastructure/Gateway"

# Infrastructure/Gateway
copy_and_update "api-gateway/src/jvmMain/kotlin/at/mocode/gateway/Application.kt" \
                "infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/Application.kt" \
                "at.mocode.gateway" \
                "at.mocode.infrastructure.gateway"

# Copy auth directory
for file in api-gateway/src/jvmMain/kotlin/at/mocode/gateway/auth/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/auth/$filename" \
                        "at.mocode.gateway.auth" \
                        "at.mocode.infrastructure.gateway.auth"
    fi
done

# Copy config directory
for file in api-gateway/src/jvmMain/kotlin/at/mocode/gateway/config/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/config/$filename" \
                        "at.mocode.gateway.config" \
                        "at.mocode.infrastructure.gateway.config"
    fi
done

# Copy discovery directory
for file in api-gateway/src/jvmMain/kotlin/at/mocode/gateway/discovery/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/discovery/$filename" \
                        "at.mocode.gateway.discovery" \
                        "at.mocode.infrastructure.gateway.discovery"
    fi
done

# Copy migrations directory
for file in api-gateway/src/jvmMain/kotlin/at/mocode/gateway/migrations/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/migrations/$filename" \
                        "at.mocode.gateway.migrations" \
                        "at.mocode.infrastructure.gateway.migrations"
    fi
done

# Copy plugins directory
for file in api-gateway/src/jvmMain/kotlin/at/mocode/gateway/plugins/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/plugins/$filename" \
                        "at.mocode.gateway.plugins" \
                        "at.mocode.infrastructure.gateway.plugins"
    fi
done

# Copy routing directory
for file in api-gateway/src/jvmMain/kotlin/at/mocode/gateway/routing/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/routing/$filename" \
                        "at.mocode.gateway.routing" \
                        "at.mocode.infrastructure.gateway.routing"
    fi
done

# Copy validation directory
for file in api-gateway/src/jvmMain/kotlin/at/mocode/gateway/validation/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/validation/$filename" \
                        "at.mocode.gateway.validation" \
                        "at.mocode.infrastructure.gateway.validation"
    fi
done

copy_and_update "api-gateway/src/jvmMain/kotlin/at/mocode/gateway/module.kt" \
                "infrastructure/gateway/src/main/kotlin/at/mocode/infrastructure/gateway/module.kt" \
                "at.mocode.gateway" \
                "at.mocode.infrastructure.gateway"

# Copy resources
create_dir "infrastructure/gateway/src/main/resources/openapi"
cp -r api-gateway/src/jvmMain/resources/openapi/* infrastructure/gateway/src/main/resources/openapi/ 2>/dev/null || echo "No openapi resources to copy"

create_dir "infrastructure/gateway/src/main/resources/static/docs"
cp -r api-gateway/src/jvmMain/resources/static/docs/* infrastructure/gateway/src/main/resources/static/docs/ 2>/dev/null || echo "No static docs to copy"

# Copy tests
copy_and_update "api-gateway/src/test/kotlin/at/mocode/gateway/ApiIntegrationTest.kt" \
                "infrastructure/gateway/src/test/kotlin/at/mocode/infrastructure/gateway/ApiIntegrationTest.kt" \
                "at.mocode.gateway" \
                "at.mocode.infrastructure.gateway"

echo "7. Migrating ComposeApp to Client Modules"

# Client/Common-UI
copy_and_update "composeApp/src/commonMain/kotlin/at/mocode/ui/theme/Theme.kt" \
                "client/common-ui/src/main/kotlin/at/mocode/client/common/theme/Theme.kt" \
                "at.mocode.ui.theme" \
                "at.mocode.client.common.theme"

copy_and_update "composeApp/src/commonMain/kotlin/at/mocode/di/AppDependencies.kt" \
                "client/common-ui/src/main/kotlin/at/mocode/client/common/di/AppDependencies.kt" \
                "at.mocode.di" \
                "at.mocode.client.common.di"

copy_and_update "composeApp/src/commonMain/kotlin/App.kt" \
                "client/common-ui/src/main/kotlin/at/mocode/client/common/App.kt" \
                "" \
                "at.mocode.client.common"

# Client/Web-App
for file in composeApp/src/commonMain/kotlin/at/mocode/ui/screens/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "client/web-app/src/main/kotlin/at/mocode/client/web/screens/$filename" \
                        "at.mocode.ui.screens" \
                        "at.mocode.client.web.screens"
    fi
done

for file in composeApp/src/commonMain/kotlin/at/mocode/ui/viewmodel/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "client/web-app/src/main/kotlin/at/mocode/client/web/viewmodel/$filename" \
                        "at.mocode.ui.viewmodel" \
                        "at.mocode.client.web.viewmodel"
    fi
done

copy_and_update "composeApp/src/jsMain/kotlin/main.kt" \
                "client/web-app/src/main/kotlin/at/mocode/client/web/main.kt" \
                "" \
                "at.mocode.client.web"

# Copy tests
for file in composeApp/src/commonTest/kotlin/at/mocode/ui/viewmodel/*.kt; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        copy_and_update "$file" \
                        "client/web-app/src/test/kotlin/at/mocode/client/web/viewmodel/$filename" \
                        "at.mocode.ui.viewmodel" \
                        "at.mocode.client.web.viewmodel"
    fi
done

# Client/Desktop-App
copy_and_update "composeApp/src/desktopMain/kotlin/main.kt" \
                "client/desktop-app/src/main/kotlin/at/mocode/client/desktop/main.kt" \
                "" \
                "at.mocode.client.desktop"

echo "Migration completed successfully!"
echo "Note: You may need to manually update imports in the migrated files to reflect the new package structure."
echo "Run a build to verify the migration."
