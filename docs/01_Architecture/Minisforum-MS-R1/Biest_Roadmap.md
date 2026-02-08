# Roadmap: Finalisierung Gitea-Infrastruktur (MS-R1)

## Phase 1: Konnektivität & Erreichbarkeit 🌐
- [x] **Schritt 1: Cloudflare Tunnel (Tor zur Welt)**
    - Ziel: Sicherer Zugriff über `git.mo-code.at` ohne Portfreigabe.
    - Status: ABGESCHLOSSEN (siehe `Gitea-SSH-Setup.md`).
- [x] **Schritt 2: SSH-Key & Git-Client Setup**
    - Ziel: Passwortloses "Push & Pull" über Port 2222.
    - Status: ABGESCHLOSSEN (siehe `Gitea-SSH-Setup.md`).
- [x] **Schritt 2b: Erster erfolgreicher Push**
    - Ziel: Validierung der gesamten Kette (Laptop → Cloudflare → Tunnel → Host → Container → Gitea).
    - Status: ABGESCHLOSSEN.

## Phase 2: Datensicherheit & Resilienz 🛡️
- [ ] **Schritt 3: Die "Biest-Versicherung" (Automatisches Backup)**
    - Ziel: Nächtliche Snapshots des LXC-Containers + SQLite-Dumps auf den Host-Speicher.
    - Status: Ausstehend.

## Phase 3: Kommunikation & Feinschliff ✉️
- [ ] **Schritt 4: SMTP-Integration (Brieftaube)**
    - Ziel: E-Mail Benachrichtigungen über mo-code.at Domain.
    - Status: Ausstehend.
- [ ] **Schritt 5: Gitea-Update-Protokoll**
    - Ziel: Dokumentation des Prozesses zum manuellen Tausch der Binary.
    - Status: Ausstehend.
