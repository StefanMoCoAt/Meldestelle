#!/bin/bash
# ===================================================================
# Docker Versions Update Utility
# Updates central docker/versions.toml and syncs to environment files
# ===================================================================

set -e

# Script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCKER_DIR="$PROJECT_ROOT/docker"
VERSIONS_TOML="$DOCKER_DIR/versions.toml"
BUILD_ARGS_DIR="$DOCKER_DIR/build-args"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to extract version from TOML file
get_version() {
    local key=$1
    grep "^$key = " "$VERSIONS_TOML" | sed 's/.*= "\(.*\)"/\1/' || echo ""
}

# Function to update version in TOML file
update_version() {
    local key=$1
    local new_value=$2

    if grep -q "^$key = " "$VERSIONS_TOML"; then
        # Update existing key
        sed -i.bak "s/^$key = .*/$key = \"$new_value\"/" "$VERSIONS_TOML"
        print_success "Updated $key to $new_value"
    else
        print_error "Key $key not found in $VERSIONS_TOML"
        return 1
    fi
}

# Function to sync TOML to environment files
sync_to_env_files() {
    print_info "Syncing versions.toml to environment files..."

    # Get current versions from TOML
    # shellcheck disable=SC2155
    local gradle_version=$(get_version "gradle")
    # shellcheck disable=SC2155
    local java_version=$(get_version "java")
    # shellcheck disable=SC2155
    local node_version=$(get_version "node")
    # shellcheck disable=SC2155
    local nginx_version=$(get_version "nginx")
    # shellcheck disable=SC2155
    local app_version=$(get_version "app-version")
    # shellcheck disable=SC2155
    local spring_default=$(get_version "spring-profiles-default")
    # shellcheck disable=SC2155
    local spring_docker=$(get_version "spring-profiles-docker")
    local prometheus_version=$(get_version "prometheus")
    local grafana_version=$(get_version "grafana")
    local keycloak_version=$(get_version "keycloak")
    local postgres_version=$(get_version "postgres")
    local redis_version=$(get_version "redis")
    local consul_version=$(get_version "consul")
    local zookeeper_version=$(get_version "zookeeper")
    local kafka_version=$(get_version "kafka")

    # Update global.env (strictly build-time versions/tags)
    cat > "$BUILD_ARGS_DIR/global.env" << EOF
# ===================================================================
# Global Docker Build Arguments - Used by all categories
# Source: docker/versions.toml
# Last updated: $(date -u +'%Y-%m-%d %H:%M:%S UTC')
# ===================================================================

# --- Build Tools ---
GRADLE_VERSION=$gradle_version
JAVA_VERSION=$java_version

# --- Build Metadata ---
VERSION=$app_version

# --- Monitoring & Infrastructure Services (image tags) ---
PROMETHEUS_IMAGE_TAG=$prometheus_version
GRAFANA_IMAGE_TAG=$grafana_version
KEYCLOAK_IMAGE_TAG=$keycloak_version

# --- Datastore Images (image tags) ---
POSTGRES_IMAGE_TAG=$postgres_version
REDIS_IMAGE_TAG=$redis_version

# --- Additional Infrastructure Images (image tags) ---
CONSUL_IMAGE_TAG=$consul_version
ZOOKEEPER_IMAGE_TAG=$zookeeper_version
KAFKA_IMAGE_TAG=$kafka_version
EOF
    print_success "Updated global.env"

    # Update clients.env (strictly build-time values; no runtime/dev vars)
    cat > "$BUILD_ARGS_DIR/clients.env" << EOF
# ===================================================================
# Clients Docker Build Arguments - dockerfiles/clients/*
# Source: docker/versions.toml [categories.clients]
# Last updated: $(date -u +'%Y-%m-%d %H:%M:%S UTC')
# ===================================================================

# --- Include Global Arguments ---
# Source global.env for GRADLE_VERSION, JAVA_VERSION, VERSION

# --- Client-Specific Build Tools ---
NODE_VERSION=$node_version
NGINX_VERSION=$nginx_version

# --- Client Build Configuration ---
CLIENT_PATH=client
CLIENT_MODULE=client
CLIENT_NAME=meldestelle-client
# Note: Runtime/Dev values moved to config/env/.env
# Keep this file strictly for build-time values only.
EOF
    print_success "Updated clients.env"

    # Update services.env (strictly build-time values; no runtime vars)
    cat > "$BUILD_ARGS_DIR/services.env" << EOF
# ===================================================================
# Services Docker Build Arguments - dockerfiles/services/*
# Source: docker/versions.toml [categories.services]
# Last updated: $(date -u +'%Y-%m-%d %H:%M:%S UTC')
# ===================================================================

# --- Include Global Arguments ---
# Source global.env for GRADLE_VERSION, JAVA_VERSION, VERSION

# --- Service-Specific Arguments ---
SERVICE_PATH=.
SERVICE_NAME=spring-boot-service
# Note: Runtime profiles/ports moved to config/env/.env
EOF
    print_success "Updated services.env"

    # Update infrastructure.env (strictly build-time values; no runtime vars)
    cat > "$BUILD_ARGS_DIR/infrastructure.env" << EOF
# ===================================================================
# Infrastructure Docker Build Arguments - dockerfiles/infrastructure/*
# Source: docker/versions.toml [categories.infrastructure]
# Last updated: $(date -u +'%Y-%m-%d %H:%M:%S UTC')
# ===================================================================

# --- Include Global Arguments ---
# Source global.env for GRADLE_VERSION, JAVA_VERSION, VERSION

# --- API Gateway Specific ---
GATEWAY_SERVICE_PATH=infrastructure/gateway
GATEWAY_SERVICE_NAME=api-gateway

# --- Auth Server Specific ---
AUTH_SERVER_PATH=infrastructure/auth/auth-server
AUTH_SERVER_SERVICE_NAME=auth-server

# --- Monitoring Server Specific ---
MONITORING_SERVER_PATH=infrastructure/monitoring/monitoring-server
MONITORING_SERVER_SERVICE_NAME=monitoring-server

# Note: Runtime profiles/ports/dependencies moved to config/env/.env
EOF
    print_success "Updated infrastructure.env"

    # --- Post-generation cleanup to enforce SSoT policies ---
    # 1) Remove any accidental bare DOCKER_* placeholders from non-global envs
    sed -i "/^DOCKER_[A-Z0-9_]\+$/d" "$BUILD_ARGS_DIR/services.env" || true
    sed -i "/^DOCKER_[A-Z0-9_]\+$/d" "$BUILD_ARGS_DIR/infrastructure.env" || true
    sed -i "/^DOCKER_[A-Z0-9_]\+$/d" "$BUILD_ARGS_DIR/clients.env" || true

    # 2) Remove forbidden DOCKER_APP_VERSION from all build-args envs (it is mapped at runtime)
    sed -i "/^DOCKER_APP_VERSION\(=.*\)\?$/d" "$BUILD_ARGS_DIR/global.env" || true
    sed -i "/^DOCKER_APP_VERSION\(=.*\)\?$/d" "$BUILD_ARGS_DIR/clients.env" || true
    sed -i "/^DOCKER_APP_VERSION\(=.*\)\?$/d" "$BUILD_ARGS_DIR/services.env" || true
    sed -i "/^DOCKER_APP_VERSION\(=.*\)\?$/d" "$BUILD_ARGS_DIR/infrastructure.env" || true

    # 3) Purge stray numeric service-port assignments that must not live in global.env
    #    e.g., lines like: prometheus = 9090
    sed -i -E "/^[a-z_]+ = [0-9]+$/d" "$BUILD_ARGS_DIR/global.env" || true

    print_success "All environment files synced successfully!"
}

