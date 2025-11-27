# syntax=docker/dockerfile:1.7

# ===================================================================
# Dockerfile for Members Service
# Based on Spring Boot Service Template with Members-specific configuration
# ===================================================================

# === CENTRALIZED BUILD ARGUMENTS ===
# Values sourced from docker/versions.toml and docker/build-args/
# Global arguments (docker/build-args/global.env)
ARG GRADLE_VERSION
ARG JAVA_VERSION
ARG BUILD_DATE
ARG VERSION

# Service-specific arguments (docker/build-args/services.env)
# Note: Keine Runtime-Profile/Ports als Build-ARGs
ARG SERVICE_PATH=members/members-service
ARG SERVICE_NAME=members-service

# ===================================================================
# Build Stage
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS builder

# Re-declare build arguments for this stage (nur Build-Zeit)
ARG SERVICE_PATH=members/members-service
ARG SERVICE_NAME=members-service

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

# Copy members service modules in dependency order
COPY members/members-domain/ members/members-domain/
COPY members/members-api/ members/members-api/
COPY members/members-application/ members/members-application/
COPY members/members-infrastructure/ members/members-infrastructure/
COPY members/members-service/ members/members-service/

# Build members service (ohne Runtime-Profile bei Build)
RUN echo "Building Members Service..." && \
    ./gradlew :members:members-service:dependencies --no-daemon --info && \
    ./gradlew :members:members-service:bootJar --no-daemon --info

# Extract JAR layers for optimized Docker layer caching
WORKDIR /builder
RUN cp /workspace/members/members-service/build/libs/*.jar app.jar && \
    java -Djarmode=layertools -jar app.jar extract

# ===================================================================
# Runtime Stage
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

# Metadata
LABEL service="members-service" \
      version="1.0.0" \
      description="Members Management Service for Austrian Equestrian Federation" \
      maintainer="Meldestelle Development Team" \
      java.version="${JAVA_VERSION}"

# Build arguments
ARG APP_USER=membersuser
ARG APP_GROUP=membersgroup
ARG APP_UID=1004
ARG APP_GID=1004

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
ARG SERVICE_PATH=members/members-service
ARG SERVICE_NAME=members-service

# Copy Spring Boot layers in optimal order for Docker layer caching
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /builder/dependencies/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /builder/spring-boot-loader/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /builder/snapshot-dependencies/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /builder/application/ ./

USER ${APP_USER}

# Expose application port and debug port
EXPOSE 8083 5004

# Health check
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS --max-time 2 http://localhost:8083/actuator/health/readiness || exit 1

# JVM configuration optimized for members service
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

# Spring Boot configuration (Profile nur zur Laufzeit via Compose/Env)
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    SERVER_PORT=8083 \
    LOGGING_LEVEL_ROOT=INFO \
    LOGGING_LEVEL_AT_MOCODE_MEMBERS=DEBUG

# Startup command with debug support
ENTRYPOINT ["sh", "-c", "\
    echo 'Starting Members Service on port 8083...'; \
    if [ \"${DEBUG:-false}\" = \"true\" ]; then \
        echo 'Debug mode enabled on port 5004'; \
        exec java $JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5004 org.springframework.boot.loader.launch.JarLauncher; \
    else \
        exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher; \
    fi"]

# ===================================================================
# Documentation
# ===================================================================
# Build commands:
#   docker build -t meldestelle/members-service:latest -f dockerfiles/services/members-service/Dockerfile .
#   docker run -p 8084:8084 --name members-service meldestelle/members-service:latest
#
# Key features:
# - Multi-stage build with JAR layer extraction for optimal caching
# - Non-root user execution for security (UID/GID 1004)
# - Optimized JVM settings for containers
# - Comprehensive health checks with members-specific endpoint
# - Debug support on port 5004
# - Vienna timezone configuration for Austrian operations
# ===================================================================
