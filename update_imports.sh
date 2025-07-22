#!/bin/bash

# Script to update imports in migrated files
# This script updates import statements from the old package structure to the new package structure

set -e  # Exit on error
echo "Starting import update process..."

# Function to update imports in a file
update_imports() {
    local file="$1"
    echo "Updating imports in $file"

    # Update shared-kernel imports
    sed -i 's/import at\.mocode\.shared\.config\./import at.mocode.core.utils.config./g' "$file"
    sed -i 's/import at\.mocode\.shared\.database\./import at.mocode.core.utils.database./g' "$file"
    sed -i 's/import at\.mocode\.shared\.discovery\./import at.mocode.core.utils.discovery./g' "$file"
    sed -i 's/import at\.mocode\.serializers\./import at.mocode.core.domain.serialization./g' "$file"
    sed -i 's/import at\.mocode\.validation\./import at.mocode.core.utils.validation./g' "$file"
    sed -i 's/import at\.mocode\.dto\.base\./import at.mocode.core.domain.model./g' "$file"
    sed -i 's/import at\.mocode\.enums\./import at.mocode.core.domain.model./g' "$file"

    # Update master-data imports
    sed -i 's/import at\.mocode\.masterdata\.infrastructure\.repository\./import at.mocode.masterdata.infrastructure.persistence./g' "$file"
    sed -i 's/import at\.mocode\.masterdata\.infrastructure\.table\./import at.mocode.masterdata.infrastructure.persistence./g' "$file"
    sed -i 's/import at\.mocode\.masterdata\.infrastructure\.api\./import at.mocode.masterdata.api.rest./g' "$file"

    # Update member-management imports
    sed -i 's/import at\.mocode\.members\.infrastructure\.repository\./import at.mocode.members.infrastructure.persistence./g' "$file"
    sed -i 's/import at\.mocode\.members\.infrastructure\.table\./import at.mocode.members.infrastructure.persistence./g' "$file"
    sed -i 's/import at\.mocode\.members\.infrastructure\.api\./import at.mocode.members.api.rest./g' "$file"

    # Update horse-registry imports
    sed -i 's/import at\.mocode\.horses\.infrastructure\.repository\./import at.mocode.horses.infrastructure.persistence./g' "$file"
    sed -i 's/import at\.mocode\.horses\.infrastructure\.table\./import at.mocode.horses.infrastructure.persistence./g' "$file"
    sed -i 's/import at\.mocode\.horses\.infrastructure\.api\./import at.mocode.horses.api.rest./g' "$file"

    # Update event-management imports
    sed -i 's/import at\.mocode\.events\.infrastructure\.repository\./import at.mocode.events.infrastructure.persistence./g' "$file"
    sed -i 's/import at\.mocode\.events\.infrastructure\.table\./import at.mocode.events.infrastructure.persistence./g' "$file"
    sed -i 's/import at\.mocode\.events\.infrastructure\.api\./import at.mocode.events.api.rest./g' "$file"

    # Update api-gateway imports
    sed -i 's/import at\.mocode\.gateway\./import at.mocode.infrastructure.gateway./g' "$file"

    # Update composeApp imports
    sed -i 's/import at\.mocode\.ui\.theme\./import at.mocode.client.common.theme./g' "$file"
    sed -i 's/import at\.mocode\.ui\.screens\./import at.mocode.client.web.screens./g' "$file"
    sed -i 's/import at\.mocode\.ui\.viewmodel\./import at.mocode.client.web.viewmodel./g' "$file"
    sed -i 's/import at\.mocode\.di\./import at.mocode.client.common.di./g' "$file"
}

# Find all Kotlin files in the new module structure and update imports
echo "Updating imports in core modules..."
find core -name "*.kt" -type f | while read -r file; do
    update_imports "$file"
done

echo "Updating imports in masterdata modules..."
find masterdata -name "*.kt" -type f | while read -r file; do
    update_imports "$file"
done

echo "Updating imports in members modules..."
find members -name "*.kt" -type f | while read -r file; do
    update_imports "$file"
