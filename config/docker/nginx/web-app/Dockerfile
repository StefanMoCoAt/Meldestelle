# syntax=docker/dockerfile:1.8
# ===================================================================
# Multi-Stage Dockerfile for Meldestelle Web-App (Kotlin/JS)
# Version: 2.2.2 - Added 'contracts' to build context
# ===================================================================

# === GLOBAL ARGS ===
ARG GRADLE_VERSION
ARG JAVA_VERSION
ARG NODE_VERSION
ARG NGINX_IMAGE_TAG
ARG WEB_BUILD_PROFILE
ARG VERSION
ARG BUILD_DATE

# ===================================================================
# Stage 1: Build Stage (Debian-based for Node.js compatibility)
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION} AS builder

ARG WEB_BUILD_PROFILE
ARG VERSION
ARG BUILD_DATE

LABEL stage=builder
WORKDIR /workspace

# 1. Gradle Optimizations
ENV GRADLE_OPTS="-Dorg.gradle.caching=true \
                 -Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.workers.max=2 \
                 -Dorg.gradle.jvmargs=-Xmx2g \
                 -XX:+UseParallelGC \
                 -XX:MaxMetaspaceSize=512m"
ENV GRADLE_USER_HOME=/home/gradle/.gradle

# 2. Copy Build Configs
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY gradle/ gradle/
COPY build.gradle.kts ./

RUN chmod +x gradlew

# 3. Copy Sources (Monorepo Structure)
COPY platform/ platform/
COPY core/ core/
COPY backend/ backend/
COPY frontend/ frontend/
COPY config/ config/
# FIX: 'contracts' is required by Gradle during configuration phase
COPY contracts/ contracts/

# FIX: Create dummy docs dir to satisfy Gradle configuration phase
RUN mkdir -p docs

# 4. Resolve Dependencies
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew :frontend:shells:meldestelle-portal:dependencies --no-daemon

# 5. Build Web App
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    if [ "$WEB_BUILD_PROFILE" = "prod" ]; then \
        echo "Building for PRODUCTION..."; \
        ./gradlew :frontend:shells:meldestelle-portal:jsBrowserDistribution -Pproduction=true --no-daemon; \
        mkdir -p /app/dist && \
        cp -r frontend/shells/meldestelle-portal/build/dist/js/productionExecutable/* /app/dist/; \
    else \
        echo "Building for DEVELOPMENT..."; \
        ./gradlew :frontend:shells:meldestelle-portal:jsBrowserDevelopmentExecutable --no-daemon; \
        mkdir -p /app/dist && \
        cp -r frontend/shells/meldestelle-portal/build/dist/js/developmentExecutable/* /app/dist/; \
    fi

# ===================================================================
# Stage 2: Runtime Stage (Alpine Nginx)
# ===================================================================
FROM nginx:${NGINX_IMAGE_TAG}

ARG VERSION
ARG BUILD_DATE
ARG JAVA_VERSION

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
