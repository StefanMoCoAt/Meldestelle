# Das Kern-Modell (Core Model)

Dieses Verzeichnis ist die "Single Source of Truth" für das destillierte, fachliche Wissen des Projekts. Nur was hier beschrieben ist, gilt als vereinbarte Wahrheit für die Implementierung.

## Struktur

*   `Entities/`: Beschreibt die zentralen fachlichen Entitäten des Systems (z.B. Event, Turnier, Akteur).
*   `Processes/`: Dokumentiert die wichtigsten fachlichen Prozesse und Abläufe (z.B. Nennungsprozess, Ergebniserfassung).
*   `Rules/`: Definiert explizite Geschäftsregeln und Validierungen.

## Workflow

Informationen in diesem Verzeichnis sind das Ergebnis der Analyse von externen Quellen (siehe `../02_Reference`) und Workshops (siehe `../03_Analysis`).
Jede Änderung am Core Model sollte nachvollziehbar und idealerweise durch ein ADR gestützt sein.
