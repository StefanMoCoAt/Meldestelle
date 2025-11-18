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
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
    WARNINGS=$((WARNINGS + 1))
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    ERRORS=$((ERRORS + 1))
}

# Function to extract version from TOML file (restricted to [versions])
get_version() {
    local key=$1
    awk -v k="$key" '
        /^\[versions\]/ { in_section=1; next }
        /^\[/ { if (in_section) exit; in_section=0 }
        in_section && $1 == k && $2 == "=" { v=$3; gsub(/"/,"",v); print v; exit }
    ' "$VERSIONS_TOML" || echo ""
}

# Function to get valid ARG names from TOML
get_valid_args() {
    # Extract all version keys from [versions] section
    awk '/^\[versions\]/,/^\[/ {if (/^[a-zA-Z].*= /) print $1}' "$VERSIONS_TOML" | grep -v "^\[" || true

    # Extract all build-args from [build-args] section
    awk '/^\[build-args\]/,/^\[/ {
        # Extract tokens inside quotes across array lines
        while (match($0, /"[A-Za-z0-9_]+"/)) {
            token = substr($0, RSTART+1, RLENGTH-2)
            print token
            $0 = substr($0, RSTART+RLENGTH)
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

    # Get all ARG declarations from Dockerfile (allow none without exiting)
    local dockerfile_args=$({ grep "^ARG " "$dockerfile" || true; } | sed 's/^ARG //' | sed 's/=.*//' | sort -u)

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
            GRADLE_VERSION|JAVA_VERSION|NODE_VERSION|NGINX_VERSION|VERSION|SPRING_PROFILES_ACTIVE|SERVICE_PATH|SERVICE_NAME|SERVICE_PORT|CLIENT_PATH|CLIENT_MODULE|CLIENT_NAME)
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

    # Check for default assignments on centralized ARGs (forbidden)
    local centralized_args_regex='^(GRADLE_VERSION|JAVA_VERSION|NODE_VERSION|NGINX_VERSION|VERSION|SPRING_PROFILES_ACTIVE)='
    local defaulted_args=$(grep -nE "^ARG ${centralized_args_regex}" "$dockerfile" || true)
    if [[ -n "$defaulted_args" ]]; then
        print_error "  ❌ Centralized ARGs must not have default values in Dockerfiles:"
        echo "$defaulted_args" | while read -r line; do
            print_error "    $relative_path:$line"
        done
    else
        print_success "  ✓ No default values set for centralized ARGs"
    fi

    # Check for hardcoded versions in ARG default values
    local hardcoded_versions=$(grep -nE "^ARG [A-Z0-9_]+=.*(alpine|[0-9]+\.[0-9]+)" "$dockerfile" | grep -v "APP_" || true)
    if [[ -n "$hardcoded_versions" ]]; then
        print_error "  ❌ Hardcoded versions found in ARG defaults (should use versions.toml):"
        echo "$hardcoded_versions" | while read -r line; do
            print_error "    $relative_path:$line"
        done
    else
        print_success "  ✓ No hardcoded version literals in ARG defaults"
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

    # 0) Fail on blank ARG values for critical build args
    local blank_args=$(grep -nE '^[[:space:]]*(GRADLE_VERSION|JAVA_VERSION|NODE_VERSION|NGINX_VERSION|VERSION|SPRING_PROFILES_ACTIVE):[[:space:]]*$' "$compose_file" || true)
    if [[ -n "$blank_args" ]]; then
        print_error "  ❌ Blank build args detected (must reference centralized DOCKER_* variables):"
        echo "$blank_args" | while read -r line; do
            print_error "    $relative_path:$line"
        done
    else
        print_success "  ✓ No blank critical build args in compose file"
    fi

    # Enforce that critical build args map to centralized DOCKER_* variables (mapping style only)
    # IMPORTANT: Only validate mappings inside build->args sections (not environment blocks)
    local critical_vars=(GRADLE_VERSION JAVA_VERSION NODE_VERSION NGINX_VERSION VERSION SPRING_PROFILES_ACTIVE)
    for v in "${critical_vars[@]}"; do
        # Find mapping-style entries for VAR: value that are within an args: block
        local mapping_lines=$(awk -v var="$v" '
            {
                line[NR] = $0
            }
            END {
                for (i = 1; i <= NR; i++) {
                    if (line[i] ~ "^[[:space:]]*" var ":[[:space:]]*.+$") {
                        found = 0
                        # Look back up to 12 lines to see if we are under an args: section
                        for (j = i - 1; j >= 1 && j >= i - 12; j--) {
                            if (line[j] ~ /^[[:space:]]*args:[[:space:]]*$/) { found = 1; break }
                            if (line[j] ~ /^[[:space:]]*(environment|services|volumes|secrets|networks):/ ) { break }
                        }
                        if (found) { printf("%d:%s\n", i, line[i]) }
                    }
                }
            }' "$compose_file" || true)
        if [[ -n "$mapping_lines" ]]; then
            while IFS= read -r line; do
                # Extract line number and content
                local ln=$(echo "$line" | cut -d: -f1)
                local content=$(echo "$line" | cut -d: -f2-)
                # Ensure value uses ${DOCKER_*}
                if echo "$content" | grep -q '\${DOCKER_'; then
                    : # OK
                else
                    print_error "  ❌ $v should reference centralized DOCKER_* variable in build args mapping (found: $content)"
                    print_error "    $relative_path:$ln"
                fi
            done <<< "$mapping_lines"
        fi
    done

    # 2a) Validate default fallbacks in ${DOCKER_*:-fallback} match SSoT values
    # Build reverse mapping from environment-mapping (env var -> versions key)
    declare -A env_to_version_key
    while IFS=':' read -r toml_key env_var; do
        [[ -z "$toml_key" || -z "$env_var" ]] && continue
        case "$toml_key" in
            gradle-version) env_to_version_key[$env_var]="gradle";;
            java-version) env_to_version_key[$env_var]="java";;
            node-version) env_to_version_key[$env_var]="node";;
            nginx-version) env_to_version_key[$env_var]="nginx";;
            postgres-version) env_to_version_key[$env_var]="postgres";;
            redis-version) env_to_version_key[$env_var]="redis";;
            prometheus-version) env_to_version_key[$env_var]="prometheus";;
            grafana-version) env_to_version_key[$env_var]="grafana";;
            keycloak-version) env_to_version_key[$env_var]="keycloak";;
            consul-version) env_to_version_key[$env_var]="consul";;
            zookeeper-version) env_to_version_key[$env_var]="zookeeper";;
            kafka-version) env_to_version_key[$env_var]="kafka";;
            spring-profiles-default) env_to_version_key[$env_var]="spring-profiles-default";;
            spring-profiles-docker) env_to_version_key[$env_var]="spring-profiles-docker";;
            app-version) env_to_version_key[$env_var]="app-version";;
        esac
    done <<< "$env_mappings"

    # Find occurrences with explicit default fallbacks
    local fallback_lines=$(grep -nE '\${DOCKER_[A-Z0-9_]+:-[^}]+' "$compose_file" || true)
    if [[ -n "$fallback_lines" ]]; then
        while IFS= read -r ln; do
            [[ -z "$ln" ]] && continue
            local num=$(echo "$ln" | cut -d: -f1)
            local text=$(echo "$ln" | cut -d: -f2-)
            # Extract var name and fallback
            local var=$(echo "$text" | sed -n 's/.*${\([A-Z0-9_]\+\):-\([^}][^}]*\)}.*/\1/p')
            local fallback=$(echo "$text" | sed -n 's/.*${\([A-Z0-9_]\+\):-\([^}][^}]*\)}.*/\2/p')
            if [[ -z "$var" || -z "$fallback" ]]; then
                continue
            fi
            local key=${env_to_version_key[$var]}
            if [[ -z "$key" ]]; then
                # Not a centralized version/profile var, ignore
                continue
            fi
            local expected=$(get_version "$key")
            if [[ -z "$expected" ]]; then
                print_warning "  ⚠ No SSoT value for $var (key: $key) to compare fallback against"
                continue
            fi
            if [[ "$fallback" != "$expected" ]]; then
                print_error "  ❌ Outdated default fallback for $var in ${relative_path}:${num} — found '$fallback', expected '$expected' from versions.toml ($key)"
            else
                print_success "  ✓ Fallback for $var matches SSoT ($expected)"
            fi
        done <<< "$fallback_lines"
    fi

    # Check for version references in compose file
    local version_refs=$(grep -o '\${DOCKER_[^}]*}' "$compose_file" | sort -u || true)

    if [[ -z "$version_refs" ]]; then
        print_warning "  ⚠ No centralized version references found"
        # do not return; still check for hardcoded images
    else
        # Validate each version reference
        while IFS= read -r ref; do
            [[ -z "$ref" ]] && continue

            local var_name=${ref#\$\{}
            var_name=${var_name%\}}
            # Strip any default fallback (:-value) from the variable name
            var_name=${var_name%%:-*}

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
    fi

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
    # Skip when running env-less mode
    if [[ "${DOCKER_SSOT_MODE:-compat}" == "envless" ]]; then
        print_info "Env-less mode active → skipping build-args/*.env validation"
        print_success "  ✓ Skipped: build-args env files not required in env-less mode"
        return
    fi

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

            # 1) Ensure only valid lines: comments, blanks, or key=value
            local invalid_lines=$(grep -n -vE '^(#|\s*$|[A-Za-z_][A-Za-z0-9_]*=)' "$full_path" || true)
            if [[ -n "$invalid_lines" ]]; then
                print_error "  ❌ Invalid lines (must be key=value or comment):"
                echo "$invalid_lines" | while read -r line; do
                    print_error "    $env_file:$line"
                done
            else
                print_success "  ✓ Format OK (only key=value/comments) in $env_file"
            fi

            # 2) No bare placeholder like `DOCKER_XYZ` without value
            local bare_docker=$(grep -nE '^DOCKER_[A-Z0-9_]+$' "$full_path" || true)
            if [[ -n "$bare_docker" ]]; then
                print_error "  ❌ Bare DOCKER_* placeholders without values found:"
                echo "$bare_docker" | while read -r line; do
                    print_error "    $env_file:$line"
                done
            else
                print_success "  ✓ No bare DOCKER_* placeholders in $env_file"
            fi

            # 3) Policy: Only global.env may contain DOCKER_* keys
            local docker_keys_count=$(grep -E '^DOCKER_[A-Z0-9_]+' "$full_path" | wc -l || echo "0")
            if [[ "$env_file" == "global.env" ]]; then
                if [[ "$docker_keys_count" -gt 0 ]]; then
                    print_success "  ✓ DOCKER_* variables present only in global.env ($docker_keys_count found)"
                else
                    print_warning "  ⚠ Expected some DOCKER_* variables in global.env (prometheus/grafana/keycloak, etc.)"
                fi
                # Required keys in global.env
                for key in GRADLE_VERSION JAVA_VERSION VERSION; do
                    if grep -q "^$key=" "$full_path"; then
                        print_success "  ✓ $key present in global.env"
                    else
                        print_error "  ❌ Missing $key in global.env"
                    fi
                done
            else
                if [[ "$docker_keys_count" -gt 0 ]]; then
                    print_error "  ❌ DOCKER_* variables must not be present in $env_file (keep them centralized in global.env)"
                else
                    print_success "  ✓ No centralized DOCKER_* vars in $env_file (as expected)"
                fi
            fi

            # 4) Ban DOCKER_APP_VERSION in any build-args env (it is mapped from VERSION at runtime)
            if grep -q '^DOCKER_APP_VERSION=' "$full_path"; then
                print_error "  ❌ DOCKER_APP_VERSION should not be defined in build-args files (mapped from VERSION in docker-build.sh)"
            fi
        else
            print_error "  ❌ Build args file missing: $env_file"
        fi
    done
}

