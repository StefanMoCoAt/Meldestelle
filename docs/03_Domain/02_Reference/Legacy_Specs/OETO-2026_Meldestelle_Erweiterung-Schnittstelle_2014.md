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
Bewerbnummern &gt;= 100 erforderliche Feld „Bewerbnummer 3-stellig“ bei Verlinkung zur
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
Die XML-Erweiterungsdatei XXXXX.erg.xml
Grobstruktur der Erweiterungsdatei
Die äußere Struktur der XML-Erweiterung stellt sich wie folgt dar:
<?xml version='1.0' encoding='UTF-8'?>
<ErgebnisDatei 
  xmlns='http://www.turnier-meldestelle.com/oeps/schema' 
  TurnierNummer='XXXXX' 
 >
<Turnier LinkID='AAAAAAAA' TurnierNummer='XXXXX'/>
<BewerbListe>
<Bewerb LinkID='BBBBBBBB' Art='BewerbArt'>
:
:
</Bewerb>
<Bewerb LinkID='BBBBBBBB' Art='BewerbArt'>
:
:
</Bewerb>
:
:
</BewerbListe>
</ErgebnisDatei>
(Die Spezifizierung des xmlns-Attributes ist zwingend!)
Dabei werden folgende Variablen verwendet:
 XXXXX steht für die Turniernummer, die auch im Dateinamen verwendet wird und dient zur
Überprüfung. Die Redundant dieses Attributs durch Verwendung sowohl in der
ErgebnisDatei als auch im Turnier ist beabsichtigt. Bei Nicht-Übereinstimmung in
irgendeinem dieser Attribute wird der gesamte Inhalt der Datei ignoriert.  
 AAAAAAAA ist die achtstellige Link-Nummer des A-Satzes aus der Datei XXXXX.erg. Derzeit
sind (noch) keine Sub-Elemente zum Element Turnier definiert.  
 BBBBBBBB ist die achtstellige Link-Nummer eines B-Satzes aus der Datei XXXXX.erg. Die
Beschreibung weiterer Attribute und der Sub-Elemente folgt später.  
o <Bewerb>-Elemente mit einer LinkID, die nicht in XXXXX.erg definiert ist, werden
ignoriert.  
o Fehlende <Bewerb>-Elemente (LinkID in XXXXX.erg angegeben, aber in
XXXXX.erg.xml nicht vorhanden) führen dazu, dass zu diesem Bewerb keine
ergänzenden Information zum OEPS übertragen werden. Eine entsprechende
Fehlermeldung beim Import ins AX könnte hilfreich sein. Prinzipiell sollte dies aber
nicht vorkommen!  
o Sollten zwei oder mehr <Bewerb>-Elemente mir derselben LinkID-Werte
enthalten sein, werden alle betreffenden Elemente außer dem ersten ignoriert. Auch
hier wäre eine Fehlermeldung beim Importieren nützlich; seitens des Meldestellen-
Exports sollte dies nicht vorkommen, und deutet auf einen offensichtlichen
Implementierungsfehler des Meldestellenprogramms hin.  
Das Element <Turnier>
<Turnier> - Syntax
<Turnier LinkID='AAAAAAAA' TurnierNummer='XXXXX'/>
<Turnier> - Attribute
 LinkID: Die achtstellige Link-Nummer des A-Satzes aus der Datei XXXXX.erg.  
 TurnierNummer: Die fünfstellige OEPS-Turniernummer. Diese wird redundant auch im
Dateinamen und als Attribut im Elternelement ErgebnisDatei verwendet.  
<Turnier> - Elemente
Dieses Element dient als Platzhalter für zukünftige Erweiterungen enthalten und enthält derzeit
keine Elemente.  
Das Element <Bewerb>
<Bewerb> - Syntax
<Bewerb LinkID='BBBBBBBB' Art='BewerbArt'>
<RichterListe>
&lt;Richter … /&gt;
&lt;Richter … /&gt;
:
:
</RichterListe>
<ErgebnisListe>
:
:
</ErgebnisListe>
</Bewerb>
<Bewerb> - Attribute
 LinkID: Die achtstellige Link-Nummer des entsprechenden B-Satzes aus der Datei
