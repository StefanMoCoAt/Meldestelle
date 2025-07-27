# Strategiepapier: Meldestelle_Pro - Gesamtplanung, Roadmap & Optimierung

**Datum:** 27. Juli 2025
**Status:** Finalisiert & zur Umsetzung freigegeben

## 1. Vision & Architektonische Grundpfeiler

Dieses Dokument ist die zentrale Blaupause für die Entwicklung des Projekts **"Meldestelle_Pro"**.

**Die Vision:** Meldestelle_Pro wird die führende digitale Plattform für die Verwaltung und Durchführung von Pferdesport-Veranstaltungen in Österreich. Das System wird nicht nur die komplexen Regularien der ÖTO und FEI korrekt abbilden, sondern durch intelligente, integrierte Werkzeuge die Arbeit für Veranstalter, Funktionäre und Teilnehmer revolutionieren und durch innovative Zusatzmodule wie das **Parcours-Design-Programm** einen einzigartigen Mehrwert schaffen.

Die Grundlage dafür ist eine moderne Software-Architektur, die auf folgenden Prinzipien beruht:
* **Modulare Microservice-Architektur**
* **Domain-Driven Design (DDD)**
* **Ereignisgesteuerte Kommunikation**
* **Multiplattform-Client-Strategie**

---

## 2. Das finale Zieldomänen-Modell (DDD Context Map)

Das System wird in die folgenden, klar abgegrenzten **Bounded Contexts (Domänen)** gegliedert. Fachliche Domänen werden als eigenständige Microservices implementiert, technische Fähigkeiten als zentrale Infrastruktur-Services.

### 2.1 Fachliche Domänen (Microservices)

* **BC1: OeTO-Verwaltung (Masterdata-Service):** Die "Quelle der Wahrheit" für alle globalen Regelwerke.
* **BC2: Sportler, Pferde & Vereine (z.B. `members-` & `horses-service`):** Verwalten die Stammdaten aller Akteure.
* **BC3: Lizenzen & Qualifikationen (`licensing-service`):** Verwaltet die sportlichen Berechtigungen einer Person.
* **BC4: Veranstaltungsplanung (`events-service`):** Modelliert die hierarchische Struktur von Veranstaltungen (`Dach-Veranstaltung` -> `Turnier-Mandat` -> `Bewerb`).
* **BC5: Mandanten- & Lizenz-Verwaltung (`tenancy-service`):** Steuert das Geschäftsmodell und den Software-Zugriff.
* **BC6: Nennungsabwicklung (`nennungs-service`):** Das operative Herzstück für Nennung, Validierung und Startlisten.
* **BC7: Abrechnung & Finanzen (`billing-service`):** Die zentrale Kasse für eine strikt getrennte Kassenführung.
* **BC8: Ergebnisdienst (`result-service`):** Erfasst, berechnet und exportiert Ergebnisse.
* **BC9: Serien-Verwaltung (`championship-service`):** Verwaltet übergeordnete Cups und Meisterschaften.

### 2.2 Technische & Infrastruktur-Domänen (Technische Services)

* **BC10: ZNS-Import (Anti-Corruption-Layer):** Isoliert das System von den OEPS-Rohdatenformaten.
* **BC11: Notification-Service:** Zentraler Dienst zum Versenden von E-Mails, SMS und Push-Benachrichtigungen.
* **BC12: Document-Generation-Service:** Zentraler Dienst zur Erstellung von Dokumenten (PDF, CSV, XML etc.).

---

## 3. Agile Entwicklungs-Roadmap

Wir verfolgen einen agilen, iterativen Ansatz. Jeder Zyklus liefert ein funktionierendes, in der Praxis testbares Produkt.

### Zyklus 1: MVP für C/C-Neu Turniere (Dressur & Springen)
* **Ziel:** Ein voll funktionsfähiges End-to-End-System für den am weitesten verbreiteten Turniertyp, um schnelles Feedback aus "Feld-Versuchen" zu erhalten.
* **Kern-Features:**
    * **Stammdaten & Import:** Implementierung der `Masterdata`-Regeln für Klassen E-LM und des `ZNS-Imports`.
    * **Veranstaltungsplanung:** "Event-Setup-Wizard" für C/C-Neu Turniere.
    * **Nennungsabwicklung:** Online-Nennung, Validierung für C-Turnier-Lizenzen, Erstellung von `Startlisten`.
    * **Abrechnung:** Verbuchung von `Nenngeld` und `Startgeld`.
    * **Ergebnisdienst:** Manuelle Eingabe für "gemeinsames Richten" (Dressur) und "Standardspringen" (Springen). Finaler Export im dualen Format (`.erg` und `.erg.xml`).
