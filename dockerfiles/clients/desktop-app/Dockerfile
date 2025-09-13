# Multi-stage build for Meldestelle Compose Desktop Application
# Builds Kotlin/JVM (Compose Desktop) client and serves via VNC with noVNC web interface

# ===================================================================
# CENTRALIZED BUILD ARGUMENTS
# Values sourced from docker/versions.toml and docker/build-args/
# ===================================================================
# Global arguments (docker/build-args/global.env)
ARG GRADLE_VERSION
ARG JAVA_VERSION
ARG BUILD_DATE
ARG VERSION

# Client-specific arguments (docker/build-args/clients.env)
ARG NODE_VERSION

# Desktop-specific arguments
ARG UBUNTU_VERSION=22.04

# ===================================================================
# Build Arguments for Client Configuration
# ===================================================================
ARG CLIENT_PATH=client
ARG CLIENT_MODULE=client

# ===================================================================
# Build Stage - Kotlin/JVM (Compose Desktop) Compilation
# ===================================================================
FROM gradle:${GRADLE_VERSION}-jdk${JAVA_VERSION} AS builder

ARG CLIENT_PATH=client
ARG CLIENT_MODULE=client

# Set working directory
WORKDIR /build

# Set build labels
LABEL service=desktop-app
LABEL stage=build

# Copy Gradle files first for better layer caching
COPY gradle/ gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY build.gradle.kts ./

# Copy version catalog
COPY gradle/libs.versions.toml gradle/libs.versions.toml

# Copy all source files needed for the build
# Core and platform modules (dependencies)
COPY core/ core/
COPY platform/ platform/

# Infrastructure modules (if needed)
COPY infrastructure/ infrastructure/

# Client modules
COPY client/ client/

# Copy any additional required directories
COPY temp/ temp/
COPY docs/ docs/

# Make Gradle wrapper executable
RUN chmod +x gradlew

# Build client application for JVM
# Create distribution package for desktop application
RUN echo "Building ${CLIENT_MODULE} module for JVM..." && \
    ./gradlew ${CLIENT_MODULE}:createDistributable --no-daemon --stacktrace --info

# ===================================================================
# Production Stage - VNC Desktop Environment
# ===================================================================
FROM ubuntu:${UBUNTU_VERSION} AS production

ARG CLIENT_PATH=client

# Set production labels
LABEL service="desktop-app" \
      environment="production" \
      description="Meldestelle Compose Desktop Application with VNC"

# Install system dependencies
RUN apt-get update && apt-get install -y \
    openjdk-21-jre-headless \
    xvfb \
    x11vnc \
    fluxbox \
    websockify \
    novnc \
    curl \
    wget \
    supervisor \
    && rm -rf /var/lib/apt/lists/*

# Create application user
RUN useradd -m -s /bin/bash appuser && \
    mkdir -p /home/appuser/.vnc

# Set up VNC
RUN mkdir -p /home/appuser/.vnc && \
    echo "meldestelle" | vncpasswd -f > /home/appuser/.vnc/passwd && \
    chmod 600 /home/appuser/.vnc/passwd && \
    chown -R appuser:appuser /home/appuser/.vnc

# Copy built application from builder stage
COPY --from=builder /build/${CLIENT_PATH}/build/compose/binaries/main/app/ /opt/meldestelle/

# Ensure launcher script has execution permissions
RUN chmod +x /opt/meldestelle/at.mocode/bin/at.mocode

# Create VNC startup script
RUN echo '#!/bin/bash' > /opt/start-vnc.sh && \
    echo 'export DISPLAY=:99' >> /opt/start-vnc.sh && \
    echo 'export VNC_PORT=5901' >> /opt/start-vnc.sh && \
    echo 'export NOVNC_PORT=6080' >> /opt/start-vnc.sh && \
    echo '' >> /opt/start-vnc.sh && \
    echo '# Start Xvfb' >> /opt/start-vnc.sh && \
    echo 'Xvfb :99 -screen 0 1024x768x16 &' >> /opt/start-vnc.sh && \
    echo 'sleep 2' >> /opt/start-vnc.sh && \
    echo '' >> /opt/start-vnc.sh && \
    echo '# Start window manager' >> /opt/start-vnc.sh && \
    echo 'fluxbox &' >> /opt/start-vnc.sh && \
    echo 'sleep 2' >> /opt/start-vnc.sh && \
    echo '' >> /opt/start-vnc.sh && \
    echo '# Start VNC server' >> /opt/start-vnc.sh && \
    echo 'x11vnc -display :99 -nopw -listen localhost -xkb -ncache 10 -ncache_cr -rfbport $VNC_PORT &' >> /opt/start-vnc.sh && \
    echo 'sleep 2' >> /opt/start-vnc.sh && \
    echo '' >> /opt/start-vnc.sh && \
    echo '# Start noVNC' >> /opt/start-vnc.sh && \
    echo 'websockify --web=/usr/share/novnc/ $NOVNC_PORT localhost:$VNC_PORT &' >> /opt/start-vnc.sh && \
    echo 'sleep 2' >> /opt/start-vnc.sh && \
    echo '' >> /opt/start-vnc.sh && \
    echo '# Start the Meldestelle application' >> /opt/start-vnc.sh && \
    echo 'cd /opt/meldestelle' >> /opt/start-vnc.sh && \
    echo 'exec ./at.mocode/bin/at.mocode' >> /opt/start-vnc.sh

RUN chmod +x /opt/start-vnc.sh

# Create supervisor configuration
RUN echo '[supervisord]' > /etc/supervisor/conf.d/meldestelle.conf && \
    echo 'nodaemon=true' >> /etc/supervisor/conf.d/meldestelle.conf && \
    echo 'user=root' >> /etc/supervisor/conf.d/meldestelle.conf && \
    echo '' >> /etc/supervisor/conf.d/meldestelle.conf && \
    echo '[program:vnc-app]' >> /etc/supervisor/conf.d/meldestelle.conf && \
    echo 'command=/opt/start-vnc.sh' >> /etc/supervisor/conf.d/meldestelle.conf && \
    echo 'user=appuser' >> /etc/supervisor/conf.d/meldestelle.conf && \
    echo 'autorestart=true' >> /etc/supervisor/conf.d/meldestelle.conf && \
    echo 'stdout_logfile=/var/log/meldestelle.log' >> /etc/supervisor/conf.d/meldestelle.conf && \
    echo 'stderr_logfile=/var/log/meldestelle_error.log' >> /etc/supervisor/conf.d/meldestelle.conf && \
    echo 'environment=HOME="/home/appuser",USER="appuser"' >> /etc/supervisor/conf.d/meldestelle.conf

# Create health check script
RUN echo '#!/bin/bash' > /opt/health-check.sh && \
    echo '# Check if noVNC is responding' >> /opt/health-check.sh && \
    echo 'curl -f http://localhost:6080/vnc.html > /dev/null 2>&1' >> /opt/health-check.sh && \
    echo 'exit $?' >> /opt/health-check.sh

RUN chmod +x /opt/health-check.sh

# Switch to application user for file permissions
USER appuser

# Set environment variables
ENV DISPLAY=:99
ENV VNC_PORT=5901
ENV NOVNC_PORT=6080

# Expose ports
EXPOSE 6080 5901

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD /opt/health-check.sh

# Switch back to root to start supervisor
USER root

# Start supervisor which manages all services
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/supervisord.conf"]
