#!/bin/bash

# ===================================================================
# Docker Secrets Setup Script - Meldestelle Project
# ===================================================================
# This script generates secure secrets for all Docker services
# Security Features:
# - Generates cryptographically secure random passwords
# - Creates JWT secrets with proper length for HMAC512
# - Sets appropriate file permissions (600) for security
# - Provides backup functionality
# - Validates secret file creation
# ===================================================================

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SECRETS_DIR="${SCRIPT_DIR}"

# Logging function
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[WARNING] $1${NC}"
}

error() {
    echo -e "${RED}[ERROR] $1${NC}"
    exit 1
}

# Function to generate secure random password
generate_password() {
    local length=${1:-32}
    openssl rand -base64 $((length * 3 / 4)) | tr -d "=+/" | cut -c1-${length}
}

# Function to generate JWT secret (64 characters for HMAC512)
generate_jwt_secret() {
    openssl rand -hex 32
}

# Function to create secret file with proper permissions
create_secret_file() {
    local filename="$1"
    local content="$2"
    local filepath="${SECRETS_DIR}/${filename}"

    # Check if file already exists
    if [[ -f "$filepath" ]]; then
        warn "Secret file $filename already exists. Use --force to overwrite."
        return 1
    fi

    # Create the secret file
    echo -n "$content" > "$filepath"
    chmod 600 "$filepath"

    log "Created secret file: $filename"
    return 0
}

# Function to backup existing secrets
backup_secrets() {
    local backup_dir="${SECRETS_DIR}/backup_$(date +%Y%m%d_%H%M%S)"

    if find "$SECRETS_DIR" -name "*.txt" -type f | grep -q .; then
        log "Creating backup of existing secrets..."
        mkdir -p "$backup_dir"
        find "$SECRETS_DIR" -name "*.txt" -type f -exec cp {} "$backup_dir/" \;
        log "Backup created in: $backup_dir"
    fi
}

# Function to validate secret file
validate_secret_file() {
    local filepath="$1"
    local min_length="$2"

    if [[ ! -f "$filepath" ]]; then
        error "Secret file does not exist: $filepath"
    fi

    local content_length=$(wc -c < "$filepath")
    if [[ $content_length -lt $min_length ]]; then
        error "Secret file $filepath is too short (${content_length} < ${min_length})"
    fi

    local permissions=$(stat -c %a "$filepath")
    if [[ "$permissions" != "600" ]]; then
        warn "Secret file $filepath has incorrect permissions: $permissions (should be 600)"
        chmod 600 "$filepath"
    fi
}

# Function to generate all secrets
generate_all_secrets() {
    local force_overwrite=${1:-false}

    log "Starting secret generation for Meldestelle Docker infrastructure..."

    # Create backup if not forcing overwrite
    if [[ "$force_overwrite" != "true" ]]; then
        backup_secrets
    fi

    # Database secrets
    log "Generating database secrets..."
    if [[ "$force_overwrite" == "true" ]] || ! [[ -f "${SECRETS_DIR}/postgres_user.txt" ]]; then
        create_secret_file "postgres_user.txt" "meldestelle"
    fi
    if [[ "$force_overwrite" == "true" ]] || ! [[ -f "${SECRETS_DIR}/postgres_password.txt" ]]; then
        create_secret_file "postgres_password.txt" "$(generate_password 32)"
    fi

    # Redis secrets
    log "Generating Redis secrets..."
    if [[ "$force_overwrite" == "true" ]] || ! [[ -f "${SECRETS_DIR}/redis_password.txt" ]]; then
        create_secret_file "redis_password.txt" "$(generate_password 32)"
    fi

    # Keycloak secrets
    log "Generating Keycloak secrets..."
    if [[ "$force_overwrite" == "true" ]] || ! [[ -f "${SECRETS_DIR}/keycloak_admin_password.txt" ]]; then
        create_secret_file "keycloak_admin_password.txt" "$(generate_password 32)"
    fi
    if [[ "$force_overwrite" == "true" ]] || ! [[ -f "${SECRETS_DIR}/keycloak_client_secret.txt" ]]; then
        create_secret_file "keycloak_client_secret.txt" "$(generate_password 64)"
    fi
    if [[ "$force_overwrite" == "true" ]] || ! [[ -f "${SECRETS_DIR}/keycloak_auth_client_secret.txt" ]]; then
        create_secret_file "keycloak_auth_client_secret.txt" "$(generate_password 64)"
    fi

    # Grafana secrets
    log "Generating Grafana secrets..."
    if [[ "$force_overwrite" == "true" ]] || ! [[ -f "${SECRETS_DIR}/grafana_admin_user.txt" ]]; then
        create_secret_file "grafana_admin_user.txt" "admin"
    fi
    if [[ "$force_overwrite" == "true" ]] || ! [[ -f "${SECRETS_DIR}/grafana_admin_password.txt" ]]; then
        create_secret_file "grafana_admin_password.txt" "$(generate_password 32)"
    fi

    # JWT secrets
    log "Generating JWT secrets..."
    if [[ "$force_overwrite" == "true" ]] || ! [[ -f "${SECRETS_DIR}/jwt_secret.txt" ]]; then
        create_secret_file "jwt_secret.txt" "$(generate_jwt_secret)"
    fi

    # VNC secrets (for desktop app)
    log "Generating VNC secrets..."
    if [[ "$force_overwrite" == "true" ]] || ! [[ -f "${SECRETS_DIR}/vnc_password.txt" ]]; then
        create_secret_file "vnc_password.txt" "$(generate_password 16)"
    fi

    # Monitoring secrets
    log "Generating monitoring secrets..."
    if [[ "$force_overwrite" == "true" ]] || ! [[ -f "${SECRETS_DIR}/metrics_auth_username.txt" ]]; then
        create_secret_file "metrics_auth_username.txt" "metrics"
    fi
    if [[ "$force_overwrite" == "true" ]] || ! [[ -f "${SECRETS_DIR}/metrics_auth_password.txt" ]]; then
        create_secret_file "metrics_auth_password.txt" "$(generate_password 32)"
    fi

    log "Secret generation completed successfully!"
}

