# 📚 Projektdokumentation "Meldestelle"

Willkommen im "Gehirn" des Projekts. Dieses Verzeichnis ist die **Single Source of Truth**.

## 📂 Struktur

### 🏗️ Core
*   **[`01_Architecture`](01_Architecture/)**: Der Masterplan. ADRs, Roadmaps und C4-Modelle.
    *   *Start hier:* `MASTER_ROADMAP_2026_Q1.md`
*   **[`02_Guides`](02_Guides/)**: Guides für neue Entwickler (Setup, Guidelines).
*   **[`03_Domain`](03_Domain/)**: Fachlichkeit (Turnierregeln, Entities).

### 🛠️ Tech Stack
*   **[`05_Backend`](05_Backend/)**: Spring Boot Services, API-Specs, DB-Schema.
    *   *Referenz:* `Services/PingService_Reference.md`
*   **[`06_Frontend`](06_Frontend/)**: Kotlin Multiplatform, Compose UI, State Management.
*   **[`07_Infrastructure`](07_Infrastructure/)**: Docker, Keycloak, CI/CD.

### 🤖 Process
*   **[`04_Agents`](04_Agents/)**: Playbooks für unsere KI-Mitarbeiter.
*   **[`90_Reports`](90_Reports/)**: Statusberichte und Meilenstein-Analysen.
*   **[`99_Journal`](99_Journal/)**: Tägliche Session-Logs.

---

## 📝 Regeln für die Dokumentation

1.  **Docs-as-Code:** Doku liegt beim Code und wird im selben PR aktualisiert.
2.  **Frontmatter:** Jede Markdown-Datei muss einen YAML-Header haben:
    ```yaml
    ---
    type: [ADR, Guide, Reference, Report, Journal]
    status: [DRAFT, ACTIVE, DEPRECATED, ARCHIVED]
    owner: [Rolle]
    ---
    ```
3.  **Archivierung:** Lösche nichts. Verschiebe veraltetes Wissen in `_archive` Ordner oder markiere es als `ARCHIVED`.

---

## 🚀 Quick Links
*   [Master Roadmap](01_Architecture/MASTER_ROADMAP_2026_Q1.md)
*   [Ping Service Reference](05_Backend/Services/PingService_Reference.md)
*   [Agent Playbooks](04_Agents/)
