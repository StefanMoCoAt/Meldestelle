# Session Log: Domain Analysis & Core Model Definition

*   **Datum:** 2026-01-15
*   **Rolle:** Domain/Product Expert
*   **Teilnehmer:** User (Stefan)
*   **Status:** Abgeschlossen

## Ziele der Session
1.  Analyse der bestehenden Dokumentation (insb. OEPS Pflichtenheft).
2.  Schärfung des Domänenmodells für nationale Turniere.
3.  Erstellung von User Stories, Use Cases und NFRs.
4.  Ableitung eines konkreten Datenbankschemas (SQL) für die Offline-First-Architektur.

## Durchgeführte Arbeiten

### 1. Analyse & Glossar
*   **Legacy Spec Analyse:** Das OEPS Pflichtenheft 2021 V2.4 wurde detailliert analysiert. Wichtigste Erkenntnis: Identifikation erfolgt über numerische `Satznummern`, nicht Namen.
*   **Glossar:** `docs/03_Domain/00_Glossary.md` erstellt. Begriffe wie *Startkarte*, *Satznummer*, *Abteilung* definiert.
*   **Core Model:** `docs/03_Domain/01_Core_Model/Entities/Overview.md` aktualisiert. Entitäten `Pferd` und `Akteur` um OEPS-spezifische Felder erweitert.

### 2. Anforderungen (Requirements)
*   **User Stories:** `docs/03_Domain/03_Analysis/User_Stories_Draft.md` erstellt. Fokus auf Offline-Import (ZNS) und Fehlertoleranz ("Override").
*   **Use Cases:** `docs/03_Domain/03_Analysis/Use_Cases_Draft.md` erstellt. Clusterung in Initialisierung, Nennung, Sport und Abschluss.
*   **NFRs:** `docs/03_Domain/03_Analysis/Non_Functional_Requirements_Draft.md` erstellt. Fokus auf Local-First, Konfliktlösung und Audit-Sicherheit.

### 3. Technisches Design
*   **Datenbankschema:** `docs/03_Domain/01_Core_Model/Entities/Database_Schema.sql` erstellt.
    *   Verwendung von UUIDs (`TEXT`) für Offline-Kompatibilität.
    *   Modellierung von `competition` mit `division_id` für Abteilungen.
    *   Einführung von `audit_log` und `version` Feldern für Sync.

## Ergebnisse & Artefakte
| Artefakt | Pfad | Status |
| :--- | :--- | :--- |
| Glossar | `docs/03_Domain/00_Glossary.md` | Final |
| Core Model | `docs/03_Domain/01_Core_Model/Entities/Overview.md` | Updated |
| Legacy Analyse | `docs/03_Domain/03_Analysis/Legacy_Spec_Analysis_2026-01.md` | Draft |
| User Stories | `docs/03_Domain/03_Analysis/User_Stories_Draft.md` | Draft |
| Use Cases | `docs/03_Domain/03_Analysis/Use_Cases_Draft.md` | Draft |
| NFRs | `docs/03_Domain/03_Analysis/Non_Functional_Requirements_Draft.md` | Draft |
| DB Schema | `docs/03_Domain/01_Core_Model/Entities/Database_Schema.sql` | Proposal |

## Nächste Schritte
*   **Review:** Architekt und Backend-Dev müssen das Schema prüfen.
*   **Implementierung:** Übertragung des SQL-Schemas in SQLDelight (`.sq` Dateien) im KMP-Modul.
*   **Prototyping:** Erster "Walking Skeleton" für den ZNS-Import basierend auf den User Stories.
