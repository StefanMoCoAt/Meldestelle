#!/bin/bash
# ===================================================================
# Docker Consistency Validator
# Validates Dockerfiles and docker-compose files against docker/versions.toml
# ===================================================================

set -e

# Script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCKER_DIR="$PROJECT_ROOT/docker"
VERSIONS_TOML="$DOCKER_DIR/versions.toml"
DOCKERFILES_DIR="$PROJECT_ROOT/dockerfiles"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
ERRORS=0
WARNINGS=0
CHECKS_PASSED=0

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
    ((CHECKS_PASSED++))
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
    ((WARNINGS++))
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    ((ERRORS++))
}

# Function to extract version from TOML file
get_version() {
    local key=$1
    grep "^$key = " "$VERSIONS_TOML" | sed 's/.*= "\(.*\)"/\1/' || echo ""
}

# Function to get valid ARG names from TOML
get_valid_args() {
    # Extract all version keys from [versions] section
    awk '/^\[versions\]/,/^\[/ {if (/^[a-zA-Z].*= /) print $1}' "$VERSIONS_TOML" | grep -v "^\[" || true

    # Extract all build-args from [build-args] section
    awk '/^\[build-args\]/,/^\[/ {
        if (/^[a-zA-Z].*= \[/) {
            in_array = 1
            line = $0
            gsub(/.*= \[/, "", line)
        }
        if (in_array) {
            gsub(/[\[\]",]/, " ", line)
            split(line, args, " ")
            for (i in args) {
                if (args[i] != "" && args[i] != "]") {
                    print args[i]
                }
            }
            if (/\]/) in_array = 0
        }
    }' "$VERSIONS_TOML" || true

    # Extract service ports
    awk '/^\[service-ports\]/,/^\[/ {if (/^[a-zA-Z].*= /) print $1}' "$VERSIONS_TOML" | grep -v "^\[" || true
}

# Function to get environment variable mappings from TOML
get_env_mappings() {
    awk '/^\[environment-mapping\]/,/^\[/ {
        if (/^[a-zA-Z].*= /) {
            key = $1
            value = $3
            gsub(/"/, "", value)
            print key ":" value
        }
    }' "$VERSIONS_TOML" || true
}

# Function to validate Dockerfile ARGs
validate_dockerfile_args() {
    local dockerfile=$1
    local relative_path=${dockerfile#$PROJECT_ROOT/}

    print_info "Validating Dockerfile: $relative_path"

    if [[ ! -f "$dockerfile" ]]; then
        print_error "Dockerfile not found: $relative_path"
        return
    fi

    # Get all ARG declarations from Dockerfile
    local dockerfile_args=$(grep "^ARG " "$dockerfile" | sed 's/^ARG //' | sed 's/=.*//' | sort -u)

    # Get valid ARG names from TOML
    local valid_args=$(get_valid_args | sort -u)

    local has_errors=false
    local has_centralized_args=false

    # Check each ARG in Dockerfile
    while IFS= read -r arg; do
        [[ -z "$arg" ]] && continue

        # Skip empty lines
        if [[ -z "$arg" ]]; then
            continue
        fi

        # Check if ARG is defined in versions.toml or is a standard Docker ARG
        case "$arg" in
            # Standard Docker build args
            BUILDPLATFORM|TARGETPLATFORM|BUILDOS|TARGETOS|BUILDARCH|TARGETARCH)
                print_success "  ✓ Standard Docker ARG: $arg"
                has_centralized_args=true
                ;;
            # Application-specific args that should be centralized
            GRADLE_VERSION|JAVA_VERSION|NODE_VERSION|NGINX_VERSION|BUILD_DATE|VERSION|SPRING_PROFILES_ACTIVE|SERVICE_PATH|SERVICE_NAME|SERVICE_PORT|CLIENT_PATH|CLIENT_MODULE|CLIENT_NAME)
                if echo "$valid_args" | grep -q "^$arg$"; then
                    print_success "  ✓ Centralized ARG: $arg"
                    has_centralized_args=true
                else
                    print_warning "  ⚠ ARG $arg should be defined in versions.toml"
                fi
                ;;
            # Runtime configuration args (acceptable)
            APP_USER|APP_GROUP|APP_UID|APP_GID)
                print_success "  ✓ Runtime configuration ARG: $arg"
                ;;
            *)
                # Check if it's a version-related ARG that should be centralized
                if [[ "$arg" =~ _(VERSION|PORT)$ ]] || [[ "$arg" =~ ^(DOCKER_|SERVICE_|CLIENT_) ]]; then
                    print_warning "  ⚠ ARG $arg might need to be centralized in versions.toml"
                else
                    print_success "  ✓ Custom ARG: $arg"
                fi
                ;;
        esac
    done <<< "$dockerfile_args"

    # Check if Dockerfile uses centralized version management
    if [[ "$has_centralized_args" == true ]]; then
        print_success "  ✓ Dockerfile uses centralized version management"
    else
        print_warning "  ⚠ Dockerfile should use centralized ARGs from versions.toml"
    fi

    # Check for hardcoded versions
    local hardcoded_versions=$(grep -E "ARG.*=.*(alpine|[0-9]+\.[0-9]+)" "$dockerfile" | grep -v "APP_" || true)
    if [[ -n "$hardcoded_versions" ]]; then
        print_error "  ❌ Hardcoded versions found (should use versions.toml):"
        echo "$hardcoded_versions" | while read -r line; do
            print_error "    $line"
        done
    else
        print_success "  ✓ No hardcoded versions found"
    fi
}

