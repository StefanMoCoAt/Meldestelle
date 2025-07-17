# Bounded Contexts Design für Self-Contained Systems

## Übersicht

Das Meldestelle-System wird in 7 Bounded Contexts unterteilt, um eine Self-Contained Systems (SCS) Architektur zu implementieren.

## Bounded Contexts

### 1. Member Management Context (member-management)
**Verantwortlichkeiten:**
- Personenverwaltung (Reiter, Funktionäre, Kontaktpersonen)
- Vereinsverwaltung (Reitvereine, Verbände)
- Mitgliedschaftsbeziehungen

**Kern-Entitäten:**
- DomPerson
- DomVerein

**APIs:**
- `/api/members/persons`
- `/api/members/clubs`
- `/api/members/memberships`

**Abhängigkeiten:**
- Master Data Context (für Länder/Bundesländer)
- Data Integration Context (für ZNS Import)

---

### 2. Horse Registry Context (horse-registry)
**Verantwortlichkeiten:**
- Pferderegistrierung und -verwaltung
- Besitzverhältnisse
- Abstammungsinformationen

**Kern-Entitäten:**
- DomPferd

**APIs:**
- `/api/horses`
- `/api/horses/ownership`
- `/api/horses/pedigree`

**Abhängigkeiten:**
- Member Management Context (für Besitzer/Verantwortliche)
- Data Integration Context (für ZNS Import)

---

### 3. License & Qualification Context (license-management)
**Verantwortlichkeiten:**
- Lizenzverwaltung
- Qualifikationstracking
- Gültigkeitsüberwachung

**Kern-Entitäten:**
- DomLizenz
- DomQualifikation
- LizenzTypGlobal
- QualifikationsTyp

**APIs:**
- `/api/licenses`
- `/api/qualifications`
- `/api/licenses/validity`

**Abhängigkeiten:**
- Member Management Context (für Lizenzinhaber)
- Master Data Context (für Lizenztypen)

---

### 4. Event Management Context (event-management)
**Verantwortlichkeiten:**
- Turnier- und Veranstaltungsorganisation
- Terminplanung
- Veranstaltungsrahmen

**Kern-Entitäten:**
- Turnier
- Veranstaltung
- VeranstaltungsRahmen
- Pruefung_Abteilung

**APIs:**
- `/api/events`
- `/api/tournaments`
- `/api/events/schedule`

**Abhängigkeiten:**
- Member Management Context (für Funktionäre)
- Master Data Context (für Plätze)
- Competition Management Context (für Bewerbe)

---

### 5. Master Data Context (master-data)
**Verantwortlichkeiten:**
- Referenzdatenverwaltung
- Geografische Daten
- Altersklassendefinitionen

**Kern-Entitäten:**
- BundeslandDefinition
- LandDefinition
- AltersklasseDefinition
- Sportfachliche_Stammdaten
- Platz

**APIs:**
- `/api/masterdata/countries`
- `/api/masterdata/states`
- `/api/masterdata/age-classes`
- `/api/masterdata/venues`

**Abhängigkeiten:**
- Keine (Basis-Context)

---

### 6. Data Integration Context (data-integration)
**Verantwortlichkeiten:**
- OEPS ZNS Datenimport
- Datentransformation
- Staging-Management

**Kern-Entitäten:**
- Person_ZNS_Staging
- Pferd_ZNS_Staging
- Verein_ZNS_Staging

**APIs:**
- `/api/integration/import`
- `/api/integration/staging`
- `/api/integration/validation`

**Abhängigkeiten:**
- Alle anderen Contexts (für Datenverteilung)

---

### 7. Competition Management Context (competition-management)
**Verantwortlichkeiten:**
- Bewerbssetup
- Disziplin-spezifische Regeln
- Wertungssystem

**Kern-Entitäten:**
- Bewerb
- Abteilung
- Pruefungsaufgabe
- DressurPruefungSpezifika
- SpringPruefungSpezifika
- Meisterschaft_Cup_Serie

**APIs:**
- `/api/competitions`
- `/api/competitions/disciplines`
- `/api/competitions/scoring`

**Abhängigkeiten:**
- Event Management Context (für Turniere)
- Member Management Context (für Teilnehmer)
- Horse Registry Context (für Pferde)

## Inter-Context Communication

### Synchrone Kommunikation
- REST APIs zwischen Contexts
- Shared DTOs für Datenaustausch

### Asynchrone Kommunikation
- Event-basierte Kommunikation für lose Kopplung
- Domain Events für wichtige Geschäftsereignisse

### Shared Kernel
- Gemeinsame Enums und Basis-DTOs
- Serializer und Validatoren
- Utility-Klassen

## Deployment-Strategie

Jeder Bounded Context wird als separates Modul implementiert:
- Eigene Gradle-Module
- Separate Datenbank-Schemas (optional)
- Unabhängige Deployment-Einheiten
- Eigene API-Endpunkte

## Vorteile der SCS-Architektur

1. **Autonomie**: Jeder Context kann unabhängig entwickelt und deployed werden
2. **Skalierbarkeit**: Contexts können individuell skaliert werden
3. **Technologie-Diversität**: Verschiedene Technologien pro Context möglich
4. **Team-Autonomie**: Teams können unabhängig an verschiedenen Contexts arbeiten
5. **Fehler-Isolation**: Probleme in einem Context beeinträchtigen andere nicht
