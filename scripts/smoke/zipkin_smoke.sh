#!/usr/bin/env bash
set -euo pipefail

GATEWAY_URL=${GATEWAY_URL:-http://localhost:8081}
ZIPKIN_URL=${ZIPKIN_URL:-http://localhost:9411}

echo "[Smoke] Triggering ping via Gateway..."
curl -sf "$GATEWAY_URL/api/ping/ping" > /dev/null || {
  echo "[Smoke][FAIL] Gateway ping failed" >&2
  exit 1
}

# Give Zipkin a moment to receive spans
sleep 1

echo "[Smoke] Checking for recent traces in Zipkin..."
TRACES_JSON=$(curl -sf "$ZIPKIN_URL/api/v2/traces?limit=5") || {
  echo "[Smoke][FAIL] Zipkin API not reachable" >&2
  exit 1
}

# Very lightweight check: ensure at least one trace contains api-gateway or ping-service
if echo "$TRACES_JSON" | grep -E 'api-gateway|ping-service' -q; then
  echo "[Smoke][OK] Traces found for api-gateway/ping-service"
  exit 0
else
  echo "[Smoke][WARN] No traces for api-gateway/ping-service in the last results" >&2
  # Not a hard failure; Zipkin may be delayed. Exit non-zero to be strict in CI
  exit 2
fi
