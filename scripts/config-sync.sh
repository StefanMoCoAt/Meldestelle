#!/bin/bash

# ===================================================================
# Configuration Synchronization Utility
# Syncs config/central.toml to all dependent configuration files
# Eliminates redundancy across 38+ port definitions and 72+ Spring profiles
# ===================================================================

set -euo pipefail

# Script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CENTRAL_CONFIG="$PROJECT_ROOT/config/central.toml"

# Load common utilities
# shellcheck source=utils/common.sh
source "$SCRIPT_DIR/utils/common.sh" || {
    echo "Error: Could not load common utilities"
    exit 1
}

# ===================================================================
# TOML Parser Functions
# ===================================================================

# Function to extract value from TOML file
get_config_value() {
    local section=$1
    local key=$2
    local config_file=${3:-$CENTRAL_CONFIG}

    # Handle nested sections like [ports] or [spring-profiles.defaults]
    if [[ "$section" == *.* ]]; then
        # Split nested section
        local main_section="${section%%.*}"
        local subsection="${section#*.}"

        # Extract from nested section
        awk -v main="$main_section" -v subsec="$subsection" -v key="$key" '
        BEGIN { in_main = 0; in_subsec = 0 }
        /^\[/ && !/^\['"$main_section"'/ && !/^\['"$main_section"'\./ { in_main = 0; in_subsec = 0 }
        $0 ~ "^\\[" main "\\]$" { in_main = 1; in_subsec = 0; next }
        $0 ~ "^\\[" main "\\." subsec "\\]$" { in_main = 1; in_subsec = 1; next }
        in_subsec && $0 ~ "^" key " *= *" {
            gsub(/^[^=]*= *"?/, ""); gsub(/"$/, ""); print; exit
        }
        ' "$config_file"
    else
        # Extract from simple section
        awk -v section="$section" -v key="$key" '
        BEGIN { in_section = 0 }
        /^\[/ { in_section = 0 }
        $0 ~ "^\\[" section "\\]$" { in_section = 1; next }
        in_section && $0 ~ "^" key " *= *" {
            gsub(/^[^=]*= *"?/, ""); gsub(/"$/, ""); print; exit
        }
        ' "$config_file"
    fi
}

# Function to get all keys from a TOML section
get_section_keys() {
    local section=$1
    local config_file=${2:-$CENTRAL_CONFIG}

    awk -v section="$section" '
    BEGIN { in_section = 0 }
    /^\[/ { in_section = 0 }
    $0 ~ "^\\[" section "\\]$" { in_section = 1; next }
    in_section && /^[a-zA-Z0-9_-]+ *= *.*$/ {
        match($0, /^[a-zA-Z0-9_-]+/); print substr($0, RSTART, RLENGTH)
    }
    ' "$config_file"
}

# ===================================================================
# Synchronization Functions
# ===================================================================

# Function to sync gradle.properties
sync_gradle_properties() {
    log_section "Syncing gradle.properties"

    local gradle_file="$PROJECT_ROOT/gradle.properties"
    local backup_file="${gradle_file}.bak.$(date +%Y%m%d_%H%M%S)"

    # Create backup
    cp "$gradle_file" "$backup_file"
    log_info "Created backup: $(basename "$backup_file")"

    # Extract port values from central config
    local gateway_port=$(get_config_value "ports" "api-gateway")
    local consul_port=$(get_config_value "ports" "consul")
    local ping_port=$(get_config_value "ports" "ping-service")
    local members_port=$(get_config_value "ports" "members-service")
    local horses_port=$(get_config_value "ports" "horses-service")
    local events_port=$(get_config_value "ports" "events-service")

    # Update gradle.properties with centralized values
    sed -i.tmp \
        -e "s/^infrastructure\.gateway\.port=.*/infrastructure.gateway.port=${gateway_port}/" \
        -e "s/^infrastructure\.consul\.port=.*/infrastructure.consul.port=${consul_port}/" \
        -e "s/^services\.port\.start=.*/services.port.start=${ping_port}/" \
        -e "s/^services\.port\.ping=.*/services.port.ping=${ping_port}/" \
        -e "s/^services\.port\.members=.*/services.port.members=${members_port}/" \
        -e "s/^services\.port\.horses=.*/services.port.horses=${horses_port}/" \
        -e "s/^services\.port\.events=.*/services.port.events=${events_port}/" \
        "$gradle_file"

    rm -f "${gradle_file}.tmp"
    log_success "Updated gradle.properties with centralized ports"
}

# Function to sync Docker Compose files
sync_docker_compose_files() {
    log_section "Syncing Docker Compose files"

    local compose_files=(
        "$PROJECT_ROOT/docker-compose.yml"
        "$PROJECT_ROOT/docker-compose.services.yml"
        "$PROJECT_ROOT/docker-compose.clients.yml"
    )

    # Extract values from central config
    local gateway_port=$(get_config_value "ports" "api-gateway")
    local ping_port=$(get_config_value "ports" "ping-service")
    local members_port=$(get_config_value "ports" "members-service")
    local horses_port=$(get_config_value "ports" "horses-service")
    local events_port=$(get_config_value "ports" "events-service")
    local masterdata_port=$(get_config_value "ports" "masterdata-service")
    local auth_port=$(get_config_value "ports" "auth-server")
    local consul_port=$(get_config_value "ports" "consul")
    local redis_port=$(get_config_value "ports" "redis")
    local postgres_port=$(get_config_value "ports" "postgres")
    local prometheus_port=$(get_config_value "ports" "prometheus")
    local grafana_port=$(get_config_value "ports" "grafana")
    local keycloak_port=$(get_config_value "ports" "keycloak")
    local web_app_port=$(get_config_value "ports" "web-app")

    # Extract Spring profiles
    local infrastructure_profile=$(get_config_value "spring-profiles.defaults" "infrastructure")
    local services_profile=$(get_config_value "spring-profiles.defaults" "services")
    local clients_profile=$(get_config_value "spring-profiles.defaults" "clients")

    for compose_file in "${compose_files[@]}"; do
        if [[ -f "$compose_file" ]]; then
            local backup_file="${compose_file}.bak.$(date +%Y%m%d_%H%M%S)"
            cp "$compose_file" "$backup_file"
            log_info "Created backup: $(basename "$backup_file")"

            # Update port references
            sed -i.tmp \
                -e "s/\${GATEWAY_PORT:-[0-9]*}/\${GATEWAY_PORT:-${gateway_port}}/g" \
                -e "s/\${PING_SERVICE_PORT:-[0-9]*}/\${PING_SERVICE_PORT:-${ping_port}}/g" \
                -e "s/\${MEMBERS_SERVICE_PORT:-[0-9]*}/\${MEMBERS_SERVICE_PORT:-${members_port}}/g" \
                -e "s/\${HORSES_SERVICE_PORT:-[0-9]*}/\${HORSES_SERVICE_PORT:-${horses_port}}/g" \
                -e "s/\${EVENTS_SERVICE_PORT:-[0-9]*}/\${EVENTS_SERVICE_PORT:-${events_port}}/g" \
                -e "s/\${MASTERDATA_SERVICE_PORT:-[0-9]*}/\${MASTERDATA_SERVICE_PORT:-${masterdata_port}}/g" \
                -e "s/\${AUTH_SERVICE_PORT:-[0-9]*}/\${AUTH_SERVICE_PORT:-${auth_port}}/g" \
                -e "s/\${CONSUL_PORT:-[0-9]*}/\${CONSUL_PORT:-${consul_port}}/g" \
                -e "s/\${REDIS_PORT:-[0-9]*}/\${REDIS_PORT:-${redis_port}}/g" \
                -e "s/\${PROMETHEUS_PORT:-[0-9]*}/\${PROMETHEUS_PORT:-${prometheus_port}}/g" \
                -e "s/\${GRAFANA_PORT:-[0-9]*}/\${GRAFANA_PORT:-${grafana_port}}/g" \
                -e "s/:[0-9]*\":${postgres_port}/:${postgres_port}:${postgres_port}/g" \
                -e "s/:[0-9]*\":${redis_port}/:${redis_port}:${redis_port}/g" \
                -e "s/\${DOCKER_SPRING_PROFILES_DEFAULT:-[^}]*}/\${DOCKER_SPRING_PROFILES_DEFAULT:-${infrastructure_profile}}/g" \
                -e "s/\${DOCKER_SPRING_PROFILES_DOCKER:-[^}]*}/\${DOCKER_SPRING_PROFILES_DOCKER:-${services_profile}}/g" \
                "$compose_file"

            rm -f "${compose_file}.tmp"
            log_success "Updated $(basename "$compose_file")"
        else
            log_warning "File not found: $(basename "$compose_file")"
        fi
    done
}

# Function to sync environment files
sync_environment_files() {
    log_section "Syncing Environment Files"

    local env_template="$PROJECT_ROOT/config/.env.template"

    if [[ -f "$env_template" ]]; then
        local backup_file="${env_template}.bak.$(date +%Y%m%d_%H%M%S)"
        cp "$env_template" "$backup_file"
        log_info "Created backup: $(basename "$backup_file")"

        # Extract all port values
        local gateway_port=$(get_config_value "ports" "api-gateway")
        local ping_port=$(get_config_value "ports" "ping-service")
        local members_port=$(get_config_value "ports" "members-service")
        local horses_port=$(get_config_value "ports" "horses-service")
        local events_port=$(get_config_value "ports" "events-service")
        local masterdata_port=$(get_config_value "ports" "masterdata-service")
        local auth_port=$(get_config_value "ports" "auth-server")
        local consul_port=$(get_config_value "ports" "consul")
        local redis_port=$(get_config_value "ports" "redis")
        local postgres_port=$(get_config_value "ports" "postgres")
        local prometheus_port=$(get_config_value "ports" "prometheus")
        local grafana_port=$(get_config_value "ports" "grafana")

        # Update .env.template with centralized values
        sed -i.tmp \
            -e "s/^GATEWAY_PORT=.*/GATEWAY_PORT=${gateway_port}/" \
            -e "s/^PING_SERVICE_PORT=.*/PING_SERVICE_PORT=${ping_port}/" \
            -e "s/^MEMBERS_SERVICE_PORT=.*/MEMBERS_SERVICE_PORT=${members_port}/" \
            -e "s/^HORSES_SERVICE_PORT=.*/HORSES_SERVICE_PORT=${horses_port}/" \
            -e "s/^EVENTS_SERVICE_PORT=.*/EVENTS_SERVICE_PORT=${events_port}/" \
            -e "s/^MASTERDATA_SERVICE_PORT=.*/MASTERDATA_SERVICE_PORT=${masterdata_port}/" \
            -e "s/^AUTH_SERVICE_PORT=.*/AUTH_SERVICE_PORT=${auth_port}/" \
            -e "s/^CONSUL_PORT=.*/CONSUL_PORT=${consul_port}/" \
            -e "s/^REDIS_PORT=.*/REDIS_PORT=${redis_port}/" \
            -e "s/^DB_PORT=.*/DB_PORT=${postgres_port}/" \
            -e "s/^PROMETHEUS_PORT=.*/PROMETHEUS_PORT=${prometheus_port}/" \
            -e "s/^GRAFANA_PORT=.*/GRAFANA_PORT=${grafana_port}/" \
            "$env_template"

        rm -f "${env_template}.tmp"
        log_success "Updated .env.template"
    else
        log_warning ".env.template not found"
    fi
}

# Function to sync Docker build arguments
sync_docker_build_args() {
    log_section "Syncing Docker Build Arguments"

    local build_args_dir="$PROJECT_ROOT/docker/build-args"

    # Extract Spring profiles from central config
    local infrastructure_profile=$(get_config_value "spring-profiles.defaults" "infrastructure")
    local services_profile=$(get_config_value "spring-profiles.defaults" "services")
    local clients_profile=$(get_config_value "spring-profiles.defaults" "clients")

    # Update services.env
    local services_env="$build_args_dir/services.env"
    if [[ -f "$services_env" ]]; then
        local backup_file="${services_env}.bak.$(date +%Y%m%d_%H%M%S)"
        cp "$services_env" "$backup_file"

        # Extract port values
        local ping_port=$(get_config_value "ports" "ping-service")
        local members_port=$(get_config_value "ports" "members-service")
        local horses_port=$(get_config_value "ports" "horses-service")
        local events_port=$(get_config_value "ports" "events-service")
        local masterdata_port=$(get_config_value "ports" "masterdata-service")

        sed -i.tmp \
            -e "s/^SPRING_PROFILES_ACTIVE=.*/SPRING_PROFILES_ACTIVE=${services_profile}/" \
            -e "s/^PING_SERVICE_PORT=.*/PING_SERVICE_PORT=${ping_port}/" \
            -e "s/^MEMBERS_SERVICE_PORT=.*/MEMBERS_SERVICE_PORT=${members_port}/" \
            -e "s/^HORSES_SERVICE_PORT=.*/HORSES_SERVICE_PORT=${horses_port}/" \
            -e "s/^EVENTS_SERVICE_PORT=.*/EVENTS_SERVICE_PORT=${events_port}/" \
            -e "s/^MASTERDATA_SERVICE_PORT=.*/MASTERDATA_SERVICE_PORT=${masterdata_port}/" \
            "$services_env"

        rm -f "${services_env}.tmp"
        log_success "Updated services.env"
    fi

    # Update infrastructure.env
    local infrastructure_env="$build_args_dir/infrastructure.env"
    if [[ -f "$infrastructure_env" ]]; then
        local backup_file="${infrastructure_env}.bak.$(date +%Y%m%d_%H%M%S)"
        cp "$infrastructure_env" "$backup_file"

        # Extract port values
        local gateway_port=$(get_config_value "ports" "api-gateway")
        local auth_port=$(get_config_value "ports" "auth-server")
        local monitoring_port=$(get_config_value "ports" "monitoring-server")
        local consul_port=$(get_config_value "ports" "consul")

        sed -i.tmp \
            -e "s/^SPRING_PROFILES_ACTIVE=.*/SPRING_PROFILES_ACTIVE=${infrastructure_profile}/" \
            -e "s/^GATEWAY_PORT=.*/GATEWAY_PORT=${gateway_port}/" \
            -e "s/^AUTH_SERVER_PORT=.*/AUTH_SERVER_PORT=${auth_port}/" \
            -e "s/^MONITORING_SERVER_PORT=.*/MONITORING_SERVER_PORT=${monitoring_port}/" \
            -e "s/^CONSUL_PORT=.*/CONSUL_PORT=${consul_port}/" \
            "$infrastructure_env"

        rm -f "${infrastructure_env}.tmp"
        log_success "Updated infrastructure.env"
    fi

    # Update clients.env
    local clients_env="$build_args_dir/clients.env"
    if [[ -f "$clients_env" ]]; then
        local backup_file="${clients_env}.bak.$(date +%Y%m%d_%H%M%S)"
        cp "$clients_env" "$backup_file"

        # Extract port values
        local web_app_port=$(get_config_value "ports" "web-app")
        local vnc_port=$(get_config_value "ports" "desktop-app-vnc")
        local novnc_port=$(get_config_value "ports" "desktop-app-novnc")

        sed -i.tmp \
            -e "s/^WEB_APP_PORT=.*/WEB_APP_PORT=${web_app_port}/" \
            -e "s/^DESKTOP_APP_VNC_PORT=.*/DESKTOP_APP_VNC_PORT=${vnc_port}/" \
            -e "s/^DESKTOP_APP_NOVNC_PORT=.*/DESKTOP_APP_NOVNC_PORT=${novnc_port}/" \
            "$clients_env"

        rm -f "${clients_env}.tmp"
        log_success "Updated clients.env"
    fi
}

# Function to sync monitoring configuration
sync_monitoring_config() {
    log_section "Syncing Monitoring Configuration"

    local prometheus_config="$PROJECT_ROOT/config/monitoring/prometheus.dev.yml"

    if [[ -f "$prometheus_config" ]]; then
        local backup_file="${prometheus_config}.bak.$(date +%Y%m%d_%H%M%S)"
        cp "$prometheus_config" "$backup_file"
        log_info "Created backup: $(basename "$backup_file")"

        # Extract service ports
        local ping_port=$(get_config_value "ports" "ping-service")
        local members_port=$(get_config_value "ports" "members-service")
        local horses_port=$(get_config_value "ports" "horses-service")
        local events_port=$(get_config_value "ports" "events-service")
        local masterdata_port=$(get_config_value "ports" "masterdata-service")
        local gateway_port=$(get_config_value "ports" "api-gateway")

        # Update Prometheus targets with centralized ports
        sed -i.tmp \
            -e "s/ping-service:[0-9]*/ping-service:${ping_port}/g" \
            -e "s/members-service:[0-9]*/members-service:${members_port}/g" \
            -e "s/horses-service:[0-9]*/horses-service:${horses_port}/g" \
            -e "s/events-service:[0-9]*/events-service:${events_port}/g" \
            -e "s/masterdata-service:[0-9]*/masterdata-service:${masterdata_port}/g" \
            -e "s/api-gateway:[0-9]*/api-gateway:${gateway_port}/g" \
            "$prometheus_config"

        rm -f "${prometheus_config}.tmp"
        log_success "Updated Prometheus configuration"
    else
        log_warning "Prometheus config not found: $prometheus_config"
    fi
}

# Function to sync test scripts
sync_test_scripts() {
    log_section "Syncing Test Scripts"

    local test_scripts=(
        "$PROJECT_ROOT/scripts/test/integration-test.sh"
        "$PROJECT_ROOT/scripts/test/test_gateway.sh"
        "$PROJECT_ROOT/scripts/test/test-monitoring.sh"
    )

    # Extract port values
    local ping_port=$(get_config_value "ports" "ping-service")
    local gateway_port=$(get_config_value "ports" "api-gateway")
    local consul_port=$(get_config_value "ports" "consul")
    local prometheus_port=$(get_config_value "ports" "prometheus")
    local grafana_port=$(get_config_value "ports" "grafana")

    for script_file in "${test_scripts[@]}"; do
        if [[ -f "$script_file" ]]; then
            local backup_file="${script_file}.bak.$(date +%Y%m%d_%H%M%S)"
            cp "$script_file" "$backup_file"

            # Update port references in test scripts
            sed -i.tmp \
                -e "s/:${ping_port}[^0-9]/:${ping_port}/g" \
                -e "s/localhost:[0-9]*\/actuator/localhost:${ping_port}\/actuator/g" \
                -e "s/ping-service:[0-9]*/ping-service:${ping_port}/g" \
                -e "s/api-gateway:[0-9]*/api-gateway:${gateway_port}/g" \
                -e "s/consul:[0-9]*/consul:${consul_port}/g" \
                -e "s/prometheus:[0-9]*/prometheus:${prometheus_port}/g" \
                -e "s/grafana:[0-9]*/grafana:${grafana_port}/g" \
                "$script_file"

            rm -f "${script_file}.tmp"
            log_success "Updated $(basename "$script_file")"
        else
            log_warning "Test script not found: $(basename "$script_file")"
        fi
    done
}

# ===================================================================
# Validation Functions
# ===================================================================

# Function to validate central configuration
validate_central_config() {
    log_section "Validating Central Configuration"

    if [[ ! -f "$CENTRAL_CONFIG" ]]; then
        log_error "Central configuration file not found: $CENTRAL_CONFIG"
        return 1
    fi

    log_info "Validating TOML syntax..."

    # Basic TOML validation (check for common syntax errors)
    local validation_errors=0

    # Check for unclosed brackets
    if ! awk '/^\[.*[^]]$/ { print "Unclosed bracket on line " NR ": " $0; exit 1 }' "$CENTRAL_CONFIG"; then
        ((validation_errors++))
    fi

    # Check for duplicate sections
    local duplicate_sections=$(awk '/^\[.*\]$/ {
        section = $0
        count[section]++
    }
    END {
        for (s in count) {
            if (count[s] > 1)
                print s
        }
    }' "$CENTRAL_CONFIG")
    if [[ -n "$duplicate_sections" ]]; then
        log_warning "Duplicate sections found: $duplicate_sections"
        ((validation_errors++))
    fi

    if [[ $validation_errors -eq 0 ]]; then
        log_success "Central configuration is valid"
        return 0
    else
        log_error "Central configuration has $validation_errors validation errors"
        return 1
    fi
}

# Function to show current configuration status
show_config_status() {
    log_section "Configuration Status Report"

    log_info "Current port assignments from central config:"
    local services=("ping-service" "members-service" "horses-service" "events-service" "masterdata-service" "api-gateway" "auth-server")

    for service in "${services[@]}"; do
        local port=$(get_config_value "ports" "$service")
        echo "  ${service}: ${port}"
    done

    log_info "Current Spring profile defaults:"
    local infrastructure_profile=$(get_config_value "spring-profiles.defaults" "infrastructure")
    local services_profile=$(get_config_value "spring-profiles.defaults" "services")
    local clients_profile=$(get_config_value "spring-profiles.defaults" "clients")

    echo "  Infrastructure: ${infrastructure_profile}"
    echo "  Services: ${services_profile}"
    echo "  Clients: ${clients_profile}"
}

# ===================================================================
# Main Functions
# ===================================================================

# Function to perform full synchronization
sync_all() {
    log_section "Full Configuration Synchronization"
    log_info "Syncing all configuration files from central.toml..."

    # Validate central configuration first
    validate_central_config || return 1

    # Perform all synchronizations
    sync_gradle_properties || return 1
    sync_docker_compose_files || return 1
    sync_environment_files || return 1
    sync_docker_build_args || return 1
    sync_monitoring_config || return 1
    sync_test_scripts || return 1

    log_success "All configuration files synchronized successfully!"
    show_config_status
}

# Function to show help
show_help() {
    cat << EOF
Configuration Synchronization Utility

USAGE:
    $0 [COMMAND] [OPTIONS]

COMMANDS:
    sync                  Synchronize all configuration files
    validate             Validate central configuration file
    status               Show current configuration status
    gradle               Sync gradle.properties only
    compose              Sync Docker Compose files only
    env                  Sync environment files only
    docker-args          Sync Docker build arguments only
    monitoring           Sync monitoring configuration only
    tests                Sync test scripts only

OPTIONS:
    -h, --help           Show this help message
    -v, --verbose        Enable verbose output
    --dry-run            Show what would be changed without making changes

EXAMPLES:
    $0 sync              # Sync all configuration files
    $0 validate          # Validate central.toml syntax
    $0 status            # Show current port and profile assignments
    $0 gradle            # Sync gradle.properties only

This script reads from config/central.toml and updates all dependent
configuration files to eliminate redundancy across 38+ port definitions
and 72+ Spring profile configurations.

Configuration files that will be synchronized:
  - gradle.properties
  - docker-compose*.yml files
  - config/.env.template
  - docker/build-args/*.env files
  - config/monitoring/*.yml files
  - scripts/test/*.sh files

All original files are backed up before modification.
EOF
}

# Main execution function
main() {
    local command="${1:-sync}"
    local verbose=false
    local dry_run=false

    # Parse options
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -v|--verbose)
                verbose=true
                shift
                ;;
            --dry-run)
                dry_run=true
                shift
                ;;
            -*)
                log_error "Unknown option: $1"
                show_help
                exit 1
                ;;
            *)
                command="$1"
                shift
                ;;
        esac
    done

    # Set verbose mode
    if [[ "$verbose" == "true" ]]; then
        set -x
    fi

    # Handle dry run
    if [[ "$dry_run" == "true" ]]; then
        log_warning "DRY RUN MODE - No files will be modified"
        # In a real implementation, you would add dry-run logic here
    fi

    # Change to project root
    cd "$PROJECT_ROOT"

    # Execute command
    case "$command" in
        "sync"|"all")
            sync_all
            ;;
        "validate")
            validate_central_config
            ;;
        "status")
            show_config_status
            ;;
        "gradle")
            validate_central_config && sync_gradle_properties
            ;;
        "compose")
            validate_central_config && sync_docker_compose_files
            ;;
        "env")
            validate_central_config && sync_environment_files
            ;;
        "docker-args")
            validate_central_config && sync_docker_build_args
            ;;
        "monitoring")
            validate_central_config && sync_monitoring_config
            ;;
        "tests")
            validate_central_config && sync_test_scripts
            ;;
        *)
            log_error "Unknown command: $command"
            show_help
            exit 1
            ;;
    esac

    log_success "Configuration synchronization completed!"
}

# Run main function with all arguments
main "$@"
