# Stage 1: Build der KMP/Wasm Frontend App
FROM gradle:8.14-jdk21 AS build
LABEL authors="Stefan Mogeritsch"
LABEL org.opencontainers.image.source="https://github.com/mogeritsch/WsMeldestelle"
LABEL org.opencontainers.image.description="Frontend application for Meldestelle"
LABEL org.opencontainers.image.version="1.0.0"

WORKDIR /app

# Copy only the files needed for dependency resolution first
# This improves layer caching - these layers won't change unless dependencies change
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY gradlew gradlew.bat ./

# No need for special Node.js compatibility packages in Debian-based image

# Download dependencies - this will be cached if dependencies don't change
RUN gradle dependencies --no-daemon

# Now copy the source code
COPY shared ./shared
COPY composeApp ./composeApp

# Ensure Gradle wrapper is executable
RUN chmod +x ./gradlew

# Build the application
RUN ./gradlew :composeApp:wasmJsBrowserDistribution --no-daemon && \
    # Clean up to reduce image size
    rm -rf ~/.gradle/caches

# Stage 2: Servieren der statischen Dateien mit Nginx
FROM nginx:1.28-alpine
LABEL authors="Stefan Mogeritsch"
LABEL org.opencontainers.image.source="https://github.com/mogeritsch/WsMeldestelle"
LABEL org.opencontainers.image.description="Frontend application for Meldestelle"
LABEL org.opencontainers.image.version="1.0.0"

# Create a non-root user for running Nginx
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy custom Nginx configuration for SPA routing
COPY composeApp/nginx.conf /etc/nginx/conf.d/default.conf

# Copy the built artifacts from the build stage
COPY --from=build /app/composeApp/build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html/

# Set proper permissions
RUN chown -R appuser:appgroup /var/cache/nginx && \
    chown -R appuser:appgroup /var/log/nginx && \
    chown -R appuser:appgroup /etc/nginx/conf.d && \
    chown -R appuser:appgroup /usr/share/nginx/html && \
    touch /var/run/nginx.pid && \
    chown -R appuser:appgroup /var/run/nginx.pid

# Switch to non-root user
USER appuser

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -q --spider http://localhost:80/ || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