XXXXX.erg.  
 Art: Die Art des Bewerbs. Mögliche Werte für dieses Attribut sind derzeit (die Liste wird für
künftige Anwendungen erweitert werden):
o Vielseitigkeit: Vielseitigkeitsprüfungen.  
o DressurGemeinsam: Gemeinsam gerichtete Dressur- und Dressurreiterprüfungen.  
o DressurGetrennt: Getrennt gerichtete Dressurprüfungen.  
o FreestyleGemeinsam: Gemeinsam gerichtete Kürprüfungen.  
o FreestyleGetrennt: Getrennt gerichtete Kürprüfungen.  
o DressurPferdeGemeinsam: Gemeinsam gerichtete Dressurpferdeprüfungen.  
<Bewerb> - Elemente
 RichterListe: Diese Liste enthält Informationen über die beim Bewerb eingesetzten
Offiziellen (Richter, Parcours- und Geländebau etc.).  
 ErgebnisListe: Die Resultate des Bewerbs. Name und Definition dieses Elements hängen
von der BewerbArt ab. Siehe weiter unten für die Beschreibung der verschiedenen
Ergebnisformate. ErgebnisListe steht für eines der folgenden Elemente:  
o ErgListeVS: Vielseitigkeitsprüfungen.  
o ErgListeDrGem: Gemeinsam gerichtete Dressur- und Dressurreiterprüfungen.  
o ErgListeDrGetr: Getrennt gerichtete Dressurprüfungen.  
o ErgListeFreestyleGem: Gemeinsam gerichtete Kürprüfungen.  
o ErgListeFreestyleGetr: Getrennt gerichtete Kürprüfungen.  
o ErgListeDrPferdGem: Gemeinsam gerichtete Dressurpferdeprüfungen.  
Das Element <Richter>
<Richter> - Syntax
<Richter  
  ID='OEPS-ID des Funktionärs' 
  Name='Name des Funktionärs' 
  Funktion='Aufgabe des Funktionärs im Bewerb' 
 />
<Richter> - Attribute
 ID: Die sechsstellige OEPS-Satznummer des Funktionärs oder 000000, falls es sich um einen
ausländischen Funktionär handelt.  
 Name: Der volle Name des Funktionärs, einschließlich seiner Nationalität.  
 Funktion: Die Aufgabe des Funktionärs im Bewerb.  
<Richter> - Elemente
Dieses Element enthält keine weiteren Elemente.  
ID und Status einer Ergebniszeile – Allgemein
Ergebniszeile – ID
Jede Ergebniszeile (XML-Element Result…) einer Ergebnisliste muss im Attribut LinkID den
achtstellige Wert des „LinkID“-Feldes der entsprechenden Ergebniszeile aus der Datei XXXXX.erg
enthalten.  
 Ergebniszeilen mit einer LinkID, die nicht in XXXXX.erg definiert ist, werden ignoriert.  
 Fehlende Ergebniszeilen (LinkID in XXXXX.erg angegeben, aber in XXXXX.erg.xml nicht
vorhanden) führen dazu, dass zu dieser Ergebniszeile keine ergänzenden Information zum
OEPS übertragen werden. Eine entsprechende Fehlermeldung beim Import ins AX könnte
hilfreich sein. Prinzipiell sollte dies aber nicht vorkommen!  
 Sollten zwei oder mehr Ergebniszeilen mir derselben LinkID enthalten sein, werden alle
