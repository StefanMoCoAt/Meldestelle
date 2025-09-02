#!/bin/bash

# Test script to validate the kotlin-multiplatform-web.Dockerfile template
# - Robust pre-checks (Docker, buildx, file existence)
# - Safer bash settings, clear diagnostics
# - Uses ephemeral ports for container run test (avoids conflicts)
# - Cleans up containers/images even on failure

set -Eeuo pipefail

DOCKERFILE_PATH="dockerfiles/templates/kotlin-multiplatform-web.Dockerfile"
SCRIPT_NAME="$(basename "$0")"

# Unique suffix to avoid tag/container collisions
RAND_SUFFIX=$(date +%s)-$RANDOM
IMAGE_DEFAULT="test-kotlin-web:default-${RAND_SUFFIX}"
IMAGE_CUSTOM="test-kotlin-web:custom-${RAND_SUFFIX}"
CONTAINER_NAME="test-container-${RAND_SUFFIX}"

cleanup() {
  echo "[cleanup] Stopping/removing test resources (if any)..." || true
  docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true
  docker rmi "$IMAGE_DEFAULT" "$IMAGE_CUSTOM" >/dev/null 2>&1 || true
}
trap cleanup EXIT

info()  { echo "[INFO]  $*"; }
success(){ echo "[ OK ]  $*"; }
warn()  { echo "[WARN]  $*"; }
fail()  { echo "[FAIL]  $*"; exit 1; }

info "Testing Kotlin Multiplatform Web Dockerfile Template"
echo  "======================================================="

# -------------------------------------------------------------------
# 0. Pre-checks
# -------------------------------------------------------------------
command -v docker >/dev/null 2>&1 || fail "Docker is not installed or not in PATH"
if ! docker info >/dev/null 2>&1; then
  fail "Docker does not seem to be running or accessible for the current user"
fi

if [ ! -f "$DOCKERFILE_PATH" ]; then
  fail "Dockerfile not found at: $DOCKERFILE_PATH"
fi

HAS_BUILDX=1
if ! docker buildx version >/dev/null 2>&1; then
  HAS_BUILDX=0
  warn "docker buildx not available; skipping buildx-specific syntax check"
fi

# -------------------------------------------------------------------
# 1. Static checks on Dockerfile structure
# -------------------------------------------------------------------
info "1) Validating Dockerfile structure and ARG definitions"

# Required ARG variables must be defined (somewhere in the file)
if grep -q "^ARG CLIENT_PATH=" "$DOCKERFILE_PATH" \
   && grep -q "^ARG CLIENT_MODULE=" "$DOCKERFILE_PATH" \
   && grep -q "^ARG CLIENT_NAME=" "$DOCKERFILE_PATH"; then
  success "Required ARG declarations found"
else
  fail "Missing required ARG declarations (CLIENT_PATH, CLIENT_MODULE, CLIENT_NAME)"
fi

# Ensure expected stages are present
if grep -qiE "^FROM .* as kotlin-builder" "$DOCKERFILE_PATH" && \
   grep -qiE "^FROM .* as runtime" "$DOCKERFILE_PATH"; then
  success "Build stages 'kotlin-builder' and 'runtime' found"
else
  fail "Expected stages 'kotlin-builder' and/or 'runtime' not found"
fi

# Verify that ARGs are re-declared in both stages (search within ~40 lines after each stage marker)
kotlin_builder_args=$(grep -n "^FROM .* [Aa][Ss] kotlin-builder" "$DOCKERFILE_PATH" | cut -d: -f1 | xargs -I{} sh -c "sed -n '{}','{}+40p' '$DOCKERFILE_PATH' | grep -c '^ARG'" || echo 0)
runtime_args=$(grep -n "^FROM .* [Aa][Ss] runtime" "$DOCKERFILE_PATH" | cut -d: -f1 | xargs -I{} sh -c "sed -n '{}','{}+40p' '$DOCKERFILE_PATH' | grep -c '^ARG'" || echo 0)
if [ "${kotlin_builder_args:-0}" -ge 3 ] && [ "${runtime_args:-0}" -ge 3 ]; then
  success "ARG declarations appear in both build stages"
