# Struktur eines Meisterschafts- oder Cup-Reglements (ÖTO/OEPS)

Ein Reglement dient als technisches Lastenheft für die Ermittlung eines Gesamtsiegers über mehrere Teilprüfungen hinweg.

## 1. Präambel & Geltungsbereich
* **Name des Cups/der Meisterschaft:** Eindeutige Bezeichnung (z.B. "ASVÖ Landescup 2025").
* **Geltungsbereich:** Hinweis auf die ÖTO und die spezifischen Sonderbestimmungen des LFV/OEPS.
* **Veranstalter:** Wer ist der Träger der Serie (z.B. Referat Springen des LFV Niederösterreich).

## 2. Teilnahmeberechtigung (Eligibility)
Dies ist entscheidend für die Filterlogik im **Nennungs_Context**.

* **Personen:** * Erforderliche Stammmitgliedschaft (Landesverband/Verein).
  * Erforderliche Lizenzklasse (z.B. "nur R1-Reiter" oder "RD1 bis RD2").
  * Altersklasse (z.B. Junioren, Oldies).
* **Pferde:** * Mindestalter des Pferdes.
  * Ausschlusskriterien (z.B. Pferde, die in den letzten 2 Jahren in Klasse S platziert waren).
* **Paar-Bindung:** Festlegung, ob die Punkte an das Paar (Reiter + Pferd) oder nur an den Reiter gebunden sind.

## 3. Qualifikation & Wertungsprüfungen
Basis für die Verknüpfung im **Veranstaltungs_Context**.

* **Termine:** Liste der Turniere, an denen Wertungspunkte gesammelt werden können.
* **Pflichtteilnahme:** Muss man an allen Turnieren teilnehmen oder gibt es eine Mindestanzahl?
* **Prüfungsdefinition:** Genaue Angabe der Bewerbe (z.B. "Alle Prüfungen der Klasse LM über 125cm").

## 4. Punktesystem & Berechnungsmodus
Das Herzstück für den **Ergebnis_Context**.

* **Wertungsmodus:**
  * **Addition der Ergebnisse:** (Häufig bei Meisterschaften) Ergebnisse aus 2-3 Teilprüfungen werden addiert (Fehler/Zeit oder Prozent).
  * **Punktsystem:** (Häufig bei Cups) Vergabe von Fixpunkten nach Platzierung (z.B. 1. Platz = 100 Pkt, 2. Platz = 98 Pkt...).
* **Faktoren:** Haben bestimmte Prüfungen ein höheres Gewicht? (z.B. Finale zählt 1,5-fach).
* **Streichresultate:** Wie viele der schlechtesten Ergebnisse werden am Ende der Serie abgezogen?
* **Nicht-Antreten/Ausschluss:** Wie viele Punkte erhält ein Reiter, der ausscheidet (z.B. 0 Punkte oder "Ausgeschieden"-Status)?

## 5. Ex-aequo-Regelung (Gleichstand)
Essenziell für die automatisierte Ranglistenerstellung.

* **Regel bei Punktgleichheit:**
  * Wer hat das bessere Ergebnis im Finale erzielt?
  * Wer hat mehr Siege in der Gesamten Serie?
  * (In der Dressur): Wer hat die höhere Wertnote in der letzten Teilprüfung?

## 6. Das Finale
* **Teilnahmevoraussetzung:** Wer darf im Finale starten (z.B. Top 15 der Vorwertungen)?
* **Startreihenfolge:** Meist in umgekehrter Reihenfolge des Zwischenstandes (Spannungsaufbau).

## 7. Preise & Ehrungen
* **Titel:** Vergabe von Schärpen, Medaillen oder Pokalen.
* **Ehrengaben:** Decken, Stallplaketten.
* **Preisgeld:** Verteilungsschlüssel für die Gesamtwertung (oft getrennt von den Einzelprüfungen).

## 8. Proteste & Sonderfälle
* Fristen für Einsprüche gegen den Zwischenstand.
* Regelung bei Turnierabsagen oder Höherer Gewalt.
