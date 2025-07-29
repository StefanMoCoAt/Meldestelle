# Checkliste: Detaillierter Entwicklungs- & Refactoring-Plan für Meldestelle_Pro

**Datum:** 28. Juli 2025

Dieses Dokument dient als zentrale Checkliste für die abgeschlossenen Planungs- und Refactoring-Arbeiten sowie für die anstehenden Implementierungs-Aufgaben gemäß unserer agilen Roadmap.

---

### ✅ Phase 1: Fundament & Architektur (Abgeschlossen)

*In dieser Phase haben wir das Gehirn und das Nervensystem unseres Projekts entworfen. Wir haben eine gemeinsame Vision geschaffen und die technischen Leitplanken für die gesamte zukünftige Entwicklung gesetzt.*

* **[x] Gesamtarchitektur finalisiert**
    * **Wofür?** Um eine gemeinsame, klare Vorstellung vom Zielsystem zu haben. Dies verhindert Missverständnisse und stellt sicher, dass alle an einem Strang ziehen.
    * **Artefakte:** Detailliertes DDD-Modell mit 12 Bounded Contexts, C4-Modell zur Visualisierung, agile Entwicklungs-Roadmap.

* **[x] `core`-Modul konzipiert & refaktorisiert**
    * **Wofür?** Um eine zentrale Bibliothek (Shared Kernel) zu schaffen, die Code-Wiederholung vermeidet, die Entwicklungsgeschwindigkeit erhöht und Konsistenz im gesamten System erzwingt.
    * **Details:**
        * **[x] `Result`-Klasse vereinheitlicht:** Wir haben uns für die typsichere `Result<T, E>`-Variante entschieden, um Geschäftsfehler sauber und explizit behandeln zu können, anstatt technische Exceptions zu missbrauchen.
        * **[x] Datenbank-Migrationen professionalisiert:** Wir haben den selbstgeschriebenen Migrator durch **Flyway** ersetzt. Damit setzen wir auf einen robusten, transaktionssicheren Industrie-Standard für die Verwaltung unseres Datenbankschemas.
        * **[ ] Konfigurations-Management (`AppConfig`) refaktorisiert:** Der Umbau von einem globalen `object` zu einer `class` wurde beschlossen. Dies macht unsere Services testbar und ihr Startverhalten vorhersagbar ("Fail-Fast"-Prinzip).
        * **[ ] Service Discovery (`ServiceRegistration`) refaktorisiert:** Durch den Umbau auf Dependency Injection ist die Komponente nun isoliert testbar und ihre Abhängigkeiten sind klar ersichtlich.
        * **[ ] Build-System (`build.gradle.kts`) optimiert:** Die Abhängigkeiten wurden korrigiert und auf ein zentrales **Version Catalog (`libs.versions.toml`)** umgestellt. Damit verwalten wir alle Versionen an einer einzigen Stelle.
        * **[ ] Dokumentation (`README.md`) & Commit erstellt:** Die Arbeit wurde sauber dokumentiert und ein nachvollziehbarer Commit-Eintrag formuliert, um die getroffenen Entscheidungen festzuhalten.

---

### 🔳 Phase 2: Implementierung von Zyklus 1 (Nächste Schritte)

*In dieser Phase bauen wir das erste funktionierende Produkt (MVP). Wir errichten die erste Säule (`masterdata-service`) auf unserem Fundament und folgen dabei konsequent unserer Clean Architecture, um eine Blaupause für alle weiteren Services zu schaffen.*

* **[ ] `masterdata`-Service implementieren**
    * **Wofür?** Dieser Service ist das digitale Regelbuch der ÖTO. Er stellt allen anderen Services die absolut notwendigen Stammdaten (Länder, Altersklassen etc.) zur Verfügung, damit diese ihre Geschäftslogik korrekt ausführen können.
    * **Schritte:**
        * **[ ] Domain-Layer (`masterdata-domain`):**
            * **Aufgabe:** Das Herz des Service. Hier definieren wir die Geschäftsmodelle als reine Kotlin-Klassen (`LandDefinition`, `AltersklasseDefinition` etc.) und die "Verträge" (`Repository-Interfaces`), die festlegen, welche Datenoperationen möglich sein müssen.
        * **[ ] Infrastructure-Layer (`masterdata-infrastructure`):**
            * **Aufgabe:** Die technische Umsetzung der Verträge. Hier schreiben wir den `Exposed`-Code, der die Domänen-Objekte in die von Flyway erstellten Datenbank-Tabellen speichert und von dort liest.
        * **[ ] Application-Layer (`masterdata-application`):**
            * **Aufgabe:** Die Anwendungslogik. Hier implementieren wir die `Use Cases`, die die Repositories orchestrieren, um komplexe Abläufe abzubilden (z.B. "Erstelle ein neues Land, aber nur, wenn der ISO-Code noch nicht existiert").
        * **[ ] API-Layer (`masterdata-api`):**
            * **Aufgabe:** Das "Gesicht" des Service. Hier bauen wir die `Ktor`-REST-Schnittstelle, die es anderen Services oder dem Frontend erlaubt, die Use Cases über das Netzwerk aufzurufen. Wir integrieren hier auch die zentrale Fehlerbehandlung via `StatusPages`.

* **[ ] `members-` & `horses-` Service implementieren**
    * **Wofür?** Diese Services verwalten die zentralen Akteure unseres Systems: die Personen und die Pferde. Sie sind die Grundlage für die Nennung und die Ergebniszuordnung.
    * **Vorgehen:** Aufbau nach dem exakten Vorbild und den Qualitätsstandards des `masterdata-service`.

* **[ ] `events-` Service implementieren**
    * **Wofür?** Dieser Service ermöglicht es dem `Mandanten-Administrator`, die Hülle für ein Turnier zu schaffen – den Rahmen, in den später die Nennungen und Ergebnisse eingetragen werden.
    * **Vorgehen:** Implementierung des "Event-Setup-Wizards" für die Erstellung von C/C-Neu Turnieren.

* **[ ] Weitere Services für Zyklus 1 (Basis-Implementierungen)**
    * **Wofür?** Um den End-to-End-Prozess für unser MVP zu vervollständigen.
    * **[ ] `nennungs-service`:** Nimmt Nennungen an und erstellt Startlisten.
    * **[ ] `billing-service`:** Verbuchung von Nenngeldern für die "Bar-Kassa".
    * **[ ] `result-service`:** Ermöglicht die manuelle Erfassung von Ergebnissen und deren Export im OEPS-Format.

---
