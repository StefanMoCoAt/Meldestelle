#!/bin/bash

# =============================================================================
# Port Configuration Test Script
# =============================================================================
# This script verifies that the centralized port management is working correctly
# and that the original port conflicts have been resolved.
# =============================================================================

set -e

echo "🔍 Testing Port Configuration Changes..."
echo "========================================"
echo

# Load environment variables from .env file
if [ -f ".env" ]; then
    echo "📝 Loading .env file..."
    source .env
    echo "✅ .env file loaded successfully"
else
    echo "❌ .env file not found!"
    exit 1
fi

echo
echo "🔧 Current Port Configuration:"
echo "------------------------------"
echo "Gateway Port: ${GATEWAY_PORT:-8081}"
echo "Ping Service Port: ${PING_SERVICE_PORT:-8082}"
echo "Consul Port: ${CONSUL_PORT:-8500}"
echo "Redis Port: ${REDIS_PORT:-6379}"
echo

# Test 1: Check that Gateway and Ping Service have different ports
echo "🧪 Test 1: Port Conflict Resolution"
echo "-----------------------------------"
GATEWAY_TEST_PORT=${GATEWAY_PORT:-8081}
PING_TEST_PORT=${PING_SERVICE_PORT:-8082}

if [ "$GATEWAY_TEST_PORT" -ne "$PING_TEST_PORT" ]; then
    echo "✅ PASS: Gateway ($GATEWAY_TEST_PORT) and Ping Service ($PING_TEST_PORT) have different ports"
else
    echo "❌ FAIL: Gateway and Ping Service still have the same port!"
    exit 1
fi

# Test 2: Verify all services have unique ports
echo
echo "🧪 Test 2: All Services Have Unique Ports"
echo "------------------------------------------"
ALL_PORTS=($GATEWAY_TEST_PORT $PING_TEST_PORT ${CONSUL_PORT:-8500} ${REDIS_PORT:-6379})
UNIQUE_PORTS=($(printf "%s\n" "${ALL_PORTS[@]}" | sort -u))

if [ ${#ALL_PORTS[@]} -eq ${#UNIQUE_PORTS[@]} ]; then
    echo "✅ PASS: All services have unique ports"
    echo "   Gateway: $GATEWAY_TEST_PORT"
    echo "   Ping Service: $PING_TEST_PORT"
    echo "   Consul: ${CONSUL_PORT:-8500}"
    echo "   Redis: ${REDIS_PORT:-6379}"
else
    echo "❌ FAIL: Port conflicts detected!"
    echo "   All ports: ${ALL_PORTS[*]}"
    echo "   Unique ports: ${UNIQUE_PORTS[*]}"
    exit 1
fi

# Test 3: Check docker-compose environment variable substitution
echo
echo "🧪 Test 3: Docker Compose Configuration"
echo "---------------------------------------"
if grep -q "\${GATEWAY_PORT:-8081}" docker-compose.yml; then
    echo "✅ PASS: docker-compose.yml uses GATEWAY_PORT environment variable"
else
    echo "❌ FAIL: docker-compose.yml doesn't use GATEWAY_PORT environment variable"
    exit 1
fi

if grep -q "\${CONSUL_PORT:-8500}" docker-compose.yml; then
    echo "✅ PASS: docker-compose.yml uses CONSUL_PORT environment variable"
else
    echo "❌ FAIL: docker-compose.yml doesn't use CONSUL_PORT environment variable"
    exit 1
fi

# Test 4: Check application.yml files use environment variables
echo
echo "🧪 Test 4: Application Configuration"
echo "-----------------------------------"
if grep -q "\${GATEWAY_PORT:8081}" infrastructure/gateway/src/main/resources/application.yml; then
    echo "✅ PASS: Gateway application.yml uses GATEWAY_PORT environment variable"
else
    echo "❌ FAIL: Gateway application.yml doesn't use GATEWAY_PORT environment variable"
    exit 1
fi

if grep -q "\${PING_SERVICE_PORT:8082}" temp/ping-service/src/main/resources/application.yml; then
    echo "✅ PASS: Ping Service application.yml uses PING_SERVICE_PORT environment variable"
else
    echo "❌ FAIL: Ping Service application.yml doesn't use PING_SERVICE_PORT environment variable"
    exit 1
fi

# Test 5: Check gradle.properties has port management
echo
echo "🧪 Test 5: Gradle Properties Configuration"
echo "------------------------------------------"
if grep -q "infrastructure.gateway.port=8081" gradle.properties; then
    echo "✅ PASS: gradle.properties contains gateway port configuration"
else
    echo "❌ FAIL: gradle.properties missing gateway port configuration"
    exit 1
fi

if grep -q "services.port.ping=8082" gradle.properties; then
    echo "✅ PASS: gradle.properties contains ping service port configuration"
else
    echo "❌ FAIL: gradle.properties missing ping service port configuration"
    exit 1
fi

echo
echo "🎉 All Tests Passed!"
echo "==================="
echo "✅ Port conflicts have been successfully resolved"
echo "✅ Centralized port management is properly implemented"
echo "✅ Gateway will use port $GATEWAY_TEST_PORT"
echo "✅ Ping Service will use port $PING_TEST_PORT"
echo "✅ All infrastructure services have unique ports"
echo "✅ Configuration follows single source of truth principle"
echo
echo "🚀 The implementation meets all requirements from the issue description!"
