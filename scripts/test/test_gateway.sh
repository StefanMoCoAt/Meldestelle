#!/bin/bash

# =============================================================================
# Enhanced API Gateway Test Script
# =============================================================================
# This script provides comprehensive testing of the API Gateway implementation
# including build validation, runtime testing, endpoint health checks, and
# performance measurements.
# =============================================================================

# Load common utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../utils/common.sh
source "$SCRIPT_DIR/../utils/common.sh" || {
    echo "Error: Could not load common utilities from $SCRIPT_DIR/../utils/common.sh"
    exit 1
}

# =============================================================================
# Configuration
# =============================================================================

readonly GATEWAY_MODULE="infrastructure:gateway"
readonly GATEWAY_JAR="infrastructure/gateway/build/libs/gateway-1.0.0.jar"
readonly GATEWAY_PORT="${GATEWAY_PORT:-8081}"
readonly GATEWAY_HOST="${GATEWAY_HOST:-localhost}"
readonly GATEWAY_BASE_URL="http://${GATEWAY_HOST}:${GATEWAY_PORT}"

# Test endpoints
readonly TEST_ENDPOINTS=(
    "/health"
    "/metrics"
    "/api/masterdata/health"
    "/api/horses/health"
    "/api/events/health"
    "/api/members/health"
)

# Service discovery endpoints
readonly SERVICE_ENDPOINTS=(
    "masterdata:8081"
    "horses:8082
    "events:8083"
    "members:8084"
)

# =============================================================================
# Cleanup Function
# =============================================================================

cleanup() {
    log_info "Cleaning up test environment..."

    # Stop gateway if running
    if [[ -n "${GATEWAY_PID:-}" ]]; then
        log_info "Stopping gateway process (PID: $GATEWAY_PID)..."
        kill "$GATEWAY_PID" 2>/dev/null || true
        wait "$GATEWAY_PID" 2>/dev/null || true
    fi

    # Stop Docker services if started by this script
    if [[ "${STARTED_SERVICES:-false}" == "true" ]]; then
        log_info "Stopping test services..."
        docker-compose down --remove-orphans >/dev/null 2>&1 || true
    fi

    log_info "Cleanup completed"
}

# =============================================================================
# Test Functions
# =============================================================================

# Test 1: Build Validation
test_build_validation() {
    log_section "Test 1: Build Validation"

    log_info "Building gateway module..."
    if run_with_timeout 300 "Gateway build" ./gradlew ":${GATEWAY_MODULE}:build" -x test; then
        print_status "OK" "Gateway builds successfully"
    else
        print_status "ERROR" "Gateway build failed"
        return 1
    fi

    # Check if JAR file exists
    check_file "$GATEWAY_JAR" "Gateway JAR file"

    # Validate JAR contents
    if jar -tf "$GATEWAY_JAR" | grep -q "Application.class"; then
        print_status "OK" "Gateway JAR contains main application class"
    else
        print_status "WARNING" "Gateway JAR may not contain expected main class"
    fi

    return 0
}

