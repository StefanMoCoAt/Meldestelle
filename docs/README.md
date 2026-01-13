# Projektdokumentation "Meldestelle"

Willkommen zur zentralen Projektdokumentation. Dieses Verzeichnis ist die "Single Source of Truth" für alle architektonischen Entscheidungen, Anleitungen und Implementierungsdetails.

Die Dokumentation wird nach dem **"Docs-as-Code"**-Prinzip gepflegt: Sie liegt neben dem Code, wird mit Git versioniert und von allen Teammitgliedern (Mensch und KI) aktuell gehalten.

## Struktur der Dokumentation

*   **/01_Architecture**: Architektur (ADRs, C4/Diagramme, Architektur-Referenzen).
*   **/02_Onboarding**: Einstieg & Entwickler-Workflow (lokales Setup, PR-Workflow, Style-Guides).
*   **/03_Agents**: Agent Operating Model (AOM) + Playbooks für Junie/Gemini und weitere KI-Unterstützungen.
*   **/04_Backend**: Backend-spezifische Dokumentation (Services, Datenmodelle, Integrationen).
*   **/05_Frontend**: Frontend-spezifische Dokumentation (KMP/Compose, Offline/Synchronisierung).
*   **/06_Infrastructure**: Betrieb & Infrastruktur (Docker, Keycloak, Observability, Ports/URLs, Runbooks).
*   **/90_Reports**: Berichte/Analysen/Status-Reports (zeitlich geordnet, nicht zwingend „verbindliche Regeln“).
*   **/99_Journal**: Kurzprotokolle pro Session (Anti-Wissensverlust, Nachvollziehbarkeit).

## Wie man diese Dokumentation pflegt

Jeder Entwickler und jeder KI-Agent ist dafür verantwortlich, die Dokumentation, die seinen Arbeitsbereich betrifft, zu aktualisieren.

*   **Bei neuen Features:** Erstelle oder aktualisiere die entsprechende Implementierungs-Doku.
*   **Bei Architektur-Änderungen:** Erstelle ein neues ADR oder aktualisiere ein bestehendes.
*   **Bei Änderungen am Setup:** Passe die Anleitungen im `Onboarding`- oder `Infrastructure`-Verzeichnis an.

Änderungen an der Dokumentation sollten Teil derselben Pull Request/Commit sein wie die zugehörigen Code-Änderungen.

### Wichtigste Einstiege

*   Agenten/Arbeitsmodus: `docs/03_Agents/`
*   Lokales Setup/Workflow: `docs/02_Onboarding/`
*   Architekturentscheidungen: `docs/01_Architecture/adr/`
*   Backend (pro Service): `docs/04_Backend/Services/`
*   Ping-Service (Startpunkt): `docs/04_Backend/Services/ping-service.md`
*   Ping-Service Implementierungs-Report (Historie): `docs/90_Reports/Ping-Service_Impl_01-2026.md`