# Function to validate docker-compose version references
validate_compose_versions() {
    local compose_file=$1
    local relative_path=${compose_file#$PROJECT_ROOT/}

    print_info "Validating Docker Compose file: $relative_path"

    if [[ ! -f "$compose_file" ]]; then
        print_error "Compose file not found: $relative_path"
        return
    fi

    # Get environment variable mappings
    local env_mappings=$(get_env_mappings)

    # Check for version references in compose file
    local version_refs=$(grep -o '\${DOCKER_[^}]*}' "$compose_file" | sort -u || true)

    if [[ -z "$version_refs" ]]; then
        print_warning "  ⚠ No centralized version references found"
        return
    fi

    # Validate each version reference
    while IFS= read -r ref; do
        [[ -z "$ref" ]] && continue

        local var_name=${ref#\$\{}
        var_name=${var_name%\}}

        # Check if mapping exists in TOML
        local mapping_found=false
        while IFS=':' read -r toml_key env_var; do
            if [[ "$env_var" == "$var_name" ]]; then
                mapping_found=true
                local toml_version=$(get_version "$toml_key")
                if [[ -n "$toml_version" ]]; then
                    print_success "  ✓ Version reference $ref maps to $toml_key = $toml_version"
                else
                    print_error "  ❌ TOML key $toml_key has no value"
                fi
                break
            fi
        done <<< "$env_mappings"

        if [[ "$mapping_found" == false ]]; then
            print_warning "  ⚠ Version reference $ref has no mapping in environment-mapping section"
        fi
    done <<< "$version_refs"

    # Check for hardcoded image versions
    local hardcoded_images=$(grep -E "image:.*:[0-9]" "$compose_file" | grep -v "\${" || true)
    if [[ -n "$hardcoded_images" ]]; then
        print_error "  ❌ Hardcoded image versions found:"
        echo "$hardcoded_images" | while read -r line; do
            print_error "    $line"
        done
    else
        print_success "  ✓ No hardcoded image versions found"
    fi
}

# Function to validate port consistency
validate_port_consistency() {
    print_info "Validating port consistency..."

    # Get ports from TOML
    local toml_ports=$(awk '/^\[service-ports\]/,/^\[/ {
        if (/^[a-zA-Z].*= [0-9]/) {
            service = $1
            port = $3
            print service ":" port
        }
    }' "$VERSIONS_TOML")

    # Check docker-compose files for port consistency
    local compose_files=("docker-compose.yml" "docker-compose.services.yml" "docker-compose.clients.yml")

    for compose_file in "${compose_files[@]}"; do
        local full_path="$PROJECT_ROOT/$compose_file"
        if [[ -f "$full_path" ]]; then
            # Extract port mappings from compose file
            local compose_ports=$(grep -E "- \".*:[0-9]+\"" "$full_path" | sed 's/.*- "\([^"]*\)".*/\1/' || true)

            # Compare with TOML ports
            while IFS=':' read -r service expected_port; do
                [[ -z "$service" ]] && continue

                # Convert service name for grep (handle different naming conventions)
                local service_pattern="$service"
                case "$service" in
                    "api-gateway") service_pattern="api-gateway" ;;
                    "ping-service") service_pattern="ping-service" ;;
                    *) ;;
                esac

                # Check if service exists in compose file and port matches
                if grep -q "$service_pattern" "$full_path"; then
                    local found_port=$(echo "$compose_ports" | grep ":$expected_port" | head -1 || true)
                    if [[ -n "$found_port" ]]; then
                        print_success "  ✓ Port consistency for $service: $expected_port"
                    else
                        print_warning "  ⚠ Port mismatch for $service (expected: $expected_port)"
                    fi
                fi
            done <<< "$toml_ports"
        fi
    done
}

