#!/bin/bash

# =============================================================================
# Enhanced Monitoring Setup Test Script
# =============================================================================
# This script provides comprehensive testing of the monitoring setup including
# Prometheus, Grafana, and Alertmanager with improved error handling, retry
# logic, cleanup options, and configuration validation.
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

readonly COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.yml}"
readonly MONITORING_SERVICES=("prometheus" "grafana" "alertmanager")
readonly STARTUP_TIMEOUT=120
readonly HEALTH_CHECK_TIMEOUT=30
readonly RETRY_COUNT=3
readonly RETRY_DELAY=10

# Service endpoints
readonly PROMETHEUS_URL="http://localhost:9090"
readonly GRAFANA_URL="http://localhost:3000"
readonly ALERTMANAGER_URL="http://localhost:9093"

# Configuration files
readonly CONFIG_FILES=(
    "config/monitoring/prometheus.yml"
    "config/monitoring/grafana/provisioning/dashboards/dashboard.yml"
    "config/monitoring/grafana/provisioning/datasources/prometheus.yml"
)

# =============================================================================
# Cleanup Function
# =============================================================================

cleanup() {
    if [[ "${CLEANUP_SERVICES:-true}" == "true" ]]; then
        log_info "Cleaning up monitoring services..."

        # Stop monitoring services
        if docker-compose -f "$COMPOSE_FILE" ps | grep -q "prometheus\|grafana\|alertmanager"; then
            log_info "Stopping monitoring services..."
            docker-compose -f "$COMPOSE_FILE" stop "${MONITORING_SERVICES[@]}" >/dev/null 2>&1 || true
        fi

        # Remove containers if requested
        if [[ "${REMOVE_CONTAINERS:-false}" == "true" ]]; then
            log_info "Removing monitoring containers..."
            docker-compose -f "$COMPOSE_FILE" rm -f "${MONITORING_SERVICES[@]}" >/dev/null 2>&1 || true
        fi

        log_info "Cleanup completed"
    else
        log_info "Cleanup skipped (CLEANUP_SERVICES=false)"
    fi
}

# =============================================================================
# Configuration Validation Functions
# =============================================================================

validate_configuration() {
    log_section "Configuration Validation"

    # Check docker-compose file
    check_file "$COMPOSE_FILE" "Docker Compose file" || return 1

    # Validate docker-compose syntax
    log_info "Validating docker-compose syntax..."
    if docker-compose -f "$COMPOSE_FILE" config >/dev/null 2>&1; then
        print_status "OK" "Docker Compose file syntax is valid"
    else
        print_status "ERROR" "Docker Compose file has syntax errors"
        return 1
    fi

    # Check required services are defined
    for service in "${MONITORING_SERVICES[@]}"; do
        if docker-compose -f "$COMPOSE_FILE" config | grep -q "^  ${service}:"; then
            print_status "OK" "Service '$service' is defined in docker-compose"
        else
            print_status "ERROR" "Service '$service' is not defined in docker-compose"
        fi
    done

    # Check configuration files
    for config_file in "${CONFIG_FILES[@]}"; do
        if [[ -f "$config_file" ]]; then
            print_status "OK" "Configuration file exists: $config_file"

            # Validate specific configuration files
            case "$config_file" in
                *prometheus.yml)
                    validate_prometheus_config "$config_file"
                    ;;
                *grafana*)
                    validate_grafana_config "$config_file"
                    ;;
            esac
        else
            print_status "WARNING" "Configuration file missing: $config_file"
        fi
    done

    return 0
}

validate_prometheus_config() {
    local config_file=$1

    # Check for required sections
    if grep -q "global:" "$config_file" && grep -q "scrape_configs:" "$config_file"; then
        print_status "OK" "Prometheus configuration has required sections"
    else
        print_status "WARNING" "Prometheus configuration may be incomplete"
    fi

    # Check for application scrape targets
    if grep -q "meldestelle" "$config_file"; then
        print_status "OK" "Prometheus configured to scrape Meldestelle application"
    else
        print_status "WARNING" "Prometheus may not be configured to scrape application metrics"
    fi
}

