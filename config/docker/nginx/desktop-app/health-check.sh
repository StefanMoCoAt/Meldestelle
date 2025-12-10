#!/bin/bash
# ===================================================================
# Health-Check-Script für Meldestelle Desktop-App (VNC)
# ===================================================================

set -e

# Environment-Variablen
VNC_PORT=${VNC_PORT:-5901}
NOVNC_PORT=${NOVNC_PORT:-6080}
DISPLAY=${DISPLAY:-:99}

# Logging-Funktion
log() {
    echo "[HEALTH] $1"
}

# 1. Überprüfe X11 Display
if ! xdpyinfo -display $DISPLAY >/dev/null 2>&1; then
    log "ERROR: X11 display $DISPLAY is not running"
    exit 1
fi
log "✓ X11 display $DISPLAY is running"

# 2. Überprüfe VNC Server
if ! netstat -ln | grep -q ":$VNC_PORT "; then
    log "ERROR: VNC server is not listening on port $VNC_PORT"
    exit 1
fi
log "✓ VNC server is running on port $VNC_PORT"

# 3. Überprüfe noVNC Web Interface
if ! curl -f -s "http://localhost:$NOVNC_PORT/" > /dev/null 2>&1; then
    log "ERROR: noVNC web interface is not responding on port $NOVNC_PORT"
    exit 1
fi
log "✓ noVNC web interface is running on port $NOVNC_PORT"

# 4. Überprüfe ob Desktop-App läuft (optional, da sie crashen könnte)
if pgrep -f "client" >/dev/null 2>&1; then
    log "✓ Desktop-App is running"
else
    log "⚠ Desktop-App is not running (may have crashed or not started yet)"
    # Nicht als Fehler behandeln, da die App crashen könnte
fi

# 5. Überprüfe Xvfb
if ! pgrep -f "Xvfb" >/dev/null 2>&1; then
    log "ERROR: Xvfb is not running"
    exit 1
fi
log "✓ Xvfb is running"

# 6. Überprüfe XFCE4
if ! pgrep -f "xfce4" >/dev/null 2>&1; then
    log "WARNING: XFCE4 desktop might not be running"
    # Nicht als kritischer Fehler behandeln
else
    log "✓ XFCE4 desktop environment is running"
fi

log "All critical services are healthy"
exit 0
