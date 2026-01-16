# syntax=docker/dockerfile:1.8

# ===================================================================
# Multi-stage Dockerfile for Meldestelle API Gateway
# Features: Security hardening, monitoring support, optimal caching, BuildKit cache mounts
# Version: 2.2.1 - Optimized for Monorepo (Fixed missing frontend dirs)
# ===================================================================

# === CENTRALIZED BUILD ARGUMENTS ===
ARG GRADLE_VERSION
ARG JAVA_VERSION
ARG BUILD_DATE
ARG VERSION

# ===================================================================
# Build Stage
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS builder

ARG VERSION
ARG BUILD_DATE

LABEL stage=builder
LABEL service="api-gateway"
LABEL maintainer="Meldestelle Development Team"

WORKDIR /workspace

# Gradle optimizations
ENV GRADLE_OPTS="-Dorg.gradle.caching=true \
                 -Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.workers.max=2 \
                 -Dorg.gradle.jvmargs=-Xmx2g \
                 -XX:+UseParallelGC \
                 -XX:MaxMetaspaceSize=512m"

ENV GRADLE_USER_HOME=/home/gradle/.gradle

# Copy gradle wrapper and configuration files
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY gradle/ gradle/

RUN chmod +x gradlew

# Copy platform and core dependencies
COPY platform/ platform/
COPY core/ core/

# Copy backend directories
COPY backend/infrastructure/ backend/infrastructure/
COPY backend/services/ backend/services/
COPY contracts/ contracts/

# Create dummy frontend directories to satisfy settings.gradle.kts include paths
# This prevents Gradle from failing configuration phase without copying actual frontend code
RUN mkdir -p \
    frontend/core/domain \
    frontend/core/design-system \
    frontend/core/navigation \
    frontend/core/network \
    frontend/core/local-db \
    frontend/core/sync \
    frontend/features/auth-feature \
    frontend/features/ping-feature \
    frontend/shared \
    frontend/shells/meldestelle-portal \
    docs

# Copy root build configuration
COPY build.gradle.kts ./

# Download and cache dependencies
RUN --mount=type=cache,id=gradle-cache-gateway,target=/home/gradle/.gradle/caches \
    --mount=type=cache,id=gradle-wrapper-gateway,target=/home/gradle/.gradle/wrapper \
    ./gradlew :backend:infrastructure:gateway:dependencies --info

# Build the application
RUN --mount=type=cache,id=gradle-cache-gateway,target=/home/gradle/.gradle/caches \
    --mount=type=cache,id=gradle-wrapper-gateway,target=/home/gradle/.gradle/wrapper \
    ./gradlew :backend:infrastructure:gateway:bootJar --info

# Extract JAR layers
RUN mkdir -p build/dependency && \
    (cd build/dependency; java -Djarmode=layertools -jar /workspace/backend/infrastructure/gateway/build/libs/*.jar extract)

# ===================================================================
# Runtime Stage
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

ARG BUILD_DATE
ARG VERSION
ARG JAVA_VERSION

ENV JAVA_VERSION=${JAVA_VERSION} \
    VERSION=${VERSION} \
    BUILD_DATE=${BUILD_DATE}

LABEL service="api-gateway" \
      version="${VERSION}" \
      description="Spring Cloud Gateway for Meldestelle microservices architecture" \
      maintainer="Meldestelle Development Team" \
      org.opencontainers.image.title="Meldestelle API Gateway" \
      org.opencontainers.image.created="${BUILD_DATE}"

ARG APP_USER=gateway
ARG APP_GROUP=gateway
ARG APP_UID=1001
ARG APP_GID=1001

WORKDIR /app

RUN apk update && \
    apk upgrade && \
    apk add --no-cache \
        curl \
        tzdata \
        tini && \
    rm -rf /var/cache/apk/* && \
    addgroup -g ${APP_GID} -S ${APP_GROUP} && \
    adduser -u ${APP_UID} -S ${APP_USER} -G ${APP_GROUP} -h /app -s /bin/sh && \
    mkdir -p /app/logs /app/tmp /app/config && \
    chown -R ${APP_USER}:${APP_GROUP} /app && \
    chmod -R 750 /app

COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /workspace/build/dependency/dependencies/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /workspace/build/dependency/spring-boot-loader/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /workspace/build/dependency/snapshot-dependencies/ ./
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /workspace/build/dependency/application/ ./

USER ${APP_USER}

EXPOSE 8081 5005

HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS --max-time 2 http://localhost:8081/actuator/health/readiness || exit 1

ENV JAVA_OPTS="-XX:MaxRAMPercentage=80.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+UseContainerSupport \
    -XX:G1HeapRegionSize=16m \
    -XX:G1ReservePercent=25 \
    -XX:InitiatingHeapOccupancyPercent=30 \
    -XX:+AlwaysPreTouch \
    -XX:+DisableExplicitGC \
    -Djava.security.egd=file:/dev/./urandom \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Europe/Vienna \
    -Dspring.backgroundpreinitializer.ignore=true \
    -Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus,gateway \
    -Dmanagement.endpoint.health.show-details=always \
    -Dmanagement.prometheus.metrics.export.enabled=true"

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    SERVER_PORT=8081 \
    LOGGING_LEVEL_ROOT=INFO \
    LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_CLOUD_GATEWAY=DEBUG

ENTRYPOINT ["tini", "--", "sh", "-c", "\
    echo 'Starting API Gateway with Java ${JAVA_VERSION}...'; \
    echo 'Active Spring profiles: '${SPRING_PROFILES_ACTIVE:-not-set}; \
    echo 'Gateway port: ${SERVER_PORT}'; \
    MEMORY_LIMIT=$(cat /sys/fs/cgroup/memory.max 2>/dev/null || cat /sys/fs/cgroup/memory/memory.limit_in_bytes 2>/dev/null || echo 'unlimited'); \
    echo \"Container memory limit: $MEMORY_LIMIT\"; \
    if [ \"${DEBUG:-false}\" = \"true\" ]; then \
        echo 'DEBUG mode enabled - remote debugging available on port 5005'; \
        exec java ${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 org.springframework.boot.loader.launch.JarLauncher; \
    else \
        echo 'Starting API Gateway in production mode'; \
        exec java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher; \
    fi"]
