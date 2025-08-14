# =============================================================================
# Multi-stage Dockerfile for Meldestelle API Gateway
# Optimized for security, performance, and maintainability
# =============================================================================

# =============================================================================
# Build stage - Extract JAR layers for better caching
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS builder

# Set working directory for build operations
WORKDIR /builder

# Copy the gateway JAR file for layer extraction
COPY infrastructure/gateway/build/libs/*.jar app.jar

# Extract JAR layers for optimized Docker layer caching
# This allows Docker to cache dependencies separately from application code
RUN java -Djarmode=layertools -jar app.jar extract

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

# Expose application port
EXPOSE 8080

# =============================================================================
# JVM and Application Configuration
# =============================================================================
# Optimized JVM settings for containerized Spring Boot reactive applications
ENV JAVA_OPTS="-server \
    -Xmx512m \
    -Xms256m \
    -XX:+UseG1GC \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+UseCGroupMemoryLimitForHeap \
    -Djava.security.egd=file:/dev/./urandom \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Europe/Vienna"

# Spring Boot specific optimizations
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus

# =============================================================================
# Health Check Configuration
# =============================================================================
# Comprehensive health check with proper timing for Spring Boot startup
HEALTHCHECK --interval=30s --timeout=15s --start-period=90s --retries=3 \
  CMD curl -f -s http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

# =============================================================================
# Application Startup
# =============================================================================
# Run the application with optimized settings and proper signal handling
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]

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
