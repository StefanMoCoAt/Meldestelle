# syntax=docker/dockerfile:1.8
# ===================================================================
# Production-Ready Keycloak Dockerfile
# ===================================================================
# Based on: quay.io/keycloak/keycloak:26.0.7
# Features:
# - Pre-built optimized image (faster startup)
# - Security hardening
# - Custom theme support
# - Health monitoring
# ===================================================================

ARG KEYCLOAK_VERSION=26.4.0

# Build stage - optimize Keycloak
FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION} AS builder

ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_DB=postgres

WORKDIR /opt/keycloak

# Pre-build Keycloak for faster startup
RUN /opt/keycloak/bin/kc.sh build \
    --db=postgres \
    --health-enabled=true \
    --metrics-enabled=true \
    --cache=ispn \
    --cache-stack=tcp

# Production stage
FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION}

LABEL maintainer="Meldestelle Development Team"
LABEL description="Production-ready Keycloak for Meldestelle authentication"
LABEL version="${KEYCLOAK_VERSION}"

# Copy pre-built Keycloak
COPY --from=builder /opt/keycloak/ /opt/keycloak/

# Set user
USER 1000

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
