# ADR-0012: Strukturierung der Domänen-Dokumentation

* **Status:** Accepted
* **Datum:** 2026-01-14
* **Autor:** Documentation & Knowledge Curator

## Kontext

Das Projekt "Meldestelle" hat eine komplexe fachliche Domäne, die durch externe Regelwerke (ÖTO, FEI) und implizites
Wissen ("Geschichten") geprägt ist.
Die Menge an Informationen im Verzeichnis `docs/02_Domain` wächst schnell an. Es besteht die Gefahr, dass Informationen
unstrukturiert abgelegt werden, schwer auffindbar sind oder veralten.
Eine klare Struktur ist notwendig, um die "Single Source of Truth" für die Fachlichkeit zu gewährleisten und die
Zusammenarbeit zwischen Domain Experts und Entwicklern zu skalieren.

## Entscheidung

Wir strukturieren das Verzeichnis `docs/02_Domain` strikt nach dem Reifegrad und der Art der Information.

### 1. Die Struktur

```text
docs/02_Domain/
├── 00_Glossary.md              # Ubiquitous Language (Zentrales Wörterbuch)
├── 01_Core_Model/              # Die "Wahrheit" für die Implementierung (Destillat)
│   ├── Entities/               # Detail-Beschreibungen der Entitäten (Event, Turnier, etc.)
│   ├── Processes/              # Fachliche Prozesse (Nennung, Ergebnis-Erfassung)
│   └── Rules/                  # Explizite Geschäftsregeln (Validierungen)
├── 02_Reference/               # Externe Quellen (Read-Only / Referenz)
│   ├── FEI_Regelwerk/          # Original-Texte / Zusammenfassungen FEI
│   ├── OETO_Regelwerk/         # Original-Texte / Zusammenfassungen ÖTO
│   └── Legacy_Specs/           # Alte Pflichtenhefte / Schnittstellen-Dokus
├── 03_Analysis/                # Arbeitsbereich ("Workbench")
│   ├── User_Stories/           # Anforderungen aus Nutzersicht
│   ├── Scenarios/              # Konkrete Beispiele / "Geschichten"
│   └── Workshops/              # Protokolle aus Domain-Workshops
└── README.md                   # Einstiegspunkt & Navigationshilfe
```

### 2. Der Workflow (Information Lifecycle)

Informationen fließen von "unten nach oben" (von Analyse zu Core Model):

1. **Input:** Rohdaten (Regelwerke, Geschichten) landen in `02_Reference` oder `03_Analysis`.
2. **Destillation:** Der Domain Expert und der Architect analysieren diese Inputs.
3. **Output:** Das Ergebnis ist ein Eintrag im `01_Core_Model`. Nur was hier steht, darf implementiert werden.
4. **Glossar:** Jeder neue Begriff muss ins `00_Glossary.md`.

## Konsequenzen

### Positiv

* **Klarheit:** Entwickler schauen primär in `01_Core_Model`. Sie müssen nicht hunderte Seiten Regelwerk lesen.
* **Rückverfolgbarkeit:** Jede Entscheidung im Core Model kann auf eine Quelle in Reference oder Analysis verweisen.
* **Skalierbarkeit:** Neue Regelwerke (z.B. WBO) können als neuer Ordner in `Reference` ergänzt werden, ohne das Core
  Model sofort zu invalidieren.

### Negativ

* **Pflegeaufwand:** Informationen müssen aktiv "destilliert" und verschoben werden. Es darf kein "Data Dump" in
  `Analysis` verbleiben.
* **Disziplin:** Das Team muss widerstehen, direkt gegen `Reference`-Dokumente zu implementieren, da diese oft
  widersprüchlich oder zu detailreich sind.

## Status der Migration

Aktuell liegen viele Dateien flach in `docs/02_Domain` oder in `Reference`.
Eine Migration der bestehenden Dateien in diese neue Struktur ist erforderlich.
