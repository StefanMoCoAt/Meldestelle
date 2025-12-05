# ===================================================================
# Multi-Stage Dockerfile für Meldestelle Desktop-App (VNC)
# ===================================================================

# 1. Build Stage (Debian-basiert für Stabilität bei Desktop-Builds)
FROM gradle:8-jdk21 AS builder

WORKDIR /app

# Copy Configs
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/ gradle/
COPY gradlew ./

# Fix Permissions
RUN chmod +x gradlew

# Copy Sources (Struktur wie im Web-App Fix)
COPY platform/ platform/
COPY core/ core/
COPY backend/ backend/
COPY frontend/ frontend/
COPY docs/ docs/
# Falls du 'domains' oder andere Ordner hast, die in settings.gradle.kts stehen:
# COPY domains/ domains/

# Dependencies laden
RUN ./gradlew :frontend:shells:meldestelle-portal:dependencies --no-daemon

# Desktop-App Distribution erstellen
# Wir nutzen 'packageDistributionForCurrentOS' oder 'createDistributable'
RUN ./gradlew :frontend:shells:meldestelle-portal:createDistributable --no-daemon

# ===================================================================
# 2. Runtime Stage - Ubuntu (Notwendig für GUI/X11 Libraries)
# ===================================================================
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

# Installiere X11, VNC, Window Manager und Libraries für Compose Multiplatform
# Compose braucht oft libgl1-mesa-glx, libgtk-3-0, libasound2 etc.
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
    net-tools \
    libgl1-mesa-glx \
    libgtk-3-0 \
    libasound2 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Kopiere Build-Ergebnis
# HINWEIS: Der Pfad muss exakt stimmen. Compose Gradle Plugin Output ist oft verschachtelt.
# Wir kopieren den Inhalt nach /app/desktop-app
COPY --from=builder /app/frontend/shells/meldestelle-portal/build/compose/binaries/main/app/ /app/desktop-app/

# Scripts (Achte darauf, dass die Pfade im Host stimmen!)
COPY config/frontends/desktop-app/entrypoint.sh /entrypoint.sh
COPY config/frontends/desktop-app/health-check.sh /opt/health-check.sh
# Wir nutzen vorerst dein Entrypoint-Script, Supervisor Config ist optional wenn Script alles macht
# COPY config/frontends/desktop-app/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

RUN chmod +x /entrypoint.sh /opt/health-check.sh

# User Setup
RUN useradd -m -s /bin/bash vncuser && \
    mkdir -p /home/vncuser/.vnc && \
    chown -R vncuser:vncuser /home/vncuser && \
    chown -R vncuser:vncuser /app

EXPOSE 5901 6080

ENV DISPLAY=:99 \
    VNC_PORT=5901 \
    NOVNC_PORT=6080 \
    API_BASE_URL="http://api-gateway:8081"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD /opt/health-check.sh

USER vncuser

ENTRYPOINT ["/entrypoint.sh"]
