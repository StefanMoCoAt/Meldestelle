#!/bin/bash

# =============================================================================
# Environment Variables Validation Script
# =============================================================================
# This script validates that all required environment variables are properly
# configured for the Meldestelle application.
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
ERRORS=0
WARNINGS=0
CHECKS=0

echo -e "${BLUE}==============================================================================${NC}"
echo -e "${BLUE}Meldestelle - Environment Variables Validation${NC}"
echo -e "${BLUE}==============================================================================${NC}"
echo

# Function to print status
print_status() {
    local status=$1
    local message=$2

    case $status in
        "OK")
            echo -e "${GREEN}✓${NC} $message"
            ;;
        "WARNING")
            echo -e "${YELLOW}⚠${NC} $message"
            ((WARNINGS++))
            ;;
        "ERROR")
            echo -e "${RED}✗${NC} $message"
            ((ERRORS++))
            ;;
        "INFO")
            echo -e "${BLUE}ℹ${NC} $message"
            ;;
    esac
    ((CHECKS++))
}

# Check if .env file exists
echo -e "${BLUE}1. Checking .env file...${NC}"
if [ -f ".env" ]; then
    print_status "OK" ".env file exists"

    # Load .env file
    set -a
    source .env
    set +a

    print_status "OK" ".env file loaded successfully"
else
    print_status "ERROR" ".env file not found"
    echo -e "${RED}Please create a .env file based on the documentation.${NC}"
    exit 1
fi
echo

# Check if docker-compose.yml exists
echo -e "${BLUE}2. Checking docker-compose.yml file...${NC}"
if [ -f "docker-compose.yml" ]; then
    print_status "OK" "docker-compose.yml file exists"
else
    print_status "ERROR" "docker-compose.yml file not found"
    exit 1
fi
echo

# Define required environment variables
echo -e "${BLUE}3. Checking required environment variables...${NC}"

# Application Configuration
check_var() {
    local var_name=$1
    local var_value=${!var_name}
    local is_required=${2:-false}
    local description=$3

    if [ -n "$var_value" ]; then
        print_status "OK" "$var_name is set: '$var_value'"
    elif [ "$is_required" = true ]; then
        print_status "ERROR" "$var_name is required but not set ($description)"
    else
        print_status "WARNING" "$var_name is not set ($description)"
    fi
}

# Application Configuration
echo -e "${YELLOW}Application Configuration:${NC}"
check_var "API_HOST" true "Server host address"
check_var "API_PORT" true "Server port"
check_var "APP_NAME" false "Application name"
check_var "APP_VERSION" false "Application version"
check_var "APP_ENVIRONMENT" true "Current environment"
echo

# Database Configuration
echo -e "${YELLOW}Database Configuration:${NC}"
check_var "DB_HOST" true "Database host"
check_var "DB_PORT" true "Database port"
check_var "DB_NAME" true "Database name"
check_var "DB_USER" true "Database user"
check_var "DB_PASSWORD" true "Database password"
check_var "POSTGRES_USER" true "PostgreSQL container user"
check_var "POSTGRES_PASSWORD" true "PostgreSQL container password"
check_var "POSTGRES_DB" true "PostgreSQL container database"
echo

# Redis Configuration
echo -e "${YELLOW}Redis Configuration:${NC}"
check_var "REDIS_EVENT_STORE_HOST" true "Redis event store host"
check_var "REDIS_EVENT_STORE_PORT" true "Redis event store port"
check_var "REDIS_CACHE_HOST" true "Redis cache host"
check_var "REDIS_CACHE_PORT" true "Redis cache port"
echo

# Security Configuration
echo -e "${YELLOW}Security Configuration:${NC}"
check_var "JWT_SECRET" true "JWT secret key"
check_var "JWT_ISSUER" true "JWT issuer"
check_var "JWT_AUDIENCE" true "JWT audience"
check_var "JWT_REALM" true "JWT realm"
check_var "API_KEY" true "API key for internal services"
echo

# Keycloak Configuration
echo -e "${YELLOW}Keycloak Configuration:${NC}"
check_var "KEYCLOAK_ADMIN" true "Keycloak admin user"
check_var "KEYCLOAK_ADMIN_PASSWORD" true "Keycloak admin password"
check_var "KC_DB" true "Keycloak database type"
check_var "KC_DB_URL" true "Keycloak database URL"
check_var "KC_DB_USERNAME" true "Keycloak database user"
check_var "KC_DB_PASSWORD" true "Keycloak database password"
echo

