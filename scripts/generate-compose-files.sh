#!/bin/bash
# ===================================================================
# Docker Compose Template Generator
# Generates docker-compose files from docker/versions.toml templates
# ===================================================================

set -e

# Script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCKER_DIR="$PROJECT_ROOT/docker"
VERSIONS_TOML="$DOCKER_DIR/versions.toml"
TEMPLATES_DIR="$DOCKER_DIR/compose-templates"
OUTPUT_DIR="$PROJECT_ROOT"

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

# Function to extract port from TOML file
get_port() {
    local service=$1
    grep "^$service = " "$VERSIONS_TOML" | grep -A 50 "\[service-ports\]" | grep "^$service = " | sed 's/.*= \(.*\)/\1/' || echo ""
}

# Function to extract environment config from TOML file
get_env_config() {
    local env=$1
    local key=$2
    awk "/\[environments\.$env\]/,/^\[/ {if (/^$key = /) {gsub(/.*= \"?|\"?$/, \"\"); print}}" "$VERSIONS_TOML" || echo ""
}

# Function to generate build args section for a service category
generate_build_args_section() {
    local category=$1

    cat << EOF
      args:
        # Global build arguments (from docker/build-args/global.env)
        GRADLE_VERSION: \${DOCKER_GRADLE_VERSION:-$(get_version "gradle")}
        JAVA_VERSION: \${DOCKER_JAVA_VERSION:-$(get_version "java")}
        BUILD_DATE: \${BUILD_DATE}
        VERSION: \${DOCKER_APP_VERSION:-$(get_version "app-version")}
EOF

    case $category in
        "services")
            cat << EOF
        # Service-specific arguments (from docker/build-args/services.env)
        SPRING_PROFILES_ACTIVE: \${DOCKER_SPRING_PROFILES_DOCKER:-$(get_version "spring-profiles-docker")}
EOF
            ;;
        "infrastructure")
            cat << EOF
        # Infrastructure-specific arguments (from docker/build-args/infrastructure.env)
        SPRING_PROFILES_ACTIVE: \${DOCKER_SPRING_PROFILES_DEFAULT:-$(get_version "spring-profiles-default")}
EOF
            ;;
        "clients")
            cat << EOF
        # Client-specific arguments (from docker/build-args/clients.env)
        NODE_VERSION: \${DOCKER_NODE_VERSION:-$(get_version "node")}
        NGINX_VERSION: \${DOCKER_NGINX_VERSION:-$(get_version "nginx")}
EOF
            ;;
    esac
}

# Function to generate environment variables section
generate_environment_vars_for_service() {
    local service=$1
    local environment=${2:-development}

    local spring_profiles=$(get_env_config $environment "spring-profiles")
    local debug_enabled=$(get_env_config $environment "debug-enabled")
    local log_level=$(get_env_config $environment "log-level")
    local debug_port=$(get_env_config $environment "jvm-debug-port")
    local service_port=$(get_port $service)

    cat << EOF
    environment:
      SPRING_PROFILES_ACTIVE: \${SPRING_PROFILES_ACTIVE:-$spring_profiles}
      SERVER_PORT: \${${service^^}_PORT:-$service_port}
      DEBUG: \${DEBUG:-$debug_enabled}
      LOGGING_LEVEL_ROOT: \${LOGGING_LEVEL_ROOT:-$log_level}
EOF

    # Add debug port if enabled
    if [[ "$debug_port" != "false" && "$debug_port" != "" ]]; then
        echo "      JVM_DEBUG_PORT: ${debug_port}"
    fi
}

# Function to generate health check section
generate_health_check() {
    local service=$1
    local environment=${2:-development}

    local interval=$(get_env_config $environment "health-check-interval")
    local timeout=$(get_env_config $environment "health-check-timeout")
    local retries=$(get_env_config $environment "health-check-retries")
    local start_period=$(get_env_config $environment "health-check-start-period")
    local service_port=$(get_port $service)

    cat << EOF
    healthcheck:
      test: ["CMD", "curl", "--fail", "http://localhost:${service_port}/actuator/health/readiness"]
      interval: ${interval:-30s}
      timeout: ${timeout:-5s}
      retries: ${retries:-3}
      start_period: ${start_period:-40s}
EOF
}

