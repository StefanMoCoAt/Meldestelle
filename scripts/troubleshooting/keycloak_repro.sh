#!/usr/bin/env bash
set -euo pipefail

# Reproduce Keycloak restart issue and capture logs
# Usage:
#   ./scripts/troubleshooting/keycloak_repro.sh
# Optional env:
#   COMPOSE_FILE (defaults to docker-compose.yml)
#   LOG_DIR (defaults to ./logs/troubleshooting)

COMPOSE_FILE=${COMPOSE_FILE:-docker-compose.yml}
LOG_DIR=${LOG_DIR:-./logs/troubleshooting}
mkdir -p "$LOG_DIR"

echo "[INFO] Using compose file: $COMPOSE_FILE"
echo "[INFO] Log directory: $LOG_DIR"

# Show effective compose config for debugging
{
  echo "# docker compose config output";
  docker compose -f "$COMPOSE_FILE" config;
} >"$LOG_DIR/compose-config.txt" 2>&1 || true

# Bring up Postgres first
echo "[INFO] Starting Postgres..."
docker compose -f "$COMPOSE_FILE" up -d postgres

# Wait for Postgres health (max ~60s)
echo "[INFO] Waiting for Postgres to become healthy..."
for i in {1..30}; do
  STATUS=$(docker inspect --format='{{json .State.Health.Status}}' meldestelle-postgres 2>/dev/null || echo '"unknown"')
  if [[ $STATUS == '"healthy"' ]]; then
    echo "[INFO] Postgres is healthy"
    break
  fi
  if [[ $i -eq 30 ]]; then
    echo "[WARN] Postgres not healthy after timeout. Proceeding anyway. Status: $STATUS"
  fi
  sleep 2
done

# Start Keycloak
echo "[INFO] Starting Keycloak..."
docker compose -f "$COMPOSE_FILE" up -d keycloak || true

# Capture initial logs snapshot (non-follow) for both services
echo "[INFO] Capturing logs snapshot..."
docker compose -f "$COMPOSE_FILE" logs --no-log-prefix postgres >"$LOG_DIR/postgres.log" 2>&1 || true
# Capture more lines for keycloak as issues are often verbose
docker compose -f "$COMPOSE_FILE" logs --no-log-prefix --tail=500 keycloak >"$LOG_DIR/keycloak.log" 2>&1 || true

# Show helpful status
echo "[INFO] docker compose ps"
docker compose -f "$COMPOSE_FILE" ps | tee "$LOG_DIR/compose-ps.txt"

echo "[INFO] Done. Logs written to: $LOG_DIR"
echo "[INFO] To follow live logs, run:"
echo "       docker compose -f $COMPOSE_FILE logs -f keycloak"
echo "       docker compose -f $COMPOSE_FILE logs -f postgres"
