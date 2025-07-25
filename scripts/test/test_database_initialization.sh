#!/bin/bash

# =============================================================================
# Enhanced Database Initialization Test Script
# =============================================================================
# This script provides comprehensive testing of database initialization and
# configuration with actual connection testing, schema validation, performance
# testing, and cleanup capabilities.
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
readonly DB_SERVICES=("postgres" "redis")
readonly BUILD_TIMEOUT=300
readonly DB_STARTUP_TIMEOUT=120
readonly CONNECTION_TIMEOUT=30

# Database configuration
readonly DB_HOST="${DB_HOST:-localhost}"
readonly DB_PORT="${DB_PORT:-5432}"
readonly DB_NAME="${DB_NAME:-meldestelle_test}"
readonly DB_USER="${DB_USER:-meldestelle_user}"
readonly DB_PASSWORD="${DB_PASSWORD:-meldestelle_password}"

# Redis configuration
readonly REDIS_HOST="${REDIS_HOST:-localhost}"
readonly REDIS_PORT="${REDIS_PORT:-6379}"

# Service modules
readonly SERVICE_MODULES=(
    "infrastructure:gateway"
    "horses:horses-service"
    "events:events-service"
    "masterdata:masterdata-service"
    "members:members-service"
)

# Test database name
readonly TEST_DB_NAME="meldestelle_test_$(date +%s)"

# =============================================================================
# Cleanup Function
# =============================================================================

cleanup() {
    log_info "Cleaning up test environment..."

    # Drop test database if created
    if [[ "${TEST_DB_CREATED:-false}" == "true" ]]; then
        log_info "Dropping test database: $TEST_DB_NAME"
        PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres \
            -c "DROP DATABASE IF EXISTS $TEST_DB_NAME;" >/dev/null 2>&1 || true
    fi

    # Stop database services if started by this script
    if [[ "${STARTED_DB_SERVICES:-false}" == "true" ]]; then
        log_info "Stopping database services..."
        docker-compose -f "$COMPOSE_FILE" stop "${DB_SERVICES[@]}" >/dev/null 2>&1 || true
    fi

    # Clean up temporary files
    rm -f /tmp/db_test_*.sql /tmp/db_performance_*.log 2>/dev/null || true

    log_info "Cleanup completed"
}

# =============================================================================
# Database Setup Functions
# =============================================================================

setup_database_services() {
    log_section "Database Services Setup"

    # Check Docker availability
    check_docker || return 1
    check_docker_compose || return 1

    # Start database services
    log_info "Starting database services: ${DB_SERVICES[*]}"

    if run_with_timeout "$DB_STARTUP_TIMEOUT" "Start database services" \
        docker-compose -f "$COMPOSE_FILE" up -d "${DB_SERVICES[@]}"; then
        STARTED_DB_SERVICES=true
        print_status "OK" "Database services started successfully"
    else
        print_status "ERROR" "Failed to start database services"
        return 1
    fi

    # Wait for PostgreSQL to be ready
    if wait_for_service "PGPASSWORD=$DB_PASSWORD pg_isready -h $DB_HOST -p $DB_PORT -U $DB_USER" \
        "PostgreSQL" "$CONNECTION_TIMEOUT" 5; then
        print_status "OK" "PostgreSQL is ready"
    else
        print_status "ERROR" "PostgreSQL failed to become ready"
        return 1
    fi

    # Wait for Redis to be ready
    if wait_for_service "redis-cli -h $REDIS_HOST -p $REDIS_PORT ping | grep -q PONG" \
        "Redis" "$CONNECTION_TIMEOUT" 5; then
        print_status "OK" "Redis is ready"
    else
        print_status "ERROR" "Redis failed to become ready"
        return 1
    fi

    return 0
}

# =============================================================================
# Environment Validation Functions
# =============================================================================

validate_environment() {
    log_section "Environment Validation"

    # Load environment file
    load_env_file

    # Validate required environment variables
    local required_vars=(
        "DB_HOST" "DB_PORT" "DB_NAME" "DB_USER" "DB_PASSWORD"
        "REDIS_HOST" "REDIS_PORT"
    )

    validate_env_vars "${required_vars[@]}" || return 1

    # Check for required tools
    local required_tools=("psql" "redis-cli")
    for tool in "${required_tools[@]}"; do
        if command_exists "$tool"; then
            print_status "OK" "$tool is available"
        else
            print_status "WARNING" "$tool is not available - some tests may be skipped"
        fi
    done

    return 0
}

# =============================================================================
# Build Testing Functions
# =============================================================================

