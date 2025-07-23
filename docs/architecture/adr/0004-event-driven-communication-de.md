# ADR-0004: Ereignisgesteuerte Kommunikation

## Status

Akzeptiert

## Kontext

Mit der Einführung einer Microservices-Architektur ([ADR-0003](0003-microservices-architecture-de.md)) mussten wir die effektivste Art der Kommunikation zwischen den Diensten bestimmen. Zu den wichtigsten Überlegungen gehörten:

1. Lose Kopplung zwischen Diensten, um ihre Unabhängigkeit zu erhalten
2. Asynchrone Verarbeitungsfähigkeiten zur Verbesserung der Systemresilienz und Skalierbarkeit
3. Zuverlässige Kommunikation, um sicherzustellen, dass wichtige Informationen nicht verloren gehen
4. Unterstützung für komplexe Workflows, die mehrere Dienste umfassen
5. Fähigkeit, den Zustand des Systems für Audit- und Debugging-Zwecke zu rekonstruieren

## Entscheidung

Wir haben uns entschieden, ein ereignisgesteuertes Kommunikationsmuster mit Apache Kafka als Message Broker zu implementieren. Die wichtigsten Aspekte dieses Ansatzes umfassen:

1. **Domänen-Ereignisse**: Dienste veröffentlichen Domänen-Ereignisse, wenn signifikante Zustandsänderungen auftreten
2. **Event Sourcing**: Für kritische Daten speichern wir alle Ereignisse, die zum aktuellen Zustand geführt haben
3. **Nachrichtenbasierte Kommunikation**: Dienste kommunizieren hauptsächlich über asynchrone Nachrichten
4. **Choreographie**: Komplexe Workflows werden durch Ereignis-Choreographie statt Orchestrierung implementiert
5. **Ereignis-Schema-Registry**: Wir führen eine Registry von Ereignis-Schemas, um Kompatibilität zu gewährleisten

Die Implementierung umfasst:
- Kafka als zentraler Message Broker
- Schema-Registry zur Verwaltung von Ereignis-Schemas
- Ereignis-Handler in jedem Dienst zur Verarbeitung von Ereignissen aus anderen Diensten
- Ereignis-Publisher in jedem Dienst zur Veröffentlichung von Domänen-Ereignissen

## Konsequenzen

### Positive

- **Lose Kopplung**: Dienste sind entkoppelt und teilen nur die Ereignis-Verträge
- **Skalierbarkeit**: Asynchrone Verarbeitung ermöglicht bessere Skalierbarkeit unter Last
- **Resilienz**: Dienste können weiter funktionieren, auch wenn andere Dienste nicht verfügbar sind
- **Audit-Trail**: Event Sourcing bietet einen vollständigen Audit-Trail aller Zustandsänderungen
- **Flexibilität**: Neue Konsumenten können hinzugefügt werden, ohne Publisher zu modifizieren

### Negative

- **Eventuelle Konsistenz**: Das System ist letztendlich konsistent, was schwer zu verstehen sein kann
- **Komplexität**: Ereignisgesteuerte Systeme sind komplexer zu entwerfen, zu implementieren und zu debuggen
- **Reihenfolgegarantien**: Die korrekte Reihenfolge von Ereignissen sicherzustellen kann herausfordernd sein
- **Idempotenz**: Dienste müssen doppelte Ereignisse korrekt behandeln
- **Lernkurve**: Entwickler müssen ereignisgesteuerte Muster und Praktiken erlernen

### Neutral

- **Überwachungsbedarf**: Umfassende Überwachung ist erforderlich, um den Ereignisfluss zu verfolgen
- **Testansatz**: Teststrategien müssen asynchrones Verhalten berücksichtigen

## Betrachtete Alternativen

### Synchrone REST-APIs

Wir haben die Verwendung synchroner REST-APIs als primären Kommunikationsmechanismus in Betracht gezogen. Dies wäre einfacher zu implementieren und zu debuggen gewesen, hätte aber zu einer engeren Kopplung zwischen Diensten und verringerter Resilienz geführt.

### Request-Response-Messaging

Wir haben ein Request-Response-Messaging-Muster in Betracht gezogen, bei dem Dienste Anfragen senden und auf Antworten warten. Dies hätte einige der Vorteile asynchroner Kommunikation geboten und gleichzeitig ein vertrautes Request-Response-Modell beibehalten, hätte aber das Publish-Subscribe-Muster nicht so effektiv unterstützt.

### GraphQL-Federation

Wir haben die Verwendung von GraphQL-Federation zur Zusammensetzung von APIs aus mehreren Diensten in Betracht gezogen. Dies hätte eine einheitliche API für Clients geboten, hätte aber eine enge Kopplung zwischen Diensten beibehalten und asynchrone Workflows nicht so effektiv unterstützt.

## Referenzen

- [Enterprise Integration Patterns](https://www.enterpriseintegrationpatterns.com/)
- [Event-Driven Architecture von Martin Fowler](https://martinfowler.com/articles/201701-event-driven.html)
- [Apache Kafka Dokumentation](https://kafka.apache.org/documentation/)
- [Event Sourcing Pattern](https://docs.microsoft.com/de-de/azure/architecture/patterns/event-sourcing)
