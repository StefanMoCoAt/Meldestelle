#!/bin/bash

# =============================================================================
# Common Utilities Library for Meldestelle Shell Scripts
# =============================================================================
# This library provides common functions for logging, error handling, cleanup,
# and other utilities used across all shell scripts in the project.
#
# Usage: source "$(dirname "$0")/utils/common.sh" || source "scripts/utils/common.sh"
# =============================================================================

# Prevent multiple sourcing
if [[ "${COMMON_UTILS_LOADED:-}" == "true" ]]; then
    return 0
fi
COMMON_UTILS_LOADED=true

# =============================================================================
# Configuration and Constants
# =============================================================================

# Colors for output
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly PURPLE='\033[0;35m'
readonly CYAN='\033[0;36m'
readonly WHITE='\033[1;37m'
readonly NC='\033[0m' # No Color

# Symbols
readonly CHECK_MARK="✓"
readonly CROSS_MARK="✗"
readonly WARNING_MARK="⚠"
readonly INFO_MARK="ℹ"
readonly ARROW_MARK="→"

# Global counters
ERRORS=0
WARNINGS=0
CHECKS=0
START_TIME=$(date +%s)

# =============================================================================
# Error Handling and Cleanup
# =============================================================================

# Enhanced error handling
set -euo pipefail

# Error trap function
error_trap() {
    local exit_code=$?
    local line_number=$1
    log_error "Script failed at line $line_number with exit code $exit_code"
    cleanup_on_exit
    exit $exit_code
}

# Set error trap
trap 'error_trap $LINENO' ERR

# Cleanup function (can be overridden by scripts)
cleanup_on_exit() {
    if declare -f cleanup > /dev/null; then
        log_info "Running cleanup..."
        cleanup
    fi
}

# Set exit trap
trap cleanup_on_exit EXIT

# =============================================================================
# Logging Functions
# =============================================================================

# Get timestamp
get_timestamp() {
    date '+%Y-%m-%d %H:%M:%S'
}

# Base logging function
log_base() {
    local level=$1
    local color=$2
    local symbol=$3
    local message=$4
    local timestamp=$(get_timestamp)

    echo -e "${color}[${timestamp}] ${symbol} [${level}]${NC} ${message}" >&2
}

# Info logging
log_info() {
    log_base "INFO" "$BLUE" "$INFO_MARK" "$1"
}

# Success logging
log_success() {
    log_base "SUCCESS" "$GREEN" "$CHECK_MARK" "$1"
}

# Warning logging
log_warning() {
    log_base "WARNING" "$YELLOW" "$WARNING_MARK" "$1"
    ((WARNINGS++))
}

# Error logging
log_error() {
    log_base "ERROR" "$RED" "$CROSS_MARK" "$1"
    ((ERRORS++))
}

# Debug logging (only if DEBUG=true)
log_debug() {
    if [[ "${DEBUG:-false}" == "true" ]]; then
        log_base "DEBUG" "$PURPLE" "🐛" "$1"
    fi
}

# Progress logging
log_progress() {
    log_base "PROGRESS" "$CYAN" "$ARROW_MARK" "$1"
}

# Section header
log_section() {
    local title=$1
    local line=$(printf '=%.0s' {1..80})
    echo -e "\n${BLUE}${line}${NC}"
    echo -e "${BLUE}${title}${NC}"
    echo -e "${BLUE}${line}${NC}\n"
}

# =============================================================================
# Status and Validation Functions
# =============================================================================

# Print status with counter increment
print_status() {
    local status=$1
    local message=$2
    ((CHECKS++))

    case $status in
        "OK"|"SUCCESS")
            log_success "$message"
            ;;
        "WARNING"|"WARN")
            log_warning "$message"
            ;;
        "ERROR"|"FAIL")
            log_error "$message"
            ;;
        "INFO")
            log_info "$message"
            ;;
        *)
            log_info "$message"
            ;;
    esac
}

# Check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check if file exists with logging
check_file() {
    local file=$1
    local description=${2:-"File"}

    if [[ -f "$file" ]]; then
        print_status "OK" "$description exists: $file"
        return 0
    else
        print_status "ERROR" "$description not found: $file"
        return 1
    fi
}

# Check if directory exists with logging
check_directory() {
    local dir=$1
    local description=${2:-"Directory"}

    if [[ -d "$dir" ]]; then
        print_status "OK" "$description exists: $dir"
        return 0
    else
        print_status "ERROR" "$description not found: $dir"
        return 1
    fi
}

# Check if service is running on port
check_service_port() {
    local port=$1
    local service_name=${2:-"Service"}
    local timeout=${3:-30}

    log_info "Checking if $service_name is running on port $port..."

    if timeout "$timeout" bash -c "until nc -z localhost $port; do sleep 1; done" 2>/dev/null; then
        print_status "OK" "$service_name is running on port $port"
        return 0
    else
        print_status "ERROR" "$service_name is not running on port $port (timeout: ${timeout}s)"
        return 1
    fi
}

# Check HTTP endpoint with retry
check_http_endpoint() {
    local url=$1
    local service_name=${2:-"Service"}
    local timeout=${3:-30}
    local retry_count=${4:-3}

    log_info "Checking HTTP endpoint: $url"

    for ((i=1; i<=retry_count; i++)); do
        if timeout "$timeout" curl -sf "$url" >/dev/null 2>&1; then
            print_status "OK" "$service_name endpoint is healthy: $url"
            return 0
        else
            if [[ $i -lt $retry_count ]]; then
                log_warning "Attempt $i/$retry_count failed, retrying in 5 seconds..."
                sleep 5
            fi
        fi
    done

    print_status "ERROR" "$service_name endpoint is not healthy: $url (after $retry_count attempts)"
    return 1
}

