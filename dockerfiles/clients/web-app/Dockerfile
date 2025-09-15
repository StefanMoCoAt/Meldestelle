# ===================================================================
# Multi-Stage Dockerfile für Meldestelle Web-App (Kotlin/JS)
# ===================================================================

# ===================================================================
# Stage 1: Build Stage - Kotlin/JS kompilieren
# ===================================================================
FROM gradle:8-jdk21-alpine AS builder

WORKDIR /app

# Kopiere Gradle-Konfiguration und Wrapper
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY gradlew ./

# Kopiere alle notwendigen Module für Multi-Modul-Projekt
COPY client ./client
COPY core ./core
COPY platform ./platform
COPY infrastructure ./infrastructure
COPY temp ./temp
COPY docs ./docs

# Setze Gradle-Wrapper Berechtigung
RUN chmod +x ./gradlew

# Dependencies downloaden (für besseres Caching)
RUN ./gradlew :client:dependencies --no-configure-on-demand

# Kotlin/JS Web-App kompilieren
RUN ./gradlew :client:jsBrowserDistribution --no-configure-on-demand

# ===================================================================
# Stage 2: Runtime Stage - Nginx für Static Files + API Proxy
# ===================================================================
FROM nginx:1.25-alpine

# Installiere curl für Health-Checks
RUN apk add --no-cache curl

# Kopiere kompilierte Web-App von Build-Stage
COPY --from=builder /app/client/build/dist/js/productionExecutable/ /usr/share/nginx/html/

# Kopiere Nginx-Konfiguration
COPY dockerfiles/clients/web-app/nginx.conf /etc/nginx/nginx.conf

# Exponiere Port 4000 (statt Standard 80)
EXPOSE 4000

# Health-Check für Container
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:4000/ || exit 1

# Starte Nginx
CMD ["nginx", "-g", "daemon off;"]