betreffenden Elemente außer dem ersten ignoriert. Auch hier wäre eine Fehlermeldung beim
Importieren nützlich, da dies seitens des Meldestellen-Exports nicht vorkommen sollte, und
einen offensichtlichen Implementierungsfehler seitens des Meldestellenprogramms darstellt.  
Ergebniszeile – Status
Abgesehen von den unterschiedlichen numerischen Ergebnissen für einen Teilnehmer gibt es eine
Reihe von Statuswerten, welche den Fall einer vorzeitigen Beendigung einer Prüfung beschreiben.
Diese Werte sind in den unterschiedlichen Elementen einer jeden Ergebniszeile im Attribut Status
enthalten und entstammen folgender Aufzählung:  
 Norm: Die Prüfung wurde ordnungsgemäß beendet.  
 Ausg: Der Teilnehmer ist aus der Prüfung ausgeschieden.  
 Disq: Der Teilnehmer wurde in der Prüfung disqualifiziert.  
 Verz: Der Teilnehmer ist zur Prüfung angetreten, hat aber auf einen Start zu einer weiteren
Teilprüfung verzichtet (z.B. Vielseitigkeit).  
 NoRes: Der Teilnehmer hat kein Resultat zu verzeichnen.  
Werden die Ergebnisse eines Bewerbes aus mehreren Teilbewerben ermittelt, enthält auch jedes
dieser Teilergebnisse ein solches Status-Attribut (z.B. Vielseitigkeit, Cupwertungen etc.).  
BewerbArt und Ergebnisliste
Abhängig von der BewerbArt eines Bewerbes kommen unterschiedliche Definitionen der
Ergebnisliste zur Anwendung. Derzeit sind folgende Kombinationen definiert.
BewerbArt Ergebnisliste
Vielseitigkeit ErglisteVS
DressurGemeinsam ErgListeDrGem
DressurGetrennt ErgListeDrGetr
FreestyleGemeinsam ErgListeFreestyleGem
FreestyleGetrennt ErgListeFreestyleGetr
DressurPferdeGemeinsam ErgListeDrPferdGem
<ErgListeVS> - Ergebnisliste Vielseitigkeit
<ErgListeVS> - Syntax und Inhalt
<ErgListeVS>
&lt;ResultVielseitigkeit … &gt;
:
:
</ResultVielseitigkeit>
&lt;ResultVielseitigkeit … &gt;
:
:
</ResultVielseitigkeit>
:
:
</ErgListeVS>
<ResultVielseitigkeit> - Syntax und Inhalt
<ResultVielseitigkeit 
  LinkID='DDDDDDDD' 
  Dressur='Gesamtfehlerpunkte aus der Dressur' 
  Cross='Gesamtfehlerpunkte aus dem Gelände' 
  Springen='Gesamtfehlerpunkte aus dem Springen' 
  Total='Fehlerpunkte total' 
  Status='Ergebnisstatus des Starters' 
  Qualif='VS-Qualifikation' 
 >
<DetailDressur 
   Status='Ergebnisstatus des Starters, Teilprüfung Dressur' 
  >
<E-VS 
    Punkte='Punktesumme des Richters bei E' 
    Prozent='Prozentwert des Richters bei E' 
    PZ='Platzziffer der Wertung des Richters bei E' 
   />
<H-VS 
    Punkte='Punktesumme des Richters bei H' 
    Prozent='Prozentwert des Richters bei H' 
    PZ='Platzziffer der Wertung des Richters bei H' 
   />
<C-VS 
    Punkte='Punktesumme des Richters bei C' 
    Prozent='Prozentwert des Richters bei C' 
    PZ='Platzziffer der Wertung des Richters bei C' 
   />
<M-VS 
    Punkte='Punktesumme des Richters bei M' 
    Prozent='Prozentwert des Richters bei M' 
    PZ='Platzziffer der Wertung des Richters bei M' 
   />
<B-VS 
    Punkte='Punktesumme des Richters bei B' 
    Prozent='Prozentwert des Richters bei B' 
    PZ='Platzziffer der Wertung des Richters bei B' 
   />
