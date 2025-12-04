# syntax=docker/dockerfile:1.8

# ===================================================================
# Multi-stage Dockerfile for Meldestelle API Gateway
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

# Infrastructure-specific arguments (docker/build-args/infrastructure.env)
# Note: No runtime profiles as build ARGs

# ===================================================================
# Build Stage
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS builder

# Re-declare build arguments for this stage
ARG VERSION
ARG BUILD_DATE

LABEL stage=builder
LABEL service="api-gateway"
LABEL maintainer="Meldestelle Development Team"
LABEL version="${VERSION}"
LABEL build.date="${BUILD_DATE}"

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

# Make gradlew executable (required on Linux/Unix systems)
RUN chmod +x gradlew

# Copy platform dependencies (changes less frequently)
COPY platform/ platform/
COPY core/ core/

# Copy backend/infrastructure directories (required by settings.gradle.kts)
COPY backend/infrastructure backend/infrastructure
COPY backend/services backend/services

# Copy client directories (required by settings.gradle.kts)
COPY frontend/ frontend/

# Copy docs directory (required by settings.gradle.kts)
COPY docs/ docs/

# Copy root build configuration
COPY build.gradle.kts ./

# Download and cache dependencies with BuildKit cache mount (removed deprecated flag)
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew :backend:infrastructure:gateway:dependencies --info

# Build the application with optimizations and build cache (removed deprecated flag)
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew :backend:infrastructure:gateway:bootJar --info

# Extract JAR layers for better caching in runtime stage
RUN mkdir -p build/dependency && \
    (cd build/dependency; java -Djarmode=layertools -jar /workspace/backend/infrastructure/gateway/build/libs/*.jar extract)

# ===================================================================
# Runtime Stage
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime
#eclipse-temurin:21-jre-alpine-3.22

# Build arguments for runtime stage
ARG BUILD_DATE
ARG VERSION
ARG JAVA_VERSION

# Convert build arguments to environment variables
ENV JAVA_VERSION=${JAVA_VERSION} \
    VERSION=${VERSION} \
    BUILD_DATE=${BUILD_DATE}

# Enhanced metadata
LABEL service="api-gateway" \
      version="${VERSION}" \
      description="Spring Cloud Gateway for Meldestelle microservices architecture" \
      maintainer="Meldestelle Development Team" \
      java.version="${JAVA_VERSION}" \
      build.date="${BUILD_DATE}" \
      org.opencontainers.image.title="Meldestelle API Gateway" \
      org.opencontainers.image.description="Spring Cloud Gateway with service discovery and monitoring" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.vendor="Österreichischer Pferdesportverband" \
      org.opencontainers.image.created="${BUILD_DATE}"

# Build arguments for user configuration
ARG APP_USER=gateway
ARG APP_GROUP=gateway
ARG APP_UID=1001
ARG APP_GID=1001

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

# Copy Spring Boot layers from builder stage for optimal caching
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /workspace/build/dependency/dependencies/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /workspace/build/dependency/spring-boot-loader/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /workspace/build/dependency/snapshot-dependencies/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /workspace/build/dependency/application/ ./

# Switch to non-root user
USER ${APP_USER}

# Expose application port and debug port
EXPOSE 8081 5005

# Enhanced health check with better configuration
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS --max-time 2 http://localhost:8081/actuator/health/readiness || exit 1

# Optimized JVM settings for Spring Cloud Gateway with Java 21
# Removed deprecated UseTransparentHugePages flag for better compatibility
ENV JAVA_OPTS="-XX:MaxRAMPercentage=80.0 \
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
    -Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus,gateway \
    -Dmanagement.endpoint.health.show-details=always \
    -Dmanagement.prometheus.metrics.export.enabled=true"

# Spring Boot configuration (Profile nur zur Laufzeit setzen, nicht im Build)
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    SERVER_PORT=8081 \
    LOGGING_LEVEL_ROOT=INFO \
    LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_CLOUD_GATEWAY=DEBUG

# Enhanced entrypoint with tini init system and conditional debug support
# Fixed memory cgroup path for better compatibility with different container runtimes
ENTRYPOINT ["tini", "--", "sh", "-c", "\
    echo 'Starting API Gateway with Java ${JAVA_VERSION}...'; \
    echo 'Active Spring profiles: '${SPRING_PROFILES_ACTIVE:-not-set}; \
    echo 'Gateway port: ${SERVER_PORT}'; \
    MEMORY_LIMIT=$(cat /sys/fs/cgroup/memory.max 2>/dev/null || cat /sys/fs/cgroup/memory/memory.limit_in_bytes 2>/dev/null || echo 'unlimited'); \
    echo \"Container memory limit: $MEMORY_LIMIT\"; \
    if [ \"${DEBUG:-false}\" = \"true\" ]; then \
        echo 'DEBUG mode enabled - remote debugging available on port 5005'; \
        exec java ${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 org.springframework.boot.loader.launch.JarLauncher; \
    else \
        echo 'Starting API Gateway in production mode'; \
        exec java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher; \
    fi"]
