# syntax=docker/dockerfile:1.7

# ===================================================================
# Optimized Dockerfile for Spring Boot Ping Service
# Features: Multi-stage build, security hardening, monitoring support
# ===================================================================

# Build arguments for flexibility
ARG GRADLE_VERSION=8.14
ARG JAVA_VERSION=21
ARG ALPINE_VERSION=3.19
ARG SPRING_PROFILES_ACTIVE=default

# Build stage: compile the ping-service JAR inside Docker
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS builder

# Add metadata labels
LABEL stage=builder
LABEL service=ping-service
LABEL maintainer="Meldestelle Development Team"

WORKDIR /workspace

# Optimize Gradle build settings for containerized builds
ENV GRADLE_OPTS="-Dorg.gradle.caching=true \
                 -Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.configureondemand=true \
                 -Xmx2g"

# Copy gradle wrapper and configuration files first for optimal caching
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY gradle/ gradle/

# Copy platform dependencies (changes less frequently)
COPY platform/ platform/

# Copy root build configuration
COPY build.gradle.kts ./

# Copy ping-service specific files last (changes most frequently)
COPY temp/ping-service/build.gradle.kts temp/ping-service/
COPY temp/ping-service/src/ temp/ping-service/src/

# Download and cache dependencies in a separate layer
RUN ./gradlew :temp:ping-service:dependencies --no-daemon --info

# Build the application with optimizations
RUN ./gradlew :temp:ping-service:bootJar --no-daemon --info \
    -Pspring.profiles.active=${SPRING_PROFILES_ACTIVE}

# ===================================================================
# Runtime stage: optimized JRE image for production
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

# Add comprehensive metadata
LABEL service="ping-service" \
      version="1.0.0" \
      description="Microservice demonstrating circuit breaker patterns and monitoring" \
      maintainer="Meldestelle Development Team" \
      java.version="${JAVA_VERSION}" \
      spring.profiles.active="${SPRING_PROFILES_ACTIVE}" \
      build.date="${BUILD_DATE:-$(date -u +'%Y-%m-%dT%H:%M:%SZ')}"

# Build arguments for runtime configuration
ARG APP_USER=appuser
ARG APP_GROUP=appgroup
ARG APP_UID=1001
ARG APP_GID=1001

# Set working directory
WORKDIR /app

# Update Alpine packages and install required tools
RUN apk update && \
    apk upgrade && \
    apk add --no-cache \
        curl \
        jq \
        tzdata && \
    rm -rf /var/cache/apk/*

# Create non-root user with specific UID/GID for better security
RUN addgroup -g ${APP_GID} -S ${APP_GROUP} && \
    adduser -u ${APP_UID} -S ${APP_USER} -G ${APP_GROUP} -h /app -s /bin/sh

# Create required directories with proper permissions
RUN mkdir -p /app/logs /app/tmp && \
    chown -R ${APP_USER}:${APP_GROUP} /app

# Copy the built JAR from builder stage with proper ownership
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} \
     /workspace/temp/ping-service/build/libs/*.jar app.jar

# Switch to non-root user
USER ${APP_USER}

# Expose application port and debug port
EXPOSE 8082 5005

# Enhanced health check with better configuration
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS --max-time 2 http://localhost:8082/actuator/health/readiness || exit 1

# Optimized JVM settings for Spring Boot 3.x with monitoring support
ENV JAVA_OPTS_BASE="-XX:MaxRAMPercentage=80.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+UseContainerSupport \
    -XX:+OptimizeStringConcat \
    -XX:+UseCompressedOops \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.jmx.enabled=false \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=UTC"

# Monitoring and observability settings
ENV JAVA_OPTS_MONITORING="-Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus \
    -Dmanagement.endpoint.health.show-details=always \
    -Dmanagement.metrics.export.prometheus.enabled=true"

# Development/debugging options (enabled via environment variables)
ENV JAVA_OPTS_DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# Combined JAVA_OPTS (debug options only applied if DEBUG=true)
ENV JAVA_OPTS="${JAVA_OPTS_BASE} ${JAVA_OPTS_MONITORING}"

# Spring Boot configuration
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
    SERVER_PORT=8082 \
    LOGGING_LEVEL_ROOT=INFO

# Optimized entrypoint with conditional debug support
ENTRYPOINT ["sh", "-c", "\
    if [ \"${DEBUG:-false}\" = \"true\" ]; then \
        echo 'Starting application in DEBUG mode on port 5005...'; \
        exec java ${JAVA_OPTS} ${JAVA_OPTS_DEBUG} -jar app.jar; \
    else \
        exec java ${JAVA_OPTS} -jar app.jar; \
    fi"]
