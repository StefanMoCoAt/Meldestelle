# ===================================================================
# Multi-Stage Dockerfile für Meldestelle Desktop-App (VNC)
# ===================================================================

# 1. Build Stage (Debian-basiert für Stabilität bei Desktop-Builds)
FROM gradle:9.2.1-jdk-25-and-25-alpine AS builder

WORKDIR /app

# Copy Configs
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/ gradle/
COPY gradlew ./

# Fix Permissions
RUN chmod +x gradlew

# Copy Sources (Struktur wie im Web-App Fix)
# Minimale Monorepo-Struktur kopieren (nur was benötigt wird)
COPY platform/ platform/
COPY core/ core/
COPY backend/ backend/
COPY frontend/ frontend/
# Verträge/Contracts werden benötigt, da in settings.gradle.kts inkludiert
COPY contracts/ contracts/
COPY docs/ docs/
# Falls du 'domains' oder andere Ordner hast, die in settings.gradle.kts stehen:
# COPY domains/ domains/

# Dependencies laden
RUN ./gradlew :frontend:shells:meldestelle-portal:dependencies --no-daemon

# Desktop-App Distribution erstellen (robust): versuche createDistributable, fallback auf createRuntimeImage
RUN set -eu; \
    echo "[DESKTOP-BUILD] createDistributable"; \
    if ./gradlew :frontend:shells:meldestelle-portal:createDistributable --no-daemon; then \
      echo "[DESKTOP-BUILD] createDistributable OK"; \
    else \
      echo "[DESKTOP-BUILD] createDistributable failed, fallback to createRuntimeImage"; \
      ./gradlew :frontend:shells:meldestelle-portal:createRuntimeImage --no-daemon; \
    fi; \
    echo "[DESKTOP-BUILD] Collecting artifacts"; \
    DIST_BINARIES_DIR="/app/frontend/shells/meldestelle-portal/build/compose/binaries/main/app"; \
    DIST_RUNTIMES_DIR="/app/frontend/shells/meldestelle-portal/build/compose/runtimes/main/app"; \
    mkdir -p /tmp/desktop-dist; \
    if [ -d "$DIST_BINARIES_DIR" ]; then \
      cp -r "$DIST_BINARIES_DIR/"* /tmp/desktop-dist/; \
    fi; \
    if [ -d "$DIST_RUNTIMES_DIR" ]; then \
      cp -r "$DIST_RUNTIMES_DIR/"* /tmp/desktop-dist/; \
    fi; \
    echo "[DESKTOP-BUILD] Collected files:"; \
    ls -la /tmp/desktop-dist || true

# ===================================================================
# Stage 2: Runtime Stage - Ubuntu mit VNC + noVNC
# ===================================================================
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

# Installiere System-Dependencies
RUN apt-get update && apt-get install -y \
    openjdk-25-jdk \
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
    # WICHTIG: GUI Libraries für Java/Compose \
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
# Wir kopieren den konsolidierten Inhalt nach /app/desktop-app
RUN mkdir -p /app/desktop-app
COPY --from=builder /tmp/desktop-dist/ /app/desktop-app/

# Scripts (Achte darauf, dass die Pfade im Host stimmen!)
# Korrigierte Pfade: Skripte liegen unter config/docker/nginx/desktop-app
COPY config/docker/nginx/desktop-app/entrypoint.sh /entrypoint.sh
COPY config/docker/nginx/desktop-app/health-check.sh /opt/health-check.sh
# Optional: Supervisor-Config nur verwenden, wenn benötigt
# COPY config/docker/nginx/desktop-app/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

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