# Function to generate service definition
generate_service_definition() {
    local service=$1
    local category=$2
    local environment=${3:-development}

    local service_port=$(get_port $service)
    local debug_port=$(get_env_config $environment "jvm-debug-port")

    cat << EOF
  $service:
    build:
      context: .
      dockerfile: dockerfiles/$category/$service/Dockerfile
$(generate_build_args_section $category)
    container_name: meldestelle-$service
$(generate_environment_vars_for_service $service $environment)
    ports:
      - "\${${service^^}_PORT:-$service_port}:$service_port"
EOF

    # Add debug port if enabled
    if [[ "$debug_port" != "false" && "$debug_port" != "" ]]; then
        echo "      - \"${debug_port}:${debug_port}\"  # Debug-Port"
    fi

    cat << EOF
    networks:
      - meldestelle-network
$(generate_health_check $service $environment)
    restart: unless-stopped
EOF
}

# Function to generate main infrastructure compose file
generate_infrastructure_compose() {
    local environment=${1:-development}

    print_info "Generating docker-compose.yml (Infrastructure)..."

    cat > "$OUTPUT_DIR/docker-compose.yml" << EOF
# ===================================================================
# Docker Compose - Infrastructure Services
# Generated from docker/versions.toml
# Environment: $environment
# Generated: $(date -u +'%Y-%m-%d %H:%M:%S UTC')
# ===================================================================

services:
  # ===================================================================
  # Database
  # ===================================================================
  postgres:
    image: postgres:16-alpine
    container_name: meldestelle-postgres
    environment:
      POSTGRES_USER: \${POSTGRES_USER:-meldestelle}
      POSTGRES_PASSWORD: \${POSTGRES_PASSWORD:-meldestelle}
      POSTGRES_DB: \${POSTGRES_DB:-meldestelle}
    ports:
      - "$(get_port postgres):$(get_port postgres)"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./docker/services/postgres:/docker-entrypoint-initdb.d
    networks:
      - meldestelle-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U meldestelle -d meldestelle"]
      interval: $(get_env_config $environment "health-check-interval")
      timeout: $(get_env_config $environment "health-check-timeout")
      retries: $(get_env_config $environment "health-check-retries")
      start_period: $(get_env_config $environment "health-check-start-period")
    restart: unless-stopped

  # ===================================================================
  # Cache
  # ===================================================================
  redis:
    image: redis:7-alpine
    container_name: meldestelle-redis
    ports:
      - "\${REDIS_PORT:-$(get_port redis)}:$(get_port redis)"
    volumes:
      - redis-data:/data
    command: redis-server --appendonly yes
    networks:
      - meldestelle-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: $(get_env_config $environment "health-check-interval")
      timeout: $(get_env_config $environment "health-check-timeout")
      retries: $(get_env_config $environment "health-check-retries")
      start_period: $(get_env_config $environment "health-check-start-period")
    restart: unless-stopped

  # ===================================================================
  # Authentication
  # ===================================================================
  keycloak:
    image: quay.io/keycloak/keycloak:\${DOCKER_KEYCLOAK_VERSION:-$(get_version "keycloak")}
    container_name: meldestelle-keycloak
    environment:
      KEYCLOAK_ADMIN: \${KEYCLOAK_ADMIN:-admin}
      KEYCLOAK_ADMIN_PASSWORD: \${KEYCLOAK_ADMIN_PASSWORD:-admin}
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:$(get_port postgres)/\${POSTGRES_DB:-meldestelle}
      KC_DB_USERNAME: \${POSTGRES_USER:-meldestelle}
      KC_DB_PASSWORD: \${POSTGRES_PASSWORD:-meldestelle}
    ports:
      - "$(get_port keycloak):8080"
    depends_on:
      postgres:
        condition: service_healthy
    volumes:
      - ./docker/services/keycloak:/opt/keycloak/data/import
    command: start-dev --import-realm
    networks:
      - meldestelle-network
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8080/"]
      interval: $(get_env_config $environment "health-check-interval")
      timeout: $(get_env_config $environment "health-check-timeout")
      retries: $(get_env_config $environment "health-check-retries")
      start_period: $(get_env_config $environment "health-check-start-period")
    restart: unless-stopped

  # ===================================================================
  # Monitoring
  # ===================================================================
  prometheus:
    image: prom/prometheus:\${DOCKER_PROMETHEUS_VERSION:-$(get_version "prometheus")}
    container_name: meldestelle-prometheus
    ports:
      - "\${PROMETHEUS_PORT:-$(get_port prometheus)}:$(get_port prometheus)"
    volumes:
      - prometheus-data:/prometheus
      - ./docker/monitoring/prometheus:/etc/prometheus:ro
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=200h'
      - '--web.enable-lifecycle'
    networks:
      - meldestelle-network
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:$(get_port prometheus)/-/healthy"]
      interval: $(get_env_config $environment "health-check-interval")
      timeout: $(get_env_config $environment "health-check-timeout")
      retries: $(get_env_config $environment "health-check-retries")
      start_period: $(get_env_config $environment "health-check-start-period")
    restart: unless-stopped

  grafana:
    image: grafana/grafana:\${DOCKER_GRAFANA_VERSION:-$(get_version "grafana")}
    container_name: meldestelle-grafana
    environment:
      GF_SECURITY_ADMIN_USER: \${GF_SECURITY_ADMIN_USER:-admin}
      GF_SECURITY_ADMIN_PASSWORD: \${GF_SECURITY_ADMIN_PASSWORD:-admin}
      GF_USERS_ALLOW_SIGN_UP: \${GF_USERS_ALLOW_SIGN_UP:-false}
      GF_INSTALL_PLUGINS: grafana-piechart-panel
    ports:
      - "\${GRAFANA_PORT:-$(get_port grafana)}:$(get_port grafana)"
    volumes:
      - grafana-data:/var/lib/grafana
      - ./docker/monitoring/grafana:/etc/grafana/provisioning:ro
    depends_on:
      - prometheus
    networks:
      - meldestelle-network
    healthcheck:
      test: ["CMD", "curl", "--fail", "http://localhost:$(get_port grafana)/api/health"]
      interval: $(get_env_config $environment "health-check-interval")
      timeout: $(get_env_config $environment "health-check-timeout")
      retries: $(get_env_config $environment "health-check-retries")
      start_period: $(get_env_config $environment "health-check-start-period")
    restart: unless-stopped

# ===================================================================
# Volumes
# ===================================================================
volumes:
  postgres-data:
    driver: local
  redis-data:
    driver: local
  prometheus-data:
    driver: local
  grafana-data:
    driver: local

# ===================================================================
# Networks
# ===================================================================
networks:
  meldestelle-network:
    driver: bridge
EOF

    print_success "Generated docker-compose.yml"
}