</DetailDressur>
<DetailCross 
   Fehler='Hindernisfehler in der Teilprüfung Gelände' 
   Zeit='Gebrauchte Zeit für die Teilprüfung Gelände' 
   Zeitfehler='Zeitfehler in der Teilprüfung Gelände' 
   Status='Ergebnisstatus für die Teilprüfung Gelände' 
  />
<DetailSpringen 
   Fehler='Hindernisfehler in der Teilprüfung Springen' 
   Zeit='Gebrauchte Zeit für die Teilprüfung Springen' 
   Zeitfehler='Zeitfehler in der Teilprüfung Springen' 
   Status='Ergebnisstatus für die Teilprüfung Springen' 
  />
</ResultVielseitigkeit>
ACHTUNG: Die Bezeichner der Elemente lauten „E-VS“, H-VS“ etc. zur Unterscheidung von den
Elementen aus ResultDressurGetrennt).
Die Attribute und Elemente von ResultVielseitigkeit entsprechen den Detailergebnissen
aus dem Bewerb. Für nicht besetzte Richterpositionen in der Dressur entfällt das entsprechende
Sub-Element in DetailDressur (z.B. Vielseitigkeit mit nur zwei Richtern bei E und C),
Bezüglich LinkID und Status findet sich im Abschnitt „ID und Status einer Ergebniszeile“
Näheres.  
<ErgListeDrGem> - Ergebnisliste Dressur, gemeinsames Richten
<ErgListeDrGem> - Syntax und Inhalt
<ErgListeDrGem>
&lt;ResultDressurGemeinsam …/&gt;
&lt;ResultDressurGemeinsam …/&gt;
:
:
</ErgListeDrGem>
<ResultDressurGemeinsam> - Syntax und Inhalt
<ResultDressurGemeinsam 
  LinkID='DDDDDDDD' 
  Note='Endnote, einschließlich allfälliger Abzüge' 
  Status='Ergebnisstatus des Starters' 
 />
Die Attribute von ResultDressurGemeinsam entsprechen den Ergebnissen aus dem Bewerb.  
Bezüglich LinkID und Status findet sich im Abschnitt „ID und Status einer Ergebniszeile“
Näheres.  
<ErgListeDrGetr> - Ergebnisliste Dressur, getrenntes Richten
<ErgListeDrGetr> - Syntax und Inhalt
<ErgListeDrGetr>
&lt;ResultDressurGetrennt …&gt;
:
</ResultDressurGetrennt>
&lt;ResultDressurGetrennt …/&gt;
:
</ResultDressurGetrennt>
:
:
</ErgListeDrGetr>
<ResultDressurGetrennt> - Syntax und Inhalt
<ResultDressurGetrennt 
  LinkID='DDDDDDDD' 
  Punkte='Summe der Punktewertungen aller Richter' 
  Prozent='Den Punkten entsprechende Prozentzahl' 
  Status='Ergebnisstatus des Starters' 
 >
<E 
   Punkte='Punktesumme des Richters bei E' 
   Prozent='Den Punkten entsprechende Prozentzahl' 
   PZ='Platzziffer der Wertung des Richters bei E' 
  />
<H 
   Punkte='Punktesumme des Richters bei H' 
   Prozent='Den Punkten entsprechende Prozentzahl' 
   PZ='Platzziffer der Wertung des Richters bei H' 
  />
<C 
   Punkte='Punktesumme des Richters bei C' 
   Prozent='Den Punkten entsprechende Prozentzahl' 
   PZ='Platzziffer der Wertung des Richters bei C' 
  />
<M 
   Punkte='Punktesumme des Richters bei M' 
   Prozent='Den Punkten entsprechende Prozentzahl' 
   PZ='Platzziffer der Wertung des Richters bei M' 
  />
<B 
   Punkte='Punktesumme des Richters bei B' 
   Prozent='Den Punkten entsprechende Prozentzahl' 
   PZ='Platzziffer der Wertung des Richters bei B' 
  />
