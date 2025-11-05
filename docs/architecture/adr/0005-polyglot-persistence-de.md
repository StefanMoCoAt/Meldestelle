# ADR-0005: Polyglotte Persistenz

## Status

Akzeptiert

## Kontext

Als Teil unserer Microservices-Architektur ([ADR-0003](0003-microservices-architecture-de.md)) mussten wir die am besten
geeignete Datenspeicherstrategie bestimmen. Verschiedene Teile unseres Systems haben unterschiedliche Anforderungen an
die Datenspeicherung:

1. Einige Daten erfordern starke Konsistenz und komplexe Beziehungen
2. Einige Daten müssen mit sehr geringer Latenz abgerufen werden
3. Einige Daten sind ereignisbasiert und müssen in einem Zeitreihenformat gespeichert werden
4. Verschiedene Dienste haben unterschiedliche Datenzugriffsmuster

Ein Einheitsansatz für die Datenspeicherung würde Kompromisse erzwingen, die die Leistung, Skalierbarkeit oder
Entwicklungsproduktivität beeinträchtigen könnten.

## Entscheidung

Wir haben uns entschieden, eine polyglotte Persistenzstrategie zu implementieren, die verschiedene
Datenspeichertechnologien für verschiedene Anwendungsfälle nutzt:

1. **PostgresQL**: Als primäre relationale Datenbank zur Speicherung strukturierter Daten mit komplexen Beziehungen
  - Wird von allen Domänendiensten für ihre primäre Datenspeicherung verwendet
  - Jeder Dienst hat sein eigenes Datenbankschema, um Isolation zu gewährleisten

2. **Redis**: Als verteilter Cache für schnellen Datenzugriff
  - Wird für das Caching häufig abgerufener Daten verwendet
  - Wird für die Sitzungsspeicherung verwendet
  - Wird für Rate-Limiting verwendet

3. **Kafka**: Als Event-Store für Event Sourcing
  - Wird zur Speicherung von Domänenereignissen für Event Sourcing verwendet
  - Ermöglicht Event-Replay zum Wiederaufbau des Zustands

4. **Elasticsearch** (geplant): für Volltextsuchfunktionen
  - Wird für erweiterte Suchfunktionen über mehrere Domänen hinweg verwendet werden

Jeder Dienst ist für die Verwaltung seiner eigenen Datenspeicherung verantwortlich, und Dienste dürfen nicht direkt auf
die Datenbanken anderer Dienste zugreifen.

## Konsequenzen

### Positive

- **Optimierte Leistung**: Jede Art von Daten wird in der am besten geeigneten Speichertechnologie gespeichert
- **Skalierbarkeit**: Verschiedene Speichertechnologien können unabhängig voneinander basierend auf ihren spezifischen
  Anforderungen skaliert werden
- **Flexibilität**: Teams können die beste Speichertechnologie für ihre spezifischen Anwendungsfälle wählen
- **Resilienz**: Probleme mit einer Speichertechnologie beeinträchtigen nicht unbedingt andere

### Negative

- **Betriebliche Komplexität**: Mehrere Speichertechnologien müssen bereitgestellt, überwacht und gewartet werden
- **Herausforderungen bei der Datenkonsistenz**: Die Aufrechterhaltung der Konsistenz über verschiedene
  Speichertechnologien hinweg erfordert sorgfältiges Design
- **Lernkurve**: Teams müssen mit mehreren Speichertechnologien vertraut sein
- **Komplexität bei Backup und Wiederherstellung**: Verschiedene Speichertechnologien haben unterschiedliche Backup- und
  Wiederherstellungsverfahren

### Neutral

- **Daten-Governance**: Umfassende Daten-Governance ist über alle Speichertechnologien hinweg erforderlich
- **Überwachungsbedarf**: Jede Speichertechnologie erfordert ihren eigenen Überwachungsansatz

## Betrachtete Alternativen

### Einzelne Datenbank für alle Dienste

Wir haben die Verwendung einer einzelnen PostgresQL-Datenbank mit separaten Schemas für jeden Dienst in Betracht
gezogen. Dies hätte den Betrieb vereinfacht, hätte aber einen Single Point of Failure geschaffen und hätte es uns nicht
ermöglicht, für verschiedene Datenzugriffsmuster zu optimieren.

### Datenbank pro Dienst, gleiche Technologie

Wir haben die Verwendung von PostgresQL für alle Dienste, aber mit separaten Datenbanken in Betracht gezogen. Dies hätte
Dienstisolation geboten und gleichzeitig den Betrieb vereinfacht, hätte es uns aber nicht ermöglicht, für verschiedene
Datenzugriffsmuster zu optimieren.

### Vollständig verteilter NoSQL-Ansatz

Wir haben die Verwendung eines vollständig verteilten NoSQL-Ansatzes mit Technologien wie Cassandra oder MongoDB für die
gesamte Datenspeicherung in Betracht gezogen. Dies hätte eine ausgezeichnete Skalierbarkeit geboten, hätte aber die
Modellierung komplexer Beziehungen erschwert und hätte signifikante Änderungen an unseren Entwicklungspraktiken
erfordert.

## Referenzen

- [Polyglot Persistence von Martin Fowler](https://meldestelle-pro.youtrack.cloud/api/files/526-28?sign=MTc2MjU2MDAwMDAwMHwyLTF8NTI2LTI4fERaVkFWVmlEbVJJbTVZSFE2SWlrbmRydHNaeDdxZUFaRExpdkNxbk9wVEkNCg&updated=1762343428460)
- [PostgresQL Dokumentation](https://www.postgresql.org/docs/)
- [Redis Dokumentation](https://redis.io/documentation)
- [Apache Kafka Dokumentation](https://kafka.apache.org/documentation/)
- [Elasticsearch Dokumentation](https://www.elastic.co/docs/solutions/search)
