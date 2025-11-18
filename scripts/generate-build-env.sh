#!/usr/bin/env bash
set -euo pipefail

# Minimal generator: creates docker/build-args/global.env from docker/versions.toml
# Usage: scripts/generate-build-env.sh [OUTPUT_FILE]

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
TOML="$ROOT_DIR/docker/versions.toml"
OUT="${1:-$ROOT_DIR/docker/build-args/global.env}"

if [[ ! -f "$TOML" ]]; then
  echo "Error: versions file not found: $TOML" >&2
  exit 1
fi

get_ver() {
  # reads [versions] table key
  local key="$1"
  awk -F'=' -v k="$key" '
    $0 ~ /^\[versions\]/ { inver=1; next }
    $0 ~ /^\[/ { if(inver) exit }
    inver && $1 ~ "^"k"$" { gsub(/[ "\t]/, "", $2); print $2; exit }
  ' "$TOML"
}

GRADLE_VERSION=$(get_ver gradle)
JAVA_VERSION=$(get_ver java)
APP_VERSION=$(get_ver app-version)
PROMETHEUS=$(get_ver prometheus)
GRAFANA=$(get_ver grafana)
KEYCLOAK=$(get_ver keycloak)
POSTGRES=$(get_ver postgres)
REDIS=$(get_ver redis)
CONSUL=$(get_ver consul)
KAFKA=$(get_ver kafka)
ZOOKEEPER=$(get_ver zookeeper)

mkdir -p "$(dirname "$OUT")"
cat > "$OUT" <<EOF
# ===================================================================
# Global Docker Build Arguments - Used by all categories
# Source: docker/versions.toml
# Last updated: $(date -u +"%Y-%m-%d %H:%M:%S UTC")
# ===================================================================

# --- Build Tools ---
GRADLE_VERSION=$GRADLE_VERSION
JAVA_VERSION=$JAVA_VERSION

# --- Build Metadata ---
VERSION=$APP_VERSION

# --- Monitoring & Infrastructure Services ---
PROMETHEUS_IMAGE_TAG=$PROMETHEUS
GRAFANA_IMAGE_TAG=$GRAFANA
KEYCLOAK_IMAGE_TAG=$KEYCLOAK

# --- Datastore Images ---
POSTGRES_IMAGE_TAG=$POSTGRES
REDIS_IMAGE_TAG=$REDIS

# --- Additional Infrastructure Images ---
CONSUL_IMAGE_TAG=$CONSUL
ZOOKEEPER_IMAGE_TAG=$ZOOKEEPER
KAFKA_IMAGE_TAG=$KAFKA

EOF

echo "Generated $OUT from $TOML"
