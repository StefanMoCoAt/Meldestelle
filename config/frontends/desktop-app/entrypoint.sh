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
# Standard-Passwort setzen, falls keines über ENV kommt
export VNC_PW=${VNC_PW:-meldestelle}

log "Environment:"
log "  DISPLAY: $DISPLAY"
log "  VNC_PORT: $VNC_PORT"
log "  NOVNC_PORT: $NOVNC_PORT"
log "  API_BASE_URL: $API_BASE_URL"

# 0. VNC Passwort generieren
log "Generating VNC password..."
mkdir -p /home/vncuser/.vnc
x11vnc -storepasswd "$VNC_PW" /home/vncuser/.vnc/passwd

# Erstelle .Xauthority wenn nicht vorhanden
touch /home/vncuser/.Xauthority

# 1. Starte X11 Virtual Display (Xvfb)
log "Starting Xvfb on display $DISPLAY..."
# rm -f /tmp/.X99-lock # Aufräumen falls Container neu gestartet wurde (optional)
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
# WICHTIG: -rfbauth statt -usepw nutzen, um interaktive Abfrage zu vermeiden
x11vnc -display $DISPLAY -forever -rfbauth /home/vncuser/.vnc/passwd -create -rfbport $VNC_PORT -shared -bg
VNC_PID=$!

# 4. Start noVNC (Websockify)
log "Starting noVNC on port $NOVNC_PORT..."

# Pfad-Korrektur für Ubuntu novnc
WEB_DIR="/usr/share/novnc"
if [ ! -d "$WEB_DIR" ]; then
    log "WARNING: $WEB_DIR not found! Searching..."
    WEB_DIR=$(find /usr/share -type d -name "novnc" | head -n 1)
fi

# Fix: Index File erstellen, falls es fehlt (Ubuntu hat oft nur vnc.html)
if [ -d "$WEB_DIR" ] && [ ! -f "$WEB_DIR/index.html" ]; then
    log "Fixing missing index.html in noVNC..."
    if [ -f "$WEB_DIR/vnc.html" ]; then
        ln -s "$WEB_DIR/vnc.html" "$WEB_DIR/index.html"
    elif [ -f "$WEB_DIR/vnc_lite.html" ]; then
        ln -s "$WEB_DIR/vnc_lite.html" "$WEB_DIR/index.html"
    fi
fi

log "Serving noVNC from: $WEB_DIR"

# Starte Websockify im Vordergrund, wenn es crasht, sehen wir es
websockify --web="$WEB_DIR" $NOVNC_PORT localhost:$VNC_PORT &

# 5. Warte bis Services bereit sind
sleep 5

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
    ls -R /app/desktop-app/
    # Wir beenden hier NICHT, damit man debuggen kann (VNC bleibt offen)
    log "Keeping VNC open for debugging..."
fi

if [ -n "$DESKTOP_APP" ]; then
    log "Found desktop app: $DESKTOP_APP"
    chmod +x "$DESKTOP_APP"
    # Starte Desktop-App
    ./"$DESKTOP_APP" &
    APP_PID=$!
fi

log "All services started successfully!"
log "VNC: vnc://localhost:$VNC_PORT (Password: $VNC_PW)"
log "noVNC: http://localhost:$NOVNC_PORT/vnc.html"

# Cleanup-Funktion
cleanup() {
    log "Shutting down services..."
    if [ -n "$APP_PID" ]; then kill $APP_PID 2>/dev/null || true; fi
    kill $NOVNC_PID 2>/dev/null || true
    # x11vnc läuft im Background (-bg), PID ist schwerer zu greifen, killall hilft:
    pkill x11vnc || true
    kill $XFCE_PID 2>/dev/null || true
    kill $XVFB_PID 2>/dev/null || true
    exit 0
}

# Signal-Handler
trap cleanup SIGTERM SIGINT

# Warten auf Prozesse (unendlich, damit Container nicht stirbt wenn App crasht)
# wait $APP_PID
tail -f /dev/null
