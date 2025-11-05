# ADR-0002: Domain-Driven Design

## Status

Akzeptiert

## Kontext

Mit der Weiterentwicklung des Meldestelle-Systems zur Bewältigung komplexer Geschäftsregeln für die Verwaltung von
Reitsportveranstaltungen standen wir vor folgenden Herausforderungen:

1. Aufrechterhaltung einer klaren Trennung zwischen Geschäftslogik und technischen Belangen
2. Sicherstellung, dass das System das Verständnis der Domänenexperten vom Problemraum genau widerspiegelt
3. Schaffung einer gemeinsamen Sprache zwischen technischen und nicht-technischen Stakeholdern
4. Organisation des Codes in einer Weise, die die Geschäftsdomänen widerspiegelt

Wir benötigten einen architektonischen Ansatz, der diese Herausforderungen adressiert und eine solide Grundlage für die
in [ADR-0001](0001-modular-architecture-de.md) beschriebene modulare Architektur bietet.

## Entscheidung

Wir haben uns entschieden, Domain-Driven Design (DDD)-Prinzipien für die Organisation unseres Quellcodes und die
Gestaltung unseres Systems zu übernehmen. Dies umfasst:

1. **Ubiquitäre Sprache**: Entwicklung einer gemeinsamen Sprache, die von Domänenexperten und Entwicklern geteilt wird
2. **Bounded Contexts**: Definition expliziter Grenzen zwischen verschiedenen Domänenbereichen (masterdata, members, horses, events)
3. **Schichtenarchitektur**: Organisation jedes Domänenmoduls in Schichten:

   - Domänenschicht: Enthält Domänenmodelle, Entitäten, Wertobjekte und Domänendienste
   - Anwendungsschicht: Enthält Anwendungsdienste, Anwendungsfälle und Befehls-/Abfragehandler
   - Infrastrukturschicht: Enthält technische Implementierungen von Repositories, Messaging usw.
   - API-Schicht: Definiert die Schnittstellen für die Interaktion mit der Domäne
   
4. **Aggregate**: Identifizierung von Aggregat-Roots, die Konsistenzgrenzen aufrechterhalten
5. **Repositories**: Verwendung des Repository-Musters zur Abstraktion des Datenzugriffs
6. **Domänen-Events**: Verwendung von Events zur Kommunikation zwischen Bounded Contexts

## Konsequenzen

### Positive

- **Business-Technologie-Ausrichtung**: Die Codestruktur spiegelt direkt die Geschäftsdomänen wider
- **Verbesserte Kommunikation**: Ubiquitäre Sprache erleichtert die Kommunikation zwischen technischen und nicht-technischen Stakeholdern
- **Wartbarkeit**: Klare Trennung der Belange macht den Code leichter zu warten
- **Testbarkeit**: Domänenlogik kann unabhängig von Infrastrukturbelangen getestet werden
- **Flexibilität**: Änderungen in einem Bounded Context haben minimale Auswirkungen auf andere

### Negative

- **Lernkurve**: DDD-Konzepte erfordern Zeit, um sie richtig zu erlernen und anzuwenden
- **Initialer Entwicklungsaufwand**: Mehr Vorabdesign und Diskussion ist erforderlich
- **Potenzielle Überentwicklung**: Risiko, komplexe DDD-Muster anzuwenden, wo einfachere Lösungen ausreichen würden

### Neutral

- **Teamorganisation**: Teams benötigen Domänenwissen sowie technische Fähigkeiten
- **Dokumentationsbedarf**: Domänenmodelle und Bounded Contexts müssen gut dokumentiert sein

## Betrachtete Alternativen

### Transaction Script Pattern

Wir haben die Verwendung eines einfacheren Transaction Script Patterns in Betracht gezogen, bei dem die Geschäftslogik
um Prozeduren statt um Domänenobjekte organisiert ist. Dies wäre anfänglich einfacher zu implementieren gewesen, wäre
aber mit zunehmender Komplexität der Geschäftslogik schwieriger zu warten geworden.

### Anemic Domain Model

Wir haben die Verwendung eines anämischen Domänenmodells in Betracht gezogen, bei dem Domänenobjekte einfache
Datencontainer sind und die Geschäftslogik in separaten Serviceklassen liegt. Dies wäre für Entwickler mit
CRUD-basiertem Hintergrund vertrauter gewesen, hätte aber nicht die Vorteile der Kapselung und der reichhaltigen
Domänenmodellierung geboten.

## Referenzen

- [Domain-Driven Design von Eric Evans](https://meldestelle-pro.youtrack.cloud/api/files/526-11?sign=MTc2MjU2MDAwMDAwMHwyLTF8NTI2LTExfE9KWGJodXhlT0Q0UEdYREpfNllaV1RXakU4YUZYbXZJb1JIdjJDVWVnZkUNCg&updated=1762339700786)
- [Implementing Domain-Driven Design von Vaughn Vernon](https://meldestelle-pro.youtrack.cloud/api/files/526-14?sign=MTc2MjU2MDAwMDAwMHwyLTF8NTI2LTE0fEx6NXJCT2NrMFNWMHRfc1pIbVRTc04xQ1Q2OEtEdzdMc1djaTNIMmRCNFENCg&updated=1762340142201)
- [Clean Architecture von Robert C. Martin](https://meldestelle-pro.youtrack.cloud/api/files/526-10?sign=MTc2MjU2MDAwMDAwMHwyLTF8NTI2LTEwfF9XbVdSakVpSW5HV1VjalY3UjhCMGFub2NIQXdPTUkyM3FFTnNTdGNIRmsNCg&updated=1762339225451)
