# syntax=docker/dockerfile:1.8
# ===================================================================
# Multi-Stage Dockerfile for Meldestelle Web-App (Kotlin/JS)
# Version: 2.3.0 - Optimized for Production Build
# ===================================================================

# === GLOBAL ARGS ===
ARG GRADLE_VERSION=8.5
ARG JAVA_VERSION=21
ARG NGINX_IMAGE_TAG=1.25-alpine
ARG VERSION=1.0.0-SNAPSHOT
ARG BUILD_DATE

# ===================================================================
# Stage 1: Build Stage
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION} AS builder

LABEL stage=builder
WORKDIR /workspace

# 1. Gradle Optimizations
ENV GRADLE_OPTS="-Dorg.gradle.caching=true \
                 -Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.workers.max=4 \
                 -Dorg.gradle.jvmargs=-Xmx3g \
                 -XX:+UseParallelGC"
ENV GRADLE_USER_HOME=/home/gradle/.gradle

# 2. Copy Build Configs
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY gradle/ gradle/
COPY build.gradle.kts ./

RUN chmod +x gradlew

# 3. Copy Sources (Monorepo Structure)
# We copy only what's needed to invalidate cache less frequently
COPY platform/ platform/
COPY core/ core/
COPY backend/ backend/
COPY frontend/ frontend/
COPY config/ config/
COPY contracts/ contracts/

# Create dummy docs dir to satisfy Gradle configuration phase if needed
RUN mkdir -p docs

# 4. Build Web App (Production Only)
# We explicitly build the production distribution for optimized artifacts.
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew :frontend:shells:meldestelle-portal:jsBrowserDistribution \
    -Pproduction=true \
    --no-daemon \
    --stacktrace

# 5. Prepare Dist
# Verify build output exists and move to a clean directory
RUN mkdir -p /app/dist && \
    if [ -d "frontend/shells/meldestelle-portal/build/dist/js/productionExecutable" ]; then \
        cp -r frontend/shells/meldestelle-portal/build/dist/js/productionExecutable/* /app/dist/; \
    else \
        echo "ERROR: Build artifact not found at expected path!" && exit 1; \
    fi

# ===================================================================
# Stage 2: Runtime Stage (Alpine Nginx)
# ===================================================================
FROM nginx:${NGINX_IMAGE_TAG}

ARG VERSION
ARG BUILD_DATE

# Metadata
LABEL service="web-app" \
      version="${VERSION}" \
      maintainer="Meldestelle Development Team" \
      build.date="${BUILD_DATE}" \
      org.opencontainers.image.title="Meldestelle Web-App"

# Tools & User Setup
RUN apk add --no-cache curl && \
    rm /etc/nginx/conf.d/default.conf

# Copy Artifacts
COPY --from=builder /workspace/config/docker/nginx/web-app/nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/dist/ /usr/share/nginx/html/
# Ensure a default favicon is always present
COPY --from=builder /workspace/config/docker/nginx/web-app/favicon.svg /usr/share/nginx/html/favicon.svg

# Permissions
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx /var/log/nginx /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid

USER nginx
EXPOSE 4000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:4000/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