# Function to show current versions
show_current_versions() {
    print_info "Current Docker versions:"
    echo "  Gradle: $(get_version "gradle")"
    echo "  Java: $(get_version "java")"
    echo "  Node.js: $(get_version "node")"
    echo "  Nginx: $(get_version "nginx")"
    echo "  Alpine: $(get_version "alpine")"
    echo "  Prometheus: $(get_version "prometheus")"
    echo "  Grafana: $(get_version "grafana")"
    echo "  Keycloak: $(get_version "keycloak")"
    echo "  App Version: $(get_version "app-version")"
    echo "  Spring Profile (Default): $(get_version "spring-profiles-default")"
    echo "  Spring Profile (Docker): $(get_version "spring-profiles-docker")"
}

# Function to show help
show_help() {
    echo "Docker Versions Update Utility"
    echo ""
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  show                         Show current versions"
    echo "  sync                         Sync versions.toml to environment files"
    echo "  update <key> <version>       Update specific version"
    echo ""
    echo "Available keys for update:"
    echo "  gradle                       Gradle version"
    echo "  java                         Java version"
    echo "  node                         Node.js version"
    echo "  nginx                        Nginx version"
    echo "  alpine                       Alpine Linux version"
    echo "  prometheus                   Prometheus version"
    echo "  grafana                      Grafana version"
    echo "  keycloak                     Keycloak version"
    echo "  app-version                  Application version"
    echo "  spring-profiles-default      Default Spring profile"
    echo "  spring-profiles-docker       Docker Spring profile"
    echo ""
    echo "Examples:"
    echo "  $0 show                      # Show current versions"
    echo "  $0 update gradle 9.1.0       # Update Gradle to 9.1.0"
    echo "  $0 update java 22            # Update Java to version 22"
    echo "  $0 sync                      # Sync versions to environment files"
    echo ""
    echo "After updating versions, run 'sync' to update environment files"
    echo "or use scripts/docker-build.sh to build with new versions."
}

# Main execution
main() {
    # Check if versions.toml exists
    if [[ ! -f "$VERSIONS_TOML" ]]; then
        print_error "Versions file not found: $VERSIONS_TOML"
        exit 1
    fi

    case $1 in
        "show")
            show_current_versions
            ;;
        "sync")
            sync_to_env_files
            ;;
        "update")
            if [[ $# -lt 3 ]]; then
                print_error "Usage: $0 update <key> <version>"
                exit 1
            fi
            update_version "$2" "$3"
            sync_to_env_files
            ;;
        "-h"|"--help"|"help")
            show_help
            ;;
        "")
            print_error "No command specified"
            show_help
            exit 1
            ;;
        *)
            print_error "Unknown command: $1"
            show_help
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"