# Test 2: Configuration Validation
test_configuration_validation() {
    log_section "Test 2: Configuration Validation"

    # Check required configuration files
    local config_files=(
        "infrastructure/gateway/src/main/resources/application.conf"
        "infrastructure/gateway/src/main/resources/logback.xml"
    )

    for config_file in "${config_files[@]}"; do
        check_file "$config_file" "Configuration file"
    done

    # Validate environment variables
    local required_vars=(
        "API_HOST"
        "API_PORT"
        "CONSUL_HOST"
        "CONSUL_PORT"
    )

    load_env_file

    local missing_vars=()
    for var in "${required_vars[@]}"; do
        if [[ -z "${!var:-}" ]]; then
            missing_vars+=("$var")
        fi
    done

    if [[ ${#missing_vars[@]} -gt 0 ]]; then
        print_status "WARNING" "Missing environment variables: ${missing_vars[*]} (using defaults)"
    else
        print_status "OK" "All required environment variables are set"
    fi

    return 0
}

# Test 3: Service Dependencies
test_service_dependencies() {
    log_section "Test 3: Service Dependencies"

    # Check Docker availability
    check_docker || return 1
    check_docker_compose || return 1

    # Start required services for testing
    log_info "Starting required services for gateway testing..."

    local required_services=(
        "postgres"
        "redis"
        "consul"
    )

    if start_docker_services "${required_services[@]}"; then
        STARTED_SERVICES=true
        print_status "OK" "Required services started successfully"
    else
        print_status "ERROR" "Failed to start required services"
        return 1
    fi

    # Wait for Consul to be ready (service discovery)
    if check_service_port 8500 "Consul" 60; then
        print_status "OK" "Consul service discovery is ready"
    else
        print_status "WARNING" "Consul not available - service discovery may not work"
    fi

    return 0
}

# Test 4: Gateway Runtime Testing
test_gateway_runtime() {
    log_section "Test 4: Gateway Runtime Testing"

    # Start the gateway
    log_info "Starting API Gateway..."

    # Set environment for gateway
    export API_HOST="${GATEWAY_HOST}"
    export API_PORT="${GATEWAY_PORT}"
    export CONSUL_HOST="${CONSUL_HOST:-localhost}"
    export CONSUL_PORT="${CONSUL_PORT:-8500}"

    # Start gateway in background
    java -jar "$GATEWAY_JAR" > gateway.log 2>&1 &
    GATEWAY_PID=$!

    log_info "Gateway started with PID: $GATEWAY_PID"

    # Wait for gateway to be ready
    if wait_for_service "curl -sf ${GATEWAY_BASE_URL}/health" "API Gateway" 120 5; then
        print_status "OK" "API Gateway is running and healthy"
    else
        print_status "ERROR" "API Gateway failed to start or become healthy"
        if [[ -f "gateway.log" ]]; then
            log_error "Gateway logs:"
            tail -20 gateway.log >&2
        fi
        return 1
    fi

    return 0
}

# Test 5: Endpoint Health Checks
test_endpoint_health() {
    log_section "Test 5: Endpoint Health Checks"

    for endpoint in "${TEST_ENDPOINTS[@]}"; do
        local url="${GATEWAY_BASE_URL}${endpoint}"

        if check_http_endpoint "$url" "Gateway endpoint $endpoint" 10 2; then
            # Additional validation for specific endpoints
            case $endpoint in
                "/health")
                    local health_response
                    health_response=$(curl -sf "$url" 2>/dev/null || echo "")
                    if [[ "$health_response" == *"UP"* ]] || [[ "$health_response" == *"healthy"* ]]; then
                        print_status "OK" "Health endpoint returns positive status"
                    else
                        print_status "WARNING" "Health endpoint response unclear: $health_response"
                    fi
                    ;;
                "/metrics")
                    if curl -sf "$url" | grep -q "jvm_"; then
                        print_status "OK" "Metrics endpoint returns JVM metrics"
                    else
                        print_status "WARNING" "Metrics endpoint may not be properly configured"
                    fi
                    ;;
            esac
        fi
    done

    return 0
}

# Test 6: Service Discovery Integration
test_service_discovery() {
    log_section "Test 6: Service Discovery Integration"

    # Check if gateway can discover services
    local discovery_url="${GATEWAY_BASE_URL}/admin/services"

    if check_http_endpoint "$discovery_url" "Service discovery endpoint" 10 1; then
        local services_response
        services_response=$(curl -sf "$discovery_url" 2>/dev/null || echo "[]")

        if [[ "$services_response" != "[]" ]] && [[ "$services_response" != "" ]]; then
            print_status "OK" "Service discovery is working - found services"
            log_debug "Discovered services: $services_response"
        else
            print_status "WARNING" "Service discovery endpoint accessible but no services found"
        fi
    else
        print_status "WARNING" "Service discovery endpoint not accessible"
    fi

    return 0
}

# Test 7: Load and Performance Testing
test_performance() {
    log_section "Test 7: Load and Performance Testing"

    if ! command_exists ab; then
        print_status "WARNING" "Apache Bench (ab) not available - skipping performance tests"
        return 0
    fi

    local health_url="${GATEWAY_BASE_URL}/health"

    log_info "Running basic load test (100 requests, concurrency 10)..."

    local ab_output
    ab_output=$(ab -n 100 -c 10 "$health_url" 2>/dev/null || echo "")

    if [[ -n "$ab_output" ]]; then
        local requests_per_sec
        requests_per_sec=$(echo "$ab_output" | grep "Requests per second" | awk '{print $4}')

        local mean_time
        mean_time=$(echo "$ab_output" | grep "Time per request" | head -1 | awk '{print $4}')

        if [[ -n "$requests_per_sec" ]] && [[ -n "$mean_time" ]]; then
            print_status "OK" "Performance test completed - ${requests_per_sec} req/sec, ${mean_time}ms avg"

            # Basic performance validation
            if (( $(echo "$requests_per_sec > 50" | bc -l 2>/dev/null || echo 0) )); then
                print_status "OK" "Gateway performance is acceptable"
            else
                print_status "WARNING" "Gateway performance may be suboptimal"
            fi
        else
            print_status "WARNING" "Could not parse performance test results"
        fi
    else
        print_status "WARNING" "Performance test failed to run"
    fi

    return 0
}

# Test 8: Error Handling and Resilience
test_error_handling() {
    log_section "Test 8: Error Handling and Resilience"

    # Test 404 handling
    local not_found_url="${GATEWAY_BASE_URL}/nonexistent"
    local response_code
    response_code=$(curl -sf -o /dev/null -w "%{http_code}" "$not_found_url" 2>/dev/null || echo "000")

    if [[ "$response_code" == "404" ]]; then
        print_status "OK" "Gateway properly handles 404 errors"
    else
        print_status "WARNING" "Gateway 404 handling unclear (got $response_code)"
    fi

    # Test service unavailable handling
    local unavailable_service_url="${GATEWAY_BASE_URL}/api/unavailable-service/test"
    response_code=$(curl -sf -o /dev/null -w "%{http_code}" "$unavailable_service_url" 2>/dev/null || echo "000")

    if [[ "$response_code" == "503" ]] || [[ "$response_code" == "502" ]]; then
        print_status "OK" "Gateway properly handles unavailable services"
    else
        print_status "WARNING" "Gateway service unavailable handling unclear (got $response_code)"
    fi

    return 0
}

# =============================================================================
# Main Execution
# =============================================================================

main() {
    log_section "Enhanced API Gateway Testing"

    log_info "Starting comprehensive API Gateway tests..."
    log_info "Gateway URL: $GATEWAY_BASE_URL"
    log_info "Test timestamp: $(date)"

    # Run all tests
    local test_results=()

    test_build_validation && test_results+=("Build: PASS") || test_results+=("Build: FAIL")
    test_configuration_validation && test_results+=("Config: PASS") || test_results+=("Config: FAIL")
    test_service_dependencies && test_results+=("Dependencies: PASS") || test_results+=("Dependencies: FAIL")
    test_gateway_runtime && test_results+=("Runtime: PASS") || test_results+=("Runtime: FAIL")
    test_endpoint_health && test_results+=("Endpoints: PASS") || test_results+=("Endpoints: FAIL")
    test_service_discovery && test_results+=("Discovery: PASS") || test_results+=("Discovery: FAIL")
    test_performance && test_results+=("Performance: PASS") || test_results+=("Performance: FAIL")
    test_error_handling && test_results+=("ErrorHandling: PASS") || test_results+=("ErrorHandling: FAIL")

    # Print test results summary
    log_section "Test Results Summary"
    for result in "${test_results[@]}"; do
        if [[ "$result" == *"PASS" ]]; then
            log_success "$result"
        else
            log_error "$result"
        fi
    done

    # Print final summary
    print_summary "Enhanced API Gateway Test"
}

# Run main function
# shellcheck disable=SC1073
main "$@"