test_service_builds() {
    log_section "Service Build Testing"

    local build_results=()

    # Test each service module build
    for module in "${SERVICE_MODULES[@]}"; do
        log_info "Building module: $module"

        if run_with_timeout "$BUILD_TIMEOUT" "Build $module" \
            ./gradlew ":${module}:build" -x test; then
            print_status "OK" "$module builds successfully"
            build_results+=("$module: PASS")
        else
            print_status "ERROR" "$module build failed"
            build_results+=("$module: FAIL")
        fi
    done

    # Summary of build results
    log_info "Build Results Summary:"
    for result in "${build_results[@]}"; do
        if [[ "$result" == *"PASS" ]]; then
            log_success "$result"
        else
            log_error "$result"
        fi
    done

    # Check if any builds failed
    if echo "${build_results[*]}" | grep -q "FAIL"; then
        print_status "ERROR" "One or more service builds failed"
        return 1
    else
        print_status "OK" "All service builds successful"
        return 0
    fi
}

# =============================================================================
# Database Connection Testing Functions
# =============================================================================

test_database_connections() {
    log_section "Database Connection Testing"

    # Test PostgreSQL connection
    test_postgresql_connection || return 1

    # Test Redis connection
    test_redis_connection || return 1

    return 0
}

test_postgresql_connection() {
    log_info "Testing PostgreSQL connection..."

    # Basic connection test
    if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres \
        -c "SELECT version();" >/dev/null 2>&1; then
        print_status "OK" "PostgreSQL connection successful"
    else
        print_status "ERROR" "PostgreSQL connection failed"
        return 1
    fi

    # Test database creation
    log_info "Testing database creation..."
    if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres \
        -c "CREATE DATABASE $TEST_DB_NAME;" >/dev/null 2>&1; then
        TEST_DB_CREATED=true
        print_status "OK" "Test database created successfully"
    else
        print_status "ERROR" "Failed to create test database"
        return 1
    fi

    # Test connection to new database
    if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$TEST_DB_NAME" \
        -c "SELECT current_database();" >/dev/null 2>&1; then
        print_status "OK" "Connection to test database successful"
    else
        print_status "ERROR" "Failed to connect to test database"
        return 1
    fi

    return 0
}

