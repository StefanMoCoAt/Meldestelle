#!/bin/bash

# Start the server in background
cd /home/stefan-mo/WsMeldestelle/meldestelle
./gradlew :server:run &
SERVER_PID=$!

# Wait for server to start
sleep 10

# Test the endpoints
echo "Testing Swagger UI endpoint:"
curl -s http://localhost:8080/swagger | head -20

echo -e "\n\nTesting OpenAPI endpoint:"
curl -s http://localhost:8080/openapi | head -20

# Kill the server
kill $SERVER_PID