* **Meta-Thema & Optimierung:**
    * **Konzept:** Ausarbeitung der Strategie für die **Daten-Aktualisierung & Synchronisation**.
    * **Implementierung:** Etablierung einer robusten **Logging-Strategie** im gesamten System.

### Zyklus 2: Erweiterung für B/A-Turniere & Professionalisierung
* **Ziel:** Abbildung der komplexeren Regeln höherer Turnierkategorien und Automatisierung von Prozessen.
* **Kern-Features:**
    * **Masterdata:** Erweiterung um Regeln für Klassen M und S und komplexe Lizenz-Höherreihungs-Logik.
    * **Springreiten-Bewertung:** Anbindung externer Zeitmessgeräte über die "Hardware-Adapter-Schicht".
    * **Dressur-Bewertung:** Implementierung des "getrennten Richtens".
    * **Abrechnung & Finanzen:** Implementierung der korrekten Preisgeldberechnung gemäß ÖTO.
    * **Client-App:** Entwicklung des **"Live-Turnier-Cockpits"** für die Meldestelle.
* **Meta-Thema & Optimierung:**
    * **Implementierung:** Umsetzung der **Benutzerverwaltung für Veranstalter** (Onboarding-Prozess).
    * **Implementierung:** Einführung von **Resilience Patterns** (Retry, Circuit Breaker) für eine stabilere Service-Kommunikation.
    * **Vorbereitung:** Proaktive **Datenbank-Performance-Optimierung** für die größeren Datenmengen von A/B-Turnieren.

### Zyklus 3 & darüber hinaus: Ökosystem & Wachstum
* **Ziel:** Das System um strategische Module zur Kundenbindung und -gewinnung erweitern.
* **Kern-Features:**
    * **Parcours-Design-Modul:** Entwicklung des visuellen Editors als **"Freemium"-Standalone-Tool**, um Parcours-Bauer als neue Nutzergruppe und Multiplikatoren zu gewinnen.
    * **Serien-Verwaltung:** Implementierung des `championship-service` für Cups und Meisterschaften.
    * **Erweiterung der Sparten:** Schrittweise Implementierung der Logiken für Vielseitigkeit, Fahren etc.
* **Meta-Themen & Optimierung:**
    * **Implementierung:** Umsetzung des **Funktionärs-Managements** und der **Reporting & Analyse-Komponente**.
    * **Implementierung:** Einführung von **Echtzeit-Updates mit WebSockets**.
    * **Vorbereitung:** Evaluierung von **GraphQL** und Vorbereitung des Deployments auf **Kubernetes**.

---

## 4. Übergreifende Optimierungs-Strategie (TODO)

Parallel zur Feature-Entwicklung verfolgen wir eine kontinuierliche Optimierungsstrategie.

### 4.1 Developer Experience (DevEx) & Code-Qualität
* **Ziel:** Die Effizienz, Qualität und Wartbarkeit der Softwareentwicklung maximieren.
* **Maßnahmen:**
    - **[ ] Logging-Strategie implementieren:** Ein zentrales, strukturiertes Logging-Framework etablieren.
    - **[ ] Contract Testing einführen:** Automatische Prüfung der Service-Kompatibilität in der CI/CD-Pipeline.
    - **[ ] Resilience Patterns implementieren:** Das System mit "Retry"- und "Circuit Breaker"-Mustern widerstandsfähiger machen.

### 4.2 Betrieb & Performance
* **Ziel:** Ein schnelles, sicheres und zuverlässiges System im Live-Betrieb gewährleisten.
* **Maßnahmen:**
    - **[ ] Advanced Caching-Strategien umsetzen:** "Cache Warming" und aktives Monitoring der Cache-Effizienz.
    - **[ ] Datenbank-Performance proaktiv optimieren:** Regelmäßige Analyse von SQL-Abfragen und Index-Optimierung.
    - **[ ] Deployment auf Kubernetes vorbereiten:** Erstellung von Helm-Charts und Definition einer "Rolling Update"-Strategie für ausfallsfreie Updates.

### 4.3 Strategische & Zukünftige Technologien
* **Ziel:** Die technologische Basis für zukünftige Anforderungen und Skalierbarkeit schaffen.
* **Maßnahmen:**
    - **[ ] Echtzeit-Updates mit WebSockets implementieren:** Für das "Live-Turnier-Cockpit" und Live-Ergebnisse.
    - **[ ] GraphQL als API-Alternative evaluieren:** Um die Datenabfragen für zukünftige mobile Clients zu optimieren.
