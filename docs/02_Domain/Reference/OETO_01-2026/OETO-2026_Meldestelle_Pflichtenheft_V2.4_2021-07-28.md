Pflichtenheft 2021 V2.4 Seite 1 von 10

Österreichischer Pferdesportverband
FEDERATION EQUESTRE NATI ONALE D’AUTRICHE

PFLICHTENHEFT 2021
Datentransfer OEPS - Meldestellen - OEPS

Version 2.4

ASCII-File Codepage 850

Dateien:

Richter und Parcoursbauer RICHT01.dat
Lizenzen LIZENZ01.dat
Pferde PFERDE01.dat
Vereine VEREIN01.dat

Diese vier Dateien sind in einer komprimierten Datei zns.zip zusammengefasst. Darüber hinaus können dort
noch andere, spartenspezifische Dateien enthalten sein, deren Inhalt an anderer Stelle zu beschreiben ist.

Nennungen n2-<Turniernummer>.dat

Ergebnisse <Turniernummer>.erg

Satzaufbau in allen Dateien: Entsprechend dem Inhalt des vorliegenden Dokuments.

Der Veranstalter ist verpflichtet die übermittelten Daten gemäß Datenschutz vertraulich
zu behandeln.

Nach dem Turnier sind die Ergebnisse an den OEPS zu übermitteln.

Österreichischer Pferdesportverband, Am Wassersprung 2, 2361 Laxenburg, Austria  
Telefon: +43 (01) 2236 710600 Fax: +43 (01) 2236 710600-99,  
E-Mail: office@oeps.at, Web: www.oeps.at
ZVR-Nummer 372 069 468

28.07.2021 ÖSTERREICHISCHER PFERDESPORTVERBAN D www.oeps.at
FEDERATION EQUESTRE NATIONALE D’AUTRICHE

Pflichtenheft 2021 V2.4 Seite 2 von 10

Details zum Pflichtenheft 2021 V2.4
Die vorliegende Version behebt ein Problem mit der Unterscheidung zwischen Österreichischen und
ausländischen Pferden bei der Ergebnisübergabe von Bewerben auf internationalen Turnieren. Dazu
wurde im D-Satz der Inhalt des Feldes „KOPFNUMMER“ genauer spezifiziert (siehe dort in rot).  
Sollten die Definitionen dieses Dokuments sich allfälligen Sondervereinbarungen für die Rückmeldung von
Ergebnissen widersprechen, ist es unter korrekter Angabe der Versionsnummer des verwendeten
Protokolls auch weiterhin möglich die Versionen 2.2 bzw. 2.3 zu verwenden.  
Für die Rückmeldung von Ergebnissen aus internationalen Bewerben (Dressur, Springen, Vielseitigkeit)
muss die vorliegende Version 2.4 verwendet werden.

28.07.2021 ÖSTERREICHISCHER PFERDESPORTVERBAND www.oeps.at
FEDERATION EQUESTRE NATIONALE D’AUTRICHE

Pflichtenheft 2021 V2.4 Seite 3 von 10
Details zum Pflichtenheft 2021 V2.3
Zur eindeutigen Identifizierung von Pferden wird künftig die eindeutige Satznummer aus der OEPS-
Datenbank verwendet, die in Datensätze, die Pferde betreffen, in das Protokoll aufgenommen wurde.  
Die in der Ergebnisdatei an den OEPS rückübermittelten Satznummern müssen mit den vom OEPS
erhaltenen übereinstimmen. Bei Pferden, für die im Zuge eines Turniers ein neuer Datensatz angelegt
wird, muss in der Ergebnisdatei ein Wert bestehend aus 10 Blanks eingetragen werden.  
Zu beachten ist, dass die Formate der Pferdedaten im PPFERDELISTE-Satz der Nenndatei und in der
Datei PFERDE01.dat nicht identisch sind: Die Information über die ID des FEI-Passes bzw. die FEI
Registrierungs-ID steht an unterschiedlichen Stellen!  
Bei den Reiterdaten wurde die Altersklasse U25 nachgetragen, die bereits seit einiger Zeit
(undokumentiert) implementiert war. Dadurch ergibt sich aber keine Formatänderung. Zu beachten ist,
dass sich die Altersklassen auch überschneiden!  
Einige erklärende Zusätze wurden ergänzt, ohne den sachlichen Inhalt des Dokuments zu verändern.  
Die roten Änderungsmarkierungen sowie die Anmerkungen zur Version 2.2 wurden entfernt. Wohl aber
enthält das Dokument die vollständige Änderungsbeschreibung der Version 2.2.  
Weiters wurden in einigen Datensätze der Ergebnisdatei die bereits seit 2014 verwendeten optionalen
LinkIDs aus der Schnittstellenerweiterung nachgetragen.  
Ab 1.1.2021 folgen alle Dateien, die vom OEPS an die Meldestellen gehen, der Spezifikation 2.3. Alle
Ergebnisdateien, die aus den Meldestellen an den OEPS gehen, sind ab diesem Zeitpunkt gemäß 2.3 zu
erstellen; insbesondere ist die neue Satznummer der Pferde korrekt anzugeben.

