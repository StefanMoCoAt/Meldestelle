# ---------- Stage 1: Build ----------
FROM gradle:8.14-jdk-21-and-24-alpine AS build

# Set working directory inside container
WORKDIR /app

# Install Node.js LTS explicitly
RUN apk add --no-cache --update nodejs npm

# Copy necessary files for the build
COPY settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle/
COPY shared ./shared/
COPY composeApp ./composeApp/

# Build the WebAssembly JavaScript application with caching
WORKDIR /app
RUN --mount=type=cache,target=/root/.gradle,id=frontend-gradle-cache \
    gradle :composeApp:wasmJsBrowserDistribution --no-daemon \
    -Dkotlin.js.nodejs.download=false \
    -Dkotlin.js.nodeCommand=/usr/bin/node \
    --exclude-task kotlinNpmInstall \
    --exclude-task kotlinStoreYarnLock \
    --exclude-task wasmJsBrowserProductionWebpack \
    --info

# ---------- Stage 2: Runtime ----------
FROM nginx:1.25.3-alpine AS runtime

# Add metadata labels
LABEL maintainer="MoCode"
LABEL description="Frontend for Meldestelle application"
LABEL version="1.0"

# Create a non-root user to run nginx
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy nginx configuration
COPY --from=build /app/composeApp/nginx.conf /etc/nginx/conf.d/default.conf

# Copy the built WebAssembly JavaScript application
COPY --from=build /app/composeApp/build/compileSync/wasmJs/main/developmentExecutable/kotlin/* /usr/share/nginx/html/
COPY --from=build /app/composeApp/build/processedResources/wasmJs/main/* /usr/share/nginx/html/

# Set proper permissions
RUN chown -R appuser:appgroup /var/cache/nginx /var/log/nginx /etc/nginx/conf.d \
    && chmod -R 755 /usr/share/nginx/html \
    && touch /var/run/nginx.pid \
    && chown appuser:appgroup /var/run/nginx.pid

# Switch to non-root user
USER appuser

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget -q --spider http://localhost:80/ || exit 1

# Expose the port the app runs on
EXPOSE 80

# Run Nginx in the foreground
CMD ["nginx", "-g", "daemon off;"]
