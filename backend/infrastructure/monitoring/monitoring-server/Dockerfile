# syntax=docker/dockerfile:1.7

# ===================================================================
# Dockerfile for Meldestelle Monitoring Server
# Based on spring-boot-service template with monitoring specifics
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

LABEL stage=builder
LABEL service=monitoring-server
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

# Copy monitoring dependencies
COPY backend/infrastructure/monitoring/monitoring-client/ backend/infrastructure/monitoring/monitoring-client/
COPY backend/infrastructure/cache/ backend/infrastructure/cache/

# Copy monitoring-server specific files
COPY backend/infrastructure/monitoring/monitoring-server/ backend/infrastructure/monitoring/monitoring-server/
COPY backend/infrastructure/monitoring/monitoring-server/src/ backend/infrastructure/monitoring/monitoring-server/src/

# Build application
RUN ./gradlew :backend:infrastructure:monitoring:monitoring-server:dependencies --no-daemon --info
RUN ./gradlew :backend:infrastructure:monitoring:monitoring-server:bootJar --no-daemon --info

# ===================================================================
# Runtime Stage
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

# Comprehensive metadata
LABEL service="monitoring-server" \
      version="1.0.0" \
      description="Monitoring and Observability Server for Meldestelle" \
      maintainer="Meldestelle Development Team" \
      java.version="${JAVA_VERSION}"

# Build arguments for user configuration
ARG APP_USER=monitoruser
ARG APP_GROUP=monitorgroup
ARG APP_UID=1003
ARG APP_GID=1003

WORKDIR /app

# System setup with additional monitoring tools
RUN apk update && \
    apk upgrade && \
    apk add --no-cache curl jq tzdata ca-certificates netcat-openbsd && \
    rm -rf /var/cache/apk/*

# Create non-root user for monitoring-server
RUN addgroup -g ${APP_GID} -S ${APP_GROUP} && \
    adduser -u ${APP_UID} -S ${APP_USER} -G ${APP_GROUP} -h /app -s /bin/sh

# Create required directories with proper permissions
RUN mkdir -p /app/logs /app/tmp /app/config /app/metrics && \
    chown -R ${APP_USER}:${APP_GROUP} /app

# Copy the built JAR from builder stage
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} \
     /workspace/backend/infrastructure/monitoring/monitoring-server/build/libs/*.jar app.jar

# Switch to non-root user
USER ${APP_USER}

# Expose monitoring-server port and debug port
EXPOSE 8088 5005

# Enhanced health check for monitoring service
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS --max-time 3 http://localhost:8088/actuator/health/readiness || exit 1

# Optimized JVM settings for monitoring workloads (aligned with service standards)
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

# Monitoring-server specific Spring Boot configuration
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS
ENV SERVER_PORT=8088
ENV MANAGEMENT_SERVER_PORT=8088
ENV LOGGING_LEVEL_ROOT=INFO
ENV LOGGING_LEVEL_AT_MOCODE=DEBUG

# Monitoring-focused startup command with debug support
ENTRYPOINT ["sh", "-c", "\
    echo 'Starting Meldestelle Monitoring Server on port 8088...'; \
    echo 'Metrics endpoint: http://localhost:8088/actuator/metrics'; \
    echo 'Prometheus endpoint: http://localhost:8088/actuator/prometheus'; \
    if [ \"${DEBUG:-false}\" = \"true\" ]; then \
        echo 'Debug mode enabled on port 5005'; \
        exec java $JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar; \
    else \
        exec java $JAVA_OPTS -jar app.jar; \
    fi"]

# ===================================================================
# Build and Usage Instructions
# ===================================================================
# Build:
#   docker build -t meldestelle/monitoring-server:latest -f backend/infrastructure/monitoring/monitoring-server/Dockerfile .
#
# Run standalone:
#   docker run -p 8083:8083 --name monitoring-server meldestelle/monitoring-server:latest
#
# Run with debug:
#   docker run -p 8083:8083 -p 5005:5005 -e DEBUG=true --name monitoring-server meldestelle/monitoring-server:latest
#
# Access endpoints:
#   Health: http://localhost:8083/actuator/health
#   Metrics: http://localhost:8083/actuator/metrics
#   Prometheus: http://localhost:8083/actuator/prometheus
# ===================================================================