test_redis_connection() {
    log_info "Testing Redis connection..."

    # Basic connection test
    if redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" ping | grep -q "PONG"; then
        print_status "OK" "Redis connection successful"
    else
        print_status "ERROR" "Redis connection failed"
        return 1
    fi

    # Test basic operations
    local test_key="test_key_$(date +%s)"
    local test_value="test_value_$(date +%s)"

    if redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" set "$test_key" "$test_value" >/dev/null 2>&1; then
        print_status "OK" "Redis SET operation successful"
    else
        print_status "ERROR" "Redis SET operation failed"
        return 1
    fi

    local retrieved_value
    retrieved_value=$(redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" get "$test_key" 2>/dev/null)
    if [[ "$retrieved_value" == "$test_value" ]]; then
        print_status "OK" "Redis GET operation successful"
    else
        print_status "ERROR" "Redis GET operation failed"
        return 1
    fi

    # Cleanup test key
    redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" del "$test_key" >/dev/null 2>&1 || true

    return 0
}

# =============================================================================
# Schema Validation Functions
# =============================================================================

test_schema_initialization() {
    log_section "Schema Initialization Testing"

    # Create test schema
    create_test_schema || return 1

    # Validate schema structure
    validate_schema_structure || return 1

    # Test schema constraints
    test_schema_constraints || return 1

    return 0
}

create_test_schema() {
    log_info "Creating test schema..."

    # Create a simple test table
    local create_table_sql="
    CREATE TABLE IF NOT EXISTS test_table (
        id SERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        email VARCHAR(255) UNIQUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    "

    if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$TEST_DB_NAME" \
        -c "$create_table_sql" >/dev/null 2>&1; then
        print_status "OK" "Test schema created successfully"
        return 0
    else
        print_status "ERROR" "Failed to create test schema"
        return 1
    fi
}

validate_schema_structure() {
    log_info "Validating schema structure..."

    # Check if table exists
    local table_exists
    table_exists=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$TEST_DB_NAME" \
        -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'test_table';" 2>/dev/null | tr -d ' ')

    if [[ "$table_exists" == "1" ]]; then
        print_status "OK" "Test table exists in schema"
    else
        print_status "ERROR" "Test table not found in schema"
        return 1
    fi

    # Check table columns
    local column_count
    column_count=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$TEST_DB_NAME" \
        -t -c "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'test_table';" 2>/dev/null | tr -d ' ')

    if [[ "$column_count" == "4" ]]; then
        print_status "OK" "Test table has correct number of columns"
    else
        print_status "WARNING" "Test table column count unexpected: $column_count"
    fi

    return 0
}

test_schema_constraints() {
    log_info "Testing schema constraints..."

    # Test NOT NULL constraint
    if ! PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$TEST_DB_NAME" \
        -c "INSERT INTO test_table (name) VALUES (NULL);" >/dev/null 2>&1; then
        print_status "OK" "NOT NULL constraint working correctly"
    else
        print_status "WARNING" "NOT NULL constraint may not be working"
    fi

    # Test UNIQUE constraint
    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$TEST_DB_NAME" \
        -c "INSERT INTO test_table (name, email) VALUES ('Test User', 'test@example.com');" >/dev/null 2>&1

    if ! PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$TEST_DB_NAME" \
        -c "INSERT INTO test_table (name, email) VALUES ('Another User', 'test@example.com');" >/dev/null 2>&1; then
        print_status "OK" "UNIQUE constraint working correctly"
    else
        print_status "WARNING" "UNIQUE constraint may not be working"
    fi

    return 0
}

# =============================================================================
# Performance Testing Functions
# =============================================================================

test_database_performance() {
    log_section "Database Performance Testing"

    # Test PostgreSQL performance
    test_postgresql_performance || return 1

    # Test Redis performance
    test_redis_performance || return 1

    return 0
}

test_postgresql_performance() {
    log_info "Testing PostgreSQL performance..."

    # Insert performance test
    local start_time
    local end_time
    local duration

    start_time=$(date +%s%N)

    # Insert 1000 test records
    for i in {1..1000}; do
        PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$TEST_DB_NAME" \
            -c "INSERT INTO test_table (name, email) VALUES ('User $i', 'user$i@example.com');" >/dev/null 2>&1
    done

    end_time=$(date +%s%N)
    duration=$(( (end_time - start_time) / 1000000 ))  # Convert to milliseconds

    if [[ $duration -lt 10000 ]]; then  # Less than 10 seconds
        print_status "OK" "PostgreSQL insert performance is good (${duration}ms for 1000 records)"
    else
        print_status "WARNING" "PostgreSQL insert performance is slow (${duration}ms for 1000 records)"
    fi

    # Query performance test
    start_time=$(date +%s%N)

    local record_count
    record_count=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$TEST_DB_NAME" \
        -t -c "SELECT COUNT(*) FROM test_table;" 2>/dev/null | tr -d ' ')

    end_time=$(date +%s%N)
    duration=$(( (end_time - start_time) / 1000000 ))

    if [[ $duration -lt 1000 ]] && [[ "$record_count" -gt 0 ]]; then
        print_status "OK" "PostgreSQL query performance is good (${duration}ms, $record_count records)"
    else
        print_status "WARNING" "PostgreSQL query performance may be suboptimal (${duration}ms)"
    fi

    return 0
}

test_redis_performance() {
    log_info "Testing Redis performance..."

    # Set performance test
    local start_time
    local end_time
    local duration

    start_time=$(date +%s%N)

    # Set 1000 test keys
    for i in {1..1000}; do
        redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" set "perf_test_$i" "value_$i" >/dev/null 2>&1
    done

    end_time=$(date +%s%N)
    duration=$(( (end_time - start_time) / 1000000 ))

    if [[ $duration -lt 5000 ]]; then  # Less than 5 seconds
        print_status "OK" "Redis SET performance is good (${duration}ms for 1000 keys)"
    else
        print_status "WARNING" "Redis SET performance is slow (${duration}ms for 1000 keys)"
    fi

    # Get performance test
    start_time=$(date +%s%N)

    for i in {1..100}; do
        redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" get "perf_test_$i" >/dev/null 2>&1
    done

    end_time=$(date +%s%N)
    duration=$(( (end_time - start_time) / 1000000 ))

    if [[ $duration -lt 1000 ]]; then  # Less than 1 second
        print_status "OK" "Redis GET performance is good (${duration}ms for 100 keys)"
    else
        print_status "WARNING" "Redis GET performance is slow (${duration}ms for 100 keys)"
    fi

    # Cleanup performance test keys
    redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" eval "
        for i=1,1000 do
            redis.call('del', 'perf_test_' .. i)
        end
    " 0 >/dev/null 2>&1 || true

    return 0
}

# =============================================================================
# Integration Testing Functions
# =============================================================================

test_database_integration() {
    log_section "Database Integration Testing"

    # Test DatabaseFactory usage
    test_database_factory_integration || return 1

    # Test service-specific database initialization
    test_service_database_integration || return 1

    return 0
}

test_database_factory_integration() {
    log_info "Testing DatabaseFactory integration..."

    # Check for direct Database.connect() calls in gateway
    local direct_connects
    direct_connects=$(find infrastructure/gateway/src -name "*.kt" -exec grep -l "Database\.connect(" {} \; 2>/dev/null || true)

    if [[ -z "$direct_connects" ]]; then
        print_status "OK" "No direct Database.connect() calls found in gateway"
    else
        print_status "ERROR" "Found direct Database.connect() calls in gateway: $direct_connects"
        return 1
    fi

    # Check for DatabaseFactory usage in gateway
    local factory_usage
    factory_usage=$(find infrastructure/gateway/src -name "*.kt" -exec grep -l "DatabaseFactory" {} \; 2>/dev/null || true)

    if [[ -n "$factory_usage" ]]; then
        print_status "OK" "DatabaseFactory usage found in gateway"
    else
        print_status "WARNING" "No DatabaseFactory usage found in gateway"
    fi

    return 0
}

test_service_database_integration() {
    log_info "Testing service database integration..."

    # Check that services don't call DatabaseFactory.init()
    local factory_inits
    factory_inits=$(find . -path "*/service/config/*" -name "*.kt" -exec grep -l "DatabaseFactory\.init(" {} \; 2>/dev/null || true)

    if [[ -z "$factory_inits" ]]; then
        print_status "OK" "No DatabaseFactory.init() calls found in service configurations"
    else
        print_status "ERROR" "Found DatabaseFactory.init() calls in service configurations: $factory_inits"
        return 1
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
    echo "  --skip-builds         Skip service build testing"
    echo "  --skip-performance    Skip performance testing"
    echo "  --keep-test-data      Don't cleanup test data after testing"
    echo "  --help               Show this help message"
    echo ""
    echo "Environment Variables:"
    echo "  DB_HOST              Database host (default: localhost)"
    echo "  DB_PORT              Database port (default: 5432)"
    echo "  DB_NAME              Database name (default: meldestelle_test)"
    echo "  DB_USER              Database user (default: meldestelle_user)"
    echo "  DB_PASSWORD          Database password (default: meldestelle_password)"
    echo "  REDIS_HOST           Redis host (default: localhost)"
    echo "  REDIS_PORT           Redis port (default: 6379)"
}

main() {
    local SKIP_BUILDS=false
    local SKIP_PERFORMANCE=false
    local KEEP_TEST_DATA=false

    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --skip-builds)
                SKIP_BUILDS=true
                shift
                ;;
            --skip-performance)
                SKIP_PERFORMANCE=true
                shift
                ;;
            --keep-test-data)
                KEEP_TEST_DATA=true
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

    log_section "Enhanced Database Initialization Test"

    log_info "Starting comprehensive database tests..."
    log_info "Test database: $TEST_DB_NAME"
    log_info "Test timestamp: $(date)"

    # Run all tests
    local test_results=()

    validate_environment && test_results+=("Environment: PASS") || test_results+=("Environment: FAIL")
    setup_database_services && test_results+=("Setup: PASS") || test_results+=("Setup: FAIL")

    if [[ "$SKIP_BUILDS" != "true" ]]; then
        test_service_builds && test_results+=("Builds: PASS") || test_results+=("Builds: FAIL")
    else
        test_results+=("Builds: SKIPPED")
    fi

    test_database_connections && test_results+=("Connections: PASS") || test_results+=("Connections: FAIL")
    test_schema_initialization && test_results+=("Schema: PASS") || test_results+=("Schema: FAIL")
    test_database_integration && test_results+=("Integration: PASS") || test_results+=("Integration: FAIL")

    if [[ "$SKIP_PERFORMANCE" != "true" ]]; then
        test_database_performance && test_results+=("Performance: PASS") || test_results+=("Performance: FAIL")
    else
        test_results+=("Performance: SKIPPED")
    fi

    # Print test results summary
    log_section "Test Results Summary"
    for result in "${test_results[@]}"; do
        if [[ "$result" == *"PASS" ]]; then
            log_success "$result"
        elif [[ "$result" == *"SKIPPED" ]]; then
            log_info "$result"
        else
            log_error "$result"
        fi
    done

    # Print final summary
    print_summary "Enhanced Database Initialization Test"
}

# Run main function
main "$@"
