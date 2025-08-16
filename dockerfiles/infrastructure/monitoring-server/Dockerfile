# syntax=docker/dockerfile:1.7

# ===================================================================
# Dockerfile for Meldestelle Monitoring Server
# Based on spring-boot-service template with monitoring specifics
# ===================================================================

# Build arguments
ARG GRADLE_VERSION=8.14
ARG JAVA_VERSION=21
ARG SPRING_PROFILES_ACTIVE=docker

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
COPY platform/ platform/
COPY core/ core/
COPY build.gradle.kts ./

# Copy monitoring dependencies
COPY infrastructure/monitoring/monitoring-client/ infrastructure/monitoring/monitoring-client/

# Copy monitoring-server specific files
COPY infrastructure/monitoring/monitoring-server/build.gradle.kts infrastructure/monitoring/monitoring-server/
COPY infrastructure/monitoring/monitoring-server/src/ infrastructure/monitoring/monitoring-server/src/

# Build application
RUN ./gradlew :infrastructure:monitoring:monitoring-server:dependencies --no-daemon --info
RUN ./gradlew :infrastructure:monitoring:monitoring-server:bootJar --no-daemon --info \
    -Pspring.profiles.active=${SPRING_PROFILES_ACTIVE}

# ===================================================================
# Runtime Stage
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

# Comprehensive metadata
LABEL service="monitoring-server" \
      version="1.0.0" \
      description="Monitoring and Observability Server for Meldestelle" \
      maintainer="Meldestelle Development Team" \
      java.version="${JAVA_VERSION}" \
      spring.profiles.active="${SPRING_PROFILES_ACTIVE}"

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
     /workspace/infrastructure/monitoring/monitoring-server/build/libs/*.jar app.jar

# Switch to non-root user
USER ${APP_USER}

# Expose monitoring-server port and debug port
EXPOSE 8083 5005

# Enhanced health check for monitoring service
HEALTHCHECK --interval=10s --timeout=5s --start-period=45s --retries=3 \
    CMD curl -fsS --max-time 3 http://localhost:8083/actuator/health/readiness || exit 1

# Optimized JVM settings for monitoring workloads
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+UseContainerSupport \
    -XX:G1HeapRegionSize=8m \
    -XX:+OptimizeStringConcat \
    -XX:+UseCompressedOops \
    -XX:MaxMetaspaceSize=256m \
    -Djava.security.egd=file:/dev/./urandom \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Europe/Vienna \
    -Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus,env,configprops,beans"

# Monitoring-server specific Spring Boot configuration
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
    SERVER_PORT=8083 \
    MANAGEMENT_SERVER_PORT=8083 \
    MANAGEMENT_ENDPOINTS_WEB_BASE_PATH=/actuator \
    LOGGING_LEVEL_ROOT=INFO \
    LOGGING_LEVEL_AT_MOCODE=DEBUG \
    LOGGING_LEVEL_MICROMETER=DEBUG

# Monitoring-focused startup command with debug support
ENTRYPOINT ["sh", "-c", "\
    echo 'Starting Meldestelle Monitoring Server on port 8083...'; \
    echo 'Metrics endpoint: http://localhost:8083/actuator/metrics'; \
    echo 'Prometheus endpoint: http://localhost:8083/actuator/prometheus'; \
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
#   docker build -t meldestelle/monitoring-server:latest -f infrastructure/monitoring/monitoring-server/Dockerfile .
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