# Function to validate all secrets
validate_all_secrets() {
    log "Validating all secret files..."

    # Define expected secrets with minimum lengths
    declare -A secrets=(
        ["postgres_user.txt"]=8
        ["postgres_password.txt"]=16
        ["redis_password.txt"]=16
        ["keycloak_admin_password.txt"]=16
        ["keycloak_client_secret.txt"]=32
        ["keycloak_auth_client_secret.txt"]=32
        ["grafana_admin_user.txt"]=4
        ["grafana_admin_password.txt"]=16
        ["jwt_secret.txt"]=64
        ["vnc_password.txt"]=8
        ["metrics_auth_username.txt"]=4
        ["metrics_auth_password.txt"]=16
    )

    local all_valid=true
    for secret_file in "${!secrets[@]}"; do
        local filepath="${SECRETS_DIR}/${secret_file}"
        local min_length=${secrets[$secret_file]}

        if validate_secret_file "$filepath" "$min_length" 2>/dev/null; then
            log "✓ $secret_file is valid"
        else
            error "✗ $secret_file is invalid or missing"
            all_valid=false
        fi
    done

    if [[ "$all_valid" == "true" ]]; then
        log "All secret files are valid and properly secured!"
    else
        error "Some secret files are invalid. Please regenerate secrets."
    fi
}

# Function to create Docker secrets
create_docker_secrets() {
    log "Creating Docker secrets..."

    # Get the project name (directory name)
    local project_name=$(basename "$(dirname "$(dirname "$SCRIPT_DIR")")")

    # Define secrets to create
    declare -A docker_secrets=(
        ["postgres_user"]="postgres_user.txt"
        ["postgres_password"]="postgres_password.txt"
        ["redis_password"]="redis_password.txt"
        ["keycloak_admin_password"]="keycloak_admin_password.txt"
        ["keycloak_client_secret"]="keycloak_client_secret.txt"
        ["grafana_admin_user"]="grafana_admin_user.txt"
        ["grafana_admin_password"]="grafana_admin_password.txt"
        ["jwt_secret"]="jwt_secret.txt"
    )

    for secret_name in "${!docker_secrets[@]}"; do
        local secret_file="${docker_secrets[$secret_name]}"
        local filepath="${SECRETS_DIR}/${secret_file}"
        local docker_secret_name="${project_name}_${secret_name}"

        # Check if Docker secret already exists
        if docker secret ls --format "{{.Name}}" | grep -q "^${docker_secret_name}$"; then
            warn "Docker secret $docker_secret_name already exists"
        else
            # Create Docker secret
            if docker secret create "$docker_secret_name" "$filepath"; then
                log "Created Docker secret: $docker_secret_name"
            else
                error "Failed to create Docker secret: $docker_secret_name"
            fi
        fi
    done
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --help                Show this help message"
    echo "  --generate           Generate all secret files (default)"
    echo "  --force              Force overwrite existing secret files"
    echo "  --validate           Validate existing secret files"
    echo "  --docker-secrets     Create Docker secrets from files"
    echo "  --all                Generate files, validate, and create Docker secrets"
    echo ""
    echo "Examples:"
    echo "  $0                   # Generate secrets (skip existing files)"
    echo "  $0 --force           # Generate secrets (overwrite existing files)"
    echo "  $0 --validate        # Validate existing secret files"
    echo "  $0 --all             # Complete setup (generate, validate, docker secrets)"
}

# Main execution
main() {
    local action="generate"
    local force_overwrite=false

    # Check dependencies
    if ! command -v openssl &> /dev/null; then
        error "openssl is required but not installed"
    fi

    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --help)
                show_usage
                exit 0
                ;;
            --generate)
                action="generate"
                shift
                ;;
            --force)
                force_overwrite=true
                shift
                ;;
            --validate)
                action="validate"
                shift
                ;;
            --docker-secrets)
                action="docker-secrets"
                shift
                ;;
            --all)
                action="all"
                shift
                ;;
            *)
                error "Unknown option: $1"
                ;;
        esac
    done

    # Ensure secrets directory exists
    mkdir -p "$SECRETS_DIR"

    # Execute requested action
    case $action in
        "generate")
            generate_all_secrets "$force_overwrite"
            ;;
        "validate")
            validate_all_secrets
            ;;
        "docker-secrets")
            create_docker_secrets
            ;;
        "all")
            generate_all_secrets "$force_overwrite"
            validate_all_secrets
            create_docker_secrets
            ;;
        *)
            error "Invalid action: $action"
            ;;
    esac

    log "Operation completed successfully!"
}

# Run main function with all arguments
main "$@"
