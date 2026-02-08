# 💻 Client-Setup: Arbeitsplatz an "Das Biest" anbinden

Diese Anleitung beschreibt die Einrichtung eines lokalen Rechners, um via SSH und Cloudflare-Tunnel auf die
Gitea-Instanz (`git.mo-code.at`) zuzugreifen.

## 1. SSH-Schlüssel generieren

Falls noch kein Schlüssel vorhanden ist, erstelle einen modernen Ed25519-Key:

```bash
ssh-keygen -t ed25519 -C "stefan.mo.co@gmail.com"
```

Bestätige die Pfade mit Enter. Den Inhalt des öffentlichen Schlüssels anzeigen:

```bash
cat ~/.ssh/id_ed25519.pub
```

**Aktion:** Kopiere den Inhalt und füge ihn in der Gitea Web-UI (`https://git.mo-code.at`) unter **`Einstellungen` ->
`SSH / GPG Schlüssel` hinzu.

## 2. Cloudflare Tunnel-Client installieren

Der Rechner benötigt `cloudflared`, um den SSH-Traffic durch das Zero Trust Gateway zu "beamen".

**Für Debian/Ubuntu:**

```bash
curl -L --output cloudflared.deb [https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb](https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb)
sudo dpkg -i cloudflared.deb
```

## 3. SSH-Konfiguration anlegen (`~/.ssh/config`)

Damit Git automatisch den richtigen Port (2222) und den Tunnel-Proxy nutzt, muss die lokale SSH-Config angepasst werden.

1. Datei öffnen: nano ~/.ssh/config
2. Folgenden Block einfügen:

```text
Host git.mo-code.at
    HostName ssh.mo-code.at
    User git
    Port 2222
    ProxyCommand /usr/bin/cloudflared access ssh --hostname %h
    IdentityFile ~/.ssh/id_ed25519
```

> **Hinweis:** Prüfe mit `which cloudflared`, ob der Pfad `/usr/bin/cloudflared` korrekt ist und passe ihn ggf. an.

## 4. Zero Trust Autorisierung (Einmalig)

Bevor die erste Verbindung klappt, muss der Rechner bei Cloudflare angemeldet werden:

```bash
cloudflared access login [https://ssh.mo-code.at](https://ssh.mo-code.at)
```

Es öffnet sich ein Browser. Logge dich mit deiner E-Mail ein und bestätige den Zugriff.

## 5. Verbindungstest

Teste die Verbindung zum Biest:

```bash
ssh -T git@git.mo-code.at
```

> **Soll-Ergebnis:** `Hi there, grandmo! You've successfully authenticated...`

---

### Was wir heute erreicht haben (Zusammenfassung für dein Archiv):

* **Host (Biest):** Läuft stabil auf Debian 12 (ARM64), nutzt `iptables-legacy` für das Netzwerk.
* **Infrastruktur:** Incus-Container `infra-gitea` ist über interne Proxies für Port 3000 (Web) und 2222 (SSH)
  erreichbar.
* **Sicherheit:** Cloudflare Tunnel schützt das System; SSH-Zugriff ist nur über eine autorisierte Zero Trust
  Application möglich.
* **Gitea:** Konfiguriert auf `git.mo-code.at` mit SSH-Server-Aktivierung im Container.