</ResultDressurGetrennt>
Die Attribute und Elemente von ResultDressurGetrennt entsprechen den Ergebnissen aus
dem Bewerb. Für nicht besetzte Richterpositionen (z.B. Richter bei H-C-B oder E-C-M) entfällt das
entsprechende Element.  
Bezüglich LinkID und Status findet sich im Abschnitt „ID und Status einer Ergebniszeile“
Näheres.  
<ErgListeFreestyleGem> - Ergebnisliste Kür, gemeinsames Richten
<ErgListeFreestyleGem> - Syntax und Inhalt
<ErgListeFreestyleGem>
&lt;ResultFreestyleGemeinsam …/&gt;
&lt;ResultFreestyleGemeinsam …/&gt;
:
:
</ErgListeFreestyleGem>
<ResultFreestyleGemeinsam> - Syntax und Inhalt
<ResultFreestyleGemeinsam 
  LinkID='DDDDDDDD' 
  Techn='Punkte für den technischen Inhalt' 
  Kunstl='Punkte für die künstlerische Ausführung' 
  Prozent='Endbewertung in Prozent' 
  Status='Ergebnisstatus des Starters' 
 />
Die Attribute und Elemente von ResultFreestyleGemeinsam entsprechen den Ergebnissen
aus dem Bewerb.  
Bezüglich LinkID und Status findet sich im Abschnitt „ID und Status einer Ergebniszeile“
Näheres.  
<ErgListeFreestyleGetr> - Ergebnisliste Kür, getrenntes Richten
<ErgListeFreestyleGetr> - Syntax und Inhalt
<ErgListeFreestyleGetr>
&lt;ResultFreestyleGetrennt …&gt;
:
</ResultFreestyleGetrennt>
&lt;ResultFreestyleGetrennt …&gt;
:
</ResultFreestyleGetrennt>
:
:
</ErgListeFreestyleGetr>
<ResultFreestyleGetrennt> - Syntax und Inhalt
<ResultFreestyleGetrennt 
  LinkID='DDDDDDDD' 
  Techn='Prozentwertung für den technischen Inhalt, gesamt' 
  Kunstl='Prozentwertung für die künstlerische Ausführung, gesamt' 
  Prozent='Endbewertung in Prozent' 
  Status='Ergebnisstatus des Starters' 
 >
<E-FS 
   Techn='Prozentwertung für den technischen Inhalt bei E' 
   Kunstl='Prozentwertung für die künstlerische Ausführung bei E' 
   Prozent='Gesamtbewertung des Richters bei E in Prozent' 
   PZ='Platzziffer der Wertung des Richters bei E' 
  />
<H-FS 
   Techn='Prozentwertung für den technischen Inhalt bei H' 
   Kunstl='Prozentwertung für die künstlerische Ausführung bei H' 
   Prozent='Gesamtbewertung des Richters bei H in Prozent' 
   PZ='Platzziffer der Wertung des Richters bei H' 
  />
<C-FS 
   Techn='Prozentwertung für den technischen Inhalt bei C' 
   Kunstl='Prozentwertung für die künstlerische Ausführung bei C' 
   Prozent='Gesamtbewertung des Richters bei C in Prozent' 
   PZ='Platzziffer der Wertung des Richters bei C' 
  />
<M-FS 
   Techn='Prozentwertung für den technischen Inhalt bei M' 
   Kunstl='Prozentwertung für die künstlerische Ausführung bei M' 
   Prozent='Gesamtbewertung des Richters bei M in Prozent' 
   PZ='Platzziffer der Wertung des Richters bei M' 
  />
<B-FS 
   Techn='Prozentwertung für den technischen Inhalt bei B' 
   Kunstl='Prozentwertung für die künstlerische Ausführung bei B' 
   Prozent='Gesamtbewertung des Richters bei B in Prozent' 
   PZ='Platzziffer der Wertung des Richters bei B' 
  />
