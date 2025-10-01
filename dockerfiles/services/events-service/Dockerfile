# syntax=docker/dockerfile:1.7

# ===================================================================
# Dockerfile for Events Service
# Based on Spring Boot Service Template with Events-specific configuration
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
ARG SERVICE_PATH=events/events-service
ARG SERVICE_NAME=events-service
ARG SERVICE_PORT=8085

# ===================================================================
# Build Stage
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS builder

# Re-declare build arguments for this stage
ARG SERVICE_PATH=events/events-service
ARG SERVICE_NAME=events-service
ARG SERVICE_PORT=8086
ARG SPRING_PROFILES_ACTIVE=docker

LABEL stage=builder
LABEL maintainer="Meldestelle Development Team"

WORKDIR /workspace

# Gradle optimizations
ENV GRADLE_OPTS="-Dorg.gradle.caching=true \
                 -Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.configureondemand=true \
                 -Xmx2g"

# Copy build files in optimal order for caching
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY gradle/ gradle/

# Make gradlew executable (required on Linux/Unix systems)
RUN chmod +x gradlew

COPY platform/ platform/
COPY core/ core/
COPY build.gradle.kts ./

# Copy events service modules in dependency order
COPY events/events-domain/ events/events-domain/
COPY events/events-api/ events/events-api/
COPY events/events-application/ events/events-application/
COPY events/events-infrastructure/ events/events-infrastructure/
COPY events/events-service/ events/events-service/

# Build events service
RUN echo "Building Events Service..." && \
    ./gradlew :events:events-service:dependencies --no-daemon --info && \
    ./gradlew :events:events-service:bootJar --no-daemon --info -Pspring.profiles.active=${SPRING_PROFILES_ACTIVE}

# Extract JAR layers for optimized Docker layer caching
WORKDIR /builder
RUN cp /workspace/events/events-service/build/libs/*.jar app.jar && \
    java -Djarmode=layertools -jar app.jar extract

# ===================================================================
# Runtime Stage
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

# Metadata
LABEL service="events-service" \
      version="1.0.0" \
      description="Events Management Service for Austrian Equestrian Federation" \
      maintainer="Meldestelle Development Team" \
      java.version="${JAVA_VERSION}"

# Build arguments
ARG APP_USER=eventsuser
ARG APP_GROUP=eventsgroup
ARG APP_UID=1006
ARG APP_GID=1006

WORKDIR /app

# System setup
RUN apk update && \
    apk upgrade && \
    apk add --no-cache curl jq tzdata && \
    rm -rf /var/cache/apk/*

# Non-root user creation
RUN addgroup -g ${APP_GID} -S ${APP_GROUP} && \
    adduser -u ${APP_UID} -S ${APP_USER} -G ${APP_GROUP} -h /app -s /bin/sh

# Directory setup
RUN mkdir -p /app/logs /app/tmp && \
    chown -R ${APP_USER}:${APP_GROUP} /app

# Re-declare build arguments for runtime stage
ARG SERVICE_PATH=events/events-service
ARG SERVICE_NAME=events-service
ARG SERVICE_PORT=8086

# Copy Spring Boot layers in optimal order for Docker layer caching
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /builder/dependencies/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /builder/spring-boot-loader/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /builder/snapshot-dependencies/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /builder/application/ ./

USER ${APP_USER}

# Expose application port and debug port
EXPOSE ${SERVICE_PORT} 5006

# Health check
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS --max-time 2 http://localhost:${SERVICE_PORT}/actuator/health/readiness || exit 1

# JVM configuration optimized for events service
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
    -Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus"

# Spring Boot configuration
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
    SERVER_PORT=${SERVICE_PORT} \
    LOGGING_LEVEL_ROOT=INFO \
    LOGGING_LEVEL_AT_MOCODE_EVENTS=DEBUG

# Startup command with debug support
ENTRYPOINT ["sh", "-c", "\
    echo 'Starting Events Service on port ${SERVICE_PORT}...'; \
    if [ \"${DEBUG:-false}\" = \"true\" ]; then \
        echo 'Debug mode enabled on port 5006'; \
        exec java $JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006 org.springframework.boot.loader.launch.JarLauncher; \
    else \
        exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher; \
    fi"]

# ===================================================================
# Documentation
# ===================================================================
# Build commands:
#   docker build -t meldestelle/events-service:latest -f dockerfiles/services/events-service/Dockerfile .
#   docker run -p 8086:8086 --name events-service meldestelle/events-service:latest
#
# Key features:
# - Multi-stage build with JAR layer extraction for optimal caching
# - Non-root user execution for security (UID/GID 1006)
# - Optimized JVM settings for containers
# - Comprehensive health checks with events-specific endpoint
# - Debug support on port 5006
# - Vienna timezone configuration for Austrian operations
# ===================================================================
