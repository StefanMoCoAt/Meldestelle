# Setup Guide: Infrastructure Services (Minisforum MS-R1)

**Status:** DRAFT
**Date:** 2026-02-07
**Context:** Host OS (Debian 12 ARM64) ist vorbereitet, Incus läuft mit Macvlan.

Dieses Dokument beschreibt die Installation der beiden Haupt-Komponenten:
1.  **infra-gitea:** Ein leichtgewichtiger Git-Server (LXC Container).
2.  **docker-host-prod:** Die Produktions-Umgebung für die Meldestelle-App (Incus VM).

---

## 1. Gitea Container (`infra-gitea`)

Wir nutzen einen LXC-Container, da Gitea sehr ressourcensparend ist.

### 1.1 Container starten

```bash
# Starten des Containers
incus launch images:debian/12 infra-gitea

# Warten bis er eine IP hat
incus list infra-gitea
```

### 1.2 Gitea Installation

Wir installieren Gitea manuell als Binary, um die volle Kontrolle zu haben.

1.  **In den Container einloggen:**
    ```bash
    incus shell infra-gitea
    ```

2.  **Abhängigkeiten installieren (im Container):**
    ```bash
    apt update && apt install -y git wget gnupg2
    ```

3.  **Git-User anlegen:**
    ```bash
    adduser --system --shell /bin/bash --gecos 'Git Version Control' --group --disabled-password git
    ```

4.  **Gitea Binary herunterladen (ARM64):**
    ```bash
    wget -O /usr/local/bin/gitea https://dl.gitea.com/gitea/1.21.4/gitea-1.21.4-linux-arm64
    chmod +x /usr/local/bin/gitea
    ```

5.  **Verzeichnisse erstellen:**
    ```bash
    mkdir -p /var/lib/gitea/{custom,data,log}
    chown -R git:git /var/lib/gitea/
    chmod -R 750 /var/lib/gitea/
    mkdir /etc/gitea
    chown root:git /etc/gitea
    chmod 770 /etc/gitea
    ```

6.  **Systemd Service anlegen:**
    Erstelle die Datei `/etc/systemd/system/gitea.service`:
    *(Inhalt siehe unten)*

    ```ini
    [Unit]
    Description=Gitea (Git with a cup of tea)
    After=syslog.target
    After=network.target

    [Service]
    RestartSec=2s
    Type=simple
    User=git
    Group=git
    WorkingDirectory=/var/lib/gitea/
    ExecStart=/usr/local/bin/gitea web --config /etc/gitea/app.ini
    Restart=always
    Environment=USER=git HOME=/home/git GITEA_WORK_DIR=/var/lib/gitea

    [Install]
    WantedBy=multi-user.target
    ```

7.  **Service starten:**
    ```bash
    systemctl enable --now gitea
    systemctl status gitea
    ```

8.  **Setup abschließen:**
    Öffne im Browser: `http://<IP-VON-INFRA-GITEA>:3000`

---

## 2. Docker Host VM (`docker-host-prod`)

Wir nutzen eine **VM** (Virtual Machine) statt eines Containers für Docker. Das bietet bessere Isolation und vermeidet Probleme mit "Docker-in-LXC" (Nesting, OverlayFS).

### 2.1 VM starten

*Hinweis: VMs brauchen etwas länger zum Starten als Container.*

```bash
# Starten der VM (mit 4 vCPUs und 8GB RAM als Startwert)
incus launch images:debian/12 docker-host-prod --vm -c limits.cpu=4 -c limits.memory=8GiB

# Warten bis sie eine IP hat (kann 1-2 Minuten dauern beim ersten Mal)
incus list docker-host-prod
```

### 2.2 Docker Installation

1.  **In die VM einloggen:**
    ```bash
    incus shell docker-host-prod
    ```

2.  **Docker installieren (Offizielles Skript):**
    ```bash
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
    ```

3.  **User-Rechte:**
    Da wir in der Incus-Shell als `root` sind, passt das erst mal. Für später (SSH-Zugriff) sollte man einen User anlegen.

4.  **Test:**
    ```bash
    docker run --rm hello-world
    ```

---

## 3. DNS / Erreichbarkeit (Optional aber empfohlen)

Da wir Macvlan nutzen, haben die Instanzen IPs aus dem Heimnetz (z.B. `10.0.0.x`).
Es empfiehlt sich, im Router (FritzBox) einzustellen:
*   "Diesem Netzwerkgerät immer die gleiche IPv4-Adresse zuweisen."
*   Ggf. lokale DNS-Namen vergeben (z.B. `gitea.local`, `docker.local`).
