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
        if curl -f -s --max-time 5 "$health_url" > /dev/null 2>&1; then
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
test_infrastructure_services() {
    log_info "========================================="
    log_info "Testing Infrastructure Services"
    log_info "========================================="

    # Start infrastructure services
    log_info "Starting infrastructure services..."
    # Use docker directly since docker-compose has system issues
    log_info "Note: Using docker run directly due to docker-compose system issues"

    # Create network if it doesn't exist
    docker network create meldestelle-network 2>/dev/null || true

    # Start PostgreSQL
    docker run -d --name meldestelle-postgres \
        --network meldestelle-network \
        -e POSTGRES_USER=meldestelle \
        -e POSTGRES_PASSWORD=meldestelle \
        -e POSTGRES_DB=meldestelle \
        -p 5432:5432 \
        postgres:16-alpine 2>/dev/null || log_info "PostgreSQL container already exists"

    # Start Redis
    docker run -d --name meldestelle-redis \
        --network meldestelle-network \
        -p 6379:6379 \
        redis:7-alpine redis-server --appendonly yes 2>/dev/null || log_info "Redis container already exists"

    # Start Consul
    docker run -d --name meldestelle-consul \
        --network meldestelle-network \
        -p 8500:8500 \
        hashicorp/consul:1.15 agent -server -ui -node=server-1 -bootstrap-expect=1 -client=0.0.0.0 2>/dev/null || log_info "Consul container already exists"

    # Start Prometheus
    docker run -d --name meldestelle-prometheus \
        --network meldestelle-network \
        -p 9090:9090 \
        prom/prometheus:v2.47.0 \
        --config.file=/etc/prometheus/prometheus.yml \
        --storage.tsdb.path=/prometheus \
        --web.console.libraries=/etc/prometheus/console_libraries \
        --web.console.templates=/etc/prometheus/consoles \
        --storage.tsdb.retention.time=200h \
        --web.enable-lifecycle 2>/dev/null || log_info "Prometheus container already exists"

    # Start Grafana
    docker run -d --name meldestelle-grafana \
        --network meldestelle-network \
        -p 3000:3000 \
        -e GF_SECURITY_ADMIN_USER=admin \
        -e GF_SECURITY_ADMIN_PASSWORD=admin \
        grafana/grafana:10.1.0 2>/dev/null || log_info "Grafana container already exists"

    # Start Keycloak
    docker run -d --name meldestelle-keycloak \
        --network meldestelle-network \
        -p 8180:8080 \
        -e KEYCLOAK_ADMIN=admin \
        -e KEYCLOAK_ADMIN_PASSWORD=admin \
        quay.io/keycloak/keycloak:23.0 start-dev 2>/dev/null || log_info "Keycloak container already exists"

    # Give services time to initialize
    log_info "Waiting 30 seconds for services to initialize..."
    sleep 30

    # Wait for services to be ready
    local services=(
        "postgres:http://localhost:5432:PostgreSQL"
        "redis:redis://localhost:6379:Redis"
        "consul:http://localhost:8500/v1/status/leader:Consul"
        "prometheus:http://localhost:9090/-/healthy:Prometheus"
        "grafana:http://localhost:3000/api/health:Grafana"
        "keycloak:http://localhost:8180/health/ready:Keycloak"
    )

    for service_info in "${services[@]}"; do
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
}

# Function to test API Gateway
test_api_gateway() {
    log_info "========================================="
    log_info "Testing API Gateway"
    log_info "========================================="

    # Start API Gateway
    log_info "Starting API Gateway..."
    docker-compose up -d api-gateway

    # Wait for API Gateway to be ready
    wait_for_health_check "API Gateway" "http://localhost:8081/actuator/health" $MAX_RETRIES || return 1

    # Check specific actuator endpoints
    local endpoints=("health" "info" "metrics")
    for endpoint in "${endpoints[@]}"; do
        local url="http://localhost:8081/actuator/$endpoint"
        if curl -f -s --max-time 5 "$url" > /dev/null 2>&1; then
            log_success "API Gateway $endpoint endpoint is accessible"
        else
            log_warning "API Gateway $endpoint endpoint is not accessible at $url"
        fi
    done

    check_service_logs "API Gateway" "meldestelle-api-gateway"
    log_success "API Gateway is healthy!"
}

# Function to test application services
test_application_services() {
    log_info "========================================="
    log_info "Testing Application Services"
    log_info "========================================="

    # Start application services
    log_info "Starting application services..."
    docker-compose -f docker-compose.yml -f docker-compose.services.yml up -d

    # Define application services with their health check URLs
    local app_services=(
        "ping-service:http://localhost:8082/actuator/health:Ping Service"
        "members-service:http://localhost:8083/actuator/health:Members Service"
        "horses-service:http://localhost:8084/actuator/health:Horses Service"
        "events-service:http://localhost:8085/actuator/health:Events Service"
        "masterdata-service:http://localhost:8086/actuator/health:Masterdata Service"
    )

    for service_info in "${app_services[@]}"; do
        IFS=':' read -r service_name health_url description <<< "$service_info"
        wait_for_health_check "$description" "$health_url" $MAX_RETRIES || return 1
        check_service_logs "$description" "meldestelle-$service_name"
    done

    log_success "All application services are healthy!"
}

# Function to test client services
test_client_services() {
    log_info "========================================="
    log_info "Testing Client Services"
    log_info "========================================="

    # Start client services
    log_info "Starting client services..."
    docker-compose -f docker-compose.yml -f docker-compose.services.yml -f docker-compose.clients.yml up -d

    # Define client services with their health check URLs
    local client_services=(
        "web-app:http://localhost:3000/health:Web Application"
        "auth-server:http://localhost:8087/actuator/health:Auth Server"
        "monitoring-server:http://localhost:8088/actuator/health:Monitoring Server"
    )

    for service_info in "${client_services[@]}"; do
        IFS=':' read -r service_name health_url description <<< "$service_info"
        wait_for_health_check "$description" "$health_url" $MAX_RETRIES || return 1
        check_service_logs "$description" "meldestelle-$service_name"
    done

    log_success "All client services are healthy!"
}

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

    log_info "Stopping and removing test containers..."

    # Stop and remove containers if they exist
    local containers=("meldestelle-postgres" "meldestelle-redis" "meldestelle-consul" "meldestelle-prometheus" "meldestelle-grafana" "meldestelle-keycloak" "meldestelle-api-gateway")

    for container in "${containers[@]}"; do
        if docker ps -a --format '{{.Names}}' | grep -q "^${container}$"; then
            log_info "Stopping and removing $container"
            docker stop "$container" >/dev/null 2>&1 || true
            docker rm "$container" >/dev/null 2>&1 || true
        fi
    done

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
    test_infrastructure_services || exit 1
    test_api_gateway || exit 1
    test_application_services || exit 1
    test_client_services || exit 1
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
    "infrastructure")
        test_infrastructure_services
        ;;
    "gateway")
        test_api_gateway
        ;;
    "services")
        test_application_services
        ;;
    "clients")
        test_client_services
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
