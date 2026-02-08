# Setup Guide: Host OS (Minisforum MS-R1)

**Status:** DRAFT
**Date:** 2026-02-07
**Target:** Pre-installed Debian 12 (Vendor OS)

Dieses Dokument beschreibt die Schritte, um das vorinstallierte Betriebssystem des Minisforum MS-R1 für den Einsatz als **Meldestelle Home-Server** vorzubereiten.

## 0. SSH Verbindung herstellen

Da der Server bereits im Netzwerk ist, verbinden wir uns per SSH.

### IP-Adresse finden
Wenn du die IP-Adresse des Servers nicht kennst:
1.  Schaue im Router (FritzBox o.ä.) nach einem Gerät namens `debian`, `minisforum` oder ähnlich.
2.  Oder schließe kurz Monitor & Tastatur an und tippe `ip a` ein. Suche nach `inet 192.168.x.x`.

### Verbinden (von deinem Arbeitsrechner)
Öffne ein Terminal (PowerShell oder Bash) auf deinem Laptop/PC:

```bash
# Syntax: ssh <user>@<ip-adresse>
# Der Standard-User bei Debian ist oft 'root' oder 'debian' oder 'user'.
# Das Passwort steht oft auf einem Zettel im Karton oder ist 'minisforum', 'password' oder leer.

ssh user@192.168.178.XX  # (Ersetze XX mit der echten IP)
```

*Falls der Login klappt, fahre mit Schritt 1 fort.*

---

## 1. Bestandsaufnahme & Update

Zuerst prüfen wir den Status des Systems und aktualisieren die Pakete.

```bash
# System-Infos anzeigen (Kernel, Architektur)
uname -a
cat /etc/debian_version
lscpu

# Prüfen, ob KVM (Virtualisierung) aktiv ist (Wichtig für Incus VMs!)
ls -l /dev/kvm
# Sollte crw-rw---- root kvm ... ausgeben.

# Paketquellen aktualisieren und System upgraden
sudo apt update && sudo apt full-upgrade -y

# Aufräumen
sudo apt autoremove -y
```

## 2. Basis-Tools installieren

Wir benötigen einige Standard-Tools für die weitere Verwaltung.

```bash
sudo apt install -y curl wget git htop vim nano ufw net-tools dnsutils
```

## 3. Sicherheit (Hardening)

Da der Server im Netzwerk hängt, sichern wir den Zugang ab.

### 3.1 Neuer Admin-User (falls noch nicht geschehen)
Vermeide die Nutzung von `root` oder Standard-Usern wie `admin`/`user`.

```bash
# Ersetze 'meldestelle-admin' mit deinem Wunschnamen
sudo adduser meldestelle-admin
sudo usermod -aG sudo meldestelle-admin
```

### 3.2 SSH Absichern
*Hinweis: Führe dies erst aus, wenn du dich mit dem neuen User erfolgreich eingeloggt hast!*

Editiere `/etc/ssh/sshd_config`:
```ssh
PermitRootLogin no
PasswordAuthentication yes # Später auf 'no' setzen, wenn SSH-Keys eingerichtet sind!
```
Neustart: `sudo systemctl restart ssh`

### 3.3 Firewall (UFW)
Wir erlauben vorerst nur SSH und später HTTP/HTTPS.

```bash
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw enable
```

## 4. Virtualisierung: Incus Installation

Wir nutzen **Incus** (Community Fork von LXD) für Container und VMs. Da Debian 12 Incus nicht in den Standard-Repos hat, nutzen wir das Zabbly-Repository (Standard für Incus).

### 4.1 Repository hinzufügen

```bash
# Keyring Verzeichnis erstellen
sudo mkdir -p /etc/apt/keyrings/

# GPG Key herunterladen
sudo curl -fsSL https://pkgs.zabbly.com/key.asc -o /etc/apt/keyrings/zabbly.asc

# Repository hinzufügen
sudo sh -c 'cat <<EOF > /etc/apt/sources.list.d/zabbly-incus-stable.sources
Enabled: yes
Types: deb
URIs: https://pkgs.zabbly.com/incus/stable
Suites: $(. /etc/os-release && echo ${VERSION_CODENAME})
Components: main
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/zabbly.asc

EOF'
```

### 4.2 Installation

```bash
sudo apt update
sudo apt install -y incus
```

### 4.3 User zur Gruppe hinzufügen

Damit du `incus` ohne `sudo` nutzen kannst:

```bash
sudo usermod -aG incus-admin meldestelle-admin
newgrp incus-admin
```

### 4.4 Initialisierung (Mit Workaround für Vendor Kernel)

⚠️ **ACHTUNG:** Der Vendor-Kernel (`6.6.10-cix-build-generic`) scheint wichtige Firewall-Module (`nf_tables`) zu vermissen. Das Standard-Setup von Incus schlägt daher fehl.

Wir müssen die Netzwerk-Brücke während der Initialisierung **deaktivieren** und später manuell konfigurieren.

Führe `incus admin init` erneut aus und antworte wie folgt:

```text
Would you like to use clustering? (yes/no) [default=no]: no
Do you want to configure a new storage pool? (yes/no) [default=yes]: yes
Name of the new storage pool [default=default]: default
Name of the storage backend to use (dir, truenas) [default=dir]: dir
Where should this storage pool store its data? [default=/var/lib/incus/storage-pools/default]: (Enter drücken)
Would you like to create a new local network bridge? (yes/no) [default=yes]: no  <-- WICHTIG!
Would you like the server to be available over the network? (yes/no) [default=no]: no
Would you like stale cached images to be updated automatically? (yes/no) [default=yes]: yes
Would you like a YAML "init" preseed to be printed? (yes/no) [default=no]: no
```

### 4.5 Netzwerk Konfiguration (Macvlan)

Da wir keine Bridge erstellen können, nutzen wir **Macvlan**. Das bedeutet, jeder Container bekommt eine eigene IP-Adresse direkt von deinem Router (FritzBox). Das ist für einen Home-Server oft sogar praktischer.

1.  **Kernel-Modul laden (WICHTIG):**
    Der Vendor-Kernel lädt `macvlan` nicht automatisch.
    ```bash
    sudo modprobe macvlan
    echo "macvlan" | sudo tee -a /etc/modules
    ```

2.  **Netzwerk-Interface finden:**
    Führe `ip -br a` aus. Suche das Interface mit deiner IP (z.B. `eth0`, `enP4p1s0` o.ä.). Ignoriere `lo`, `docker0` etc.

3.  **Profil anpassen:**
    Ersetze `<INTERFACE>` im folgenden Befehl mit dem Namen aus Schritt 2 (z.B. `eth0`).

    ```bash
    # Füge das Netzwerk-Device zum default Profil hinzu
    incus profile device add default eth0 nic nictype=macvlan parent=<INTERFACE>
    ```

4.  **Test:**
    Starte einen Test-Container:
    ```bash
    incus launch images:debian/12 test-container
    incus list
    # Sollte eine IP aus deinem Heimnetz (192.168.178.xxx) haben.
    ```

## 5. Nächste Schritte

Nachdem der Host vorbereitet ist, werden wir gemäß Roadmap folgende Instanzen erzeugen:

1.  **Gitea Container:** `incus launch images:debian/12 infra-gitea`
2.  **Docker Host VM:** `incus launch images:debian/12 docker-host-prod --vm`

Bitte melde zurück, wenn Schritt 1-4 erfolgreich waren oder ob Fehler (z.B. fehlendes `/dev/kvm`) aufgetreten sind.
