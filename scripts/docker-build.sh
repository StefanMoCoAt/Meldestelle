#!/bin/bash
# ===================================================================
# Docker Build Script with Centralized Version Management
# Supports two modes:
#   - compat  (default): load docker/build-args/*.env (current behavior)
#   - envless: parse docker/versions.toml directly and export DOCKER_* vars
# ===================================================================

set -e

# Script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCKER_DIR="$PROJECT_ROOT/docker"
BUILD_ARGS_DIR="$DOCKER_DIR/build-args"
VERSIONS_TOML="$DOCKER_DIR/versions.toml"

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

# --- Helpers to read versions.toml directly (POSIX-friendly) ---
get_version() {
    local key=$1
    awk -v k="$key" '
        /^\[versions\]/ { in_section=1; next }
        /^\[/ { if (in_section) exit; in_section=0 }
        in_section && $1 == k && $2 == "=" { v=$3; gsub(/"/ ,"", v); print v; exit }
    ' "$VERSIONS_TOML" || true
}

get_env_mappings() {
    awk '/^\[environment-mapping\]/,/^\[/ { if (/^[a-zA-Z].*= /) { key=$1; val=$3; gsub(/"/,"",val); print key":"val } }' "$VERSIONS_TOML" || true
}

# Function to load from versions.toml (env-less mode)
load_from_versions() {
    if [[ ! -f "$VERSIONS_TOML" ]]; then
        print_error "versions.toml not found at $VERSIONS_TOML"
        exit 1
    fi

    print_info "Loading centralized versions directly from versions.toml (env-less mode)..."

    # Export BUILD_DATE if not already set
    export BUILD_DATE=${BUILD_DATE:-$(date -u +'%Y-%m-%dT%H:%M:%SZ')}

    # Map all environment-mapping keys to DOCKER_* variables using [versions] values
    while IFS=: read -r toml_key env_var; do
        [[ -z "$toml_key" || -z "$env_var" ]] && continue
        val=$(get_version "$toml_key")
        if [[ -n "$val" ]]; then
            export "$env_var"="$val"
        fi
    done < <(get_env_mappings)

    # Additional convenience exports used by compose build args
    export DOCKER_GRADLE_VERSION="${DOCKER_GRADLE_VERSION:-$(get_version gradle)}"
    export DOCKER_JAVA_VERSION="${DOCKER_JAVA_VERSION:-$(get_version java)}"
    export DOCKER_NODE_VERSION="${DOCKER_NODE_VERSION:-$(get_version node)}"
    export DOCKER_NGINX_VERSION="${DOCKER_NGINX_VERSION:-$(get_version nginx)}"

    # Ensure DOCKER_APP_VERSION is derived from app-version
    local app_ver
    app_ver=$(get_version "app-version")
    if [[ -n "$app_ver" ]]; then
        export DOCKER_APP_VERSION="$app_ver"
    fi

    # Backwards compatibility for scripts expecting plain names
    export VERSION="${VERSION:-$app_ver}"

    print_success "versions.toml loaded; DOCKER_* variables exported."
}

# Function to load environment files (compat mode)
load_env_files() {
    print_info "Loading centralized Docker version environment files (compat mode)..."

    # Load global environment variables
    if [[ -f "$BUILD_ARGS_DIR/global.env" ]]; then
        # shellcheck disable=SC2046
        export $(grep -v '^#' "$BUILD_ARGS_DIR/global.env" | xargs)
        print_info "✓ Loaded global.env"
    else
        print_error "Global environment file not found: $BUILD_ARGS_DIR/global.env"
        exit 1
    fi

    # Load category-specific environment variables
    for env_file in services.env clients.env infrastructure.env; do
        if [[ -f "$BUILD_ARGS_DIR/$env_file" ]]; then
            # shellcheck disable=SC2046
            export $(grep -v '^#' "$BUILD_ARGS_DIR/$env_file" | xargs)
            print_info "✓ Loaded $env_file"
        else
            print_warning "Optional environment file not found: $BUILD_ARGS_DIR/$env_file"
        fi
    done

    # Set BUILD_DATE if not already set
    export BUILD_DATE=${BUILD_DATE:-$(date -u +'%Y-%m-%dT%H:%M:%SZ')}

    # Map to Docker Compose environment variables
    export DOCKER_GRADLE_VERSION="${GRADLE_VERSION}"
    export DOCKER_JAVA_VERSION="${JAVA_VERSION}"
    export DOCKER_NODE_VERSION="${NODE_VERSION}"
    export DOCKER_NGINX_VERSION="${NGINX_VERSION}"
    export DOCKER_APP_VERSION="${VERSION}"
    export DOCKER_SPRING_PROFILES_DEFAULT="${SPRING_PROFILES_ACTIVE:-default}"
    export DOCKER_SPRING_PROFILES_DOCKER="docker"

    print_success "All environment files loaded successfully!"
}

# Function to show current versions
show_versions() {
    print_info "Current centralized Docker versions:"
    echo "  Gradle Version: ${DOCKER_GRADLE_VERSION:-not set}"
    echo "  Java Version: ${DOCKER_JAVA_VERSION:-not set}"
    echo "  Node Version: ${DOCKER_NODE_VERSION:-not set}"
    echo "  Nginx Version: ${DOCKER_NGINX_VERSION:-not set}"
    echo "  App Version: ${DOCKER_APP_VERSION:-not set}"
    echo "  Build Date: ${BUILD_DATE:-not set}"
    echo "  Spring Profile (Default): ${DOCKER_SPRING_PROFILES_DEFAULT:-not set}"
    echo "  Spring Profile (Docker): ${DOCKER_SPRING_PROFILES_DOCKER:-not set}"
}

# Function to build specific category
build_category() {
    local category=$1
    local compose_file=""

    case $category in
        "infrastructure")
            compose_file="docker-compose.yml"
            ;;
        "services")
            compose_file="docker-compose.yml -f docker-compose.services.yml"
            ;;
        "clients")
            compose_file="docker-compose.yml -f docker-compose.clients.yml"
            ;;
        "all")
            compose_file="docker-compose.yml -f docker-compose.services.yml -f docker-compose.clients.yml"
            ;;
        *)
            print_error "Invalid category: $category"
            print_info "Valid categories: infrastructure, services, clients, all"
            exit 1
            ;;
    esac

    print_info "Building $category with centralized versions..."
    cd "$PROJECT_ROOT"

    if docker-compose -f $compose_file build; then
        print_success "$category built successfully!"
    else
        print_error "Failed to build $category"
        exit 1
    fi
}

