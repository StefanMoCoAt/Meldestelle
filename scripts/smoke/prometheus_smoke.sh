#!/usr/bin/env bash
set -euo pipefail

PING_SERVICE_URL=${PING_SERVICE_URL:-http://localhost:8082}
GATEWAY_URL=${GATEWAY_URL:-http://localhost:8081}

check_metrics() {
  local url="$1"
  echo "[Smoke] Checking Prometheus metrics at $url ..."
  local body
  body=$(curl -sf "$url/actuator/prometheus") || return 1
  echo "$body" | grep -E 'http_server_requests|jvm_memory_used_bytes' -q
}

if check_metrics "$PING_SERVICE_URL"; then
  echo "[Smoke][OK] ping-service exposes Prometheus metrics"
else
  echo "[Smoke][FAIL] ping-service Prometheus endpoint not available" >&2
  exit 1
fi

if check_metrics "$GATEWAY_URL"; then
  echo "[Smoke][OK] api-gateway exposes Prometheus metrics"
else
  echo "[Smoke][FAIL] api-gateway Prometheus endpoint not available" >&2
  exit 1
fi