Änderung im File n2-xxxxx.dat (Nenndaten) Stelle Länge

PPFERDELISTE – Satz (neuer Eintrag)
• SATZNUMMER DES PFERDES numerisch (10) 202 10
• Format 0000000000
Die zehnstellige Pferde-ID aus dem OEPS-Datenbestand

KKARTEI – Satz (neuer Eintrag)
• SATZNUMMER DES PFERDES numerisch (10) 247 10
• Format 0000000000
Die zehnstellige Pferde-ID aus dem OEPS-Datenbestand

Neues Feld im File PFERDE01.dat (zns.zip) Stelle Länge

• SATZNUMMER DES PFERDES numerisch (10) 202 10
• Format 0000000000
Die zehnstellige Pferde-ID aus dem OEPS-Datenbestand

• Änderung im File xxxxx.erg (Ergebnisse)

D-SATZ (neuer Eintrag)
• SATZNUMMER DES PFERDES numerisch (10) 166 10
• Format 0000000000
Die zehnstellige Pferde-ID aus dem OEPS-Datenbestand

28.07.2021 ÖSTERREICHISCHER PFERDESPORTVERBAND www.oeps.at
FEDERATION EQUESTRE NATIONALE D’AUTRICHE

Pflichtenheft 2021 V2.4 Seite 4 von 10

Details zum Pflichtenheft 2012 V2.2

o Für Meldestellen, die wie bisher Turniere mit weniger als 100 Bewerben betreuen, ergeben
sich keine Änderungen. Es kann ohne Probleme mit der Version 2.2 (2011) und dem
Meldestellenprogramm wie bisher gearbeitet werden oder das Update auf die Version 2012
durchgeführt werden.

o Meldestellen die Turniere mit 100 und mehr Bewerben durchführen MÜSSEN das Update auf
die Version 2012 V2.2 in ihrem Programm durchführen. Dabei ist nur an der Stelle 61 im B-
Satz der xxxxx.erg die Ausgabe der 3-stelligen Bewerbnummer erforderlich.

Änderung im File n2-xxxxx.dat (Nenndaten) Stelle Anm.

BBEWERBE – SATZ (neuer Eintrag)
• BEWERBNUMMER 3-stellig numerisch (3) 60
• Format 000
Hier werden die Bewerbe 3-stellig mit Filler „0“ ausgegeben

• Im bisherigen Feld Bewerbe an der Stelle 2(2) werden alle 2-stelligen Bewerbe angegeben. Bei
einer Bewerbnummer von 100 und höher wird der Wert auf „00“ gesetzt. An der Stelle 60(3) werden
immer die Bewerbe 3-stellig ausgegeben.

BBEWERBE

010Dressurprüfung L CDN-A\* 20110122001
070Dressurprüfung A CDN-B 20110122007
120Dressurprüfung L CDN-A\* 20110122012
000Dressurprüfung A CDN-B 20110122100
000Dressurprüfung A CDN-B 20110122101
Info: Bei Turnieren mit 100 und mehr Bewerben kann die Bewerbnummer an der Stelle 60(3) ausgelesen werden.

• Änderung im File XXXXX.erg (Ergebnisse)

B-SATZ (neuer Eintrag)  
• BEWERBE 3-stellig numerisch (3) 61
• Format 000
Hier werden immer die Bewerbe 3-stellig, mit führenden Nullen ausgegeben

• Update für Turniere mit 100 Bewerben und mehr

• Das Feld 61(3) muss immer mit der 3-stelligen Bewerbnummer gefüllt werden (z.B.: 067, 004, 102)!

