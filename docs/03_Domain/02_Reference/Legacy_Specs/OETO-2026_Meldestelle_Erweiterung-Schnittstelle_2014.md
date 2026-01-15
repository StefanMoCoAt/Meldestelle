# Erweiterung der Ergebnisschnittstelle 2014

**Kontext:** Dieses Dokument ist eine historische, technische Spezifikation für eine XML-basierte Erweiterung der Ergebnisschnittstelle zwischen Meldestellen und dem OEPS. Es wird hier als Referenz für das Legacy-System archiviert.

---

```xml
<?xml version='1.0' encoding='UTF-8'?>
<!--
Erweiterung der Ergebnisschnittstelle
zwischen den Meldestellen und dem OEPS
Herbert Marchl, 18. Juni 2014
Umfasst Vielseitigkeit, Dressur
Update 2. Juli 2014:
 Neues Attribut „PZ“ für die Platzziffern der einzelnen Richter bei getrennt gerichteten Dressur- und
Kürprüfungen.
 Neues Attribut „Prozent“ für das Gesamtergebnis pro Richter bei getrennt gerichteten
Kürprüfungen.
 Umbenennen der Ergebnisse der einzelnen Richter von „E-VS“, „H-VS“ etc. zu „E-FS“, „H-FS“ etc.
 Neudefinition des Elements DetailDressur bei Vielseitigkeitsprüfungen:
o Einführung der Richterpunkte, Richterprozente und Platzziffern als Attribute „Punkte“,
„Prozent“ bzw. „PZ“ der Sub-Elemente „E-VS“, „H-VS“, „C-VS“, „M-VS“ und „B-VS“.
o Entfernen der bisherigen Attribute mit den Richterpunkten.
o Ein neues Attribut, „Punkte“, enthält die Gesamt-Punktesumme aller Richter aus dem
Teilbewerb Dressur.
-->
<ErgebnisDatei
  xmlns='http://www.turnier-meldestelle.com/oeps/schema'
  TurnierNummer='XXXXX'
 >
<Turnier LinkID='AAAAAAAA' TurnierNummer='XXXXX'/>
<BewerbListe>
<Bewerb LinkID='BBBBBBBB' Art='BewerbArt'>
<RichterListe>
<Richter ID='OEPS-ID' Name='Name' Funktion='Aufgabe'/>
</RichterListe>
<ErgebnisListe>
<!-- Je nach BewerbArt: ErgListeVS, ErgListeDrGem, etc. -->
</ErgebnisListe>
</Bewerb>
</BewerbListe>
</ErgebnisDatei>

<!-- ##################################################################### -->
<!-- #                         DETAIL DEFINITIONEN                       # -->
<!-- ##################################################################### -->

<!--

Einleitung
Die Erweiterung deckt in der derzeitigen Fassung die Sparten Vielseitigkeit und Dressur ab. Im
ursprünglichen Übergabeformat war die Vielseitigkeit noch nicht enthalten. Ziel für alle Sparten ist
es, neben dem Gesamtergebnis der Teilnehmer auch Detailergebnisse übermitteln zu können, für die
Vielseitigkeit insbesondere die Ergebnisse der Teilbewerbe Dressur, Gelände und Springen.
Insgesamt stellt die vorliegende Spezifikation einen Ansatz dar, das alte Format schrittweise durch
ein neues, flexibleres zu ersetzen.
Die in diesem Dokument vorgestellte Struktur hat als primäres Ziel, eine unter Einhaltung von
Rückwärtskompatibilität leicht erweiterbare Definition der Ergebnisdaten zu präsentieren. Dies trifft
sowohl für die Vielseitigkeit als auch für die Anwendbarkeit auf andere Sparten – im vorliegenden
Dokument auf verschiedene Dressurbewerbe – zu.
Übersicht
Die in diesem Dokument genannten Erweiterungen werden in einer zusätzlichen Datei
untergebracht, sodass die Übergabe von den Meldestellen zum OEPS ab Einführung der Erweiterung
zwei Dateien umfasst:
 XXXXX.erg: Die bereits bekannte und im OEPS-Dokument „Ergebnisschnittstelle Pflichtenheft
2012“ beschriebene Datei, erweitert um die in diesem Dokument beschriebenen
Ergänzungen.
 XXXXX.erg.xml: Eine zusätzliche Datei in XML-Syntax, welche die zusätzlichen Informationen
enthält. Als Zeichensatz für diese Datei wird UTF-8 vereinbart. In dieser Datei sind
numerische Resultate im Textformat mit der vom OEPS geforderten Anzahl an Dezimalen
anzugeben. Als Dezimaltrennzeichen ist das Komma zu verwenden, Tausendertrennzeichen
sind nicht erlaubt.
XXXXX steht hier für die fünfstellige Turniernummer des Turniers, dessen Ergebnisse übermittelt
werden soll.
Ergänzungen zu XXXXX.erg
An den A-, B- und D-Satz der ursprünglichen Ergebnisdatei wird jeweils ein achtstelliges numerisches
Feld („LinkID“) angefügt, welches dazu dient, diese Datensätze mit entsprechenden Abschnitten in
der neuen Datei XXXXX.erg.xml zu verlinken. Der Wertebereich für dieses Feld ist 00000001-
99999999 („00000000“ wird als „nicht vorhanden“ interpretiert). Dadurch ergeben sich folgende
Satzlängen für diese Datensätze:
 A-Satz: 95 Zeichen (alt, ohne Verlinkung zur XML-Erweiterung) oder 103 Zeichen (neu, mit
Verlinkung zur XML- Erweiterung).
 B-Satz: Hier ist zu berücksichtigen, dass das in der bestehenden Ergebnisschnittstelle nur für
Bewerbnummern >= 100 erforderliche Feld „Bewerbnummer 3-stellig“ bei Verlinkung zur
XML- Erweiterung ab sofort verpflichtend immer befüllt sein muss! Folgende Längen sind
daher möglich:
o 60 Zeichen: Alte Version ohne dreistellige Bewerbnummern und ohne Verlinkung zur
XML- Erweiterung.
o 63 Zeichen: Version 2.2 mit dreistelligen Bewerbnummern, aber ohne Verlinkung zur
XML- Erweiterung.
o 71 Zeichen: Neue Version mit obligatorischen dreistelligen Bewerbnummern und
Verlinkung zur XML- Erweiterung.
 D-Satz: 165 Zeichen (alt, ohne Verlinkung zur XML- Erweiterung) oder 173 Zeichen (neu, mit
Verlinkung zur XML- Erweiterung).
Die Werte des neuen Feldes „LinkID“ müssen in der gesamten Ergebnisdatei, also auch zwischen den
A-, B- und D-Sätzen, eindeutig sein. Andere Einschränkungen für die Werte der „LinkID“-Felder
bestehen nicht, diese können vom Meldestellenprogramm beliebig gesetzt werden.
Besonders zu beachten ist, dass die Definition des Zeichensatzes der ursprünglichen Datei (ASCII,
Codepage 850) nicht verändert wird!
Näheres zur Technik der Verlinkung findet sich im Abschnitt über die XML-Erweiterung.

-->

<!-- ... (Rest des Originaldokuments als Kommentar belassen oder bei Bedarf extrahieren) ... -->
```
