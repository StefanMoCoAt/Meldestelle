# =============================================================================
# Multi-stage Dockerfile for Meldestelle API Gateway
# Optimized for security, performance, and maintainability
# =============================================================================

# =============================================================================
# Build stage - Full Gradle build for better dependency management
# =============================================================================
FROM gradle:8.14-jdk21-alpine AS builder

LABEL stage=builder
LABEL service=api-gateway
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

# Copy gateway specific files
COPY infrastructure/gateway/build.gradle.kts infrastructure/gateway/
COPY infrastructure/gateway/src/ infrastructure/gateway/src/

# Build application
RUN ./gradlew :infrastructure:gateway:dependencies --no-daemon --info
RUN ./gradlew :infrastructure:gateway:bootJar --no-daemon --info

# Extract JAR layers for optimized Docker layer caching
WORKDIR /builder
RUN cp /workspace/infrastructure/gateway/build/libs/*.jar app.jar && \
    java -Djarmode=layertools -jar app.jar extract

# =============================================================================
# Runtime stage - Optimized production image
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# =============================================================================
# Metadata and Build Information
# =============================================================================
LABEL maintainer="Meldestelle Team <support@meldestelle.at>"
LABEL description="Self-Contained Systems API Gateway for Austrian Equestrian Federation"
LABEL version="1.0.0"
LABEL org.opencontainers.image.title="Meldestelle Gateway"
LABEL org.opencontainers.image.description="Spring Cloud Gateway with Circuit Breaker, Health Monitoring, and Service Discovery"
LABEL org.opencontainers.image.vendor="Meldestelle"
LABEL org.opencontainers.image.version="1.0.0"
LABEL org.opencontainers.image.created="2025-08-14"
LABEL org.opencontainers.image.source="https://github.com/meldestelle/api-gateway"
LABEL org.opencontainers.image.documentation="https://api.meldestelle.at/docs"

# =============================================================================
# Security and System Setup
# =============================================================================
# Install curl for health checks and security updates
RUN apk update && \
    apk add --no-cache curl ca-certificates tzdata && \
    apk upgrade && \
    rm -rf /var/cache/apk/*

# Create dedicated non-root user with specific UID/GID for security
RUN addgroup -g 1001 -S gateway && \
    adduser -u 1001 -S gateway -G gateway -s /bin/sh

# Set timezone for consistent logging and operations
ENV TZ=Europe/Vienna

# =============================================================================
# Application Setup
# =============================================================================
# Set working directory
WORKDIR /app

# Create required directories with proper permissions
RUN mkdir -p /app/logs /app/tmp && \
    chown -R gateway:gateway /app

# Copy Spring Boot layers in optimal order for Docker layer caching
# Dependencies change less frequently than application code
COPY --from=builder --chown=gateway:gateway /builder/dependencies/ ./
COPY --from=builder --chown=gateway:gateway /builder/spring-boot-loader/ ./
COPY --from=builder --chown=gateway:gateway /builder/snapshot-dependencies/ ./
COPY --from=builder --chown=gateway:gateway /builder/application/ ./

# =============================================================================
# Runtime Configuration
# =============================================================================
# Switch to non-root user for security
USER gateway

# Expose application port and debug port
EXPOSE 8080 5005

# =============================================================================
# JVM and Application Configuration
# =============================================================================
# Optimized JVM settings for containerized Spring Boot reactive applications
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
    -Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus,gateway"

# Spring Boot specific optimizations
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    SPRING_PROFILES_ACTIVE=docker \
    SERVER_PORT=8080 \
    MANAGEMENT_SERVER_PORT=8080 \
    LOGGING_LEVEL_ROOT=INFO

# =============================================================================
# Health Check Configuration
# =============================================================================
# Enhanced health check with proper timing for Spring Boot startup
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS --max-time 3 http://localhost:8080/actuator/health/readiness || exit 1

# =============================================================================
# Application Startup
# =============================================================================
# Gateway-focused startup command with debug support
ENTRYPOINT ["sh", "-c", "\
    echo 'Starting Meldestelle API Gateway on port 8080...'; \
    if [ \"${DEBUG:-false}\" = \"true\" ]; then \
        echo 'Debug mode enabled on port 5005'; \
        exec java $JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 org.springframework.boot.loader.launch.JarLauncher; \
    else \
        exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher; \
    fi"]

# =============================================================================
# Documentation
# =============================================================================
# Build commands:
#   docker build -t meldestelle/gateway:latest -f infrastructure/gateway/Dockerfile .
#   docker run -p 8080:8080 --name gateway meldestelle/gateway:latest
#
# Key optimizations:
# - Multi-stage build with JAR layer extraction for better caching
# - Non-root user execution for security
# - Optimized JVM settings for containers
# - Comprehensive health checks
# - Proper timezone and encoding configuration
# - Security updates and minimal attack surface
# =============================================================================
