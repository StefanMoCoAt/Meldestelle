#!/bin/bash
# ===================================================================
# Service Startup and Health Check Test Script
# Meldestelle Project - Docker Services Testing
# ===================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
TIMEOUT_SECONDS=300
HEALTH_CHECK_INTERVAL=10
MAX_RETRIES=30

# NEU: Alle Compose-Dateien zentral definieren
COMPOSE_FILES="-f docker-compose.yml -f docker-compose.services.yml -f docker-compose.clients.yml"


# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to wait for service health check
wait_for_health_check() {
    local service_name=$1
    local health_url=$2
    local max_attempts=$3
    local attempt=1

    log_info "Waiting for $service_name health check at $health_url"

    while [ $attempt -le $max_attempts ]; do
        # ALT: if curl -f -s --max-time 5 "$health_url" > /dev/null 2>&1; then
        # NEU: Die Option -L wurde hinzugefügt, um HTTP-Redirects zu folgen.
        if curl -f -s -L --max-time 5 "$health_url" > /dev/null 2>&1; then
            log_success "$service_name is healthy (attempt $attempt/$max_attempts)"
            return 0
        fi

        log_info "$service_name health check failed (attempt $attempt/$max_attempts), retrying in $HEALTH_CHECK_INTERVAL seconds..."
        sleep $HEALTH_CHECK_INTERVAL
        ((attempt++))
    done

    log_error "$service_name failed to become healthy after $max_attempts attempts"
    return 1
}

# Function to check service logs for errors
check_service_logs() {
    local service_name=$1
    local container_name=$2

    log_info "Checking $service_name logs for errors..."

    # Get last 50 lines of logs
    local logs=$(docker logs --tail 50 "$container_name" 2>&1 || echo "")

    # Check for common error patterns
    if echo "$logs" | grep -qi "error\|exception\|failed\|fatal"; then
        log_warning "$service_name has error messages in logs:"
        echo "$logs" | grep -i "error\|exception\|failed\|fatal" | tail -5
    else
        log_success "$service_name logs look clean"
    fi
}

# Function to test infrastructure services
test_all_services() {
    log_info "========================================="
    log_info "Starting All Meldestelle Services"
    log_info "========================================="

    # Start ALL services using all compose files
    log_info "Starting full environment with docker-compose..."
    # ALT: docker compose up -d
    docker compose $COMPOSE_FILES up -d

    # Give services time to initialize
    log_info "Waiting 45 seconds for services to initialize..."
    sleep 45

    # =========================================
    # CHECK INFRASTRUCTURE
    # =========================================
    log_info "--- Checking Infrastructure Services ---"
    local infra_services=(
        "postgres:http://localhost:5432:PostgreSQL"
        "redis:redis://localhost:6379:Redis"
        "consul:http://localhost:8500/v1/status/leader:Consul"
        "prometheus:http://localhost:9090/-/healthy:Prometheus"
        "grafana:http://localhost:3000/api/health:Grafana"
        "keycloak:http://localhost:8180/:Keycloak"
    )

    for service_info in "${infra_services[@]}"; do
        # Parse service info: service_name:health_url:description
        # Extract service name (everything before first colon)
        service_name=$(echo "$service_info" | cut -d':' -f1)

        # Extract health_url (everything after first colon, before last colon)
        # For "postgres:http://localhost:5432:PostgreSQL" -> "http://localhost:5432"
        temp_url=$(echo "$service_info" | cut -d':' -f2-)
        health_url=$(echo "$temp_url" | sed 's/:[^:]*$//')

        # Extract description (everything after last colon)
        description=$(echo "$service_info" | sed 's/.*://')

        # Special handling for PostgreSQL and Redis (no HTTP health checks)
        if [ "$service_name" = "postgres" ]; then
            log_info "Testing PostgreSQL connection..."
            if docker exec meldestelle-postgres pg_isready -U meldestelle -d meldestelle > /dev/null 2>&1; then
                log_success "PostgreSQL is ready"
            else
                log_error "PostgreSQL is not ready"
                return 1
            fi
        elif [ "$service_name" = "redis" ]; then
            log_info "Testing Redis connection..."
            if docker exec meldestelle-redis redis-cli ping > /dev/null 2>&1; then
                log_success "Redis is ready"
            else
                log_error "Redis is not ready"
                return 1
            fi
        else
            wait_for_health_check "$description" "$health_url" $MAX_RETRIES || return 1
        fi
        check_service_logs "$description" "meldestelle-$service_name"
    done
    log_success "All infrastructure services are healthy!"

    # =========================================
    # CHECK API GATEWAY
    # =========================================
    log_info "--- Checking API Gateway ---"
    wait_for_health_check "API Gateway" "http://localhost:8081/actuator/health" $MAX_RETRIES || return 1
    check_service_logs "API Gateway" "meldestelle-api-gateway"
    log_success "API Gateway is healthy!"

    # =========================================
    # CHECK APPLICATION SERVICES
    # =========================================
    log_info "--- Checking Application Services ---"
    local app_services=(
        "ping-service:http://localhost:8082/actuator/health:Ping Service"
    )
    # Note: Add other services like members-service here when they are enabled

    for service_info in "${app_services[@]}"; do
        IFS=':' read -r service_name health_url description <<< "$service_info"
        wait_for_health_check "$description" "$health_url" $MAX_RETRIES || return 1
        check_service_logs "$description" "meldestelle-$service_name"
    done
    log_success "All application services are healthy!"

    # =========================================
    # CHECK CLIENT SERVICES
    # =========================================
    log_info "--- Checking Client Services ---"
    local client_services=(
        "web-app:http://localhost:4000/health:Web Application"
        "auth-server:http://localhost:8087/actuator/health:Auth Server"
    )
    # Note: Add other client services here when enabled

    for service_info in "${client_services[@]}"; do
        # ... (parsing logic remains the same)
        service_name=$(echo "$service_info" | cut -d':' -f1)
        health_url=$(echo "$service_info" | cut -d':' -f2)
        description=$(echo "$service_info" | cut -d':' -f3)
        wait_for_health_check "$description" "$health_url" $MAX_RETRIES || return 1
        # Use the container name from docker-compose.clients.yml (e.g., meldestelle-web-app)
        check_service_logs "$description" "meldestelle-$service_name"
    done
    log_success "All client services are healthy!"
}