# Additional drift-detection helpers

# Get a port value from [service-ports] in versions.toml
get_toml_port() {
    local service_key=$1
    awk -v key="$service_key" '
        /^\[service-ports\]/ { in_section=1; next }
        /^\[/ { if (in_section) exit; in_section=0 }
        in_section && $1 == key { print $3; exit }
    ' "$VERSIONS_TOML" || echo ""
}

# Validate equality between versions.toml and build-args env files (key-to-key)
validate_env_value_equality() {
    # Skip when running env-less mode (no build-args/*.env are authoritative)
    if [[ "${DOCKER_SSOT_MODE:-compat}" == "envless" ]]; then
        print_info "Env-less mode active → skipping TOML↔env value equality check"
        print_success "  ✓ Skipped: values derive directly from versions.toml at runtime"
        return
    fi

    print_info "Validating value equality between versions.toml and build-args envs..."

    local has_diff=false

    # Internal helper for comparing a TOML key to an env key within a given file
    _check_env_pair() {
        local env_file=$1
        local env_key=$2
        local toml_key=$3
        local expected
        local actual
        local path="$DOCKER_DIR/build-args/$env_file"

        if [[ ! -f "$path" ]]; then
            print_error "  ❌ Missing env file: $env_file"
            has_diff=true
            return
        fi

        # Expected from TOML
        expected=$(get_version "$toml_key")
        # Fallback: try service-ports lookup for any matching key if not found in [versions]
        if [[ -z "$expected" ]]; then
            local port_lookup=$(get_toml_port "$toml_key")
            if [[ -n "$port_lookup" ]]; then
                expected="$port_lookup"
            fi
        fi

        # Actual from env file
        actual=$(grep -E "^${env_key}=" "$path" | head -1 | sed 's/^[^=]*=//')

        if [[ -z "$expected" ]]; then
            print_warning "  ⚠ TOML key '$toml_key' returned no value (check versions.toml)"
            return
        fi
        if [[ -z "$actual" ]]; then
            print_error "  ❌ $env_file missing $env_key (expected $expected)"
            has_diff=true
            return
        fi
        if [[ "$expected" != "$actual" ]]; then
            print_error "  ❌ Mismatch in $env_file: $env_key='$actual' != $toml_key='$expected'"
            has_diff=true
        else
            print_success "  ✓ $env_file: $env_key matches $toml_key ($expected)"
        fi
    }

    # global.env mappings (build-only) — use *_IMAGE_TAG instead of DOCKER_* vars
    _check_env_pair "global.env" "GRADLE_VERSION" "gradle"
    _check_env_pair "global.env" "JAVA_VERSION" "java"
    _check_env_pair "global.env" "VERSION" "app-version"
    _check_env_pair "global.env" "PROMETHEUS_IMAGE_TAG" "prometheus"
    _check_env_pair "global.env" "GRAFANA_IMAGE_TAG" "grafana"
    _check_env_pair "global.env" "KEYCLOAK_IMAGE_TAG" "keycloak"

    # clients.env mappings (build-only)
    _check_env_pair "clients.env" "NODE_VERSION" "node"
    _check_env_pair "clients.env" "NGINX_VERSION" "nginx"
    # No APP_VERSION or runtime/dev values here by policy

    # services.env mappings (build-only)
    # Only paths/names are expected here; no runtime profiles/ports
    # Skipping runtime checks by policy

    # infrastructure.env mappings (build-only)
    # Only paths/names are expected here; no runtime profiles/ports
    # Skipping runtime checks by policy

    if [[ "$has_diff" == false ]]; then
        print_success "Environment files are fully synchronized with versions.toml"
    fi
}

# Scan for free-floating version strings outside controlled files
scan_free_floating_versions() {
    print_info "Scanning for free-floating version literals outside SSoT-managed files..."

    # Collect version values from [versions]
    local version_values
    version_values=$(awk '
        /^\[versions\]/ { in_section=1; next }
        /^\[/ { if (in_section) exit; in_section=0 }
        in_section && $2 == "=" { v=$3; gsub(/"/,"",v); if (v ~ /[\.-]/) print v }
    ' "$VERSIONS_TOML" )

    local found_any=false
    while IFS= read -r val; do
        [[ -z "$val" ]] && continue
        # search occurrences excluding controlled locations
        local hits
        # Use find to avoid non-portable grep --exclude flags (BusyBox compatibility)
        hits=$(
            find "$PROJECT_ROOT" -type f \
                -not -path "*/.git/*" \
                -not -path "*/build/*" \
                -not -path "*/.gradle/*" \
                -not -path "*/node_modules/*" \
                -not -path "*/dist/*" \
                -not -path "*/out/*" \
                -not -path "*/target/*" \
                -not -path "$PROJECT_ROOT/README.md" \
                -not -path "$PROJECT_ROOT/docker/versions.toml" \
                -not -path "$PROJECT_ROOT/docker/build-args/global.env" \
                -not -path "$PROJECT_ROOT/docker/build-args/services.env" \
                -not -path "$PROJECT_ROOT/docker/build-args/clients.env" \
                -not -path "$PROJECT_ROOT/docker/build-args/infrastructure.env" \
                -not -name "docker-compose*.yml" \
                -not -name "docker-compose*.yml.optimized" \
                -not -path "$PROJECT_ROOT/scripts/generate-compose-files.sh" \
                -not -path "$PROJECT_ROOT/scripts/docker-versions-update.sh" \
                -print0 \
            | while IFS= read -r -d '' f; do
                grep -nF -- "$val" "$f" || true
              done
        )
        if [[ -n "$hits" ]]; then
            found_any=true
            print_warning "  ⚠ Detected occurrences of version literal '$val' outside controlled files:"
            echo "$hits" | sed 's/^/    /'
        fi
    done <<< "$version_values"

    # Generic pattern scan for suspicious literals (best-effort; warnings only)
    local generic
    # Portable generic scan using find + grep (avoid non-POSIX grep options)
    generic=$(\
        find "$PROJECT_ROOT" -type f \
            -not -path "*/.git/*" \
            -not -path "*/build/*" \
            -not -path "*/.gradle/*" \
            -not -path "*/node_modules/*" \
            -not -path "*/dist/*" \
            -not -path "*/out/*" \
            -not -path "*/target/*" \
            -not -path "$PROJECT_ROOT/docker/versions.toml" \
            -not -name "docker-compose*.yml" \
            -not -name "docker-compose*.yml.optimized" \
            -not -path "$PROJECT_ROOT/docker/build-args/global.env" \
            -not -path "$PROJECT_ROOT/docker/build-args/services.env" \
            -not -path "$PROJECT_ROOT/docker/build-args/clients.env" \
            -not -path "$PROJECT_ROOT/docker/build-args/infrastructure.env" \
            -not -path "$PROJECT_ROOT/scripts/generate-compose-files.sh" \
            -not -path "$PROJECT_ROOT/scripts/docker-versions-update.sh" \
            -not -path "$PROJECT_ROOT/README.md" \
            -print0 \
        | xargs -0 -r grep -nE -- '(^|[^0-9])([0-9]+\.[0-9]+\.[0-9]+([a-zA-Z0-9._-]+)?)' 2>/dev/null \
        | head -n 200 || true)
    if [[ -n "$generic" ]]; then
        found_any=true
        print_warning "  ⚠ Generic version-like strings found (review for potential drift):"
        echo "$generic" | sed 's/^/    /'
    fi

    if [[ "$found_any" == false ]]; then
        print_success "  ✓ No free-floating version literals detected"
    fi
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

            # Validate docker-compose files (including optimized variants)
            for compose_file in docker-compose*.yml docker-compose*.yml.optimized; do
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
            echo ""

            # Validate value equality between versions.toml and build-args envs
            validate_env_value_equality
            echo ""

            # Scan repository for free-floating version literals
            scan_free_floating_versions
            ;;
        "dockerfiles")
            find "$DOCKERFILES_DIR" -name "Dockerfile" -type f | while read -r dockerfile; do
                validate_dockerfile_args "$dockerfile"
                echo ""
            done
            ;;
        "compose")
            for compose_file in docker-compose*.yml docker-compose*.yml.optimized; do
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
