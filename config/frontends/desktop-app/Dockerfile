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
# Stage 2: Runtime Stage - Ubuntu mit VNC + noVNC
# ===================================================================
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

# Installiere System-Dependencies
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    xvfb \
    x11vnc \
    python3 \
    python3-numpy \
    xfce4 \
    xfce4-goodies \
    curl \
    wget \
    unzip \
    supervisor \
    net-tools \
    git \
    # WICHTIG: GUI Libraries für Java/Compose
    libgl1-mesa-glx \
    libgl1-mesa-dri \
    libgtk-3-0 \
    libasound2 \
    libxcursor1 \
    libxi6 \
    libxtst6 \
    libxrandr2 \
    && apt-get remove -y xfce4-power-manager \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*

# Manuelle Installation von noVNC und Websockify
# Wir klonen Websockify an den Ort, wo noVNC es erwartet
RUN mkdir -p /usr/share/novnc \
    && git clone --depth 1 https://github.com/novnc/noVNC.git /usr/share/novnc \
    && git clone --depth 1 https://github.com/novnc/websockify /usr/share/novnc/utils/websockify \
    && ln -s /usr/share/novnc/vnc.html /usr/share/novnc/index.html

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
