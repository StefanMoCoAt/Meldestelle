# Session Log: Infrastructure Planning & Reporting Requirements

**Datum:** 06.02.2026
**Teilnehmer:** User (Owner), Lead Architect, DevOps Engineer

## 1. Zusammenfassung
In dieser Session wurde die zukünftige Infrastruktur für den Home-Server (Minisforum MS-R1) geplant und eine Anforderung für das Reporting-System (PDF-Generierung) aufgenommen.

## 2. Reporting / Printing (Vorgemerkt)
Der User benötigt eine Lösung zum Generieren von personalisierten PDFs (Startlisten, Ergebnislisten, Protokolle).
*   **Status:** Als "Next Step" in die Roadmap aufgenommen.
*   **Offene Fragen:** Architektur (Zentral vs. Dezentral), Technologie-Stack.
*   **Aktion:** Wurde in `MASTER_ROADMAP_2026_Q1.md` dokumentiert.

## 3. Infrastructure Setup (Minisforum MS-R1)
Der User hat neue Hardware bestellt (ARM64 Architektur).

**Hardware Specs:**
*   CPU: 12 Kerne ARM (CP8180)
*   RAM: bis 64GB LPDDR5
*   Network: 2x 10G LAN

**Entscheidung: "The Incus Way"**
Da Proxmox auf ARM experimentell ist, wurde folgender Stack beschlossen:
1.  **Base OS:** Debian 12 (Bookworm).
2.  **Hypervisor:** **Incus** (Community Fork von LXD).
3.  **Container-Strategie:**
    *   `infra-gitea` (LXC): Gitea + Actions Runner (Native ARM Builds).
    *   `docker-host-prod` (LXC mit nesting): Docker Host für den Meldestelle-Stack.
4.  **Networking:** Cloudflare Tunnel.

**Vorteile:**
*   Native Performance auf ARM.
*   Snapshots & Backups (via ZFS/Btrfs).
*   Saubere Trennung von Infrastruktur (Gitea) und Applikation (Docker).

## 4. Nächste Schritte
*   Warten auf Hardware-Lieferung.
*   Installation & Setup gemäß Plan.
*   Einrichtung der CI/CD Pipeline (Gitea Actions) für ARM-Builds.