B020Dressurprüfung, Aufgabe A2 A CDN-B 012000000002
B140Dressurprüfung, Aufgabe A4 A CDN-B 012000000014
B220Dressurprüfung, Aufgabe L5 L CDN-B 012000000022
B000Dressurprüfung, Aufgabe A2 A CDN-B 012000000100
B010Dressurprüfung, Aufgabe A4 A CDN-B 012000000101
B020Dressurprüfung, Aufgabe L5 L CDN-B 012000000102
Der Eintrag (100-Stelle abgeschnitten) ist ohne Bedeutung, die Bewerbnummer wird beim OEPS gemäß 61(3) gelesen.
Hier sind programmtechnisch keine Änderungen erforderlich.

Info: Beim OEPS wird immer nach Bewerben an der Stelle 61(3) gesucht. Ist dieser Eintrag nicht vorhanden oder „000“ werden
die Bewerbnummern von 2(2) übernommen. Dadurch können die Ergebnisdateien nach der Version 2.2 (2011 oder 2012)
eingelesen werden.
28.07.2021 ÖSTERREICHISCHER PFERDESPORTVERBAND www.oeps.at
FEDERATION EQUESTRE NATIONALE D’AUTRICHE

Pflichtenheft 2021 V2.4 Seite 5 von 10

Info für die Meldestellen in der LIZENZ01.dat:
• An der Stelle 201(10) werden nur die bezahlten Lizenzen (Startkarten) ausgegeben.

z.B.: RD1,F1 beide Lizenzen bezahlt.
RD1,W
<BLANK> Nicht bez. Lizenzen werden nicht angezeigt

• Ist eine zusätzliche Information, da Teilnehmer a u c h mehrere Lizenzen bzw. Startkarten haben
können. Aus dem Eintrag <LETZTE ZAHLUNG> (z.B. 2011) ist kein Rückschluss auf die tatsächlich
bezahlte Lizenz möglich.

Pferde in der PFERDE01.dat
• Pferde für die länger als 3 Jahre keine Pferdegebühr bezahlt wurde werden nicht angegeben. Der
Pferdepass ist dem OEPS vorzulegen – neue Registrierung.

Allgemeine Hinweise zum D-Satz:

• Kopfnummer in der XXXXX.erg

• Bei internationalen Turnieren ist hier bei Pferden, die beim OEPS registriert sind, die
Kopfnummer lt. Registrierung einzutragen!

• Nicht beim OEPS registrierte Pferde erhalten bei nationalen Turnieren eine Z-Nummer.

• Satznummer und Nationalität in der XXXXX.erg

• Wenn ein ausl. Reiter mit österr. Lizenz die Lizenz bezahlt hat, startet er immer für seinen
österreichischen Verein. Im Ergebnisfile ist „AUT“ einzutragen.

• Wenn ein ausl. Reiter die Lizenz nicht bezahlt hat und auch nicht bezahlen möchte (z.B. bei
der Meldestelle) ist er nicht startberechtigt.

• Wenn ein ausl. Reiter mit österr. Lizenz kein Mitglied ist, startet er immer als Gastreiter.

• Wenn auf der Sperrliste eine Forderung „€ xx,xx“ aufscheint, ist der Teilnehmer nicht
startberechtigt. Die Forderung ist zu bezahlen. Sonst ist kein Start möglich.
28.07.2021 ÖSTERREICHISCHER PFERDESPORTVERBAND www.oeps.at
FEDERATION EQUESTRE NATIONALE D’AUTRICHE

Pflichtenheft 2021 V2.4 Seite 6 von 10

n2-XXXXX.dat (Nenndaten)

• TURNIERBEZEICHNUNG
A-SATZ Stelle Länge
ID 1 1 Alphanumerisch (1) WERT “A”
TURNIERNR 2 5 Numerisch 5 Stellen
TURNIERNAME.ORT 7 25 Alphanumerisch (25).  
VON 32 8 Datum FORMAT: JJJJMMTT
BIS 40 8 Datum FORMAT: JJJJMMTT
KATEGORIE 48 25 Alphanumerisch (25).