validate_grafana_config() {
    local config_file=$1

    # Basic validation for Grafana config files
    if [[ "$config_file" == *"datasources"* ]]; then
        if grep -q "prometheus" "$config_file"; then
            print_status "OK" "Grafana datasource configuration includes Prometheus"
        else
            print_status "WARNING" "Grafana datasource configuration may not include Prometheus"
        fi
    fi
}

# =============================================================================
# Service Management Functions
# =============================================================================

start_monitoring_services() {
    log_section "Starting Monitoring Services"

    # Check Docker availability
    check_docker || return 1
    check_docker_compose || return 1

    # Start services with timeout
    log_info "Starting monitoring stack: ${MONITORING_SERVICES[*]}"

    if run_with_timeout "$STARTUP_TIMEOUT" "Start monitoring services" \
        docker-compose -f "$COMPOSE_FILE" up -d "${MONITORING_SERVICES[@]}"; then
        print_status "OK" "Monitoring services started successfully"
    else
        print_status "ERROR" "Failed to start monitoring services"
        return 1
    fi

    # Wait for services to be ready
    log_info "Waiting for services to be ready..."
    sleep 15  # Initial wait for containers to initialize

    return 0
}

# =============================================================================
# Health Check Functions
# =============================================================================

check_prometheus() {
    log_section "Prometheus Health Check"

    local health_url="${PROMETHEUS_URL}/-/healthy"
    local ready_url="${PROMETHEUS_URL}/-/ready"

    # Check if Prometheus is healthy
    if check_http_endpoint "$health_url" "Prometheus health" "$HEALTH_CHECK_TIMEOUT" "$RETRY_COUNT"; then
        print_status "OK" "Prometheus is healthy"
    else
        print_status "ERROR" "Prometheus health check failed"
        return 1
    fi

    # Check if Prometheus is ready
    if check_http_endpoint "$ready_url" "Prometheus readiness" "$HEALTH_CHECK_TIMEOUT" 1; then
        print_status "OK" "Prometheus is ready"
    else
        print_status "WARNING" "Prometheus readiness check failed"
    fi

    # Check Prometheus configuration
    local config_url="${PROMETHEUS_URL}/api/v1/status/config"
    if check_http_endpoint "$config_url" "Prometheus configuration" 10 1; then
        print_status "OK" "Prometheus configuration is accessible"
    else
        print_status "WARNING" "Prometheus configuration endpoint not accessible"
    fi

    # Check targets
    check_prometheus_targets

    return 0
}

check_prometheus_targets() {
    log_info "Checking Prometheus targets..."

    local targets_url="${PROMETHEUS_URL}/api/v1/targets"
    local targets_response

    targets_response=$(curl -sf "$targets_url" 2>/dev/null || echo "")

    if [[ -n "$targets_response" ]]; then
        # Check for application targets
        if echo "$targets_response" | grep -q "meldestelle"; then
            print_status "OK" "Prometheus can discover application targets"
        else
            print_status "WARNING" "No application targets found in Prometheus"
            log_info "Make sure the application is running and exposing metrics"
        fi

        # Check for healthy targets
        local healthy_targets
        healthy_targets=$(echo "$targets_response" | grep -o '"health":"up"' | wc -l)
        if [[ "$healthy_targets" -gt 0 ]]; then
            print_status "OK" "Found $healthy_targets healthy targets"
        else
            print_status "WARNING" "No healthy targets found"
        fi
    else
        print_status "WARNING" "Could not retrieve Prometheus targets"
    fi
}

