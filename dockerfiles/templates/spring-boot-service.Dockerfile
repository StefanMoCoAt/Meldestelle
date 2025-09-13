# syntax=docker/dockerfile:1.7

# ===================================================================
# Multi-stage Dockerfile Template for Spring Boot Services
# Features: Security hardening, monitoring support, optimal caching, centralized version management
# Version: 3.0.0 - Central Version Management Implementation
# ===================================================================
# IMPORTANT: Build arguments are now managed centrally via docker/versions.toml
# Use: docker-compose build or scripts/docker-build.sh for automated version injection

# === CENTRALIZED BUILD ARGUMENTS ===
# Values sourced from docker/versions.toml and docker/build-args/
# Global arguments (docker/build-args/global.env)
ARG GRADLE_VERSION
ARG JAVA_VERSION
ARG BUILD_DATE
ARG VERSION

# Service-specific arguments (docker/build-args/services.env or infrastructure.env)
ARG SPRING_PROFILES_ACTIVE
ARG SERVICE_PATH=.
ARG SERVICE_NAME=spring-boot-service
ARG SERVICE_PORT=8080

# ===================================================================
# Build Stage
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS builder

# Re-declare build arguments for this stage
ARG SERVICE_PATH=.
ARG SERVICE_NAME=spring-boot-service
ARG SERVICE_PORT=8080
ARG SPRING_PROFILES_ACTIVE=default

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

# Create standalone project structure when using template generically
RUN if [ "${SERVICE_PATH}" = "." ]; then \
        echo "Creating isolated standalone Spring Boot application..."; \
        mkdir -p /tmp/standalone-app/src/main/kotlin/com/example /tmp/standalone-app/src/main/resources; \
        cd /tmp/standalone-app; \
        echo 'plugins { id("org.springframework.boot") version "3.2.0"; id("io.spring.dependency-management") version "1.1.4"; kotlin("jvm") version "2.2.0"; kotlin("plugin.spring") version "2.2.0" }' > build.gradle.kts; \
        echo 'group = "com.example"; version = "1.0.0"; java { sourceCompatibility = JavaVersion.VERSION_21 }' >> build.gradle.kts; \
        echo 'repositories { mavenCentral() }' >> build.gradle.kts; \
        echo 'dependencies { implementation("org.springframework.boot:spring-boot-starter-web"); testImplementation("org.springframework.boot:spring-boot-starter-test") }' >> build.gradle.kts; \
        echo 'package com.example; import org.springframework.boot.autoconfigure.SpringBootApplication; import org.springframework.boot.runApplication; @SpringBootApplication class Application; fun main(args: Array<String>) { runApplication<Application>(*args) }' > src/main/kotlin/com/example/Application.kt; \
        echo 'rootProject.name = "standalone-app"' > settings.gradle.kts; \
        cp /workspace/gradlew /workspace/gradlew.bat .; \
        cp -r /workspace/gradle .; \
        echo "Building standalone application..."; \
        ./gradlew bootJar --no-daemon --info -Pspring.profiles.active=${SPRING_PROFILES_ACTIVE}; \
        cp build/libs/*.jar /workspace/app.jar; \
    else \
        echo "Building specific service: ${SERVICE_NAME}"; \
        ./gradlew :${SERVICE_NAME}:dependencies --no-daemon --info; \
        ./gradlew :${SERVICE_NAME}:bootJar --no-daemon --info -Pspring.profiles.active=${SPRING_PROFILES_ACTIVE}; \
        cp ${SERVICE_PATH}/build/libs/*.jar /workspace/app.jar; \
    fi

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

# Re-declare build arguments for runtime stage
ARG SERVICE_PATH=.
ARG SERVICE_NAME=spring-boot-service
ARG SERVICE_PORT=8080

# Copy JAR (different locations for standalone vs service-specific builds)
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} \
     /workspace/app.jar app.jar

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