• GENANNTE TEILNEHMER
Kopfzeile “RREITERLISTE” Stelle Länge
SATZNUMMER DES REITERS 1 6 Alphanumerisch (6) FORMAT: 000000
FAMILIENNAME 7 50 Alphanumerisch (50).  
VORNAME 57 25 Alphanumerisch (25).  
BUNDESLAND 82 2 Numerisch ANMERKUNG ZU BUNDESLAND: 01
Wien, 02 NÖ, 03 Burgenland, 04
Steiermark, 05 Kärnten, 06 OÖ, 07
Salzburg, 08 Tirol, 09 Vorarlberg, 00
Unbekannt
VEREINSNAME 84 50 Alphanumerisch (50).  
NATIONALITÄT (Staatsbürgerschaft) 134 3 Alphanumerisch (3).  
REITERLIZENZ 137 4 Alphanumerisch (4). Keine Reiterlizenz: BLANK
STARTKARTE 141 1 Alphanumerisch (1) Keine Startkarte: BLANK
FAHRLIZENZ 142 2 Alphanumerisch (2). Keine Fahrlizenz: BLANK
ALTERSKLASSE JUGEND/JUNIOR/U25 144 2 Alphanumerisch (2). WERTE: Standard: BLANK
JG=Jugendlicher, JR=Junior, 25= U25
ALTERSKLASSE JUNGER-REITER 146 1 Alphanumerisch (1) WERTE: Standard: BLANK Y=Junger
Reiter
MITGLIEDSNUMMER 147 8 Numerisch FORMAT: 99999999, kein Mitglied:
00000000
TELEFONNUMMER 155 21 Alphanumerisch (21). Standard: BLANK
KADER 176 1 Alphanumerisch (1) Standard: BLANK, K=Kaderreiter
JAHR (letzte Zahlung) 177 4 Numerisch FORMAT: 9999
GESCHLECHT 181 1 Alphanumerisch (1) Werte: “W”, “M”
GEBURTSDATUM 182 8 Datum FORMAT: JJJJMMTT
FEI-ID 190 10 Alphanumerisch (10). Standard: BLANK
SPERRLISTE 200 1 Alphanumerisch (1) Werte: BLANK = nicht auf Sperrliste,
“S” = Info für auf Sperrliste nachsehen!

• BEWERBE
Kopfzeile - “BBEWERBE” Stelle Länge
ID 1 1 Alphanumerisch. WERT: BLANK
BEWERBNUMMER 2 2 Numerisch 2 Stellen, bei BewNr &gt;99 Wert “00”
ABTEILUNG 4 1 Numerisch WERTE: 0 = keine Abteilung, 1 = 1.
Abteilung etc.
BEWERBNAME 5 35 Alphanumerisch (35).  
KLASSE 40 4 Alphanumerisch (4)  
KATEGORIE 44 8 Alphanumerisch (8)

DATUM 52 8 Datum FORMAT: JJJJMMTT
BEWERBNUMMER 3-stellig 60 3 Numerisch Format: 999

28.07.2021 ÖSTERREICHISCHER PFERDESPORTVERBAND www.oeps.at
FEDERATION EQUESTRE NATIONALE D’AUTRICHE

Pflichtenheft 2021 V2.4 Seite 7 von 10

• GENANNTE PFERDE

Kopfzeile - “PPFERDELISTE” Stelle Länge
KOPFNR 1 4 Alphanumerisch (4).  
PFERDENAME 5 30 Alphanumerisch (30).  
LEBENSNUMMER 35 9 Numerisch FORMAT: 999999999
GESCHLECHT 44 1 Alphanumerisch (1)  
GEBURTSJAHR 45 4 Numerisch FORMAT: 9999
FARBE 49 15 Alphanumerisch (15).  
ABSTAMMUNG 64 15 Alphanumerisch (15).  
FEI-PASS 79 10 Alphanumerisch (10). Standard: BLANK
VEREIN (Nummer) 89 4 Numerisch FORMAT: 9999
JAHR (letzte Zahlung) 93 4 Numerisch FORMAT: 9999
VERANTWORTLICHE PERSON 97 75 Alphanumerisch (75). Familienname, Vorname
VATER 172 30 Alphanumerisch (30) Standard: BLANK
SATZNUMMER DES PFERDES 202 10 Alphanumerisch (10) FORMAT: 0000000000, eventuell mit
vorlaufenden Blanks

• NENNUNG JE PFERD

