# ===================================================================
# Multi-Stage Dockerfile für Meldestelle Web-App (Kotlin/JS)
# ===================================================================

# ===================================================================
# Stage 1: Build Stage - Kotlin/JS kompilieren
# ===================================================================
# Build args (build-time only)
ARG GRADLE_VERSION
ARG JAVA_VERSION
ARG NODE_VERSION
ARG NGINX_IMAGE_TAG=1.28.0-alpine
# Toggle build profile: dev (default) or prod
ARG WEB_BUILD_PROFILE=dev
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION} AS builder

# Install Node.js (version aligned with versions.toml)
# Derive major version from NODE_VERSION (e.g., 22.21.0 -> setup_22.x)
RUN apt-get update && apt-get install -y curl && \
    NODE_MAJOR=$(echo "$NODE_VERSION" | cut -d. -f1) && \
    curl -fsSL "https://deb.nodesource.com/setup_${NODE_MAJOR}.x" | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Kopiere Gradle-Konfiguration und Wrapper
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY gradlew ./

# Kopiere alle notwendigen Module für Multi-Modul-Projekt
COPY frontend ./frontend
COPY backend ./backend
COPY core ./core
COPY domains ./domains
COPY platform ./platform
COPY docs ./docs

# Setze Gradle-Wrapper Berechtigung
RUN chmod +x ./gradlew

# Dependencies downloaden (für besseres Caching)
RUN ./gradlew :frontend:shells:meldestelle-portal:dependencies --no-configure-on-demand

# Kotlin/JS Web-App kompilieren (Profil wählbar über WEB_BUILD_PROFILE)
# - dev  → jsBrowserDevelopmentExecutable (schneller, Source Maps)
# - prod → jsBrowserDistribution (minifiziert, optimiert)
RUN if [ "$WEB_BUILD_PROFILE" = "prod" ]; then \
      ./gradlew :frontend:shells:meldestelle-portal:jsBrowserDistribution --no-configure-on-demand -Pproduction=true; \
      mkdir -p /app/web-dist && cp -r frontend/shells/meldestelle-portal/build/dist/js/productionExecutable/* /app/web-dist/; \
    else \
      ./gradlew :frontend:shells:meldestelle-portal:jsBrowserDevelopmentExecutable --no-configure-on-demand; \
      mkdir -p /app/web-dist && cp -r frontend/shells/meldestelle-portal/build/dist/js/developmentExecutable/* /app/web-dist/; \
    fi

# ===================================================================
# Stage 2: Runtime Stage - Nginx für Static Files + API Proxy
# ===================================================================
# Build arg controls runtime base image tag (declared globally to allow usage in FROM)
FROM nginx:${NGINX_IMAGE_TAG}

# Installiere curl für Health-Checks
RUN apk add --no-cache curl

# Kopiere kompilierte Web-App von Build-Stage (vereinheitlichtes Ausgabeverzeichnis)
COPY --from=builder /app/web-dist/ /usr/share/nginx/html/

# Kopiere Nginx-Konfiguration
COPY dockerfiles/clients/web-app/nginx.conf /etc/nginx/nginx.conf

# Exponiere Port 4000 (statt Standard 80)
EXPOSE 4000

# Downloads (Platzhalter) ausliefern lassen
COPY dockerfiles/clients/web-app/downloads/ /usr/share/nginx/html/downloads/

# Health-Check für Container
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:4000/ || exit 1

# Starte Nginx
CMD ["nginx", "-g", "daemon off;"]
