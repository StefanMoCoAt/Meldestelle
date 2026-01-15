# Plan zur Strukturierung der Domänen-Dokumentation

*   **Datum:** 2026-01-14
*   **Autor:** Documentation & Knowledge Curator
*   **Status:** Entwurf

## Ausgangslage

Das Verzeichnis `docs/02_Domain` enthält wertvolle, aber zunehmend unübersichtliche Informationen.
Es gibt eine Mischung aus:
*   Destilliertem Wissen (`01_Core_Entities.md`)
*   Referenz-Material (`Reference/FEI...`, `Reference/OETO...`)
*   Entwicklungs-Infos (`Development/`)
*   Implizitem Wissen (`Reference/Geschichten`)

## Zielbild

Gemäß **ADR-0012** (Proposed) soll eine strikte Trennung nach Reifegrad eingeführt werden.

## Migrations-Schritte

1.  **Verzeichnisse erstellen:**
    *   `docs/02_Domain/01_Core_Model/Entities`
    *   `docs/02_Domain/01_Core_Model/Processes`
    *   `docs/02_Domain/01_Core_Model/Rules`
    *   `docs/02_Domain/03_Analysis/Scenarios`

2.  **Dateien verschieben & umbenennen:**
    *   `01_Core_Entities.md` -> `01_Core_Model/Entities/Overview.md` (oder aufsplitten)
    *   `Reference/Geschichten` -> `03_Analysis/Scenarios`
    *   `Reference/FEI_01-2026` -> `02_Reference/FEI_Regelwerk`
    *   `Reference/OETO_01-2026` -> `02_Reference/OETO_Regelwerk`

3.  **Development-Ordner auflösen:**
    *   Die Inhalte von `docs/02_Domain/Development` (`kdoc-style.md`, `start-local.md`, `branchschutz...`) gehören **nicht** in die Domäne.
    *   `start-local.md` -> `docs/02_Onboarding/`
    *   `branchschutz...` -> `docs/02_Onboarding/`
    *   `kdoc-style.md` -> `docs/02_Onboarding/CodingGuidelines/`

4.  **Glossar anlegen:**
    *   Erstellung von `docs/02_Domain/00_Glossary.md` als leere Hülle für den Start.

## Nächste Schritte für den User

*   Bestätigung des ADR-0012.
*   Freigabe zur Durchführung der Datei-Operationen (Verschieben/Umbenennen).
