# TODO-Liste: Nächste Schritte & Meta-Themen für Meldestelle_Pro

**Datum:** 26.Juli 2025

Dieses Dokument listet die übergeordneten Aufgaben und konzeptionellen Ausarbeitungen auf, die parallel zur oder nach der initialen MVP-Entwicklung (Zyklus 1) angegangen werden müssen.

---

### 1. Daten-Aktualisierung & Synchronisation

- [ ] **Konzept ausarbeiten:** Eine detaillierte Strategie für die Synchronisation von aktualisierten `zns.zip`-Dateien definieren.
    - [ ] **Frage klären:** Wie identifizieren wir Änderungen in den Rohdaten? (z.B. über Zeitstempel, Hash-Werte)
    - [ ] **Frage klären:** Wie gehen wir mit Konflikten um? (z.B. wenn ein Datensatz manuell in unserem System geändert und gleichzeitig vom OEPS aktualisiert wurde)
- [ ] **Implementierung im `ZNS-Import (ACL)`-Service:** Die Update-Logik implementieren, die bestehende `DomPerson`-, `DomPferd`- und `DomLizenz`-Entitäten aktualisiert, anstatt sie nur neu anzulegen.
- [ ] **Testfälle definieren:** Spezifische Tests für Update-Szenarien erstellen (z.B. "Reiter erhält neue Lizenz", "Pferd wechselt Besitzer").

---

### 2. Benutzerverwaltung für Veranstalter (Onboarding)

- [ ] **Konzept ausarbeiten:** Den genauen Workflow für das Onboarding eines neuen Vereins (Mandanten) definieren.
- [ ] **Rollen in Keycloak anlegen:** Die Rollen `Mandanten-Administrator` und `Veranstalter` mit den entsprechenden Berechtigungen in Keycloak konfigurieren.
- [ ] **Self-Service-UI entwerfen:** Eine einfache Benutzeroberfläche für den "Vereins-Admin" entwerfen, mit der er weitere Benutzer seines Vereins einladen und verwalten kann.
- [ ] **Implementierung der UI:** Die Self-Service-Benutzerverwaltung in der Client-Anwendung umsetzen.

---

### 3. Funktionärs-Management

- [ ] **Domäne erweitern:** Die `Members-Domäne` (oder eine neue, spezialisierte `Funktionärs-Domäne`) um Entitäten zur Verwaltung von Funktionärs-Qualifikationen und Verfügbarkeiten erweitern.
- [ ] **UI für Funktionärs-Planung entwerfen:** Eine Ansicht für den `Veranstalter` oder `Mandanten-Admin` konzipieren, um verfügbare Funktionäre für ein Turnier zu suchen und einzuplanen (basierend auf der Idee der `FunktionaerEinsatzPlanung`).
- [ ] **Implementierung der Funktionärs-Verwaltung:** Die entsprechenden Backend-Services und UI-Komponenten umsetzen.

---

### 4. Reporting & Analysen

- [ ] **Anforderungen definieren:** In Abstimmung mit Ihnen (Stefan-Mo) eine Liste der wichtigsten Berichte und Statistiken erstellen, die ein Veranstalter benötigt.
    - [ ] Mögliche Berichte: Finanzielle Gesamtabrechnung, Nennungs-Statistiken pro Bewerb, Teilnehmer-Demografie etc.
- [ ] **Technisches Konzept erstellen:** Entscheiden, wie die Reporting-Komponente auf die Daten der verschiedenen Microservices zugreift (z.B. über dedizierte Query-APIs oder ein separates Data-Warehouse).
- [ ] **UI für Berichte entwerfen:** Eine übersichtliche Dashboard-Ansicht für den `Veranstalter` gestalten, in der er seine Berichte abrufen und exportieren kann.
- [ ] **Implementierung der Reporting-Komponente:** Die Backend-Logik zur Datenaggregation und die Frontend-Komponenten zur Visualisierung umsetzen.
