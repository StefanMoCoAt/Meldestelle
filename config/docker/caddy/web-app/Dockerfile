# syntax=docker/dockerfile:1.8
# ===================================================================
# Multi-Stage Dockerfile for Meldestelle Web-App (Kotlin/JS)
# Version: 3.0.0 - Caddy Edition with Runtime Config
# ===================================================================

# === GLOBAL ARGS ===
ARG GRADLE_VERSION=9.3.1
ARG JAVA_VERSION=25
ARG CADDY_VERSION=2.11-alpine
ARG VERSION=1.0.0-SNAPSHOT
ARG BUILD_DATE

# ===================================================================
# Stage 1: Build Stage
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION} AS builder

LABEL stage=builder
WORKDIR /workspace

# 1. Gradle Optimizations (Memory & Caching)
# Increased Heap to 4g for Kotlin 2.3 JS Compilation
# REMOVED: -Dorg.gradle.daemon=false (We want the daemon to handle classloading correctly!)
ENV GRADLE_OPTS="-Dorg.gradle.caching=true \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.workers.max=4 \
                 -Dorg.gradle.jvmargs=-Xmx4g \
                 -XX:+UseParallelGC"
ENV GRADLE_USER_HOME=/home/gradle/.gradle

# 2. Dependency Layering (Optimize Cache Hit Rate)
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY gradle/ gradle/
# Copy Version Catalog explicitly first!
COPY gradle/libs.versions.toml gradle/libs.versions.toml

RUN chmod +x gradlew

# 3. Copy Sources (Monorepo Structure)
COPY platform/ platform/
COPY core/ core/
COPY backend/ backend/
COPY frontend/ frontend/
COPY config/ config/
COPY contracts/ contracts/

# Create dummy docs dir
RUN mkdir -p docs

# 4. Build Web App
# REMOVED: --no-daemon flag to allow Gradle to manage its classloaders properly
# This fixes the "IsolatedKotlinClasspathClassCastException" in Gradle 9.x + KMP
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew :frontend:shells:meldestelle-portal:jsBrowserDistribution \
    -Pproduction=true \
    -PnoSourceMaps=true \
    --stacktrace

# 5. Prepare Dist
RUN mkdir -p /app/dist && \
    cp -r frontend/shells/meldestelle-portal/build/dist/js/productionExecutable/* /app/dist/

# ===================================================================
# Stage 2: Runtime Stage (Caddy)
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

# Copy Static Assets from Builder
COPY --from=builder /app/dist/ /usr/share/caddy/

# Ensure favicon exists (fallback)
COPY --from=builder /workspace/config/docker/nginx/web-app/favicon.svg /usr/share/caddy/favicon.svg

EXPOSE 4000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:4000/health || exit 1

CMD ["caddy", "run", "--config", "/etc/caddy/Caddyfile", "--adapter", "caddyfile"]
