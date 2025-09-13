#!/bin/bash
# ===================================================================
# Docker Build Script with Centralized Version Management
# Automatically sources versions from docker/versions.toml via environment files
# ===================================================================

set -e

# Script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCKER_DIR="$PROJECT_ROOT/docker"
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

# Function to load environment files
load_env_files() {
    print_info "Loading centralized Docker version environment files..."

    # Load global environment variables
    if [[ -f "$BUILD_ARGS_DIR/global.env" ]]; then
        export $(grep -v '^#' "$BUILD_ARGS_DIR/global.env" | xargs)
        print_info "✓ Loaded global.env"
    else
        print_error "Global environment file not found: $BUILD_ARGS_DIR/global.env"
        exit 1
    fi

    # Load category-specific environment variables
    for env_file in services.env clients.env infrastructure.env; do
        if [[ -f "$BUILD_ARGS_DIR/$env_file" ]]; then
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
    echo "Examples:"
    echo "  $0 services                    # Build all services"
    echo "  $0 clients                     # Build client applications"
    echo "  $0 all                         # Build everything"
    echo "  $0 --versions                  # Show current versions"
    echo ""
    echo "The script automatically loads versions from:"
    echo "  - docker/build-args/global.env"
    echo "  - docker/build-args/services.env"
    echo "  - docker/build-args/clients.env"
    echo "  - docker/build-args/infrastructure.env"
}

# Main execution
main() {
    # Parse command line arguments
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -v|--versions)
            load_env_files
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
            load_env_files
            show_versions
            echo ""
            build_category "$1"
            ;;
    esac
}

# Run main function with all arguments
main "$@"
