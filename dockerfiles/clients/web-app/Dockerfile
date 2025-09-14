# syntax=docker/dockerfile:1.8

# ===================================================================
# Multi-stage Dockerfile for Meldestelle Compose for Web Application
# Features: BuildKit cache mounts, security hardening, optimal layer caching
# Version: 2.0.0 - Enhanced optimization and security
# ===================================================================

# === CENTRALIZED BUILD ARGUMENTS ===
# Values sourced from docker/versions.toml and docker/build-args/
# Global arguments (docker/build-args/global.env)
ARG GRADLE_VERSION
ARG JAVA_VERSION
ARG BUILD_DATE
ARG VERSION

# Client-specific arguments (docker/build-args/clients.env)
ARG NGINX_VERSION
ARG NODE_VERSION

# ===================================================================
# Build Arguments for Client Configuration
# ===================================================================
ARG CLIENT_PATH=client
ARG CLIENT_MODULE=client

# ===================================================================
# Build Stage - Kotlin/JS (Compose for Web) Compilation
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS builder

ARG CLIENT_PATH=client
ARG CLIENT_MODULE=client
ARG BUILD_DATE
ARG VERSION=1.0.0

# Enhanced metadata
LABEL stage=builder \
      service="web-app" \
      maintainer="Meldestelle Development Team" \
      version="${VERSION}" \
      build.date="${BUILD_DATE}"

# Set working directory
WORKDIR /build

# Gradle optimizations for containerized builds
ENV GRADLE_OPTS="-Dorg.gradle.caching=true \
                 -Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.configureondemand=true \
                 -Dorg.gradle.workers.max=2 \
                 -Dorg.gradle.jvmargs=-Xmx2g \
                 -XX:+UseParallelGC \
                 -XX:MaxMetaspaceSize=512m"

# Set Gradle user home for better caching
ENV GRADLE_USER_HOME=/home/gradle/.gradle

# Install Node.js for Kotlin/Wasm npm operations
#ARG NODE_VERSION
#RUN apk add --no-cache \
#        nodejs \
#        npm \
#        curl && \
#    # Verify installation \
#    node --version && \
#    npm --version && \
#    # Create Gradle Node.js directory structure and symlinks \
#    mkdir -p /home/gradle/.gradle/nodejs/node-v22.0.0-linux-x64/bin && \
#    ln -sf /usr/bin/node /home/gradle/.gradle/nodejs/node-v22.0.0-linux-x64/bin/node && \
#    ln -sf /usr/bin/npm /home/gradle/.gradle/nodejs/node-v22.0.0-linux-x64/bin/npm && \
#    chown -R gradle:gradle /home/gradle/.gradle

# Copy Gradle files first for better layer caching
COPY gradle/ gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY build.gradle.kts ./

# Copy version catalog
COPY gradle/libs.versions.toml gradle/libs.versions.toml

# Copy all source files needed for the build
# Core and platform modules (dependencies)
COPY core/ core/
COPY platform/ platform/

# Infrastructure modules (if needed)
COPY infrastructure/ infrastructure/

# Client modules
COPY client/ client/

# Copy any additional required directories
COPY temp/ temp/
COPY docs/ docs/

# Make Gradle wrapper executable
RUN chmod +x gradlew

# Download and cache dependencies with BuildKit cache mount
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew ${CLIENT_MODULE}:dependencies --no-daemon --info

# Build client application with BuildKit cache mount
# For Compose Multiplatform Web (WASM), wasmJsBrowserDistribution produces static assets
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    echo "Building ${CLIENT_MODULE} module..." && \
    ./gradlew ${CLIENT_MODULE}:wasmJsBrowserDistribution --no-daemon --stacktrace --info

# ===================================================================
# Production Stage - Nginx Static File Server
# ===================================================================
FROM nginx:${NGINX_VERSION} AS production

ARG CLIENT_PATH=client
ARG BUILD_DATE
ARG VERSION=1.0.0

# Enhanced metadata
LABEL service="web-app" \
      version="${VERSION}" \
      environment="production" \
      description="Meldestelle Compose for Web Application served via Nginx" \
      maintainer="Meldestelle Development Team" \
      build.date="${BUILD_DATE}" \
      org.opencontainers.image.title="Meldestelle Web App" \
      org.opencontainers.image.description="Kotlin Multiplatform Web application with WASM" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.created="${BUILD_DATE}"

# Enhanced Alpine setup with security hardening
RUN apk update && \
    apk upgrade && \
    apk add --no-cache \
        curl \
        tzdata && \
    rm -rf /var/cache/apk/* && \
    addgroup -g 1001 -S nginx-group && \
    adduser -S -D -H -u 1001 -h /var/cache/nginx -s /sbin/nologin -G nginx-group -g nginx nginx-user

# Copy built distribution files from builder stage (WASM build output)
COPY --from=builder /build/${CLIENT_PATH}/build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html/
COPY --from=builder /build/${CLIENT_PATH}/src/wasmJsMain/resources/ /usr/share/nginx/html/

# Copy custom nginx configuration
COPY dockerfiles/clients/web-app/nginx.conf /etc/nginx/nginx.conf

# Create log directories and set permissions
RUN mkdir -p /var/log/nginx && \
    chown -R nginx-user:nginx-group /var/log/nginx && \
    chown -R nginx-user:nginx-group /var/cache/nginx && \
    chown -R nginx-user:nginx-group /usr/share/nginx/html

# Health check endpoint
RUN echo '{"status":"ok","service":"web-app"}' > /usr/share/nginx/html/health

# Switch to non-root user
USER nginx-user

# Expose port
EXPOSE 4000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl --fail http://localhost:4000/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
