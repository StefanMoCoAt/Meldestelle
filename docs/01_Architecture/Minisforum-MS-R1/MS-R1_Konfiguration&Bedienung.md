# Technisches Referenzhandbuch: MS-R1 "Das Biest"

## 1. System-Übersicht & Architektur

Das System "MS-R1" (interner Codename "Das Biest") ist ein High-End ARM64-Server auf Basis des CIX P1 SoC. 

### Hardware-Spezifikationen

* **CPU:** ARM64 Architektur (CIX P1).
* **RAM:** 64 GB LPDDR.
* **Speicher:** NVMe-basiertes Root-Dateisystem.
* **OS:** Debian 12 (Bookworm).

## 2. Design-Entscheidungen & Ausschlussverfahren

### 2.1 Wahl des Betriebssystems

**Entscheidung:** Debian 12 (Bookworm).
**Begründung:** Maximale Stabilität für Infrastruktur-Dienste. 
**Ausschluss:** Ubuntu wurde aufgrund der Snap-Abhängigkeiten und Fedora aufgrund der kürzeren Release-Zyklen ausgeschlossen.

### 2.2 Kernel- & Firewall-Management

**Problem:** Der spezifische ARM-Kernel unterstützt bestimmte moderne `nftables`- und `iptables`-Module (wie `xt_CHECKSUM`) nicht.
**Lösung:** Umstellung auf `iptables-legacy`.
**Vorteil:** Ermöglicht die korrekte Paketverarbeitung für virtuelle Netzwerke, die sonst zu "Hängern" in der UI führen würden.

## 3. Benutzer- und Sicherheitskonzept

Um das System abzusichern, wurde eine strikte Bereinigung der Standard-User durchgeführt.

* **Administrative Accounts:** `grandmo` (Haupt-Admin) und `mastermo` (Redundanz).
* **System-Accounts:** `git` (UID 101) für Anwendungsdienste ohne Sudo-Privilegien.

## Benutzer-Management
Das System verfügt über eine strikte Trennung zwischen Administration und Diensten:

* **grandmo:** Haupt-Administrator (Sudo-Rechte)
* **mastermo:** Backup-Administrator (Sudo-Rechte)
* **git:** System-User für Gitea-Dienste (keine Sudo-Rechte)

## 4. Kritische Wartungsbefehle

Da Incus die Firewall-Regeln aufgrund von Kernel-Einschränkungen nicht selbst injizieren kann (`firewall=false`), muss das NAT-Routing manuell persistiert werden:

```bash
# Manuelles Aktivieren des Internet-Gateways für Container
sudo iptables -t nat -A POSTROUTING -s 10.0.6.0/24 ! -d 10.0.6.0/24 -j MASQUERADE
```

## Kernel- & Firewall-Besonderheiten

Aufgrund der ARM64-Architektur und Kernel-Einschränkungen wurden folgende Anpassungen vorgenommen:

* **Firewall-Modus:** Das System nutzt `iptables-legacy`, da moderne NFT-Module teilweise fehlen.
* **NAT-Regel:** Da Incus die Firewall nicht automatisch verwalten kann, muss der Internetzugriff für Container manuell maskiert werden:
  `sudo iptables -t nat -A POSTROUTING -s 10.0.6.0/24 ! -d 10.0.6.0/24 -j MASQUERADE`
  
## Wichtige Befehle

* **Neustart erzwingen:** `sudo systemctl reboot`
* **Admin-Rechte prüfen:** `sudo whoami` (sollte 'root' ergeben)

---

## 2. Incus_Konfiguration&Bedienungsanleitung.md

# Infrastruktur-Dokumentation: Incus Virtualisierung

## 1. Virtualisierungs-Strategie

Einsatz von Incus (LXC-Fork) zur Bereitstellung isolierter System-Container.

## 2. Netzwerk-Infrastruktur (`incusbr0`)

### 2.1 Technische Konfiguration

* **Subnetz:** 10.0.6.1/24.
* **Modus:** Managed Bridge.
* **Einschränkung:** `ipv4.firewall: "false"`.

## Netzwerk-Konfiguration (`incusbr0`)
* **Subnetz:** 10.0.6.1/24
* **Firewall-Management:** Deaktiviert (`ipv4.firewall=false`), um Kernel-Fehler (`xt_CHECKSUM`) zu umgehen.
* **Erstellung:** `incus network create incusbr0 ipv4.address=10.0.6.1/24 ipv4.nat=true ipv4.firewall=false ipv6.address=none`

### 2.2 Analyse des Ausschlussverfahrens

Ursprünglich schlug die Erstellung der Brücke fehl (`exit status 1: Extension CHECKSUM revision 0 not supported`). 

* **Versuch A (Standard):** Incus verwaltet Firewall. -> **Fehlgeschlagen** (Kernel-Modul fehlt).
* **Versuch B (Bypass):** Incus erstellt nur das Interface, Administrator verwaltet Firewall manuell. -> **Erfolgreich**.

## 3. Port-Exposition via Proxy-Device

Anstatt Ports instabil über die IP-Adresse des Hosts zu binden, nutzt das Biest das native Incus-Proxy-Gerät:

