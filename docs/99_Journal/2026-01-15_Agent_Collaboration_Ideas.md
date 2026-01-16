# Journal: Ideen zur Verbesserung der Agenten-Zusammenarbeit

**Datum:** 15.01.2026
**Autor:** Documentation & Knowledge Curator
**Kontext:** Analyse des Status Quo der Agenten-Playbooks und Prozessoptimierung.

## 1. Status Quo Analyse

Die aktuelle Struktur mit spezialisierten Agenten-Playbooks (`docs/04_Agents/Playbooks/`) und einer zentralen Dokumentation (`docs/`) ist sehr solide.
- **Stärken:** Klare Verantwortlichkeiten, "Docs-as-Code" als Fundament, Unterscheidung zwischen Konzept (Gemini) und Umsetzung (Junie).
- **Potenzial:** Die Schnittstellen zwischen den Agenten (Handover) sind implizit definiert, könnten aber formalisiert werden, um Reibungsverluste zu minimieren.

## 2. Vorschläge zur Verbesserung

### A. Formalisierte Handover-Artefakte
Statt nur Text zu übergeben, sollten Agenten strukturierte Formate nutzen, die vom nächsten Agenten direkt weiterverarbeitet werden können.

*   **Domain Expert -> Backend/QA:**
    *   Statt Prosa: Nutzung von **Gherkin** (Given/When/Then) für Akzeptanzkriterien.
    *   Vorteil: QA kann dies direkt in Tests überführen, Backend Dev versteht die Edge-Cases.
    *   *Beispiel:* `docs/03_Domain/UseCases/UC001_Nennung.feature`

*   **Architect -> Devs:**
    *   Statt nur ADR-Text: Bereitstellung von **PlantUML/Mermaid** Diagrammen, die ins Repo eingecheckt werden.
    *   Vorteil: Visualisierung ist direkt in der Doku eingebunden und versioniert.

### B. Cross-Agent Review Checklisten
Jedes Playbook könnte eine kurze "Pre-Flight Checklist" erhalten, bevor ein Task als "Done" markiert wird.

*   *Backend Dev:* "Habe ich die API-Änderungen mit dem Frontend-Expert (bzw. dessen Doku) abgeglichen?"
*   *Architect:* "Habe ich das ADR vom Domain Expert auf fachliche Korrektheit prüfen lassen?"

### C. "Session Context" Header
Um den Kontext für die KI-Modelle (Junie/Gemini) schneller herzustellen, könnte ein standardisierter Header für Prompts etabliert werden, der auf die relevanten Doku-Pfade verweist.

*   *Idee:* Ein kleines "Context-File" oder ein Abschnitt im Journal, der sagt: "Wir arbeiten gerade an Feature X, relevante Doku ist A, B und C."

### D. Explizite "Shift-Left" QA
Der **QA Specialist** wird oft erst am Ende aktiv. Er sollte explizit aufgefordert werden, schon während der **Domain-Analyse** Fragen zu stellen ("Wie testen wir das?", "Brauchen wir Testdaten dafür?").

## 3. Nächste Schritte
1.  Diskussion dieser Vorschläge mit dem Lead Architect (User).
2.  Bei Zustimmung: Ergänzung der Playbooks um die spezifischen Handover-Formate und Checklisten.
3.  Pilotierung des "Gherkin-Handovers" bei der nächsten fachlichen Anforderung.