# Function to generate services compose file
generate_services_compose() {
    local environment=${1:-development}

    print_info "Generating docker-compose.services.yml..."

    cat > "$OUTPUT_DIR/docker-compose.services.yml" << EOF
# ===================================================================
# Docker Compose - Application Services
# Generated from docker/versions.toml
# Environment: $environment
# Generated: $(date -u +'%Y-%m-%d %H:%M:%S UTC')
# ===================================================================

services:
$(generate_service_definition "ping-service" "services" $environment)

$(generate_service_definition "api-gateway" "infrastructure" $environment)

# ===================================================================
# Networks (shared network from main compose file)
# ===================================================================
networks:
  meldestelle-network:
    driver: bridge
EOF

    print_success "Generated docker-compose.services.yml"
}

# Function to generate clients compose file
generate_clients_compose() {
    local environment=${1:-development}

    print_info "Generating docker-compose.clients.yml..."

    cat > "$OUTPUT_DIR/docker-compose.clients.yml" << EOF
# ===================================================================
# Docker Compose - Client Applications
# Generated from docker/versions.toml
# Environment: $environment
# Generated: $(date -u +'%Y-%m-%d %H:%M:%S UTC')
# ===================================================================

services:
  # ===================================================================
  # Web Application (Compose for Web)
  # ===================================================================
  web-app:
    build:
      context: .
      dockerfile: dockerfiles/clients/web-app/Dockerfile
$(generate_build_args_section "clients")
        # Application-specific arguments
        CLIENT_PATH: client
        CLIENT_MODULE: client
        CLIENT_NAME: meldestelle-web-app
    container_name: meldestelle-web-app
    environment:
      NODE_ENV: \${NODE_ENV:-$(get_env_config $environment "spring-profiles")}
      API_BASE_URL: http://api-gateway:\${GATEWAY_PORT:-$(get_port "api-gateway")}
      WS_URL: ws://api-gateway:\${GATEWAY_PORT:-$(get_port "api-gateway")}/ws
      APP_TITLE: \${APP_NAME:-Meldestelle}
      APP_VERSION: \${APP_VERSION:-$(get_version "app-version")}
    ports:
      - "$(get_port "web-app"):$(get_port "web-app")"
    networks:
      - meldestelle-network
    healthcheck:
      test: ["CMD", "curl", "--fail", "http://localhost:$(get_port "web-app")/health"]
      interval: $(get_env_config $environment "health-check-interval")
      timeout: $(get_env_config $environment "health-check-timeout")
      retries: $(get_env_config $environment "health-check-retries")
      start_period: $(get_env_config $environment "health-check-start-period")
    restart: unless-stopped

# ===================================================================
# Networks (shared network from main compose file)
# ===================================================================
networks:
  meldestelle-network:
    driver: bridge
EOF

    print_success "Generated docker-compose.clients.yml"
}