# =============================================================================
# Utility Functions
# =============================================================================

# Wait for service with timeout
wait_for_service() {
    local check_command=$1
    local service_name=$2
    local timeout=${3:-60}
    local interval=${4:-5}

    log_info "Waiting for $service_name to be ready (timeout: ${timeout}s)..."

    local elapsed=0
    while [[ $elapsed -lt $timeout ]]; do
        if eval "$check_command" >/dev/null 2>&1; then
            log_success "$service_name is ready"
            return 0
        fi

        sleep "$interval"
        elapsed=$((elapsed + interval))
        log_progress "Waiting for $service_name... (${elapsed}s/${timeout}s)"
    done

    log_error "$service_name failed to become ready within ${timeout}s"
    return 1
}

# Create directory with logging
create_directory() {
    local dir=$1
    local description=${2:-"Directory"}

    if [[ ! -d "$dir" ]]; then
        if mkdir -p "$dir"; then
            log_success "$description created: $dir"
        else
            log_error "Failed to create $description: $dir"
            return 1
        fi
    else
        log_info "$description already exists: $dir"
    fi
}

# Backup file with timestamp
backup_file() {
    local file=$1
    local backup_dir=${2:-"./backups"}

    if [[ -f "$file" ]]; then
        create_directory "$backup_dir" "Backup directory"
        local timestamp=$(date +%Y%m%d_%H%M%S)
        local backup_file="$backup_dir/$(basename "$file").backup.$timestamp"

        if cp "$file" "$backup_file"; then
            log_success "File backed up: $file → $backup_file"
            echo "$backup_file"
        else
            log_error "Failed to backup file: $file"
            return 1
        fi
    else
        log_warning "File not found for backup: $file"
        return 1
    fi
}

# Run command with timeout and logging
run_with_timeout() {
    local timeout_duration=$1
    local description=$2
    shift 2
    local command=("$@")

    log_info "Running: $description"
    log_debug "Command: ${command[*]}"

    if timeout "$timeout_duration" "${command[@]}"; then
        log_success "$description completed successfully"
        return 0
    else
        local exit_code=$?
        if [[ $exit_code -eq 124 ]]; then
            log_error "$description timed out after ${timeout_duration}s"
        else
            log_error "$description failed with exit code $exit_code"
        fi
        return $exit_code
    fi
}

# =============================================================================
# Summary and Reporting Functions
# =============================================================================

# Print execution summary
print_summary() {
    local script_name=${1:-"Script"}
    local end_time=$(date +%s)
    local duration=$((end_time - START_TIME))

    log_section "Execution Summary"

    echo -e "Script: ${WHITE}$script_name${NC}"
    echo -e "Duration: ${WHITE}${duration}s${NC}"
    echo -e "Total checks: ${WHITE}$CHECKS${NC}"
    echo -e "${GREEN}Successful: $((CHECKS - ERRORS - WARNINGS))${NC}"
    echo -e "${YELLOW}Warnings: $WARNINGS${NC}"
    echo -e "${RED}Errors: $ERRORS${NC}"
    echo

    if [[ $ERRORS -eq 0 ]]; then
        if [[ $WARNINGS -eq 0 ]]; then
            log_success "All checks passed! $script_name completed successfully."
            return 0
        else
            log_warning "$script_name completed with warnings. Please review the warnings above."
            return 0
        fi
    else
        log_error "$script_name failed with $ERRORS errors. Please fix the errors above."
        return 1
    fi
}

# =============================================================================
# Environment and Configuration
# =============================================================================

# Load environment file if it exists
load_env_file() {
    local env_file=${1:-.env}

    if [[ -f "$env_file" ]]; then
        log_info "Loading environment from: $env_file"
        set -a
        # shellcheck source=/dev/null
        source "$env_file"
        set +a
        log_success "Environment loaded successfully"
    else
        log_warning "Environment file not found: $env_file"
    fi
}

# Validate required environment variables
validate_env_vars() {
    local vars=("$@")
    local missing_vars=()

    for var in "${vars[@]}"; do
        if [[ -z "${!var:-}" ]]; then
            missing_vars+=("$var")
        fi
    done

    if [[ ${#missing_vars[@]} -gt 0 ]]; then
        log_error "Missing required environment variables: ${missing_vars[*]}"
        return 1
    else
        log_success "All required environment variables are set"
        return 0
    fi
}

# =============================================================================
# Docker and Service Management
# =============================================================================

# Check if Docker is running
check_docker() {
    if command_exists docker && docker info >/dev/null 2>&1; then
        print_status "OK" "Docker is running"
        return 0
    else
        print_status "ERROR" "Docker is not running or not accessible"
        return 1
    fi
}

# Check if docker-compose is available
check_docker_compose() {
    if command_exists docker-compose; then
        print_status "OK" "docker-compose is available"
        return 0
    elif docker compose version >/dev/null 2>&1; then
        print_status "OK" "docker compose (plugin) is available"
        return 0
    else
        print_status "ERROR" "Neither docker-compose nor docker compose is available"
        return 1
    fi
}

# Start Docker services with health check wait
start_docker_services() {
    local services=("$@")
    local compose_file=${COMPOSE_FILE:-docker-compose.yml}

    log_info "Starting Docker services: ${services[*]}"

    if docker-compose -f "$compose_file" up -d "${services[@]}"; then
        log_success "Docker services started"

        # Wait for services to be healthy
        for service in "${services[@]}"; do
            wait_for_service "docker-compose -f $compose_file ps $service | grep -q 'healthy\\|Up'" "$service" 120 10
        done
    else
        log_error "Failed to start Docker services"
        return 1
    fi
}

# =============================================================================
# Initialization
# =============================================================================

log_debug "Common utilities library loaded successfully"
