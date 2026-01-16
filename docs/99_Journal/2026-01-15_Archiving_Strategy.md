# Journal: Strategie für Archivierung und Dokumenten-Lifecycle

**Datum:** 15.01.2026
**Autor:** Documentation & Knowledge Curator
**Kontext:** Entwicklung einer Strategie zur Handhabung veralteter Dokumente (ToDos, Roadmaps) und Vereinheitlichung des Dokumenten-Status.

## 1. Problemstellung
Aktuell existieren Dokumente wie `MASTER_ROADMAP_2026_Q1.md` neben älteren Roadmaps oder Status-Reports. Es ist nicht immer sofort ersichtlich, was "Single Source of Truth" (SSOT) ist und was historische Relevanz hat.
Zudem fehlen einheitliche Header, die den Status eines Dokuments (z.B. "DRAFT", "ACTIVE", "DEPRECATED") auf den ersten Blick klarmachen.

## 2. Lösungsansatz: Lifecycle-Management

### A. Ordner-Struktur für Archivierung
Wir führen in jedem Hauptbereich (`01_Architecture`, `05_Backend`, etc.) einen Unterordner `_archive/` ein.
*   **Regel:** Sobald ein Dokument (z.B. eine Roadmap oder eine ToDo-Liste) abgearbeitet oder obsolet ist, wird es **nicht gelöscht**, sondern in den `_archive/`-Ordner verschoben.
*   **Benennung:** Beim Verschieben wird das Datum vorangestellt: `YYYY-MM-DD_OriginalName.md`.

### B. Standardisierter Header (Frontmatter-Style)
Jedes Dokument erhält einen standardisierten Header-Block ganz oben.

```markdown
---
type: [Roadmap | Concept | Reference | ADR | Report]
status: [DRAFT | ACTIVE | DEPRECATED | ARCHIVED]
owner: [Rolle, z.B. Lead Architect]
last_update: YYYY-MM-DD
context: [Link zu Ticket/Epic oder "General"]
---
```

*   **ACTIVE:** Die aktuelle Wahrheit. Darf nur einmal pro Thema existieren (SSOT).
*   **DEPRECATED:** Noch gültig, aber Ablösung geplant.
*   **ARCHIVED:** Historisch, nur noch zum Nachlesen. (Liegt idealerweise im `_archive/` Ordner).

### C. Umgang mit ToDo-Listen
ToDo-Listen in Markdown-Dateien neigen dazu, zu veralten.
*   **Strategie:** ToDos gehören primär in den Issue-Tracker (wenn vorhanden) oder in die `MASTER_ROADMAP`.
*   **Temporäre ToDos:** Wenn ToDos in Konzepten stehen, müssen sie bei Abschluss in die Roadmap oder das Backlog überführt werden. Das Dokument selbst wird dann zum "Reference"-Dokument (ohne offene ToDos) oder archiviert.

## 3. Umsetzungsschritte

1.  **Curator Playbook Update:** Der Curator ist verantwortlich für das Verschieben in `_archive/` am Ende einer Session.
2.  **Template Erstellung:** Definition des Standard-Headers für alle neuen Dokumente.
3.  **Cleanup:** Einmaliges Aufräumen der bestehenden Ordner (`01_Architecture`, `90_Reports`).

## 4. Beispiel-Workflow
1.  Architect erstellt `Roadmap_Q1.md` (Status: ACTIVE).
2.  Quartal endet. Architect erstellt `Roadmap_Q2.md`.
3.  Curator verschiebt `Roadmap_Q1.md` nach `01_Architecture/_archive/2026-03-31_Roadmap_Q1.md` und setzt Status auf ARCHIVED.
