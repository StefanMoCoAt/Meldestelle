# ===================================================================
# Dockerfile for Meldestelle Web Application
# Based on kotlin-multiplatform-web template
# ===================================================================

# Build arguments
ARG GRADLE_VERSION=8.14
ARG JAVA_VERSION=21
ARG NGINX_VERSION=alpine

# ===================================================================
# Build Stage - Kotlin/JS Compilation
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS kotlin-builder

LABEL stage=kotlin-builder
LABEL service=web-app
LABEL maintainer="Meldestelle Development Team"

WORKDIR /workspace

# Gradle optimizations for Kotlin Multiplatform builds
ENV GRADLE_OPTS="-Dorg.gradle.caching=true \
                 -Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.configureondemand=true \
                 -Dorg.gradle.jvmargs=-Xmx3g \
                 -Dkotlin.compiler.execution.strategy=in-process"

# Copy build configuration files first for optimal Docker layer caching
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY gradle/ gradle/
COPY build.gradle.kts ./

# Copy platform and core dependencies (changes less frequently)
COPY platform/ platform/
COPY core/ core/

# Copy client modules in dependency order for optimal caching
COPY client/common-ui/ client/common-ui/
COPY client/web-app/ client/web-app/

# Download and cache dependencies in a separate layer
RUN ./gradlew :client:web-app:dependencies --no-daemon --info

# Build web application with production optimizations
RUN ./gradlew :client:web-app:jsBrowserProductionWebpack --no-daemon --info

# Verify build output
RUN ls -la /workspace/client/web-app/build/dist/ || (echo "Build failed - no dist directory found" && exit 1)

# ===================================================================
# Production Stage - Nginx serving
# ===================================================================
FROM nginx:${NGINX_VERSION} AS runtime

# Comprehensive metadata
LABEL service="web-app" \
      version="1.0.0" \
      description="Meldestelle Web Application - Kotlin Multiplatform Client" \
      maintainer="Meldestelle Development Team" \
      build.gradle.version="${GRADLE_VERSION}" \
      java.version="${JAVA_VERSION}" \
      nginx.version="${NGINX_VERSION}"

# Security and system setup
RUN apk update && \
    apk upgrade && \
    apk add --no-cache curl jq && \
    rm -rf /var/cache/apk/*

# Remove default nginx content and logs
RUN rm -rf /usr/share/nginx/html/* && \
    rm -f /var/log/nginx/*.log

# Copy built web application from builder stage
COPY --from=kotlin-builder /workspace/client/web-app/build/dist/ /usr/share/nginx/html/

# Copy optimized nginx configuration
COPY client/web-app/nginx.conf /etc/nginx/nginx.conf

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

# ===================================================================
# Build and Usage Instructions
# ===================================================================
# Build:
#   docker build -t meldestelle/web-app:latest -f client/web-app/Dockerfile .
#
# Run standalone:
#   docker run -p 3001:80 --name web-app meldestelle/web-app:latest
#
# Run with API backend:
#   docker run -p 3001:80 --link api-gateway:api-gateway --name web-app meldestelle/web-app:latest
#
# Access application:
#   http://localhost:3001
#   http://localhost:3001/health (health check)
#
# Development with hot-reload (use docker-compose.override.yml instead)
# ===================================================================