check_grafana() {
    log_section "Grafana Health Check"

    local health_url="${GRAFANA_URL}/api/health"
    local datasources_url="${GRAFANA_URL}/api/datasources"

    # Check if Grafana is healthy
    if check_http_endpoint "$health_url" "Grafana health" "$HEALTH_CHECK_TIMEOUT" "$RETRY_COUNT"; then
        print_status "OK" "Grafana is healthy"

        # Parse health response
        local health_response
        health_response=$(curl -sf "$health_url" 2>/dev/null || echo "")
        if [[ "$health_response" == *"ok"* ]]; then
            print_status "OK" "Grafana health status is OK"
        else
            print_status "WARNING" "Grafana health status unclear: $health_response"
        fi
    else
        print_status "ERROR" "Grafana health check failed"
        return 1
    fi

    # Check datasources (requires authentication, so this might fail)
    log_info "Checking Grafana datasources..."
    local datasources_response
    datasources_response=$(curl -sf -u "admin:admin" "$datasources_url" 2>/dev/null || echo "")

    if [[ -n "$datasources_response" ]] && [[ "$datasources_response" != "Unauthorized" ]]; then
        if echo "$datasources_response" | grep -q "prometheus"; then
            print_status "OK" "Grafana has Prometheus datasource configured"
        else
            print_status "WARNING" "Prometheus datasource not found in Grafana"
        fi
    else
        print_status "INFO" "Could not check Grafana datasources (authentication required)"
    fi

    return 0
}

check_alertmanager() {
    log_section "Alertmanager Health Check"

    local health_url="${ALERTMANAGER_URL}/-/healthy"
    local ready_url="${ALERTMANAGER_URL}/-/ready"
    local status_url="${ALERTMANAGER_URL}/api/v1/status"

    # Check if Alertmanager is healthy
    if check_http_endpoint "$health_url" "Alertmanager health" "$HEALTH_CHECK_TIMEOUT" "$RETRY_COUNT"; then
        print_status "OK" "Alertmanager is healthy"
    else
        print_status "ERROR" "Alertmanager health check failed"
        return 1
    fi

    # Check if Alertmanager is ready
    if check_http_endpoint "$ready_url" "Alertmanager readiness" "$HEALTH_CHECK_TIMEOUT" 1; then
        print_status "OK" "Alertmanager is ready"
    else
        print_status "WARNING" "Alertmanager readiness check failed"
    fi

    # Check Alertmanager status
    if check_http_endpoint "$status_url" "Alertmanager status" 10 1; then
        print_status "OK" "Alertmanager status endpoint is accessible"
    else
        print_status "WARNING" "Alertmanager status endpoint not accessible"
    fi

    return 0
}

# =============================================================================
# Integration Tests
# =============================================================================

test_monitoring_integration() {
    log_section "Monitoring Integration Tests"

    # Test Prometheus-Grafana integration
    log_info "Testing Prometheus-Grafana integration..."

    # Check if Prometheus metrics are accessible from Grafana's perspective
    local prometheus_query_url="${PROMETHEUS_URL}/api/v1/query?query=up"
    if check_http_endpoint "$prometheus_query_url" "Prometheus query API" 10 1; then
        print_status "OK" "Prometheus query API is accessible for Grafana"
    else
        print_status "WARNING" "Prometheus query API may not be accessible for Grafana"
    fi

    # Test alerting rules
    log_info "Checking alerting rules..."
    local rules_url="${PROMETHEUS_URL}/api/v1/rules"
    local rules_response
    rules_response=$(curl -sf "$rules_url" 2>/dev/null || echo "")

    if [[ -n "$rules_response" ]]; then
        if echo "$rules_response" | grep -q "meldestelle"; then
            print_status "OK" "Meldestelle alerting rules are loaded"
        else
            print_status "WARNING" "No Meldestelle-specific alerting rules found"
        fi
    else
        print_status "WARNING" "Could not retrieve alerting rules"
    fi

    return 0
}

# =============================================================================
# Performance and Load Tests
# =============================================================================

