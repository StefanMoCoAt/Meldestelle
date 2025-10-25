# **Infrastruktur-Strategie zur DSGVO-Konformität für das Projekt "Meldestelle"**

Version: 1.0
Datum: 17\. Oktober 2025
Status: In Planung
Ziel: Definition eines phasen basierten Ansatzes zur Entwicklung und zum Betrieb der "Meldestelle"-Anwendung, der von einer pragmatischen Entwicklungsphase zu einem vollständig DSGVO-konformen Produktionsbetrieb übergeht.

## **1\. Zusammenfassung & Zielsetzung**

Dieses Dokument beschreibt die zweistufige Strategie für die Infrastruktur des "Meldestelle"-Projekts. Ziel ist es, in der initialen Entwicklungs- und Testphase maximale Geschwindigkeit und Kosteneffizienz zu ermöglichen, während gleichzeitig ein klar definierter Pfad zur Erreichung vollständiger DSGVO-Konformität für den späteren Live-Betrieb sichergestellt wird.

Wir verfolgen einen pragmatischen Ansatz, der den Aufwand und die Kosten in jeder Phase an die tatsächlichen Risiken und Anforderungen anpasst.

## **2\. Grundprinzipien**

* **Pragmatismus vor Dogmatismus:** In der Entwicklungsphase nutzen wir etablierte, effiziente Cloud-Dienste, auch wenn diese rechtliche Grauzonen aufweisen, solange das Risiko minimal ist (keine externen personenbezogenen Daten).
* **Containerisierung als Schlüssel:** Die gesamte Anwendung und ihre Infrastruktur wird von Anfang an in Docker-Containern betrieben. Dies gewährleistet maximale Portabilität und macht den späteren Wechsel der Hosting-Umgebung trivial.
* **Automatisierung als Ziel:** Eine CI/CD-Pipeline ist von Beginn an integraler Bestandteil, um manuelle Fehler zu reduzieren und den Deployment-Prozess zu standardisieren.
* **Klare Trennlinie:** Es gibt einen klar definierten "Point of no Return": Bevor die erste Zeile personenbezogener Daten von Dritten verarbeitet wird, muss die Migration zu Phase 2 abgeschlossen sein.

## **3\. Phasenplan**

### **Phase 1: Entwicklungs- & Feldversuchs-Phase (MVP)**

**Ziel:** Schnelle Entwicklung, Implementierung von Kernfunktionen, Durchführung von Tests und ersten Feldversuchen in einer kontrollierten Umgebung.

**Dauer:** Von Projektbeginn bis zum Abschluss der Feldversuche und vor der Aufnahme von echten Nutzerdaten.

**Technologie-Stack:**

* **Code-Hosting:** **GitHub** (US-Anbieter)
  * *Begründung:* Exzellente Entwickler-Tools, Marktführer, nahtlose Integrationen.
* **CI/CD-Pipeline:** **GitHub Actions**
  * *Begründung:* Perfekte Integration mit dem Code-Hosting. Der rechenintensive Build-Prozess wird auf leistungsstarke Server von GitHub ausgelagert, was den lokalen Heimserver schont.
* **Hosting-Infrastruktur:** **Proxmox Heimserver** (Intel N100 Mini-PC)
  * *Begründung:* Kostengünstige, flexible und kontrollierte Umgebung für Entwicklung und Tests.
* **Externer Zugriff:** **Cloudflare Tunnel**
  * *Begründung:* Bietet hochsicheren Zugriff auf den Heimserver ohne offene Ports, verbirgt die private IP-Adresse und ist einfach zu verwalten.

**DSGVO-Bewertung dieser Phase:**

* **Status:** **Nicht streng DSGVO-konform.**
* **Risiko:** **Akzeptabel und kontrolliert.**
* **Begründung:** Personenbezogene Daten (Name/E-Mail des Entwicklers in Git-Commits) werden an einen US-Anbieter (GitHub/Microsoft) übertragen. Da in dieser Phase keine externen oder sensiblen personenbezogenen Daten im Code oder den Systemen verarbeitet werden, wird dieses Restrisiko bewusst in Kauf genommen. Die rechtliche Grundlage bilden die Standardvertragsklauseln (SCCs) und das Trans-Atlantic Data Privacy Framework (TADPF).

### **Phase 2: Go-Live & Betrieb (Produktionsumgebung)**

