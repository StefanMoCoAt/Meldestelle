#!/bin/bash

# ===================================================================
# Docker Compose Test Script
# Tests all three docker-compose files separately
# ===================================================================

set -e

echo "=== Docker Compose Test Script ==="
echo "Testing all three compose files for the Meldestelle project"
echo ""

# Function to cleanup containers
cleanup() {
    echo "Cleaning up containers..."
    docker-compose down -v --remove-orphans 2>/dev/null || true
    docker-compose -f docker-compose.services.yml down -v --remove-orphans 2>/dev/null || true
    docker-compose -f docker-compose.clients.yml down -v --remove-orphans 2>/dev/null || true
    docker system prune -f 2>/dev/null || true
}

# Function to test a compose file
test_compose_file() {
    local compose_file=$1
    local description=$2

    echo "=== Testing $description ==="
    echo "File: $compose_file"
    echo ""

    # Test compose file syntax
    echo "1. Testing syntax..."
    if docker-compose -f "$compose_file" config >/dev/null 2>&1; then
        echo "✓ Syntax OK"
    else
        echo "✗ Syntax ERROR"
        docker-compose -f "$compose_file" config
        return 1
    fi

    # Test if we can start the services (dry-run)
    echo "2. Testing service definitions..."
    if docker-compose -f "$compose_file" up --dry-run >/dev/null 2>&1; then
        echo "✓ Service definitions OK"
    else
        echo "✗ Service definitions ERROR"
        docker-compose -f "$compose_file" up --dry-run
        return 1
    fi

    echo ""
}

# Trap to ensure cleanup on exit
trap cleanup EXIT

echo "Starting Docker Compose tests..."
echo ""

# Test 1: Main infrastructure file
test_compose_file "docker-compose.yml" "Infrastructure Services (docker-compose.yml)"

# Test 2: Services file
test_compose_file "docker-compose.services.yml" "Application Services (docker-compose.services.yml)"

# Test 3: Clients file
test_compose_file "docker-compose.clients.yml" "Client Applications (docker-compose.clients.yml)"

echo "=== Test Summary ==="
echo "All tests completed. Check output above for any errors."
echo ""

# Additional check: Test combined files
echo "=== Testing Combined Files ==="
echo "Testing services with infrastructure..."
if docker-compose -f docker-compose.yml -f docker-compose.services.yml config >/dev/null 2>&1; then
    echo "✓ Infrastructure + Services combination OK"
else
    echo "✗ Infrastructure + Services combination ERROR"
    docker-compose -f docker-compose.yml -f docker-compose.services.yml config
fi

echo ""
echo "Testing full stack..."
if docker-compose -f docker-compose.yml -f docker-compose.services.yml -f docker-compose.clients.yml config >/dev/null 2>&1; then
    echo "✓ Full stack combination OK"
else
    echo "✗ Full stack combination ERROR"
    docker-compose -f docker-compose.yml -f docker-compose.services.yml -f docker-compose.clients.yml config
fi

echo ""
echo "=== Test completed ==="
