---
type: Log
agent: DevOps Engineer
date: 2026-02-07
status: IN_PROGRESS
---

# 🐧 Log: Hardware Setup Minisforum MS-R1

## Kontext
Der neue Home-Server (Minisforum MS-R1) ist eingetroffen. Dies ist die Ziel-Hardware für den "Offline-First" Betrieb der Meldestelle.
Wir haben die Dokumentation (Handbuch & Specs) erhalten und beginnen mit der Integration in die Architektur-Dokumentation.

## Hardware Specs (Zusammenfassung)
*   **Modell:** Minisforum MS-R1
*   **CPU:** CP8180 (12 Cores / 12 Threads, 2.6 GHz) - ARM64 Architektur? (Muss verifiziert werden, Specs sagen "Arm Immortalis-G720" GPU, deutet auf ARM SoC hin).
*   **RAM:** Max 64GB LPDDR5 5500MHz (ECC Supported).
*   **Storage:** 1x M.2 NVMe (PCIe 4.0 x4).
*   **Network:** 2x 10G LAN (SFP+ via RTL8127?). *Korrektur aus Specs:* "10G LAN(RJ45)(RTL8127) x 2".
*   **OS Support:** Debian 12 (Vendor Image vorhanden).

## Actions
1.  [x] Dokumentation (Handbuch, Specs) gesichtet.
2.  [ ] `MASTER_ROADMAP` aktualisieren (Hardware-Details bestätigen).
3.  [ ] Systemabbild sichern (bereits vom User erledigt).

## Nächste Schritte
*   Verifizierung der CPU-Architektur (ARM64 vs x86). Die Roadmap ging von ARM64 aus. Die Specs nennen "CP8180" und "Arm Immortalis", was dies bestätigt.
*   Planung der Virtualisierung (Incus auf Debian 12).