**Ziel:** Bereitstellung der Anwendung für die Öffentlichkeit in einer hochverfügbaren, sicheren und vollständig DSGVO-konformen Umgebung.

**Trigger für die Migration:** Der Abschluss der Feldversuche und die geplante Verarbeitung von personenbezogenen Daten von echten Nutzern.

**Technologie-Stack (Phase 2 \- Go-Live & Betrieb):**

* **Hosting-Infrastruktur:** **Virtual Private Server (VPS) bei einem EU-Anbieter** (z.B. Hetzner, Standort Deutschland).
  * *Begründung:* Gewährleistet, dass alle Daten und Prozesse die EU physisch nicht verlassen (Datenhoheit). Bietet professionelle Performance und Zuverlässigkeit.
* **Code-Hosting & CI/CD:** **Self-hosted Forgejo**, installiert auf dem VPS.
  * *Begründung:* Forgejo ist eine leichtgewichtige, von der Community betriebene Open-Source-Alternative. Es bietet Git-Hosting und ein integriertes CI/CD-System (Forgejo Actions), das mit GitHub Actions kompatibel ist. Der gesamte Lebenszyklus des Codes (Speicherung, Bau, Test) findet auf dem eigenen Server in Deutschland statt.
* **Container Registry:** Die in Forgejo integrierte Container Registry.
  * *Begründung:* Hält auch die fertigen Docker-Images innerhalb der eigenen, konformen Infrastruktur.
* **Externer Zugriff:** Standard-Reverse-Proxy (z.B. Traefik oder Nginx) direkt auf dem VPS.
  * *Begründung:* Der Server hat eine öffentliche IP, ein Tunnel ist nicht mehr nötig. Der Proxy steuert den Zugriff auf die Anwendungs-Container.

### **DSGVO-Bewertung (Phase 2\)**

* **Status:** **Vollständig DSGVO-konform.**
* **Risiko:** **Minimal.**
* **Begründung:** Der gesamte Datenverarbeitungsprozess – von der Codezeile in **Forgejo**, über den Build-Prozess durch **Forgejo Actions**, bis zur laufenden Anwendung und den Nutzerdaten in der Datenbank – findet ausschließlich auf Servern in Deutschland unter eigener Kontrolle statt.

### **4\. Migrationsschritte von Phase 1 zu Phase 2**

1. **Infrastruktur aufsetzen:** Einen geeigneten VPS bei Hetzner mieten und mit einem schlanken Debian-System grundlegend absichern.
2. **Forgejo installieren:** Forgejo als Docker-Container auf dem neuen VPS installieren und konfigurieren.
3. **Code migrieren:** Das "Meldestelle"-Repository von GitHub auf die eigene Forgejo-Instanz spiegeln/umziehen.
4. **Pipeline adaptieren:** Die GitHub Actions-Workflows in die Forgejo Actions-Konfiguration überführen. **Da Forgejo Actions weitgehend mit GitHub Actions kompatibel ist, ist dieser Schritt deutlich einfacher als eine vollständige Portierung zu einem anderen System.**
5. **Anwendung deployen:** Die CI/CD-Pipeline in Forgejo erstmals ausführen, um die "Meldestelle"-Anwendung (Docker-Container) auf dem VPS zu deployen.
6. **DNS-Umschaltung:** Die DNS-Einträge für mo-code.at (und Subdomains) bei Cloudflare vom Tunnel auf die neue, feste IP-Adresse des Hetzner-Servers umstellen.
7. **Decommissioning:** Nach erfolgreichem Testbetrieb den Cloudflare Tunnel und die alten GitHub-Workflows deaktivieren.

## **5\. Zeit- und Kostenschätzung**

* **Phase 1:**
  * **Kosten:** Minimal (Stromkosten für Heimserver). Die genutzten Dienste (GitHub, Cloudflare) sind im Rahmen des Projekts kostenlos.
  * **Zeitaufwand:** Fokus liegt zu 100% auf der Anwendungsentwicklung.
* **Phase 2:**
  * **Kosten:** Monatliche Gebühren für den VPS (ca. 15-30 €/Monat, je nach Größe).
  * **Zeitaufwand:** Für die Migration von Phase 1 zu 2 sollte ein dediziertes Zeitfenster von **ca. 1-2 Wochen** eingeplant werden, um alle Schritte sorgfältig durchzuführen und zu testen.