test_monitoring_performance() {
    log_section "Monitoring Performance Tests"

    # Test Prometheus query performance
    log_info "Testing Prometheus query performance..."

    local start_time
    local end_time
    local duration

    start_time=$(date +%s%N)
    curl -sf "${PROMETHEUS_URL}/api/v1/query?query=up" >/dev/null 2>&1
    local query_result=$?
    end_time=$(date +%s%N)

    duration=$(( (end_time - start_time) / 1000000 ))  # Convert to milliseconds

    if [[ $query_result -eq 0 ]]; then
        if [[ $duration -lt 1000 ]]; then
            print_status "OK" "Prometheus query performance is good (${duration}ms)"
        else
            print_status "WARNING" "Prometheus query performance is slow (${duration}ms)"
        fi
    else
        print_status "WARNING" "Prometheus query performance test failed"
    fi

    # Test Grafana response time
    log_info "Testing Grafana response time..."

    start_time=$(date +%s%N)
    curl -sf "${GRAFANA_URL}/api/health" >/dev/null 2>&1
    local grafana_result=$?
    end_time=$(date +%s%N)

    duration=$(( (end_time - start_time) / 1000000 ))

    if [[ $grafana_result -eq 0 ]]; then
        if [[ $duration -lt 2000 ]]; then
            print_status "OK" "Grafana response time is good (${duration}ms)"
        else
            print_status "WARNING" "Grafana response time is slow (${duration}ms)"
        fi
    else
        print_status "WARNING" "Grafana response time test failed"
    fi

    return 0
}

# =============================================================================
# Main Execution
# =============================================================================

show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --no-cleanup          Don't stop services after testing"
    echo "  --remove-containers   Remove containers after testing"
    echo "  --config-only         Only validate configuration, don't start services"
    echo "  --help               Show this help message"
    echo ""
    echo "Environment Variables:"
    echo "  COMPOSE_FILE         Docker compose file to use (default: docker-compose.yml)"
    echo "  CLEANUP_SERVICES     Whether to cleanup services (default: true)"
    echo "  REMOVE_CONTAINERS    Whether to remove containers (default: false)"
}

main() {
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --no-cleanup)
                export CLEANUP_SERVICES=false
                shift
                ;;
            --remove-containers)
                export REMOVE_CONTAINERS=true
                shift
                ;;
            --config-only)
                local CONFIG_ONLY=true
                shift
                ;;
            --help)
                show_usage
                exit 0
                ;;
            *)
                log_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done

    log_section "Enhanced Monitoring Setup Test"

    log_info "Starting comprehensive monitoring tests..."
    log_info "Compose file: $COMPOSE_FILE"
    log_info "Test timestamp: $(date)"

    # Always validate configuration
    validate_configuration || exit 1

    # If config-only mode, exit after validation
    if [[ "${CONFIG_ONLY:-false}" == "true" ]]; then
        log_info "Configuration validation completed (config-only mode)"
        print_summary "Monitoring Configuration Validation"
        exit 0
    fi

    # Run all tests
    local test_results=()

    start_monitoring_services && test_results+=("Startup: PASS") || test_results+=("Startup: FAIL")
    check_prometheus && test_results+=("Prometheus: PASS") || test_results+=("Prometheus: FAIL")
    check_grafana && test_results+=("Grafana: PASS") || test_results+=("Grafana: FAIL")
    check_alertmanager && test_results+=("Alertmanager: PASS") || test_results+=("Alertmanager: FAIL")
    test_monitoring_integration && test_results+=("Integration: PASS") || test_results+=("Integration: FAIL")
    test_monitoring_performance && test_results+=("Performance: PASS") || test_results+=("Performance: FAIL")

    # Print test results summary
    log_section "Test Results Summary"
    for result in "${test_results[@]}"; do
        if [[ "$result" == *"PASS" ]]; then
            log_success "$result"
        else
            log_error "$result"
        fi
    done

    # Print access information
    log_section "Monitoring Access Information"
    log_info "Prometheus: ${PROMETHEUS_URL}"
    log_info "Grafana: ${GRAFANA_URL} (default credentials: admin/admin)"
    log_info "Alertmanager: ${ALERTMANAGER_URL}"

    # Print final summary
    print_summary "Enhanced Monitoring Test"
}

# Run main function
main "$@"
