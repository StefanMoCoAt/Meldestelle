# `.junie/` – Tooling für Docs-as-Code

Dieses Verzeichnis enthält **nur Tooling/Guardrails** für das Projekt.

**Wichtig:** Die **Single Source of Truth** für Projektwissen ist `docs/`.
In `.junie/` liegen **keine verbindlichen Projektregeln** und keine zweite Dokumentationswelt.

## Scripts

* `./.junie/scripts/validate-links.sh`
  * Prüft Markdown-Links in `docs/**/*.md` auf gebrochene relative Links.
  * Schlägt fehl, wenn veraltete Pfade (z.B. `docs/00_Domain/`, `docs/adr/`) im Text vorkommen.

* `./.junie/scripts/check-docs-drift.sh`
  * Sehr schlanke Drift-Checks gegen die aktuelle Doku-Struktur (z.B. Konsistenz-Checks in Architektur/ADRs/C4).

* `./.junie/scripts/markdown-autofix.sh`
  * Hilfsscript für Markdown-Formatierung.

* `./.junie/scripts/render-plantuml.sh`
  * Rendern von PlantUML-Diagrammen (wenn genutzt).
