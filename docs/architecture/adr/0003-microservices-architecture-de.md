# ADR-0003: Microservices-Architektur

## Status

Akzeptiert

## Kontext

Nach der Entscheidung, eine modulare Architektur ([ADR-0001](0001-modular-architecture-de.md)) und Domain-Driven Design ([ADR-0002](0002-domain-driven-design-de.md)) zu übernehmen, mussten wir die Deployment-Strategie für unsere Module festlegen. Zu den wichtigsten Überlegungen gehörten:

1. Unabhängige Skalierbarkeit verschiedener Teile des Systems
2. Deployment-Unabhängigkeit, um Teams zu ermöglichen, Änderungen ohne Koordination mit anderen Teams zu veröffentlichen
3. Technologieunabhängigkeit, um verschiedenen Diensten die Verwendung unterschiedlicher Technologien nach Bedarf zu ermöglichen
4. Resilienz, um sicherzustellen, dass Ausfälle in einem Teil des Systems nicht das gesamte System beeinträchtigen
5. Klare Zuständigkeitsgrenzen, die mit den Teamverantwortlichkeiten übereinstimmen

## Entscheidung

Wir haben uns entschieden, eine Microservices-Architektur zu implementieren, bei der jedes Domänenmodul als separater Dienst bereitgestellt wird:

- **masterdata-service**: Verwaltet Stammdaten wie Standorte, Disziplinen usw.
- **members-service**: Verwaltet Mitgliederregistrierung und -profile
- **horses-service**: Verwaltet Pferderegistrierung und -informationen
- **events-service**: Verwaltet Veranstaltungserstellung, -planung und -anmeldungen

Jeder Dienst:

- Hat sein eigenes Datenbankschema
- Ist unabhängig bereitstellbar
- Kommuniziert mit anderen Diensten über klar definierte APIs und nachrichtenbasierte Kommunikation
- Ist für seine eigene Domänenlogik gemäß DDD-Prinzipien verantwortlich

Wir haben auch unterstützende Infrastrukturdienste implementiert:

- **gateway**: API-Gateway für Routing und Authentifizierung
- **auth**: Authentifizierungs- und Autorisierungsdienst (Keycloak)
- **cache**: Caching-Dienst (Redis)
- **messaging**: Message Broker für die Kommunikation zwischen Diensten (Kafka)
- **monitoring**: Überwachungs- und Beobachtbarkeitsdienste

## Konsequenzen

### Positive

- **Unabhängige Skalierbarkeit**: Jeder Dienst kann basierend auf seinen spezifischen Lastanforderungen skaliert werden
- **Deployment-Unabhängigkeit**: Teams können Änderungen an ihren Diensten bereitstellen, ohne sich mit anderen Teams abstimmen zu müssen
- **Technologieflexibilität**: Verschiedene Dienste können je nach Bedarf unterschiedliche Technologien verwenden
- **Resilienz**: Ausfälle in einem Dienst beeinträchtigen nicht unbedingt andere
- **Klare Zuständigkeit**: Jeder Dienst hat klare Zuständigkeitsgrenzen, die mit den Teamverantwortlichkeiten übereinstimmen
- **Kleinere Codebasen**: Jeder Dienst hat eine kleinere, fokussiertere Codebasis

### Negative

- **Komplexität verteilter Systeme**: Microservices bringen die Herausforderungen verteilter Systeme mit sich
- **Betrieblicher Mehraufwand**: Mehr Dienste müssen bereitgestellt, überwacht und gewartet werden
- **Herausforderungen bei der Datenkonsistenz**: Die Aufrechterhaltung der Datenkonsistenz über Dienste hinweg erfordert sorgfältiges Design
- **Netzwerklatenz**: Die Kommunikation zwischen Diensten fügt Latenz hinzu
- **Testkomplexität**: End-to-End-Tests werden komplexer

### Neutral

- **Teamorganisation**: Teams müssen um Dienste statt um Features herum organisiert werden
- **Dokumentationsbedarf**: Dienstschnittstellen und -interaktionen müssen gut dokumentiert sein

## Betrachtete Alternativen

### Modularer Monolith

Wir haben die Implementierung eines modularen Monolithen in Betracht gezogen, bei dem alle Module als eine einzige Anwendung bereitgestellt würden, jedoch mit klaren Modulgrenzen. Dies wäre einfacher bereitzustellen gewesen und hätte die Herausforderungen verteilter Systeme vermieden, hätte aber nicht die Vorteile der unabhängigen Skalierbarkeit und Bereitstellung geboten.

### Service-basierte Architektur

Wir haben eine dienstbasierte Architektur mit weniger, größeren Diensten in Betracht gezogen, die mehrere Domänenbereiche umfassen würden. Dies hätte den betrieblichen Overhead reduziert, aber es schwieriger gemacht, klare Domänengrenzen und unabhängige Skalierbarkeit aufrechtzuerhalten.

## Referenzen

- [Microservices von Martin Fowler](https://martinfowler.com/articles/microservices.html)
- [Building Microservices von Sam Newman](https://samnewman.io/books/building_microservices/)
- [Microservices Patterns von Chris Richardson](https://microservices.io/book)
