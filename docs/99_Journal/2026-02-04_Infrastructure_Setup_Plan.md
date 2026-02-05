# 🏗️ Journal: Infrastructure Setup & CI/CD Planning

**Datum:** 04.02.2026
**Autor:** DevOps Engineer & Curator (AI)
**Status:** 🚧 In Progress (Paused due to technical issues)

## Zusammenfassung
Nach der erfolgreichen Verifikation des `ping-service` wurde mit der Planung und Einrichtung der CI/CD-Infrastruktur begonnen. Die Entscheidung fiel auf eine **Self-Hosted Lösung** (Gitea + Gitea Runner) auf dem vorhandenen Proxmox-Server, angebunden via **Cloudflare Tunnel** für sicheren Zugriff ohne Portfreigaben.

## 1. Cloudflare Bereinigung (Erledigt ✅)
Ziel war es, die Domain `mo-code.at` für den Tunnel vorzubereiten, ohne den Mail-Empfang zu stören.

**Durchgeführte Schritte:**
1.  **Login:** Cloudflare Dashboard -> Domain `mo-code.at` -> DNS -> Records.
2.  **Gelöscht:**
    *   `A | mo-code.at | 81.19.145.155`
    *   `A | www | 81.19.145.155`
    *   `A | ftp | 81.19.145.155`
3.  **Behalten & Korrigiert (Mail):**
    *   `A | mail | 81.19.149.91` -> **Proxy Status auf "DNS Only" (Grau) gesetzt.**
    *   `CNAME | imap | imap.world4you.com` -> **Proxy Status auf "DNS Only" (Grau) gesetzt.**
    *   `MX` und `TXT` Records wurden unverändert gelassen.

---

## 2. Proxmox Docker-Host Setup (Anleitung)

Diese Anleitung dient zur Wiederherstellung/Neuinstallation der VM, falls Probleme auftreten.

### Vorbereitung: ISO Download
1.  Proxmox GUI -> `local` Storage -> `ISO Images`.
2.  **Download from URL:** `https://releases.ubuntu.com/24.04.1/ubuntu-24.04.1-live-server-amd64.iso`
3.  Warten bis Download fertig.

### Schritt A: VM Erstellen (Klick-für-Klick)
1.  **Create VM** (Button oben rechts).
2.  **General:**
    *   Name: `docker-host`
    *   VM ID: (Standard lassen, z.B. 100)
3.  **OS:**
    *   ISO image: `ubuntu-24.04.1...iso`
    *   Type: Linux / 6.x - 2.6 Kernel
4.  **System:**
    *   Graphics/Machine/BIOS: Standard lassen.
    *   **Qemu Agent:** ✅ Aktivieren (Häkchen setzen).
5.  **Disks:**
    *   Storage: `local-lvm`
    *   Disk size: **100 GiB**
    *   **SSD emulation:** ✅ Aktivieren.
    *   **Discard:** ✅ Aktivieren.
6.  **CPU:**
    *   Sockets: 1
    *   Cores: **4**
    *   Type: **host** (Wichtig für Performance!).
7.  **Memory:**
    *   Memory: **8192** (8 GB).
    *   Ballooning: ✅ Aktivieren.
8.  **Network:**
    *   Bridge: `vmbr0`
    *   Model: `VirtIO`
9.  **Confirm** -> Finish.

### Schritt B: Ubuntu Installation
1.  VM starten -> **Console**.
2.  Sprache: English.
3.  Installer Update: "Update to the new installer" (falls gefragt).
4.  Keyboard: German.
5.  **Base:** Ubuntu Server (minimized optional, Standard empfohlen).
6.  **Network:** DHCP lassen.
7.  **Storage:** "Use an entire disk" -> Standard lassen -> Done.
8.  **Profile:**
    *   Name: `Stefan`
    *   Server name: `docker-host`
    *   Username: `stefan`
    *   Password: (Merken!)
9.  **SSH Setup:** **[X] Install OpenSSH server** (Mit Leertaste auswählen).
10. **Snaps:** **NICHTS** auswählen (kein Docker, kein MicroK8s).
11. Installieren & Reboot.

### Schritt C: Docker Installation (via Terminal/SSH)
Verbinde dich von deinem PC aus: `ssh stefan@<VM-IP>`

```bash
# 1. System aktualisieren & QEMU Agent sicherstellen
sudo apt update && sudo apt upgrade -y
sudo apt install -y qemu-guest-agent
sudo systemctl enable --now qemu-guest-agent

# 2. Docker Installations-Script laden & ausführen
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 3. User zur Docker-Gruppe hinzufügen (Wichtig!)
sudo usermod -aG docker $USER

# 4. Neustart, damit Rechte greifen
sudo reboot
```

### Schritt D: Test
Nach dem Neustart wieder einloggen:
```bash
docker run hello-world
```
Sollte "Hello from Docker!" ausgeben.

---

## 3. Ausblick: Nächste Schritte (DevOps Stack)

Sobald der Docker-Host läuft, werden wir folgende `docker-compose.yml` (Entwurf) verwenden, um Gitea und Cloudflare Tunnel zu starten:

```yaml
services:
  gitea:
    image: gitea/gitea:latest
    container_name: gitea
    environment:
      - USER_UID=1000
      - USER_GID=1000
    volumes:
      - ./gitea_data:/data
      - /etc/timezone:/etc/timezone:ro
      - /etc/localtime:/etc/localtime:ro
    ports:
      - "3000:3000"
      - "2222:22"
    restart: always

  tunnel:
    image: cloudflare/cloudflared:latest
    container_name: cloudflared
    restart: always
    command: tunnel run
    environment:
      - TUNNEL_TOKEN=<WIRD_GENERIERT>
```

## Troubleshooting Tipps (Installation bricht ab)
*   **ISO Check:** Prüfe die Checksumme des ISOs oder lade es neu herunter.
*   **RAM:** Versuche es testweise mit 4 GB statt 8 GB.
*   **Disk:** Prüfe im Proxmox Storage, ob wirklich genug Platz auf `local-lvm` frei ist.
*   **Console:** Beobachte die Fehlermeldung in der Proxmox-Konsole genau (oft I/O Errors).
