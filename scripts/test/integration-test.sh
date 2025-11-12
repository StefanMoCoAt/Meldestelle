#!/bin/bash

# =============================================================================
# Full System Integration Test Script
# =============================================================================
# Comprehensive testing of all Meldestelle services including infrastructure,
# application services, client applications, and inter-service connectivity.
# =============================================================================

# Load common utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../utils/common.sh
source "$SCRIPT_DIR/../utils/common.sh" || {
    echo "Error: Could not load common utilities"
    exit 1
}

# =============================================================================
# Configuration
# =============================================================================

readonly COMPOSE_FILES="-f docker-compose.yml -f docker-compose.services.yml -f docker-compose.clients.yml"
# shellcheck disable=SC2034
readonly TIMEOUT_SECONDS=300
# shellcheck disable=SC2034
readonly HEALTH_CHECK_INTERVAL=10
readonly MAX_RETRIES=30

# Project root and Docker configuration
# shellcheck disable=SC2155
readonly PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
readonly DOCKER_DIR="$PROJECT_ROOT/docker"
readonly BUILD_ARGS_DIR="$DOCKER_DIR/build-args"

# Service endpoints (from common configuration)
# shellcheck disable=SC2034
readonly SERVICES_CONFIG=(
    "postgres:5432:PostgreSQL:pg_isready -U meldestelle"
    "redis:6379:Redis:redis-cli ping"
    "consul:8500:Consul:http://localhost:8500/v1/status/leader"
    "api-gateway:8081:API Gateway:http://localhost:8082actuator/health"
    "ping-service:8082ing Service:http://localhost:8082ctuator/health"
)

# Integration with central Docker version management
load_docker_versions() {
    if [[ -f "$BUILD_ARGS_DIR/global.env" ]]; then
        source "$BUILD_ARGS_DIR/global.env"
        log_info "Loaded centralized Docker versions"
    else
        log_warning "Centralized Docker versions not found, using defaults"
    fi
}

# Function to wait for service health check using common utilities
wait_for_service_with_retry() {
    local service_name=$1
    local health_check=$2
    local max_attempts=${3:-$MAX_RETRIES}

    log_info "Waiting for $service_name to become healthy..."

    if retry_with_backoff "$max_attempts" "$health_check" "Waiting for $service_name"; then
        log_success "$service_name is healthy"
        return 0
    else
        log_error "$service_name failed to become healthy after $max_attempts attempts"
        return 1
    fi
}

# HTTP health check function
http_health_check() {
    local url=$1
    curl -f -s -L --max-time 5 "$url" > /dev/null 2>&1
}

# PostgreSQL health check function
postgres_health_check() {
    docker exec meldestelle-postgres pg_isready -U meldestelle -d meldestelle > /dev/null 2>&1
}

# Redis health check function
redis_health_check() {
    docker exec meldestelle-redis redis-cli ping > /dev/null 2>&1
}

# Function to check service logs for errors
check_service_logs() {
    local service_name=$1
    local container_name=$2

    log_info "Checking $service_name logs for errors..."

    # Get last 50 lines of logs
    # shellcheck disable=SC2155
    local logs=$(docker logs --tail 50 "$container_name" 2>&1 || echo "")

    # Check for common error patterns
    if echo "$logs" | grep -qi "error\|exception\|failed\|fatal"; then
        log_warning "$service_name has error messages in logs:"
        echo "$logs" | grep -i "error\|exception\|failed\|fatal" | tail -5
    else
        log_success "$service_name logs look clean"
    fi
}

# =============================================================================
# Enhanced Test Categories and Selective Execution
# =============================================================================