Kopfzeile “KKARTEI” Stelle Länge
KOPFNR 1 4 Alphanumerisch (4).  
PFERDENAME 5 30 Alphanumerisch (30).  
SATZNUMMER DES REITERS 35 6 Alphanumerisch (6). FORMAT: 000000
REITER 41 75 Alphanumerisch (75). Familienname Vorname
SPERRE (Code) 116 2 Numerisch WERTE: siehe Nennliste. Der höchste
Fehlercode, der bei den Bewerben
aufgetreten ist, wird hier eingetragen.
Teilnahmeberechtigung kontrollieren!
ACCONTO 118 5 Numerisch 3 ganze Stellen, 2 Dezimalstellen, ohne
Kommazeichen. BETRAG DER MIT
VERANSTALTER VERRECHNET
WIRD
STALL 123 1 Numerisch WERTE: 0 = kein Stall, 2 = Box bestellt.
Pro Pferd nur eine Box.
GENANNTE BEWERBE 124 37 Alphanumerisch (37). Bewerbe getrennt durch “,”
Bewerbnummer, ohne vorlaufende
Nullen, ohne Abteilungsnummer
BEZAHLT 161 5 Numerisch 3 ganze Stellen, 2 Dezimalstellen, ohne
Kommazeichen. BETRAG DER VOM
NENNER EINBEZAHLT WURDE (zu
Kontrollzwecken)
ERSATZREITER Satznummer 166 6 Alphanumerisch (6) Standard: BLANK
ERSATZREITER Familienname, Vorname 172 75 Alphanumerisch (75) Standard: BLANK
SATZNUMMER DES PFERDES 247 10 Alphanumerisch (10) FORMAT: 0000000000, eventuell mit
vorlaufenden Blanks

RICHT01.DAT
• LISTE DER RICHTER
X-SATZ
Stelle Länge
ID 1 1 Alphanumerisch WERT “X”
SATZNUMMER 2 6 Numerisch FORMAT: 999999
NAME 8 75 Alphanumerisch (75). Familienname, Vorname
QUALIFIKATIONEN 83 30 Alphanumerisch (30). Qualifikationen getrennt durch “,”. Ohne
Filler, daher kann die Zeile kürzer als
113 Zeichen sein.
28.07.2021 ÖSTERREICHISCHER PFERDESPORTVERBAND www.oeps.at
FEDERATION EQUESTRE NATIONALE D’AUTRICHE

Pflichtenheft 2021 V2.4 Seite 8 von 10

• LISTE DER PARCOURSBAUER
Y-SATZ Stelle Länge
ID 1 1 Alphanumerisch WERT “Y”
SATZNUMMER 2 6 Numerisch FORMAT: 999999
NAME 8 75 Alphanumerisch (75). Familienname, Vorname
QUALIFIKATIONEN 83 30 Alphanumerisch (30). Qualifikationen getrennt durch “,”. Ohne
Filler, daher kann die Zeile kürzer als
113 Zeichen sein.

PFERDE01.DAT
• LISTE DER PFERDE
Stelle Länge

KOPFNR 1 4 Alphanumerisch (4)  
PFERDENAME 5 30 Alphanumerisch (30)  
LEBENSNUMMER 35 9 Numerisch FORMAT: 999999999, Vorsicht: Bei
ausl. Pferden wird eine zufällige
Systemnummer generiert. Ist daher
keine gültige Lebensnummer. Nicht für
Pferdesuche verwenden!
GESCHLECHT 44 1 Alphanumerisch (1)  
GEB.JAHR 45 4 Numerisch FORMAT: 9999
FARBE 49 15 Alphanumerisch (15)  
ABSTAMMUNG 64 15 Alphanumerisch (15)  
VEREIN (Nummer) 79 4 Numerisch FORMAT: 9999
JAHR (letzte Zahlung) 83 4 Numerisch FORMAT: 9999
VERANTWORTLICHE PERSON 87 75 Alphanumerisch (75) Standard: BLANK
VATER 162 30 Alphanumerisch (30) Standard: BLANK
FEI-PASS 192 10 Alphanumerisch (10) Standard: BLANK
SATZNUMMER DES PFERDES 202 10 Alphanumerisch (10) FORMAT: 0000000000, eventuell mit
vorlaufenden Blanks

LIZENZ01.DAT
• LISTE DER LIZENZNEHMER
Stelle Länge
SATZNUMMER DES REITERS 1 6 Alphanumerisch (6) FORMAT: 000000
FAMILIENNAME 7 50 Alphanumerisch (50)  
VORNAME 57 25 Alphanumerisch (25)  
BUNDESLAND 82 2 Numerisch FORMAT: 99
VEREINSNAME 84 50 Alphanumerisch (50)  
NATIONALITÄT (Staatsbürgerschaft) 134 3 Alphanumerisch (3)  
REITERLIZENZ 137 4 Alphanumerisch (4) Keine Lizenz: BLANK
STARTKARTE 141 1 Alphanumerisch (1) Keine Startkarte: BLANK
FAHRLIZENZ 142 2 Alphanumerisch (2) Keine Fahrlizenz: BLANK

