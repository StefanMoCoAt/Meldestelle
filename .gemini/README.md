# Gemini – Projekt-Entry-Point

Dieses Verzeichnis enthält die **kurze Start-Anweisung** für Gemini (parallel/extern), damit Ergebnisse konsistent zur Projektstrategie entstehen.

## Single Source of Truth

* **Projektwissen & Entscheidungen:** `docs/`
* `.gemini/` und `.junie/` sind nur Tooling/Guardrails, keine zweite Wahrheit.

## Startreihenfolge (Pflicht)

1. `docs/README.md` (Gesamtstruktur)
2. `docs/04_Agents/README.md` (Artefakt-Vertrag & Arbeitsmodus)
3. `AGENTS.md` (Übersicht der Rollen und Links zu den Playbooks)

## Output-Regel (Anti-Wissensverlust)

Jede Gemini-Session endet mit **genau einem** Artefakt in `docs/`:

* `ADR` (`docs/01_Architecture/adr/`)
* `Reference` (passender Bereich)
* `How-to / Runbook` (passender Bereich)
* `Journal Entry` (`docs/99_Journal/`)