# Service Discovery
echo -e "${YELLOW}Service Discovery Configuration:${NC}"
check_var "CONSUL_HOST" true "Consul host"
check_var "CONSUL_PORT" true "Consul port"
echo

# Messaging Configuration
echo -e "${YELLOW}Messaging Configuration:${NC}"
check_var "ZOOKEEPER_CLIENT_PORT" true "Zookeeper client port"
check_var "KAFKA_BROKER_ID" true "Kafka broker ID"
check_var "KAFKA_ZOOKEEPER_CONNECT" true "Kafka Zookeeper connection"
echo

# Monitoring Configuration
echo -e "${YELLOW}Monitoring Configuration:${NC}"
check_var "GF_SECURITY_ADMIN_USER" true "Grafana admin user"
check_var "GF_SECURITY_ADMIN_PASSWORD" true "Grafana admin password"
echo

# Security Checks
echo -e "${BLUE}4. Security validation...${NC}"

# Check JWT secret strength
if [ -n "$JWT_SECRET" ]; then
    if [ ${#JWT_SECRET} -lt 32 ]; then
        print_status "WARNING" "JWT_SECRET should be at least 32 characters long for security"
    else
        print_status "OK" "JWT_SECRET length is adequate (${#JWT_SECRET} characters)"
    fi

    if [[ "$JWT_SECRET" == *"default"* ]] || [[ "$JWT_SECRET" == *"change"* ]]; then
        print_status "WARNING" "JWT_SECRET appears to be a default value - change for production"
    else
        print_status "OK" "JWT_SECRET appears to be customized"
    fi
fi

# Check for default passwords
if [ "$POSTGRES_PASSWORD" = "meldestelle" ]; then
    print_status "WARNING" "Using default PostgreSQL password - change for production"
fi

if [ "$KEYCLOAK_ADMIN_PASSWORD" = "admin" ]; then
    print_status "WARNING" "Using default Keycloak admin password - change for production"
fi

if [ "$GF_SECURITY_ADMIN_PASSWORD" = "admin" ]; then
    print_status "WARNING" "Using default Grafana admin password - change for production"
fi
echo

# Port conflict checks
echo -e "${BLUE}5. Port conflict checks...${NC}"
declare -A ports
ports["API_PORT"]=$API_PORT
ports["DB_PORT"]=$DB_PORT
ports["REDIS_EVENT_STORE_PORT"]=$REDIS_EVENT_STORE_PORT
ports["CONSUL_PORT"]=$CONSUL_PORT
ports["ZOOKEEPER_CLIENT_PORT"]=$ZOOKEEPER_CLIENT_PORT

# Check for duplicate ports
declare -A port_usage
for service in "${!ports[@]}"; do
    port=${ports[$service]}
    if [ -n "$port" ]; then
        if [ -n "${port_usage[$port]}" ]; then
            print_status "ERROR" "Port conflict: $service ($port) conflicts with ${port_usage[$port]}"
        else
            port_usage[$port]=$service
            print_status "OK" "$service using port $port"
        fi
    fi
done
echo

# Environment-specific checks
echo -e "${BLUE}6. Environment-specific checks...${NC}"
if [ "$APP_ENVIRONMENT" = "production" ]; then
    print_status "INFO" "Production environment detected - additional security checks recommended"

    if [ "$LOGGING_LEVEL" = "DEBUG" ]; then
        print_status "WARNING" "DEBUG logging enabled in production environment"
    fi

    if [ "$SERVER_CORS_ALLOWED_ORIGINS" = "*" ]; then
        print_status "WARNING" "CORS allows all origins in production environment"
    fi
else
    print_status "OK" "Development environment detected"
fi
echo

# Summary
echo -e "${BLUE}==============================================================================${NC}"
echo -e "${BLUE}Validation Summary${NC}"
echo -e "${BLUE}==============================================================================${NC}"
echo -e "Total checks performed: ${CHECKS}"
echo -e "${GREEN}Successful checks: $((CHECKS - ERRORS - WARNINGS))${NC}"
echo -e "${YELLOW}Warnings: ${WARNINGS}${NC}"
echo -e "${RED}Errors: ${ERRORS}${NC}"
echo

if [ $ERRORS -eq 0 ]; then
    if [ $WARNINGS -eq 0 ]; then
        echo -e "${GREEN}✓ All checks passed! Your environment configuration is ready.${NC}"
        exit 0
    else
        echo -e "${YELLOW}⚠ Configuration is valid but has warnings. Review the warnings above.${NC}"
        exit 0
    fi
else
    echo -e "${RED}✗ Configuration has errors that must be fixed before running the application.${NC}"
    exit 1
fi