ALTERSKLASSE JUGEND/JUNIOR/U25 144 2 Alphanumerisch (2) WERTE: Standard: BLANK,
JG=JUGENDLICHER, JR=JUNIOR,
25=U25
ALTERSKLASSE JUNGER-REITER 146 1 Alphanumerisch (1) WERTE: Standard: BLANK Y=JUNGER
REITER
MITGLIEDSNUMMER 147 8 Numerisch FORMAT: 999999999
TELEFONNUMMER 155 21 Alphanumerisch (21) Standard: BLANK
KADER 176 1 Alphanumerisch (1) derzeit immer BLANK
JAHR (letzte Zahlung) 177 4 Numerisch FORMAT: 9999

GESCHLECHT 181 1 Alphanumerisch (1) Werte: “W”, “M”
GEBURTSDATUM 182 8 Datum FORMAT: JJJJMMTT
FEI-ID 190 10 Alphanumerisch (10) Standard: BLANK (10)
SPERRLISTE 200 1 Alphanumerisch (1) Werte: BLANK = nicht auf Sperrliste,  
“S” = auf Sperrliste, dort nachsehen!
LIZENZINFO 201 10 Alphanumerisch (10) Standard: BLANK

28.07.2021 ÖSTERREICHISCHER PFERDESPORTVERBAND www.oeps.at
FEDERATION EQUESTRE NATIONALE D’AUTRICHE
Pflichtenheft 2021 V2.4 Seite 9 von 10

VEREIN01.DAT
• LISTE DER VEREINE
V-SATZ
Stelle Länge
VEREIN (Nummer) 1 4 Numerisch FORMAT: 9999
VEREINSNAME 5 50 Alphanumerisch (50)

ERGEBNISSE XXXXX.ERG (Code: ASCII Codepage 850)

• TURNIERBEZEICHNUNG  
A-SATZ
Stelle Länge
ID 1 1 Alphanumerisch (1) WERT “A”
TURNIERNUMMER 2 5 Numerisch FORMAT: 99999
TURNIERNAME.ORT 7 25 Alphanumerisch (25)  
VON 32 8 Datum FORMAT: JJJJMMTT
BIS 40 8 Datum FORMAT: JJJJMMTT
KATEGORIE 48 25 Alphanumerisch (25)  
VERSION OEPS 73 3 Alphanumerisch (3) WERT 2.4
VERSION MELDESTELLE 76 20 Alphanumerisch (20) Name und Versionsnummer
LINKID 96 8 Numerisch, optional FORMAT: 99999999, vorlaufende
Nullen
• BEWERBE
VOR DEN ERGEBNISSEN EINES BEWERBES IST EIN B-SATZ ZU STELLEN
Für jeden Bewerb und jede Abteilung einen B-Satz und eine Auflistung der Ergebnisse.
B-SATZ
Stelle Länge
ID 1 1 Alphanumerisch (1) Wert: “B”
BEWERBNUMMER 2 2 Numerisch FORMAT: 99
ABTEILUNG 4 1 Numerisch WERTE: 0=keine Abteilung,  
1=1. Abteilung etc..
BEWERBNAME 5 35 Alphanumerisch (35)  
KLASSE 40 4 Alphanumerisch (4) Bei Westernturnier; “..”

KATEGORIE 44 8 Alphanumerisch (8)  
STARTER 52 3 Numerisch FORMAT: 999
GELDPREIS (Summe) 55 6 Numerisch FORMAT: 999999, 6 ganze Stellen,
keine Kommastellen, Euro ohne
Rundung

BEWERBNUMMER 3-stellig 61 3 Numerisch Format: 999

LINKID 64 8 Numerisch, optional FORMAT: 99999999, vorlaufende
Nullen

• EINGESETZTE RICHTER UND PARCOURSBAUER
C-SATZ Bei ausl. Richtern und Parcours- oder Geländebauern: 000000
Stelle Länge vorlaufende Nullen sind anzuführen
ID 1 1 Alphanumerisch (1) Wert: “C”
BEWERBNUMMER 2 3 Numerisch FORMAT: 999
RICHTER-1 5 6 Numerisch FORMAT: 999999 Standard: 000000
RICHTER-2 11 6 Numerisch FORMAT: 999999 Standard: 000000
RICHTER-3 17 6 Numerisch FORMAT: 999999 Standard: 000000
RICHTER-4 23 6 Numerisch FORMAT: 999999 Standard: 000000
RICHTER-5 29 6 Numerisch FORMAT: 999999 Standard: 000000
RICHTER-6 35 6 Numerisch FORMAT: 999999 Standard: 000000
RICHTER-7 41 6 Numerisch FORMAT: 999999 Standard: 000000
RICHTER-8 47 6 Numerisch FORMAT: 999999 Standard: 000000
PARCOURSBAU 53 6 Numerisch FORMAT: 999999 Standard: 000000
PARCOURSBAU-ASSISTENT 59 6 Numerisch FORMAT: 999999 Standard: 000000