# Function to test infrastructure services only
test_infrastructure_services() {
    log_section "Testing Infrastructure Services"

    # Load Docker versions
    load_docker_versions

    # Start infrastructure services only
    log_info "Starting infrastructure services..."
    # shellcheck disable=SC2164
    cd "$PROJECT_ROOT"
    docker compose -f docker-compose.yml up -d

    # Wait for initialization
    log_info "Waiting 30 seconds for infrastructure services to initialize..."
    sleep 30

    # Test PostgreSQL
    log_info "Testing PostgreSQL connection..."
    wait_for_service_with_retry "PostgreSQL" postgres_health_check || return 1

    # Test Redis
    log_info "Testing Redis connection..."
    wait_for_service_with_retry "Redis" redis_health_check || return 1

    # Test Consul
    log_info "Testing Consul..."
    wait_for_service_with_retry "Consul" "http_health_check http://localhost:8500/v1/status/leader" || return 1

    # Test Prometheus
    log_info "Testing Prometheus..."
    wait_for_service_with_retry "Prometheus" "http_health_check http://localhost:9090/-/healthy" || return 1

    # Test Grafana
    log_info "Testing Grafana..."
    wait_for_service_with_retry "Grafana" "http_health_check http://localhost:3000/api/health" || return 1

    # Test Keycloak
    log_info "Testing Keycloak..."
    wait_for_service_with_retry "Keycloak" "http_health_check http://localhost:8180/" || return 1

    log_success "All infrastructure services are healthy!"
}

# Function to test application services
test_application_services() {
    log_section "Testing Application Services"

    # Start application services
    log_info "Starting application services..."
    # shellcheck disable=SC2164
    cd "$PROJECT_ROOT"
    docker compose $COMPOSE_FILES up -d

    # Wait for initialization
    log_info "Waiting 45 seconds for application services to initialize..."
    sleep 45

    # Test API Gateway
    log_info "Testing API Gateway..."
    wait_for_service_with_retry "API Gateway" "http_health_check http://localhost:8082actuator/health" || return 1

    # Test Ping Service
    log_info "Testing Ping Service..."
    wait_for_service_with_retry "Ping Service" "http_health_check http://localhost:8082ctuator/health" || return 1

    log_success "All application services are healthy!"
}

# Function to test client applications
test_client_applications() {
    log_section "Testing Client Applications"

    # Start client applications
    log_info "Starting client applications..."
    # shellcheck disable=SC2164
    cd "$PROJECT_ROOT"
    docker compose -f docker-compose.yml -f docker-compose.clients.yml up -d

    # Wait for initialization
    log_info "Waiting 60 seconds for client applications to initialize..."
    sleep 60

    # Test Web Application
    log_info "Testing Web Application..."
    wait_for_service_with_retry "Web App" "http_health_check http://localhost:4000/health" || return 1

    # Test Desktop Application (VNC interface)
    log_info "Testing Desktop Application VNC interface..."
    wait_for_service_with_retry "Desktop App" "http_health_check http://localhost:6080/" || return 1

    log_success "All client applications are healthy!"
}

