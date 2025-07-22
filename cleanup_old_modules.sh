#!/bin/bash

# Script to remove old module directories after successful migration
# This script should be run after verifying that all new modules build successfully
#
# Usage:
#   ./cleanup_old_modules.sh         # Remove old module directories
#   ./cleanup_old_modules.sh --dry-run  # Show what would be removed without actually removing

set -e  # Exit on error

# Check for dry run mode
DRY_RUN=false
if [ "$1" == "--dry-run" ]; then
    DRY_RUN=true
    echo "Running in DRY RUN mode - no files will be deleted"
fi

echo "Starting cleanup of old module directories..."

# List of old module directories to remove
OLD_MODULES=(
    "shared-kernel"
    "master-data"
    "member-management"
    "horse-registry"
    "event-management"
    "api-gateway"
    "composeApp"
)

# Check if directories exist and remove them
for module in "${OLD_MODULES[@]}"; do
    if [ -d "$module" ]; then
        if [ "$DRY_RUN" = true ]; then
            echo "[DRY RUN] Would remove old module directory: $module"
        else
            echo "Removing old module directory: $module"
            rm -rf "$module"
        fi
    else
        echo "Module directory not found: $module (already removed)"
    fi
done

if [ "$DRY_RUN" = true ]; then
    echo "Dry run completed. No files were deleted."
    echo "To actually remove the directories, run the script without the --dry-run option."
else
    echo "Cleanup completed successfully!"
    echo "All old module directories have been removed."
    echo "The migration is now complete."
fi