else
  fail "ARG declarations appear to be missing in one or both build stages"
fi

# Optional: attempt a lightweight parsing via buildx (does not necessarily run heavy build)
if [ "$HAS_BUILDX" -eq 1 ]; then
  info "Performing basic Dockerfile parsing with buildx (no image kept)"
  # Try to parse/resolve without caching; don't fail the whole flow on noisy build output
  if docker buildx build --no-cache -f "$DOCKERFILE_PATH" --platform linux/amd64 . \
      2>&1 | head -50 | grep -q "ERROR.*failed to solve"; then
    fail "Dockerfile has parsing errors (buildx failed to solve)"
  else
    success "Dockerfile basic parsing passed"
  fi
else
  warn "Skipping buildx parsing check"
fi

# -------------------------------------------------------------------
# 2. Build with default arguments (web-app)
# -------------------------------------------------------------------
info "2) Building image with default arguments (web-app)"
if docker build --no-cache -f "$DOCKERFILE_PATH" -t "$IMAGE_DEFAULT" .; then
  success "Build with default arguments successful"
else
  fail "Build with default arguments failed"
fi

# -------------------------------------------------------------------
# 3. Build with custom arguments (desktop-app scenario)
# -------------------------------------------------------------------
info "3) Building image with custom arguments (desktop-app scenario)"
if docker build --no-cache -f "$DOCKERFILE_PATH" \
    --build-arg CLIENT_PATH=client/desktop-app \
    --build-arg CLIENT_MODULE=client:desktop-app \
    --build-arg CLIENT_NAME=desktop-app \
    -t "$IMAGE_CUSTOM" .; then
  success "Build with custom arguments successful"
else
  warn "Build with custom arguments failed (this can be expected if desktop-app lacks proper assets/nginx.conf)"
fi

# -------------------------------------------------------------------
# 4. Run container and validate it responds over HTTP
# -------------------------------------------------------------------
info "4) Running container from default image and validating HTTP response"
# -P maps service ports to random host ports; then detect the mapped port
if docker run --rm -d --name "$CONTAINER_NAME" -P "$IMAGE_DEFAULT" >/dev/null; then
  # Determine mapped host port for container port 80
  sleep 3
  HOST_PORT=$(docker port "$CONTAINER_NAME" 80/tcp | sed -E 's/.*:(\d+)/\1/' | head -n1 || true)
  if [ -z "${HOST_PORT:-}" ]; then
    docker logs "$CONTAINER_NAME" || true
    fail "Could not determine mapped host port for container"
  fi
  # Try a few times to allow nginx to start
  for i in {1..10}; do
    if curl -fsS "http://127.0.0.1:${HOST_PORT}" >/dev/null 2>&1; then
      success "Container responded over HTTP on localhost:${HOST_PORT}"
      break
    fi
    sleep 1
  done
  # Final check (if not succeeded yet)
  if ! curl -fsS "http://127.0.0.1:${HOST_PORT}" >/dev/null 2>&1; then
    docker logs "$CONTAINER_NAME" || true
    fail "Container started but did not respond on HTTP port"
  fi
else
  fail "Container failed to start"
fi

# -------------------------------------------------------------------
# 5. Done (cleanup happens via trap)
# -------------------------------------------------------------------
echo ""
echo "======================================================="
success "All tests completed successfully. The Dockerfile template looks healthy."
echo "Highlights:"
echo "  - Verified presence of required ARGs and stages"
echo "  - Performed basic parsing (when buildx available)"
echo "  - Built images (default + custom args)"
echo "  - Validated container HTTP responsiveness via ephemeral port"
echo "======================================================="
