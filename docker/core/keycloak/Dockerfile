# syntax=docker/dockerfile:1.8
# ===================================================================
# Production-Ready Keycloak Dockerfile
# ===================================================================
# Based on: quay.io/keycloak/keycloak:26.4.2
# Features:
# - Pre-built optimized image (faster startup)
# - Security hardening
# - Custom theme support
# - Health monitoring
# ===================================================================
ARG KEYCLOAK_IMAGE_TAG

FROM quay.io/keycloak/keycloak:${KEYCLOAK_IMAGE_TAG}

LABEL maintainer="Meldestelle Development Team"
LABEL description="Production-ready Keycloak for Meldestelle authentication"
LABEL version="${KEYCLOAK_IMAGE_TAG}"

# Set environment variables for build
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_DB=postgres

WORKDIR /opt/keycloak

# Pre-build Keycloak for faster startup
RUN /opt/keycloak/bin/kc.sh build \
    --db=postgres \
    --health-enabled=true \
    --metrics-enabled=true

# Set user
USER 1000

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
