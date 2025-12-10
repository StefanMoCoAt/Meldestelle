#!/bin/bash
set -e

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"; }

log "Starting Meldestelle Desktop-App..."

export DISPLAY=${DISPLAY:-:99}
export VNC_PORT=${VNC_PORT:-5901}
export NOVNC_PORT=${NOVNC_PORT:-6080}
export VNC_PW=${VNC_PW:-meldestelle}
export API_BASE_URL=${API_BASE_URL:-http://api-gateway:8081}
# Grafik-Optimierungen für Docker (Software Rendering erzwingen)
export SKIKO_RENDER_API="SOFTWARE"
export LIBGL_ALWAYS_SOFTWARE=1
export MESA_GL_VERSION_OVERRIDE=3.3

# 0. VNC Passwort Setup
mkdir -p /home/vncuser/.vnc
x11vnc -storepasswd "$VNC_PW" /home/vncuser/.vnc/passwd

# 1. Start Xvfb
log "Starting Xvfb..."
rm -f /tmp/.X99-lock
Xvfb $DISPLAY -screen 0 1280x1024x24 -ac +extension GLX +render -noreset &
sleep 2

# 2. Start Window Manager
log "Starting XFCE4..."
startxfce4 &
sleep 2

# 3. Start VNC Server
log "Starting x11vnc on port $VNC_PORT..."
x11vnc -display $DISPLAY -forever -rfbauth /home/vncuser/.vnc/passwd -create -rfbport $VNC_PORT -shared -bg

# 4. Start noVNC (Fix für Websockify Pfad)
log "Starting noVNC on port $NOVNC_PORT..."
# Wir nutzen das mitgelieferte Proxy-Script, das ist robuster als websockify direkt aufzurufen
/usr/share/novnc/utils/novnc_proxy --vnc localhost:$VNC_PORT --listen $NOVNC_PORT &
NOVNC_PID=$!

# 5. Start Desktop App (Fix für Pfad-Probleme)
log "Searching for App binary..."

# Wir suchen rekursiv nach der Datei "Meldestelle" im Ordner "bin", die ausführbar ist
# Screenshot zeigte: /app/desktop-app/Meldestelle/bin/Meldestelle
APP_PATH=$(find /app/desktop-app -type f -path "*/bin/Meldestelle" | head -n 1)

if [ -z "$APP_PATH" ]; then
    # Fallback: Suche irgendeine Datei im bin Ordner, die KEIN Shellscript/Bat ist und executable
    APP_PATH=$(find /app/desktop-app -type f -path "*/bin/*" ! -name "*.sh" ! -name "*.bat" | head -n 1)
fi

if [ -f "$APP_PATH" ]; then
    log "🚀 Launching App from: $APP_PATH"
    chmod +x "$APP_PATH"
    "$APP_PATH" &
    APP_PID=$!
else
    log "❌ CRITICAL ERROR: App binary not found!"
    log "Files in /app/desktop-app:"
    ls -R /app/desktop-app
fi

log "Ready! Access: http://localhost:6080/vnc.html"

# Cleanup bei Stop
cleanup() {
    log "Stopping..."
    if [ -n "$APP_PID" ]; then kill $APP_PID 2>/dev/null || true; fi
    kill $NOVNC_PID 2>/dev/null || true
    pkill x11vnc || true
    exit 0
}
trap cleanup SIGTERM SIGINT

# Loggt Websockify Output in die Konsole
tail -f /dev/null
