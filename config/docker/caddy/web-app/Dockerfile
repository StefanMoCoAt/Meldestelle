# syntax=docker/dockerfile:1.8
# ===================================================================
# Dockerfile for Meldestelle Web-App (Pre-built Artifacts)
# Version: 3.1.0 - Local Build Injection
# ===================================================================

# === GLOBAL ARGS ===
ARG CADDY_VERSION=2.11-alpine
ARG VERSION=1.0.0-SNAPSHOT
ARG BUILD_DATE

# ===================================================================
# Stage 1: Runtime Stage (Caddy)
# ===================================================================
FROM caddy:${CADDY_VERSION}

ARG VERSION
ARG BUILD_DATE

LABEL service="web-app" \
      version="${VERSION}" \
      maintainer="Meldestelle Development Team" \
      build.date="${BUILD_DATE}"

# Copy Caddy Config & Templates
COPY config/docker/caddy/web-app/Caddyfile /etc/caddy/Caddyfile
COPY config/docker/caddy/web-app/config.json /usr/share/caddy/config.json

# Copy Pre-built Static Assets from Host
# NOTE: You must run `./gradlew :frontend:shells:meldestelle-portal:jsBrowserDistribution -Pproduction=true` locally first!
COPY frontend/shells/meldestelle-portal/build/dist/js/productionExecutable/ /usr/share/caddy/

# Ensure favicon exists (fallback)
COPY config/docker/nginx/web-app/favicon.svg /usr/share/caddy/favicon.svg

EXPOSE 4000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:4000/health || exit 1

CMD ["caddy", "run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
