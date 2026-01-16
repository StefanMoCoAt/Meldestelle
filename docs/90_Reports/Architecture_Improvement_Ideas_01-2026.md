# Vorschläge zur Verbesserung der Dokumentations- und Roadmap-Struktur
**Von:** Lead Software Architect
**An:** Documentation & Knowledge Curator

Um die Fragmentierung der Dokumentation zu vermeiden und die Zusammenarbeit der Agenten zu optimieren, schlage ich folgende Strukturverbesserungen vor:

## 1. Roadmap-Hierarchie ("Single Source of Truth")
Aktuell entstehen oft parallele Roadmaps in Unterordnern (z.B. `05_Backend/ROADMAP.md`). Das führt zu Verwirrung.

**Vorschlag:**
*   Es gibt nur **eine** `MASTER_ROADMAP.md` im Root von `docs/` oder in `docs/01_Architecture/`.
*   Diese Roadmap ist **phasenorientiert** (nicht komponentenorientiert).
*   Jede Phase enthält Sektionen für die jeweiligen Rollen (Backend, Frontend, DevOps, QA).
*   Detail-Roadmaps in Unterordnern sind verboten oder müssen strikt als "Detail-Spezifikation" eines Master-Items gekennzeichnet sein.

## 2. "Living Architecture" Dokumentation
Architektur-Diagramme und Entscheidungen veralten schnell.

**Vorschlag:**
*   **ADRs (Architecture Decision Records):** Jede größere Entscheidung (z.B. "Wechsel auf SQLDelight") muss zwingend als ADR in `docs/01_Architecture/decisions/` festgehalten werden. Das Format ist strikt: *Kontext, Entscheidung, Konsequenzen*.
*   **System-Metapher:** Wir sollten eine zentrale Metapher oder ein Glossar pflegen, das vom Domain Expert validiert wird, damit "Ping", "Meldung", "Fall" überall gleich verstanden werden.

## 3. Agent-Interaktion
Damit ich als Architect nicht "alles mache", müssen die Schnittstellen zwischen den Agenten klarer definiert sein.

**Vorschlag:**
*   **Handover-Protokolle:** Wenn ich eine Architektur-Vorgabe mache (z.B. "Nutze UUIDv7"), muss ich ein Ticket/Issue/Task in der Roadmap definieren, das der Backend Developer dann *eigenverantwortlich* umsetzt.
*   **Review-Pflicht:** Der QA Specialist sollte explizit angefordert werden, *bevor* eine Phase als "Done" markiert wird.

## 4. Build-System als Dokumentation
Die `libs.versions.toml` ist faktisch unsere Dokumentation des Tech-Stacks.

**Vorschlag:**
*   Kommentare in der TOML-Datei sollten erklären, *warum* eine Version gewählt wurde (z.B. "# Downgrade wegen Spring Cloud Inkompatibilität"). Das habe ich bereits begonnen, sollte aber Standard werden.

---
Bitte prüfe diese Vorschläge und integriere sie in dein Playbook oder die Dokumentations-Richtlinien.
