# syntax=docker/dockerfile:1.7

# ===================================================================
# Dockerfile for Horses Service
# Based on Spring Boot Service Template with Horses-specific configuration
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
ARG SERVICE_PATH=horses/horses-service
ARG SERVICE_NAME=horses-service
ARG SERVICE_PORT=8084

# ===================================================================
# Build Stage
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS builder

# Re-declare build arguments for this stage
ARG SERVICE_PATH=horses/horses-service
ARG SERVICE_NAME=horses-service
ARG SERVICE_PORT=8085
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

# Copy horses service modules in dependency order
COPY horses/horses-domain/ horses/horses-domain/
COPY horses/horses-api/ horses/horses-api/
COPY horses/horses-application/ horses/horses-application/
COPY horses/horses-infrastructure/ horses/horses-infrastructure/
COPY horses/horses-service/ horses/horses-service/

# Build horses service
RUN echo "Building Horses Service..." && \
    ./gradlew :horses:horses-service:dependencies --no-daemon --info && \
    ./gradlew :horses:horses-service:bootJar --no-daemon --info -Pspring.profiles.active=${SPRING_PROFILES_ACTIVE}

# Extract JAR layers for optimized Docker layer caching
WORKDIR /builder
RUN cp /workspace/horses/horses-service/build/libs/*.jar app.jar && \
    java -Djarmode=layertools -jar app.jar extract

# ===================================================================
# Runtime Stage
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

# Metadata
LABEL service="horses-service" \
      version="1.0.0" \
      description="Horses Management Service for Austrian Equestrian Federation" \
      maintainer="Meldestelle Development Team" \
      java.version="${JAVA_VERSION}"

# Build arguments
ARG APP_USER=horsesuser
ARG APP_GROUP=horsesgroup
ARG APP_UID=1005
ARG APP_GID=1005

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
ARG SERVICE_PATH=horses/horses-service
ARG SERVICE_NAME=horses-service
ARG SERVICE_PORT=8085

# Copy Spring Boot layers in optimal order for Docker layer caching
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /builder/dependencies/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /builder/spring-boot-loader/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /builder/snapshot-dependencies/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /builder/application/ ./

USER ${APP_USER}

# Expose application port and debug port
EXPOSE ${SERVICE_PORT} 5005

# Health check
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS --max-time 2 http://localhost:${SERVICE_PORT}/actuator/health/readiness || exit 1

# JVM configuration optimized for horses service
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
    LOGGING_LEVEL_AT_MOCODE_HORSES=DEBUG

# Startup command with debug support
ENTRYPOINT ["sh", "-c", "\
    echo 'Starting Horses Service on port ${SERVICE_PORT}...'; \
    if [ \"${DEBUG:-false}\" = \"true\" ]; then \
        echo 'Debug mode enabled on port 5005'; \
        exec java $JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 org.springframework.boot.loader.launch.JarLauncher; \
    else \
        exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher; \
    fi"]

# ===================================================================
# Documentation
# ===================================================================
# Build commands:
#   docker build -t meldestelle/horses-service:latest -f dockerfiles/services/horses-service/Dockerfile .
#   docker run -p 8085:8085 --name horses-service meldestelle/horses-service:latest
#
# Key features:
# - Multi-stage build with JAR layer extraction for optimal caching
# - Non-root user execution for security (UID/GID 1005)
# - Optimized JVM settings for containers
# - Comprehensive health checks with horses-specific endpoint
# - Debug support on port 5005
# - Vienna timezone configuration for Austrian operations
# ===================================================================
