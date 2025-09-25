# syntax=docker/dockerfile:1.8

# ===================================================================
# Multi-stage Dockerfile for Meldestelle Ping Service
# Features: Security hardening, monitoring support, optimal caching, BuildKit cache mounts
# Version: 2.1.0 - Optimized and corrected version
# ===================================================================

# === CENTRALIZED BUILD ARGUMENTS ===
# Values sourced from docker/versions.toml and docker/build-args/
# Global arguments (docker/build-args/global.env)
ARG GRADLE_VERSION
ARG JAVA_VERSION
ARG BUILD_DATE
ARG VERSION

# Service-specific arguments (docker/build-args/services.env)
ARG SPRING_PROFILES_ACTIVE

# Build stage: compile the ping-service JAR inside Docker
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS builder

# Add metadata labels
LABEL stage=builder \
      service=ping-service \
      maintainer="Meldestelle Development Team" \
      version="${VERSION}" \
      build.date="${BUILD_DATE}"

WORKDIR /workspace

# Gradle optimizations for containerized builds (removed deprecated configureondemand)
ENV GRADLE_OPTS="-Dorg.gradle.caching=true \
                 -Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.workers.max=2 \
                 -Dorg.gradle.jvmargs=-Xmx2g \
                 -XX:+UseParallelGC \
                 -XX:MaxMetaspaceSize=512m"

# Set Gradle user home for better caching
ENV GRADLE_USER_HOME=/home/gradle/.gradle

# Copy gradle wrapper and configuration files first for optimal caching
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY gradle/ gradle/

# Copy platform dependencies (changes less frequently)
COPY platform/ platform/

# Copy client directories (required by settings.gradle.kts)
COPY clients/ clients/

# Copy core directories (required by settings.gradle.kts)
COPY core/ core/

# Copy infrastructure directories (required by settings.gradle.kts)
COPY infrastructure/ infrastructure/

# Copy services directories (required by settings.gradle.kts)
COPY services/ services/

# Copy docs directory (required by settings.gradle.kts)
COPY docs/ docs/

# Copy root build configuration
COPY build.gradle.kts ./

# Copy ping modules (changes most frequently)
COPY services/ping/ping-api/ services/ping/ping-api/
COPY services/ping/ping-service/ services/ping/ping-service/

# Download and cache dependencies in a separate layer with build cache
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew :services:ping:ping-service:dependencies --no-daemon --info

# Build the application with optimizations and build cache
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew :services:ping:ping-service:bootJar --no-daemon --info \
    -Pspring.profiles.active=${SPRING_PROFILES_ACTIVE}

# ===================================================================
# Runtime stage: optimized JRE image for production
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

# Build arguments for runtime stage
ARG BUILD_DATE
ARG VERSION=1.0.0
ARG JAVA_VERSION=21
ARG SPRING_PROFILES_ACTIVE=default

# Convert build arguments to environment variables
ENV JAVA_VERSION=${JAVA_VERSION} \
    VERSION=${VERSION} \
    BUILD_DATE=${BUILD_DATE}

# Add comprehensive metadata
LABEL service="ping-service" \
      version="${VERSION}" \
      description="Microservice demonstrating circuit breaker patterns and monitoring" \
      maintainer="Meldestelle Development Team" \
      java.version="${JAVA_VERSION}" \
      spring.profiles.active="${SPRING_PROFILES_ACTIVE}" \
      build.date="${BUILD_DATE}" \
      org.opencontainers.image.title="Ping Service" \
      org.opencontainers.image.description="Spring Boot microservice with circuit breaker patterns" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.created="${BUILD_DATE}"

# Build arguments for runtime configuration
ARG APP_USER=appuser
ARG APP_GROUP=appgroup
ARG APP_UID=1001
ARG APP_GID=1001

# Set working directory
WORKDIR /app

# Enhanced Alpine setup with security hardening
RUN apk update && \
    apk upgrade && \
    apk add --no-cache \
        curl \
        tzdata \
        tini && \
    rm -rf /var/cache/apk/* && \
    addgroup -g ${APP_GID} -S ${APP_GROUP} && \
    adduser -u ${APP_UID} -S ${APP_USER} -G ${APP_GROUP} -h /app -s /bin/sh && \
    mkdir -p /app/logs /app/tmp /app/config && \
    chown -R ${APP_USER}:${APP_GROUP} /app && \
    chmod -R 750 /app

# Copy the built JAR from builder stage with proper ownership
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} \
     /workspace/services/ping/ping-service/build/libs/*.jar app.jar

# Switch to non-root user
USER ${APP_USER}

# Expose application port and debug port
EXPOSE 8082 5005

# Enhanced health check with better configuration
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS --max-time 2 http://localhost:8082/actuator/health/readiness || exit 1

# Optimized JVM settings for Spring Boot microservice with Java 21
# Removed deprecated UseTransparentHugePages flag for better compatibility
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+UseContainerSupport \
    -XX:G1HeapRegionSize=16m \
    -XX:G1ReservePercent=25 \
    -XX:InitiatingHeapOccupancyPercent=30 \
    -XX:+AlwaysPreTouch \
    -XX:+DisableExplicitGC \
    -Djava.security.egd=file:/dev/./urandom \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Europe/Vienna \
    -Dspring.backgroundpreinitializer.ignore=true \
    -Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus \
    -Dmanagement.endpoint.health.show-details=always \
    -Dmanagement.prometheus.metrics.export.enabled=true"

# Spring Boot configuration
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
    SERVER_PORT=8082 \
    LOGGING_LEVEL_ROOT=INFO

# Enhanced entrypoint with tini init system and conditional debug support
# Fixed memory cgroup path for better compatibility with different container runtimes
ENTRYPOINT ["tini", "--", "sh", "-c", "\
    echo 'Starting Ping Service with Java ${JAVA_VERSION}...'; \
    echo 'Active Spring profiles: ${SPRING_PROFILES_ACTIVE}'; \
    echo 'Service port: ${SERVER_PORT}'; \
    MEMORY_LIMIT=$(cat /sys/fs/cgroup/memory.max 2>/dev/null || cat /sys/fs/cgroup/memory/memory.limit_in_bytes 2>/dev/null || echo 'unlimited'); \
    echo \"Container memory limit: $MEMORY_LIMIT\"; \
    if [ \"${DEBUG:-false}\" = \"true\" ]; then \
        echo 'DEBUG mode enabled - remote debugging available on port 5005'; \
        exec java ${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar; \
    else \
        echo 'Starting Ping Service in production mode'; \
        exec java ${JAVA_OPTS} -jar app.jar; \
    fi"]
