# Multi-stage build for Meldestelle Compose for Web Application
# Builds Kotlin/JS (Compose for Web) client and serves via Nginx

# ===================================================================
# Arguments (can be overridden during build)
# ===================================================================
ARG JVM_VERSION=21
ARG GRADLE_VERSION=8.10
ARG NODE_VERSION=18
ARG NGINX_VERSION=1.25-alpine

# ===================================================================
# Build Arguments for Client Configuration
# ===================================================================
ARG CLIENT_PATH=client/web-app
ARG CLIENT_MODULE=client:web-app

# ===================================================================
# Build Stage - Kotlin/JS (Compose for Web) Compilation
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JVM_VERSION} AS builder

ARG CLIENT_PATH=client/web-app
ARG CLIENT_MODULE=client:web-app

# Set working directory
WORKDIR /build

# Set build labels
LABEL service=web-app
LABEL stage=build

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

# Install Node.js for JavaScript toolchain
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_${NODE_VERSION}.x | bash - && \
    apt-get install -y nodejs

# Make Gradle wrapper executable
RUN chmod +x gradlew

# Build client application
# For Compose for Web projects, jsBrowserDistribution produces static assets
RUN echo "Building ${CLIENT_MODULE} module..." && \
    ./gradlew ${CLIENT_MODULE}:jsBrowserDistribution --no-daemon --stacktrace --info

# ===================================================================
# Production Stage - Nginx Static File Server
# ===================================================================
FROM nginx:${NGINX_VERSION} AS production

ARG CLIENT_PATH=client/web-app

# Set production labels
LABEL service="web-app" \
      environment="production" \
      description="Meldestelle Compose for Web Application"

# Create nginx user if not exists and set permissions
RUN addgroup -g 1001 -S nginx-group && \
    adduser -S -D -H -u 1001 -h /var/cache/nginx -s /sbin/nologin -G nginx-group -g nginx nginx-user

# Copy built distribution files from builder stage
COPY --from=builder /build/${CLIENT_PATH}/build/dist/js/productionExecutable/ /usr/share/nginx/html/
COPY --from=builder /build/${CLIENT_PATH}/src/jsMain/resources/ /usr/share/nginx/html/

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
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl --fail http://localhost:3000/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
