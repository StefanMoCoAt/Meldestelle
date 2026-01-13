# Agent Operating Model (AOM)

Dieses Verzeichnis definiert, **wie** KI-Unterstützung im Projekt eingesetzt wird:
Rollen/Playbooks, Ablageorte und der minimale Prozess, damit Wissen nicht verloren geht.

## Governance (Konfliktregel)

* **Single Source of Truth:** `docs/`
* **Tooling/Automatisierung:** `.junie/` (Scripts, Checks, optional Archiv – keine zweite „Wahrheit“)
* **Personas-Übersicht:** `AGENTS.md` (Repo-Root)

Wenn Aussagen in `.junie/*` und `docs/*` widersprechen, gilt **`docs/*`**.

## Artefakt-Vertrag (Anti-Wissensverlust)

Jede KI-Session endet mit **genau einem** Artefakt in `docs/`:

1. **ADR** (`docs/01_Architecture/adr/`) – Entscheidung/Optionen/Trade-offs (Status `proposed` ist erlaubt)
2. **Reference** (passender Bereich) – Fakten/Ist-Zustand/Inventar
3. **How-to / Runbook** (passender Bereich) – konkrete Schritte (Setup/Betrieb/Recovery)
4. **Journal Entry** (`docs/99_Journal/`) – Kurzprotokoll, wenn nichts „fertig“ wird

## Tool-Rollen (keine Doppelarbeit)

* **Junie (IDE-nah):** Code/Repo-Wahrheit (Dateien, konkrete Implementierung, Refactors)
* **Gemini (parallel/extern):** Variantenraum (Optionen, Argumentation, Formulierungen, Gegenentwurf)

„Wahr“ wird es erst, wenn es im passenden `docs/*` Artefakt verankert ist.

## Playbooks

* `Playbooks/Junie.md`
* `Playbooks/Gemini.md`
* `Playbooks/Curator.md`
