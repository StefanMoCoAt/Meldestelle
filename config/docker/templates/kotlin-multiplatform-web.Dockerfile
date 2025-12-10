# ===================================================================
# Multi-stage Dockerfile Template for Kotlin Multiplatform Web Client
# Features: Kotlin/JS compilation, Nginx serving, development support, centralized version management
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

# Client-specific arguments (docker/build-args/clients.env)
ARG NODE_VERSION
ARG NGINX_VERSION

# Client-specific build arguments (can be overridden at build time)
ARG CLIENT_PATH=client/web-app
ARG CLIENT_MODULE=client:web-app
ARG CLIENT_NAME=web-app

# ===================================================================
# Build Stage - Kotlin/JS Compilation
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS kotlin-builder

# Re-declare build arguments for kotlin-builder stage
ARG CLIENT_PATH=client/web-app
ARG CLIENT_MODULE=client:web-app
ARG CLIENT_NAME=web-app
ARG NODE_VERSION

LABEL stage=kotlin-builder
LABEL maintainer="Meldestelle Development Team"

WORKDIR /workspace

# Install specific Node.js version for Kotlin/JS compatibility
RUN apk add --no-cache wget ca-certificates && \
    wget -q -O - https://unofficial-builds.nodejs.org/download/release/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-x64-musl.tar.xz | \
    tar -xJ -C /usr/local --strip-components=1 && \
    apk del wget ca-certificates && \
    rm -rf /var/cache/apk/* && \
    npm config set cache /tmp/.npm-cache && \
    npm config set progress false && \
    npm config set audit false

# Gradle optimizations for Kotlin Multiplatform builds
ENV GRADLE_OPTS="-Dorg.gradle.caching=true \
                 -Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.configureondemand=true \
                 -Dorg.gradle.jvmargs=-Xmx3g \
                 -Dkotlin.compiler.execution.strategy=in-process"

# Kotlin/JS and Node.js environment variables
ENV NODE_OPTIONS="--max-old-space-size=4096" \
    NPM_CONFIG_CACHE="/tmp/.npm-cache" \
    KOTLIN_JS_GENERATE_EXTERNALS=false

# Copy build configuration files first for optimal caching
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY gradle/ gradle/
COPY build.gradle.kts ./

# Copy platform and core dependencies
COPY platform/ platform/
COPY core/ core/

# Copy client modules in dependency order
COPY client/common-ui/ client/common-ui/
COPY ${CLIENT_PATH}/ ${CLIENT_PATH}/

# Clear npm cache and verify Node.js installation
RUN npm cache clean --force && \
    node --version && npm --version

# Download dependencies in a separate layer
RUN ./gradlew :${CLIENT_MODULE}:dependencies --no-daemon --info --stacktrace

# Build web application with production optimizations and better error handling
RUN ./gradlew :${CLIENT_MODULE}:jsBrowserProductionWebpack --no-daemon --info --stacktrace --debug

# Verify build output
RUN ls -la /workspace/${CLIENT_PATH}/build/dist/ || (echo "Build failed - no dist directory found" && exit 1)

# ===================================================================
# Production Stage - Nginx serving
# ===================================================================
FROM nginx:${NGINX_VERSION} AS runtime

# Re-declare build arguments for runtime stage
ARG CLIENT_PATH=client/web-app
ARG CLIENT_MODULE=client:web-app
ARG CLIENT_NAME=web-app

# Metadata
LABEL service="${CLIENT_NAME}" \
      version="1.0.0" \
      description="Kotlin Multiplatform Web Client for Meldestelle" \
      maintainer="Meldestelle Development Team"

# Security and system setup
RUN apk update && \
    apk upgrade && \
    apk add --no-cache curl jq && \
    rm -rf /var/cache/apk/*

# Remove default nginx content and logs
RUN rm -rf /usr/share/nginx/html/* && \
    rm -f /var/log/nginx/*.log

# Copy built web application from builder stage
COPY --from=kotlin-builder /workspace/${CLIENT_PATH}/build/dist/ /usr/share/nginx/html/

# Copy nginx configuration
COPY ${CLIENT_PATH}/nginx.conf /etc/nginx/nginx.conf

# Set proper permissions for nginx
RUN chown -R nginx:nginx /usr/share/nginx/html /var/cache/nginx /var/run /var/log/nginx && \
    chmod -R 755 /usr/share/nginx/html

# Switch to nginx user for security
USER nginx

# Health check specifically for the web application
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD curl -f http://localhost/health || exit 1

# Expose HTTP port
EXPOSE 80

# Start nginx with proper signal handling for graceful shutdowns
STOPSIGNAL SIGQUIT

# Run nginx in foreground with error handling
CMD ["sh", "-c", "nginx -t && exec nginx -g 'daemon off;'"]
