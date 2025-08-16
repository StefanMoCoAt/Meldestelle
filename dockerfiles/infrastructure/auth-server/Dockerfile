# syntax=docker/dockerfile:1.7

# ===================================================================
# Dockerfile for Meldestelle Auth Server
# Based on spring-boot-service template with auth-server specifics
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
LABEL service=auth-server
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

# Copy infrastructure dependencies
COPY infrastructure/auth/auth-client/ infrastructure/auth/auth-client/

# Copy auth-server specific files
COPY infrastructure/auth/auth-server/build.gradle.kts infrastructure/auth/auth-server/
COPY infrastructure/auth/auth-server/src/ infrastructure/auth/auth-server/src/

# Build application
RUN ./gradlew :infrastructure:auth:auth-server:dependencies --no-daemon --info
RUN ./gradlew :infrastructure:auth:auth-server:bootJar --no-daemon --info \
    -Pspring.profiles.active=${SPRING_PROFILES_ACTIVE}

# ===================================================================
# Runtime Stage
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

# Comprehensive metadata
LABEL service="auth-server" \
      version="1.0.0" \
      description="Authentication and Authorization Server for Meldestelle" \
      maintainer="Meldestelle Development Team" \
      java.version="${JAVA_VERSION}" \
      spring.profiles.active="${SPRING_PROFILES_ACTIVE}"

# Build arguments for user configuration
ARG APP_USER=authuser
ARG APP_GROUP=authgroup
ARG APP_UID=1002
ARG APP_GID=1002

WORKDIR /app

# System setup with security updates
RUN apk update && \
    apk upgrade && \
    apk add --no-cache curl jq tzdata ca-certificates && \
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

# Security-focused startup command with debug support
ENTRYPOINT ["sh", "-c", "\
    echo 'Starting Meldestelle Auth Server on port 8081...'; \
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
#   docker build -t meldestelle/auth-server:latest -f infrastructure/auth/auth-server/Dockerfile .
#
# Run standalone:
#   docker run -p 8081:8081 --name auth-server meldestelle/auth-server:latest
#
# Run with debug:
#   docker run -p 8081:8081 -p 5005:5005 -e DEBUG=true --name auth-server meldestelle/auth-server:latest
# ===================================================================
