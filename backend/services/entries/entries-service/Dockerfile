# syntax=docker/dockerfile:1.8

# ===================================================================
# Multi-stage Dockerfile for Meldestelle Entries Service
# ===================================================================

# === CENTRALIZED BUILD ARGUMENTS ===
ARG GRADLE_VERSION
ARG JAVA_VERSION
ARG BUILD_DATE
ARG VERSION

FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS builder

LABEL stage=builder \
      service=entries-service \
      maintainer="Meldestelle Development Team" \
      version="${VERSION}" \
      build.date="${BUILD_DATE}"

WORKDIR /workspace

ENV GRADLE_OPTS="-Dorg.gradle.caching=true \
                 -Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.workers.max=2 \
                 -Dorg.gradle.jvmargs=-Xmx2g \
                 -XX:+UseParallelGC \
                 -XX:MaxMetaspaceSize=512m"

ENV GRADLE_USER_HOME=/home/gradle/.gradle

COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY gradle/ gradle/
RUN chmod +x gradlew
COPY platform/ platform/
COPY frontend/ frontend/
COPY core/ core/
COPY backend/ backend/
COPY docs/ docs/
COPY build.gradle.kts ./

# Copy entries modules
COPY backend/services/entries/entries-service/ backend/services/entries/entries-service/

RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew :backend:services:entries:entries-service:dependencies --no-daemon --info

RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew :backend:services:entries:entries-service:bootJar --no-daemon --info

# ===================================================================
# Runtime stage
# ===================================================================
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS runtime

ARG BUILD_DATE
ARG VERSION
ARG JAVA_VERSION

ENV JAVA_VERSION=${JAVA_VERSION} \
    VERSION=${VERSION} \
    BUILD_DATE=${BUILD_DATE}

LABEL service="entries-service" \
      version="${VERSION}" \
      description="Microservice for Entries Management" \
      maintainer="Meldestelle Development Team" \
      java.version="${JAVA_VERSION}" \
      build.date="${BUILD_DATE}"

ARG APP_USER=appuser
ARG APP_GROUP=appgroup
ARG APP_UID=1001
ARG APP_GID=1001

WORKDIR /app

RUN apk update && \
    apk upgrade && \
    apk add --no-cache curl tzdata tini && \
    rm -rf /var/cache/apk/* && \
    addgroup -g ${APP_GID} -S ${APP_GROUP} && \
    adduser -u ${APP_UID} -S ${APP_USER} -G ${APP_GROUP} -h /app -s /bin/sh && \
    mkdir -p /app/logs /app/tmp /app/config && \
    chown -R ${APP_USER}:${APP_GROUP} /app && \
    chmod -R 750 /app

COPY --from=builder --chown=${APP_USER}:${APP_GROUP} \
     /workspace/backend/services/entries/entries-service/build/libs/*.jar app.jar

USER ${APP_USER}

EXPOSE 8083 5005

HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -fsS --max-time 2 http://localhost:8083/actuator/health/readiness || exit 1

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
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
    -Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus \
    -Dmanagement.endpoint.health.show-details=always \
    -Dmanagement.prometheus.metrics.export.enabled=true"

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS
ENV SERVER_PORT=8083
ENV LOGGING_LEVEL_ROOT=INFO

ENTRYPOINT ["tini", "--", "sh", "-c", "\
    echo 'Starting Entries Service with Java ${JAVA_VERSION}...'; \
    echo 'Service port: ${SERVER_PORT}'; \
    if [ \"${DEBUG:-false}\" = \"true\" ]; then \
        echo 'DEBUG mode enabled'; \
        exec java ${JAVA_OPTS} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar; \
    else \
        exec java ${JAVA_OPTS} -jar app.jar; \
    fi"]
