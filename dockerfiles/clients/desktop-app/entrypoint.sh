#!/bin/bash
# ===================================================================
# Entrypoint-Script für Meldestelle Desktop-App (VNC)
# ===================================================================

set -e

# Logging-Funktion
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

log "Starting Meldestelle Desktop-App VNC Container..."

# Environment-Variablen setzen
export DISPLAY=${DISPLAY:-:99}
export VNC_PORT=${VNC_PORT:-5901}
export NOVNC_PORT=${NOVNC_PORT:-6080}
export API_BASE_URL=${API_BASE_URL:-http://api-gateway:8081}

log "Environment:"
log "  DISPLAY: $DISPLAY"
log "  VNC_PORT: $VNC_PORT"
log "  NOVNC_PORT: $NOVNC_PORT"
log "  API_BASE_URL: $API_BASE_URL"

# Erstelle .Xauthority wenn nicht vorhanden
touch /home/vncuser/.Xauthority

# 1. Starte X11 Virtual Display (Xvfb)
log "Starting Xvfb on display $DISPLAY..."
Xvfb $DISPLAY -screen 0 1280x1024x24 -ac +extension GLX +render -noreset &
XVFB_PID=$!

# Warte bis X11 bereit ist
sleep 3

# 2. Starte Desktop Environment (XFCE4)
log "Starting XFCE4 desktop environment..."
startxfce4 &
XFCE_PID=$!

# Warte bis Desktop bereit ist
sleep 5

# 3. Starte VNC Server
log "Starting VNC server on port $VNC_PORT..."
x11vnc -display $DISPLAY -forever -usepw -create -rfbport $VNC_PORT -nopw -shared -bg
VNC_PID=$!

# 4. Starte noVNC Web Interface
log "Starting noVNC web interface on port $NOVNC_PORT..."
websockify --web=/usr/share/novnc/ $NOVNC_PORT localhost:$VNC_PORT &
NOVNC_PID=$!

# 5. Warte bis Services bereit sind
sleep 10

# 6. Starte Desktop-App
log "Starting Meldestelle Desktop-App..."
cd /app/desktop-app
export API_BASE_URL=$API_BASE_URL

# Finde die ausführbare Datei
if [ -f "client/bin/client" ]; then
    DESKTOP_APP="client/bin/client"
elif [ -f "bin/client" ]; then
    DESKTOP_APP="bin/client"
elif [ -f "client" ]; then
    DESKTOP_APP="client"
else
    log "ERROR: Desktop-App executable not found!"
    log "Contents of /app/desktop-app:"
    ls -la /app/desktop-app/
    exit 1
fi

log "Found desktop app: $DESKTOP_APP"
chmod +x "$DESKTOP_APP"

# Starte Desktop-App
./"$DESKTOP_APP" &
APP_PID=$!

log "All services started successfully!"
log "VNC: vnc://localhost:$VNC_PORT"
log "noVNC: http://localhost:$NOVNC_PORT/vnc.html"

# Cleanup-Funktion
cleanup() {
    log "Shutting down services..."
    kill $APP_PID 2>/dev/null || true
    kill $NOVNC_PID 2>/dev/null || true
    kill $VNC_PID 2>/dev/null || true
    kill $XFCE_PID 2>/dev/null || true
    kill $XVFB_PID 2>/dev/null || true
    exit 0
}

# Signal-Handler
trap cleanup SIGTERM SIGINT

# Warten auf Prozesse
wait $APP_PID