# Help function
show_help() {
    echo "Docker Build Script with Centralized Version Management"
    echo ""
    echo "Usage: $0 [OPTIONS] [CATEGORY]"
    echo ""
    echo "Categories:"
    echo "  infrastructure  Build infrastructure services (API Gateway)"
    echo "  services        Build application services (ping-service, etc.)"
    echo "  clients         Build client applications (web-app, desktop-app)"
    echo "  all             Build everything"
    echo ""
    echo "Options:"
    echo "  -v, --versions  Show current versions"
    echo "  -h, --help      Show this help message"
    echo ""
    echo "Environment:"
    echo "  DOCKER_SSOT_MODE=envless|compat  Default: compat"
    echo ""
    echo "Examples:"
    echo "  $0 services                    # Build all services"
    echo "  $0 clients                     # Build client applications"
    echo "  $0 all                         # Build everything"
    echo "  $0 --versions                  # Show current versions"
    echo "  DOCKER_SSOT_MODE=envless $0 --versions  # Use versions.toml directly"
}

# Main execution
main() {
    local MODE="${DOCKER_SSOT_MODE:-compat}"

    # Parse command line arguments
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -v|--versions)
            if [[ "$MODE" == "envless" ]]; then
                load_from_versions
            else
                load_env_files
            fi
            show_versions
            exit 0
            ;;
        "")
            print_error "No category specified"
            show_help
            exit 1
            ;;
        *)
            # Load environment and build
            if [[ "$MODE" == "envless" ]]; then
                load_from_versions
            else
                load_env_files
            fi
            show_versions
            echo ""
            build_category "$1"
            ;;
    esac
}

# Run main function with all arguments
main "$@"