</ResultFreestyleGetrennt>
ACHTUNG: Die Bezeichner der Elemente lauten „E-FS“, H-FS“ etc. zur Unterscheidung von den
Elementen aus ResultDressurGetrennt).
Die Attribute und Elemente von ResultFreestyleGetrennt entsprechen den Ergebnissen aus
dem Bewerb. Für nicht besetzte Richterpositionen (z.B. Richter bei H-C-B oder E-C-M) entfällt das
entsprechende Element.  
Bezüglich LinkID und Status findet sich im Abschnitt „ID und Status einer Ergebniszeile“
Näheres.  
<ErgListeDrPferdGem> - Ergebnisliste Dressurpferdeprüfung
<ErgListeDrPferdGem> - Syntax und Inhalt
<ErgListeDrPferdGem>
&lt;ResultDressurPferdGem …/&gt;
&lt;ResultDressurPferdGem …/&gt;
:
:
</ErgListeDrPferdGem>
<ResultDressurPferdGem> - Syntax und Inhalt
<ResultDressurPferdGem 
  LinkID='DDDDDDDD' 
  Schritt='Note für den Schritt' 
  Trab='Note für den Trab' 
  Galopp='Note für den Galopp' 
  Durchl='Note für die Durchlässigkeit' 
  GesEindr='Note für den Gesamteindruck' 
  Abzug='Allfällige Abzüge für Verreiten' 
  Total='Ergebnis Schritt+Trab+Galopp+Durchl+GesEindr-Abzug' 
  Status='Ergebnisstatus des Starters' 
 />
Die Attribute von ResultDressurPferdGem entsprechen den Ergebnissen aus dem Bewerb.  
Bezüglich LinkID und Status findet sich im Abschnitt „ID und Status einer Ergebniszeile“
Näheres.  
XSD Schema
Für die in diesem Dokument beschriebene XML-Syntax ist eine Sammlung von XSD Schema-Dateien
verfügbar, mit welcher Code zum Überprüfen und Einlesen der Datei XXXXX.erg.xml erzeugt werden
kann. Diese Sammlung kann für Erweiterungen auf andere Arten von Bewerben und auch andere
Sparten jederzeit erweitert werden, ohne dabei die Rückwärtskompatibilität zu gefährden.  
Die Basisdate des Schemasammlung ist Export2OEPS.xsd, alle übrigen sind vermittels
xs:include Elementen darin bereits enthalten.
Hinsichtlich der XML-Erweiterung ist Folgendes anzumerken:
 Die Reihenfolge der Attribute eines Elements ist nicht festgelegt; Es obliegt der
interpretierenden Applikation, bei der Darstellung der Ergebnisse für eine wohldefinierte
Struktur zu sorgen.  
 Die Reihenfolge der Sub-Elemente eines Elements innerhalb der einzelnen Zeilen der
Ergebnislisten (z.B. DetailDressur – DetailCross – DetailSpringen) ist nicht
festgelegt; Auch hier obliegt es der interpretierenden Applikation, bei der Darstellung der
Ergebnisse für eine wohldefinierte Struktur zu sorgen.  
Schlussbemerkung
Das vorliegende Dokument stellt einen ersten Schritt zur Erweiterung/Ersetzung des bestehenden
Übergabeprotokolls von den Meldestellen zum OEPS mit besonderem Augenmerk auf bis dato noch
nicht definierte Sparten dar. Die Ausführungen für Dressurergebnisse sollen zeigen, dass das neue
Übergabeformat bei geeigneter Erweiterung auf alle Sparten des OEPS anwendbar ist. Diejenigen
Daten, die mit der derzeit verwendeten Datei XXXXX.erg transportiert werden, können durchaus
ebenfalls in das XML-Format aufgenommen werden. Dies würde auf längere Sicht die veraltete
Methode der Datenübergabe obsolet machen und gleichzeitig einen geordneten und
wohldefinierten Übergang auf das neue Format erlauben.
