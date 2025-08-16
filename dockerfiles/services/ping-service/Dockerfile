# syntax=docker/dockerfile:1.8

# ===================================================================
# Optimized Dockerfile for Spring Boot Ping Service
# Features: Multi-stage build, security hardening, monitoring support, enhanced caching
# ===================================================================

# Build arguments for flexibility
ARG GRADLE_VERSION=8.14
ARG JAVA_VERSION=21
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

# Download and cache dependencies in a separate layer with build cache
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew :temp:ping-service:dependencies --no-daemon --info

# Build the application with optimizations and build cache
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew :temp:ping-service:bootJar --no-daemon --info \
    -Pspring.profiles.active=${SPRING_PROFILES_ACTIVE}

# ===================================================================
# Runtime stage: optimized JRE image for production
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

# Build arguments for runtime stage
ARG BUILD_DATE
ARG SPRING_PROFILES_ACTIVE=default

# Add comprehensive metadata
LABEL service="ping-service" \
      version="1.0.0" \
      description="Microservice demonstrating circuit breaker patterns and monitoring" \
      maintainer="Meldestelle Development Team" \
      java.version="${JAVA_VERSION}" \
      spring.profiles.active="${SPRING_PROFILES_ACTIVE}" \
      build.date="${BUILD_DATE}"

# Build arguments for runtime configuration
ARG APP_USER=appuser
ARG APP_GROUP=appgroup
ARG APP_UID=1001
ARG APP_GID=1001

# Set working directory
WORKDIR /app

# Update Alpine packages, install tools, create user and directories in one layer
RUN apk update && \
    apk upgrade && \
    apk add --no-cache \
        curl \
        tzdata && \
    rm -rf /var/cache/apk/* && \
    addgroup -g ${APP_GID} -S ${APP_GROUP} && \
    adduser -u ${APP_UID} -S ${APP_USER} -G ${APP_GROUP} -h /app -s /bin/sh && \
    mkdir -p /app/logs /app/tmp && \
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

# Optimized JVM settings for Spring Boot 3.x with Java 21 and monitoring support
ENV JAVA_OPTS="-XX:MaxRAMPercentage=80.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+UseContainerSupport \
    -Djava.security.egd=file:/dev/./urandom \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Europe/Vienna \
    -Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus \
    -Dmanagement.endpoint.health.show-details=always \
    -Dmanagement.metrics.export.prometheus.enabled=true"

# Spring Boot configuration
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
    SERVER_PORT=8082 \
    LOGGING_LEVEL_ROOT=INFO

# Enhanced entrypoint with conditional debug support and better logging
ENTRYPOINT ["sh", "-c", "\
    echo 'Starting ping-service with Java ${JAVA_VERSION}...'; \
    echo 'Active Spring profiles: ${SPRING_PROFILES_ACTIVE}'; \
    if [ \"${DEBUG:-false}\" = \"true\" ]; then \
        echo 'DEBUG mode enabled - remote debugging available on port 5005'; \
        exec java ${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar; \
    else \
        echo 'Starting application in production mode'; \
        exec java ${JAVA_OPTS} -jar app.jar; \
    fi"]
