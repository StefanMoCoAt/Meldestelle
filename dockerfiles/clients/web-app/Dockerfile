# ===================================================================
# Multi-Stage Dockerfile für Meldestelle Web-App (Kotlin/JS)
# ===================================================================

# ===================================================================
# Stage 1: Build Stage - Kotlin/JS kompilieren
# ===================================================================
# Build args (build-time only)
ARG GRADLE_VERSION=9.1.0
ARG JAVA_VERSION=21
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION} AS builder

# Install Node.js and npm for Kotlin/JS builds (Ubuntu-based image has better Node.js compatibility)
RUN apt-get update && apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Kopiere Gradle-Konfiguration und Wrapper
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY gradlew ./

# Kopiere alle notwendigen Module für Multi-Modul-Projekt
COPY clients ./clients
COPY core ./core
COPY platform ./platform
COPY infrastructure ./infrastructure
COPY services ./services
COPY docs ./docs

# Setze Gradle-Wrapper Berechtigung
RUN chmod +x ./gradlew

# Dependencies downloaden (für besseres Caching)
RUN ./gradlew :clients:app:dependencies --no-configure-on-demand

# Kotlin/JS Web-App kompilieren (PRODUCTION Build)
RUN ./gradlew :clients:app:jsBrowserDistribution --no-configure-on-demand -Pproduction=true

# ===================================================================
# Stage 2: Runtime Stage - Nginx für Static Files + API Proxy
# ===================================================================
# Build arg controls runtime base image tag (build-time only)
ARG NGINX_IMAGE_TAG=1.28.0-alpine
FROM nginx:${NGINX_IMAGE_TAG}

# Installiere curl für Health-Checks
RUN apk add --no-cache curl

# Kopiere kompilierte Web-App von Build-Stage
COPY --from=builder /app/clients/app/build/dist/js/productionExecutable/ /usr/share/nginx/html/

# Kopiere Nginx-Konfiguration
COPY dockerfiles/clients/web-app/nginx.conf /etc/nginx/nginx.conf

# Exponiere Port 4000 (statt Standard 80)
EXPOSE 4000

# Health-Check für Container
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:4000/ || exit 1

# Starte Nginx
CMD ["nginx", "-g", "daemon off;"]