# ENTFERNT: test_api_gateway, test_application_services, test_client_services wurden in test_all_services integriert.

# Function to test network connectivity
test_network_connectivity() {
    log_info "========================================="
    log_info "Testing Network Connectivity"
    log_info "========================================="

    # Test internal network connectivity between services
    log_info "Testing service-to-service connectivity..."

    # Test API Gateway can reach backend services
    if docker exec meldestelle-api-gateway curl -f -s --max-time 5 http://ping-service:8082/actuator/health > /dev/null 2>&1; then
        log_success "API Gateway can reach Ping Service"
    else
        log_error "API Gateway cannot reach Ping Service"
        return 1
    fi

    # Test application service can reach infrastructure
    if docker exec meldestelle-ping-service curl -f -s --max-time 5 http://consul:8500/v1/status/leader > /dev/null 2>&1; then
        log_success "Application services can reach Consul"
    else
        log_error "Application services cannot reach Consul"
        return 1
    fi

    log_success "Network connectivity tests passed!"
}

# Function to generate test report
generate_test_report() {
    log_info "========================================="
    log_info "Test Report Summary"
    log_info "========================================="

    # Get running containers
    local running_containers=$(docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep meldestelle)

    echo "Running Meldestelle Services:"
    echo "$running_containers"

    # Check resource usage
    log_info "Resource usage summary:"
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" $(docker ps -q --filter "name=meldestelle")
}

# Function to cleanup
cleanup() {
    log_info "========================================="
    log_info "Cleaning up test environment"
    log_info "========================================="

    log_info "Stopping and removing all test containers..."

    # Use the same files to tear down the environment
    docker compose $COMPOSE_FILES down --remove-orphans -v

#    # Stop and remove containers if they exist
#    local containers=("meldestelle-postgres" "meldestelle-redis" "meldestelle-consul" "meldestelle-prometheus" "meldestelle-grafana" "meldestelle-keycloak" "meldestelle-api-gateway")
#
#    for container in "${containers[@]}"; do
#        if docker ps -a --format '{{.Names}}' | grep -q "^${container}$"; then
#            log_info "Stopping and removing $container"
#            docker stop "$container" >/dev/null 2>&1 || true
#            docker rm "$container" >/dev/null 2>&1 || true
#        fi
#    done

    # Remove network if it exists
    docker network rm meldestelle-network >/dev/null 2>&1 || true

    log_info "Cleanup completed"
}

# Main test execution
main() {
    log_info "========================================="
    log_info "Starting Meldestelle Services Test Suite"
    log_info "========================================="

    # Set trap to cleanup on exit
    trap cleanup EXIT

    # Run tests in sequence
    test_all_services || exit 1
    test_network_connectivity || exit 1

    # Generate report
    generate_test_report

    log_success "========================================="
    log_success "All tests passed successfully!"
    log_success "All services are running and healthy!"
    log_success "========================================="
}

# Parse command line arguments
case "${1:-}" in
    "all")
        test_all_services
        ;;
    "network")
        test_network_connectivity
        ;;
    "cleanup")
        cleanup
        ;;
    *)
        main
        ;;
esac
