# Gemini – Projekt-Entry-Point

Dieses Verzeichnis enthält die **kurze Start-Anweisung** für Gemini (parallel/extern), damit Ergebnisse konsistent zur Projektstrategie entstehen.

## Single Source of Truth

* **Projektwissen & Entscheidungen:** `docs/`
* `.junie/` ist nur Tooling/Guardrails, keine zweite Wahrheit.

## Startreihenfolge (Pflicht)

1. `docs/README.md`
2. `docs/03_Agents/README.md` (Artefakt-Vertrag)
3. Relevanter technischer Bereich (pro System):
   * Architektur: `docs/01_Architecture/`
   * Backend (Services): `docs/04_Backend/Services/`
   * Frontend: `docs/05_Frontend/`
   * Infrastruktur: `docs/06_Infrastructure/`

## Output-Regel (Anti-Wissensverlust)

Jede Gemini-Session endet mit **genau einem** Artefakt in `docs/`:

* `ADR` (`docs/01_Architecture/adr/`)
* `Reference` (passender Bereich)
* `How-to / Runbook` (passender Bereich)
* `Journal Entry` (`docs/99_Journal/`)

## Technische Wahrheit „pro System“

Für Services gilt: Eine stabile, nicht-datierte Seite unter `docs/04_Backend/Services/` ist der Einstieg.
Zeitlich datierte Detailanalysen liegen unter `docs/90_Reports/`.

Beispiel:
* Ping-Service: `docs/04_Backend/Services/ping-service.md`
