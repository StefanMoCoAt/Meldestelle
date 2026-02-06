# Session Log: Infrastructure Planning & Reporting Requirements

**Datum:** 06.02.2026
**Teilnehmer:** User (Owner), Lead Architect, DevOps Engineer

## 1. Zusammenfassung
In dieser Session wurde die zukünftige Infrastruktur für den Home-Server (Minisforum MS-R1) detailliert geplant und korrigiert. Zudem wurden Anforderungen für Reporting und Offline-Fähigkeit präzisiert.

## 2. Reporting / Printing (Vorgemerkt)
Der User benötigt eine Lösung zum Generieren von personalisierten PDFs (Startlisten, Ergebnislisten, Protokolle).
*   **Status:** Als "Next Step" in die Roadmap aufgenommen.
*   **Architektur:** Dezentraler Microservice (um Resource-Bursts vom Core-System fernzuhalten).
*   **Aktion:** Wurde in `MASTER_ROADMAP_2026_Q1.md` dokumentiert.

## 3. Infrastructure Setup (Minisforum MS-R1)
**Hardware Specs:**
*   Model: Minisforum MS-R1
*   CPU: 12 Kerne ARM (CP8180)
*   RAM: bis 64GB LPDDR5
*   Network: 2x 10G LAN
*   Storage: NVMe + USB-SSD (Backup)

**Software Stack (Final Decision):**
1.  **Base OS:** Debian 12 (Vendor Variant, vorinstalliert/angepasst für Treiber).
2.  **Hypervisor:** **Incus** (Community Fork von LXD).
3.  **Virtualization Strategy:**
    *   `infra-gitea` (LXC Container): Gitea + Actions Runner.
        *   *Vorteil:* Leichtgewichtig, Native ARM Builds via Runner.
    *   `docker-host-prod` (**VM**): Debian VM als Docker Host.
        *   *Grund:* Bessere Isolation, Kernel-Unabhängigkeit vom Host, keine "Nesting"-Probleme (OverlayFS/ZFS).
4.  **CI/CD & Build Strategy:**
    *   **Tool:** `docker buildx` auf dem Gitea Runner.
    *   **ARM64 Images:** Werden **nativ** gebaut (rasend schnell, da Host = ARM).
    *   **x86_64 Images:** Werden via **QEMU-Emulation** gebaut (langsamer, aber funktional).

## 4. Networking & Offline-First
*   **Remote Access:** Cloudflare Tunnel.
*   **Local LAN (Offline):**
    *   Die "Main-Native-Desktop-App" (Meldestelle) fungiert als lokaler Anchor/Server im LAN.
    *   Clients (Zeitnehmer) verbinden sich via LAN.
    *   **Discovery:** Lokale DNS-Strategie oder Service Discovery (mDNS) notwendig, damit Clients den Server auch ohne Internet finden.

## 5. Backup
*   **Strategie:** Automatisierte Snapshots/Backups auf externe **USB-SSD**.
*   **Motto:** "Kein Backup, kein Mitleid."

## 6. Nächste Schritte
*   Warten auf Hardware-Lieferung.
*   Installation & Setup gemäß korrigiertem Plan (VM statt LXC für Docker).
*   Vorbereitung der CI/CD Pipelines auf Multi-Architektur (Buildx/QEMU).
