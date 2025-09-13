# syntax=docker/dockerfile:1.8

# ===================================================================
# Dockerfile for Meldestelle Auth Server
# Features: Security hardening, monitoring support, optimal caching, BuildKit cache mounts
# Version: 2.0.0 - Enhanced optimization and security
# ===================================================================

# === CENTRALIZED BUILD ARGUMENTS ===
# Values sourced from docker/versions.toml and docker/build-args/
# Global arguments (docker/build-args/global.env)
ARG GRADLE_VERSION
ARG JAVA_VERSION
ARG BUILD_DATE
ARG VERSION

# Infrastructure-specific arguments (docker/build-args/infrastructure.env)
ARG SPRING_PROFILES_ACTIVE

# ===================================================================
# Build Stage
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS builder

# Re-declare build arguments for this stage
ARG SPRING_PROFILES_ACTIVE=docker
ARG BUILD_DATE
ARG VERSION=1.0.0

LABEL stage=builder \
      service="auth-server" \
      maintainer="Meldestelle Development Team" \
      version="${VERSION}" \
      build.date="${BUILD_DATE}"

WORKDIR /workspace

# Gradle optimizations for containerized builds
ENV GRADLE_OPTS="-Dorg.gradle.caching=true \
                 -Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.configureondemand=true \
                 -Dorg.gradle.workers.max=2 \
                 -Dorg.gradle.jvmargs=-Xmx2g \
                 -XX:+UseParallelGC \
                 -XX:MaxMetaspaceSize=512m"

# Set Gradle user home for better caching
ENV GRADLE_USER_HOME=/home/gradle/.gradle

# Copy build files in optimal order for caching
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY gradle/ gradle/
COPY platform/ platform/
COPY core/ core/
COPY build.gradle.kts ./

# Copy infrastructure dependencies
COPY infrastructure/auth/auth-client/ infrastructure/auth/auth-client/

# Copy auth-server specific files
COPY infrastructure/auth/auth-server/build.gradle.kts infrastructure/auth/auth-server/
COPY infrastructure/auth/auth-server/src/ infrastructure/auth/auth-server/src/

# Download and cache dependencies with BuildKit cache mount
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew :infrastructure:auth:auth-server:dependencies --no-daemon --info

# Build application with BuildKit cache mount
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew :infrastructure:auth:auth-server:bootJar --no-daemon --info \
    -Pspring.profiles.active=${SPRING_PROFILES_ACTIVE}

# ===================================================================
# Runtime Stage
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

# Build arguments for runtime stage
ARG BUILD_DATE
ARG VERSION=1.0.0
ARG JAVA_VERSION=21
ARG SPRING_PROFILES_ACTIVE=docker

# Convert build arguments to environment variables
ENV JAVA_VERSION=${JAVA_VERSION} \
    VERSION=${VERSION} \
    BUILD_DATE=${BUILD_DATE}

# Enhanced metadata
LABEL service="auth-server" \
      version="${VERSION}" \
      description="Authentication and Authorization Server for Meldestelle" \
      maintainer="Meldestelle Development Team" \
      java.version="${JAVA_VERSION}" \
      spring.profiles.active="${SPRING_PROFILES_ACTIVE}" \
      build.date="${BUILD_DATE}" \
      org.opencontainers.image.title="Meldestelle Auth Server" \
      org.opencontainers.image.description="Spring Boot authentication service with Keycloak integration" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.created="${BUILD_DATE}"

# Build arguments for user configuration
ARG APP_USER=authuser
ARG APP_GROUP=authgroup
ARG APP_UID=1002
ARG APP_GID=1002

WORKDIR /app

# Enhanced Alpine setup with security hardening
RUN apk update && \
    apk upgrade && \
    apk add --no-cache \
        curl \
        jq \
        tzdata \
        ca-certificates \
        tini && \
    rm -rf /var/cache/apk/*

# Create non-root user for auth-server
RUN addgroup -g ${APP_GID} -S ${APP_GROUP} && \
    adduser -u ${APP_UID} -S ${APP_USER} -G ${APP_GROUP} -h /app -s /bin/sh

# Create required directories with proper permissions
RUN mkdir -p /app/logs /app/tmp /app/config && \
    chown -R ${APP_USER}:${APP_GROUP} /app

# Copy the built JAR from builder stage
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} \
     /workspace/infrastructure/auth/auth-server/build/libs/*.jar app.jar

# Switch to non-root user
USER ${APP_USER}

# Expose auth-server port and debug port
EXPOSE 8081 5005

# Enhanced health check for auth service
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS --max-time 3 http://localhost:8081/actuator/health/readiness || exit 1

# Optimized JVM settings for auth workloads
ENV JAVA_OPTS="-XX:MaxRAMPercentage=80.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+UseContainerSupport \
    -XX:G1HeapRegionSize=16m \
    -XX:+OptimizeStringConcat \
    -XX:+UseCompressedOops \
    -Djava.security.egd=file:/dev/./urandom \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Europe/Vienna \
    -Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus,configprops"

# Auth-server specific Spring Boot configuration
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
    SERVER_PORT=8081 \
    MANAGEMENT_SERVER_PORT=8081 \
    LOGGING_LEVEL_ROOT=INFO \
    LOGGING_LEVEL_AT_MOCODE=DEBUG

# Enhanced entrypoint with tini init system and conditional debug support
ENTRYPOINT ["tini", "--", "sh", "-c", "\
    echo 'Starting Meldestelle Auth Server with Java ${JAVA_VERSION}...'; \
    echo 'Active Spring profiles: ${SPRING_PROFILES_ACTIVE}'; \
    echo 'Auth server port: 8081'; \
    echo 'Container memory: '$(cat /sys/fs/cgroup/memory/memory.limit_in_bytes 2>/dev/null || echo 'unlimited'); \
    if [ \"${DEBUG:-false}\" = \"true\" ]; then \
        echo 'DEBUG mode enabled - remote debugging available on port 5005'; \
        exec java ${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar; \
    else \
        echo 'Starting auth server in production mode'; \
        exec java ${JAVA_OPTS} -jar app.jar; \
    fi"]

# ===================================================================
# Build and Usage Instructions
# ===================================================================
# Build:
#   docker build -t meldestelle/auth-server:latest -f infrastructure/auth/auth-server/Dockerfile .
#
# Run standalone:
#   docker run -p 8081:8081 --name auth-server meldestelle/auth-server:latest
#
# Run with debug:
#   docker run -p 8081:8081 -p 5005:5005 -e DEBUG=true --name auth-server meldestelle/auth-server:latest
# ===================================================================