# Function to validate build args environment files
validate_build_args_files() {
    print_info "Validating build-args environment files..."

    local build_args_files=("global.env" "services.env" "infrastructure.env" "clients.env")

    for env_file in "${build_args_files[@]}"; do
        local full_path="$DOCKER_DIR/build-args/$env_file"

        if [[ -f "$full_path" ]]; then
            print_success "  ✓ Build args file exists: $env_file"

            # Check if file is not empty
            if [[ -s "$full_path" ]]; then
                print_success "  ✓ Build args file is not empty: $env_file"
            else
                print_warning "  ⚠ Build args file is empty: $env_file"
            fi

            # Check for DOCKER_ environment variables
            local docker_vars=$(grep "^DOCKER_" "$full_path" | wc -l || echo "0")
            if [[ "$docker_vars" -gt 0 ]]; then
                print_success "  ✓ Found $docker_vars centralized version variables in $env_file"
            else
                print_warning "  ⚠ No DOCKER_ version variables found in $env_file"
            fi
        else
            print_error "  ❌ Build args file missing: $env_file"
        fi
    done
}

# Function to show validation summary
show_summary() {
    echo ""
    echo "==============================================="
    echo "Docker Consistency Validation Summary"
    echo "==============================================="
    echo -e "${GREEN}Checks Passed: $CHECKS_PASSED${NC}"
    echo -e "${YELLOW}Warnings: $WARNINGS${NC}"
    echo -e "${RED}Errors: $ERRORS${NC}"
    echo "==============================================="

    if [[ $ERRORS -eq 0 ]]; then
        if [[ $WARNINGS -eq 0 ]]; then
            print_success "All consistency checks passed! ✨"
            return 0
        else
            print_warning "Validation completed with warnings. Consider addressing them for optimal consistency."
            return 0
        fi
    else
        print_error "Validation failed with $ERRORS errors. Please fix them before proceeding."
        return 1
    fi
}

# Function to show help
show_help() {
    echo "Docker Consistency Validator"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  all              Run all validation checks (default)"
    echo "  dockerfiles      Validate Dockerfile ARG declarations"
    echo "  compose          Validate docker-compose version references"
    echo "  ports            Validate port consistency"
    echo "  build-args       Validate build-args environment files"
    echo ""
    echo "Examples:"
    echo "  $0               # Run all validations"
    echo "  $0 dockerfiles   # Validate only Dockerfiles"
    echo "  $0 compose       # Validate only compose files"
    echo "  $0 ports         # Validate only port consistency"
}

# Main execution
main() {
    # Check if versions.toml exists
    if [[ ! -f "$VERSIONS_TOML" ]]; then
        print_error "Versions file not found: $VERSIONS_TOML"
        exit 1
    fi

    local command=${1:-all}

    print_info "Docker Consistency Validation - Starting..."
    print_info "Versions file: $VERSIONS_TOML"
    echo ""

    case $command in
        "all")
            # Validate Dockerfiles
            find "$DOCKERFILES_DIR" -name "Dockerfile" -type f | while read -r dockerfile; do
                validate_dockerfile_args "$dockerfile"
                echo ""
            done

            # Validate docker-compose files
            for compose_file in docker-compose*.yml; do
                if [[ -f "$PROJECT_ROOT/$compose_file" ]]; then
                    validate_compose_versions "$PROJECT_ROOT/$compose_file"
                    echo ""
                fi
            done

            # Validate port consistency
            validate_port_consistency
            echo ""

            # Validate build args files
            validate_build_args_files
            ;;
        "dockerfiles")
            find "$DOCKERFILES_DIR" -name "Dockerfile" -type f | while read -r dockerfile; do
                validate_dockerfile_args "$dockerfile"
                echo ""
            done
            ;;
        "compose")
            for compose_file in docker-compose*.yml; do
                if [[ -f "$PROJECT_ROOT/$compose_file" ]]; then
                    validate_compose_versions "$PROJECT_ROOT/$compose_file"
                    echo ""
                fi
            done
            ;;
        "ports")
            validate_port_consistency
            ;;
        "build-args")
            validate_build_args_files
            ;;
        "-h"|"--help"|"help")
            show_help
            exit 0
            ;;
        *)
            print_error "Unknown command: $command"
            show_help
            exit 1
            ;;
    esac

    show_summary
}

# Run main function with all arguments
main "$@"