# Function to show help
show_help() {
    echo "Docker Compose Template Generator"
    echo ""
    echo "Usage: $0 [COMMAND] [ENVIRONMENT]"
    echo ""
    echo "Commands:"
    echo "  all              Generate all compose files"
    echo "  infrastructure   Generate docker-compose.yml (infrastructure)"
    echo "  services         Generate docker-compose.services.yml"
    echo "  clients          Generate docker-compose.clients.yml"
    echo ""
    echo "Environments:"
    echo "  development      Development environment (default)"
    echo "  production       Production environment"
    echo "  testing          Testing environment"
    echo ""
    echo "Examples:"
    echo "  $0 all                           # Generate all files for development"
    echo "  $0 all production                # Generate all files for production"
    echo "  $0 infrastructure development    # Generate infrastructure compose for dev"
    echo "  $0 services production           # Generate services compose for prod"
}

# Main execution
main() {
    # Check if versions.toml exists
    if [[ ! -f "$VERSIONS_TOML" ]]; then
        print_error "Versions file not found: $VERSIONS_TOML"
        exit 1
    fi

    local command=${1:-all}
    local environment=${2:-development}

    # Validate environment
    if [[ ! "$environment" =~ ^(development|production|testing)$ ]]; then
        print_error "Invalid environment: $environment"
        print_error "Valid environments: development, production, testing"
        exit 1
    fi

    print_info "Generating Docker Compose files for environment: $environment"

    case $command in
        "all")
            generate_infrastructure_compose $environment
            generate_services_compose $environment
            generate_clients_compose $environment
            print_success "All compose files generated successfully!"
            ;;
        "infrastructure")
            generate_infrastructure_compose $environment
            ;;
        "services")
            generate_services_compose $environment
            ;;
        "clients")
            generate_clients_compose $environment
            ;;
        "-h"|"--help"|"help")
            show_help
            ;;
        *)
            print_error "Unknown command: $command"
            show_help
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"