28.07.2021 ÖSTERREICHISCHER PFERDESPORTVERBAND www.oeps.at
FEDERATION EQUESTRE NATIONALE D’AUTRICHE

Pflichtenheft 2021 V2.4 Seite 10 von
10
• ERGEBNIS-ZEILE  
D-SATZ

ID 1 1 Alphanumerisch (1) Wert: “D”
PLATZ 2 3 Numerisch (3) FORMAT: 999. Ausschluss = 997,
Disqualifikation = 999. BEI
EINLAUFSPRINGPRÜFUNGEN: Platz
immer 000 (Ausnahme 997 oder 999)
KOPFNUMMER 5 4 Alphanumerisch (4) Nationale Bewerbe: Die Kopfnummer lt.
OEPS-Registrierung oder die am
Turnier vergebene Y- oder Z-
Kopfnummer.  
Internationale Bewerbe: Die am Turnier
vergeben dreistellige Startnummer mit
vorlaufendem Blank oder Punkt.
PFERDENAME 9 30 Alphanumerisch (30).  
SATZNUMMER DES REITERS 39 6 Alphanumerisch (6) Format: 000000. Bei am Turnier
angelegten Reitern die Nation gem. FEI
Abkürzung anführen (z.B.
Deutschland=GER) und 3 Leerzeichen
FAMILIENNAME 45 50 Alphanumerisch (50)  
VORNAME 95 25 Alphanumerisch (25)

AUSSCHLUSS 120 1 Alphanumerisch (1) Bei Minuspunkten im Ergebnis ” - ”
Disqualifiziert “D”
Ausschluss im Grundparcours, Stechen
bzw. Dressur “A” (bei Stechen ist der
Platz anzugeben)
Teilnahmeverzicht (nur im Stechen) “T”,
der Platz ist anzugeben.
PUNKTE / WERTNOTE 121 6 Numerisch FORMAT: 999999, 4 ganze Stellen, 2
Kommastellen, kein Kommazeichen.
Bei Minuspunkten siehe Ausschluss
ZEIT/PROZENT 127 5 Numerisch FORMAT: 99999, 3 ganze Stellen. 2
Kommastellen, kein Kommazeichen -
Punkten bei Dressurbewerben:
FORMAT: 99999, 2 ganze Stellen, 3
Komastellen, kein Kommazeichen
STECHEN / SR1, SR2 (SR=Siegerrunde) 132 4 Alphanumerisch (4) Kein Stechen ” “, 1. Stechen (SR1,
SR2)”X “, 2. Stechen”XX “, 3.

Stechen “XXX”, 4. Stechen “XXXX”.
GELDPREIS 136 6 Numerisch FORMAT: 999999, 6 ganze Stellen,
keine Kommastellen, volle Euro ohne
Rundung (€ 120,75 = 000120)
NATION 142 3 Alphanumerisch (3) (FEI-  
Code)
PLATZIERT 145 1 Alphanumerisch (1). Standard: Blank, Platziert ” \* ”
FEI-ID Reiter 146 10 Alphanumerisch (10). Standard: BLANK
FEI-PASS 156 10 Alphanumerisch (10). Standard: BLANK

SATZNUMMER DES PFERDES 166 10 Alphanumerisch (10) FORMAT: 0000000000, eventuell mit
vorlaufenden Blanks. Bei am Turnier
angelegten Pferden 10 Blanks
LINKID 176 8 Numerisch, optional FORMAT: 99999999, vorlaufende
Nullen
TRENNZEILE (nach jedem Bewerb)