```bash
incus config device add infra-gitea gitea-proxy proxy listen=tcp:0.0.0.0:3000 connect=tcp:127.0.0.1:3000
```

Vorteil: Der Dienst ist unter localhost:3000 erreichbar, selbst wenn sich die interne Container-IP ändert.

## 4. Administration der Web-UI

* **URL:** https://localhost:8443.
* **Sicherheit:** Zugriff nur via TLS-Client-Zertifikat. Das Zertifikat muss im Browser-Keystore des Administrators hinterlegt sein.

## Web-UI (Incus Dashboard)
* **Zugriff:** `https://localhost:8443`
* **Authentifizierung:** TLS-Zertifikate (Client-Zertifikat im Browser importiert).
* **Token-Management:** Falls ein neuer Browser verbunden werden muss: `incus config trust add [Name]`.

## Proxy-Geräte (Port-Forwarding)
Um Dienste aus Containern auf dem Host verfügbar zu machen:
`incus config device add [Container] [Proxy-Name] proxy listen=tcp:0.0.0.0:[Host-Port] connect=tcp:127.0.0.1:[Container-Port]`

---

## 3. Gitea_Konfiguration&Bedienungsanleitung.md

# Anwendungs-Dokumentation: Gitea (MoCode)

## 1. Instanz-Details

* **Container-Name:** `infra-gitea`.
* **Software-Version:** 1.21.4 (ARM64 Binary).
* **Betriebs-User:** `git` (Heimatverzeichnis: `/home/git`).

## Container-Setup
* **Name:** `infra-gitea`
* **Interne IP:** 10.0.6.159
* **Betrieb als:** User `git` (UID 101)

## 2. Speicher-Architektur & FHS-Konformität

Um Datenverlust bei Updates zu vermeiden, werden alle persistenten Daten außerhalb des Programmverzeichnisses (`/usr/local/bin`) gespeichert:

* **WorkPath (GITEA_WORK_DIR):** `/var/lib/gitea`.
* **Repositories:** `/var/lib/gitea/data/gitea-repositories`.
* **Datenbank:** SQLite3 (`/var/lib/gitea/data/gitea.db`).

## 3. Prozess-Management ("Gradle-Style" Troubleshooting)

Aufgrund der engen Kopplung zwischen Gitea und dem SSH-Daemon kann es zu blockierten Sockets kommen.
**Fehlersymptom:** `bind: address already in use`.
**Lösungsschritte (Hard Reset):**

1. `pkill -9 -u git` (Beendet alle Instanzen des Users).
2. `fuser -k 3000/tcp` (Erzwingt Freigabe des Ports).
3. `systemctl restart gitea` (Sauberer Neustart via Systemd).

## 4. Systemd-Service Konfiguration

Der Dienst wird über eine spezialisierte Unit-Datei gesteuert, die den `WORK_DIR` Fehler umgeht:

```ini
[Service]
Environment=GITEA_WORK_DIR=/var/lib/gitea
ExecStart=/usr/local/bin/gitea web --config /var/lib/gitea/custom/conf/app.ini
User=git
Restart=always
```

## 5. Sicherheits-Audit

* **Registrierung:** Deaktiviert (Private Instanz).
* **Sichtbarkeit:** "Ansehen erfordert Anmeldung" ist aktiv.
* **SSH:** Port 2222 (Vermeidung von Kollisionen mit Host-Port 22).

### Zusammenfassung der Architektur-Vorteile:

Durch diese Konfiguration ist das System **"Biest-sicher"**:

1. **Isolation:** Gitea kann das Host-System nicht kompromittieren.
2. **Pfad-Treue:** Updates der Binary beeinflussen die Datenbank nicht.
3. **Resilienz:** Dank Systemd startet Gitea nach jedem Absturz oder Reboot innerhalb von 2 Sekunden automatisch neu.

## Pfade & Verzeichnisse (Wichtig!)
Daten werden konsequent unter `/var/lib/gitea` gespeichert, um System-Verzeichnisse sauber zu halten:
* **Programm:** `/usr/local/bin/gitea`
* **Konfiguration:** `/var/lib/gitea/custom/conf/app.ini`
* **Repositories:** `/var/lib/gitea/data/gitea-repositories`
* **Datenbank:** `/var/lib/gitea/data/gitea.db` (SQLite3)
* **LFS-Pfad:** `/var/lib/gitea/data/lfs`

## Netzwerkzugriff
* **Web-Interface:** Port 3000 (auf Host gemappt via Incus-Proxy).
* **SSH-Port:** 2222 (um Konflikte mit Host-SSH zu vermeiden).

## Systemd-Autostart
Gitea wird als Hintergrunddienst im Container verwaltet:
* **Service-Datei:** `/etc/systemd/system/gitea.service`
* **Besonderheit:** `GITEA_WORK_DIR=/var/lib/gitea` muss gesetzt sein, um Permission-Fehler in `/usr/local/bin` zu vermeiden.

## Wartung & Fehlerbehebung
* **Status prüfen:** `systemctl status gitea` (im Container)
* **Hängende Prozesse killen:** `pkill -u git` oder `fuser -k 3000/tcp`
* **Gezielter Neustart:** `systemctl restart gitea`



