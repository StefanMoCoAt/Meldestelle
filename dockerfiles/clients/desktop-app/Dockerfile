# ===================================================================
# Multi-Stage Dockerfile für Meldestelle Desktop-App (VNC)
# ===================================================================

# ===================================================================
# Stage 1: Build Stage - Kotlin Desktop-App kompilieren
# ===================================================================
FROM gradle:8-jdk21-alpine AS builder

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

# Desktop-App kompilieren (createDistributable für native Distribution)
RUN ./gradlew :clients:app:createDistributable --no-configure-on-demand

# ===================================================================
# Stage 2: Runtime Stage - Ubuntu mit VNC + noVNC
# ===================================================================
FROM ubuntu:22.04

# Verhindere interaktive Installationen
ENV DEBIAN_FRONTEND=noninteractive

# Installiere System-Dependencies
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    xvfb \
    x11vnc \
    novnc \
    websockify \
    xfce4 \
    xfce4-goodies \
    curl \
    wget \
    unzip \
    supervisor \
    && rm -rf /var/lib/apt/lists/*

# Arbeitsverzeichnis
WORKDIR /app

# Kopiere kompilierte Desktop-App von Build-Stage
COPY --from=builder /app/clients/app/build/compose/binaries/main/desktop/ ./desktop-app/

# Kopiere Scripts
COPY dockerfiles/clients/desktop-app/entrypoint.sh /entrypoint.sh
COPY dockerfiles/clients/desktop-app/health-check.sh /opt/health-check.sh
COPY dockerfiles/clients/desktop-app/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Setze Permissions
RUN chmod +x /entrypoint.sh /opt/health-check.sh

# Erstelle VNC-User
RUN useradd -m -s /bin/bash vncuser && \
    mkdir -p /home/vncuser/.vnc && \
    chown -R vncuser:vncuser /home/vncuser && \
    chown -R vncuser:vncuser /app

# VNC und noVNC Ports
EXPOSE 5901 6080

# Environment Variables
ENV DISPLAY=:99
ENV VNC_PORT=5901
ENV NOVNC_PORT=6080
ENV API_BASE_URL=http://api-gateway:8081

# Health-Check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD /opt/health-check.sh

# User wechseln
USER vncuser

# Entrypoint
ENTRYPOINT ["/entrypoint.sh"]
