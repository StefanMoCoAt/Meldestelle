# Open-Source-Konformität & Lizenz-Checkliste

Dieses Dokument dient der Überwachung und Sicherstellung der Open-Source-Konformität des Projekts **Meldestelle**. Es wird vom Lead Architect gepflegt.

## Status der Kern-Komponenten (Stand: Januar 2026)

| Komponente | Lizenz | Status | Risiko | Maßnahme / Kommentar |
| :--- | :--- | :--- | :--- | :--- |
| **Kotlin / JVM** | Apache 2.0 / GPLv2+CE | ✅ OK | Sehr gering | Standard-Stack. |
| **Spring Boot / Cloud** | Apache 2.0 | ✅ OK | Sehr gering | |
| **PostgreSQL** | PostgreSQL (BSD-like) | ✅ OK | Sehr gering | |
| **Redis** | **RSALv2 / SSPL** | ⚠️ KRITISCH | Hoch | **Umstieg auf Valkey (BSD) geplant.** |
| **Consul** | **BSL 1.1** | ⚠️ BEOBACHTEN | Mittel | Lizenzänderung durch HashiCorp. Für interne Nutzung aktuell unkritisch. |
| **Keycloak** | Apache 2.0 | ✅ OK | Gering | |
| **SQLDelight** | Apache 2.0 | ✅ OK | Sehr gering | |
| **Redisson** | Apache 2.0 (Core) | ✅ OK | Gering | Sicherstellen, dass keine PRO-Features genutzt werden. |

---

## Checkliste für neue Abhängigkeiten

Bevor eine neue Bibliothek oder Infrastruktur-Komponente hinzugefügt wird, muss sie folgende Kriterien erfüllen:

1.  **Lizenz-Typ:**
    *   Bevorzugt: Apache 2.0, MIT, BSD (3-Clause).
    *   Akzeptabel: MPL 2.0.
    *   Einzelfallprüfung: LGPL (nur als dynamische Bibliothek).
    *   **Verboten:** AGPL, SSPL, RSAL, BSL (sofern nicht explizit vom Architect freigegeben).

2.  **Community & Governance:**
    *   Wird das Projekt von einer neutralen Foundation (Apache, CNCF, Linux Foundation) verwaltet?
    *   Gibt es eine "Single Vendor" Abhängigkeit (Risiko einer plötzlichen Lizenzänderung)?

3.  **Transitive Abhängigkeiten:**
    *   Bringt die Bibliothek "versteckte" Copyleft-Lizenzen mit? (Check via Gradle License Plugin).

---

## TODOs & Strategische Entscheidungen

- [ ] **Migration Redis -> Valkey:** Umstellung der Docker-Images und Test der Kompatibilität.
- [ ] **Consul Review:** Jährliche Prüfung der BSL-Auswirkungen auf unser Deployment-Modell.
- [ ] **Automatisierung:** Integration eines License-Check-Plugins in den CI-Build (z.B. `com.github.jk1.dependency-license-report`).

---
*Dieses Dokument ist Teil der "Docs-as-Code" Strategie und muss bei jeder Änderung am Tech-Stack aktualisiert werden.*
