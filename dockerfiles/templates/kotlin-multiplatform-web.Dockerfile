# ===================================================================
# Multi-stage Dockerfile Template for Kotlin Multiplatform Web Client
# Features: Kotlin/JS compilation, Nginx serving, development support
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
LABEL maintainer="Meldestelle Development Team"

WORKDIR /workspace

# Gradle optimizations for Kotlin Multiplatform
ENV GRADLE_OPTS="-Dorg.gradle.caching=true \
                 -Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.configureondemand=true \
                 -Xmx3g"

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

# Download dependencies in a separate layer
RUN ./gradlew :${CLIENT_MODULE}:dependencies --no-daemon --info

# Build web application with production optimizations
RUN ./gradlew :${CLIENT_MODULE}:jsBrowserProductionWebpack --no-daemon --info

# ===================================================================
# Production Stage - Nginx serving
# ===================================================================
FROM nginx:${NGINX_VERSION} AS runtime

# Metadata
LABEL service="${CLIENT_NAME}" \
      version="1.0.0" \
      description="Kotlin Multiplatform Web Client for Meldestelle" \
      maintainer="Meldestelle Development Team"

# Security and system setup
RUN apk update && \
    apk upgrade && \
    apk add --no-cache curl && \
    rm -rf /var/cache/apk/*

# Remove default nginx content
RUN rm -rf /usr/share/nginx/html/*

# Copy built web application from builder stage
COPY --from=kotlin-builder /workspace/${CLIENT_PATH}/build/dist/ /usr/share/nginx/html/

# Copy nginx configuration if exists, otherwise use default
COPY ${CLIENT_PATH}/nginx.conf /etc/nginx/nginx.conf

# Create non-root user for nginx (if not using default nginx user)
RUN adduser -D -s /bin/sh -G www-data nginx-user

# Set proper permissions
RUN chown -R nginx:nginx /usr/share/nginx/html /var/cache/nginx /var/run /var/log/nginx

# Health check for web application
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:80/ || exit 1

# Expose HTTP port
EXPOSE 80

# Start nginx with proper signal handling
STOPSIGNAL SIGQUIT

# Run nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
