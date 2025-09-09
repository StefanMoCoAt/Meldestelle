# ===================================================================
# Dockerfile for Meldestelle KobWeb Application
# Builds Kotlin/JS (KobWeb) client and serves via Nginx
# ===================================================================

# Build arguments
ARG GRADLE_VERSION=8.14
ARG JAVA_VERSION=21
ARG NGINX_VERSION=alpine
ARG NODE_VERSION=20.11.0

# Client-specific build arguments
ARG CLIENT_PATH=client/kobweb-app
ARG CLIENT_MODULE=client:kobweb-app

# ===================================================================
# Build Stage - Kotlin/JS (KobWeb) Compilation
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION}-alpine AS kotlin-builder

ARG CLIENT_PATH=client/kobweb-app
ARG CLIENT_MODULE=client:kobweb-app
ARG NODE_VERSION=20.11.0

LABEL stage=kotlin-builder
LABEL service=kobweb-app
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

# Gradle optimizations
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

# Copy build configuration first
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY gradle/ gradle/
COPY build.gradle.kts ./

# Copy platform and core dependencies
COPY platform/ platform/
COPY core/ core/

# Copy client modules in dependency order
COPY client/common-ui/ client/common-ui/
COPY ${CLIENT_PATH}/ ${CLIENT_PATH}/

# Clear npm cache and verify Node.js
RUN npm cache clean --force && \
    node --version && npm --version

# Warm up dependencies
RUN ./gradlew :${CLIENT_MODULE}:dependencies --no-daemon --info --stacktrace || true

# Build production bundle. For KobWeb projects, jsBrowserProductionWebpack produces static assets
RUN ./gradlew :${CLIENT_MODULE}:jsBrowserProductionWebpack --no-daemon --info --stacktrace

# Verify build output
RUN ls -la /workspace/${CLIENT_PATH}/build/dist/ || (echo "Build failed - no dist directory found" && exit 1)

# ===================================================================
# Production Stage - Nginx serving static assets
# ===================================================================
FROM nginx:${NGINX_VERSION} AS runtime

ARG CLIENT_PATH=client/kobweb-app
ARG GRADLE_VERSION=8.14
ARG JAVA_VERSION=21
ARG NGINX_VERSION=alpine

LABEL service="kobweb-app" \
      version="1.0.0" \
      description="Meldestelle KobWeb Application" \
      maintainer="Meldestelle Development Team" \
      build.gradle.version="${GRADLE_VERSION}" \
      java.version="${JAVA_VERSION}" \
      nginx.version="${NGINX_VERSION}"

RUN apk update && \
    apk upgrade && \
    apk add --no-cache curl && \
    rm -rf /var/cache/apk/*

# Clean default content
RUN rm -rf /usr/share/nginx/html/* && \
    rm -f /var/log/nginx/*.log

# Copy built web application
COPY --from=kotlin-builder /workspace/${CLIENT_PATH}/build/dist/ /usr/share/nginx/html/

# Provide a minimal nginx config if none in project (fallback)
# Try to copy project-specific nginx.conf if available
# We use a small trick: copy will fail if file missing, so we create a basic one beforehand
RUN printf "user nginx;\nworker_processes auto;\nerror_log /var/log/nginx/error.log warn;\npid /var/run/nginx.pid;\n\n events { worker_connections 1024; }\n http {\n  include /etc/nginx/mime.types;\n  default_type application/octet-stream;\n  sendfile on;\n  keepalive_timeout 65;\n  server {\n    listen 80;\n    server_name _;\n    root /usr/share/nginx/html;\n    location /health { return 200 'OK'; add_header Content-Type text/plain; }\n    location / { try_files $uri $uri/ /index.html; }\n  }\n }\n" > /etc/nginx/nginx.conf

# Permissions
RUN chown -R nginx:nginx /usr/share/nginx/html /var/cache/nginx /var/run /var/log/nginx && \
    chmod -R 755 /usr/share/nginx/html

USER nginx

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD curl -f http://localhost/health || exit 1

EXPOSE 80

STOPSIGNAL SIGQUIT
CMD ["sh", "-c", "nginx -t && exec nginx -g 'daemon off;'"]