done

echo "Updating imports in horses modules..."
find horses -name "*.kt" -type f | while read -r file; do
    update_imports "$file"
done

echo "Updating imports in events modules..."
find events -name "*.kt" -type f | while read -r file; do
    update_imports "$file"
done

echo "Updating imports in infrastructure modules..."
find infrastructure -name "*.kt" -type f | while read -r file; do
    update_imports "$file"
done

echo "Updating imports in client modules..."
find client -name "*.kt" -type f | while read -r file; do
    update_imports "$file"
done

# Also update references to old packages in code (not just imports)
echo "Updating references to old packages in code..."
find core masterdata members horses events infrastructure client -name "*.kt" -type f | while read -r file; do
    # Update references to shared-kernel classes
    sed -i 's/at\.mocode\.shared\.config\./at.mocode.core.utils.config./g' "$file"
    sed -i 's/at\.mocode\.shared\.database\./at.mocode.core.utils.database./g' "$file"
    sed -i 's/at\.mocode\.shared\.discovery\./at.mocode.core.utils.discovery./g' "$file"
    sed -i 's/at\.mocode\.serializers\./at.mocode.core.domain.serialization./g' "$file"
    sed -i 's/at\.mocode\.validation\./at.mocode.core.utils.validation./g' "$file"
    sed -i 's/at\.mocode\.dto\.base\./at.mocode.core.domain.model./g' "$file"
    sed -i 's/at\.mocode\.enums\./at.mocode.core.domain.model./g' "$file"

    # Update references to master-data classes
    sed -i 's/at\.mocode\.masterdata\.infrastructure\.repository\./at.mocode.masterdata.infrastructure.persistence./g' "$file"
    sed -i 's/at\.mocode\.masterdata\.infrastructure\.table\./at.mocode.masterdata.infrastructure.persistence./g' "$file"
    sed -i 's/at\.mocode\.masterdata\.infrastructure\.api\./at.mocode.masterdata.api.rest./g' "$file"

    # Update references to member-management classes
    sed -i 's/at\.mocode\.members\.infrastructure\.repository\./at.mocode.members.infrastructure.persistence./g' "$file"
    sed -i 's/at\.mocode\.members\.infrastructure\.table\./at.mocode.members.infrastructure.persistence./g' "$file"
    sed -i 's/at\.mocode\.members\.infrastructure\.api\./at.mocode.members.api.rest./g' "$file"

    # Update references to horse-registry classes
    sed -i 's/at\.mocode\.horses\.infrastructure\.repository\./at.mocode.horses.infrastructure.persistence./g' "$file"
    sed -i 's/at\.mocode\.horses\.infrastructure\.table\./at.mocode.horses.infrastructure.persistence./g' "$file"
    sed -i 's/at\.mocode\.horses\.infrastructure\.api\./at.mocode.horses.api.rest./g' "$file"

    # Update references to event-management classes
    sed -i 's/at\.mocode\.events\.infrastructure\.repository\./at.mocode.events.infrastructure.persistence./g' "$file"
    sed -i 's/at\.mocode\.events\.infrastructure\.table\./at.mocode.events.infrastructure.persistence./g' "$file"
    sed -i 's/at\.mocode\.events\.infrastructure\.api\./at.mocode.events.api.rest./g' "$file"

    # Update references to api-gateway classes
    sed -i 's/at\.mocode\.gateway\./at.mocode.infrastructure.gateway./g' "$file"

    # Update references to composeApp classes
    sed -i 's/at\.mocode\.ui\.theme\./at.mocode.client.common.theme./g' "$file"
    sed -i 's/at\.mocode\.ui\.screens\./at.mocode.client.web.screens./g' "$file"
    sed -i 's/at\.mocode\.ui\.viewmodel\./at.mocode.client.web.viewmodel./g' "$file"
    sed -i 's/at\.mocode\.di\./at.mocode.client.common.di./g' "$file"
done

echo "Import update process completed!"
echo "Run a build to verify the changes."
