# ADR-0001: Modulare Architektur

## Status

Akzeptiert

## Kontext

Das Meldestelle-System wurde ursprünglich als monolithische Anwendung entwickelt. Mit zunehmender Komplexität und Größe
des Systems traten mehrere Herausforderungen auf:

1. Der Quellcode wurde schwer zu warten und zu verstehen
2. Entwicklungsteams mussten sich eng koordinieren, was die Entwicklung verlangsamte
3. Die gesamte Anwendung musste skaliert werden, auch wenn nur bestimmte Teile mehr Ressourcen benötigten
4. Technologieentscheidungen wurden durch die monolithische Architektur eingeschränkt

Das Team musste entscheiden, ob es mit dem monolithischen Ansatz fortfahren oder zu einer modularenen Architektur
migrieren sollte.

## Entscheidung

Wir haben uns entschieden, von einer monolithischen Struktur zu einer modularen Architektur zu migrieren und das System
in die folgenden Module zu organisieren:

- **core**: Gemeinsame Kernkomponenten
- **masterdata**: Stammdatenverwaltung
- **members**: Mitgliederverwaltung
- **horses**: Pferderegistrierung
- **events**: Veranstaltungsverwaltung
- **infrastructure**: Gemeinsame Infrastrukturkomponenten
- **client**: Client-Anwendungen

Jedes Domänenmodul (masterdata, members, horses, events) folgt einem Clean-Architecture-Ansatz mit separaten API-,
Anwendung-, Domänen-, Infrastruktur- und Service-Schichten.

## Konsequenzen

### Positive

- **Verbesserte Wartbarkeit**: Kleinere, fokussierte Module sind leichter zu verstehen und zu warten
- **Unabhängige Entwicklung**: Teams können an verschiedenen Modulen mit minimaler Koordination arbeiten
- **Selektive Skalierung**: Einzelne Module können basierend auf ihren spezifischen Anforderungen skaliert werden
- **Technologieflexibilität**: Verschiedene Module können je nach Bedarf unterschiedliche Technologien verwenden
- **Klare Grenzen**: Domänengrenzen sind explizit definiert, was die konzeptionelle Integrität des Systems verbessert

### Negative

- **Erhöhte Komplexität**: Die Gesamtsystemarchitektur ist komplexer
- **Deployment-Overhead**: Mehr Komponenten müssen bereitgestellt und verwaltet werden
- **Leistungsüberlegungen**: Modulübergreifende Kommunikation fügt Latenz hinzu
- **Migrationsaufwand**: Erheblicher Aufwand erforderlich, um von der monolithischen Struktur zu migrieren

### Neutral

- **Teamorganisation**: Teams müssen um Module statt um Features herum organisiert werden
- **Dokumentationsbedarf**: Umfassendere Dokumentation ist erforderlich, um das System als Ganzes zu verstehen

## Betrachtete Alternativen

### Erweiterter Monolith

Wir haben in Betracht gezogen, die interne Struktur des Monolithen mit besseren Modulgrenzen zu verbessern, ihn aber als
eine einzige bereitstellbare Einheit zu behalten. Dies wäre einfacher bereitzustellen gewesen, hätte aber die Probleme
mit der Skalierung und Technologieflexibilität nicht gelöst.

### Microservices

Wir haben einen feingranularen Microservices-Ansatz mit vielen kleineren Diensten in Betracht gezogen. Dies hätte
maximale Flexibilität geboten, aber für unsere aktuellen Bedürfnisse übermäßige Komplexität und betrieblichen Overhead
eingeführt.

## Referenzen

- [Modular Monoliths von Simon Brown](https://meldestelle-pro.youtrack.cloud/api/files/526-8?sign=MTc2MjU2MDAwMDAwMHwyLTF8NTI2LTh8QldrSXd1MHoyUlE1T3lZSjBDNVh4Ry1zcGZZM1lWSlE0VXN2M2FQSXNDbw0K&updated=1762338956551)
- [Clean Architecture von Robert C. Martin](https://meldestelle-pro.youtrack.cloud/api/files/526-10?sign=MTc2MjU2MDAwMDAwMHwyLTF8NTI2LTEwfF9XbVdSakVpSW5HV1VjalY3UjhCMGFub2NIQXdPTUkyM3FFTnNTdGNIRmsNCg&updated=1762339225451)
