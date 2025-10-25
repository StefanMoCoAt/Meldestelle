---
owner: ops-team
status: active
review_cycle: 180d
last_reviewed: 2025-10-15
summary: Anleitung zur Installation und Konfiguration des Nginx Reverse Proxys auf dem Proxmox-Host für die Meldestelle-Services.
---

# How-To: Proxmox/Nginx Reverse Proxy deployen

Diese Anleitung beschreibt die Einrichtung des Nginx Reverse Proxys auf dem Proxmox-Host. Die Beispielkonfiguration liegt im Repository und wird unverändert übernommen.

- Beispielkonfiguration: proxmox-nginx/meldestelle.conf

## Voraussetzungen

- Proxmox-Host mit root-/sudo-Zugang
- Installiertes Nginx (`apt install nginx`)
- Lokale Container-Services laufen auf dem Host und sind über `localhost` erreichbar (Web 4000, Gateway 8081, VNC 6080)

## Schritte

1) Konfigurationsdatei auf den Host kopieren

```bash
sudo cp docs/proxmox-nginx/meldestelle.conf /etc/nginx/sites-available/
```

2) Site aktivieren (Symlink anlegen)

```bash
sudo ln -s /etc/nginx/sites-available/meldestelle.conf /etc/nginx/sites-enabled/
```

3) Nginx Konfiguration testen und neu laden

```bash
sudo nginx -t && sudo systemctl reload nginx
```

4) DNS konfigurieren (Beispiele)

- meldestelle.yourdomain.com → öffentliche IP deines Proxmox-Hosts
- api.meldestelle.yourdomain.com → öffentliche IP deines Proxmox-Hosts
- vnc.meldestelle.yourdomain.com → öffentliche IP deines Proxmox-Hosts

5) Health-Checks

```bash
curl -i http://api.meldestelle.yourdomain.com/actuator/health
curl -i http://meldestelle.yourdomain.com/health
```

## HTTPS (optional)

In der Beispielkonfiguration sind HTTPS-Serverblöcke und HTTP→HTTPS Redirects als Kommentar enthalten. Aktiviere diese Blöcke, wenn du Zertifikate (Let's Encrypt/Cloudflare) eingerichtet hast. Datei: proxmox-nginx/meldestelle.conf

## Fehlerbehebung

- 502 Bad Gateway: Zielcontainer läuft nicht oder Port falsch → Dienste starten (`make full-up`) und Ports prüfen.
- CORS-Fehler: API ausschließlich über die `api.*`-Domain aufrufen; Web-App über `meldestelle.*`.
- Änderungen ohne Effekt: `nginx -t` ausführen und `systemctl reload nginx`.
