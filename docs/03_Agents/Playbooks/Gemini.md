# Playbook: Gemini (parallel/extern)

## Zweck
Gemini wird genutzt für **Konzeptarbeit**: Varianten vergleichen, Argumente/Trade-offs schärfen, Formulierungen für ADRs/Doku liefern, Review von Entwürfen.

## Startpunkt
1. `docs/README.md`
2. `docs/03_Agents/README.md` (Artefakt-Vertrag)
3. Je nach Thema: Architektur (`docs/01_Architecture/`), Backend (`docs/04_Backend/`), Frontend (`docs/05_Frontend/`), Infrastruktur (`docs/06_Infrastructure/`)

## Do
* Immer 2–4 Optionen mit Vor-/Nachteilen liefern.
* Offene Fragen explizit als Liste zurückgeben.
* Formuliere Outputs so, dass sie **direkt** in ein `docs/*` Artefakt übernommen werden können.

## Don’t
* Keine Annahmen als Fakten verkaufen.
* Keine Repo-spezifischen Behauptungen ohne Quelle (Dateipfad/Link).

## Abschluss (Pflicht)
Der Output wird durch den `Curator` als genau **ein** Artefakt in `docs/` verankert (ADR/Reference/How-to/Journal).