# Function to test network connectivity
test_network_connectivity() {
    log_section "Testing Network Connectivity"

    # Test service-to-service connectivity
    log_info "Testing service-to-service connectivity..."

    # Test API Gateway can reach backend services
    if docker exec meldestelle-api-gateway curl -f -s --max-time 5 http://ping-service:8082ctuator/health > /dev/null 2>&1; then
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

# =============================================================================
# Enhanced Reporting and Monitoring
# =============================================================================

# Function to generate integration report
generate_integration_report() {
    log_section "Integration Test Report"

    # Service status matrix
    log_info "Service Status Matrix:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" --filter "name=meldestelle"

    # Performance metrics
    log_info "Performance Metrics:"
    # shellcheck disable=SC2046
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" $(docker ps -q --filter "name=meldestelle") 2>/dev/null || true

    # Resource usage summary
    # shellcheck disable=SC2155
    local containers=$(docker ps --filter "name=meldestelle" --format "{{.Names}}" | wc -l)
    log_info "Total running containers: $containers"

    # Test summary
    print_test_summary
}

# Enhanced cleanup function using common utilities
cleanup() {
    log_section "Cleaning up test environment"

    log_info "Stopping and removing all test containers..."
    # shellcheck disable=SC2164
    cd "$PROJECT_ROOT"

    # Use the same files to tear down the environment
    docker compose "$COMPOSE_FILES" down --remove-orphans -v 2>/dev/null || true

    # Remove network if it exists
    docker network rm meldestelle-network >/dev/null 2>&1 || true

    log_success "Cleanup completed"
}

# Function to run full system integration test
run_full_integration_test() {
    log_section "Full System Integration Test"

    # Load Docker versions
    load_docker_versions

    # Start ALL services using all compose files
    log_info "Starting full environment with all services..."
    # shellcheck disable=SC2164
    cd "$PROJECT_ROOT"
    docker compose "$COMPOSE_FILES" up -d

    # Give services time to initialize
    log_info "Waiting 60 seconds for all services to initialize..."
    sleep 60

    # Run comprehensive tests
    test_infrastructure_services || return 1
    test_application_services || return 1
    test_client_applications || return 1
    test_network_connectivity || return 1

    # Generate comprehensive report
    generate_integration_report

    log_success "Full system integration test completed successfully!"
}

# =============================================================================
# Command Line Interface and Help System
# =============================================================================

# Function to show help
show_help() {
    cat << EOF
Full System Integration Test Script

USAGE:
    $0 [OPTIONS] [CATEGORY]

CATEGORIES:
    infrastructure      Test infrastructure services only (PostgreSQL, Redis, Consul, etc.)
    services           Test application services (API Gateway, Ping Service, etc.)
    clients            Test client applications (Web App, Desktop App)
    network            Test inter-service network connectivity
    all                Run full system integration test (default)
    cleanup            Clean up test environment only

OPTIONS:
    -h, --help         Show this help message
    -v, --verbose      Enable verbose logging
    --no-cleanup       Skip cleanup on exit
    --cleanup-only     Only run cleanup and exit

EXAMPLES:
    $0                      # Run full integration test
    $0 infrastructure       # Test infrastructure services only
    $0 services             # Test application services only
    $0 clients              # Test client applications only
    $0 network              # Test network connectivity only
    $0 cleanup              # Clean up test environment
    $0 --help               # Show this help

ENVIRONMENT VARIABLES:
    CLEANUP_SERVICES=false     Skip cleanup on exit
    REMOVE_CONTAINERS=true     Remove containers during cleanup
    MAX_RETRIES=30            Maximum retry attempts for health checks
    HEALTH_CHECK_INTERVAL=10   Seconds between health check attempts

The script automatically loads versions from the centralized Docker version
management system and integrates with the common utilities for consistent
logging, error handling, and cleanup procedures.
EOF
}

# =============================================================================
# Main Execution Function
# =============================================================================

# Main execution function with enhanced argument parsing
main() {
    local category="${1:-all}"
    local cleanup_on_exit=true

    # Parse options
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -v|--verbose)
                set -x
                shift
                ;;
            --no-cleanup)
                cleanup_on_exit=false
                shift
                ;;
            --cleanup-only)
                cleanup
                exit 0
                ;;
            -*)
                log_error "Unknown option: $1"
                show_help
                exit 1
                ;;
            *)
                category="$1"
                shift
                ;;
        esac
    done

    # Set cleanup trap if requested
    if [[ "$cleanup_on_exit" == "true" ]]; then
        trap cleanup EXIT
    fi

    # Execute based on category
    log_section "Meldestelle Integration Test Suite"
    log_info "Category: $category"
    log_info "Cleanup on exit: $cleanup_on_exit"

    case "$category" in
        "infrastructure")
            test_infrastructure_services || exit 1
            ;;
        "services")
            test_application_services || exit 1
            ;;
        "clients")
            test_client_applications || exit 1
            ;;
        "network")
            test_network_connectivity || exit 1
            ;;
        "all")
            run_full_integration_test || exit 1
            ;;
        "cleanup")
            cleanup
            exit 0
            ;;
        *)
            log_error "Unknown category: $category"
            show_help
            exit 1
            ;;
    esac

    log_success "Integration test completed successfully!"
}

# Execute main function with all arguments
main "$@"
