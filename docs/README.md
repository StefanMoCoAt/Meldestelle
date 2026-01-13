# Projektdokumentation "Meldestelle"

Willkommen zur zentralen Projektdokumentation. Dieses Verzeichnis ist die "Single Source of Truth" für alle architektonischen Entscheidungen, Anleitungen und Implementierungsdetails.

Die Dokumentation wird nach dem **"Docs-as-Code"**-Prinzip gepflegt: Sie liegt neben dem Code, wird mit Git versioniert und von allen Teammitgliedern (Mensch und KI) aktuell gehalten.

## Struktur der Dokumentation

*   **/01_Architecture**: Enthält **Architecture Decision Records (ADRs)**. Jede wichtige Architekturentscheidung (z.B. "Warum nutzen wir ein API-Gateway?") wird hier in einer eigenen Datei festgehalten.
*   **/02_Onboarding**: Anleitungen für den schnellen Einstieg in das Projekt. Enthält `Getting_Started.md` für das lokale Setup.
*   **/03_Agents**: Definitionen und spezifische Anleitungen für die im Projekt eingesetzten KI-Agenten.
    *   `AGENTS.md`: Definiert die Rollen, Verantwortlichkeiten und Regeln für jeden Agenten.
    *   `Gemini/`, `Junie/`: Ablageorte für finalisierte Berichte und Analysen der jeweiligen KI-Assistenten.
*   **/04_Backend**: Dokumentation, die sich speziell auf die Backend-Services bezieht.
*   **/05_Frontend**: Dokumentation für das KMP-Frontend ("Meldestelle Portal").
*   **/06_Infrastructure**: Anleitungen und Konfigurationsdetails zur Infrastruktur (Docker, Keycloak, Consul, etc.).

## Wie man diese Dokumentation pflegt

Jeder Entwickler und jeder KI-Agent ist dafür verantwortlich, die Dokumentation, die seinen Arbeitsbereich betrifft, zu aktualisieren.

*   **Bei neuen Features:** Erstelle oder aktualisiere die entsprechende Implementierungs-Doku.
*   **Bei Architektur-Änderungen:** Erstelle ein neues ADR oder aktualisiere ein bestehendes.
*   **Bei Änderungen am Setup:** Passe die Anleitungen im `Onboarding`- oder `Infrastructure`-Verzeichnis an.

Änderungen an der Dokumentation sollten Teil derselben Pull Request/Commit sein wie die zugehörigen Code-Änderungen.
