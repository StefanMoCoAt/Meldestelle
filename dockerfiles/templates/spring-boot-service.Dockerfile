# syntax=docker/dockerfile:1.7

# ===================================================================
# Multi-stage Dockerfile Template for Spring Boot Services
# Features: Security hardening, monitoring support, optimal caching
# ===================================================================

# Build arguments
ARG GRADLE_VERSION=8.14
ARG JAVA_VERSION=21
ARG ALPINE_VERSION=3.19
ARG SPRING_PROFILES_ACTIVE=default

# ===================================================================
# Build Stage
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS builder

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
COPY platform/ platform/
COPY build.gradle.kts ./

# Copy service-specific files (replace SERVICE_PATH with actual path)
COPY ${SERVICE_PATH}/build.gradle.kts ${SERVICE_PATH}/
COPY ${SERVICE_PATH}/src/ ${SERVICE_PATH}/src/

# Build application
RUN ./gradlew :${SERVICE_NAME}:dependencies --no-daemon --info
RUN ./gradlew :${SERVICE_NAME}:bootJar --no-daemon --info \
    -Pspring.profiles.active=${SPRING_PROFILES_ACTIVE}

# ===================================================================
# Runtime Stage
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

# Metadata
LABEL service="${SERVICE_NAME}" \
      version="1.0.0" \
      maintainer="Meldestelle Development Team" \
      java.version="${JAVA_VERSION}"

# Build arguments
ARG APP_USER=appuser
ARG APP_GROUP=appgroup
ARG APP_UID=1001
ARG APP_GID=1001

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

# Copy JAR
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} \
     /workspace/${SERVICE_PATH}/build/libs/*.jar app.jar

USER ${APP_USER}

# Expose ports
EXPOSE ${SERVICE_PORT} 5005

# Health check
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS --max-time 2 http://localhost:${SERVICE_PORT}/actuator/health/readiness || exit 1

# JVM configuration
ENV JAVA_OPTS="-XX:MaxRAMPercentage=80.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+UseContainerSupport \
    -Djava.security.egd=file:/dev/./urandom \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=UTC \
    -Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus"

# Spring Boot configuration
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE} \
    SERVER_PORT=${SERVICE_PORT} \
    LOGGING_LEVEL_ROOT=INFO

# Startup command with debug support
ENTRYPOINT ["sh", "-c", "\
    if [ \"${DEBUG:-false}\" = \"true\" ]; then \
        echo 'Starting ${SERVICE_NAME} in DEBUG mode on port 5005...'; \
        exec java $JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar; \
    else \
        exec java $JAVA_OPTS -jar app.jar; \
    fi"]
