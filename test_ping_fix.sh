#!/bin/bash

echo "Testing ping service fix..."
echo "=========================="

echo "1. Checking if services are running..."
echo "Consul:"
curl -s http://localhost:8500/v1/health/state/passing | jq -r '.[] | select(.ServiceName=="ping-service") | "Service: " + .ServiceName + ", Status: " + .Status'

echo ""
echo "Ping service health:"
curl -s http://localhost:8082/actuator/health | jq '.status'

echo ""
echo "2. Testing gateway ping endpoint..."
echo "GET http://localhost:8081/api/ping"
response=$(curl -s -w "\nHTTP_CODE:%{http_code}" http://localhost:8081/api/ping)
echo "$response"

echo ""
echo "3. Testing gateway actuator health..."
curl -s http://localhost:8081/actuator/health | jq '.status'