Anmerkung:
Das Feld NATION im Ergebnis (D) Satz dient dazu, um jedes Ergebnis eindeutig einer Nation zuordnen zu können. Hintergrund: Reiter mit
ausländischer Staatsbürgerschaft (z.B. GER) aber österreichischer Lizenz können, je nach Turnier und ÖTO-Paragraph entweder für ihren
österr. Verein oder als Gastreiter starten. Startet ein ausl. Reiter mit gültiger österr. Lizenz für seinen österr. Verein ist “AUT” einzutragen.
Ist er Gastreiter ist die Staatsbürgerschaft lt. LIZENZ01.dat einzutragen. Die Zuordnung hat für das ganze Turnier Gültigkeit und kann nicht
geändert werden.
28.07.2021 ÖSTERREICHISCHER PFERDESPORTVERBAND www.oeps.at
FEDERATION EQUESTRE NATIONALE D’AUTRICHE
Update Juli 2011

Pflichtenheft 2021 V2.4 Seite 11 von
10

T-SATZ - TAUSCHLISTE
Stelle Länge
ID 1 1 Alphanumerisch (1) Wert: “T”
KOPFNUMMER GENANNTES PFERD 2 4 Alphanumerisch (4)  
LEBENSNUMMER GENANNTES PFERD 6 9 Numerisch FORMAT: 999999999
NAME GENANNTES PFERD 15 30 Alphanumerisch (30)  
KOPFNUMMER EINGETAUSCHTES PFERD 45 4 Alphanumerisch (4)  
LEBENSNR EINGETAUSCHTES PFERD 49 9 Numerisch FORMAT: 999999999
NAME EINGETAUSCHTES PFERD 58 30 Alphanumerisch (30)  
TRENNZEILE
ID 1 1 Alphanumerisch (1) Wert: ” \* ”

N-SATZ - NACHNENNUNGEN

Stelle Länge
ID 1 1 Alphanumerisch (1) Wert: “N”
KOPFNUMMER 2 4 Alphanumerisch (4)  
LEBENSNUMMER 6 9 Numerisch 9 Stellen
PFERDENAME 15 30 Alphanumerisch (30)

Anmerkung:
Einträge in T bzw. N-Satz bei Turnieren der Klasse A, A*, B, B* - bei Turnieren mit Verrechnung einer Nachnenngebühr. Bei C-Turnieren
erfolgen Nennungen über das ZNS ohne Nachnenngebühr. Dient nur für die Erstellung eines Zeitplanes. Bei C-Turnieren werden Einträge
in den T- bzw. N-Sätzen vom OEPS ignoriert.

Ergänzungen zu den Nenndaten (n2-XXXXX.dat):
Der OEPS sorgt dafür, dass für jedes von einem Ausländer genannte Pferd eine eigene Z-Kopfnummer in den Nenndaten
zur Verfügung steht. Es gibt aber Fälle in denen der Pferdename einfach noch nicht bekannt ist (sollte nicht mehr
vorkommen, ist aber nicht auszuschließen). In diesen Fällen (z.B. Stallbesitzer meldet nur dass er mit 6 Pferden kommt)
werden so viele Z-Kopfnummern angelegt wie Pferde genannt werden, die Pferdenamen sind dann nur mehr symbolisch
(1. Pferd, 2. Pferd usw.)
Jeder ausländische Reiter, welcher in der Nenndatei vorkommt erhält eine eigene (freie) Satznummer. Jeder ausländische
Reiter wird im OEPS, wie jede andere Person, mit einer eigenen Satznummer angelegt, es gibt aber keinen
Zusammenhang zwischen Nationalität und der ersten Stelle einer 6-stelligen Satznummer.

Zu den Ergebnisdaten:
Sollten in den Ergebnissen ausländische Reiter aufscheinen, zu denen die Satznummer nicht bekannt ist, so ist wie bisher
in der Spezifikation beschrieben (Feld Satznummer im D-Satz) die Satznummer durch die Nation zu ersetzen (gefolgt von
3 BLANK), zusätzlich ist die Nation in der Spalte „Nation“ zu spezifizieren. Dies erlaubt uns dann diesen Reiter mit
angegebenen Namen und Nation in unserer Datenbank zu finden oder neu anzulegen.

Teilnahmeverzicht:
Ein Teilnahmeverzicht (Code „T“) ist nur im Stechen (SR1, SR2) mit Angabe des Platzes zulässig. Bei Nicht-Antreten zum
Bewerb (Grundumlauf, Dressurbewerbe, 1. Teilbewerb von Vielseitigkeitsprüfungen etc.) wird kein Datensatz eingetragen.  
28.07.2021 ÖSTERREICHISCHER PFERDESPORTVERBAND www.oeps.at
FEDERATION EQUESTRE NATIONALE D’AUTRICHE
